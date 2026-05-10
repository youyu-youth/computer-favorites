package com.yyyouth.service.ratelimit.aspect;

import cn.dev33.satoken.stp.StpUtil;
import com.yyyouth.common.constants.HttpStatus;
import com.yyyouth.common.exception.BusinessException;
import com.yyyouth.service.ratelimit.annotation.RateLimit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.Collections;

/**
 * @author yyyouth zg
 * @date 2026-05-09
 *
 * 接口限流切面
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {

    private static final ParserContext TEMPLATE_PARSER_CONTEXT = ParserContext.TEMPLATE_EXPRESSION;

    private static final String KEY_PREFIX = "rate:limit:";

    /**
     * 原子限流 Lua 脚本（固定窗口）。
     * KEYS[1] = 限流 key
     * ARGV[1] = 窗口秒数
     * ARGV[2] = 窗口内允许次数 limit
     * 返回值：1 = 放行，0 = 被限流
     *
     * 原子语义：
     *  - INCR 后如果是首次创建，同一调用中设 EXPIRE（不会出现 INCR 成功但 EXPIRE 失败导致永不过期的 bug）
     *  - 超过 limit 返回 0，切面在 Java 侧拋限流异常
     */
    private static final RedisScript<Long> RATE_LIMIT_SCRIPT = new DefaultRedisScript<>(
            "local current = redis.call('INCR', KEYS[1]) " +
                    "if current == 1 then redis.call('EXPIRE', KEYS[1], ARGV[1]) end " +
                    "if current > tonumber(ARGV[2]) then return 0 end " +
                    "return 1",
            Long.class
    );

    private final StringRedisTemplate stringRedisTemplate;

    private final ExpressionParser expressionParser = new SpelExpressionParser();

    private final ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    /**
     * 限流环绕通知
     *
     * @param joinPoint 切点
     * @param rateLimit 限流注解
     * @return 原始返回值
     * @throws Throwable 业务异常
     */
    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        String key = buildKey(joinPoint, rateLimit);
        long windowSeconds = Math.max(1L, rateLimit.unit().toSeconds(rateLimit.window()));
        try {
            // Lua 原子执行：INCR + 首次 EXPIRE + 阈值判断三合一，根除跨调用原子性 bug
            Long allowed = stringRedisTemplate.execute(
                    RATE_LIMIT_SCRIPT,
                    Collections.singletonList(key),
                    String.valueOf(windowSeconds),
                    String.valueOf(rateLimit.limit())
            );
            if (allowed != null && allowed == 0L) {
                log.warn("接口触发限流，key={}, limit={}, window={}s", key, rateLimit.limit(), windowSeconds);
                throw new BusinessException(HttpStatus.BAD_REQUEST, rateLimit.message());
            }
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            // Redis 不可用时降级放行，不阻断业务主路径
            log.warn("限流组件异常，降级放行，key={}, error={}", key, ex.getMessage());
        }
        return joinPoint.proceed();
    }

    /**
     * 构建限流 key
     *
     * @param joinPoint 切点
     * @param rateLimit 限流注解
     * @return 限流 key
     */
    private String buildKey(ProceedingJoinPoint joinPoint, RateLimit rateLimit) {
        String resolved = resolveExpression(joinPoint, rateLimit.key());
        if (!StringUtils.hasText(resolved)) {
            Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
            resolved = method.getDeclaringClass().getName() + ":" + method.getName();
        }
        return KEY_PREFIX + resolved;
    }

    /**
     * 解析 SpEL 模板表达式
     *
     * @param joinPoint 切点
     * @param expression 表达式
     * @return 解析结果
     */
    private String resolveExpression(ProceedingJoinPoint joinPoint, String expression) {
        if (!StringUtils.hasText(expression)) {
            return "";
        }
        try {
            Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
            Object[] args = joinPoint.getArgs();
            StandardEvaluationContext context = new StandardEvaluationContext();
            String[] parameterNames = parameterNameDiscoverer.getParameterNames(method);
            if (parameterNames != null) {
                for (int index = 0; index < parameterNames.length; index++) {
                    context.setVariable(parameterNames[index], args[index]);
                }
            }
            for (int index = 0; index < args.length; index++) {
                context.setVariable("p" + index, args[index]);
                context.setVariable("a" + index, args[index]);
            }
            context.setVariable("loginId", resolveLoginId());
            Object value = expressionParser.parseExpression(expression, TEMPLATE_PARSER_CONTEXT).getValue(context);
            return value == null ? "" : String.valueOf(value);
        } catch (Exception ex) {
            log.warn("解析限流表达式失败，expression={}, error={}", expression, ex.getMessage());
            return expression;
        }
    }

    /**
     * 获取当前登录用户 ID，未登录返回 anonymous
     *
     * @return 登录用户 ID 或 anonymous
     */
    private String resolveLoginId() {
        try {
            return StpUtil.isLogin() ? String.valueOf(StpUtil.getLoginId()) : "anonymous";
        } catch (Exception ex) {
            return "anonymous";
        }
    }
}

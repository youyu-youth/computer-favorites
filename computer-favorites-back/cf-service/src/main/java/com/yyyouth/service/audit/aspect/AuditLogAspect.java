package com.yyyouth.service.audit.aspect;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.json.JSONUtil;
import com.yyyouth.service.audit.annotation.AuditLog;
import com.yyyouth.service.mapper.system.AuditLogMapper;
import com.yyyouth.service.user.auth.support.StpAdminUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author yyyouth zg
 * @date 2026-04-16
 *
 * 审计日志切面
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "audit.log", name = "enabled", havingValue = "true", matchIfMissing = true)
public class AuditLogAspect {

    private static final int AUDIT_SUCCESS_RESULT = 1;

    private static final int AUDIT_FAILED_RESULT = 0;

    private static final String ADMIN_USER_TYPE = "admin";

    private static final String USER_USER_TYPE = "user";

    private static final String ANONYMOUS_USER_TYPE = "anonymous";

    private static final int MAX_ERROR_MSG_LENGTH = 500;

    private static final ParserContext TEMPLATE_PARSER_CONTEXT = ParserContext.TEMPLATE_EXPRESSION;

    private final AuditLogMapper auditLogMapper;

    private final ExpressionParser expressionParser = new SpelExpressionParser();

    private final ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    /**
     * 审计注解切点
     */
    @Pointcut("@annotation(com.yyyouth.service.audit.annotation.AuditLog) || @within(com.yyyouth.service.audit.annotation.AuditLog)")
    public void auditPointcut() {
    }

    /**
     * 环绕通知，统一处理审计日志
     *
     * @param joinPoint 切点
     * @return 原始返回值
     * @throws Throwable 业务异常
     */
    @Around("auditPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        Class<?> targetClass = joinPoint.getTarget().getClass();
        ResolvedAuditConfig auditConfig = resolveAuditConfig(method, targetClass);
        if (!auditConfig.enabled()) {
            return joinPoint.proceed();
        }

        HttpServletRequest request = resolveRequest();
        LocalDateTime operationTime = LocalDateTime.now();
        Object result = null;
        Throwable throwable = null;

        try {
            result = joinPoint.proceed();
            persistAuditLog(joinPoint, method, auditConfig, request, operationTime, result, null);
            return result;
        } catch (Throwable ex) {
            throwable = ex;
            if (!auditConfig.ignoreOnException()) {
                persistAuditLog(joinPoint, method, auditConfig, request, operationTime, null, ex);
            }
            throw ex;
        } finally {
            if (throwable == null) {
                log.debug("审计切面已处理完成，method={}", method.getName());
            }
        }
    }

    /**
     * 持久化审计日志
     *
     * @param joinPoint 切点
     * @param method 方法
     * @param auditConfig 审计配置
     * @param request 请求
     * @param operationTime 操作时间
     * @param result 返回值
     * @param throwable 异常
     */
    private void persistAuditLog(ProceedingJoinPoint joinPoint,
                                 Method method,
                                 ResolvedAuditConfig auditConfig,
                                 HttpServletRequest request,
                                 LocalDateTime operationTime,
                                 Object result,
                                 Throwable throwable) {
        try {
            OperatorContext operatorContext = resolveOperatorContext();
            com.yyyouth.model.pojo.system.AuditLog auditLog = new com.yyyouth.model.pojo.system.AuditLog();
            auditLog.setUserId(operatorContext.userId());
            auditLog.setUserType(operatorContext.userType());
            auditLog.setModule(auditConfig.module());
            auditLog.setAction(auditConfig.action());
            auditLog.setTargetType(resolveTargetType(request, method));
            auditLog.setTargetId(resolveTargetId(method, joinPoint.getArgs()));
            auditLog.setResult(throwable == null ? AUDIT_SUCCESS_RESULT : AUDIT_FAILED_RESULT);
            auditLog.setErrorMsg(buildErrorMessage(joinPoint, method, auditConfig, request, result, throwable));
            auditLog.setIp(resolveRequestIp(request));
            auditLog.setRequestUrl(resolveRequestUrl(request));
            auditLog.setRequestMethod(request == null ? null : request.getMethod());
            auditLog.setCreateTime(operationTime);
            auditLogMapper.insert(auditLog);
        } catch (Exception ex) {
            log.warn("写入审计日志失败，method={}", method.getName(), ex);
        }
    }

    /**
     * 解析审计配置
     *
     * @param method 方法
     * @param targetClass 目标类
     * @return 审计配置
     */
    private ResolvedAuditConfig resolveAuditConfig(Method method, Class<?> targetClass) {
        AuditLog classAnnotation = AnnotatedElementUtils.findMergedAnnotation(targetClass, AuditLog.class);
        AuditLog methodAnnotation = AnnotatedElementUtils.findMergedAnnotation(method, AuditLog.class);
        String module = resolveAttribute(classAnnotation == null ? "" : classAnnotation.module(),
                methodAnnotation == null ? "" : methodAnnotation.module());
        String action = resolveAttribute(classAnnotation == null ? "" : classAnnotation.action(),
                methodAnnotation == null ? "" : methodAnnotation.action());
        String description = resolveAttribute(classAnnotation == null ? "" : classAnnotation.description(),
                methodAnnotation == null ? "" : methodAnnotation.description());
        boolean ignoreResult = methodAnnotation != null ? methodAnnotation.ignoreResult()
                : classAnnotation != null && classAnnotation.ignoreResult();
        boolean ignoreOnException = methodAnnotation != null ? methodAnnotation.ignoreOnException()
                : classAnnotation != null && classAnnotation.ignoreOnException();
        return new ResolvedAuditConfig(StringUtils.hasText(module) && StringUtils.hasText(action), module, action, description,
                ignoreResult, ignoreOnException);
    }

    private String resolveAttribute(String classLevelValue, String methodLevelValue) {
        return StringUtils.hasText(methodLevelValue) ? methodLevelValue : classLevelValue;
    }

    /**
     * 构建兼容的错误信息字段
     *
     * @param joinPoint 切点
     * @param method 方法
     * @param auditConfig 审计配置
     * @param request 请求
     * @param result 返回值
     * @param throwable 异常
     * @return 摘要信息
     */
    private String buildErrorMessage(ProceedingJoinPoint joinPoint,
                                     Method method,
                                     ResolvedAuditConfig auditConfig,
                                     HttpServletRequest request,
                                     Object result,
                                     Throwable throwable) {
        Map<String, Object> summary = new LinkedHashMap<>();
        String description = resolveDescription(joinPoint, method, auditConfig.description(), request, result, throwable);
        if (StringUtils.hasText(description)) {
            summary.put("description", description);
        }
        Object argsSummary = summarizeArguments(method, joinPoint.getArgs());
        if (argsSummary != null) {
            summary.put("args", argsSummary);
        }
        if (!auditConfig.ignoreResult() && throwable == null) {
            Object resultSummary = summarizeObject(result);
            if (resultSummary != null) {
                summary.put("result", resultSummary);
            }
        }
        if (throwable != null) {
            summary.put("exception", throwable.getClass().getSimpleName() + ":" + throwable.getMessage());
        }
        if (summary.isEmpty()) {
            return null;
        }
        return truncate(JSONUtil.toJsonStr(summary), MAX_ERROR_MSG_LENGTH);
    }

    /**
     * 解析描述表达式
     *
     * @param joinPoint 切点
     * @param method 方法
     * @param expression 描述表达式
     * @param request 请求
     * @param result 返回值
     * @param throwable 异常
     * @return 描述结果
     */
    private String resolveDescription(ProceedingJoinPoint joinPoint,
                                      Method method,
                                      String expression,
                                      HttpServletRequest request,
                                      Object result,
                                      Throwable throwable) {
        if (!StringUtils.hasText(expression)) {
            return null;
        }
        try {
            StandardEvaluationContext context = new StandardEvaluationContext();
            Object[] args = joinPoint.getArgs();
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
            context.setVariable("request", request);
            context.setVariable("result", result);
            context.setVariable("exception", throwable);
            if (expression.contains("#{")) {
                return expressionParser.parseExpression(expression, TEMPLATE_PARSER_CONTEXT).getValue(context, String.class);
            }
            if (expression.startsWith("#")) {
                Object value = expressionParser.parseExpression(expression).getValue(context);
                return value == null ? null : String.valueOf(value);
            }
            return expression;
        } catch (Exception ex) {
            log.warn("解析审计描述失败，method={}, expression={}", method.getName(), expression, ex);
            return expression;
        }
    }

    /**
     * 摘要方法参数
     *
     * @param method 方法
     * @param args 参数值
     * @return 参数摘要
     */
    private Object summarizeArguments(Method method, Object[] args) {
        if (args == null || args.length == 0) {
            return null;
        }
        Map<String, Object> summary = new LinkedHashMap<>();
        Parameter[] parameters = method.getParameters();
        for (int index = 0; index < parameters.length; index++) {
            summary.put(parameters[index].getName(), summarizeObject(args[index]));
        }
        return summary;
    }

    /**
     * 摘要对象内容
     *
     * @param value 原始对象
     * @return 可序列化摘要
     */
    private Object summarizeObject(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof MultipartFile file) {
            return "MultipartFile(" + file.getOriginalFilename() + ")";
        }
        if (value instanceof HttpServletRequest) {
            return "HttpServletRequest";
        }
        if (value instanceof byte[]) {
            return "byte[" + ((byte[]) value).length + "]";
        }
        if (value instanceof CharSequence || value instanceof Number || value instanceof Boolean) {
            return value;
        }
        if (value.getClass().isArray()) {
            return Arrays.toString((Object[]) value);
        }
        try {
            return JSONUtil.parse(value);
        } catch (Exception ex) {
            return String.valueOf(value);
        }
    }

    /**
     * 解析目标类型
     *
     * @param request 请求
     * @param method 方法
     * @return 目标类型
     */
    private String resolveTargetType(HttpServletRequest request, Method method) {
        if (request != null && StringUtils.hasText(request.getRequestURI())) {
            String[] segments = request.getRequestURI().split("/");
            if (segments.length >= 4) {
                return segments[3];
            }
        }
        return method.getDeclaringClass().getSimpleName();
    }

    /**
     * 解析目标ID
     *
     * @param method 方法
     * @param args 参数
     * @return 目标ID
     */
    private Long resolveTargetId(Method method, Object[] args) {
        if (args == null || args.length == 0) {
            return null;
        }
        Parameter[] parameters = method.getParameters();
        for (int index = 0; index < parameters.length; index++) {
            String parameterName = parameters[index].getName();
            if ("id".equals(parameterName) || parameterName.endsWith("Id")) {
                Long targetId = convertToLong(args[index]);
                if (targetId != null) {
                    return targetId;
                }
            }
        }
        return null;
    }

    private Long convertToLong(Object value) {
        if (value instanceof Long longValue) {
            return longValue;
        }
        if (value instanceof Integer integerValue) {
            return integerValue.longValue();
        }
        if (value instanceof String stringValue && StringUtils.hasText(stringValue)) {
            try {
                return Long.parseLong(stringValue);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private OperatorContext resolveOperatorContext() {
        try {
            Long adminId = StpAdminUtil.getLoginIdAsLong();
            if (adminId != null && adminId > 0) {
                return new OperatorContext(adminId, ADMIN_USER_TYPE);
            }
        } catch (Exception ignored) {
            // ignore
        }

        try {
            Long userId = StpUtil.getLoginIdAsLong();
            if (userId != null && userId > 0) {
                return new OperatorContext(userId, USER_USER_TYPE);
            }
        } catch (Exception ignored) {
            // ignore
        }
        return new OperatorContext(null, ANONYMOUS_USER_TYPE);
    }

    private HttpServletRequest resolveRequest() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes servletRequestAttributes) {
            return servletRequestAttributes.getRequest();
        }
        return null;
    }

    private String resolveRequestIp(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(forwardedFor)) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String resolveRequestUrl(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String requestUri = request.getRequestURI();
        if (!StringUtils.hasText(requestUri)) {
            return null;
        }
        if (!StringUtils.hasText(request.getQueryString())) {
            return requestUri;
        }
        return requestUri + "?" + request.getQueryString();
    }

    private String truncate(String value, int maxLength) {
        if (!StringUtils.hasText(value) || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    private record ResolvedAuditConfig(boolean enabled,
                                       String module,
                                       String action,
                                       String description,
                                       boolean ignoreResult,
                                       boolean ignoreOnException) {
    }

    private record OperatorContext(Long userId, String userType) {
    }
}

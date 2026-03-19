package com.yyyouth.common.constants;

/**
 * @Author:忧郁的青年(yyyouth) --zg
 * @Date:2025/11/22
 * 验证码Redis键前缀常量
 */
public class CaptchaConstants {
    // 注册验证码Redis键前缀
    public static final String REGISTER_CODE_KEY_PREFIX = "email:code:register:";

    // 登录验证码Redis键前缀
    public static final String LOGIN_CODE_KEY_PREFIX = "email:code:login:";
    
    // 重置密码验证码Redis键前缀
    public static final String RESET_CODE_KEY_PREFIX = "email:code:reset:";

    // 验证码有效期（分钟）
    public static final int CODE_EXPIRE_MINUTES = 5;
    
    // 验证码发送间隔（秒）
    public static final int SEND_INTERVAL_SECONDS = 60;
    
    // 24小时内最大发送次数
    public static final int MAX_SEND_COUNT_PER_DAY = 1000;

    // 验证码邮件标题
    public static final String CAPTCHA_TITLE = "邮箱验证码";

}

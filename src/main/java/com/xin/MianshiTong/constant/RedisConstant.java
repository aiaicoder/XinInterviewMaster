package com.xin.MianshiTong.constant;

/**
 * @author 15712
 */
public interface RedisConstant {
    //签到前缀
    public static final String SIGN_PREFIX = "mst:sign:";

    //通过年份和用户id拼接key
    static String getSignKey(Integer year, Long userId) {
        return SIGN_PREFIX + year + ":" + userId;
    }
}

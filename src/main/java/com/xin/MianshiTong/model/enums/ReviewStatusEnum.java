package com.xin.MianshiTong.model.enums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户审核状态枚举
 * 
 */
public enum ReviewStatusEnum {

    PENDING("待审核", 0),
    APPROVED("通过", 1),
    REJECTED("拒绝", 2);

    private final String text;
    private final Integer value;

    ReviewStatusEnum(String text, Integer value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 获取值列表
     *
     * @return 值列表
     */
    public static List<Integer> getValues() {
        return Arrays.stream(values())
                     .map(ReviewStatusEnum::getValue)
                     .collect(Collectors.toList());
    }

    /**
     * 根据 value 获取枚举
     *
     * @param value 枚举值
     * @return 对应的枚举
     */
    public static ReviewStatusEnum getEnumByValue(Integer value) {
        if (value == null) {
            return null;
        }
        return Arrays.stream(values())
                     .filter(enumItem -> enumItem.value.equals(value))
                     .findFirst()
                     .orElse(null);
    }

    public Integer getValue() {
        return value;
    }

    public String getText() {
        return text;
    }
}
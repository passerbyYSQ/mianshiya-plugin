package com.github.yuyuanweb.mianshiyaplugin.actions;

import lombok.Getter;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2025/7/18
 */
@Getter
public enum IdType {
    UUID("UUID"),
    SnowflakeID("Snowflake ID");

    private final String value;

    IdType(String value) {
        this.value = value;
    }

}

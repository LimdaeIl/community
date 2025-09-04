package com.community.soap.common.snowflake;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class snowflakeConfig {

    @Bean
    public Snowflake snowflake() {
        return new Snowflake();
    }
}

package com.community.soap.common.config;

import com.community.soap.common.snowflake.Snowflake;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class snowflakeConfig {

    @Bean
    public Snowflake snowflake() {
        return new Snowflake();
    }
}

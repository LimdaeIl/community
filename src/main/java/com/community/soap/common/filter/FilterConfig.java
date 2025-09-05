package com.community.soap.common.filter;

import com.community.soap.common.jwt.JwtProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    private static final int ORDER_EXCEPTION = 1; // 맨 앞
    private static final int ORDER_JWT = 2;       // 그 다음

    @Bean
    public FilterRegistrationBean<ExceptionHandlingFilter> exceptionFilter(ObjectMapper om) {
        FilterRegistrationBean<ExceptionHandlingFilter> reg = new FilterRegistrationBean<>();
        reg.setFilter(new ExceptionHandlingFilter(om));
        reg.addUrlPatterns("/*");
        reg.setOrder(ORDER_EXCEPTION);
        return reg;
    }

    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtFilter(
            JwtProvider jwtProvider,
            JwtFilterProperties props
    ) {
        FilterRegistrationBean<JwtAuthenticationFilter> reg = new FilterRegistrationBean<>();
        reg.setFilter(new JwtAuthenticationFilter(jwtProvider, props));
        reg.addUrlPatterns("/*");
        reg.setOrder(ORDER_JWT);
        return reg;
    }
}

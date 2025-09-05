package com.community.soap.common.filter;


import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "security.jwt-filter")
public class JwtFilterProperties {

    /**
     * 인증 제외 경로 (PathPattern 문법, 예: /api/public/**)
     */
    private List<String> excludePaths = new ArrayList<>();

    /**
     * 인증 제외 HTTP 메서드 (예: [OPTIONS, GET] 등)
     */
    private List<String> excludeMethods = List.of("OPTIONS");

    /**
     * 토큰을 쿠키로도 받을 때의 쿠키명
     */
    private boolean cookieFallbackEnabled = false; // 기본 꺼짐
    private String accessTokenCookie = "ACCESS_TOKEN";
}

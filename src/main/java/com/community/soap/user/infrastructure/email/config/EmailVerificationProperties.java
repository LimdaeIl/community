package com.community.soap.user.infrastructure.email.config;

import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "auth.email")
public class EmailVerificationProperties {

    /**
     * 코드 유효 시간
     */
    private Duration codeTtl = Duration.ofMinutes(5);
    /**
     * 재전송 쿨타임
     */
    private Duration cooltime = Duration.ofSeconds(60);
    /**
     * 시도 횟수 제한(윈도우 TTL = codeTtl)
     */
    private int maxAttempts = 5;
    /**
     * 차단 기간
     */
    private Duration blockTtl = Duration.ofMinutes(10);
    /**
     * 검증 성공 플래그 TTL
     */
    private Duration verifiedTtl = Duration.ofMinutes(10);
}

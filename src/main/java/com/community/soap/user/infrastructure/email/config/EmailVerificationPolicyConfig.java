package com.community.soap.user.infrastructure.email.config;

import com.community.soap.user.application.policy.EmailVerificationPolicy;
import java.time.Duration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(EmailVerificationProperties.class)
public class EmailVerificationPolicyConfig {

    @Bean
    public EmailVerificationPolicy emailVerificationPolicy(EmailVerificationProperties p) {
        return new EmailVerificationPolicy() {
            public Duration codeTtl() {
                return p.getCodeTtl();
            }

            public Duration cooltime() {
                return p.getCooltime();
            }

            public Duration blockTtl() {
                return p.getBlockTtl();
            }

            public long maxAttempts() {
                return p.getMaxAttempts();
            }

            public Duration verifiedTtl() {
                return p.getVerifiedTtl();
            }
        };
    }
}

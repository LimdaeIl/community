package com.community.soap.payment.infrastructure;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Configuration
@EnableConfigurationProperties(TossPaymentsProperties.class)
public class TossRestClientConfig {

    @Bean
    public RestClient tossRestClient(TossPaymentsProperties props) {
        String basic = Base64.getEncoder()
                .encodeToString((props.secretKey() + ":").getBytes(StandardCharsets.UTF_8)); // Basic secretKey:

        return RestClient.builder()
                .baseUrl(props.baseUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + basic)
                .build();
    }
}
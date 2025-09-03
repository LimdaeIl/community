package com.community.soap;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SoapApplication {

	public static void main(String[] args) {
        // .env 파일 로드 (resources 기준 경로로)
        Dotenv dotenv = Dotenv.configure()
                .directory("src/main/resources") // 또는 .env 의 실제 경로
                .ignoreIfMissing()
                .load();

        // 시스템 프로퍼티에 등록 (Spring 이 인식할 수 있도록)
        dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));


        SpringApplication.run(SoapApplication.class, args);
	}

}

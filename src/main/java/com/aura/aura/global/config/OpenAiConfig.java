package com.aura.aura.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

/**
 * OpenAI Vision API 호출용 RestClient.
 *
 * 타임아웃이 이 설정의 핵심이다. 기획서상 아우라 스캔은 3초 이내에 끝나야 하는데,
 * 타임아웃이 없으면 API 지연이 그대로 매장 고객의 대기 시간이 된다.
 * 읽기 타임아웃을 넘기면 AnalysisService 에서 기본 무드로 폴백시키고
 * aura_analysis.is_fallback 을 true 로 기록한다.
 *
 * API 키는 반드시 환경변수로 주입한다.
 *   - 로컬:      실행 구성의 Environment variables 에 OPENAI_API_KEY
 *   - Cloud Run: Secret Manager → --set-secrets=OPENAI_API_KEY=...
 */
@Configuration
public class OpenAiConfig {

    @Value("${openai.api-key}")
    private String apiKey;

    @Value("${openai.base-url:https://api.openai.com/v1}")
    private String baseUrl;

    @Value("${openai.connect-timeout-ms:2000}")
    private long connectTimeoutMs;

    @Value("${openai.read-timeout-ms:5000}")
    private long readTimeoutMs;

    @Bean
    public RestClient openAiRestClient() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofMillis(connectTimeoutMs));
        requestFactory.setReadTimeout(Duration.ofMillis(readTimeoutMs));

        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                .build();
    }
}

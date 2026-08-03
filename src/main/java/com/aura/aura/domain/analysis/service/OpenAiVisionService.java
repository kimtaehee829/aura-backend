package com.aura.aura.domain.analysis.service;

import com.aura.aura.domain.analysis.dto.openai.AuraAnalysisResult;
import com.aura.aura.domain.analysis.dto.openai.OpenAiRequest;
import com.aura.aura.domain.analysis.dto.openai.OpenAiResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenAiVisionService {

    private final RestClient openAiRestClient;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final String SYSTEM_PROMPT = """
            이미지에 있는 사람의 착장을 분석하여 다음 항목을 추출하세요.
            응답은 반드시 아래 JSON 형식을 정확히 준수하여 반환해야 합니다:
            
            {
              "style": "STREET, ROCK_CHIC, CLASSIC, MINIMAL 중 택 1",
              "mood": "STREET, ROMANTIC, CLASSIC, MINIMAL 중 택 1",
              "energy_level": "HIGH 또는 LOW 중 택 1",
              "palette": ["#메인컬러", "#2순위컬러", "#3순위컬러"]
            }
            
            팔레트는 반드시 3개의 헥스(hex) 컬러 코드 배열이어야 합니다.
            """;

    public AuraAnalysisResult analyzeAura(String base64Image) {
        OpenAiRequest requestBody = buildRequest(base64Image);

        try {
            OpenAiResponse response = openAiRestClient.post()
                    .uri("/chat/completions")
                    .body(requestBody)
                    .retrieve()
                    .body(OpenAiResponse.class);

            if (response == null || response.getChoices() == null || response.getChoices().isEmpty()) {
                log.warn("OpenAI API 응답이 비어있습니다.");
                return null;
            }

            String content = response.getChoices().get(0).getMessage().getContent();
            return parseResult(content);
        } catch (RestClientException e) {
            log.error("OpenAI API 통신 중 오류 발생 (타임아웃 또는 인증 오류): {}", e.getMessage());
            return null;
        } catch (Exception e) {
            log.error("OpenAI API 응답 처리 중 오류 발생: {}", e.getMessage());
            return null;
        }
    }

    private OpenAiRequest buildRequest(String base64Image) {
        OpenAiRequest.Content textContent = OpenAiRequest.Content.builder()
                .type("text")
                .text(SYSTEM_PROMPT)
                .build();

        OpenAiRequest.Content imageContent = OpenAiRequest.Content.builder()
                .type("image_url")
                .imageUrl(OpenAiRequest.ImageUrl.builder()
                        .url(base64Image)
                        .build())
                .build();

        OpenAiRequest.Message userMessage = OpenAiRequest.Message.builder()
                .role("user")
                .content(List.of(textContent, imageContent))
                .build();

        return OpenAiRequest.builder()
                .model("gpt-4o-mini")
                .messages(List.of(userMessage))
                .responseFormat(OpenAiRequest.ResponseFormat.builder().type("json_object").build())
                .maxTokens(300)
                .build();
    }

    private AuraAnalysisResult parseResult(String jsonContent) {
        try {
            return objectMapper.readValue(jsonContent, AuraAnalysisResult.class);
        } catch (JsonProcessingException e) {
            log.error("OpenAI 응답 JSON 파싱 실패: {}", jsonContent);
            return null;
        }
    }
}

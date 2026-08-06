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
              "palette": ["#메인컬러", "#2순위컬러", "#추가컬러"]
            }
            
            [컬러 추출 규칙]
            
            1. 색상 수집 범위
            - 실제 보이는 의상과 소지품에서만 색을 고릅니다. 상의, 아우터를 가장 먼저 보고, 그 다음 하의, 신발, 모자, 악세서리 순으로 봅니다.
            - 피부색, 머리카락, 배경, 조명으로 생긴 반사광은 제외합니다.
            - 메인 컬러와 2순위 컬러의 경우, 상상하거나 보정한 색이 아닌 실제 관찰되는 색이어야 합니다.
            
            2. pallete[0] - 메인 컬러 (가방 본체에 입혀짐)
            - 착장에서 면적이 가장 넓거나 시선을 가장 먼저 끄는 유채색을 우선적으로 고릅니다.
            - 다음 중 하나라도 해당되면 무채색으로 간주합니다. 
             * 명도(Lightness) 25% 이하인 어두운 색 (검정 계열)
             * 채도(Saturation) 15% 이하인 색 (흰색·회색 계열)
            - 착장 전체가 무채색이라면, 가장 채도가 높은 부분을 찾아 메인으로 씁니다.
            - 그래도 유채색이 전혀 없다면 위에서 판단한 mood에 대응하는 아래 색을 사용합니다.
             * STREET   #FF3B30
             * ROMANTIC #F2A5C0
             * CLASSIC  #C8A96E
             * MINIMAL  #7FA6C9
             
            3. pallete[1] - 패턴 컬러 (가방 표면 로고 패턴에 입혀짐)
            - 메인 다음으로 눈에 띄는 색을 고릅니다.
            - 메인 컬러와 명도 차이가 20% 이상 나야합니다. 메인 위에 이 색으로 패턴이 입혀질때 패턴이 또렷하게 보여야 하기 때문입니다.
            - 조건에 맞는 색이 착장에 없다면 메인 컬러의 명도를 조정하여 만듭니다.(메인이 밝으면 더 어둡게, 메인이 어두우면 더 밝게)
            
            4. pallete[2] - 추가 컬러 (가방 모서리 라인 등 부수적인 파트에 입혀짐)
            - 이 값은 서버에서 재계산되므로 대략적인 값이면 됩니다.
            - 착장의 금속 장식이나 포인트 색이 보이면 그것을 쓰고, 없으면 메인 컬러를 밝게 조정한 값을 넣으세요.
            - 피부색, 머리카락, 배경에서는 절대 추출하지 않습니다.
            
            5. 공통
            - 세 색은 뚜렷이 구분되어야 합니다.
            - 모두 #RRGGBB 형식의 6자리 대문자 헥스 코드로 반환합니다.
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

        String formattedImage = base64Image;
        if (!formattedImage.startsWith("data:image/")) {
            formattedImage = "data:image/jpeg;base64," + formattedImage;
        }

        OpenAiRequest.Content imageContent = OpenAiRequest.Content.builder()
                .type("image_url")
                .imageUrl(OpenAiRequest.ImageUrl.builder()
                        .url(formattedImage)
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

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
              "energy_level": "0~100 사이 정수 (무드 판정의 확신도가 높을수록 100에 가깝게, 애매할수록 0에 가깝게 반환)",
              "palette": ["#메인컬러", "#2순위컬러", "#추가컬러"]
            }
            
            [무드(mood) 판정 기준]
            가방에 적용할 브랜드 패턴 변형을 결정합니다. 아래 두 축으로 판단한 뒤 가장 가까운 것을 고르세요.
            
            축 A — 실루엣의 구조
              각지고 오버사이즈하며 그래픽 요소가 있음 ...... STREET
              재단되어 있고 구조적이며 정돈됨 .............. CLASSIC
              부드럽고 흐르는 선, 섬세한 디테일 ............ ROMANTIC
              단순하고 구조가 없으며 군더더기 없음 .......... MINIMAL
            
            축 B — 대비와 채도
              대비가 높고 강렬하거나 네온 계열 ............. STREET
              따뜻하거나 파스텔, 부드럽게 가라앉은 톤 ....... ROMANTIC
              뉴트럴하고 절제되어 있으며 세련됨 ............ CLASSIC
              대비가 낮고 단색에 가까움 ................... MINIMAL
            
            판정 우선순위 (두 무드가 비슷해 보일 때 아래 순서대로 적용)
            1. STREET 와 MINIMAL 이 헷갈릴 때
               → 그래픽, 로고, 프린트, 스니커즈, 후디가 보이면 STREET.
                 표면이 단순하고 끊김 없이 이어지면 MINIMAL.
            2. CLASSIC 과 MINIMAL 이 헷갈릴 때
               → 재단된 옷(블레이저, 트렌치, 카라 셔츠, 구조적인 코트)이 하나라도 보이면 CLASSIC.
                 단순하고 구조가 없는 옷차림이면 MINIMAL.
            3. ROMANTIC 과 CLASSIC 이 헷갈릴 때
               → 선이 곡선적이고 원단이 부드럽거나 흐르는 느낌이면 ROMANTIC.
                 선이 직선적이고 원단이 빳빳해 보이면 CLASSIC.
            4. 그래도 판단이 서지 않을 때
               → MINIMAL 을 선택하세요. 가장 안전한 기본값입니다.
            
            * 옷차림만 보고 판단하세요. 표정으로 무드를 추론하지 마세요.
            
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
        } catch (org.springframework.web.client.RestClientResponseException e) {
            log.error("OpenAI API 통신 중 오류 발생 (상태 코드: {}): {}", e.getStatusCode(), e.getResponseBodyAsString());
            return null;
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

        String formattedImage = base64Image.replaceAll("\\s+", "");
        if (!formattedImage.startsWith("data:image/")) {
            if (formattedImage.startsWith("iVBOR")) {
                formattedImage = "data:image/png;base64," + formattedImage;
            } else if (formattedImage.startsWith("/9j/")) {
                formattedImage = "data:image/jpeg;base64," + formattedImage;
            } else {
                formattedImage = "data:image/jpeg;base64," + formattedImage;
            }
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

package com.aura.aura.domain.analysis.dto.openai;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OpenAiRequest {

    private String model;
    private List<Message> messages;
    
    @JsonProperty("response_format")
    private ResponseFormat responseFormat;
    
    @JsonProperty("max_tokens")
    private Integer maxTokens;

    @Getter
    @Builder
    public static class Message {
        private String role;
        private List<Content> content;
    }

    @Getter
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Content {
        private String type;
        private String text;
        @JsonProperty("image_url")
        private ImageUrl imageUrl;
    }

    @Getter
    @Builder
    public static class ImageUrl {
        private String url;
    }
    
    @Getter
    @Builder
    public static class ResponseFormat {
        private String type;
    }
}

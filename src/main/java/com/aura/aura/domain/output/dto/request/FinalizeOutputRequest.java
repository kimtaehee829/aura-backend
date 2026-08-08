package com.aura.aura.domain.output.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.util.List;

@Getter
public class FinalizeOutputRequest {

    @NotNull(message = "aura_code는 필수입니다.")
    @Size(min = 3, max = 3, message = "aura_code는 3개의 색상이어야 합니다.")
    @JsonProperty("aura_code")
    private List<@NotBlank(message = "aura_code 값은 비어있을 수 없습니다.") String> auraCode;

    @NotNull(message = "attached_accessory_id는 필수입니다.")
    @JsonProperty("attached_accessory_id")
    private Long attachedAccessoryId;
}
package com.aura.aura.domain.analysis.service;

import com.aura.aura.domain.analysis.dto.AnalysisResponse;
import com.aura.aura.domain.analysis.dto.openai.AuraAnalysisResult;
import com.aura.aura.domain.analysis.entity.AuraAnalysis;
import com.aura.aura.domain.analysis.entity.Mood;
import com.aura.aura.domain.analysis.repository.AuraAnalysisRepository;
import com.aura.aura.domain.session.entity.Session;
import com.aura.aura.domain.session.repository.SessionRepository;
import com.aura.aura.global.exception.BusinessException;
import com.aura.aura.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalysisService {

    private final OpenAiVisionService openAiVisionService;
    private final AuraAnalysisRepository auraAnalysisRepository;
    private final SessionRepository sessionRepository;

    public AnalysisResponse previewAnalysis(String base64Image) {
        long startTime = System.currentTimeMillis();
        AuraAnalysisResult result = openAiVisionService.analyzeAura(base64Image);
        long latencyMs = System.currentTimeMillis() - startTime;

        if (result == null) {
            return buildFallbackResponse(latencyMs);
        }

        String p1 = result.getPalette() != null && result.getPalette().size() > 0 ? result.getPalette().get(0) : "#000000";
        String p2 = result.getPalette() != null && result.getPalette().size() > 1 ? result.getPalette().get(1) : "#000000";
        String p3 = AuraColorDeriver.deriveAccent(p1, p2, Mood.from(result.getMood()));

        return AnalysisResponse.builder()
                .style(result.getStyle())
                .mood(result.getMood())
                .energyLevel(result.getEnergyLevel())
                .palette(List.of(p1, p2, p3))
                .patternUrl(AnalysisResponse.resolvePatternUrl(result.getMood()))
                .latencyMs((int) latencyMs)
                .fallback(false)
                .build();
    }

    @Transactional
    public AnalysisResponse createAnalysis(String publicId, String base64Image) {
        Session session = sessionRepository.findByPublicId(publicId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SESSION_NOT_FOUND));

        if (auraAnalysisRepository.findBySessionId(session.getId()).isPresent()) {
            throw new BusinessException(ErrorCode.ANALYSIS_ALREADY_EXISTS);
        }

        long startTime = System.currentTimeMillis();
        AuraAnalysisResult result = openAiVisionService.analyzeAura(base64Image);
        long latencyMs = System.currentTimeMillis() - startTime;

        AuraAnalysis auraAnalysis;

        if (result == null) {
            auraAnalysis = AuraAnalysis.builder()
                    .session(session)
                    .style("STREET")
                    .mood("STREET")
                    .energyLevel("HIGH")
                    .palette1("#2E4A7D")
                    .palette2("#FFD700")
                    .palette3("#1A1A2E")
                    .latencyMs((int) latencyMs)
                    .isFallback(true)
                    .build();
        } else {
            String p1 = result.getPalette() != null && result.getPalette().size() > 0 ? result.getPalette().get(0) : "#000000";
            String p2 = result.getPalette() != null && result.getPalette().size() > 1 ? result.getPalette().get(1) : "#000000";
            String p3 = AuraColorDeriver.deriveAccent(p1, p2, Mood.from(result.getMood()));

            auraAnalysis = AuraAnalysis.builder()
                    .session(session)
                    .style(result.getStyle())
                    .mood(result.getMood())
                    .energyLevel(result.getEnergyLevel())
                    .palette1(p1)
                    .palette2(p2)
                    .palette3(p3)
                    .latencyMs((int) latencyMs)
                    .isFallback(false)
                    .build();
        }

        auraAnalysisRepository.save(auraAnalysis);

        return AnalysisResponse.fromEntity(auraAnalysis);
    }

    private AnalysisResponse buildFallbackResponse(long latencyMs) {
        log.warn("분석 실패로 Fallback 응답을 반환합니다. (Latency: {}ms)", latencyMs);
        return AnalysisResponse.builder()
                .style("STREET")
                .mood("STREET")
                .energyLevel("HIGH")
                .palette(java.util.List.of("#2E4A7D", "#FFD700", "#1A1A2E"))
                .patternUrl(AnalysisResponse.resolvePatternUrl("STREET"))
                .latencyMs((int) latencyMs)
                .fallback(true)
                .build();
    }
}

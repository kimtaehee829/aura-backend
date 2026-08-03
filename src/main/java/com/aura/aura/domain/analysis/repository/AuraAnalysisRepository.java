package com.aura.aura.domain.analysis.repository;

import com.aura.aura.domain.analysis.entity.AuraAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuraAnalysisRepository extends JpaRepository<AuraAnalysis, Long> {
    Optional<AuraAnalysis> findBySessionId(Long sessionId);
}

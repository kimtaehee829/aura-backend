package com.aura.aura.domain.analysis.entity;

import com.aura.aura.domain.session.entity.Session;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "aura_analysis")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuraAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false, unique = true)
    private Session session;

    @Column(length = 30)
    private String style;

    @Column(nullable = false, length = 30)
    private String mood;

    @Column(name = "energy_level")
    private Integer energyLevel;

    @Column(name = "palette_1", nullable = false, length = 7)
    private String palette1;

    @Column(name = "palette_2", length = 7)
    private String palette2;

    @Column(name = "palette_3", length = 7)
    private String palette3;

    @Column(name = "latency_ms")
    private Integer latencyMs;

    @Column(name = "is_fallback", nullable = false)
    private Boolean isFallback;

    @Column(name = "raw_json", columnDefinition = "JSON")
    private String rawJson;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public AuraAnalysis(Session session, String style, String mood, Integer energyLevel,
                        String palette1, String palette2, String palette3,
                        Integer latencyMs, Boolean isFallback, String rawJson) {
        this.session = session;
        this.style = style;
        this.mood = mood;
        this.energyLevel = energyLevel;
        this.palette1 = palette1;
        this.palette2 = palette2;
        this.palette3 = palette3;
        this.latencyMs = latencyMs;
        this.isFallback = isFallback != null ? isFallback : false;
        this.rawJson = rawJson;
        this.createdAt = LocalDateTime.now();
    }
}

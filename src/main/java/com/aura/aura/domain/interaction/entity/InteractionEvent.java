package com.aura.aura.domain.interaction.entity;

import com.aura.aura.domain.product.entity.Product;
import com.aura.aura.domain.session.entity.Session;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "interaction_events")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InteractionEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private Session session;

    @Column(name = "seq", nullable = false)
    private Integer seq;

    @Column(name = "phase", nullable = false)
    private String phase;

    @Column(name = "target_type", nullable = false)
    private String targetType;

    @Column(name = "target_part")
    private String targetPart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_product_id")
    private Product targetProduct;

    @Column(name = "gesture", nullable = false)
    private String gesture;

    @Column(name = "dwell_ms", nullable = false)
    private Integer dwellMs;

    @Column(name = "rotation_degrees")
    private Integer rotationDegrees;

    @Column(name = "is_completed", nullable = false)
    private Boolean isCompleted;

    @Column(name = "elapsed_ms", nullable = false)
    private Integer elapsedMs;

    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;

    @Builder
    public InteractionEvent(Session session, Integer seq, String phase, String targetType,
                            String targetPart, Product targetProduct, String gesture,
                            Integer dwellMs, Integer rotationDegrees, Boolean isCompleted,
                            Integer elapsedMs, LocalDateTime occurredAt) {
        this.session = session;
        this.seq = seq;
        this.phase = phase;
        this.targetType = targetType;
        this.targetPart = targetPart;
        this.targetProduct = targetProduct;
        this.gesture = gesture;
        this.dwellMs = dwellMs;
        this.rotationDegrees = rotationDegrees;
        this.isCompleted = isCompleted != null ? isCompleted : false;
        this.elapsedMs = elapsedMs;
        this.occurredAt = occurredAt;
    }
}

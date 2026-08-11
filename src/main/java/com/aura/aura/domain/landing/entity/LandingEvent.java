package com.aura.aura.domain.landing.entity;

import com.aura.aura.domain.product.entity.Product;
import com.aura.aura.domain.session.entity.Session;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "landing_events")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LandingEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private Session session;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 50)
    private LandingEventType eventType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public LandingEvent(Session session, LandingEventType eventType, Product product) {
        this.session = session;
        this.eventType = eventType;
        this.product = product;
        this.createdAt = LocalDateTime.now();
    }
}

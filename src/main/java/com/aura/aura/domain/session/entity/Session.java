package com.aura.aura.domain.session.entity;

import com.aura.aura.domain.product.entity.Product;
import com.aura.aura.domain.store.entity.Store;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "sessions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "public_id", nullable = false, unique = true, length = 24)
    private String publicId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bag_product_id")
    private Product bagProduct;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "abandoned_at_status", length = 20)
    private String abandonedAtStatus;

    @Column(name = "consent_agreed_at")
    private LocalDateTime consentAgreedAt;

    @Column(name = "started_at", nullable = false, updatable = false)
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Builder
    public Session(String publicId, Store store, Product bagProduct, String status, LocalDateTime consentAgreedAt) {
        this.publicId = publicId;
        this.store = store;
        this.bagProduct = bagProduct;
        this.status = status;
        this.consentAgreedAt = consentAgreedAt;
        this.startedAt = LocalDateTime.now();
    }

    public void updateStatus(String status) {
        this.status = status;
    }

    public void abandon() {
        this.abandonedAtStatus = this.status;
        this.status = "ABANDONED";
        this.endedAt = LocalDateTime.now();
    }

    public void complete() {
        this.status = "COMPLETE";
        this.endedAt = LocalDateTime.now();
    }
}

package com.aura.aura.domain.session.entity;

import com.aura.aura.domain.product.entity.Product;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "session_accessories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SessionAccessory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private Session session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "slot_order", nullable = false)
    private Integer slotOrder;

    @Column(name = "is_attached", nullable = false)
    private Boolean isAttached;

    @Column(name = "attached_at")
    private LocalDateTime attachedAt;

    @Builder
    public SessionAccessory(Session session, Product product, Integer slotOrder, Boolean isAttached) {
        this.session = session;
        this.product = product;
        this.slotOrder = slotOrder;
        this.isAttached = isAttached != null ? isAttached : false;
        if (this.isAttached) {
            this.attachedAt = LocalDateTime.now();
        }
    }

    public void attach() {
        this.isAttached = true;
        this.attachedAt = LocalDateTime.now();
    }
}

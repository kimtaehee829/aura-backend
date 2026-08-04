package com.aura.aura.domain.product.service;

import com.aura.aura.domain.product.dto.AccessoryAttachResponse;
import com.aura.aura.domain.product.dto.AccessoryResponse;
import com.aura.aura.domain.product.entity.Product;
import com.aura.aura.domain.product.repository.ProductRepository;
import com.aura.aura.domain.session.entity.Session;
import com.aura.aura.domain.session.entity.SessionAccessory;
import com.aura.aura.domain.session.repository.SessionAccessoryRepository;
import com.aura.aura.domain.session.repository.SessionRepository;
import com.aura.aura.global.exception.BusinessException;
import com.aura.aura.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccessoryService {

    private final ProductRepository productRepository;
    private final SessionRepository sessionRepository;
    private final SessionAccessoryRepository sessionAccessoryRepository;

    @Transactional(readOnly = true)
    public List<AccessoryResponse> getSessionAccessories(String publicId) {
        Session session = sessionRepository.findByPublicId(publicId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SESSION_NOT_FOUND));

        List<Product> accessories = productRepository.findAllByProductType("ACCESSORY");
        List<SessionAccessory> sessionAccessories = sessionAccessoryRepository.findAllBySessionId(session.getId());

        Map<Long, Boolean> attachedStatusMap = sessionAccessories.stream()
                .collect(Collectors.toMap(
                        sa -> sa.getProduct().getId(),
                        SessionAccessory::getIsAttached
                ));

        List<AccessoryResponse> responses = new ArrayList<>();
        int slot = 1;
        for (Product accessory : accessories) {
            Boolean isAttached = attachedStatusMap.getOrDefault(accessory.getId(), false);
            responses.add(AccessoryResponse.of(accessory, slot++, isAttached));
        }

        return responses;
    }

    @Transactional
    public AccessoryAttachResponse attachAccessory(String publicId, Long productId) {
        Session session = sessionRepository.findByPublicId(publicId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SESSION_NOT_FOUND));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        long attachedCount = sessionAccessoryRepository.countBySessionIdAndIsAttachedTrue(session.getId());
        
        Optional<SessionAccessory> existingOpt = sessionAccessoryRepository.findBySessionIdAndProductId(session.getId(), productId);
        
        SessionAccessory target;
        if (existingOpt.isPresent()) {
            target = existingOpt.get();
            if (target.getIsAttached()) {
                return buildAttachResponse(target);
            }
        } else {
            target = SessionAccessory.builder()
                    .session(session)
                    .product(product)
                    .slotOrder(1)
                    .isAttached(false)
                    .build();
        }

        if (attachedCount >= 1) {
            throw new BusinessException(ErrorCode.ACCESSORY_LIMIT_EXCEEDED);
        }

        target.attach();
        sessionAccessoryRepository.save(target);

        return buildAttachResponse(target);
    }

    private AccessoryAttachResponse buildAttachResponse(SessionAccessory sessionAccessory) {
        return AccessoryAttachResponse.builder()
                .productId(sessionAccessory.getProduct().getId())
                .name(sessionAccessory.getProduct().getName())
                .isAttached(sessionAccessory.getIsAttached())
                .attachedAt(sessionAccessory.getAttachedAt())
                .build();
    }
}

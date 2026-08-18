package com.aura.aura.domain.landing.service;

import com.aura.aura.domain.landing.dto.request.LandingEventRequest;
import com.aura.aura.domain.landing.entity.LandingEvent;
import com.aura.aura.domain.landing.entity.LandingEventType;
import com.aura.aura.domain.landing.repository.LandingEventRepository;
import com.aura.aura.domain.output.dto.response.LandingResponse;
import com.aura.aura.domain.output.service.OutputService;
import com.aura.aura.domain.product.entity.Product;
import com.aura.aura.domain.product.repository.ProductRepository;
import com.aura.aura.domain.session.entity.Session;
import com.aura.aura.domain.session.repository.SessionRepository;
import com.aura.aura.global.exception.BusinessException;
import com.aura.aura.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class LandingService {

    private final OutputService outputService;
    private final LandingEventRepository landingEventRepository;
    private final SessionRepository sessionRepository;
    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public LandingResponse getLanding(String publicId) {
        return outputService.getLanding(publicId);
    }

    public void createLandingEvent(String publicId, LandingEventRequest request) {
        Session session = sessionRepository.findByPublicId(publicId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SESSION_NOT_FOUND));

        LandingEventType eventType;
        try {
            eventType = LandingEventType.valueOf(request.getEventType());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }

        Product product = null;
        if (request.getProductId() != null) {
            product = productRepository.findById(request.getProductId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
        }

        if (eventType == LandingEventType.PURCHASE_CLICK && product == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }

        LandingEvent landingEvent = LandingEvent.builder()
                .session(session)
                .eventType(eventType)
                .product(product)
                .build();

        landingEventRepository.save(landingEvent);
    }
}

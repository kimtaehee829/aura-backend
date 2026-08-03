package com.aura.aura.domain.session.service;

import com.aura.aura.domain.analysis.entity.AuraAnalysis;
import com.aura.aura.domain.analysis.repository.AuraAnalysisRepository;
import com.aura.aura.domain.product.entity.Product;
import com.aura.aura.domain.product.repository.ProductRepository;
import com.aura.aura.domain.session.dto.*;
import com.aura.aura.domain.session.entity.Session;
import com.aura.aura.domain.session.repository.SessionRepository;
import com.aura.aura.domain.store.entity.Store;
import com.aura.aura.domain.store.repository.StoreRepository;
import com.aura.aura.global.exception.BusinessException;
import com.aura.aura.global.exception.ErrorCode;
import com.aura.aura.global.util.PublicIdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final SessionRepository sessionRepository;
    private final StoreRepository storeRepository;
    private final ProductRepository productRepository;
    private final AuraAnalysisRepository auraAnalysisRepository;

    @Transactional
    public SessionCreateResponse createSession(SessionCreateRequest request) {
        if (!Boolean.TRUE.equals(request.getConsentAgreed())) {
            throw new BusinessException(ErrorCode.CONSENT_REQUIRED);
        }

        Store store = storeRepository.findById(request.getStoreId())
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));

        Product bagProduct = productRepository.findById(1L).orElse(null);

        String publicId = PublicIdGenerator.generate();

        Session session = Session.builder()
                .publicId(publicId)
                .store(store)
                .bagProduct(bagProduct)
                .status("ANALYZING")
                .consentAgreedAt(LocalDateTime.now())
                .build();

        sessionRepository.save(session);

        SessionCreateResponse.BagProductDto bagDto = null;
        if (bagProduct != null) {
            bagDto = new SessionCreateResponse.BagProductDto(
                    bagProduct.getId(),
                    bagProduct.getName(),
                    bagProduct.getModelUrl(),
                    bagProduct.getImageUrl()
            );
        }

        return new SessionCreateResponse(
                session.getPublicId(),
                session.getStatus(),
                bagDto,
                session.getStartedAt()
        );
    }

    @Transactional
    public SessionStatusUpdateResponse updateStatus(String publicId, SessionStatusUpdateRequest request) {
        Session session = sessionRepository.findByPublicId(publicId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SESSION_NOT_FOUND));

        if ("COMPLETE".equals(session.getStatus())) {
            throw new BusinessException(ErrorCode.SESSION_ALREADY_COMPLETED);
        }

        session.updateStatus(request.getStatus());

        return new SessionStatusUpdateResponse(session.getPublicId(), session.getStatus());
    }

    @Transactional
    public SessionAbandonResponse abandonSession(String publicId) {
        Session session = sessionRepository.findByPublicId(publicId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SESSION_NOT_FOUND));

        session.abandon();

        return new SessionAbandonResponse(session.getPublicId(), session.getStatus(), session.getAbandonedAtStatus());
    }

    @Transactional(readOnly = true)
    public SessionResponse getSession(String publicId) {
        Session session = sessionRepository.findByPublicId(publicId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SESSION_NOT_FOUND));

        SessionResponse.BagProductDto bagDto = null;
        if (session.getBagProduct() != null) {
            bagDto = SessionResponse.BagProductDto.builder()
                    .productId(session.getBagProduct().getId())
                    .name(session.getBagProduct().getName())
                    .build();
        }

        SessionResponse.AuraDto auraDto = null;
        Optional<AuraAnalysis> auraAnalysisOpt = auraAnalysisRepository.findBySessionId(session.getId());
        if (auraAnalysisOpt.isPresent()) {
            AuraAnalysis analysis = auraAnalysisOpt.get();
            auraDto = SessionResponse.AuraDto.builder()
                    .style(analysis.getStyle())
                    .mood(analysis.getMood())
                    .energyLevel(analysis.getEnergyLevel())
                    .palette(java.util.List.of(analysis.getPalette1(), analysis.getPalette2(), analysis.getPalette3()))
                    .build();
        }

        return SessionResponse.builder()
                .publicId(session.getPublicId())
                .status(session.getStatus())
                .bagProduct(bagDto)
                .aura(auraDto)
                .startedAt(session.getStartedAt())
                .build();
    }
}

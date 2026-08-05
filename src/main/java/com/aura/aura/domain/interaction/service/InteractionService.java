package com.aura.aura.domain.interaction.service;

import com.aura.aura.domain.interaction.dto.InteractionCreateRequest;
import com.aura.aura.domain.interaction.dto.InteractionCreateResponse;
import com.aura.aura.domain.interaction.dto.InteractionEventDto;
import com.aura.aura.domain.interaction.entity.InteractionEvent;
import com.aura.aura.domain.interaction.repository.InteractionEventRepository;
import com.aura.aura.domain.product.entity.Product;
import com.aura.aura.domain.product.repository.ProductRepository;
import com.aura.aura.domain.session.entity.Session;
import com.aura.aura.domain.session.repository.SessionRepository;
import com.aura.aura.global.exception.BusinessException;
import com.aura.aura.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InteractionService {

    private final SessionRepository sessionRepository;
    private final InteractionEventRepository interactionEventRepository;
    private final ProductRepository productRepository;

    @Transactional
    public InteractionCreateResponse saveInteractions(String publicId, InteractionCreateRequest request) {
        Session session = sessionRepository.findByPublicId(publicId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SESSION_NOT_FOUND));

        if (request.events() == null || request.events().isEmpty()) {
            return new InteractionCreateResponse(0);
        }

        Set<Integer> existingSeqs = interactionEventRepository.findSeqsBySessionId(session.getId());

        List<InteractionEvent> toSave = request.events().stream()
                .filter(dto -> !existingSeqs.contains(dto.getSeq()))
                .map(dto -> toEntity(session, dto))
                .collect(Collectors.toList());

        if (!toSave.isEmpty()) {
            interactionEventRepository.saveAll(toSave);
        }

        return new InteractionCreateResponse(toSave.size());
    }

    private InteractionEvent toEntity(Session session, InteractionEventDto dto) {
        Product targetProduct = null;
        if (dto.getTargetProductId() != null) {
            targetProduct = productRepository.getReferenceById(dto.getTargetProductId());
        }

        return InteractionEvent.builder()
                .session(session)
                .seq(dto.getSeq())
                .phase(dto.getPhase())
                .targetType(dto.getTargetType())
                .targetPart(dto.getTargetPart())
                .targetProduct(targetProduct)
                .gesture(dto.getGesture())
                .dwellMs(dto.getDwellMs())
                .rotationDegrees(dto.getRotationDegrees())
                .isCompleted(dto.getIsCompleted())
                .elapsedMs(dto.getElapsedMs())
                .occurredAt(dto.getOccurredAt())
                .build();
    }
}

package com.aura.aura.domain.session.repository;

import com.aura.aura.domain.session.entity.SessionAccessory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SessionAccessoryRepository extends JpaRepository<SessionAccessory, Long> {

    List<SessionAccessory> findAllBySessionId(Long sessionId);

    Optional<SessionAccessory> findBySessionIdAndProductId(Long sessionId, Long productId);

    @Query("SELECT COUNT(sa) FROM SessionAccessory sa WHERE sa.session.id = :sessionId AND sa.isAttached = true")
    long countBySessionIdAndIsAttachedTrue(@Param("sessionId") Long sessionId);
}

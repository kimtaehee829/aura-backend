package com.aura.aura.domain.session.repository;

import com.aura.aura.domain.session.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SessionRepository extends JpaRepository<Session, Long> {
    Optional<Session> findByPublicId(String publicId);
}

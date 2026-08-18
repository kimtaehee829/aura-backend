package com.aura.aura.domain.output.repository;

import com.aura.aura.domain.output.entity.SessionOutput;
import com.aura.aura.domain.session.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SessionOutputRepository extends JpaRepository<SessionOutput, Long> {

    Optional<SessionOutput> findBySession(Session session);

}
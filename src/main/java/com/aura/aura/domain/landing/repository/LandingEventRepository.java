package com.aura.aura.domain.landing.repository;

import com.aura.aura.domain.landing.entity.LandingEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface LandingEventRepository extends JpaRepository<LandingEvent, Long> {
    List<LandingEvent> findByOccurredAtBetweenOrderByOccurredAtAsc(LocalDateTime start, LocalDateTime end);
}

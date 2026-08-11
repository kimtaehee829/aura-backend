package com.aura.aura.domain.landing.repository;

import com.aura.aura.domain.landing.entity.LandingEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LandingEventRepository extends JpaRepository<LandingEvent, Long> {
}

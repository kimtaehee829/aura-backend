package com.aura.aura.domain.interaction.repository;

import com.aura.aura.domain.interaction.entity.InteractionEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public interface InteractionEventRepository extends JpaRepository<InteractionEvent, Long> {

    @Query("select e.seq from InteractionEvent e where e.session.id = :sessionId")
    Set<Integer> findSeqsBySessionId(@Param("sessionId") Long sessionId);

    List<InteractionEvent> findByOccurredAtBetweenOrderByOccurredAtAsc(LocalDateTime start, LocalDateTime end);

}

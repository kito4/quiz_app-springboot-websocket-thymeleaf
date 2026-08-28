package org.example.bruh.repository;

import org.example.bruh.entity.Participant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ParticipantRepository extends JpaRepository<Participant, Long> {
    List<Participant> findBySessionId(Long sessionId);
    Optional<Participant> findByUserIdAndSessionId(Long userId, Long sessionId);
    List<Participant> findBySessionIdOrderByScoreDesc(Long sessionId);
    List<Participant> findByUserIdOrderByIdDesc(Long userId);
    long countBySessionId(Long sessionId);
}

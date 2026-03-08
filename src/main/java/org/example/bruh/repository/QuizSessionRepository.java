package org.example.bruh.repository;


import org.example.bruh.entity.QuizSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QuizSessionRepository extends JpaRepository<QuizSession, Long> {
    Optional<QuizSession> findByRoomCode(String roomCode);
}
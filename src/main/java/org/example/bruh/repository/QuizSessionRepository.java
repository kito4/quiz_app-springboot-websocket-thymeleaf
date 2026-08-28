package org.example.bruh.repository;

import org.example.bruh.entity.QuizSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuizSessionRepository extends JpaRepository<QuizSession, Long> {
    Optional<QuizSession> findByRoomCode(String roomCode);
    List<QuizSession> findByHostIdOrderByIdDesc(Long hostId);
    List<QuizSession> findByQuizIdOrderByIdDesc(Long quizId);
}

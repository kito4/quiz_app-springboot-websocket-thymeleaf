package org.example.bruh.repository;

import org.example.bruh.entity.UserAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserAnswerRepository extends JpaRepository<UserAnswer, Long> {
    List<UserAnswer> findByParticipantIdAndQuestionId(Long participantId, Long questionId);
    boolean existsByParticipantIdAndQuestionId(Long participantId, Long questionId);
}

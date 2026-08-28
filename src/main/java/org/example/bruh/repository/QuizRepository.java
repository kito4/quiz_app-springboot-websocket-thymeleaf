package org.example.bruh.repository;

import org.example.bruh.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
    List<Quiz> findByCreatedByOrderByIdDesc(Long createdBy);
}

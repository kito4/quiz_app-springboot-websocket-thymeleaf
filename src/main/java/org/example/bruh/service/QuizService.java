package org.example.bruh.service;

import org.example.bruh.entity.Quiz;
import org.example.bruh.entity.Question;
import org.example.bruh.entity.QuizSession;
import org.example.bruh.repository.AnswerRepository;
import org.example.bruh.repository.QuestionRepository;
import org.example.bruh.repository.QuizRepository;
import org.example.bruh.repository.QuizSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;

@Service
public class QuizService {

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private AnswerRepository answerRepository;

    @Autowired
    private QuizSessionRepository sessionRepository;

    private final SecureRandom rnd = new SecureRandom();

    public Quiz saveQuiz(Quiz quiz) {
        return quizRepository.save(quiz);
    }

    public Question addQuestion(Question q) {
        return questionRepository.save(q);
    }

    public List<Question> findQuestions(Long quizId) {
        return questionRepository.findByQuizId(quizId);
    }

    public Optional<Quiz> findQuiz(Long id) {
        return quizRepository.findById(id);
    }

    public QuizSession startSession(Long quizId) {
        QuizSession s = new QuizSession();
        s.setQuizId(quizId);
        s.setRoomCode(randomCode(6));
        s.setStatus("RUNNING");
        s.setStartedAt(java.time.Instant.now());
        return sessionRepository.save(s);
    }

    private String randomCode(int len) {
        String chars = "ABCDEFGHJKMNPQRSTUVWXYZ23456789";
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) sb.append(chars.charAt(rnd.nextInt(chars.length())));
        return sb.toString();
    }
}

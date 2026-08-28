package org.example.bruh.service;

import org.example.bruh.entity.Answer;
import org.example.bruh.entity.Question;
import org.example.bruh.entity.Quiz;
import org.example.bruh.entity.QuizSession;
import org.example.bruh.repository.AnswerRepository;
import org.example.bruh.repository.QuestionRepository;
import org.example.bruh.repository.QuizRepository;
import org.example.bruh.repository.QuizSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
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

    public Optional<Quiz> findQuiz(Long id) {
        return quizRepository.findById(id);
    }

    public List<Quiz> findQuizzesByOrganizer(Long userId) {
        return quizRepository.findByCreatedByOrderByIdDesc(userId);
    }

    public List<Question> findQuestions(Long quizId) {
        return questionRepository.findByQuizIdOrderByIdAsc(quizId);
    }

    public long countQuestions(Long quizId) {
        return questionRepository.countByQuizId(quizId);
    }

    public Question addQuestion(Question q) {
        return questionRepository.save(q);
    }

    public Answer addAnswer(Answer a) {
        return answerRepository.save(a);
    }

    public List<Answer> findAnswers(Long questionId) {
        return answerRepository.findByQuestionIdOrderByIdAsc(questionId);
    }

    @Transactional
    public void deleteQuestion(Long questionId) {
        answerRepository.deleteByQuestionId(questionId);
        questionRepository.deleteById(questionId);
    }

    @Transactional
    public void deleteQuiz(Long quizId) {

        for (Question q : questionRepository.findByQuizIdOrderByIdAsc(quizId)) {
            answerRepository.deleteByQuestionId(q.getId());
        }
        questionRepository.deleteByQuizId(quizId);
        quizRepository.deleteById(quizId);
    }


    public QuizSession startSession(Long quizId, Long hostId) {
        QuizSession s = new QuizSession();
        s.setQuizId(quizId);
        s.setHostId(hostId);
        s.setRoomCode(randomCode(6));
        s.setStatus("CREATED");
        s.setStartedAt(Instant.now());
        return sessionRepository.save(s);
    }

    private String randomCode(int len) {
        String chars = "ABCDEFGHJKMNPQRSTUVWXYZ23456789";
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }
}

package org.example.bruh.service;

import org.example.bruh.dto.LeaderboardEntry;
import org.example.bruh.dto.QuestionDto;
import org.example.bruh.entity.*;
import org.example.bruh.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SessionService {

    private static final int BASE_POINTS = 100;
    private static final int SPEED_BONUS_MAX = 50;

    @Autowired
    private QuizSessionRepository sessionRepository;

    @Autowired
    private ParticipantRepository participantRepository;

    @Autowired
    private UserAnswerRepository userAnswerRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private AnswerRepository answerRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private QuizRepository quizRepository;

    public Optional<QuizSession> findByRoom(String code) {
        return sessionRepository.findByRoomCode(code == null ? null : code.trim().toUpperCase());
    }

    public Optional<QuizSession> findById(Long id) {
        return sessionRepository.findById(id);
    }

    public QuizSession save(QuizSession session) {
        return sessionRepository.save(session);
    }

    public long countParticipants(Long sessionId) {
        return participantRepository.countBySessionId(sessionId);
    }


    @Transactional
    public Participant joinSession(Long userId, QuizSession session) {
        Optional<Participant> existing =
                participantRepository.findByUserIdAndSessionId(userId, session.getId());
        if (existing.isPresent()) {
            return existing.get();
        }
        Participant p = new Participant();
        p.setUserId(userId);
        p.setSessionId(session.getId());
        p.setScore(0);
        return participantRepository.save(p);
    }


    @Transactional
    public SubmitResult submitAnswer(String roomCode, Long userId, Long questionId, List<Long> answerIds) {
        QuizSession session = findByRoom(roomCode).orElse(null);
        if (session == null) {
            return SubmitResult.rejected("Комната не найдена");
        }
        if (!"RUNNING".equals(session.getStatus())) {
            return SubmitResult.rejected("Квиз сейчас не идёт");
        }
        if (!Objects.equals(session.getCurrentQuestionId(), questionId)) {
            return SubmitResult.rejected("Этот вопрос уже не показывается");
        }

        Participant participant = participantRepository
                .findByUserIdAndSessionId(userId, session.getId()).orElse(null);
        if (participant == null) {
            return SubmitResult.rejected("Вы не подключены к этой комнате");
        }
        if (userAnswerRepository.existsByParticipantIdAndQuestionId(participant.getId(), questionId)) {
            return SubmitResult.rejected("Вы уже ответили на этот вопрос");
        }

        List<Long> selected = answerIds == null ? List.of() : answerIds;


        long now = System.currentTimeMillis();
        for (Long answerId : selected) {
            UserAnswer ua = new UserAnswer();
            ua.setParticipantId(participant.getId());
            ua.setQuestionId(questionId);
            ua.setAnswerId(answerId);
            ua.setAnsweredAt(now);
            userAnswerRepository.save(ua);
        }


        List<Answer> all = answerRepository.findByQuestionIdOrderByIdAsc(questionId);
        Set<Long> correctIds = all.stream()
                .filter(a -> Boolean.TRUE.equals(a.getIsCorrect()))
                .map(Answer::getId)
                .collect(Collectors.toSet());
        boolean correct = !selected.isEmpty()
                && correctIds.containsAll(selected)
                && selected.size() == correctIds.size();

        int points = 0;
        if (correct) {
            points = BASE_POINTS + SPEED_BONUS_MAX;
            participant.setScore((participant.getScore() == null ? 0 : participant.getScore()) + points);
            participantRepository.save(participant);
        }

        return new SubmitResult(true, correct, points, participant.getScore());
    }


    public List<LeaderboardEntry> leaderboard(Long sessionId) {
        List<Participant> participants =
                participantRepository.findBySessionIdOrderByScoreDesc(sessionId);
        Map<Long, String> names = new HashMap<>();
        for (Participant p : participants) {
            names.computeIfAbsent(p.getUserId(),
                    id -> userRepository.findById(id).map(User::getUsername).orElse("User " + id));
        }
        List<LeaderboardEntry> result = new ArrayList<>();
        int rank = 1;
        for (Participant p : participants) {
            result.add(new LeaderboardEntry(p.getId(), names.get(p.getUserId()),
                    p.getScore() == null ? 0 : p.getScore(), rank++));
        }
        return result;
    }


    public QuestionDto toQuestionDto(Question q, int index, int total, Integer timeLimitSeconds) {
        QuestionDto dto = new QuestionDto();
        dto.setId(q.getId());
        dto.setText(q.getText());
        dto.setImageUrl(q.getImageUrl());
        dto.setType(q.getType());
        dto.setIndex(index);
        dto.setTotal(total);
        dto.setTimeLimitSeconds(timeLimitSeconds);
        dto.setAnswers(answerRepository.findByQuestionIdOrderByIdAsc(q.getId()).stream().map(a -> {
            QuestionDto.AnswerDto ad = new QuestionDto.AnswerDto();
            ad.setId(a.getId());
            ad.setText(a.getText());
            return ad;
        }).toList());
        return dto;
    }


    @Transactional
    public QuizSession finishSession(QuizSession session) {
        session.setStatus("FINISHED");
        session.setCurrentQuestionId(null);
        session.setFinishedAt(Instant.now());
        return sessionRepository.save(session);
    }

    public Optional<Quiz> quizOf(QuizSession session) {
        return quizRepository.findById(session.getQuizId());
    }


    public static class SubmitResult {
        private final boolean accepted;
        private final boolean correct;
        private final int points;
        private final int totalScore;
        private final String reason;

        public SubmitResult(boolean accepted, boolean correct, int points, int totalScore) {
            this.accepted = accepted;
            this.correct = correct;
            this.points = points;
            this.totalScore = totalScore;
            this.reason = null;
        }

        private SubmitResult(String reason) {
            this.accepted = false;
            this.correct = false;
            this.points = 0;
            this.totalScore = 0;
            this.reason = reason;
        }

        public static SubmitResult rejected(String reason) {
            return new SubmitResult(reason);
        }

        public boolean isAccepted() { return accepted; }
        public boolean isCorrect() { return correct; }
        public int getPoints() { return points; }
        public int getTotalScore() { return totalScore; }
        public String getReason() { return reason; }
    }
}

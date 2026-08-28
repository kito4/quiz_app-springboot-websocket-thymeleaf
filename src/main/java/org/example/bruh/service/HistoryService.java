package org.example.bruh.service;

import org.example.bruh.entity.Participant;
import org.example.bruh.entity.Quiz;
import org.example.bruh.entity.QuizSession;
import org.example.bruh.repository.ParticipantRepository;
import org.example.bruh.repository.QuizRepository;
import org.example.bruh.repository.QuizSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
public class HistoryService {

    @Autowired
    private ParticipantRepository participantRepository;

    @Autowired
    private QuizSessionRepository sessionRepository;

    @Autowired
    private QuizRepository quizRepository;


    public List<ParticipationRow> participationHistory(Long userId) {
        List<ParticipationRow> rows = new ArrayList<>();
        for (Participant p : participantRepository.findByUserIdOrderByIdDesc(userId)) {
            QuizSession session = sessionRepository.findById(p.getSessionId()).orElse(null);
            if (session == null) continue;
            Quiz quiz = quizRepository.findById(session.getQuizId()).orElse(null);
            rows.add(new ParticipationRow(
                    quiz == null ? "Квиз #" + session.getQuizId() : quiz.getTitle(),
                    session.getRoomCode(),
                    session.getStatus(),
                    p.getScore() == null ? 0 : p.getScore(),
                    session.getStartedAt() == null ? "" : session.getStartedAt().toString().substring(0, 10)
            ));
        }
        return rows;
    }


    public List<HostRow> hostedHistory(Long userId) {
        List<HostRow> rows = new ArrayList<>();
        for (QuizSession s : sessionRepository.findByHostIdOrderByIdDesc(userId)) {
            Quiz quiz = quizRepository.findById(s.getQuizId()).orElse(null);
            rows.add(new HostRow(
                    s.getId(),
                    quiz == null ? "Квиз #" + s.getQuizId() : quiz.getTitle(),
                    s.getRoomCode(),
                    s.getStatus(),
                    s.getStartedAt() == null ? "" : s.getStartedAt().toString().substring(0, 10)
            ));
        }
        return rows;
    }

    public static class ParticipationRow {
        private final String quizTitle;
        private final String roomCode;
        private final String status;
        private final int score;
        private final String date;

        public ParticipationRow(String quizTitle, String roomCode, String status, int score, String date) {
            this.quizTitle = quizTitle;
            this.roomCode = roomCode;
            this.status = status;
            this.score = score;
            this.date = date;
        }

        public String getQuizTitle() { return quizTitle; }
        public String getRoomCode() { return roomCode; }
        public String getStatus() { return status; }
        public int getScore() { return score; }
        public String getDate() { return date; }
    }

    public static class HostRow {
        private final Long sessionId;
        private final String quizTitle;
        private final String roomCode;
        private final String status;
        private final String date;

        public HostRow(Long sessionId, String quizTitle, String roomCode, String status, String date) {
            this.sessionId = sessionId;
            this.quizTitle = quizTitle;
            this.roomCode = roomCode;
            this.status = status;
            this.date = date;
        }

        public Long getSessionId() { return sessionId; }
        public String getQuizTitle() { return quizTitle; }
        public String getRoomCode() { return roomCode; }
        public String getStatus() { return status; }
        public String getDate() { return date; }
    }
}

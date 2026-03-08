package org.example.bruh.service;

import org.example.bruh.entity.Participant;
import org.example.bruh.entity.QuizSession;
import org.example.bruh.repository.ParticipantRepository;
import org.example.bruh.repository.QuizSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SessionService {

    @Autowired
    private QuizSessionRepository sessionRepository;

    @Autowired
    private ParticipantRepository participantRepository;

    public Optional<QuizSession> findByRoom(String code) {
        return sessionRepository.findByRoomCode(code);
    }

    public Participant joinSession(Long userId, QuizSession session) {
        // Если участник уже зарегистрирован — вернуть существующего
        Optional<Participant> existing = participantRepository.findByUserIdAndSessionId(userId, session.getId());
        if (existing.isPresent()) return existing.get();

        Participant p = new Participant();
        p.setUserId(userId);
        p.setSessionId(session.getId());
        p.setScore(0);
        return participantRepository.save(p);
    }

    public List<Participant> leaderboard(Long sessionId) {
        return participantRepository.findBySessionIdOrderByScoreDesc(sessionId);
    }
}

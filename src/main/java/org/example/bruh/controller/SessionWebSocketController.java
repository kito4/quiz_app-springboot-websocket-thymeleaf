package org.example.bruh.controller;

import org.example.bruh.dto.QuestionDto;
import org.example.bruh.entity.Participant;
import org.example.bruh.entity.Question;
import org.example.bruh.repository.AnswerRepository;
import org.example.bruh.repository.ParticipantRepository;
import org.example.bruh.repository.QuestionRepository;
import org.example.bruh.repository.QuizSessionRepository;
import org.example.bruh.service.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Optional;

@Controller
public class SessionWebSocketController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private QuizSessionRepository sessionRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private AnswerRepository answerRepository;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private ParticipantRepository participantRepository;

    // message mapping to start session / push next question / end session

    @MessageMapping("/session/next")
    public void nextQuestion(NextQuestionMessage msg) {
        Optional<Question> qOpt = questionRepository.findById(msg.getQuestionId());
        if (qOpt.isPresent()) {
            Question q = qOpt.get();
            // build DTO with answers (without isCorrect flag)
            var dto = new QuestionDto();
            dto.setId(q.getId());
            dto.setText(q.getText());
            dto.setImageUrl(q.getImageUrl());
            dto.setType(q.getType());
            dto.setTimeLimitSeconds(msg.getTimeLimitSeconds());
            var answers = answerRepository.findByQuestionId(q.getId()).stream().map(a -> {
                var ad = new QuestionDto.AnswerDto();
                ad.setId(a.getId());
                ad.setText(a.getText());
                return ad;
            }).toList();
            dto.setAnswers(answers);

            // update session currentQuestionId
            var sOpt = sessionRepository.findByRoomCode(msg.getRoomCode());
            if (sOpt.isPresent()) {
                var s = sOpt.get();
                s.setCurrentQuestionId(q.getId());
                sessionRepository.save(s);
            }

            messagingTemplate.convertAndSend("/topic/session/" + msg.getRoomCode() + "/question", dto);
        }
    }

    @MessageMapping("/session/end")
    public void endSession(EndSessionMessage msg) {
        var sOpt = sessionRepository.findByRoomCode(msg.getRoomCode());
        if (sOpt.isPresent()) {
            var s = sOpt.get();
            s.setStatus("FINISHED");
            sessionRepository.save(s);

            // push final leaderboard
            List<Participant> lb = sessionService.leaderboard(s.getId());
            messagingTemplate.convertAndSend("/topic/session/" + msg.getRoomCode() + "/leaderboard", lb);
        }
    }

    public static class NextQuestionMessage {
        private String roomCode;
        private Long questionId;
        private Integer timeLimitSeconds;

        public String getRoomCode() { return roomCode; }
        public void setRoomCode(String roomCode) { this.roomCode = roomCode; }
        public Long getQuestionId() { return questionId; }
        public void setQuestionId(Long questionId) { this.questionId = questionId; }
        public Integer getTimeLimitSeconds() { return timeLimitSeconds; }
        public void setTimeLimitSeconds(Integer timeLimitSeconds) { this.timeLimitSeconds = timeLimitSeconds; }
    }

    public static class EndSessionMessage {
        private String roomCode;
        public String getRoomCode() { return roomCode; }
        public void setRoomCode(String roomCode) { this.roomCode = roomCode; }
    }
}

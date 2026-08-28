package org.example.bruh.controller;

import org.example.bruh.dto.QuestionDto;
import org.example.bruh.entity.*;
import org.example.bruh.repository.UserRepository;
import org.example.bruh.service.QuizService;
import org.example.bruh.service.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class SessionController {

    @Autowired
    private QuizService quizService;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @GetMapping("/session/join")
    public String joinForm() {
        return "join-room";
    }


    @PostMapping("/session/join")
    public String join(@RequestParam String code,
                       @AuthenticationPrincipal UserDetails principal,
                       Model model) {
        Optional<QuizSession> sOpt = sessionService.findByRoom(code);
        if (sOpt.isEmpty()) {
            model.addAttribute("error", "Комната с таким кодом не найдена");
            return "join-room";
        }
        QuizSession session = sOpt.get();
        if ("FINISHED".equals(session.getStatus())) {
            model.addAttribute("error", "Этот квиз уже завершён");
            return "join-room";
        }
        User user = userRepository.findByUsername(principal.getUsername()).orElse(null);
        sessionService.joinSession(user.getId(), session);


        broadcastJoined(session);

        model.addAttribute("roomCode", session.getRoomCode());
        model.addAttribute("quizTitle", sessionService.quizOf(session).map(Quiz::getTitle).orElse("Квиз"));
        return "room";
    }


    @PostMapping("/session/start/{quizId}")
    public String start(@PathVariable Long quizId,
                        @AuthenticationPrincipal UserDetails principal) {
        Quiz quiz = quizService.findQuiz(quizId).orElse(null);
        if (quiz == null || quizService.countQuestions(quizId) == 0) {
            return "redirect:/dashboard?error=no-questions";
        }
        User user = userRepository.findByUsername(principal.getUsername()).orElse(null);
        QuizSession session = quizService.startSession(quizId, user == null ? null : user.getId());
        return "redirect:/session/host/" + session.getId();
    }


    @GetMapping("/session/host/{sessionId}")
    public String host(@PathVariable Long sessionId, Model model) {
        QuizSession session = sessionService.findById(sessionId).orElse(null);
        if (session == null) {
            return "redirect:/dashboard";
        }
        Quiz quiz = sessionService.quizOf(session).orElse(null);
        List<Question> questions = quizService.findQuestions(session.getQuizId());

        model.addAttribute("quizSession", session);
        model.addAttribute("quiz", quiz);
        model.addAttribute("questions", questions);
        return "session-host";
    }


    @PostMapping("/session/{sessionId}/next")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> next(@PathVariable Long sessionId) {
        QuizSession session = sessionService.findById(sessionId).orElse(null);
        if (session == null) {
            return ResponseEntity.notFound().build();
        }
        if ("FINISHED".equals(session.getStatus())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Сессия завершена"));
        }

        List<Question> questions = quizService.findQuestions(session.getQuizId());
        if (questions.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "В квизе нет вопросов"));
        }


        Question next = null;
        Long currentId = session.getCurrentQuestionId();
        if (currentId == null) {
            next = questions.get(0);
        } else {
            for (Question q : questions) {
                if (q.getId() > currentId) {
                    next = q;
                    break;
                }
            }
        }
        if (next == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Вопросы закончились — завершите квиз"));
        }

        if (!"RUNNING".equals(session.getStatus())) {
            session.setStatus("RUNNING");
        }
        session.setCurrentQuestionId(next.getId());
        sessionService.save(session);

        Quiz quiz = sessionService.quizOf(session).orElse(null);
        int timeLimit = quiz == null || quiz.getTimeLimitSeconds() == null ? 30 : quiz.getTimeLimitSeconds();
        int index = questions.indexOf(next) + 1;

        QuestionDto dto = sessionService.toQuestionDto(next, index, questions.size(), timeLimit);
        String room = session.getRoomCode();
        messagingTemplate.convertAndSend("/topic/session/" + room + "/question", dto);
        broadcastStatus(session);

        Map<String, Object> resp = new HashMap<>();
        resp.put("questionId", next.getId());
        resp.put("index", index);
        resp.put("total", questions.size());
        resp.put("timeLimitSeconds", timeLimit);
        return ResponseEntity.ok(resp);
    }


    @PostMapping("/session/{sessionId}/end")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> end(@PathVariable Long sessionId) {
        QuizSession session = sessionService.findById(sessionId).orElse(null);
        if (session == null) {
            return ResponseEntity.notFound().build();
        }
        sessionService.finishSession(session);

        String room = session.getRoomCode();
        broadcastStatus(session);
        messagingTemplate.convertAndSend("/topic/session/" + room + "/leaderboard",
                sessionService.leaderboard(sessionId));

        return ResponseEntity.ok(Map.of("status", "FINISHED"));
    }

    public static class SubmitRequest {
        private String roomCode;
        private Long questionId;
        private List<Long> answerIds;

        public String getRoomCode() { return roomCode; }
        public void setRoomCode(String roomCode) { this.roomCode = roomCode; }
        public Long getQuestionId() { return questionId; }
        public void setQuestionId(Long questionId) { this.questionId = questionId; }
        public List<Long> getAnswerIds() { return answerIds; }
        public void setAnswerIds(List<Long> answerIds) { this.answerIds = answerIds; }
    }


    @PostMapping("/api/session/submit")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> submit(@RequestBody SubmitRequest req,
                                                      @AuthenticationPrincipal UserDetails principal) {
        User user = userRepository.findByUsername(principal.getUsername()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        SessionService.SubmitResult result =
                sessionService.submitAnswer(req.getRoomCode(), user.getId(), req.getQuestionId(), req.getAnswerIds());

        Map<String, Object> resp = new HashMap<>();
        resp.put("accepted", result.isAccepted());
        resp.put("correct", result.isCorrect());
        resp.put("points", result.getPoints());
        resp.put("totalScore", result.getTotalScore());
        if (result.getReason() != null) {
            resp.put("reason", result.getReason());
        }


        if (result.isAccepted()) {
            Optional<QuizSession> sOpt = sessionService.findByRoom(req.getRoomCode());
            if (sOpt.isPresent()) {
                QuizSession session = sOpt.get();
                messagingTemplate.convertAndSend(
                        "/topic/session/" + session.getRoomCode() + "/leaderboard",
                        sessionService.leaderboard(session.getId()));
            }
        }
        return ResponseEntity.ok(resp);
    }


    @GetMapping("/api/session/{roomCode}/state")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> state(@PathVariable String roomCode) {
        Optional<QuizSession> sOpt = sessionService.findByRoom(roomCode);
        if (sOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        QuizSession session = sOpt.get();

        Map<String, Object> resp = new HashMap<>();
        resp.put("status", session.getStatus());
        resp.put("roomCode", session.getRoomCode());
        resp.put("quizTitle", sessionService.quizOf(session).map(Quiz::getTitle).orElse("Квиз"));
        resp.put("leaderboard", sessionService.leaderboard(session.getId()));

        QuestionDto current = null;
        if ("RUNNING".equals(session.getStatus()) && session.getCurrentQuestionId() != null) {
            List<Question> questions = quizService.findQuestions(session.getQuizId());
            for (int i = 0; i < questions.size(); i++) {
                if (questions.get(i).getId().equals(session.getCurrentQuestionId())) {
                    Quiz quiz = sessionService.quizOf(session).orElse(null);
                    int timeLimit = quiz == null || quiz.getTimeLimitSeconds() == null
                            ? 30 : quiz.getTimeLimitSeconds();
                    current = sessionService.toQuestionDto(questions.get(i), i + 1, questions.size(), timeLimit);
                    break;
                }
            }
        }
        resp.put("question", current);
        return ResponseEntity.ok(resp);
    }

    private void broadcastStatus(QuizSession session) {
        Map<String, Object> status = new HashMap<>();
        status.put("status", session.getStatus());
        status.put("roomCode", session.getRoomCode());
        messagingTemplate.convertAndSend("/topic/session/" + session.getRoomCode() + "/status", status);
    }

    private void broadcastJoined(QuizSession session) {
        messagingTemplate.convertAndSend("/topic/session/" + session.getRoomCode() + "/joined",
                Map.of("count", sessionService.countParticipants(session.getId())));
    }
}

package org.example.bruh.controller;


import org.example.bruh.entity.QuizSession;
import org.example.bruh.repository.QuizRepository;
import org.example.bruh.repository.QuizSessionRepository;
import org.example.bruh.repository.UserRepository;
import org.example.bruh.service.QuizService;
import org.example.bruh.service.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Optional;

@Controller
@RequestMapping("/session")
public class SessionController {

    @Autowired
    private QuizService quizService;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private QuizSessionRepository sessionRepository;

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/join")
    public String joinForm() { return "join-room"; }

    @PostMapping("/join")
    public String join(@RequestParam String code, Principal principal, Model model) {
        Optional<QuizSession> s = sessionService.findByRoom(code);
        if (s.isEmpty()) {
            model.addAttribute("error", "Room not found");
            return "join-room";
        }
        model.addAttribute("roomCode", code);
        return "room"; // Thymeleaf template that will open websocket and subscribe
    }

    @PostMapping("/start/{quizId}")
    public String start(@PathVariable Long quizId, Model model) {
        QuizSession session = quizService.startSession(quizId);
        model.addAttribute("roomCode", session.getRoomCode());
        return "session-started";
    }
}

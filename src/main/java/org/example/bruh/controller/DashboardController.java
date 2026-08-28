package org.example.bruh.controller;

import org.example.bruh.entity.User;
import org.example.bruh.repository.UserRepository;
import org.example.bruh.service.HistoryService;
import org.example.bruh.service.QuizService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class DashboardController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private QuizService quizService;

    @Autowired
    private HistoryService historyService;

    @GetMapping({"/", "/dashboard"})
    public String dashboard(@AuthenticationPrincipal UserDetails principal, Model model) {
        User user = userRepository.findByUsername(principal.getUsername()).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }

        boolean organizer = "ROLE_ORGANIZER".equals(user.getRole());
        model.addAttribute("username", user.getUsername());
        model.addAttribute("organizer", organizer);

        model.addAttribute("participations", historyService.participationHistory(user.getId()));

        if (organizer) {

            model.addAttribute("quizzes", quizService.findQuizzesByOrganizer(user.getId()));
            model.addAttribute("hosted", historyService.hostedHistory(user.getId()));
        } else {
            model.addAttribute("quizzes", List.of());
            model.addAttribute("hosted", List.of());
        }
        return "dashboard";
    }
}

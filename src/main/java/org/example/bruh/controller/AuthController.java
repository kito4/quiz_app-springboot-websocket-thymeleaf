package org.example.bruh.controller;

import org.example.bruh.entity.User;
import org.example.bruh.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String registerForm() {
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String username,
                           @RequestParam String password,
                           @RequestParam(required = false) String role,
                           Model model) {
        String name = username == null ? "" : username.trim();
        if (name.isEmpty() || password == null || password.length() < 4) {
            model.addAttribute("error", "Имя — минимум 1 символ, пароль — минимум 4 символа");
            return "register";
        }
        if (userRepository.existsByUsername(name)) {
            model.addAttribute("error", "Такой пользователь уже существует");
            return "register";
        }
        User u = new User();
        u.setUsername(name);
        u.setPassword(passwordEncoder.encode(password));
        u.setRole("ROLE_ORGANIZER".equals(role) ? "ROLE_ORGANIZER" : "ROLE_PARTICIPANT");
        userRepository.save(u);
        return "redirect:/login?registered=true";
    }
}

package org.example.bruh.controller;

import org.example.bruh.entity.*;
import org.example.bruh.repository.UserRepository;
import org.example.bruh.service.QuizService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/quiz")
public class QuizController {

    @Autowired
    private QuizService quizService;

    @Autowired
    private UserRepository userRepository;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @GetMapping("/create")
    public String createForm() {
        return "quiz-create";
    }

    @PostMapping
    public String create(@RequestParam String title,
                         @RequestParam(required = false) String category,
                         @RequestParam(required = false) Integer timeLimitSeconds,
                         @AuthenticationPrincipal UserDetails principal,
                         Model model) {
        if (title == null || title.trim().isEmpty()) {
            model.addAttribute("error", "Название не может быть пустым");
            return "quiz-create";
        }
        User user = userRepository.findByUsername(principal.getUsername()).orElse(null);
        Quiz q = new Quiz();
        q.setTitle(title.trim());
        q.setCategory(category == null || category.trim().isEmpty() ? "Без категории" : category.trim());
        q.setTimeLimitSeconds(timeLimitSeconds == null || timeLimitSeconds <= 0 ? 30 : timeLimitSeconds);
        q.setCreatedBy(user == null ? null : user.getId());
        Quiz saved = quizService.saveQuiz(q);
        return "redirect:/quiz/" + saved.getId() + "/questions";
    }

    @GetMapping("/{id}/questions")
    public String questions(@PathVariable Long id, Model model) {
        Quiz quiz = quizService.findQuiz(id).orElse(null);
        if (quiz == null) {
            return "redirect:/dashboard";
        }
        model.addAttribute("quiz", quiz);
        model.addAttribute("questions", quizService.findQuestions(id));
        return "quiz-questions";
    }

    @PostMapping("/{id}/question")
    public String addQuestion(@PathVariable Long id,
                              @RequestParam String text,
                              @RequestParam(required = false) String imageUrl,
                              @RequestParam(required = false) MultipartFile imageFile,
                              @RequestParam String type,
                              @RequestParam(name = "answers") List<String> answers,
                              @RequestParam(name = "correctIndexes", required = false) List<Integer> correctIndexes) {
        if (quizService.findQuiz(id).isEmpty()) {
            return "redirect:/dashboard";
        }

        Question q = new Question();
        q.setQuizId(id);
        q.setText(text);
        q.setType("MULTIPLE".equals(type) ? "MULTIPLE" : "SINGLE");


        String uploaded = saveImage(imageFile);
        if (uploaded != null) {
            q.setImageUrl(uploaded);
        } else if (imageUrl != null && !imageUrl.trim().isEmpty()) {
            q.setImageUrl(imageUrl.trim());
        }

        Question saved = quizService.addQuestion(q);

        for (int i = 0; i < answers.size(); i++) {
            String answerText = answers.get(i);
            if (answerText == null || answerText.trim().isEmpty()) {
                continue;
            }
            Answer a = new Answer();
            a.setQuestionId(saved.getId());
            a.setText(answerText.trim());
            a.setIsCorrect(correctIndexes != null && correctIndexes.contains(i));
            quizService.addAnswer(a);
        }

        return "redirect:/quiz/" + id + "/questions";
    }

    @PostMapping("/{id}/question/{questionId}/delete")
    public String deleteQuestion(@PathVariable Long id, @PathVariable Long questionId) {
        quizService.deleteQuestion(questionId);
        return "redirect:/quiz/" + id + "/questions";
    }

    @PostMapping("/{id}/delete")
    public String deleteQuiz(@PathVariable Long id) {
        quizService.deleteQuiz(id);
        return "redirect:/dashboard";
    }

    private String saveImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        try {
            Path dir = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(dir);
            String original = file.getOriginalFilename() == null ? "img" : file.getOriginalFilename();
            String ext = original.contains(".") ? original.substring(original.lastIndexOf('.')) : ".png";
            String name = UUID.randomUUID() + ext;
            Path target = dir.resolve(name);
            file.transferTo(target);
            return "/uploads/" + name;
        } catch (IOException e) {
            return null;
        }
    }
}

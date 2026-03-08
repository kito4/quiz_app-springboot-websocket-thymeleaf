package org.example.bruh.controller;

import org.example.bruh.entity.Answer;
import org.example.bruh.entity.Question;
import org.example.bruh.entity.Quiz;
import org.example.bruh.repository.AnswerRepository;
import org.example.bruh.repository.QuestionRepository;
import org.example.bruh.repository.QuizRepository;
import org.example.bruh.service.QuizService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/quiz")
public class QuizController {

    @Autowired
    private QuizService quizService;

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private AnswerRepository answerRepository;

    @GetMapping("/create")
    public String createForm() { return "quiz-create"; }

    @PostMapping
    public String create(@RequestParam String title,
                         @RequestParam String category,
                         Principal principal) {
        Quiz q = new Quiz();
        q.setTitle(title);
        q.setCategory(category);
        // createdBy omitted for brevity
        quizService.saveQuiz(q);
        return "redirect:/dashboard";
    }

    @GetMapping("/{id}/questions")
    public String questions(@PathVariable Long id, Model model) {
        List<Question> list = quizService.findQuestions(id);
        model.addAttribute("questions", list);
        model.addAttribute("quizId", id);
        return "quiz-questions";
    }

    @PostMapping("/{id}/question")
    public String addQuestion(@PathVariable Long id,
                              @RequestParam String text,
                              @RequestParam(required = false) String imageUrl,
                              @RequestParam String type,
                              @RequestParam(name = "answers") List<String> answers,
                              @RequestParam(name = "correctIndex") Integer correctIndex) {
        Question q = new Question();
        q.setQuizId(id);
        q.setText(text);
        q.setImageUrl(imageUrl);
        q.setType(type);
        Question saved = quizService.addQuestion(q);

        for (int i = 0; i < answers.size(); i++) {
            Answer a = new Answer();
            a.setQuestionId(saved.getId());
            a.setText(answers.get(i));
            a.setIsCorrect(i == correctIndex);
            answerRepository.save(a);
        }

        return "redirect:/quiz/" + id + "/questions";
    }

}

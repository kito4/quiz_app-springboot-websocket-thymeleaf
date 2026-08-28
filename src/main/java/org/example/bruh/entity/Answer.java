package org.example.bruh.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "answer")
public class Answer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long questionId;

    @Column(columnDefinition = "TEXT")
    private String text;

    private Boolean isCorrect;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public Boolean getIsCorrect() { return isCorrect; }
    public void setIsCorrect(Boolean correct) { isCorrect = correct; }
}

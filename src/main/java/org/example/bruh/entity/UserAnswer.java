package org.example.bruh.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "user_answer")
public class UserAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long participantId;

    private Long questionId;

    private Long answerId;

    private Long answeredAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getParticipantId() { return participantId; }
    public void setParticipantId(Long participantId) { this.participantId = participantId; }

    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }

    public Long getAnswerId() { return answerId; }
    public void setAnswerId(Long answerId) { this.answerId = answerId; }

    public Long getAnsweredAt() { return answeredAt; }
    public void setAnsweredAt(Long answeredAt) { this.answeredAt = answeredAt; }
}

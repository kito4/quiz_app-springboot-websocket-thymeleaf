package org.example.bruh.entity;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "quiz_session")
public class QuizSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long quizId;

    @Column(unique = true)
    private String roomCode;

    private String status;

    private Long currentQuestionId;

    private Long hostId;

    private Instant startedAt;

    private Instant finishedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getQuizId() { return quizId; }
    public void setQuizId(Long quizId) { this.quizId = quizId; }

    public String getRoomCode() { return roomCode; }
    public void setRoomCode(String roomCode) { this.roomCode = roomCode; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getCurrentQuestionId() { return currentQuestionId; }
    public void setCurrentQuestionId(Long currentQuestionId) { this.currentQuestionId = currentQuestionId; }

    public Long getHostId() { return hostId; }
    public void setHostId(Long hostId) { this.hostId = hostId; }

    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }

    public Instant getFinishedAt() { return finishedAt; }
    public void setFinishedAt(Instant finishedAt) { this.finishedAt = finishedAt; }
}

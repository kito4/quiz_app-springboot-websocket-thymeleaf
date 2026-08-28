package org.example.bruh.dto;


public class LeaderboardEntry {
    private Long participantId;
    private String username;
    private Integer score;
    private Integer rank;

    public LeaderboardEntry() {}

    public LeaderboardEntry(Long participantId, String username, Integer score, Integer rank) {
        this.participantId = participantId;
        this.username = username;
        this.score = score;
        this.rank = rank;
    }

    public Long getParticipantId() { return participantId; }
    public void setParticipantId(Long participantId) { this.participantId = participantId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }

    public Integer getRank() { return rank; }
    public void setRank(Integer rank) { this.rank = rank; }
}

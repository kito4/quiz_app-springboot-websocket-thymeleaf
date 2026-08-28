package org.example.bruh.dto;

import java.util.List;


public class QuestionDto {
    private Long id;
    private String text;
    private String imageUrl;
    private String type;
    private Integer index;
    private Integer total;
    private Integer timeLimitSeconds;
    private List<AnswerDto> answers;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Integer getIndex() { return index; }
    public void setIndex(Integer index) { this.index = index; }

    public Integer getTotal() { return total; }
    public void setTotal(Integer total) { this.total = total; }

    public Integer getTimeLimitSeconds() { return timeLimitSeconds; }
    public void setTimeLimitSeconds(Integer timeLimitSeconds) { this.timeLimitSeconds = timeLimitSeconds; }

    public List<AnswerDto> getAnswers() { return answers; }
    public void setAnswers(List<AnswerDto> answers) { this.answers = answers; }

    public static class AnswerDto {
        private Long id;
        private String text;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
    }
}

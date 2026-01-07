package com.healthsphere.dto;

public class WebRTCAnswerDTO {
    private String sessionId;
    private String answer;

    public WebRTCAnswerDTO() {}

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }
}



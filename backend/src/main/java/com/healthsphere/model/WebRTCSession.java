package com.healthsphere.model;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class WebRTCSession {
    private String sessionId;
    private Long doctorId;
    private Long patientId;
    private String offer;
    private String answer;
    private Map<String, String> iceCandidates = new ConcurrentHashMap<>();
    private LocalDateTime createdAt;
    private boolean active;

    public WebRTCSession(String sessionId, Long doctorId, Long patientId) {
        this.sessionId = sessionId;
        this.doctorId = doctorId;
        this.patientId = patientId;
        this.createdAt = LocalDateTime.now();
        this.active = true;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public String getOffer() {
        return offer;
    }

    public void setOffer(String offer) {
        this.offer = offer;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public Map<String, String> getIceCandidates() {
        return iceCandidates;
    }

    public void setIceCandidates(Map<String, String> iceCandidates) {
        this.iceCandidates = iceCandidates;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}



package com.healthsphere.service;

import com.healthsphere.dto.WebRTCAnswerDTO;
import com.healthsphere.dto.WebRTCOfferDTO;
import com.healthsphere.dto.WebRTCICEDTO;
import com.healthsphere.model.WebRTCSession;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class WebRTCService {
    private Map<String, WebRTCSession> sessions = new ConcurrentHashMap<>();

    public String createSession(Long doctorId, Long patientId) {
        String sessionId = UUID.randomUUID().toString();
        WebRTCSession session = new WebRTCSession(sessionId, doctorId, patientId);
        sessions.put(sessionId, session);
        return sessionId;
    }

    public void setOffer(String sessionId, String offer) {
        WebRTCSession session = sessions.get(sessionId);
        if (session != null) {
            session.setOffer(offer);
        }
    }

    public void setAnswer(String sessionId, String answer) {
        WebRTCSession session = sessions.get(sessionId);
        if (session != null) {
            session.setAnswer(answer);
        }
    }

    public void addICECandidate(String sessionId, String candidate, String sdpMid, Integer sdpMLineIndex) {
        WebRTCSession session = sessions.get(sessionId);
        if (session != null) {
            String key = sdpMid + "_" + sdpMLineIndex;
            session.getIceCandidates().put(key, candidate);
        }
    }

    public WebRTCSession getSession(String sessionId) {
        return sessions.get(sessionId);
    }

    public WebRTCSession getSessionByParticipants(Long doctorId, Long patientId) {
        return sessions.values().stream()
            .filter(s -> s.getDoctorId().equals(doctorId) && s.getPatientId().equals(patientId) && s.isActive())
            .findFirst()
            .orElse(null);
    }

    public void endSession(String sessionId) {
        WebRTCSession session = sessions.get(sessionId);
        if (session != null) {
            session.setActive(false);
            sessions.remove(sessionId);
        }
    }
}


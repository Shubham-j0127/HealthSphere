package com.healthsphere.controller;

import com.healthsphere.dto.WebRTCAnswerDTO;
import com.healthsphere.dto.WebRTCOfferDTO;
import com.healthsphere.dto.WebRTCICEDTO;
import com.healthsphere.model.User;
import com.healthsphere.model.WebRTCSession;
import com.healthsphere.service.UserService;
import com.healthsphere.service.WebRTCService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/webrtc")
@CrossOrigin(origins = "*")
public class WebRTCController {
    @Autowired
    private WebRTCService webRTCService;

    @Autowired
    private UserService userService;

    @PostMapping("/session")
    public ResponseEntity<Map<String, String>> createSession(
            @RequestBody Map<String, Long> request,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            User user = userService.getUserByEmail(email);
            if (user == null) {
                return ResponseEntity.badRequest().build();
            }

            Long doctorId = request.get("doctorId");
            Long patientId = request.get("patientId");

            // Verify user is either the doctor or patient
            Long currentDoctorId = userService.getDoctorIdByUserId(user.getId());
            Long currentPatientId = userService.getPatientIdByUserId(user.getId());

            if ((currentDoctorId == null || !currentDoctorId.equals(doctorId)) &&
                (currentPatientId == null || !currentPatientId.equals(patientId))) {
                return ResponseEntity.badRequest().build();
            }

            String sessionId = webRTCService.createSession(doctorId, patientId);
            Map<String, String> response = new HashMap<>();
            response.put("sessionId", sessionId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/offer")
    public ResponseEntity<Map<String, String>> setOffer(
            @RequestBody WebRTCOfferDTO offerDTO,
            Authentication authentication) {
        try {
            WebRTCSession session = webRTCService.getSession(offerDTO.getSessionId());
            if (session == null) {
                return ResponseEntity.badRequest().build();
            }

            webRTCService.setOffer(offerDTO.getSessionId(), offerDTO.getOffer());
            Map<String, String> response = new HashMap<>();
            response.put("status", "offer_received");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/offer/{sessionId}")
    public ResponseEntity<Map<String, String>> getOffer(@PathVariable String sessionId) {
        try {
            WebRTCSession session = webRTCService.getSession(sessionId);
            if (session == null || session.getOffer() == null) {
                return ResponseEntity.badRequest().build();
            }

            Map<String, String> response = new HashMap<>();
            response.put("offer", session.getOffer());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/answer")
    public ResponseEntity<Map<String, String>> setAnswer(
            @RequestBody WebRTCAnswerDTO answerDTO,
            Authentication authentication) {
        try {
            WebRTCSession session = webRTCService.getSession(answerDTO.getSessionId());
            if (session == null) {
                return ResponseEntity.badRequest().build();
            }

            webRTCService.setAnswer(answerDTO.getSessionId(), answerDTO.getAnswer());
            Map<String, String> response = new HashMap<>();
            response.put("status", "answer_received");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/answer/{sessionId}")
    public ResponseEntity<Map<String, String>> getAnswer(@PathVariable String sessionId) {
        try {
            WebRTCSession session = webRTCService.getSession(sessionId);
            if (session == null || session.getAnswer() == null) {
                return ResponseEntity.badRequest().build();
            }

            Map<String, String> response = new HashMap<>();
            response.put("answer", session.getAnswer());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/ice")
    public ResponseEntity<Map<String, String>> addICECandidate(
            @RequestBody WebRTCICEDTO iceDTO,
            Authentication authentication) {
        try {
            webRTCService.addICECandidate(
                iceDTO.getSessionId(),
                iceDTO.getCandidate(),
                iceDTO.getSdpMid(),
                iceDTO.getSdpMLineIndex()
            );
            Map<String, String> response = new HashMap<>();
            response.put("status", "ice_candidate_received");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/ice/{sessionId}")
    public ResponseEntity<Map<String, Object>> getICECandidates(@PathVariable String sessionId) {
        try {
            WebRTCSession session = webRTCService.getSession(sessionId);
            if (session == null) {
                return ResponseEntity.badRequest().build();
            }

            Map<String, Object> response = new HashMap<>();
            response.put("candidates", session.getIceCandidates());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/end/{sessionId}")
    public ResponseEntity<Map<String, String>> endSession(
            @PathVariable String sessionId,
            Authentication authentication) {
        try {
            webRTCService.endSession(sessionId);
            Map<String, String> response = new HashMap<>();
            response.put("status", "session_ended");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}


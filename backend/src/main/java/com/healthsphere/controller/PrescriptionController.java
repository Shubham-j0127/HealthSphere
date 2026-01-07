package com.healthsphere.controller;

import com.healthsphere.dto.PrescriptionDTO;
import com.healthsphere.model.User;
import com.healthsphere.service.PrescriptionService;
import com.healthsphere.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/prescriptions")
@CrossOrigin(origins = "*")
public class PrescriptionController {
    @Autowired
    private PrescriptionService prescriptionService;

    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<PrescriptionDTO> createPrescription(
            @RequestBody PrescriptionDTO dto,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            User user = userService.getUserByEmail(email);
            if (user == null) {
                return ResponseEntity.badRequest().build();
            }
            
            Long doctorId = userService.getDoctorIdByUserId(user.getId());
            if (doctorId == null) {
                return ResponseEntity.badRequest().build();
            }

            PrescriptionDTO created = prescriptionService.createPrescription(dto, doctorId);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<PrescriptionDTO>> getPrescriptionsByPatient(@PathVariable Long patientId) {
        try {
            List<PrescriptionDTO> prescriptions = prescriptionService.getPrescriptionsByPatient(patientId);
            return ResponseEntity.ok(prescriptions);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<PrescriptionDTO>> getPrescriptionsByDoctor(@PathVariable Long doctorId) {
        try {
            List<PrescriptionDTO> prescriptions = prescriptionService.getPrescriptionsByDoctor(doctorId);
            return ResponseEntity.ok(prescriptions);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<PrescriptionDTO> getPrescription(
            @PathVariable Long id,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            User user = userService.getUserByEmail(email);
            if (user == null) {
                return ResponseEntity.badRequest().build();
            }
            
            String role = user.getRole().name();
            Long userId = user.getId();

            PrescriptionDTO prescription = prescriptionService.getPrescriptionById(id, role, userId);
            return ResponseEntity.ok(prescription);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}



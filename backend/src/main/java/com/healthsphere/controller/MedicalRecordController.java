package com.healthsphere.controller;

import com.healthsphere.dto.MedicalRecordDTO;
import com.healthsphere.model.User;
import com.healthsphere.service.MedicalRecordService;
import com.healthsphere.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/medical-records")
@CrossOrigin(origins = "*")
public class MedicalRecordController {
    @Autowired
    private MedicalRecordService medicalRecordService;

    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<MedicalRecordDTO> createMedicalRecord(
            @RequestBody MedicalRecordDTO dto,
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

            MedicalRecordDTO created = medicalRecordService.createMedicalRecord(dto, doctorId);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<MedicalRecordDTO>> getRecordsByPatient(@PathVariable Long patientId) {
        try {
            List<MedicalRecordDTO> records = medicalRecordService.getMedicalRecordsByPatient(patientId);
            return ResponseEntity.ok(records);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<MedicalRecordDTO>> getRecordsByDoctor(@PathVariable Long doctorId) {
        try {
            List<MedicalRecordDTO> records = medicalRecordService.getMedicalRecordsByDoctor(doctorId);
            return ResponseEntity.ok(records);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<MedicalRecordDTO> getMedicalRecord(
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

            MedicalRecordDTO record = medicalRecordService.getMedicalRecordById(id, role, userId);
            return ResponseEntity.ok(record);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<MedicalRecordDTO> updateMedicalRecord(
            @PathVariable Long id,
            @RequestBody MedicalRecordDTO dto,
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

            MedicalRecordDTO updated = medicalRecordService.updateMedicalRecord(id, dto, doctorId);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}


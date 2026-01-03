package com.healthsphere.service;

import com.healthsphere.dto.PrescriptionDTO;
import com.healthsphere.model.Doctor;
import com.healthsphere.model.NotificationType;
import com.healthsphere.model.Patient;
import com.healthsphere.model.Prescription;
import com.healthsphere.repository.DoctorRepository;
import com.healthsphere.repository.PatientRepository;
import com.healthsphere.repository.PrescriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PrescriptionService {
    @Autowired
    private PrescriptionRepository prescriptionRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private NotificationService notificationService;

    @Transactional
    public PrescriptionDTO createPrescription(PrescriptionDTO dto, Long doctorId) {
        Patient patient = patientRepository.findById(dto.getPatientId())
            .orElseThrow(() -> new RuntimeException("Patient not found"));
        
        Doctor doctor = doctorRepository.findById(doctorId)
            .orElseThrow(() -> new RuntimeException("Doctor not found"));

        Prescription prescription = new Prescription();
        prescription.setPatient(patient);
        prescription.setDoctor(doctor);
        prescription.setPrescriptionDate(dto.getPrescriptionDate() != null ? dto.getPrescriptionDate() : LocalDateTime.now());
        prescription.setMedicines(dto.getMedicines());
        prescription.setDosage(dto.getDosage());
        prescription.setInstructions(dto.getInstructions());
        prescription.setValidUntil(dto.getValidUntil());
        prescription.setNotes(dto.getNotes());

        prescription = prescriptionRepository.save(prescription);
        
        // Send notification to patient
        notificationService.createNotification(
            patient.getUser().getId(),
            "New Prescription",
            "Dr. " + doctor.getFirstName() + " " + doctor.getLastName() + 
            " has prescribed: " + prescription.getMedicines(),
            NotificationType.PRESCRIPTION_CREATED,
            prescription.getId(),
            "PRESCRIPTION"
        );
        
        return convertToDTO(prescription);
    }

    public List<PrescriptionDTO> getPrescriptionsByPatient(Long patientId) {
        List<Prescription> prescriptions = prescriptionRepository.findByPatientIdOrderByPrescriptionDateDesc(patientId);
        return prescriptions.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public List<PrescriptionDTO> getPrescriptionsByDoctor(Long doctorId) {
        List<Prescription> prescriptions = prescriptionRepository.findByDoctorIdOrderByPrescriptionDateDesc(doctorId);
        return prescriptions.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public PrescriptionDTO getPrescriptionById(Long id, String userRole, Long userId) {
        Prescription prescription = prescriptionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Prescription not found"));

        // Security check
        if (userRole.equals("PATIENT")) {
            if (!prescription.getPatient().getUser().getId().equals(userId)) {
                throw new RuntimeException("Access denied");
            }
        } else if (userRole.equals("DOCTOR")) {
            if (!prescription.getDoctor().getUser().getId().equals(userId)) {
                throw new RuntimeException("Access denied");
            }
        }

        return convertToDTO(prescription);
    }

    private PrescriptionDTO convertToDTO(Prescription prescription) {
        PrescriptionDTO dto = new PrescriptionDTO();
        dto.setId(prescription.getId());
        dto.setPatientId(prescription.getPatient().getId());
        dto.setDoctorId(prescription.getDoctor().getId());
        dto.setPatientName(prescription.getPatient().getFirstName() + " " + prescription.getPatient().getLastName());
        dto.setDoctorName(prescription.getDoctor().getFirstName() + " " + prescription.getDoctor().getLastName());
        dto.setPrescriptionDate(prescription.getPrescriptionDate());
        dto.setMedicines(prescription.getMedicines());
        dto.setDosage(prescription.getDosage());
        dto.setInstructions(prescription.getInstructions());
        dto.setValidUntil(prescription.getValidUntil());
        dto.setNotes(prescription.getNotes());
        return dto;
    }
}


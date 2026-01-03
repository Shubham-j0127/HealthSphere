package com.healthsphere.service;

import com.healthsphere.dto.MedicalRecordDTO;
import com.healthsphere.model.Doctor;
import com.healthsphere.model.MedicalRecord;
import com.healthsphere.model.Patient;
import com.healthsphere.repository.DoctorRepository;
import com.healthsphere.repository.MedicalRecordRepository;
import com.healthsphere.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MedicalRecordService {
    @Autowired
    private MedicalRecordRepository medicalRecordRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Transactional
    public MedicalRecordDTO createMedicalRecord(MedicalRecordDTO dto, Long doctorId) {
        Patient patient = patientRepository.findById(dto.getPatientId())
            .orElseThrow(() -> new RuntimeException("Patient not found"));
        
        Doctor doctor = doctorRepository.findById(doctorId)
            .orElseThrow(() -> new RuntimeException("Doctor not found"));

        MedicalRecord record = new MedicalRecord();
        record.setPatient(patient);
        record.setDoctor(doctor);
        record.setRecordDate(dto.getRecordDate() != null ? dto.getRecordDate() : LocalDateTime.now());
        record.setDiagnosis(dto.getDiagnosis());
        record.setMedications(dto.getMedications());
        record.setLabResults(dto.getLabResults());
        record.setNotes(dto.getNotes());
        record.setSymptoms(dto.getSymptoms());
        record.setTreatmentPlan(dto.getTreatmentPlan());

        record = medicalRecordRepository.save(record);
        return convertToDTO(record);
    }

    public List<MedicalRecordDTO> getMedicalRecordsByPatient(Long patientId) {
        List<MedicalRecord> records = medicalRecordRepository.findByPatientIdOrderByRecordDateDesc(patientId);
        return records.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public List<MedicalRecordDTO> getMedicalRecordsByDoctor(Long doctorId) {
        List<MedicalRecord> records = medicalRecordRepository.findByDoctorIdOrderByRecordDateDesc(doctorId);
        return records.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public MedicalRecordDTO getMedicalRecordById(Long id, String userRole, Long userId) {
        MedicalRecord record = medicalRecordRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Medical record not found"));

        // Security check: Patient can only see their own records, Doctor can see records they created
        if (userRole.equals("PATIENT")) {
            if (!record.getPatient().getUser().getId().equals(userId)) {
                throw new RuntimeException("Access denied");
            }
        } else if (userRole.equals("DOCTOR")) {
            if (!record.getDoctor().getUser().getId().equals(userId)) {
                throw new RuntimeException("Access denied");
            }
        }

        return convertToDTO(record);
    }

    @Transactional
    public MedicalRecordDTO updateMedicalRecord(Long id, MedicalRecordDTO dto, Long doctorId) {
        MedicalRecord record = medicalRecordRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Medical record not found"));

        // Only the doctor who created it can update
        if (!record.getDoctor().getId().equals(doctorId)) {
            throw new RuntimeException("Only the doctor who created this record can update it");
        }

        if (dto.getDiagnosis() != null) record.setDiagnosis(dto.getDiagnosis());
        if (dto.getMedications() != null) record.setMedications(dto.getMedications());
        if (dto.getLabResults() != null) record.setLabResults(dto.getLabResults());
        if (dto.getNotes() != null) record.setNotes(dto.getNotes());
        if (dto.getSymptoms() != null) record.setSymptoms(dto.getSymptoms());
        if (dto.getTreatmentPlan() != null) record.setTreatmentPlan(dto.getTreatmentPlan());

        record = medicalRecordRepository.save(record);
        return convertToDTO(record);
    }

    private MedicalRecordDTO convertToDTO(MedicalRecord record) {
        MedicalRecordDTO dto = new MedicalRecordDTO();
        dto.setId(record.getId());
        dto.setPatientId(record.getPatient().getId());
        dto.setDoctorId(record.getDoctor().getId());
        dto.setPatientName(record.getPatient().getFirstName() + " " + record.getPatient().getLastName());
        dto.setDoctorName(record.getDoctor().getFirstName() + " " + record.getDoctor().getLastName());
        dto.setRecordDate(record.getRecordDate());
        dto.setDiagnosis(record.getDiagnosis());
        dto.setMedications(record.getMedications());
        dto.setLabResults(record.getLabResults());
        dto.setNotes(record.getNotes());
        dto.setSymptoms(record.getSymptoms());
        dto.setTreatmentPlan(record.getTreatmentPlan());
        return dto;
    }
}


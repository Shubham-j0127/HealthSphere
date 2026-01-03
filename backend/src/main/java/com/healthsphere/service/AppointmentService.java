package com.healthsphere.service;

import com.healthsphere.dto.AppointmentDTO;
import com.healthsphere.model.Appointment;
import com.healthsphere.model.AppointmentStatus;
import com.healthsphere.model.Doctor;
import com.healthsphere.model.NotificationType;
import com.healthsphere.model.Patient;
import com.healthsphere.repository.AppointmentRepository;
import com.healthsphere.repository.DoctorRepository;
import com.healthsphere.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AppointmentService {
    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private NotificationService notificationService;

    @Transactional
    public AppointmentDTO bookAppointment(AppointmentDTO dto, Long patientId) {
        Patient patient = patientRepository.findById(patientId)
            .orElseThrow(() -> new RuntimeException("Patient not found"));
        
        Doctor doctor = doctorRepository.findById(dto.getDoctorId())
            .orElseThrow(() -> new RuntimeException("Doctor not found"));

        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setAppointmentDate(dto.getAppointmentDate());
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        appointment.setReason(dto.getReason());
        appointment.setNotes(dto.getNotes());

        appointment = appointmentRepository.save(appointment);
        
        // Send notification to patient
        notificationService.createNotification(
            patient.getUser().getId(),
            "Appointment Scheduled",
            "Your appointment with Dr. " + doctor.getFirstName() + " " + doctor.getLastName() + 
            " has been scheduled for " + appointment.getAppointmentDate().toLocalDate() + 
            " at " + appointment.getAppointmentDate().toLocalTime(),
            NotificationType.APPOINTMENT_REMINDER,
            appointment.getId(),
            "APPOINTMENT"
        );
        
        return convertToDTO(appointment);
    }

    public List<AppointmentDTO> getAppointmentsByPatient(Long patientId) {
        List<Appointment> appointments = appointmentRepository.findByPatientIdOrderByAppointmentDateDesc(patientId);
        return appointments.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public List<AppointmentDTO> getAppointmentsByDoctor(Long doctorId) {
        List<Appointment> appointments = appointmentRepository.findByDoctorIdOrderByAppointmentDateDesc(doctorId);
        return appointments.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public AppointmentDTO getAppointmentById(Long id, String userRole, Long userId) {
        Appointment appointment = appointmentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Appointment not found"));

        // Security check
        if (userRole.equals("PATIENT")) {
            if (!appointment.getPatient().getUser().getId().equals(userId)) {
                throw new RuntimeException("Access denied");
            }
        } else if (userRole.equals("DOCTOR")) {
            if (!appointment.getDoctor().getUser().getId().equals(userId)) {
                throw new RuntimeException("Access denied");
            }
        }

        return convertToDTO(appointment);
    }

    @Transactional
    public AppointmentDTO updateAppointmentStatus(Long id, AppointmentStatus status, Long userId, String userRole) {
        Appointment appointment = appointmentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Appointment not found"));

        // Both patient and doctor can update status, but with restrictions
        if (userRole.equals("PATIENT")) {
            if (!appointment.getPatient().getUser().getId().equals(userId)) {
                throw new RuntimeException("Access denied");
            }
            // Patients can only cancel
            if (status != AppointmentStatus.CANCELLED) {
                throw new RuntimeException("Patients can only cancel appointments");
            }
        } else if (userRole.equals("DOCTOR")) {
            if (!appointment.getDoctor().getUser().getId().equals(userId)) {
                throw new RuntimeException("Access denied");
            }
        }

        appointment.setStatus(status);
        appointment = appointmentRepository.save(appointment);
        
        // Send notification based on status
        if (status == AppointmentStatus.CONFIRMED) {
            notificationService.createNotification(
                appointment.getPatient().getUser().getId(),
                "Appointment Confirmed",
                "Your appointment with Dr. " + appointment.getDoctor().getFirstName() + " " + 
                appointment.getDoctor().getLastName() + " has been confirmed for " + 
                appointment.getAppointmentDate().toLocalDate() + " at " + 
                appointment.getAppointmentDate().toLocalTime(),
                NotificationType.APPOINTMENT_CONFIRMED,
                appointment.getId(),
                "APPOINTMENT"
            );
        } else if (status == AppointmentStatus.CANCELLED) {
            notificationService.createNotification(
                appointment.getPatient().getUser().getId(),
                "Appointment Cancelled",
                "Your appointment with Dr. " + appointment.getDoctor().getFirstName() + " " + 
                appointment.getDoctor().getLastName() + " scheduled for " + 
                appointment.getAppointmentDate().toLocalDate() + " has been cancelled",
                NotificationType.APPOINTMENT_CANCELLED,
                appointment.getId(),
                "APPOINTMENT"
            );
        }
        
        return convertToDTO(appointment);
    }

    @Transactional
    public AppointmentDTO updateAppointment(Long id, AppointmentDTO dto, Long userId, String userRole) {
        Appointment appointment = appointmentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Appointment not found"));

        // Only doctor can update appointment details
        if (!userRole.equals("DOCTOR") || !appointment.getDoctor().getUser().getId().equals(userId)) {
            throw new RuntimeException("Only the assigned doctor can update appointment details");
        }

        if (dto.getAppointmentDate() != null) appointment.setAppointmentDate(dto.getAppointmentDate());
        if (dto.getReason() != null) appointment.setReason(dto.getReason());
        if (dto.getNotes() != null) appointment.setNotes(dto.getNotes());
        if (dto.getStatus() != null) appointment.setStatus(dto.getStatus());

        appointment = appointmentRepository.save(appointment);
        return convertToDTO(appointment);
    }

    public List<AppointmentDTO> getAvailableDoctors() {
        // This would typically return a list of doctors, but for now we'll use DoctorRepository
        // In a real system, you'd want to check availability
        return List.of();
    }

    private AppointmentDTO convertToDTO(Appointment appointment) {
        AppointmentDTO dto = new AppointmentDTO();
        dto.setId(appointment.getId());
        dto.setPatientId(appointment.getPatient().getId());
        dto.setDoctorId(appointment.getDoctor().getId());
        dto.setPatientName(appointment.getPatient().getFirstName() + " " + appointment.getPatient().getLastName());
        dto.setDoctorName(appointment.getDoctor().getFirstName() + " " + appointment.getDoctor().getLastName());
        dto.setAppointmentDate(appointment.getAppointmentDate());
        dto.setStatus(appointment.getStatus());
        dto.setReason(appointment.getReason());
        dto.setNotes(appointment.getNotes());
        return dto;
    }
}


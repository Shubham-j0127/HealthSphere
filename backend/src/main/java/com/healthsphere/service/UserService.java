package com.healthsphere.service;

import com.healthsphere.model.Doctor;
import com.healthsphere.model.Patient;
import com.healthsphere.model.User;
import com.healthsphere.repository.DoctorRepository;
import com.healthsphere.repository.PatientRepository;
import com.healthsphere.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    public Long getPatientIdByUserId(Long userId) {
        Patient patient = patientRepository.findByUserId(userId);
        return patient != null ? patient.getId() : null;
    }

    public Long getDoctorIdByUserId(Long userId) {
        Doctor doctor = doctorRepository.findByUserId(userId);
        return doctor != null ? doctor.getId() : null;
    }

    public List<Map<String, Object>> getAllDoctors() {
        List<Doctor> doctors = doctorRepository.findAll();
        return doctors.stream().map(doctor -> {
            Map<String, Object> doctorInfo = new HashMap<>();
            doctorInfo.put("id", doctor.getId());
            doctorInfo.put("firstName", doctor.getFirstName());
            doctorInfo.put("lastName", doctor.getLastName());
            doctorInfo.put("specialization", doctor.getSpecialization());
            doctorInfo.put("name", doctor.getFirstName() + " " + doctor.getLastName());
            return doctorInfo;
        }).collect(Collectors.toList());
    }

    public com.healthsphere.dto.UserInfoDTO getUserInfo(String email) {
        User user = getUserByEmail(email);
        if (user == null) {
            return null;
        }

        com.healthsphere.dto.UserInfoDTO dto = new com.healthsphere.dto.UserInfoDTO();
        dto.setUserId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole().name());

        if (user.getRole() == com.healthsphere.model.Role.PATIENT) {
            Patient patient = patientRepository.findByUserId(user.getId());
            if (patient != null) {
                dto.setPatientId(patient.getId());
                dto.setFirstName(patient.getFirstName());
                dto.setLastName(patient.getLastName());
            }
        } else if (user.getRole() == com.healthsphere.model.Role.DOCTOR) {
            Doctor doctor = doctorRepository.findByUserId(user.getId());
            if (doctor != null) {
                dto.setDoctorId(doctor.getId());
                dto.setFirstName(doctor.getFirstName());
                dto.setLastName(doctor.getLastName());
            }
        }

        return dto;
    }
}


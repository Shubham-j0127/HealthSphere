package com.healthsphere.service;

import com.healthsphere.dto.AuthResponse;
import com.healthsphere.dto.LoginRequest;
import com.healthsphere.dto.RegisterRequest;
import com.healthsphere.model.Doctor;
import com.healthsphere.model.Patient;
import com.healthsphere.model.Role;
import com.healthsphere.model.User;
import com.healthsphere.repository.DoctorRepository;
import com.healthsphere.repository.PatientRepository;
import com.healthsphere.repository.UserRepository;
import com.healthsphere.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user = userRepository.save(user);

        if (request.getRole() == Role.PATIENT) {
            Patient patient = new Patient(user, request.getFirstName(), request.getLastName());
            patient.setPhoneNumber(request.getPhoneNumber());
            patientRepository.save(patient);
            user.setPatient(patient);
        } else if (request.getRole() == Role.DOCTOR) {
            Doctor doctor = new Doctor(user, request.getFirstName(), request.getLastName(), request.getSpecialization());
            doctor.setPhoneNumber(request.getPhoneNumber());
            doctorRepository.save(doctor);
            user.setDoctor(doctor);
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        return new AuthResponse(token, user.getEmail(), user.getRole().name(), user.getId());
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new RuntimeException("User not found"));

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        return new AuthResponse(token, user.getEmail(), user.getRole().name(), user.getId());
    }
}



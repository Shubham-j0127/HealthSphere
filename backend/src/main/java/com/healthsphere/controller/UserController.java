package com.healthsphere.controller;

import com.healthsphere.dto.UserInfoDTO;
import com.healthsphere.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserInfoDTO> getCurrentUser(Authentication authentication) {
        try {
            String email = authentication.getName();
            UserInfoDTO userInfo = userService.getUserInfo(email);
            if (userInfo == null) {
                return ResponseEntity.badRequest().build();
            }
            return ResponseEntity.ok(userInfo);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/doctors")
    public ResponseEntity<List<Map<String, Object>>> getAllDoctors() {
        try {
            List<Map<String, Object>> doctors = userService.getAllDoctors();
            return ResponseEntity.ok(doctors);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}


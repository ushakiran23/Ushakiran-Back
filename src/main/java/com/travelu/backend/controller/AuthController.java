package com.travelu.backend.controller;

import com.travelu.backend.dto.AuthResponse;
import com.travelu.backend.dto.LoginRequest;
import com.travelu.backend.dto.RegisterRequest;
import com.travelu.backend.dto.ForgotPasswordRequest;
import com.travelu.backend.entity.User;
import com.travelu.backend.entity.PasswordResetToken;
import com.travelu.backend.security.JwtUtils;
import com.travelu.backend.service.UserService;
import com.travelu.backend.repository.PasswordResetTokenRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserService userService;
    private final JwtUtils jwtUtils;
    private final PasswordResetTokenRepository tokenRepository;

    public AuthController(UserService userService,
                          JwtUtils jwtUtils,
                          PasswordResetTokenRepository tokenRepository) {
        this.userService = userService;
        this.jwtUtils = jwtUtils;
        this.tokenRepository = tokenRepository;
    }

    // ---------------- REGISTER ----------------
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            User user = userService.register(request.getEmail(), request.getPassword());
            if (user == null) {
                return ResponseEntity
                        .status(409)
                        .body("Email already registered");
            }

            String token = jwtUtils.generateToken(user.getEmail());
            return ResponseEntity.ok(new AuthResponse(token));

        } catch (Exception e) {
            return ResponseEntity
                    .status(500)
                    .body("Error in register: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
    }

    // ---------------- LOGIN ----------------
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            User user = userService.login(request.getEmail(), request.getPassword());
            if (user == null) {
                return ResponseEntity
                        .status(401)
                        .body("Invalid email or password");
            }

            String token = jwtUtils.generateToken(user.getEmail());
            return ResponseEntity.ok(new AuthResponse(token));

        } catch (Exception e) {
            return ResponseEntity
                    .status(500)
                    .body("Error in login: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
    }

    // ---------------- FORGOT PASSWORD ----------------
    @PostMapping("/forgot")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        try {
            String email = request.getEmail();
            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity
                        .badRequest()
                        .body("Email is required");
            }

            boolean exists = userService.emailExists(email);
            if (!exists) {
                return ResponseEntity
                        .status(404)
                        .body("No account found for this email");
            }

            // Generate reset token and save to DB
            String token = UUID.randomUUID().toString();
            LocalDateTime expiry = LocalDateTime.now().plusMinutes(10); // valid for 10 minutes

            PasswordResetToken resetToken = new PasswordResetToken(email, token, expiry);
            tokenRepository.save(resetToken);

            // For now, return reset link in response (later we send email)
            String resetLink = "http://localhost:5173/reset-password?token=" + token;

            return ResponseEntity.ok("Reset link: " + resetLink);

        } catch (Exception e) {
            return ResponseEntity
                    .status(500)
                    .body("Error in forgot password: " + e.getClass().getSimpleName());
        }
    }

    // ---------------- HEALTH CHECK ----------------
    @GetMapping("/ping")
    public String ping() {
        return "Travel-U backend is running!";
    }

    // ---------------- CURRENT USER ----------------
    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof User)) {
            return ResponseEntity.status(401).body("Not authenticated");
        }

        User user = (User) authentication.getPrincipal();
        String email = user.getEmail();

        return ResponseEntity.ok("Logged in as: " + email);
    }
}

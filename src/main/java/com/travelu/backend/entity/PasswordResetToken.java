package com.travelu.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "password_reset_tokens")
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    @Column(unique = true)
    private String token;

    private LocalDateTime expiryTime;

    public PasswordResetToken() {}

    public PasswordResetToken(String email, String token, LocalDateTime expiryTime) {
        this.email = email;
        this.token = token;
        this.expiryTime = expiryTime;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getToken() {
        return token;
    }

    public LocalDateTime getExpiryTime() {
        return expiryTime;
    }
}

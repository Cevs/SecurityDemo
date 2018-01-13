package com.example.SecurityDemo.repositories;

import com.example.SecurityDemo.domain.PasswordResetToken;
import org.passay.PasswordValidator;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    PasswordResetToken findByToken(String token);
    PasswordResetToken findById(long id);
}

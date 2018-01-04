package com.example.SecurityDemo.repositories;

import com.example.SecurityDemo.domain.User;
import com.example.SecurityDemo.domain.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {

    VerificationToken findByToken(String token);
    VerificationToken findByUser(User user);
}

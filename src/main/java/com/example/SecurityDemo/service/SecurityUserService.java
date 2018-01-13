package com.example.SecurityDemo.service;


import com.example.SecurityDemo.domain.PasswordResetToken;
import com.example.SecurityDemo.domain.User;
import com.example.SecurityDemo.repositories.PasswordResetTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Calendar;

@Service
@Transactional
public class SecurityUserService implements  ISecurityUserService{

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Override
    public String validatePasswordResetToken(long id, String token) {
        PasswordResetToken passwordToken = passwordResetTokenRepository.findByToken(token);
        if((passwordToken==null) || (passwordToken.getUser().getId() != id)){
            return "invalidToken";
        }

        final LocalDateTime timeNow = LocalDateTime.now();
        if((passwordToken.getExpiryDate().isBefore(timeNow))){
            return "expired";
        }

        final User user = passwordToken.getUser();
        final Authentication auth = new UsernamePasswordAuthenticationToken(user, null,
                Arrays.asList(new SimpleGrantedAuthority("CHANGE_PASSWORD_PRIVILEGE")));
        SecurityContextHolder.getContext().setAuthentication(auth);
        return null;
    }
}

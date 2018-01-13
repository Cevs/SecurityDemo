package com.example.SecurityDemo.service;

import org.springframework.stereotype.Service;

public interface ISecurityUserService {
    String validatePasswordResetToken(long id, String token);
}

package com.example.SecurityDemo.service;

import com.example.SecurityDemo.Dto.UserDto;
import com.example.SecurityDemo.UserAlreadyExistException;
import com.example.SecurityDemo.domain.User;
import com.example.SecurityDemo.domain.VerificationToken;

import java.time.LocalDateTime;
import java.util.Date;

public interface IUserService {
    User registerNewUserAccount(UserDto accountDto)
            throws UserAlreadyExistException;

    User getUser(String verificationToken);

    boolean checkIfExists(String username);

    void saveRegisteredUser(User user);

    void createVerificationToken(User user, String token, LocalDateTime date);

    VerificationToken getVerificationToken(String token);

    VerificationToken generateNewVerificationToken(String existingToken);

    User findUserByEmail(String userEmail);

    void createPasswordResetTokenForUser(User user, String token);

    void changePassword(User user, String newPassword);
}

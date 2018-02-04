package com.example.SecurityDemo.service;

import com.example.SecurityDemo.Dto.UserDto;
import com.example.SecurityDemo.exceptions.UserAlreadyExistException;
import com.example.SecurityDemo.domain.PasswordResetToken;
import com.example.SecurityDemo.domain.Role;
import com.example.SecurityDemo.domain.User;
import com.example.SecurityDemo.domain.VerificationToken;
import com.example.SecurityDemo.repositories.PasswordResetTokenRepository;
import com.example.SecurityDemo.repositories.RoleRepository;
import com.example.SecurityDemo.repositories.UserRepository;
import com.example.SecurityDemo.repositories.VerificationTokenRepository;
import org.jboss.aerogear.security.otp.api.Base32;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
public class UserService implements IUserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    VerificationTokenRepository tokenRepository;
    @Autowired
    PasswordResetTokenRepository passwordResetTokenRepository;
    @Autowired
    PasswordEncoder passwordEncoder;

    public static final String TOKEN_INVALID = "invalidToken";
    public static final String TOKEN_EXPIRED = "expired";
    public static final String TOKEN_VALID = "valid";

    public static String QR_PREFIX ="https://chart.googleapis.com/chart?chs=200x200&chld=M%%7C0&cht=qr&chl=";
    public static String APP_NAME = "SecurityDemo";
    @Override
    public User registerNewUserAccount(UserDto accountDto) throws UserAlreadyExistException {

        if(emailExist(accountDto.getEmail())){
            throw new UserAlreadyExistException("There is an account with that email address: "
            + accountDto.getEmail());
        }

        User user = new User();
        user.setFirstName(accountDto.getFirstName());
        user.setLastName(accountDto.getLastName());
        user.setPassword(passwordEncoder.encode(accountDto.getPassword()));
        user.setEmail(accountDto.getEmail());
        user.setRoles(getRoles());
        user.setEnabled(false);
        user.setUsing2FA(accountDto.isUsing2fa());

        return userRepository.save(user);
    }

    @Override
    public User getUser(String verificationToken) {
        User user = tokenRepository.findByToken(verificationToken).getUser();
        return user;
    }

    @Override
    public boolean checkIfExists(String email) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if(optionalUser.isPresent()){
            return true;
        }
        return false;
    }

    @Override
    public VerificationToken getVerificationToken(String token) {
        return  tokenRepository.findByToken(token);
    }

    @Override
    public VerificationToken generateNewVerificationToken(String existingToken) {
        VerificationToken verificationToken = tokenRepository.findByToken(existingToken);
        verificationToken.updateToke(UUID.randomUUID().toString());
        verificationToken.setExpiryDate(LocalDateTime.now());
        verificationToken = tokenRepository.save(verificationToken);
        return verificationToken;
    }

    @Override
    public User findUserByEmail(String userEmail) {
        return userRepository.findByEmail(userEmail).get();
    }

    @Override
    public void createPasswordResetTokenForUser(User user, String token) {
        PasswordResetToken oldToken = passwordResetTokenRepository.findById(user.getId());
        if(oldToken!= null){
            passwordResetTokenRepository.delete(oldToken);
        }
        PasswordResetToken myToken = new PasswordResetToken(token, user);
        passwordResetTokenRepository.save(myToken);
    }

    /*
        Called when user forget password and want to reset it
     */
    @Override
    public void changePassword(User user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        passwordResetTokenRepository.delete(user.getId());
    }

    /*
        Called when user changing password from within application.
    */
    public void updatePassword(User user, String newPassword){
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    public String validateVerificationToken(String token) {
        final VerificationToken verificationToken = tokenRepository.findByToken(token);
        if(verificationToken == null){
            return TOKEN_INVALID;
        }

        final User user = verificationToken.getUser();
        final LocalDateTime time = LocalDateTime.now();
        if(time.isAfter(verificationToken.getExpiryDate())){
            tokenRepository.delete(verificationToken);
            return TOKEN_EXPIRED;
        }

        user.setEnabled(true);
        userRepository.save(user);
        return TOKEN_VALID;
    }

    @Override
    public String generateQRUrl(User user){
        try{
            return QR_PREFIX + URLEncoder.encode(String.format(
                    "otpauth://totp/%s:%s?secret=%s&issuer=%s",
                    APP_NAME, user.getEmail(), user.getSecret(), APP_NAME),
                    "UTF-8");
        }catch (UnsupportedEncodingException E){
            return null;
        }


    }

    @Override
    public User update2FA(boolean use2FA) {
        Authentication curAuth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) curAuth.getPrincipal();
        currentUser.setUsing2FA(use2FA);
        currentUser = userRepository.save(currentUser);
        Authentication auth = new UsernamePasswordAuthenticationToken(currentUser, currentUser.getPassword(), curAuth.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
        return currentUser;
    }

    @Override
    public User generateNewQRUrl() {
        Authentication curAuth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) curAuth.getPrincipal();
        currentUser.setSecret(Base32.random());
        currentUser = userRepository.save(currentUser);
        Authentication auth = new UsernamePasswordAuthenticationToken(currentUser, currentUser.getPassword(), curAuth.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
        return currentUser;
    }

    @Override
    public void updateUser(User user) {
        userRepository.save(user);
    }

    @Override
    public void createVerificationToken(User user, String token, LocalDateTime date) {
        VerificationToken myToken = new VerificationToken(token, user, date);
        tokenRepository.save(myToken);
    }

    private Set<Role> getRoles(){
        Set<Role> roles = new HashSet<Role>();
        roles.add(roleRepository.findByType("USER"));
        return roles;
    }

    private boolean emailExist(String email) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if(optionalUser.isPresent()){
            return true;
        }
        return  false;
    }
}

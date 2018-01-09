package com.example.SecurityDemo.service;

import com.example.SecurityDemo.Dto.UserDto;
import com.example.SecurityDemo.domain.Role;
import com.example.SecurityDemo.domain.User;
import com.example.SecurityDemo.domain.VerificationToken;
import com.example.SecurityDemo.repositories.RoleRepository;
import com.example.SecurityDemo.repositories.UserRepository;
import com.example.SecurityDemo.repositories.VerificationTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
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

    @Override
    public User registerNewUserAccount(UserDto accountDto) throws EmailExistsException {

        if(emailExist(accountDto.getEmail())){
            throw new EmailExistsException("There is an account with that email address: "
            + accountDto.getEmail());
        }

        User user = new User();
        user.setFirstName(accountDto.getFirstName());
        user.setLastName(accountDto.getLastName());
        user.setPassword(accountDto.getPassword());
        user.setEmail(accountDto.getEmail());
        user.setRoles(getRoles());

        return userRepository.save(user);
    }

    @Override
    public User getUser(String verificationToken) {
        User user = tokenRepository.findByToken(verificationToken).getUser();
        return user;
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
    public void saveRegisteredUser(User user) {
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

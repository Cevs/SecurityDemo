package com.example.SecurityDemo.config;

import com.example.SecurityDemo.CustomWebAuthenticationDetails;
import com.example.SecurityDemo.domain.User;
import com.example.SecurityDemo.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import java.util.Optional;

public class CustomAuthenticationProvider extends DaoAuthenticationProvider{
    @Autowired
    private UserRepository userRepository;


    @Override
    public Authentication authenticate(Authentication auth) throws AuthenticationException {
        //final String verificationCode = ((CustomWebAuthenticationDetails)auth.getDetails()).getVerificationCode();
        final Optional<User> optionalUser = userRepository.findByEmail(auth.getName());
        if(!optionalUser.isPresent()){
            throw new BadCredentialsException("Invalid username or password");
        }

        //TO DO: Two factor auth

        final Authentication result = super.authenticate(auth);
        return new UsernamePasswordAuthenticationToken(optionalUser.get(), result.getCredentials(), result.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}

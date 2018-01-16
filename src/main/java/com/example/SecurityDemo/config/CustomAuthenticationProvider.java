package com.example.SecurityDemo.config;

import com.example.SecurityDemo.domain.CustomWebAuthenticationDetails;
import com.example.SecurityDemo.domain.User;
import com.example.SecurityDemo.repositories.UserRepository;
import org.jboss.aerogear.security.otp.Totp;
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
        final String verificationCode = ((CustomWebAuthenticationDetails)auth.getDetails()).getVerificationCode();
        final Optional<User> optionalUser = userRepository.findByEmail(auth.getName());
        if(!optionalUser.isPresent()){
            throw new BadCredentialsException("Invalid username or password");
        }
        User user = optionalUser.get();
        if(user.isUsing2FA()){
            final Totp totp = new Totp(user.getSecret());
            if(!isValidLong(verificationCode) || !totp.verify(verificationCode)){
                throw new BadCredentialsException("Invalid verification code");
            }
        }

        final Authentication result = super.authenticate(auth);
        return new UsernamePasswordAuthenticationToken(optionalUser.get(), result.getCredentials(), result.getAuthorities());
    }

    private boolean isValidLong(String verificationCode) {
        try{
            Long.parseLong(verificationCode);
        }catch (final NumberFormatException e){
            return false;
        }
        return true;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}

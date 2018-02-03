package com.example.SecurityDemo.service;

import com.example.SecurityDemo.LoginAttemptService;
import com.example.SecurityDemo.domain.CustomUserDetails;
import com.example.SecurityDemo.domain.User;
import com.example.SecurityDemo.exceptions.IpBanException;
import com.example.SecurityDemo.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.util.Optional;

@Service

public class CustomUserDetailsService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private LoginAttemptService loginAttemptService;
    @Autowired
    private HttpServletRequest request;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException{
        String ip = getClientIP();
        if(loginAttemptService.isBlocked(ip)){
            throw new IpBanException("Ip is blocked");
        }
        Optional<User> optionalUser = userRepository.findByEmail(username);

        optionalUser.orElseThrow(()->new UsernameNotFoundException("Username not found"));
        return optionalUser.map(CustomUserDetails::new).get();
    }

    private final String getClientIP(){
        final String xfHeader = request.getHeader("X-Forwarded-For");
        if(xfHeader == null){
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }


}

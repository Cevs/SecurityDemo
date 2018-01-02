package com.example.SecurityDemo.service;

import com.example.SecurityDemo.domain.CustomUserDetails;
import com.example.SecurityDemo.domain.User;
import com.example.SecurityDemo.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> optionalUser = userRepository.findByEmail(username);

        optionalUser.orElseThrow(()->new UsernameNotFoundException("Username not found"));
        return optionalUser.map(CustomUserDetails::new).get();
    }
}

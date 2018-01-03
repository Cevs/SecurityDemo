package com.example.SecurityDemo.service;

import com.example.SecurityDemo.Dto.UserDto;
import com.example.SecurityDemo.domain.Role;
import com.example.SecurityDemo.domain.User;
import com.example.SecurityDemo.repositories.RoleRepository;
import com.example.SecurityDemo.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
public class UserService implements IUserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;

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

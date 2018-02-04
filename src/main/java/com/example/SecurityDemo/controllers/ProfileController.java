package com.example.SecurityDemo.controllers;


import com.example.SecurityDemo.domain.Role;
import com.example.SecurityDemo.domain.User;
import com.example.SecurityDemo.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Set;

@Controller
public class ProfileController {

    @Autowired
    UserService userService;

    private User user;

    @RequestMapping("/profile")
    public String showProfile(Model model){
        user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        model.addAttribute("firstName",user.getFirstName());
        model.addAttribute("lastName",user.getLastName());
        model.addAttribute("email",user.getEmail());
        Role role = (Role) user.getRoles().toArray()[0];
        model.addAttribute("role",role.getType());
        return "profile";
    }
}

package com.example.SecurityDemo.controllers;


import com.example.SecurityDemo.Dto.PasswordDto;
import com.example.SecurityDemo.GenericResponse;
import com.example.SecurityDemo.domain.Role;
import com.example.SecurityDemo.domain.User;
import com.example.SecurityDemo.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.Valid;
import java.util.Locale;

@Controller
public class ProfileController {

    @Autowired
    UserService userService;
    @Autowired
    private MessageSource messages;

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

    @RequestMapping(value = "/user/profile/savePassword", method = RequestMethod.POST)
    @ResponseBody
    public GenericResponse savePassword(Locale locale, @Valid PasswordDto passwordDto){
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        userService.updatePassword(user,passwordDto.getNewPassword());
        return new GenericResponse(messages.getMessage("message.resetPasswordSuc",null, locale));
    }
}

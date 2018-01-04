package com.example.SecurityDemo.controllers;

import com.example.SecurityDemo.Dto.UserDto;
import com.example.SecurityDemo.domain.User;
import com.example.SecurityDemo.domain.VerificationToken;
import com.example.SecurityDemo.events.OnRegistrationCompleteEvent;
import com.example.SecurityDemo.service.EmailExistsException;
import com.example.SecurityDemo.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;
import sun.misc.Request;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class RegistrationController {

    @Autowired
    IUserService userService;
    @Autowired
    ApplicationEventPublisher eventPublisher;
    @Autowired
    private MessageSource messages;

    @RequestMapping(value="/user/registration", method = RequestMethod.GET)
    public String showRegistrationForm(WebRequest request, Model model){
        UserDto userDto = new UserDto();
        model.addAttribute("user", userDto);
        return "registration";
    }

    @RequestMapping(value = "/user/registration", method = RequestMethod.POST)
    public ModelAndView registerUserAccount(@ModelAttribute("user") @Valid UserDto accountDto,
                                            BindingResult result, WebRequest request, Errors errors){
        User registered = new User();
        if(!result.hasErrors()){
            registered = createUserAccount(accountDto, result);
        }
        if(registered == null){
            result.rejectValue("email", "message.regError");
        }
        if(result.hasErrors()){
            replaceErrorsWithCustom(errors, result);
            return new ModelAndView("registration","user",accountDto);
        }
        try{
            String appUrl = request.getContextPath();
            eventPublisher.publishEvent(new OnRegistrationCompleteEvent
                    (registered, request.getLocale(), appUrl));
        }catch (Exception e){
            String s = e.getMessage();
            return new ModelAndView("emailError","user",accountDto);
        }
        return new ModelAndView("successRegister","user",accountDto);
    }

    @RequestMapping(value = "/registrationConfirm", method = RequestMethod.GET)
    public String confirmRegistration(WebRequest request, Model model, @RequestParam("token") String token){

        Locale locale = request.getLocale();
        VerificationToken verificationToken = userService.getVerificationToken(token);
        if(verificationToken == null){
            String message = messages.getMessage("auth.message.invalidToken", null, locale);
            model.addAttribute("message", message);
            return "redirect:/badUser.html?lang="+locale.getLanguage();
        }

        User user = verificationToken.getUser();
        LocalDateTime time = LocalDateTime.now();
        if(time.isAfter(verificationToken.getExpiryDate())){
            String messageValue = messages.getMessage("auth.message.expired", null, locale);
            model.addAttribute("message",messageValue);
            return "redirect:/badUser.html?lang="+locale.getLanguage();
        }

        user.setEnabled(true);
        userService.saveRegisteredUser(user);
        return "redirect:/index.html";
    }

    //Replace default errors with custom for field validation
    private void replaceErrorsWithCustom(Errors errors, BindingResult result) {
        List<String> customFieldErrors = Arrays.asList("firstName","email", "lastName", "password", "matchingPassword");
        ArrayList<ObjectError> errorList = new ArrayList<>(errors.getAllErrors());


        if(errors.hasGlobalErrors()){
            int i = errorList.size();
            errorList.remove(errorList.size()-1);
            result.rejectValue("matchingPassword","message.matchingPassword");

        }

        List<FieldError> fieldErrorList = errorList
                .stream()
                .map(e -> (FieldError) e)
                .collect(Collectors.toList());

        for(FieldError fe: fieldErrorList){
            String fieldName = fe.getField().toString();
            if(customFieldErrors.contains(fieldName)){
                result.rejectValue(fieldName,"message."+fieldName);
            }
        }
    }

    private User createUserAccount(UserDto accountDto, BindingResult result){
        User registered = null;
        try{
            registered = userService.registerNewUserAccount(accountDto);
        }catch (EmailExistsException e){
            return null;
        }

        return registered;
    }
}

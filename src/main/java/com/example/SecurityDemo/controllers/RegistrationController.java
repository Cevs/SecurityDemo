package com.example.SecurityDemo.controllers;

import com.example.SecurityDemo.Dto.UserDto;
import com.example.SecurityDemo.GenericResponse;
import com.example.SecurityDemo.domain.User;
import com.example.SecurityDemo.domain.VerificationToken;
import com.example.SecurityDemo.events.OnRegistrationCompleteEvent;
import com.example.SecurityDemo.service.EmailExistsException;
import com.example.SecurityDemo.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
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
    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    PasswordEncoder passwordEncoder;

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
    public String confirmRegistration(WebRequest request, ModelMap model, @RequestParam("token") String token,
                                      RedirectAttributes redirectAttributes){

        Locale locale = request.getLocale();
        VerificationToken verificationToken = userService.getVerificationToken(token);
        if(verificationToken == null){
            String message = messages.getMessage("auth.message.invalidToken", null, locale);
            model.addAttribute("message", message);
            return "redirect:/badUser";
        }

        User user = verificationToken.getUser();
        LocalDateTime time = LocalDateTime.now();
        if(time.isAfter(verificationToken.getExpiryDate())){
            String messageValue = messages.getMessage("auth.message.expired", null, locale);
            model.addAttribute("message",messageValue);
            model.addAttribute("expired",true);
            model.addAttribute("token",token);

            redirectAttributes.addFlashAttribute("model",model);
            return "redirect:/badUser";
        }

        user.setEnabled(true);
        userService.saveRegisteredUser(user);
        model.addAttribute("message",messages.getMessage("message.accountVerified",null,locale));
        return "redirect:/login";
    }

    @RequestMapping(value = "/user/resendRegistrationToken", method = RequestMethod.GET)
    @ResponseBody
    public GenericResponse resendRegistrationToken(
            HttpServletRequest request, @RequestParam("token") String existingToken) {

        VerificationToken newToken = userService.generateNewVerificationToken(existingToken);
        User user = userService.getUser(newToken.getToken());
        String appUrl =
                "http://"+request.getServerName()+ ":"+request.getServerPort()+ request.getContextPath();
        SimpleMailMessage email =
                constructResendVerificationTokenEmail(appUrl, request.getLocale(),newToken, user);
        mailSender.send(email);

        return new GenericResponse(
                messages.getMessage("message.resendToken",null, request.getLocale())
        );
    }

    @RequestMapping("/badUser")
    public ModelAndView BadUser(@ModelAttribute("model") ModelMap model){
        return new ModelAndView("badUser",model);
    }

    @RequestMapping(value = "/user/exist", method = RequestMethod.POST, params="email")
    @ResponseBody
    public boolean checkUser(HttpServletRequest request, @RequestParam String email){
          return userService.checkIfExists(email);
    }

    private SimpleMailMessage constructResendVerificationTokenEmail
            (String appUrl, Locale locale, VerificationToken newToken, User user) {

        String confirmationUrl =
                appUrl + "/registrationConfirm.html?token="+newToken.getToken();
        String message = messages.getMessage("message.resendToken",null,locale);
        SimpleMailMessage email = new SimpleMailMessage();
        email.setSubject("Resend Registration Token");
        email.setTo(user.getEmail());
        email.setText(message+"\n"+confirmationUrl);
        //email.setFrom(env.getProperty("support.email")); Potrebno autowired Environment klasu
        return email;
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

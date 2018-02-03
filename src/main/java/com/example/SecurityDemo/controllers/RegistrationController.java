package com.example.SecurityDemo.controllers;

import com.example.SecurityDemo.Dto.PasswordDto;
import com.example.SecurityDemo.Dto.UserDto;
import com.example.SecurityDemo.GenericResponse;
import com.example.SecurityDemo.exceptions.UserNotFoundException;
import com.example.SecurityDemo.captcha.ICaptchaService;
import com.example.SecurityDemo.domain.User;
import com.example.SecurityDemo.domain.VerificationToken;
import com.example.SecurityDemo.events.OnRegistrationCompleteEvent;
import com.example.SecurityDemo.service.ISecurityUserService;
import com.example.SecurityDemo.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.context.SecurityContextHolder;
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
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.UnsupportedEncodingException;
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
    @Autowired
    private ICaptchaService captchaService;
    @Autowired
    private ISecurityUserService securityUserService;

    @RequestMapping(value="/user/registration", method = RequestMethod.GET)
    public String showRegistrationForm(HttpServletResponse response,WebRequest request, Model model){
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
        UserDto userDto = new UserDto();
        model.addAttribute("user", userDto);
        return "registration";
    }

    @RequestMapping(value = "/user/registration",method = RequestMethod.POST)
    @ResponseBody
    public GenericResponse registerUserAccount(@Valid final UserDto accountDto, final HttpServletRequest request){

        String response = request.getParameter("g-recaptcha-response");
        captchaService.processResponse(response);

        final User registered = userService.registerNewUserAccount(accountDto);
        eventPublisher.publishEvent(new OnRegistrationCompleteEvent(registered, request.getLocale(), getAppUrl(request)));
        return new GenericResponse("success");
    }

    @RequestMapping(value = "/registrationConfirm", method = RequestMethod.GET)
    public String confirmRegistration(WebRequest request, ModelMap model, @RequestParam("token") String token,
                                      RedirectAttributes redirectAttributes) throws UnsupportedEncodingException {
        Locale locale = request.getLocale();
        final String result = userService.validateVerificationToken(token);
        if(result.equals("valid")){
            final User user = userService.getUser(token);
            if(user.isUsing2FA()){
                model.addAttribute("qr", userService.generateQRUrl(user));
                redirectAttributes.addFlashAttribute("model",model);
                return "redirect:/qrcode.html?lang=" + locale.getLanguage();
            }
            //Upitno dal ovo tu treba biti. Ako ne prebacit u konstruktor usera
            user.setEnabled(true);
            model.addAttribute("message",messages.getMessage("message.accountVerified",null,locale));
            return "redirect:/login?lang="+locale.getLanguage();
        }
        model.addAttribute("message",messages.getMessage("auth.message."+result, null, locale));
        model.addAttribute("expired","expired".equals(result));
        model.addAttribute("token",token);
        return "redirect:/badUser?lang="+locale.getLanguage();

    }

    @RequestMapping(value = "/qrcode", method = RequestMethod.GET)
    public ModelAndView qrcode(@ModelAttribute("model") ModelMap model){
        String s = (String) model.get("qr");
        return new ModelAndView("qrcode",model );
    }

    @RequestMapping(value = "/user/update/twoFactorSettings", method = RequestMethod.POST)
    @ResponseBody
    public GenericResponse modifyUser2FA(@RequestParam("use2FA") boolean use2FA) throws UnsupportedEncodingException
    {
        User user = userService.update2FA(use2FA);
        if(use2FA){
            return new GenericResponse(userService.generateQRUrl(user));
        }
        return null;
    }
    @RequestMapping(value = "/user/resendRegistrationToken", method = RequestMethod.GET)
    @ResponseBody
    public GenericResponse resendRegistrationToken(
            HttpServletRequest request, @RequestParam("token") String existingToken) {

        VerificationToken newToken = userService.generateNewVerificationToken(existingToken);
        User user = userService.getUser(newToken.getToken());
        SimpleMailMessage email =
                constructResendVerificationTokenEmail(getAppUrl(request), request.getLocale(),newToken, user);
        mailSender.send(email);

        return new GenericResponse(
                messages.getMessage("message.resendToken",null, request.getLocale())
        );
    }

    @RequestMapping("/successRegister")
    public String SuccessRegister(){
        return "successRegister";
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

    @RequestMapping(value = "/user/changePassword", method = RequestMethod.GET)
    public String showChangePasswordPage(Locale locale, Model model, @RequestParam("id") long id,
                                         @RequestParam("token")String token){

        String result = securityUserService.validatePasswordResetToken(id, token);
        if(result != null){
            model.addAttribute("message",messages.getMessage("auth.message."+result,
                    null, locale));
            return "redirect:/login?lang="+locale.getLanguage();
        }
        return "redirect:/user/updatePassword?lang="+locale.getLanguage();
    }

    @RequestMapping(value = "/user/resetPassword", method = RequestMethod.POST)
    @ResponseBody
    public GenericResponse resetPassword(HttpServletRequest request, @RequestParam("email") String userEmail){
        User user = userService.findUserByEmail(userEmail);
        if(user == null){
            throw new UserNotFoundException();
        }

        String token = UUID.randomUUID().toString();
        userService.createPasswordResetTokenForUser(user, token);
        mailSender.send(constructResetTokenEmail(getAppUrl(request), request.getLocale(), token, user));

        return new GenericResponse(messages.getMessage("message.resetPasswordEmail", null,
                request.getLocale()));
    }

    @RequestMapping(value = "/user/savePassword", method = RequestMethod.POST)
    @ResponseBody
    public GenericResponse savePassword(Locale locale, @Valid PasswordDto passwordDto){
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        userService.changePassword(user,passwordDto.getNewPassword());

        return new GenericResponse(messages.getMessage("message.resetPasswordSuc",null, locale));
    }

    @RequestMapping(value = "/forgetPassword")
    public String forgetPassword(HttpServletResponse response){
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
        return "forgetPassword";
    }

    @RequestMapping(value = "/user/updatePassword")
    public String updatePassword(){
        return "updatePassword";
    }
    private SimpleMailMessage constructResetTokenEmail(String contextPath, Locale locale, String token, User user) {
        String url = contextPath + "/user/changePassword?id="+user.getId() + "&token="+token;
        String message = messages.getMessage("message.resetPassword", null, locale);
        if(user.isUsing2FA()) {
            message += "\nTwo Factor QRcode: " + userService.generateQRUrl(user) +"\n";
        }
        message +="\nReset link:";
        return constructEmail("Reset Password:", message + " \r\n"+url,user);
    }

    private SimpleMailMessage constructResendVerificationTokenEmail
            (String appUrl, Locale locale, VerificationToken newToken, User user) {

        String confirmationUrl =
                appUrl + "/registrationConfirm.html?token="+newToken.getToken();
        String message = messages.getMessage("message.resendToken",null,locale);
        return constructEmail("Resend Registration Token", message +" \r\n"+confirmationUrl, user);
    }

    private SimpleMailMessage constructEmail(String subject, String body, User user) {
        SimpleMailMessage email = new SimpleMailMessage();
        email.setSubject(subject);
        email.setText(body);
        email.setTo(user.getEmail());
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
    private String getAppUrl(HttpServletRequest request) {
        return "https://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
    }

}

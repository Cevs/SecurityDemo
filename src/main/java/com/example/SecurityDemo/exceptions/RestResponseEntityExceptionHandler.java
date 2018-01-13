package com.example.SecurityDemo.exceptions;

import com.example.SecurityDemo.GenericResponse;
import com.example.SecurityDemo.captcha.ReCaptchaInvalidException;
import com.example.SecurityDemo.exceptions.UserAlreadyExistException;
import com.example.SecurityDemo.exceptions.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    @Autowired
    private MessageSource messages;

    @Override
    protected ResponseEntity<Object> handleBindException
            (BindException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        BindingResult result = ex.getBindingResult();
        GenericResponse bodyOfResponse =
                new GenericResponse(result.getFieldErrors(), result.getGlobalErrors());
        return handleExceptionInternal(
                ex, bodyOfResponse, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler({ReCaptchaInvalidException.class})
    public ResponseEntity<Object> handleReCaptchaError(RuntimeException ex, WebRequest request){
        GenericResponse bodyOfResponse = new GenericResponse(
                messages.getMessage("message.invalidReCaptcha", null, request.getLocale()), "InvalidReCaptcha");
        return handleExceptionInternal(ex, bodyOfResponse, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler({UserAlreadyExistException.class})
    public ResponseEntity<Object> handleUserAlreadyExist(RuntimeException ex, WebRequest request){
        GenericResponse bodyOfResponse = new GenericResponse(
                messages.getMessage("message.userAlreadyExist", null, request.getLocale()), "UserAlreadyExist");

        return handleExceptionInternal(
                ex, bodyOfResponse, new HttpHeaders(), HttpStatus.CONFLICT, request
        );
    }

    @ExceptionHandler({UserNotFoundException.class})
    public ResponseEntity<Object> handleUserNotFound(RuntimeException ex, WebRequest request){
        logger.error("404 Status Code", ex);
        GenericResponse bodyOfResponse = new GenericResponse(
                messages.getMessage("message.userNotFound", null, request.getLocale()), "UserNotFound");

        return handleExceptionInternal(
                ex, bodyOfResponse, new HttpHeaders(), HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler({MailAuthenticationException.class})
    public ResponseEntity<Object> handleMail(RuntimeException ex, WebRequest request){
        logger.error("500 Status Code", ex);
        GenericResponse bodyOfResponse = new GenericResponse(
                messages.getMessage(
                        "message.email.config.error", null, request.getLocale()), "MailError");

        return handleExceptionInternal(
                ex, bodyOfResponse, new HttpHeaders(), HttpStatus.NOT_FOUND, request);

    }

    @ExceptionHandler({ Exception.class })
    public ResponseEntity<Object> handleInternal(RuntimeException ex, WebRequest request) {
        logger.error("500 Status Code", ex);
        GenericResponse bodyOfResponse = new GenericResponse(
                messages.getMessage(
                        "message.error", null, request.getLocale()), "InternalError");

        return handleExceptionInternal(
                ex, bodyOfResponse, new HttpHeaders(), HttpStatus.NOT_FOUND, request);
    }
}

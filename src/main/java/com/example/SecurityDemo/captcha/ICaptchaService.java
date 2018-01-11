package com.example.SecurityDemo.captcha;

import org.springframework.stereotype.Service;

public interface ICaptchaService {
    void processResponse(String response) throws ReCaptchaInvalidException;
    String getReCaptchaSite();
    String getReCaptchaSecret();
}

package com.example.SecurityDemo;

import com.example.SecurityDemo.exceptions.IpBanException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component("myAuthenticationFailureHandler")
public class MyAuthenticationFailureHandler implements AuthenticationFailureHandler
{
    @Autowired
    private LoginAttemptService loginAttemptService;

    private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException authenticationException) throws IOException, ServletException {
        loginAttemptService.loginFailed(getIpAddress(request));
        handle(request, response, authenticationException);
    }

    private void handle(HttpServletRequest request, HttpServletResponse response, AuthenticationException authenticationException) throws IOException {

        if(authenticationException instanceof BadCredentialsException){
            redirectStrategy.sendRedirect(request,response, "/login?error=true&credentials=true");
        }
        else if(authenticationException instanceof DisabledException){
            redirectStrategy.sendRedirect(request,response, "/login?error=true&disabled=true");
        }
        else if(authenticationException.getCause() instanceof IpBanException){
            redirectStrategy.sendRedirect(request,response, "/login?error=true&ban=true");
        }
        else{
            redirectStrategy.sendRedirect(request,response, "/login?error=true&unknown=true");
        }

    }

    private String getIpAddress(HttpServletRequest request){
        final String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        } else {
            return xfHeader.split(",")[0];
        }
    }

}

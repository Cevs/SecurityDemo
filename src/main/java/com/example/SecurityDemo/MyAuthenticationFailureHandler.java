package com.example.SecurityDemo;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
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

    private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException authenticationException) throws IOException, ServletException {
        handle(request, response, authenticationException);
    }

    private void handle(HttpServletRequest request, HttpServletResponse response, AuthenticationException authenticationException) throws IOException {

        if(authenticationException instanceof BadCredentialsException){
            redirectStrategy.sendRedirect(request,response, "/login?error=true&credentials=true");
        }
        else if(authenticationException instanceof DisabledException){
            redirectStrategy.sendRedirect(request,response, "/login?error=true&disabled=true");
        }
        else{
            redirectStrategy.sendRedirect(request,response, "/login?error=true&unknown=true");
        }

    }

}

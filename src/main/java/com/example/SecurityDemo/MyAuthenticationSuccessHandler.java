package com.example.SecurityDemo;

import com.example.SecurityDemo.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLSession;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@Component("myAuthenticationSuccessHandler")
public class MyAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    @Autowired
    ActiveUserStore activeUserStore;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        handle(request, response, authentication);
        final HttpSession session = request.getSession(true);
        if(session != null){
            session.setMaxInactiveInterval(30*60);
            User user = (User) authentication.getPrincipal();
            String username = user.getEmail();
            LoggedUser loggedUser = new LoggedUser(username, activeUserStore);
            session.setAttribute("user",loggedUser);
        }
        clearAuthenticationAttributes(request);
    }

    private void clearAuthenticationAttributes(HttpServletRequest request) {
        final HttpSession session = request.getSession(false);
        if(session == null){
            return;
        }
        session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
    }

    private void handle(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        final String targetUrl = determineTargetUrl(authentication);
        if(response.isCommitted()){
            return;
        }
        redirectStrategy.sendRedirect(request,response,targetUrl);
    }

    private String determineTargetUrl(Authentication authentication) {

        //To do: check for privileges

        return  "index";

    }
}

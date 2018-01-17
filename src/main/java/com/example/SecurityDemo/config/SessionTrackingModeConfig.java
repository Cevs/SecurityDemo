package com.example.SecurityDemo.config;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.SessionTrackingMode;
import javax.servlet.annotation.WebListener;
import java.util.EnumSet;

@WebListener
public class SessionTrackingModeConfig implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        servletContextEvent.getServletContext().setSessionTrackingModes(EnumSet.of(SessionTrackingMode.COOKIE));
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {

    }
}

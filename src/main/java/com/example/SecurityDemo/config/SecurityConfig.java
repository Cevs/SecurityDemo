package com.example.SecurityDemo.config;

import com.example.SecurityDemo.CustomAuthenticationFailureHandler;
import com.example.SecurityDemo.CustomWebAuthenticationDetailsSource;
import com.example.SecurityDemo.repositories.UserRepository;
import com.example.SecurityDemo.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableWebSecurity
@EnableJpaRepositories(basePackageClasses = UserRepository.class)
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private CustomUserDetailsService userDetailsService;
    @Autowired
    private CustomWebAuthenticationDetailsSource authenticationDetailsSource;


    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(authProvider());
    }



    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable();
        http.authorizeRequests()
                .antMatchers("/login*","/registrationConfirm**","/badUser**","/user/registration*",
                        "/emailError*","/resources/**","/successRegister*","/successRegister*",
                        "/user/resendRegistrationToken", "/css/**", "/js/**", "/images/**","/resources/**").permitAll()
                .anyRequest().authenticated()
                .and()
                .formLogin()
                .loginPage("/login")
                .defaultSuccessUrl("/index")
                .failureUrl("/login?error=true")
                .authenticationDetailsSource(authenticationDetailsSource)
                .permitAll();
    }

    @Bean
    public DaoAuthenticationProvider authProvider(){
        final CustomAuthenticationProvider authProvider = new CustomAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(encoder());
        return authProvider;
    }

    @Bean
    public PasswordEncoder encoder(){
        return new BCryptPasswordEncoder(11);
    }

}

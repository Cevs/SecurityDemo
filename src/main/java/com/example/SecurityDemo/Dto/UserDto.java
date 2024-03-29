package com.example.SecurityDemo.Dto;

import com.example.SecurityDemo.validation.PasswordMatches;
import com.example.SecurityDemo.validation.ValidEmail;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;

@PasswordMatches
public class UserDto {
    @NotNull
    @NotEmpty(message = "")
    private String firstName;

    @NotNull
    @NotEmpty(message = "")
    private String lastName;

    @NotNull
    @NotEmpty(message = "")
    @ValidEmail
    private String email;

    @NotNull
    @NotEmpty(message = "")
    private String password;
    private String matchingPassword;

    private boolean using2fa;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getMatchingPassword() {
        return matchingPassword;
    }

    public void setMatchingPassword(String matchingPassword) {
        this.matchingPassword = matchingPassword;
    }

    public boolean isUsing2fa() {
        return using2fa;
    }

    public void setUsing2fa(boolean using2fa) {
        this.using2fa = using2fa;
    }
}

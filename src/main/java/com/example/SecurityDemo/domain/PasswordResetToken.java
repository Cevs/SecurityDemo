package com.example.SecurityDemo.domain;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
public class PasswordResetToken {
    private static final int EXPIRATION = 60*24;

    @Id
    //We need to define new generator because we want out primary key to
    //be same as primary key of table "user_profile" that is class/Entity User
    @GeneratedValue(generator = "newGenerator")
    @GenericGenerator(name ="newGenerator", strategy = "foreign",
            parameters = {@org.hibernate.annotations.Parameter(value="user", name="property")})//value -> name of class/Entity
    private Long id;

    @Column(name = "token")
    private String token;

    @OneToOne(targetEntity = User.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable =  false, name = "id")
    private User user;

    @Column(name="expiry_date")
    private LocalDateTime expiryDate;

    public PasswordResetToken() {}

    public PasswordResetToken(String token, User user) {
        this.token = token;
        this.user = user;
        this.expiryDate = LocalDateTime.now().plusHours(EXPIRATION);
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate.plusHours(EXPIRATION);
    }
}

package com.example.SecurityDemo.domain;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;

@Entity
@Table(name="verification_token", schema = "public")
public class VerificationToken {
    private static final int EXPIRATION = 24; //Hours

    @Id
    //We need to define new generator because we want out primary key to
    //be same as primary key of table "user_profile" that is class/Entity User
    @GeneratedValue(generator = "newGenerator")
    @GenericGenerator(name ="newGenerator", strategy = "foreign",
            parameters = {@org.hibernate.annotations.Parameter(value="user", name="property")})//value -> name of class/Entity
    private Long id;

    @Column(name="token")
    private String token;

    @OneToOne(targetEntity = User.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "id")
    private User user;

    @Column(name="expiry_date")
    private LocalDateTime expiryDate;

    public VerificationToken(String token, User user, LocalDateTime registerDate) {
        this.token = token;
        this.user = user;
        this.expiryDate = registerDate.plusHours(EXPIRATION);
    }

    private Date calculateExpiryDate(int expiryTimeInMinutes){
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Timestamp(cal.getTime().getTime()));
        cal.add(Calendar.MINUTE, expiryTimeInMinutes);
        return new Date(cal.getTime().getTime());
    }

    public VerificationToken(){}

    public static int getEXPIRATION() {
        return EXPIRATION;
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
        this.expiryDate = expiryDate;
    }
}

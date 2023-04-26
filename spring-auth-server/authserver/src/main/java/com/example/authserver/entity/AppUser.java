package com.example.authserver.entity;

import java.util.Date;
import java.util.UUID;

import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import lombok.Data;

@Entity
@Data
public class AppUser {

    @Id
    private String id;
    
    @Column(name = "google_id", unique = true, length = 45)
    private String googleId;
    
    @Column(name = "phone", unique = true, length = 45)
    private String phone;

    @Column(name = "email", unique = true, length = 45)
    private String email;
     
    @Column(name = "password", length = 64)
    private String password;
     
    @Column(name = "first_name", length = 20)
    private String firstName;
     
    @Column(name = "last_name", length = 20)
    private String lastName;

    @Column(name = "role", length = 20)
    private String role;

    private Date createdDt;

    @PrePersist() 
    public void beforeCreate() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
        if (createdDt == null) {
            createdDt = new Date();
        }
    }

    public static AppUser fromUserDetails(UserDetails userDetails) {
        AppUser appUser = new AppUser();
        appUser.setEmail( userDetails.getUsername() );
        appUser.setPassword( userDetails.getPassword() );
        appUser.setRole( userDetails.getAuthorities().iterator().next().getAuthority() );
        return appUser;
    }
}

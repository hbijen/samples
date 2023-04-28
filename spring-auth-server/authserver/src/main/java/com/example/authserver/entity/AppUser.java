package com.example.authserver.entity;

import java.util.Date;
import java.util.UUID;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.example.authserver.enums.RegisterWith;
import com.example.authserver.model.SignupModel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import lombok.Data;

@Entity
@Data
public class AppUser {

    @Id
    private String id;
    
    @Column(name = "email", unique = true, length = 255)
    private String email;
     
    @Column(name = "phone", unique = true, length = 16)
    private String phone;

    @Column(name = "register_with", length = 16)
    @Enumerated(EnumType.STRING)
    private RegisterWith registerWith;

    @Column(name = "password", length = 100)
    private String password;
     
    @Column(name = "first_name", length = 255)
    private String firstName;
     
    @Column(name = "last_name", length = 255)
    private String lastName;

    @Column(name = "created_dt")
    private Date createdDt;

    @Column(name = "role", length = 20)
    private String role;

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

    public static AppUser from(SignupModel signupModel) {
        AppUser appUser = new AppUser();
        appUser.setRole("ROLE_User"); //default role for newly created user

        if ( signupModel.getRegisterWith() == RegisterWith.email ) {
            appUser.setEmail(signupModel.getEmail());
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            String encodedPassword = passwordEncoder.encode(signupModel.getPassword());
            appUser.setPassword("{bcrypt}"+encodedPassword);
            appUser.setRegisterWith(RegisterWith.email);
        }
        if ( signupModel.getRegisterWith() == RegisterWith.sms ) {
            appUser.setPhone(signupModel.getPhone());
            appUser.setRegisterWith(RegisterWith.sms);
        }

        return appUser;     
    }    
}

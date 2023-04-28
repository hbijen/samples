package com.example.authserver.model;

import java.util.Arrays;
import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.example.authserver.entity.AppUser;

/**
 * Provides a custom implementation of spring security UserDetails object
 */
public class AppUserDetails implements UserDetails {

    private AppUser appUser;
     
    public AppUserDetails(AppUser appUser) {
        this.appUser = appUser;
    }
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Arrays.asList(new String[]{this.appUser.getRole()}).stream().map(d -> new SimpleGrantedAuthority(d)).toList();
        
    }

    @Override
    public String getPassword() {
        return appUser.getPassword();
    }

    @Override
    public String getUsername() {
        return appUser.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
   
}

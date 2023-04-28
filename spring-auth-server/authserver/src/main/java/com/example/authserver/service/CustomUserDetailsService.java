package com.example.authserver.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Service;

import com.example.authserver.entity.AppUser;
import com.example.authserver.model.AppUserDetails;
import com.example.authserver.repository.AppUserRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * Implements spring interfaces to allows read/write to custom user table
 * A bean need not be created in the config file, as the @Service annotation creates an instance
 *
 * FIXME:
 * 1. Update AppUser, AppUserRepository to match the table structure in the mysql db
 * 2. Or replace with the appropriate api to read/write the user
 * 
 */
@Service
@Slf4j
public class CustomUserDetailsService implements UserDetailsManager {
    
	@Autowired
    private AppUserRepository appUserRepo;
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        AppUser appUser = appUserRepo.findByEmail(username);
        if (appUser == null) {
            throw new UsernameNotFoundException("User not found");
        }
        return new AppUserDetails(appUser);
    }

    @Override
    public void createUser(UserDetails user) {
        appUserRepo.save(AppUser.fromUserDetails(user));
    }

    @Override
    public void updateUser(UserDetails user) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateUser'");
    }

    @Override
    public void deleteUser(String username) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteUser'");
    }

    @Override
    public void changePassword(String oldPassword, String newPassword) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'changePassword'");
    }

    @Override
    public boolean userExists(String username) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'userExists'");
    }

}

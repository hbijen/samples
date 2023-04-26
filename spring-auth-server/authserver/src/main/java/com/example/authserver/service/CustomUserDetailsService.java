package com.example.authserver.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Service;

import com.example.authserver.entity.AppUser;
import com.example.authserver.model.AppUserDetails;
import com.example.authserver.repository.AppUserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsManager {
    
	@Autowired
    private AppUserRepository appUserRepo;
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        //TODO: Replace with the appropriate api to load the user!
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

    // public static void main() {
    //     BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    //     System.out.println("password:  " + passwordEncoder.encode("password"));
    // }

}

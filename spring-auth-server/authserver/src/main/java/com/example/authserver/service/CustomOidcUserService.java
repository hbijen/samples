package com.example.authserver.service;

import java.util.List;
import java.util.stream.Stream;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import com.example.authserver.entity.AppUser;
import com.example.authserver.enums.RegisterWith;
import com.example.authserver.model.AppUserDetails;

import lombok.extern.slf4j.Slf4j;

/**
 * Upon oauth authentication of external provider such as google, save the user details to the custom user table.
 * Use CustomUserDetailsService to save the user
 */
@Slf4j
public class CustomOidcUserService extends OidcUserService {
    final CustomUserDetailsService userDetailsService;

    public CustomOidcUserService(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        DefaultOidcUser oidcUser  = (DefaultOidcUser)super.loadUser(userRequest);

        log.info("CustomOidcUserService attr: " + oidcUser.getAttributes());
        log.info("CustomOidcUserService getUserInfo: " + oidcUser.getUserInfo());
        log.info("CustomOidcUserService getUserInfo: " + oidcUser.getAuthorities());
        log.info("CustomOidcUserService getName: " + oidcUser.getName());
        log.info("CustomOidcUserService getName: " + oidcUser.getIdToken());
        log.info("CustomOidcUserService trace: ", new Throwable());

        String email = oidcUser.getEmail();
        AppUserDetails userDetails = null;
        
        try {
            userDetails = (AppUserDetails)userDetailsService.loadUserByUsername(email);
        } catch (UsernameNotFoundException ex) {
            //creates a new user entry if not already exists.
            AppUser appUser = new AppUser();
            appUser.setEmail(email);
            appUser.setRole("ROLE_User"); // new users default role
            appUser.setRegisterWith(RegisterWith.google);
            userDetails = new AppUserDetails(appUser);
            userDetailsService.createUser(userDetails);
        }

        // include the application role as an authority to the OidcUser created upon successful google login
        List<GrantedAuthority> authorities = Stream.concat(oidcUser.getAuthorities().stream(), userDetails.getAuthorities().stream()).toList();

        return new DefaultOidcUser(authorities,userRequest.getIdToken(),oidcUser.getUserInfo());
    }
}

package com.example.authserver.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import com.example.authserver.service.CustomOidcUserService;
import com.example.authserver.service.CustomUserDetailsService;

@Configuration(proxyBeanMethods = false)
@EnableWebSecurity
public class DefaultSecurityConfig {
    
    @Autowired
	private ClientRegistrationRepository clientRegistrationRepository;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;    

    @Bean
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(authorizeRequests ->
            authorizeRequests
            .requestMatchers("/static/**", "/login", "/register/**").permitAll()
            .anyRequest().authenticated());

        // for standard user name password login
        http.formLogin()
            .loginPage("/login")
            .permitAll();

        // for google login
        http.oauth2Login( oauth2Login -> oauth2Login
                .userInfoEndpoint()
                    .oidcUserService(customOidcUserService())
                    .and()
                .loginPage("/login")
            )
            .oauth2Client();
        return http.build();
    }

    @Bean()
    public CustomOidcUserService customOidcUserService() {
        return new CustomOidcUserService(customUserDetailsService);
    }

    // @Bean()
    // public CustomOAuth2UserService customOAuth2UserService() {
    //     return new CustomOAuth2UserService(customUserDetailsService);
    // }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

	private LogoutSuccessHandler oidcLogoutSuccessHandler() {
		OidcClientInitiatedLogoutSuccessHandler oidcLogoutSuccessHandler =
				new OidcClientInitiatedLogoutSuccessHandler(this.clientRegistrationRepository);

		// Set the location that the End-User's User Agent will be redirected to
		// after the logout has been performed at the Provider
		oidcLogoutSuccessHandler.setPostLogoutRedirectUri("{baseUrl}/index");

		return oidcLogoutSuccessHandler;
	}     
}

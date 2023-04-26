package com.example.resourceserver.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@Slf4j
public class ResourceServerConfig {

    // @Autowired DefaultWebSecurityExpressionHandler expressionHandler;

    // @PostConstruct
    // private void postConstruct() {
    //     expressionHandler.setRoleHierarchy(roleHierarchy());
    // }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // @formatter:off
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(new JwtRoleConverter());
                
		http
			//.securityMatcher("/messages/**")
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/public/**", "/signup", "/about").permitAll()
                .requestMatchers("/product/**").hasAuthority("SCOPE_product.read")
                .requestMatchers("/sales/**").hasAuthority("SCOPE_sales.read")
            )
			.oauth2ResourceServer()
				.jwt()
                .jwtAuthenticationConverter(jwtAuthenticationConverter);
        // @formatter:on

        return http.build();
    }
   

    @Bean
    public RoleHierarchy roleHierarchy() {
        RoleHierarchyImpl roleHierarchy = new RoleHierarchyImpl();
        // roleHierarchy.setHierarchy("""
        //     ROLE_Admin > ROLE_Owner 
        //     ROLE_Owner > ROLE_Employee
        //     ROLE_Employee > ROLE_LowEmployee 
        //     ROLE_LowEmployee > ROLE_User
        //     """);
        roleHierarchy.setHierarchy("ROLE_Admin > ROLE_Owner \n ROLE_Owner > ROLE_Employee \n ROLE_Employee > ROLE_LowEmployee \n ROLE_LowEmployee > ROLE_User");
        return roleHierarchy;
    }

    @Bean
    public MethodSecurityExpressionHandler expressionHandler(RoleHierarchy roleHierarchy) {
        DefaultMethodSecurityExpressionHandler expressionHandler = new DefaultMethodSecurityExpressionHandler();
        expressionHandler.setRoleHierarchy(roleHierarchy);
        return expressionHandler;
    }


    public class JwtRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

        @Override
        @SuppressWarnings("unchecked")
        public Collection<GrantedAuthority> convert(Jwt jwt) {
            System.out.println("jwt claims...." + jwt.getClaims());
            log.info("jwt claims...." + jwt.getClaims());
            ArrayList<String> scope = (ArrayList<String>) jwt.getClaims().get("scope");
            ArrayList<String> roles = (ArrayList<String>) jwt.getClaims().get("roles");

            List<GrantedAuthority> authorities = new ArrayList<>();
            if (scope != null) {
                authorities.addAll(scope.stream().map(d -> new SimpleGrantedAuthority("SCOPE_"+d)).collect(Collectors.toList()));
            }
            if (roles != null) {
                authorities.addAll(roles.stream().map(d -> new SimpleGrantedAuthority(d)).collect(Collectors.toList()));
            }
            return authorities;
        }
    
    }
    
}
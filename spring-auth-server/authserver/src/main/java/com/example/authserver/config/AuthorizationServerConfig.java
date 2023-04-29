package com.example.authserver.config;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

import com.example.authserver.model.RegistrationClient;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class AuthorizationServerConfig {
	
    @Autowired
	private Environment env;

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SecurityFilterChain authServerSecurityFilterChain(HttpSecurity http) throws Exception {
        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);

        http.getConfigurer(OAuth2AuthorizationServerConfigurer.class)
				.oidc(Customizer.withDefaults());

        // @formatter:off
        http
            // Redirect to the login page when not authenticated from the
            // authorization endpoint
            .exceptionHandling((exceptions) -> 
                exceptions.authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login")))
            // Accept access tokens for User Info and/or Client Registration
            .oauth2ResourceServer()
            .jwt();

        return http.build();
    }

    /**
     * @Import(OAuth2AuthorizationServerConfiguration.class) could be used instead of creating this bean
     * however if endpoint url or other settings needs to be customized
     */
	@Bean 
	public AuthorizationServerSettings authorizationServerSettings() {
        // create an mapping in /etc/hosts for 127.0.0.1 and auth-server 
        String issuerUrl = env.getProperty("app.auth.issuer", "http://auth-server:9000");
		return AuthorizationServerSettings.builder()
				.issuer(issuerUrl)
				.build();
	}    


    /**
     * add user roles to jwt token
     */
    @Bean
    OAuth2TokenCustomizer<JwtEncodingContext> jwtCustomizer() {
        return context -> {
            
            if (context.getTokenType() == OAuth2TokenType.ACCESS_TOKEN) {

                Authentication principal = context.getPrincipal();
                log.info("OAuth2TokenCustomizer: " + principal.getPrincipal() + ": " + principal.getAuthorities());
                Set<String> authorities = principal.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toSet());
                context.getClaims().claim("roles", authorities);
            }
        };
    }

	@Bean
    public RegisteredClientRepository registeredClientRepository(JdbcTemplate jdbcTemplate) {

		JdbcRegisteredClientRepository registeredClientRepository = new JdbcRegisteredClientRepository(jdbcTemplate);
		List<RegistrationClient> clients = RegistrationClient.fromEnv(env);
		for (RegistrationClient client: clients) {
			RegisteredClient registeredClient = registeredClientRepository.findByClientId(client.getClientId());
			if (registeredClient == null) {
                // client id is unique, use it as the primary key
                // this allows client configuration updates whenever there are changes in yaml
				RegisteredClient.Builder clientBuilder = RegisteredClient.withId(client.getClientId())
						.clientId(client.getClientId())
						.clientSecret(client.getClientSecret())
						.clientName(client.getClientName())
						.tokenSettings(tokenSettings())
						.clientSettings(ClientSettings.builder().requireAuthorizationConsent(false).build());

				client.getClientAuthenticationMethods().forEach((d) -> clientBuilder.clientAuthenticationMethod(new ClientAuthenticationMethod(d)));
				client.getAuthorizationGrantTypes().forEach((d) -> clientBuilder.authorizationGrantType(new AuthorizationGrantType(d)));
				client.getRedirectUris().forEach((d) -> clientBuilder.redirectUri(d));
				client.getScopes().forEach((d) -> clientBuilder.scope(d));

				registeredClientRepository.save(clientBuilder.build());
			}
		}

		return registeredClientRepository;
	}

	@Bean
	public OAuth2AuthorizationService authorizationService(JdbcTemplate jdbcTemplate, RegisteredClientRepository registeredClientRepository) {
		return new JdbcOAuth2AuthorizationService(jdbcTemplate, registeredClientRepository);
	}

	@Bean
	public OAuth2AuthorizationConsentService authorizationConsentService(JdbcTemplate jdbcTemplate, RegisteredClientRepository registeredClientRepository) {
		return new JdbcOAuth2AuthorizationConsentService(jdbcTemplate, registeredClientRepository);
	}

    /// Methods for signing the token 
    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }

    @Bean
    public JWKSource<SecurityContext> jwkSource() throws NoSuchAlgorithmException {
        RSAKey rsaKey = generateRsa();
        JWKSet jwkSet = new JWKSet(rsaKey);
        return (jwkSelector, securityContext) -> jwkSelector.select(jwkSet);
    }

    private static RSAKey generateRsa() throws NoSuchAlgorithmException {
        KeyPair keyPair = generateRsaKey();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        return new RSAKey.Builder(publicKey)
        .privateKey(privateKey)
        .keyID(UUID.randomUUID().toString())
        .build();
    }

    private static KeyPair generateRsaKey() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        return keyPairGenerator.generateKeyPair();
    }


    @Bean
	public TokenSettings tokenSettings() {
		// the time to live defaults to 10 days each
		Long accessTokenTTL =  env.getProperty("app.auth.token.accessTokenTTL", Long.class, 10L);
		Long refreshTokenTTL =  env.getProperty("app.auth.token.refreshTokenTTL", Long.class, 10L);
		return TokenSettings.builder()
				.accessTokenTimeToLive(Duration.ofDays(accessTokenTTL))
				.refreshTokenTimeToLive(Duration.ofDays(refreshTokenTTL))
				.build();
	}
  
}

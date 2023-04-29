package com.example.authserver.model;

import java.util.Arrays;
import java.util.List;

import org.springframework.core.env.Environment;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class RegistrationClient {
	private static final long serialVersionUID = 1L;
	private String clientId;
	private String clientSecret;
	private String clientName;
	private List<String> clientAuthenticationMethods;
	private List<String> authorizationGrantTypes;
	private List<String> redirectUris;
	private List<String> scopes;

	public static List<RegistrationClient> fromEnv(Environment env) {
		String clientKeys = env.getProperty("app.client.keys","");
		System.out.println("clientKeys " + clientKeys);
		System.out.println("asList(clientKeys) " + asList(clientKeys));
		return asList(clientKeys).stream().map((d) -> {
			RegistrationClient client = new RegistrationClient();
			final String prefixKey = String.format("app.client.registration.%s.", d);
			System.out.println("client name " + env.getProperty(prefixKey+"client-name"));
			client.setClientId(env.getProperty(prefixKey+"client-id"));
			client.setClientSecret(env.getProperty(prefixKey+"client-secret"));
			client.setClientName(env.getProperty(prefixKey+"client-name"));
			client.setClientAuthenticationMethods(asList(env.getProperty(prefixKey+"client-authentication-methods")));
			client.setAuthorizationGrantTypes(asList(env.getProperty(prefixKey+"authorization-grant-types")));
			client.setRedirectUris(asList(env.getProperty(prefixKey+"redirect-uris")));
			client.setScopes(asList(env.getProperty(prefixKey+"scopes")));
			return client;
		}).toList();
	}

	private static List<String> asList(String commaSeparatedValues) {
		return Arrays.asList(commaSeparatedValues.split(",")).stream().map(String::trim).toList();
	}
}

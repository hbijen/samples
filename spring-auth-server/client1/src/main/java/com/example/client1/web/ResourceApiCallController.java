package com.example.client1.web;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class ResourceApiCallController {

    private final WebClient webClient;

	public ResourceApiCallController(WebClient webClient) {
		this.webClient = webClient;
	}    

	@GetMapping(value = "/authorized", params = OAuth2ParameterNames.ERROR)
	public Object authorizationFailed(HttpServletRequest request) {
        Map<String, String> result = new HashMap<>();
		String errorCode = request.getParameter(OAuth2ParameterNames.ERROR);
		if (StringUtils.hasText(errorCode)) {
            result.put("error_code", errorCode);
            result.put("error_description", request.getParameter(OAuth2ParameterNames.ERROR_DESCRIPTION));
            result.put("error_uri", request.getParameter(OAuth2ParameterNames.ERROR_URI));
            return result;
		}
		return "redirect:/get-products";
	}

	@GetMapping(value = "/get-public")
	public String getPublicData(Model model,
        @RegisteredOAuth2AuthorizedClient("product-client-id") OAuth2AuthorizedClient authorizedClient) {

        String[] items = new String[]{};
        try {            
            items = this.webClient
                .get()
                .uri("http://127.0.0.1:8090/public/data")
                .attributes(ServletOAuth2AuthorizedClientExchangeFilterFunction.oauth2AuthorizedClient(authorizedClient))
                .retrieve()
                .bodyToMono(String[].class)
                .block();
        } catch (WebClientException ex) {
            model.addAttribute("error", ex.getMessage());
            log.error("WebClient exception: " + ex.getMessage(),ex);
        } catch (Exception ex) {
            log.error("Exception exception: " + ex.getMessage(),ex);
        }                 
        model.addAttribute("title", "Public Data");
        model.addAttribute("items", items);
		return "index";
	}    

	@GetMapping(value = "/get-products")
	public String getProduct(Model model,
        @RegisteredOAuth2AuthorizedClient("product-client-id") OAuth2AuthorizedClient authorizedClient) {

        String[] items = new String[]{};
        try {
            items = this.webClient
                .get()
                .uri("http://127.0.0.1:8090/product/data")
                .attributes(ServletOAuth2AuthorizedClientExchangeFilterFunction.oauth2AuthorizedClient(authorizedClient))
                .retrieve()
                .bodyToMono(String[].class)
                .block();
        } catch (WebClientException ex) {
            model.addAttribute("error", ex.getMessage());
            log.error("WebClient exception: " + ex.getMessage(),ex);
        } catch (Exception ex) {
            log.error("Exception exception: " + ex.getMessage(),ex);
        }        
        
        model.addAttribute("title", "Product Data");
        model.addAttribute("items", items);

		return "index";
	}
	@GetMapping(value = "/secure-products")
	public String getSecureProduct(Model model,
        @RegisteredOAuth2AuthorizedClient("product-client-id") OAuth2AuthorizedClient authorizedClient) {

        String[] items = new String[]{};
        try {
            items = this.webClient
                .get()
                .uri("http://127.0.0.1:8090/product/secured-data")
                .attributes(ServletOAuth2AuthorizedClientExchangeFilterFunction.oauth2AuthorizedClient(authorizedClient))
                .retrieve()
                .bodyToMono(String[].class)
                .block();
        } catch (WebClientException ex) {
            model.addAttribute("error", ex.getMessage());
            log.error("WebClient exception: " + ex.getMessage(),ex);
        } catch (Exception ex) {
            log.error("Exception exception: " + ex.getMessage(),ex);
        }        
        
        model.addAttribute("title", "Product Secure Data");
        model.addAttribute("items", items);

		return "index";
	}
	@GetMapping(value = "/get-sales")
	public String getSales(Model model,
        @RegisteredOAuth2AuthorizedClient("product-client-id") OAuth2AuthorizedClient authorizedClient) {

        String[] items = new String[]{};
        
        try {
            items = this.webClient
                .get()
                .uri("http://127.0.0.1:8090/sales/data")
                .attributes(ServletOAuth2AuthorizedClientExchangeFilterFunction.oauth2AuthorizedClient(authorizedClient))
                .retrieve()
                .bodyToMono(String[].class)
                .block();
        } catch (WebClientException ex) {
            model.addAttribute("error", ex.getMessage());
            log.error("WebClient exception: " + ex.getMessage(),ex);
        } catch (Exception ex) {
            log.error("Exception exception: " + ex.getMessage(),ex);
        }


        model.addAttribute("title", "Sales Data");
        model.addAttribute("items", items);
		return "index";
	}

}

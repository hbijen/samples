package com.example.resourceserver.api;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/product")
public class ProductController {
    
    @GetMapping("/data")
    @PreAuthorize("hasRole('ROLE_User')")
    public String[] getData() {
        return new String[] { "Product 1", "Product 2" };
    }

    @GetMapping("/secured-data")
    @PreAuthorize("hasRole('ROLE_LowEmployee')")
    public String[] getData2() {
        return new String[] { "Product 1 secured", "Product 2 secured" };
    }    
}

package com.example.resourceserver.api;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sales")
public class SalesController {
    
    @GetMapping("/data")
    @PreAuthorize("hasRole('LowEmployee')")
    public String[] getData() {
        return new String[] { "Sales 1", "Sales 2" };
    }

}

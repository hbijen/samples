package com.example.resourceserver.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/public")
public class PublicController {
    
    @GetMapping("/data")
    public String[] getData() {
        return new String[] { "Public One", "Public Two" };
    }

}

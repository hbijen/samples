package com.example.client1.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class HomeController {
    
	@GetMapping("/")
	public String root() {
		return "redirect:/homepage";
	}
    
    @GetMapping("/homepage")
    public String homepage() {
        return "index";
    }

}

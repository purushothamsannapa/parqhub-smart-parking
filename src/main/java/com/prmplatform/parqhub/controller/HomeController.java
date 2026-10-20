package com.prmplatform.parqhub.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String homePage() {
        return "index"; // templates/index.html
    }

    @GetMapping("/login")
    public String userLogin() {
        return "user-login"; // templates/user-login.html
    }
}
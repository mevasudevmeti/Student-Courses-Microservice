package com.leedsbeckett.financeservice.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PortalController {

    @GetMapping("/")
    public String dashboard() {
        return "forward:/index.html";
    }
}

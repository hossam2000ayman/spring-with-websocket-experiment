package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class WebController {

    @Autowired
    private UserService userService;

    @GetMapping("/admin")
    public String adminDashboard(Model model, Authentication authentication) {
        model.addAttribute("users", userService.findAll());
        model.addAttribute("currentUser", authentication.getName());
        return "admin/dashboard";
    }

    @GetMapping("/chat")
    public String chatPage(Model model, Authentication authentication) {
        User currentUser = userService.findByEmail(authentication.getName());
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("onlineUsers", userService.findOnlineUsers());
        return "chat/index";
    }

    @PostMapping("/register")
    public String registerUser(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String password,
            Model model) {
        try {
            userService.createUser(name, email, password);
            return "redirect:/login?registered=true";
        } catch (Exception e) {
            model.addAttribute("error", "Registration failed: " + e.getMessage());
            return "register";
        }
    }
}

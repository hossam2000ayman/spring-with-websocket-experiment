package com.example.demo.controller;

import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/admin/api")
public class AdminApiController {

    @Autowired
    private UserService userService;

    // Admin User Management API
    @PostMapping("/users")
    public ResponseEntity<Map<String, Object>> createUser(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String role) {
        Map<String, Object> response = new HashMap<>();
        try {
            Set<Role> roles = new HashSet<>();
            Role userRole = userService.getRoleByName("ROLE_" + role.toUpperCase());
            roles.add(userRole);
            
            User user = new User(name, email, password);
            user.setRoles(roles);
            
            User savedUser = userService.create(user, roles);
            response.put("success", true);
            response.put("user", savedUser);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        try {
            User user = userService.findById(id);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<Map<String, Object>> updateUser(
            @PathVariable Long id,
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam(required = false) String password,
            @RequestParam String role) {
        Map<String, Object> response = new HashMap<>();
        try {
            User user = userService.findById(id);
            user.setName(name);
            user.setEmail(email);
            
            if (password != null && !password.isEmpty()) {
                user.setPassword(password);
            }
            
            Set<Role> roles = new HashSet<>();
            Role userRole = userService.getRoleByName("ROLE_" + role.toUpperCase());
            roles.add(userRole);
            user.setRoles(roles);
            
            User updatedUser = userService.updateUser(id, user);
            response.put("success", true);
            response.put("user", updatedUser);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            userService.deleteById(id);
            response.put("success", true);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return ResponseEntity.ok(response);
    }
}
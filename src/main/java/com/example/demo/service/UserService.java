package com.example.demo.service;

import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }


    public User createUser(User user) {
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            throw new RuntimeException("Password cannot be empty");
        }
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);
        return userRepository.save(user);
    }

    public User create(User user, Set<Role> roles) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRoles(roles);
        user.setLastSeen(LocalDateTime.now());

        return userRepository.save(user);
    }

    public User updateUser(Long id, User userDetails) {
        User user = findById(id);

        user.setName(userDetails.getName());
        user.setEmail(userDetails.getEmail());

        if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
        }

        if (userDetails.getRoles() != null) {
            user.setRoles(userDetails.getRoles());
        }

        return userRepository.save(user);
    }

    public void deleteById(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    public User createUser(String name, String email, String password) {
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Default role not found"));

        Set<Role> roles = new HashSet<>();
        roles.add(userRole);

        password = passwordEncoder.encode(password);
        User user = new User(name, email, password);
        user.setRoles(roles);
        user.setLastSeen(LocalDateTime.now());

        return userRepository.save(user);
    }

    public User createAdmin(String name, String email, String password) {
        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseThrow(() -> new RuntimeException("Admin role not found"));

        Set<Role> roles = new HashSet<>();
        roles.add(adminRole);

        password = passwordEncoder.encode(password);
        User user = new User(name, email, password);
        user.setRoles(roles);
        user.setLastSeen(LocalDateTime.now());

        return userRepository.save(user);
    }

    public void initializeRoles() {
        if (!roleRepository.existsByName("ROLE_ADMIN")) {
            Role adminRole = new Role("ROLE_ADMIN", "Administrator with full access");
            roleRepository.save(adminRole);
        }

        if (!roleRepository.existsByName("ROLE_USER")) {
            Role userRole = new Role("ROLE_USER", "Regular user with chat access");
            roleRepository.save(userRole);
        }
    }

    public void initializeUsers() {
        if (!userRepository.existsByEmail("admin@chat.com")) {
            createAdmin("Admin User", "admin@chat.com", "admin123");
        }
        if (!userRepository.existsByEmail("user@chat.com")) {
            createUser("Normal User", "user@chat.com", "user123");
        }
    }

    public void setOnlineStatus(Long userId, boolean online) {
        User user = findById(userId);
        user.setOnline(online);
        user.setLastSeen(LocalDateTime.now());
        userRepository.save(user);
    }

    public List<User> findOnlineUsers() {
        return userRepository.findAll().stream()
                .filter(User::isOnline)
                .toList();
    }

    public Role getRoleByName(String roleName) {
        return roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));
    }
}
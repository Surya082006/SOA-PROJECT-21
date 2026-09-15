package com.bibliotech.authservice.controller;

import com.bibliotech.authservice.dto.AuthResponse;
import com.bibliotech.authservice.dto.LoginRequest;
import com.bibliotech.authservice.dto.RegisterRequest;
import com.bibliotech.authservice.model.Role;
import com.bibliotech.authservice.model.User;
import com.bibliotech.authservice.repository.UserRepository;
import com.bibliotech.authservice.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest().body("Username already taken");
        }
        Role role = request.getRole() == null ? Role.STUDENT : request.getRole();
        User user = new User(
                request.getUsername(),
                passwordEncoder.encode(request.getPassword()),
                role
        );
        userRepository.save(user);
        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElse(null);

        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity.status(401).body("Invalid username or password");
        }

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());
        return ResponseEntity.ok(new AuthResponse(token));
    }
}

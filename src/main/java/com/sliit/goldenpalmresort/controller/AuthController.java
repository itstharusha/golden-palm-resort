package com.sliit.goldenpalmresort.controller;

import com.sliit.goldenpalmresort.dto.LoginRequest;
import com.sliit.goldenpalmresort.dto.LoginResponse;
import com.sliit.goldenpalmresort.model.User;
import com.sliit.goldenpalmresort.repository.UserRepository;
import com.sliit.goldenpalmresort.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtService.generateToken(userDetails.getUsername());
            User user = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
            
            // Update last login time
            if (user != null) {
                user.setLastLogin(java.time.LocalDateTime.now());
                userRepository.save(user);
            }

            return ResponseEntity.ok(new LoginResponse(token, user, "Login successful", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody LoginRequest request) {
        try {
            if (userRepository.findByUsername(request.getUsername()).isPresent()) {
                return ResponseEntity.badRequest().body("Username already exists");
            }

            User newUser = new User();
            newUser.setUsername(request.getUsername());
            newUser.setPassword(passwordEncoder.encode(request.getPassword()));
            newUser.setEmail(request.getUsername() + "@example.com"); // Simple email assignment
            newUser.setFirstName(request.getUsername());
            newUser.setLastName(request.getUsername()); // Use username as last name instead of hardcoded "User"
            newUser.setRole(User.UserRole.GUEST);
            newUser.setActive(true); // Explicitly set user as active

            userRepository.save(newUser);
            return ResponseEntity.ok("User registered successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Registration failed");
        }
    }
}
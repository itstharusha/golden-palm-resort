package com.sliit.goldenpalmresort.service;

import com.sliit.goldenpalmresort.dto.LoginRequest;
import com.sliit.goldenpalmresort.dto.LoginResponse;
import com.sliit.goldenpalmresort.dto.RegisterRequest;
import com.sliit.goldenpalmresort.model.User;
import com.sliit.goldenpalmresort.repository.UserRepository;
import com.sliit.goldenpalmresort.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AuthenticationManager authenticationManager;

    public LoginResponse login(LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtService.generateToken(userDetails.getUsername());

            Optional<User> user = userRepository.findByUsername(userDetails.getUsername());
            if (user.isPresent()) {
                return new LoginResponse(token, user.get(), "Login successful", true);
            } else {
                return new LoginResponse("User not found", false);
            }
        } catch (Exception e) {
            return new LoginResponse("Invalid username or password", false);
        }
    }

    public LoginResponse register(RegisterRequest registerRequest) {
        // Check if username already exists
        if (userRepository.findByUsername(registerRequest.getUsername()).isPresent()) {
            return new LoginResponse("Username already exists", false);
        }

        // Check if email already exists
        if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            return new LoginResponse("Email already exists", false);
        }

        // Create new user
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setFirstName(registerRequest.getFirstName());
        user.setLastName(registerRequest.getLastName());
        user.setPhone(registerRequest.getPhone());
        user.setRole(User.UserRole.GUEST); // Default role for new registrations
        user.setActive(true);

        try {
            user = userRepository.save(user);
            String token = jwtService.generateToken(user.getUsername());
            return new LoginResponse(token, user, "Registration successful", true);
        } catch (Exception e) {
            return new LoginResponse("Registration failed: " + e.getMessage(), false);
        }
    }

    public boolean validateToken(String token, String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) return false;
        return jwtService.isTokenValid(token, userOpt.get());
    }

    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}
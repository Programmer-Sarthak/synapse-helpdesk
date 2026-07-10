package com.synapse.helpdesk.controllers;

import com.synapse.helpdesk.dtos.UserLoginDto;
import com.synapse.helpdesk.dtos.UserRegistrationDto;
import com.synapse.helpdesk.dtos.UserResponseDto;
import com.synapse.helpdesk.models.User;
import com.synapse.helpdesk.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> registerNewUser(@Valid @RequestBody UserRegistrationDto dto) {
        return ResponseEntity.ok(userService.registerUser(dto));
    }

    @PostMapping("/login")
    public ResponseEntity<String> loginUser(@Valid @RequestBody UserLoginDto dto) {
        return ResponseEntity.ok(userService.loginUser(dto));
    }

    @GetMapping("/me")
    public ResponseEntity<java.util.Map<String, Object>> getCurrentUser() {
        User user = (User) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();

        java.util.Map<String, Object> userData = new java.util.HashMap<>();
        userData.put("id", user.getId());
        userData.put("name", user.getName());
        userData.put("role", user.getRole());

        return ResponseEntity.ok(userData);
    }
}
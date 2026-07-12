package com.synapse.helpdesk.controllers;

import com.synapse.helpdesk.dtos.UserLoginDto;
import com.synapse.helpdesk.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody UserLoginDto dto) {
        return ResponseEntity.ok(userService.loginUser(dto));
    }
}
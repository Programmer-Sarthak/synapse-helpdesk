package com.synapse.helpdesk.services;

import com.synapse.helpdesk.dtos.UserLoginDto;
import com.synapse.helpdesk.dtos.UserRegistrationDto;
import com.synapse.helpdesk.dtos.UserResponseDto;
import com.synapse.helpdesk.models.User;
import com.synapse.helpdesk.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    private final UserRepository userRepository;

    public UserResponseDto registerUser(UserRegistrationDto dto) {

        if(userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new RuntimeException("Email already taken!");
        }

        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setRole("ROLE_EMPLOYEE");
        user.setCreatedAt(LocalDateTime.now());

        user.setPassword(passwordEncoder.encode(dto.getPassword()));

        User savedUser = userRepository.save(user);

        return mapToResponseDto(savedUser);
    }

    public String loginUser(UserLoginDto dto) {

        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid Credentials"));

        if(passwordEncoder.matches(dto.getPassword(), user.getPassword()))
            return jwtService.generateToken(user);
        else throw new RuntimeException("Invalid Credentials");
    }

    private UserResponseDto mapToResponseDto(User user) {

        UserResponseDto dto = new UserResponseDto();

        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setCreatedAt(user.getCreatedAt());

        return dto;
    }
}


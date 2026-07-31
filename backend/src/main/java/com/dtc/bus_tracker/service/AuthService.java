package com.dtc.bus_tracker.service;

import com.dtc.bus_tracker.dto.LoginResponse;
import com.dtc.bus_tracker.entity.AdminUser;
import com.dtc.bus_tracker.repository.AdminUserRepository;
import com.dtc.bus_tracker.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public LoginResponse login(String username, String password) {
        AdminUser user = adminUserRepository.findByUsername(username)
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid username or password");
        }

        return LoginResponse.builder()
                .token(jwtService.generateToken(user.getUsername()))
                .username(user.getUsername())
                .role("ADMIN")
                .build();
    }
}

package com.dtc.bus_tracker.service;

import com.dtc.bus_tracker.entity.AdminUser;
import com.dtc.bus_tracker.repository.AdminUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminUserSeeder implements CommandLineRunner {

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.default-username}")
    private String defaultUsername;

    @Value("${admin.default-password}")
    private String defaultPassword;

    @Override
    public void run(String... args) {
        if (adminUserRepository.count() > 0) {
            return;
        }
        adminUserRepository.save(AdminUser.builder()
                .username(defaultUsername)
                .passwordHash(passwordEncoder.encode(defaultPassword))
                .build());
        log.warn("Seeded default admin user '{}'. Set ADMIN_USERNAME/ADMIN_PASSWORD env vars "
                + "and rotate this before deploying anywhere real.", defaultUsername);
    }
}

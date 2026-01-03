package com.p2.auth_service;

import com.p2.auth_service.model.AppUser;
import com.p2.auth_service.repository.AppUserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class AuthServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AuthServiceApplication.class, args);
	}


    @Bean
    CommandLineRunner seedData(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (appUserRepository.findByEmail("admin@test.com").isEmpty()) {
                String adminHashedPassword = passwordEncoder.encode("adminpassword1");
                AppUser adminUser = new AppUser(
                        "admin@test.com",
                        adminHashedPassword,
                        "ADMIN",
                        "Admin",
                        "Test"
                );
                appUserRepository.save(adminUser);
            }

            if (appUserRepository.findByEmail("seller@test.com").isEmpty()) {
                String hashedPassword = passwordEncoder.encode("password123");
                AppUser testSeller = new AppUser(
                        "seller@test.com",
                        hashedPassword,
                        "SELLER",
                        "Seller",
                        "Test"
                );
                appUserRepository.save(testSeller);
            }
        };
    }
}

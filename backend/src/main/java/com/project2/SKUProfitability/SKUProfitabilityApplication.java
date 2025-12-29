package com.project2.SKUProfitability;

import com.project2.SKUProfitability.Model.AppUser;
import com.project2.SKUProfitability.Repository.AppUserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class SKUProfitabilityApplication {

	public static void main(String[] args) {
		SpringApplication.run(SKUProfitabilityApplication.class, args);
	}

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    CommandLineRunner seedData(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (appUserRepository.findByEmail("admin@skuprofitability.com").isEmpty()) {
                String adminHashedPassword = passwordEncoder.encode("adminpassword1");
                AppUser adminUser = new AppUser(
                        "admin@skuprofitability.com",
                        adminHashedPassword,
                        "ADMIN",
                        "Admin",
                        "User"
                );
                appUserRepository.save(adminUser);
            }

            if (appUserRepository.findByEmail("test@skuprofitability.com").isEmpty()) {
                String hashedPassword = passwordEncoder.encode("password123");
                AppUser testSeller = new AppUser(
                        "test@skuprofitability.com",
                        hashedPassword,
                        "SELLER",
                        "Test",
                        "User"
                );
                appUserRepository.save(testSeller);
            }
        };
    }
}


package com.project2.SKUProfitability.Service;

import com.project2.SKUProfitability.DTO.AppUserDTO;
import com.project2.SKUProfitability.DTO.RegisterCustomerDTO;
import com.project2.SKUProfitability.Model.AppUser;
import com.project2.SKUProfitability.Repository.AppUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AppUserService {
    private final AppUserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public AppUserService(AppUserRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    public AppUserDTO registerNewCustomer(RegisterCustomerDTO dto) {
        if (repository.findByEmail(dto.email()).isPresent()) {
            throw new IllegalArgumentException("Email already in use.");
        }

        String hashedPassword = passwordEncoder.encode(dto.password());

        AppUser user = new AppUser(
                dto.email(),
                hashedPassword,
                "SELLER",
                dto.firstName(),
                dto.lastName()
        );

        return AppUserToDto(repository.save(user));
    }

    private AppUserDTO AppUserToDto(AppUser user) {
        return new AppUserDTO(
                user.getUserId(),
                user.getEmail(),
                user.getPassword(),
                user.getUserRole(),
                user.getFirstName(),
                user.getLastName()
        );
    }
}

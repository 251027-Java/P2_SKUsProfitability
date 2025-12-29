package com.project2.SKUProfitability.Service;

import com.project2.SKUProfitability.DTO.AppUserDTO;
import com.project2.SKUProfitability.DTO.RegisterCustomerDTO;
import com.project2.SKUProfitability.Model.AppUser;
import com.project2.SKUProfitability.Repository.AppUserRepository;
import com.project2.SKUProfitability.Util.ValidationUtil;
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
        ValidationUtil.validateEmail(dto.email());
        ValidationUtil.validatePassword(dto.password());
        ValidationUtil.validateName(dto.firstName(), "First Name");
        ValidationUtil.validateName(dto.lastName(), "Last Name");
        
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

        return appUserToDto(repository.save(user));
    }

    private AppUserDTO appUserToDto(AppUser user) {
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

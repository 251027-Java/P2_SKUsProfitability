package com.p2.AuthService.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "app_users",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"email"})
        }
)
@Data
@NoArgsConstructor
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = true)
    private String password;

    @Column(name = "user_role", nullable = false)
    private String userRole;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    public AppUser(String email, String password, String userRole, String firstName, String lastName) {
        this.email = email;
        this.password = password;
        this.userRole = userRole;
        this.firstName = firstName;
        this.lastName = lastName;
    }
}

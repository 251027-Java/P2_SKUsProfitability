package com.project2.SKUProfitability.Model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "AppUsers",
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

    @Column(name = "userRole", nullable = false)
    private String userRole;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    // Constructor for creating new users
    public AppUser(String email, String password, String userRole, String firstName, String lastName) {
        this.email = email;
        this.password = password;
        this.userRole = userRole;
        this.firstName = firstName;
        this.lastName = lastName;
    }
}

package com.example.kursinisbackend.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name="users")
@Inheritance(strategy = InheritanceType.JOINED)
public class User {
    public enum Role { CLIENT, OWNER, DRIVER, ADMIN }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    private Role role;

    public User() {}
    public User(String username, String passwordHash, Role role){
        this.username = username; this.passwordHash = passwordHash; this.role = role;
    }

    @Override public String toString(){ return username + " (" + role + ")"; }
}

package com.example.kursinisbackend.repos;

import com.example.kursinisbackend.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
    List<User> findByUsernameContainingIgnoreCase(String username);
    List<User> findByRole(User.Role role);
}

package com.example.kursinisbackend.repos;

import com.example.kursinisbackend.entities.MenuItem;
import com.example.kursinisbackend.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
}

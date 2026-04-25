package com.example.kursinisbackend.repos;

import com.example.kursinisbackend.entities.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
}

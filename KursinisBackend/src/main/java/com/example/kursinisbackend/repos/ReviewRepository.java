package com.example.kursinisbackend.repos;

import com.example.kursinisbackend.entities.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByRestaurantId(Long restaurant_id);

    List<Review> findByDriverId(Integer driverId);

    List<Review> findByClientId(Integer clientId);

    List<Review> findByAuthorId(Integer authorId);
}
package com.example.kursinisbackend.repos;

import com.example.kursinisbackend.entities.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByClient_Id(Long clientId);

    List<Order> findByStatus(Order.Status status);

    List<Order> findByRestaurant_Id(Long restaurantId);

    List<Order> findByDriver_Id(Long driverId);
}
package com.example.kursinisbackend.repos;

import com.example.kursinisbackend.entities.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByOrderId(Long orderId);
}

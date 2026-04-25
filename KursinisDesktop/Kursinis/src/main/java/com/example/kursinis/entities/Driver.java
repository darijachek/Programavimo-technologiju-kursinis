package com.example.kursinis.entities;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
public class Driver extends User {
    private String vehicle;
    private Double rating;
    public Driver(){}
    public Driver(String u, String h){ super(u,h, Role.DRIVER); }


}
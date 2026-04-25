package com.example.kursinisbackend.entities;

import jakarta.persistence.Entity;

@Entity
public class Admin extends User {
    public Admin(){}
    public Admin(String u, String h){ super(u,h, Role.ADMIN); }


}

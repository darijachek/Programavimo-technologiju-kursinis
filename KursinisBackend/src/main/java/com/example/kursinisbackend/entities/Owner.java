package com.example.kursinisbackend.entities;

import jakarta.persistence.Entity;

@Entity
public class Owner extends User {
    public Owner(){}
    public Owner(String u, String h){ super(u,h, Role.OWNER); }
}

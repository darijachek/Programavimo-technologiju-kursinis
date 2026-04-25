package com.example.kursinis.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "owner")
public class Owner extends User {
    public Owner(){}
    public Owner(String u, String h){ super(u,h, Role.OWNER); }
}
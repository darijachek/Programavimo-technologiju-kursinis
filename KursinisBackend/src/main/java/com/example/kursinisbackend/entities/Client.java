package com.example.kursinisbackend.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
public class Client extends User {

    private String address;

    private int loyaltyPoints = 0;

    public Client(){}
    public Client(String u, String h){ super(u,h, Role.CLIENT); }


}

package com.example.kursinisbackend.services;

import java.time.LocalTime;

public class PricingService {
    public static double multiplierNow() {
        LocalTime now = LocalTime.now();
        if (now.isAfter(LocalTime.of(11, 0)) && now.isBefore(LocalTime.of(13, 0))) {
            return 1.2;
        }
        return 1.0;
    }
}

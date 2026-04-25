package com.example.kursinis.services;

public class PricingService {
    public static double multiplierNow(){
        int h = java.time.LocalDateTime.now().getHour();
        if (h >= 17 && h <= 21) return 1.20;
        if (h >= 22 || h <= 6) return 0.90;
        return 1.0;
    }
}


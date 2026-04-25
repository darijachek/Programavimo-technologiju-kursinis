package com.example.kursinis;

import com.example.kursinis.entities.User;
import lombok.Getter;
import lombok.Setter;

public class Session {
    @Getter
    @Setter
    private static User currentUser;

    public static boolean isLoggedIn(){ return currentUser != null; }
}

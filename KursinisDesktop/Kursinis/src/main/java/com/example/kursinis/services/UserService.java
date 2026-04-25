package com.example.kursinis.services;

import com.example.kursinis.dao.GenericDAO;
import com.example.kursinis.entities.User;

public class UserService extends GenericDAO<User> {
    public UserService() { super(User.class); }
}

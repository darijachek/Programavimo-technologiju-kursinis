package com.example.kursinis.services;

import com.example.kursinis.dao.GenericDAO;
import com.example.kursinis.entities.Message;

public class MessageService extends GenericDAO<Message> {
    public MessageService() {
        super(Message.class);
    }
}

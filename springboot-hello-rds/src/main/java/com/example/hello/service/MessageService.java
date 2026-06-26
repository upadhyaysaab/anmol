package com.example.hello.service;

import com.example.hello.entity.Message;
import com.example.hello.repository.MessageRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageService {

    private final MessageRepository repository;

    public MessageService(MessageRepository repository) {
        this.repository = repository;
    }

    public Message save(String text) {
        return repository.save(new Message(text));
    }

    public List<Message> getAll() {
        return repository.findAll();
    }

}

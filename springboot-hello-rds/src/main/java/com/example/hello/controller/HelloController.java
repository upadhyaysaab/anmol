package com.example.hello.controller;

import com.example.hello.entity.Message;
import com.example.hello.service.MessageService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class HelloController {

    private final MessageService service;

    public HelloController(MessageService service) {
        this.service = service;
    }

    @GetMapping("/hello")
    public String hello() {
        return "Hello from Spring Boot!";
    }

    @PostMapping("/message")
    public Message save(@RequestParam String text) {
        return service.save(text);
    }

    @GetMapping("/messages")
    public List<Message> all() {
        return service.getAll();
    }

}

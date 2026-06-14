package com.example.app;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HelloAppTest {

    @Test
    public void testMessage() {

        HelloApp app = new HelloApp();

        assertEquals(
                "Hello Jenkins Integration Test",
                app.getMessage()
        );
    }
}

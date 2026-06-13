package com.example.app;

import org.junit.jupiter.api.Test;
import java.net.*;
import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

public class HelloAppTest {

    @Test
    public void testHelloEndpoint() throws Exception {

        URL url = new URL("http://localhost:8080/hello");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        assertEquals(200, conn.getResponseCode());

        BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream())
        );

        String response = br.readLine();
        br.close();

        assertEquals("Hello Jenkins Integration Test", response);
    }
}

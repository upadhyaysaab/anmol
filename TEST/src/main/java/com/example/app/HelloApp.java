package com.example.app;

public class HelloApp {

    public String getMessage() {
        return "Hello Jenkins Integration Test";
    }

    public static void main(String[] args) throws Exception {

        com.sun.net.httpserver.HttpServer server =
                com.sun.net.httpserver.HttpServer.create(
                        new java.net.InetSocketAddress(8080), 0);

        server.createContext("/hello", exchange -> {

            String response = "Hello Jenkins Integration Test";

            exchange.sendResponseHeaders(200, response.getBytes().length);

            exchange.getResponseBody().write(response.getBytes());

            exchange.close();
        });

        server.start();

        System.out.println("Server started on port 8080");
    }
}

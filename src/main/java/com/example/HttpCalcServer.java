package com.example;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class HttpCalcServer {

    public static void main(String[] args) throws IOException {
        int port = 8080;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/calc", new CalcHandler());
        server.setExecutor(null); // используем дефолтный пул потоков
        server.start();
        System.out.println("HTTP Calculator started on http://localhost:" + port + "/calc");
        System.out.println("Use POST with JSON body: {\"operation\":\"+\",\"a\":10,\"b\":20}");
    }

    static class CalcHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // 1. Проверяем метод
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "{\"status\":\"error\",\"message\":\"Method not allowed. Use POST.\"}");
                return;
            }

            // 2. Читаем тело запроса
            String body = readBody(exchange.getRequestBody());
            if (body == null || body.trim().isEmpty()) {
                sendResponse(exchange, 400, "{\"status\":\"error\",\"message\":\"Empty request body.\"}");
                return;
            }

            // 3. Простой парсинг JSON (без библиотек)
            // Ожидаем: {"operation":"+","a":10,"b":20}
            String operation = extractField(body, "operation");
            String aStr = extractField(body, "a");
            String bStr = extractField(body, "b");

            if (operation == null || aStr == null || bStr == null) {
                sendResponse(exchange, 400, "{\"status\":\"error\",\"message\":\"Invalid JSON format. Expected: {\"operation\":\"...\",\"a\":...,\"b\":...}\"}");
                return;
            }

            int a, b;
            try {
                a = Integer.parseInt(aStr);
                b = Integer.parseInt(bStr);
            } catch (NumberFormatException e) {
                sendResponse(exchange, 400, "{\"status\":\"error\",\"message\":\"a and b must be integers.\"}");
                return;
            }

            // 4. Вызываем логику калькулятора
            int result = 0;
            boolean success = true;
            String errorMessage = null;

            switch (operation) {
                case "+":
                    result = a + b;
                    break;
                case "-":
                    result = a - b;
                    break;
                case "*":
                    result = a * b;
                    break;
                case "/":
                    if (b == 0) {
                        success = false;
                        errorMessage = "Division by zero is not allowed.";
                    } else {
                        result = a / b;
                    }
                    break;
                default:
                    success = false;
                    errorMessage = "Unknown operation. Use +, -, *, /";
            }

            // 5. Формируем JSON-ответ
            String responseJson;
            if (success) {
                responseJson = String.format(
                    "{\"status\":\"success\",\"operation\":\"%s\",\"input_a\":%d,\"input_b\":%d,\"result\":%d}",
                    operation, a, b, result
                );
            } else {
                responseJson = "{\"status\":\"error\",\"message\":\"" + escapeJson(errorMessage) + "\"}";
            }

            sendResponse(exchange, success ? 200 : 400, responseJson);
        }

        // Очень простой «парсер» JSON: ищет значение по ключу
        private String extractField(String json, String key) {
            String prefix = "\"" + key + "\":";
            int start = json.indexOf(prefix);
            if (start == -1) return null;
            start += prefix.length();

            // Пропускаем пробелы
            while (start < json.length() && Character.isWhitespace(json.charAt(start))) {
                start++;
            }
            if (start >= json.length()) return null;

            char first = json.charAt(start);
            if (first == '"') {
                // Строковое значение
                int end = json.indexOf('"', start + 1);
                if (end == -1) return null;
                return json.substring(start + 1, end);
            } else {
                // Числовое значение: читаем до запятой или конца
                int end = start;
                while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '-' || json.charAt(end) == '.')) {
                    end++;
                }
                String val = json.substring(start, end).trim();
                return val.isEmpty() ? null : val;
            }
        }

        private String readBody(java.io.InputStream inputStream) throws IOException {
            StringBuilder sb = new StringBuilder();
            try (Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8.name())) {
                while (scanner.hasNextLine()) {
                    sb.append(scanner.nextLine());
                }
            }
            return sb.toString();
        }

        private void sendResponse(HttpExchange exchange, int statusCode, String responseBody) throws IOException {
            exchange.getResponseHeaders().set("Content-Type", "application/json"); 
	    byte[] bytes = responseBody.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(statusCode, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }

        private String escapeJson(String s) {
            if (s == null) return "";
            return s.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
        }
    }
}

package com.example;

public class LogStressTest {
    public static void main(String[] args) {
        for (int i = 0; i < 20000; i++) {
            SimpleLogger.log("Log entry #" + i);
            if (i % 1000 == 0) {
                System.out.println("Written " + i + " lines");
            }
        }
        System.out.println("Test finished. Check app.log and app.log.1");
    }
}

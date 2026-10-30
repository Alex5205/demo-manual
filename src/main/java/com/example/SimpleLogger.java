package com.example;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SimpleLogger {

    private static final String LOG_FILE = "app.log";
    private static final String ARCHIVE_FILE = "app.log.1";
    private static final long MAX_SIZE_BYTES = 500 * 1024; // 500 КБ

    public static void log(String message) {
        File logFile = new File(LOG_FILE);

        // Если файл уже есть и он больше лимита — делаем ротацию
        if (logFile.exists() && logFile.length() > MAX_SIZE_BYTES) {
            File oldLog = new File(ARCHIVE_FILE);
            // Если старый архив уже есть — удаляем его, чтобы не плодить много файлов
            if (oldLog.exists()) {
                oldLog.delete();
            }
            // Переименовываем текущий лог в архив
            boolean renamed = logFile.renameTo(oldLog);
            if (!renamed) {
                System.err.println("Не удалось переименовать лог-файл!");
                return;
            }
        }

        // Формируем строку с меткой времени
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String line = "[" + timestamp + "] " + message + "\n";

        // Дописываем строку в файл
        try (FileWriter writer = new FileWriter(logFile, true)) {
            writer.write(line);
        } catch (IOException e) {
            System.err.println("Ошибка записи в лог: " + e.getMessage());
        }
    }

    // Простой тест, чтобы быстро проверить работу
    public static void main(String[] args) {
        log("Приложение запущено");
        log("Выполняется операция сложения");
        log("Результат: 10 + 20 = 30");
        log("Операция завершена");

        System.out.println("Лог записан в app.log");
    }
}

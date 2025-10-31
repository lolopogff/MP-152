package ru.mentee.power;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Простой класс для проверки подключения к PostgreSQL.
 * Демонстрирует базовое использование JDBC без дополнительных библиотек.
 */
public class SimpleConnection {

    // Пока хардкодим настройки (в следующем уроке исправим!)
    private static final String URL = "jdbc:postgresql://localhost:5432/mentee_db";
    private static final String USERNAME = "mentee";
    private static final String PASSWORD = "password123";

    public static void main(String[] args) {
        System.out.println("🔍 Тестируем подключение к PostgreSQL...");

        try {
            // Подключаемся к БД
            Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            System.out.println("✅ Соединение установлено!");

            // Выполняем простой запрос
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) as user_count FROM users");

            if (resultSet.next()) {
                int userCount = resultSet.getInt("user_count");
                System.out.println("👥 Пользователей в базе: " + userCount);
            }

            // Получаем версию PostgreSQL
            ResultSet versionResult = statement.executeQuery("SELECT version()");
            if (versionResult.next()) {
                String version = versionResult.getString(1);
                String shortVersion = version.split(" ")[1]; // Только номер версии
                System.out.println("🗄️ Версия PostgreSQL: " + shortVersion);
            }

            // Закрываем соединения
            resultSet.close();
            versionResult.close();
            statement.close();
            connection.close();

            System.out.println("🎯 Все работает! Готов к изучению SQL.");

        } catch (Exception e) {
            System.err.println("❌ Ошибка подключения: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
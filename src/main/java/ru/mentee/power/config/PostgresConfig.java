/* @MENTEE_POWER (C)2025 */
package ru.mentee.power.config;

import java.util.Properties;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PostgresConfig implements DatabaseConfig {

    private final Properties properties;

    public PostgresConfig(Properties properties) {
        this.properties = properties;
        logConfigurationStatus();
    }

    @Override
    public String getUrl() {
        String url = properties.getProperty(DB_URL);
        if (url == null || url.trim().isEmpty()) {
            log.warn("Отсутствует параметр подключения к БД: {}", DB_URL);
            return "jdbc:postgresql://localhost:5432/mentee_db";
        }

        if (!url.startsWith("jdbc:postgresql://")) {
            log.warn(
                    "Некорректный формат URL PostgreSQL: {}. Ожидается:"
                            + " jdbc:postgresql://host:port/database",
                    url);
        }

        return url;
    }

    @Override
    public String getUsername() {
        String username = properties.getProperty(DB_USERNAME);
        if (username == null || username.trim().isEmpty()) {
            log.warn("Отсутствует имя пользователя БД: {}", DB_USERNAME);
            return "postgres";
        }
        return username;
    }

    @Override
    public String getPassword() {
        String password = properties.getProperty(DB_PASSWORD);
        if (password == null || password.trim().isEmpty()) {
            log.warn("Отсутствует пароль БД: {}", DB_PASSWORD);
            return "password";
        }
        return password;
    }

    @Override
    public String getDriver() {
        String driver = properties.getProperty(DB_DRIVER);
        if (driver == null || driver.trim().isEmpty()) {
            return "org.postgresql.Driver";
        }
        return driver;
    }

    @Override
    public boolean getShowSql() {
        String showSql = properties.getProperty(DB_SHOW_SQL);
        if (showSql == null) {
            return true;
        }

        boolean result = parseBoolean(showSql);

        if (result) {
            log.info(
                    "ВНИМАНИЕ: Включено отображение SQL запросов. Не рекомендуется для production"
                            + " окружения");
        }

        return result;
    }


    private void logConfigurationStatus() {
        log.info("Проверка конфигурации PostgreSQL...");

        String url = getUrl();
        String username = getUsername();
        String password = getPassword();
        String driver = getDriver();

        if (url == null || url.trim().isEmpty()) {
            log.warn("Отсутствует параметр: {}. Используется значение по умолчанию", DB_URL);
        } else {
            log.debug("URL базы данных: {}", url);
        }

        if (username == null || username.trim().isEmpty()) {
            log.warn("Отсутствует параметр: {}. Используется значение по умолчанию", DB_USERNAME);
        } else {
            log.debug("Имя пользователя БД: {}", username);
        }

        if (password == null || password.trim().isEmpty()) {
            log.warn("Отсутствует параметр: {}. Используется значение по умолчанию", DB_PASSWORD);
        } else {
            log.debug("Пароль БД установлен (длина: {})", password.length());
        }

        log.debug("Драйвер БД: {}", driver);
        log.debug("Show SQL: {}", getShowSql());

        log.info("Проверка конфигурации PostgreSQL завершена");
    }


    private boolean parseBoolean(String value) {
        if (value == null) {
            return true;
        }

        String normalizedValue = value.trim().toLowerCase();

        switch (normalizedValue) {
            case "true":
            case "1":
            case "yes":
            case "on":
            case "enabled":
            case "enable":
                return true;
            case "false":
            case "0":
            case "no":
            case "off":
            case "disabled":
            case "disable":
                return false;
            default:
                log.warn(
                        "Некорректное булево значение '{}' для параметра {}. Будет использовано"
                                + " значение по умолчанию: true",
                        value,
                        DB_SHOW_SQL);
                return true;
        }
    }


    public void printConfigurationSummary() {
        log.info("=== Конфигурация PostgreSQL ===");
        log.info("URL: {}", getUrl());
        log.info("Username: {}", getUsername());
        log.info("Password: {}", getPassword() != null ? "***" : "не установлен");
        log.info("Driver: {}", getDriver());
        log.info("Show SQL: {}", getShowSql());
        log.info("=================================");
    }


    public boolean isValid() {
        String url = getUrl();
        String username = getUsername();
        String password = getPassword();

        boolean isValid =
                url != null
                        && !url.trim().isEmpty()
                        && username != null
                        && !username.trim().isEmpty()
                        && password != null
                        && !password.trim().isEmpty();

        if (!isValid) {
            log.warn(
                    "Конфигурация PostgreSQL невалидна. Проверьте наличие всех обязательных"
                            + " параметров.");
        }

        return isValid;
    }


    public boolean hasAllRequiredParameters() {
        return getUrl() != null
                && !getUrl().trim().isEmpty()
                && getUsername() != null
                && !getUsername().trim().isEmpty()
                && getPassword() != null
                && !getPassword().trim().isEmpty();
    }
}

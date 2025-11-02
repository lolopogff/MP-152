/* @MENTEE_POWER (C)2025 */
package ru.mentee.power.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ApplicationConfig implements DatabaseConfig, Overridable, Fileable {
    public static final String APP_NAME = "app.name";

    private final DatabaseConfig dbConfig;
    private final Properties properties;
    private final SecureValidator validator;

    public ApplicationConfig(Properties properties, ConfigFilePath configFilePath)
            throws IOException {
        this.properties = properties;

        load(configFilePath.getAppMainConfigPath());

        this.validator = new SecureValidator(properties, false);
        validator.checkPasswordsInPropertiesOnly();

        loadIfExists(configFilePath.getAppSecretPath());

        override();

        this.dbConfig = new PostgresConfig(properties);

        validator.validateFinalConfig();

        log.info("Конфигурация успешно загружена");
    }

    public String getApplicationName() {
        return properties.getProperty(APP_NAME);
    }

    public String getUrl() {
        return dbConfig.getUrl();
    }

    public String getUsername() {
        return dbConfig.getUsername();
    }

    public String getPassword() {
        return dbConfig.getPassword();
    }

    public String getDriver() {
        return dbConfig.getDriver();
    }

    public boolean getShowSql() {
        return dbConfig.getShowSql();
    }

    @Override
    public void load(String pathProperties) throws IOException {
        try (InputStream input = getClass().getResourceAsStream(pathProperties)) {
            if (input == null) {
                throw new IOException("Файл не найден: %s".formatted(pathProperties));
            }
            properties.load(input);
            log.info("Загружен файл конфигурации: {}", pathProperties);
        }
    }


    private void loadIfExists(String pathProperties) {
        try (InputStream input = getClass().getResourceAsStream(pathProperties)) {
            if (input != null) {
                properties.load(input);
                log.info("Загружен файл конфигурации: {}", pathProperties);
            } else {
                log.warn("Файл конфигурации не найден: {}", pathProperties);
            }
        } catch (IOException e) {
            log.warn("Ошибка при загрузке файла {}: {}", pathProperties, e.getMessage());
        }
    }

    @Override
    public void override() {
        overrideFromEnv(DB_URL, "URL базы данных");
        overrideFromEnv(DB_USERNAME, "имя пользователя");
        overrideFromEnv(DB_PASSWORD, "пароль");
        overrideFromEnv(DB_DRIVER, "драйвер");
    }

    private void overrideFromEnv(String propertyName, String description) {
        String envValue = System.getenv(propertyName);
        if (envValue != null && !envValue.trim().isEmpty()) {
            properties.setProperty(propertyName, envValue);
            log.info("{} переопределен из Environment Variable", description);
        }
    }
}
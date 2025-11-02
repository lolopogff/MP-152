/* @MENTEE_POWER (C)2025 */
package ru.mentee.power.config;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import ru.mentee.power.exception.SASTException;

@Slf4j
public class SecureValidator {
    private final Properties properties;
    private final Set<String> weakPasswords;
    private final Set<String> sensitiveProperties;
    @Getter private int vulnerabilityCount;
    private final boolean allowPasswordsInProperties;

    public SecureValidator(Properties properties) {
        this(properties, true);
    }

    public SecureValidator(Properties properties, boolean allowPasswordsInProperties) {
        this.properties = properties;
        this.weakPasswords = initializeWeakPasswords();
        this.sensitiveProperties = initializeSensitiveProperties();
        this.vulnerabilityCount = 0;
        this.allowPasswordsInProperties = allowPasswordsInProperties;
    }

    public void validate() {
        log.info("Запуск проверки безопасности конфигурации...");
        vulnerabilityCount = 0;

        if (!allowPasswordsInProperties) {
            checkPasswordsInProperties();
        }

        checkWeakPasswords();

        checkOtherVulnerabilities();

        if (vulnerabilityCount > 0) {
            throw SASTException.forMultipleVulnerabilities(vulnerabilityCount);
        }

        log.info("Проверка безопасности завершена успешно");
    }


    public void validateMainConfig() {
        log.info("Валидация основных настроек конфигурации...");
        vulnerabilityCount = 0;

        checkPasswordsInProperties();

        checkWeakPasswords();

        checkRequiredParameters();

        if (vulnerabilityCount > 0) {
            throw SASTException.forMultipleVulnerabilities(vulnerabilityCount);
        }

        log.info("Валидация основных настроек завершена успешно");
    }

    private void checkPasswordsInProperties() {
        for (String propertyName : properties.stringPropertyNames()) {
            String propertyValue = properties.getProperty(propertyName);

            if (isSensitiveProperty(propertyName)
                    && propertyValue != null
                    && !propertyValue.trim().isEmpty()) {
                log.error(
                        "НАРУШЕНИЕ БЕЗОПАСНОСТИ: Чувствительный параметр '{}' найден в properties"
                                + " файле. Рекомендуется использовать environment variables или"
                                + " secret.properties",
                        propertyName);
                vulnerabilityCount++;

                if (propertyName.toLowerCase().contains("password")) {
                    throw SASTException.forPasswordInProperties(propertyName);
                }
            }
        }
    }

    private void checkWeakPasswords() {
        for (String propertyName : properties.stringPropertyNames()) {
            if (propertyName.toLowerCase().contains("password")) {
                String password = properties.getProperty(propertyName);

                if (password != null && weakPasswords.contains(password.toLowerCase().trim())) {
                    log.error(
                            "КРИТИЧЕСКОЕ НАРУШЕНИЕ: Обнаружен слабый пароль в свойстве '{}'",
                            propertyName);
                    throw SASTException.forWeakPassword(propertyName, password);
                }
            }
        }
    }

    private void checkOtherVulnerabilities() {
        String showSql = properties.getProperty("db.showSql");
        if ("true".equalsIgnoreCase(showSql)) {
            log.warn("ВНИМАНИЕ: Включен показ SQL запросов. Не рекомендуется для production");
        }

        String password = properties.getProperty("db.password");
        if (password == null || password.trim().isEmpty()) {
            log.error("КРИТИЧЕСКОЕ НАРУШЕНИЕ: Пароль базы данных не установлен");
            vulnerabilityCount++;
            throw SASTException.forMissingRequiredParameter("db.password");
        }

        checkRequiredParameters();
    }

    private void checkRequiredParameters() {
        String[] requiredParams = {"db.url", "db.username", "db.password", "db.driver", "app.name"};

        for (String param : requiredParams) {
            String value = properties.getProperty(param);
            if (value == null || value.trim().isEmpty()) {
                log.error("КРИТИЧЕСКОЕ НАРУШЕНИЕ: Отсутствует обязательный параметр: {}", param);
                vulnerabilityCount++;
                throw SASTException.forMissingRequiredParameter(param);
            }
        }
    }

    private boolean isSensitiveProperty(String propertyName) {
        String lowerName = propertyName.toLowerCase();
        return lowerName.contains("password")
                || lowerName.contains("secret")
                || lowerName.contains("key")
                || lowerName.contains("token")
                || lowerName.contains("credential")
                || lowerName.contains("auth");
    }

    private String maskPassword(String password) {
        if (password == null || password.length() <= 2) {
            return "***";
        }
        return password.charAt(0) + "***" + password.charAt(password.length() - 1);
    }


    public void checkPasswordsInPropertiesOnly() {
        log.info("Проверка паролей в основном конфигурационном файле...");
        vulnerabilityCount = 0;
        checkPasswordsInProperties();

        if (vulnerabilityCount > 0) {
            throw SASTException.forMultipleVulnerabilities(vulnerabilityCount);
        }

        log.info("Проверка паролей завершена");
    }


    public void validateFinalConfig() {
        log.info("Финальная проверка конфигурации...");
        vulnerabilityCount = 0;

        checkWeakPasswords();

        checkOtherVulnerabilities();

        checkRequiredParameters();

        if (vulnerabilityCount > 0) {
            throw SASTException.forMultipleVulnerabilities(vulnerabilityCount);
        }

        log.info("Финальная проверка завершена успешно");
    }

    private Set<String> initializeWeakPasswords() {
        Set<String> weak = new HashSet<>();
        weak.add("password");
        weak.add("123456");
        weak.add("admin");
        weak.add("qwerty");
        weak.add("letmein");
        weak.add("welcome");
        weak.add("monkey");
        weak.add("12345678");
        weak.add("123456789");
        weak.add("1234");
        weak.add("12345");
        weak.add("pass");
        weak.add("secret");
        weak.add("test");
        weak.add("temp");
        weak.add("root");
        weak.add("guest");
        weak.add("123");
        weak.add("111111");
        weak.add("123123");
        return weak;
    }

    private Set<String> initializeSensitiveProperties() {
        Set<String> sensitive = new HashSet<>();
        sensitive.add("password");
        sensitive.add("secret");
        sensitive.add("key");
        sensitive.add("token");
        sensitive.add("credential");
        sensitive.add("auth");
        sensitive.add("private");
        sensitive.add("secure");
        return sensitive;
    }
}

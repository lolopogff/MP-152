/* @MENTEE_POWER (C)2025 */
package ru.mentee.power.exception;

/**
 * Исключение для уязвимостей безопасности, обнаруженных SAST (Static Application Security Testing)
 * Наследуется от RuntimeException для unchecked исключений
 */
public class SASTException extends RuntimeException {

    private String vulnerabilityType;
    private String severity;

    /**
     * Базовый конструктор с сообщением об уязвимости
     * @param message детальное описание уязвимости
     */
    public SASTException(String message) {
        super(message);
        this.vulnerabilityType = extractVulnerabilityType(message);
        this.severity = determineSeverity(message);
    }

    /**
     * Конструктор с сообщением и причиной исключения
     * @param message детальное описание уязвимости
     * @param cause первоначальное исключение
     */
    public SASTException(String message, Throwable cause) {
        super(message, cause);
        this.vulnerabilityType = extractVulnerabilityType(message);
        this.severity = determineSeverity(message);
    }

    /**
     * Приватный конструктор для фабричных методов
     */
    private SASTException(String message, String vulnerabilityType, String severity) {
        super(message);
        this.vulnerabilityType = vulnerabilityType;
        this.severity = severity;
    }

    // Фабричные методы

    /**
     * Создает исключение для слабых паролей
     * @param propertyName имя свойства с паролем
     * @param password слабый пароль (будет замаскирован)
     */
    public static SASTException forWeakPassword(String propertyName, String password) {
        String message = createWeakPasswordMessage(propertyName, password);
        return new SASTException(message, "WEAK_PASSWORD", "CRITICAL");
    }

    /**
     * Создает исключение для паролей в properties файлах
     * @param propertyName имя свойства с паролем
     */
    public static SASTException forPasswordInProperties(String propertyName) {
        String message = createPasswordInPropertiesMessage(propertyName);
        return new SASTException(message, "PASSWORD_IN_PROPERTIES", "HIGH");
    }

    /**
     * Создает исключение с конкретным типом уязвимости
     * @param vulnerabilityType тип уязвимости
     * @param message детальное описание
     * @param severity уровень серьезности
     */
    public static SASTException withType(
            String vulnerabilityType, String message, String severity) {
        String formattedMessage = formatMessage(vulnerabilityType, message, severity);
        return new SASTException(formattedMessage, vulnerabilityType, severity);
    }

    /**
     * Создает исключение для отсутствия SSL соединения
     * @param url URL базы данных
     */
    public static SASTException forInsecureConnection(String url) {
        String message =
                String.format(
                        "Обнаружено небезопасное соединение с базой данных: %s. Отсутствует SSL"
                                + " шифрование. Рекомендуется включить SSL для защиты передаваемых"
                                + " данных.",
                        url);
        return new SASTException(message, "INSECURE_CONNECTION", "HIGH");
    }

    /**
     * Создает исключение для включенного debug режима
     * @param propertyName имя свойства
     */
    public static SASTException forDebugModeEnabled(String propertyName) {
        String message =
                String.format(
                        "Обнаружен включенный debug режим в свойстве '%s'. Не рекомендуется для"
                            + " production окружения. Может раскрыть конфиденциальную информацию.",
                        propertyName);
        return new SASTException(message, "DEBUG_MODE_ENABLED", "MEDIUM");
    }

    /**
     * Создает исключение для отсутствующих обязательных параметров
     * @param parameterName имя обязательного параметра
     */
    public static SASTException forMissingRequiredParameter(String parameterName) {
        String message =
                String.format(
                        "Отсутствует обязательный параметр конфигурации: '%s'. Это может привести к"
                                + " неработоспособности приложения или уязвимостям безопасности.",
                        parameterName);
        return new SASTException(message, "MISSING_REQUIRED_PARAMETER", "HIGH");
    }

    /**
     * Создает исключение для множественных уязвимостей
     * @param vulnerabilityCount количество обнаруженных уязвимостей
     */
    public static SASTException forMultipleVulnerabilities(int vulnerabilityCount) {
        String message =
                String.format(
                        "Обнаружено %d уязвимостей безопасности в конфигурации. "
                                + "Проверьте логи для детальной информации о каждой уязвимости.",
                        vulnerabilityCount);
        return new SASTException(message, "MULTIPLE_VULNERABILITIES", "HIGH");
    }

    public String getVulnerabilityType() {
        return vulnerabilityType;
    }

    public String getSeverity() {
        return severity;
    }

    @Override
    public String toString() {
        return String.format(
                "SASTException[vulnerabilityType=%s, severity=%s]: %s",
                vulnerabilityType, severity, getMessage());
    }

    // Вспомогательные методы для форматирования сообщений

    private static String formatMessage(String vulnerabilityType, String message, String severity) {
        return String.format("[%s] %s (Severity: %s)", vulnerabilityType, message, severity);
    }

    private static String createWeakPasswordMessage(String propertyName, String password) {
        String maskedPassword = maskPassword(password);
        return String.format(
                "[WEAK_PASSWORD] Обнаружен слабый пароль в свойстве '%s'. Пароль: '%s'."
                    + " Использование слабых паролей подвергает систему риску взлома. Рекомендуется"
                    + " использовать сложные пароли длиной не менее 12 символов с комбинацией букв,"
                    + " цифр и специальных символов. (Severity: CRITICAL)",
                propertyName, maskedPassword);
    }

    private static String createPasswordInPropertiesMessage(String propertyName) {
        return String.format(
                "[PASSWORD_IN_PROPERTIES] Чувствительный параметр '%s' обнаружен в properties"
                    + " файле. Хранение паролей и секретов в конфигурационных файлах небезопасно."
                    + " Рекомендуется использовать environment variables, секретные хранилища или"
                    + " специализированные сервисы управления секретами. (Severity: HIGH)",
                propertyName);
    }

    private static String maskPassword(String password) {
        if (password == null || password.length() <= 2) {
            return "***";
        }
        return password.charAt(0) + "***" + password.charAt(password.length() - 1);
    }

    private String extractVulnerabilityType(String message) {
        if (message.contains("слабый пароль") || message.contains("WEAK_PASSWORD")) {
            return "WEAK_PASSWORD";
        } else if (message.contains("properties файл")
                || message.contains("PASSWORD_IN_PROPERTIES")) {
            return "PASSWORD_IN_PROPERTIES";
        } else if (message.contains("SSL")
                || message.contains("шифрование")
                || message.contains("небезопасное соединение")) {
            return "INSECURE_CONNECTION";
        } else if (message.contains("debug")
                || message.contains("showSql")
                || message.contains("debug режим")) {
            return "DEBUG_MODE_ENABLED";
        } else if (message.contains("отсутствует обязательный параметр")
                || message.contains("MISSING_REQUIRED_PARAMETER")) {
            return "MISSING_REQUIRED_PARAMETER";
        } else if (message.contains("множественных уязвимостей")
                || message.contains("MULTIPLE_VULNERABILITIES")) {
            return "MULTIPLE_VULNERABILITIES";
        } else {
            return "SECURITY_VULNERABILITY";
        }
    }

    private String determineSeverity(String message) {
        if (message.contains("CRITICAL")
                || message.contains("критический")
                || message.contains("слабый пароль")) {
            return "CRITICAL";
        } else if (message.contains("HIGH")
                || message.contains("высокий")
                || message.contains("пароль в файле")
                || message.contains("обязательный параметр")) {
            return "HIGH";
        } else if (message.contains("MEDIUM")
                || message.contains("средний")
                || message.contains("debug режим")) {
            return "MEDIUM";
        } else if (message.contains("LOW") || message.contains("низкий")) {
            return "LOW";
        } else {
            return "MEDIUM";
        }
    }
}

/* @MENTEE_POWER (C)2025 */
package ru.mentee.power.config;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.util.Properties;
import org.junit.jupiter.api.Test;
import ru.mentee.power.exception.SASTException;

class ApplicationConfigTest {

    @Test
    void shouldThrowError() throws IOException {
        assertThatThrownBy(
                        () ->
                                new ApplicationConfig(
                                        new Properties(),
                                        new ConfigFilePath(
                                                "/application-with-secret.properties",
                                                "/secret.properties")))
                .isInstanceOf(SASTException.class)
                .hasMessageContaining(
                        "[PASSWORD_IN_PROPERTIES] Чувствительный параметр 'db.password' обнаружен в properties файле. Хранение паролей и секретов в конфигурационных файлах небезопасно. Рекомендуется использовать environment variables, секретные хранилища или специализированные сервисы управления секретами. (Severity: HIGH)");
    }

    @Test
    void shouldHasProperties() throws IOException {
        ApplicationConfig databaseConfig =
                new ApplicationConfig(new Properties(), new ConfigFilePath());
        assertThat(databaseConfig.getPassword()).isNotNull();
        assertThat(databaseConfig.getUsername()).isNotNull();
        assertThat(databaseConfig.getUrl()).isNotNull();
        assertThat(databaseConfig.getShowSql()).isTrue();
    }

    @Test
    void shouldExistWithoutSecret() {
        assertThatCode(
                        () ->
                                new ApplicationConfig(
                                        new Properties(),
                                        new ConfigFilePath(
                                                "/application.properties",
                                                "/fake-secret.properties")))
                .doesNotThrowAnyException();
    }
}

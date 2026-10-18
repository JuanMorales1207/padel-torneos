
package ar.com.padel.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class PropertiesLoader {

    private static final String EXTERNAL_CONFIG =
            "config/db.properties";

    private static final String INTERNAL_CONFIG =
            "application.properties";

    private static Properties properties;

    private PropertiesLoader() {
    }

    public static synchronized Properties getProperties() {
        if (properties == null) {
            properties = loadProperties();
        }

        return properties;
    }

    private static Properties loadProperties() {
        Properties props = new Properties();
        Path externalPath = Path.of(EXTERNAL_CONFIG);

        // 1. Priorizar la configuración externa.
        if (Files.exists(externalPath)) {
            try (InputStream input =
                    Files.newInputStream(externalPath)) {
                props.load(input);
                return props;
            } catch (IOException e) {
                throw new IllegalStateException(
                        "No se pudo leer " + EXTERNAL_CONFIG, e);
            }
        }

        // 2. Si no existe la externa, buscar dentro del JAR.
        try (InputStream input =
                PropertiesLoader.class.getClassLoader()
                        .getResourceAsStream(INTERNAL_CONFIG)) {

            if (input == null) {
                throw new IllegalStateException(
                        "No se encontró " + EXTERNAL_CONFIG
                        + " ni " + INTERNAL_CONFIG);
            }

            props.load(input);
            return props;

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Error al cargar " + INTERNAL_CONFIG, e);
        }
    }
}

package ar.com.padel.config;

import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;

public final class DataSourceProvider {

    private static BasicDataSource dataSource;

    private DataSourceProvider() {}

    public static synchronized DataSource getDataSource() {
        if (dataSource == null) {
            Properties props = PropertiesLoader.getProperties();

            String url = requireProperty(props, "db.url");
            String username = requireProperty(props, "db.user");
            String password = requireProperty(props, "db.password");

            BasicDataSource ds = new BasicDataSource();

            ds.setDriverClassName("com.mysql.cj.jdbc.Driver");
            ds.setUrl(url);
            ds.setUsername(username);
            ds.setPassword(password);

            // Configuración del pool.
            ds.setInitialSize(getIntProperty(props, "db.pool.initialSize", 2));
            ds.setMaxTotal(getIntProperty(props, "db.pool.maxTotal", 10));
            ds.setMaxIdle(getIntProperty(props, "db.pool.maxIdle", 5));
            ds.setMinIdle(getIntProperty(props, "db.pool.minIdle", 1));
            dataSource = ds;
        }
        return dataSource;
    }

    private static String requireProperty(Properties props, String key) {
        String value = props.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Falta configurar la propiedad: " + key);
        }
        return value.trim();
    }

    private static int getIntProperty(Properties props, String key, int defaultValue) {
        String value = props.getProperty(key);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }

        try {
            int number = Integer.parseInt(value.trim());
            if (number < 0) {
                throw new IllegalArgumentException("El valor de " + key + " no puede ser negativo");
            }
            return number;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Valor numérico inválido para " + key, e);
        }
    }
}

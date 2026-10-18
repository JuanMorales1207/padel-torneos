
package ar.com.padel.test;

import java.sql.Connection;
import javax.sql.DataSource;

import ar.com.padel.config.DataSourceProvider;

public class DatabaseConnectionTest {

    public static void main(String[] args) {
        DataSource dataSource = DataSourceProvider.getDataSource();

        try (Connection connection = dataSource.getConnection()) {
            System.out.println("Conexion exitosa con MySQL.");
            System.out.println("Base de datos: "
                    + connection.getCatalog());
            System.out.println("Servidor: "
                    + connection.getMetaData().getDatabaseProductName());
        } catch (Exception e) {
            System.err.println("No se pudo conectar con MySQL.");
            e.printStackTrace();
        }
    }
}

package ar.com.padel.controller.menu;


import javafx.scene.control.Alert;
import javafx.stage.FileChooser;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

import ar.com.padel.config.PropertiesLoader;


public class MenuSuperController extends MenuAdminController {

	@FXML
	private Label lblBackupDB;

	@Override
	@FXML
	public void initialize() {
		super.initialize();

		// La opción de backup se conectará cuando
		// se impleme la funcionalidad de respaldo.
	}


@FXML
private void onBackupDatabase() {
    File errorFile = null;

    try {
    	setActive(lblBackupDB);
        // Recuperar la configuración de conexión.
        Properties props = PropertiesLoader.getProperties();

        String url = props.getProperty("db.url");
        String dbUser = props.getProperty("db.user");
        String dbPass = props.getProperty("db.password");

        if (url == null || dbUser == null || dbPass == null) {
            throw new IllegalStateException(
                    "Faltan propiedades de conexión en la configuración."
            );
        }

        // Obtener host, puerto y nombre de la base de datos.
        URI uri = URI.create(url.substring("jdbc:".length()));

        String host = uri.getHost();
        int port = uri.getPort() == -1 ? 3306 : uri.getPort();
        String dbName = uri.getPath().replaceFirst("^/", "");

        if (host == null || dbName.isBlank()) {
            throw new IllegalArgumentException(
                    "La URL de conexión no tiene un formato válido."
            );
        }

        // Nombre automático del archivo.
        String fileName = dbName + "_backup_" +
                LocalDateTime.now().format(
                        DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")
                ) + ".sql";

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Guardar copia de seguridad");
        chooser.setInitialFileName(fileName);
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("SQL Database", "*.sql")
        );

        File file = chooser.showSaveDialog(
                lblBackupDB.getScene().getWindow()
        );

        if (file == null) {
            return;
        }

        // Archivo temporal para capturar errores de mysqldump.
        errorFile = File.createTempFile("padelcito_backup_error", ".log");

        ProcessBuilder pb = new ProcessBuilder(
        		"C:\\Program Files\\MariaDB 12.1\\bin\\mariadb-dump.exe",
                "--host=" + host,
                "--port=" + port,
                "--user=" + dbUser,
                "--default-character-set=utf8mb4",
                "--single-transaction",
                "--routines",
                "--triggers",
                "--events",
                "--add-drop-table",
                "--databases",
                dbName
        );

        // La contraseña no se expone como argumento del proceso.
        pb.environment().put("MYSQL_PWD", dbPass);

        pb.redirectOutput(file);
        pb.redirectError(errorFile);

        Process process = pb.start();

        int exitCode = process.waitFor();

        if (exitCode != 0 || !file.exists() || file.length() == 0) {
            String error = Files.readString(errorFile.toPath());

            throw new RuntimeException(
                    "Falló la generación del backup.\n" + error
            );
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Copia de seguridad");
        alert.setHeaderText(null);
        alert.setContentText(
                "Backup generado correctamente.\n\n" +
                file.getAbsolutePath()
        );
        alert.showAndWait();

    } catch (Exception e) {
        e.printStackTrace();

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Copia de seguridad");
        alert.setHeaderText("No se pudo generar el backup.");
        alert.setContentText(e.getMessage());
        alert.showAndWait();

    } finally {
        if (errorFile != null) {
            try {
                Files.deleteIfExists(errorFile.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

}

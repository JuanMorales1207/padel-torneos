package ar.com.padel.controller.menu;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;

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
    private void onBackupDatabase(MouseEvent event) {
        // Pendiente: implementar el backup de la db padel_torneos.
    }
}

package ar.com.padel.controller.content.dialog;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class AboutDialogController {

    @FXML
    private Button btnClose;

    @FXML
    private void onClose() {
        Stage stage = (Stage) btnClose.getScene().getWindow();
        stage.close();
    }
}

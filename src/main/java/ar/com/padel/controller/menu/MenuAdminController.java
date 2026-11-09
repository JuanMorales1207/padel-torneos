package ar.com.padel.controller.menu;

import java.io.IOException;
import java.util.List;

import ar.com.padel.controller.content.dialog.ModifyUserDialogController;
import ar.com.padel.security.context.SessionContext;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class MenuAdminController extends MenuBaseController {

    @FXML private Label lblTorneos;
    @FXML private Label lblPartidos;
    @FXML private Label lblParejas;
    @FXML private Label lblJugadores;
    @FXML private Label lblInscripciones;
    @FXML private Label lblEstadisticas;
    @FXML private Label lblEmpleados;
    @FXML private Label lblReportes;
    @FXML private Label lblAcercaDe;
    @FXML private Label lblUser;
    @FXML private Button btnModifyUser;

    @FXML
    public void initialize() {
        initializeMenu(lblUser, lblTorneos, lblPartidos, lblJugadores, lblParejas, lblInscripciones, 
        		lblEstadisticas, lblEmpleados, lblReportes, lblAcercaDe);
    }

    @FXML
    private void onTorneos(MouseEvent event) {
    	mainLayoutController.setContentWithBase("Torneos", "/fxml/Tournament.fxml");
        setActive(lblTorneos);
    }

    @FXML
    private void onPartidos(MouseEvent event) {
    	mainLayoutController.setContentWithBase("Partidos", "/fxml/TournamentMatches.fxml");
        setActive(lblPartidos);
    }

    @FXML
    private void onJugadores() {
        mainLayoutController.setContentWithBase("Jugadores", "/fxml/Players.fxml");
        setActive(lblJugadores);
    }
    
    @FXML
    private void onParejas() {
        mainLayoutController.setContentWithBase("Parejas", "/fxml/Couples.fxml");
        setActive(lblParejas);
    }

    @FXML
    private void onInscripciones(MouseEvent event) {
    	mainLayoutController.setContentWithBase("Inscripciones", "/fxml/TournamentRegistration.fxml");
        setActive(lblInscripciones);
    }

    @FXML
    private void onEstadisticas(MouseEvent event) {
        mainLayoutController.setContentWithBase("Estadísticas", "/fxml/Statistics.fxml");
        setActive(lblEstadisticas);
    }

    @FXML
    private void onEmpleados(MouseEvent event) {
    	mainLayoutController.setContentWithBase("Empleados", "/fxml/Employees.fxml");
        setActive(lblEmpleados);
    }

    @FXML
    private void onReportes(MouseEvent event) {
    	mainLayoutController.setContentWithBase("Reportes", "/fxml/ComingSoon.fxml");
        setActive(lblReportes);
    }

    @FXML
    private void onAcercaDe() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/dialog/AcercaDe.fxml")
            );

            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Acerca de");
            stage.setScene(new Scene(root));
            stage.initOwner(lblAcercaDe.getScene().getWindow());
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            setActive(lblAcercaDe);
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            showError("No se pudo abrir la ventana Acerca de.");
        }
    }

    @FXML
    private void onModifyUser() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/dialog/ModifyUserDialog.fxml")
            );

            Parent root = loader.load();

            ModifyUserDialogController controller = loader.getController();

            // Pasamos el usuario que inició sesión.
            controller.setEmployee(SessionContext.getLoggedUser());

            Stage stage = new Stage();
            stage.setWidth(640);
            stage.setHeight(570);
            stage.setResizable(false);
            stage.setTitle("Mi cuenta");
            stage.setScene(new Scene(root));

            // La ventana se abre asociada al menú actual.
            stage.initOwner(btnModifyUser.getScene().getWindow());
            stage.initModality(Modality.APPLICATION_MODAL);

            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            showError("No se pudo abrir la ventana Mi cuenta.");
        }
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Mi cuenta");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

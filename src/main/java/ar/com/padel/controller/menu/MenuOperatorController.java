package ar.com.padel.controller.menu;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;

public class MenuOperatorController extends MenuBaseController {

    @FXML private Label lblTorneos;
    @FXML private Label lblPartidos;
    @FXML private Label lblJugadores;
    @FXML private Label lblInscripciones;
    @FXML private Label lblEstadisticas;
    @FXML private Label lblAcercaDe;
    @FXML private Label lblUser;

    @FXML
    public void initialize() {
        initializeMenu(lblUser, lblTorneos, lblPartidos, lblJugadores, 
        		lblInscripciones, lblEstadisticas, lblAcercaDe);
    }

    @FXML
    private void onTorneos(MouseEvent event) {
        // TODO: conectar con la pantalla de torneos.
        setActive(lblTorneos);
    }

    @FXML
    private void onPartidos(MouseEvent event) {
        // TODO: conectar con la pantalla de partidos.
        setActive(lblPartidos);
    }

    @FXML
    private void onJugadores() {
        mainLayoutController.setContentWithBase("Jugadores", "/fxml/Players.fxml");
        setActive(lblJugadores);
    }

    @FXML
    private void onInscripciones(MouseEvent event) {
        // TODO: conectar con la pantalla de inscripciones.
        setActive(lblInscripciones);
    }

    @FXML
    private void onEstadisticas(MouseEvent event) {
        // TODO: conectar con la pantalla de estadísticas.
        setActive(lblEstadisticas);
    }

    @FXML
    private void onAcercaDe(MouseEvent event) {
        // TODO: implementar la ventana Acerca de.
        setActive(lblAcercaDe);
    }

    @FXML
    private void onModifyUser() {
        // TODO: implementar la pantalla Mi cuenta.
    }
}

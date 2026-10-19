package ar.com.padel.controller.menu;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;

public class MenuOperatorController extends MenuBaseController {

    @FXML private Label lblTorneos;
    @FXML private Label lblPartidos;
    @FXML private Label lblJugadores;
    @FXML private Label lblParejas;
    @FXML private Label lblInscripciones;
    @FXML private Label lblEstadisticas;
    @FXML private Label lblAcercaDe;
    @FXML private Label lblUser;

    @FXML
    public void initialize() {
        initializeMenu(lblUser, lblTorneos, lblPartidos, lblJugadores, lblParejas,
        		lblInscripciones, lblEstadisticas, lblAcercaDe);
    }

    @FXML
    private void onTorneos(MouseEvent event) {
    	mainLayoutController.setContentWithBase("Torneos", "/fxml/ComingSoon.fxml");
        setActive(lblTorneos);
    }

    @FXML
    private void onPartidos(MouseEvent event) {
    	mainLayoutController.setContentWithBase("Partidos", "/fxml/ComingSoon.fxml");
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
    	mainLayoutController.setContentWithBase("Inscripciones", "/fxml/ComingSoon.fxml");
        setActive(lblInscripciones);
    }

    @FXML
    private void onEstadisticas(MouseEvent event) {
        mainLayoutController.setContentWithBase("Estadísticas", "/fxml/ComingSoon.fxml");
        setActive(lblEstadisticas);
    }

    @FXML
    private void onAcercaDe(MouseEvent event) {
    	mainLayoutController.setContentWithBase("Acerca de", "/fxml/ComingSoon.fxml");
        setActive(lblAcercaDe);
    }

    @FXML
    private void onModifyUser() {
        //TODO: implementar la pantalla Mi cuenta.
    	mainLayoutController.setContentWithBase("Mi Cuenta", "/fxml/ComingSoon.fxml");
    }
}

package ar.com.padel.controller.menu;

import java.util.Arrays;
import java.util.List;

import ar.com.padel.controller.MainLayoutController;
import ar.com.padel.security.context.SessionContext;
import ar.com.padel.ui.SceneManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public abstract class MenuBaseController {

    protected MainLayoutController mainLayoutController;
    private List<Label> menuItems;

    /**
     * Recibe la referencia al layout principal para poder
     * cambiar el contenido central desde el menú.
     */
    public void setMainLayoutController(
            MainLayoutController controller) {
        this.mainLayoutController = controller;
    }

    /**
     * Inicializa los elementos comunes de los menús.
     */
    protected void initializeMenu(
            Label lblUser, Label... items) {

        menuItems = Arrays.asList(items);

        if (SessionContext.getLoggedUser() != null) {
            lblUser.setText("👤 " + SessionContext.getLoggedUser().getFullName());
        } else {
            lblUser.setText("Usuario");
        }
    }

    /**
     * Marca una opción como seleccionada.
     */
    protected void setActive(Label selected) {
        if (menuItems == null || selected == null) {
            return;
        }

        for (Label item : menuItems) {
            item.getStyleClass().remove("menu-button-active");
        }

        if (!selected.getStyleClass().contains("menu-button-active")) {
            selected.getStyleClass().add("menu-button-active");
        }
    }

    /**
     * Cierra la sesión y vuelve a la pantalla de login.
     */
    @FXML
    public void onLogout() {
        SceneManager.logout();
    }
}

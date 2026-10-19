package ar.com.padel.controller;

import java.sql.SQLException;

import ar.com.padel.model.entity.Employee;
import ar.com.padel.model.service.LoginService;
import ar.com.padel.ui.SceneManager;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

public class LoginController {

    @FXML private TextField txtUser;
    @FXML private PasswordField txtPassword;
    @FXML private Button btnLogin;
    @FXML private Label lblError;
    @FXML private VBox containerRight;
    @FXML private ImageView imgPadelcito;
    private LoginService loginService;

    /**
     * El MainApp configura el servicio antes de mostrar la pantalla.
     */
    public void setLoginService(LoginService loginService) {
        this.loginService = loginService;
    }

    @FXML
    private void initialize() {
    	imgPadelcito.setPreserveRatio(true);
        imgPadelcito.fitWidthProperty().bind(containerRight.widthProperty());
        imgPadelcito.fitHeightProperty().bind(containerRight.heightProperty());
        hideError();
    }

    @FXML
    private void onLogin() {
        hideError();

        String username = txtUser.getText().trim();
        String password = txtPassword.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Ingresá el usuario y la contraseña.");
            return;
        }

        if (loginService == null) {
            showError("El servicio de autenticación no está configurado.");
            return;
        }

        btnLogin.setDisable(true);

        try {
            Employee employee = loginService.login(username, password);
            // La sesión ya fue establecida por LoginService.
            if (employee != null) {
                SceneManager.showMainLayout();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError( "No se pudo acceder a la base de datos. Verificá la conexión e intentá nuevamente.");
        } catch (RuntimeException e) {
            showError(e.getMessage() != null? e.getMessage(): "No se pudo iniciar sesión.");
        } finally {
            btnLogin.setDisable(false);
        }
    }

    @FXML
    private void onKeyTyped(KeyEvent event) {
        hideError();
    }

    private void showError(String message) {
        lblError.setText(message);
        lblError.setVisible(true);
        lblError.setManaged(true);
    }

    private void hideError() {
        lblError.setText("");
        lblError.setVisible(false);
        lblError.setManaged(false);
    }
}

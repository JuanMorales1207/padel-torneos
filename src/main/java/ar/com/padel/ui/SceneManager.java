package ar.com.padel.ui;

import java.io.IOException;
import java.net.URL;

import javax.sql.DataSource;

import ar.com.padel.MainApp;
import ar.com.padel.config.DataSourceProvider;
import ar.com.padel.controller.LoginController;
import ar.com.padel.model.dao.EmployeeDAO;
import ar.com.padel.model.dao.EmployeeDAOImpl;
import ar.com.padel.model.service.LoginService;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public final class SceneManager {

    private static LoginService loginService;

    private SceneManager() { }

    /**
     * Inicializa las dependencias de autenticación.
     * Se ejecuta una sola vez al iniciar la aplicación.
     */
    public static void initialize() {
        DataSource dataSource = DataSourceProvider.getDataSource();
        EmployeeDAO employeeDAO = new EmployeeDAOImpl(dataSource);
        loginService = new LoginService(employeeDAO);
    }

    /**
     * Muestra la pantalla de login e inyecta LoginService.
     */
    public static void showLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource("/fxml/Login.fxml"));
            Parent root = loader.load();
            LoginController controller = loader.getController();
            controller.setLoginService(loginService);
            showScene(root, "Pádel Torneos", true);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo cargar la pantalla de login.", e);
        }
    }

    /**
     * Muestra el layout principal después del login.
     */
    public static void showMainLayout() {
        setScene("/fxml/MainLayout.fxml", "Pádel Torneos", false);
    }

    /**
     * Permite volver al login y cerrar la sesión.
     */
    public static void logout() {
        if (loginService != null) {
            loginService.logout();
        }
        showLogin();
    }


	private static void setScene(String fxml, String title, boolean login) {
	
	    try {
	        URL resource = SceneManager.class.getResource(fxml);
		        if (resource == null) {
	            throw new IllegalStateException("No se encontró el archivo FXML: " + fxml);
	        }
	
	        FXMLLoader loader = new FXMLLoader(resource);
	        Parent root = loader.load();
	        showScene(root, title, login);
	        } catch (Exception e) {
		        System.err.println("Error al cargar la pantalla: " + fxml);
		        e.printStackTrace();
		        throw new IllegalStateException("Error cargando la pantalla: " + fxml, e);
		    }
	}

	private static void showScene(Parent root, String title, boolean login) {

        Scene scene = new Scene(root);
        addStylesheet(scene, "/css/app.css");
        if (!login) {
            addStylesheet(scene, "/css/main-layout.css");
        }
        Stage stage = MainApp.getPrimaryStage();
        if (stage == null) {
            throw new IllegalStateException("La ventana principal no fue inicializada.");
        }

        stage.setTitle(title);
        stage.setScene(scene);

        if (login) {
            stage.setMinWidth(760);
            stage.setMinHeight(500);
            stage.setWidth(900);
            stage.setHeight(560);
        } else {
            stage.setMinWidth(1000);
            stage.setMinHeight(650);
            stage.setWidth(1200);
            stage.setHeight(750);
        }

        stage.centerOnScreen();
        stage.show();
    }

    private static void addStylesheet(Scene scene, String path) {
        URL css = SceneManager.class.getResource(path);

        if (css == null) {
            throw new IllegalStateException("No se encontró la hoja de estilos: " + path);
        }

        scene.getStylesheets().add(css.toExternalForm());
    }
}

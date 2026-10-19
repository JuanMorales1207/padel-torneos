package ar.com.padel;

import ar.com.padel.ui.SceneManager;
import javafx.application.Application;
import javafx.stage.Stage;

public class MainApp extends Application {
	
    private static Stage primaryStage;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;

        try {
            stage.setTitle("Padelcito App");
            // Inicializar las dependencias de la aplicación.
            SceneManager.initialize();
            // La pantalla inicial siempre será el login.
            SceneManager.showLogin();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("No se pudo iniciar la aplicación Padelcito App.");
            stage.close();
        }
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}

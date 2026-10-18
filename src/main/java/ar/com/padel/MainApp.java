package ar.com.padel;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) {
        Label mensaje = new Label(
                "Sistema de Gestión y Análisis de Torneos de Pádel"
        );

        StackPane root = new StackPane(mensaje);
        Scene scene = new Scene(root, 900, 600);

        stage.setTitle("Pádel Torneos");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

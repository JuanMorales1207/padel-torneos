package ar.com.padel.controller;

import ar.com.padel.controller.content.BaseContentController;
import ar.com.padel.controller.menu.MenuBaseController;
import ar.com.padel.security.context.SessionContext;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.net.URL;

public class MainLayoutController {

    @FXML private AnchorPane menuContainer;
    @FXML private StackPane contentContainer;

    @FXML
    public void initialize() {
        loadMenu();
        showWelcome();
    }

    private void loadMenu() {
        String menuPath;
        if (SessionContext.isSuper()) {
            menuPath = "/fxml/menu/MenuSuper.fxml";
        } else if (SessionContext.isAdmin()) {
            menuPath = "/fxml/menu/MenuAdmin.fxml";
        } else {
            menuPath = "/fxml/menu/MenuOperator.fxml";
        }
        try {
            URL menuUrl = getClass().getResource(menuPath);
            if (menuUrl == null) {
                throw new IllegalStateException("No se encuentra el menú: " + menuPath);
            }
            FXMLLoader loader = new FXMLLoader(menuUrl);
            Parent menu = loader.load();
            MenuBaseController menuController = loader.getController();
            menuController.setMainLayoutController(this);
            menuContainer.getChildren().setAll(menu);
            AnchorPane.setTopAnchor(menu, 0.0);
            AnchorPane.setBottomAnchor(menu, 0.0);
            AnchorPane.setLeftAnchor(menu, 0.0);
            AnchorPane.setRightAnchor(menu, 0.0);
        } catch (IOException e) {
            throw new RuntimeException("Error cargando menú", e);
        }
    }

    private void showWelcome() {
        setContent("/fxml/Welcome.fxml");
    }

    public void setContent(String fxmlPath) {
        try {
            URL url = getClass().getResource(fxmlPath);

            if (url == null) {
                throw new IllegalStateException("No se encuentra el contenido: " + fxmlPath);
            }

            Parent view = FXMLLoader.load(url);
            contentContainer.getChildren().setAll(view);

            if (view instanceof Region region) {
                region.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            }

        } catch (IOException e) {
            throw new RuntimeException("Error cargando contenido: " + fxmlPath, e);
        }
    }

    public void setContentWithBase(String title, String contentFxml) {
        try {
            URL baseUrl = getClass().getResource("/fxml/BaseContent.fxml");
            URL contentUrl = getClass().getResource(contentFxml);

            if (baseUrl == null) {
                throw new IllegalStateException("No se encuentra /fxml/BaseContent.fxml");
            }

            if (contentUrl == null) {
                throw new IllegalStateException("No se encuentra " + contentFxml);
            }

            FXMLLoader baseLoader = new FXMLLoader(baseUrl);
            Parent baseRoot = baseLoader.load();
            BaseContentController baseController = baseLoader.getController();
            baseController.setTitle(title);
            Parent contentRoot = FXMLLoader.load(contentUrl);
            baseController.setContent(contentRoot);
            contentContainer.getChildren().setAll(baseRoot);
            if (baseRoot instanceof Region region) {
                region.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error cargando contenido con encabezado", e);
        }
    }
}

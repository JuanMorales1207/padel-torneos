package ar.com.padel.controller.content;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.layout.Region;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

public class BaseContentController {

    @FXML
    private Label titleLabel;

    @FXML
    private StackPane contentContainer;

    public void setTitle(String title) {
        titleLabel.setText(title);
    }
    
    public void setContent(Node content) {
        contentContainer.getChildren().setAll(content);
        if (content instanceof Region region) {
            region.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        }
        StackPane.setAlignment(content, Pos.TOP_LEFT);
    }
}
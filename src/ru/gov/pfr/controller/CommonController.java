package ru.gov.pfr.controller;

import ru.gov.pfr.enteryPoint.EnteryPoint;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

/**
 *
 * @author K Neretin
 */
public class CommonController extends AnchorPane {

    @FXML
    protected Scene scene;
    protected EnteryPoint enteryPoint;
    protected Stage stage;

    public void setScene(Scene scene) {
        this.scene = scene;
    }

    public void setEnteryPoint(EnteryPoint epoint) {
        enteryPoint = epoint;
    }

    @FXML
    protected void removeInvalidClass(TextField field) {
        if (field.getStyleClass().contains("invalid")) {
            field.getStyleClass().remove("invalid");
        }
    }

    @FXML
    protected void addInvalidClass(TextField field) {
        if (field.getStyleClass().contains("invalid") == false) {
            field.getStyleClass().add("invalid");
        }
    }

    @FXML
    protected boolean isInvalidClass(TextField field) {
        return field.getStyleClass().contains("invalid");
    }

    public void setStage(Stage modalStage) {
        this.stage = modalStage;
    }

    public void closeStage() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                stage.close();
            }
        });
    }
}

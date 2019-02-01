package org.mars_sim.javafx.tools;

import org.mars_sim.javafx.ComboBoxItem;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class ComboBoxFX extends Application {

    @Override
    public void start(Stage stage) {
        HBox box = new HBox();

        ComboBox<ComboBoxItem> cb = new ComboBox<ComboBoxItem>();

        cb.setCellFactory(e -> new ListCell<ComboBoxItem>() {
            @Override
            public void updateItem(ComboBoxItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                    setDisable(false);
                } else {
                    setText(item.toString());

                    // If item is a header we disable it.
                    setDisable(!item.isHeader());

                    // If item is a header we add a style to it so it looks like a header.
                    if (item.isHeader()) {
                        setStyle("-fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-font-weight: normal;");
                    }
                }
            }
        });

        ObservableList<ComboBoxItem> items = FXCollections.observableArrayList(
        		new ComboBoxItem("Labels", true),
        		new ComboBoxItem("Building", false),
                new ComboBoxItem("Vehicle", false)
                );

        cb.getItems().addAll(items);

        box.getChildren().add(cb);

        stage.setScene(new Scene(box));
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}


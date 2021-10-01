package de.softknk.gui;

import de.softknk.main.AppSettings;
import de.softknk.model.operations.Operation;
import de.softknk.model.util.Loader;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;

public class OperationData extends Button implements EventHandler {

    private Operation operation;

    public OperationData(Operation operation, Image operationImage) {
        this.setPrefSize(Dashboard.WIDTH, Dashboard.HEIGHT / 5);
        this.setBackground(null);
        this.setContentDisplay(ContentDisplay.TOP);
        this.setAlignment(Pos.CENTER);
        this.setGraphic(new ImageView(operationImage));
        this.setFont(Loader.loadFont(AppSettings.DEFAULT_FONT, 14));
        this.setTextFill(Color.WHITE);

        this.operation = operation;

        this.setOnMouseEntered(mouseEvent -> this.setTextFill(Color.rgb(180, 180, 180)));
        this.setOnMouseExited(mouseEvent -> this.setTextFill(Color.WHITE));

        this.setTooltip(new Tooltip(this.operation.operationName()));

        this.setOnAction(this);
    }

    public void update() {
        if (operation.getPrice() <= 1_000_000) {
            this.setText("LVL. " + operation.getLevel() + "  |  " + operation.getPrice() / 1000.0 + "k");
        } else {
            this.setText("LVL. " + operation.getLevel() + "  |  " + operation.getPrice() * 1.0 / 1_000_000 + "M");
        }
    }

    @Override
    public void handle(Event event) {
        if (operation.canBuyLevel()) {
            operation.doOperation();
            update();
        }
    }
}
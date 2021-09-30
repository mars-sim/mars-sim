package de.softknk.gui;

import de.softknk.data.SaveGameData;
import de.softknk.main.AppSettings;
import de.softknk.main.SoftknkioApp;
import de.softknk.model.operations.*;
import de.softknk.model.util.Loader;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.text.DecimalFormat;

public class Dashboard extends VBox {

    public static final double WIDTH = AppSettings.WINDOW_WIDTH * 0.20;
    public static final double HEIGHT = AppSettings.WINDOW_HEIGHT - 50;
    public static final double TRANSLATE = 25;

    private Operation[] operations;
    private Label pointsPerSecond_label;

    public Dashboard(int pointLevel, int machineLevel, int excavatorLevel, int mineLevel, int factoryLevel) {
        this.setPrefSize(WIDTH, HEIGHT);
        this.setStyle("-fx-background-color: rgba(138, 109, 98, 0.35);");
        this.setSpacing(0);
        this.setTranslateX(TRANSLATE);
        this.setTranslateY(TRANSLATE);

        this.operations = new Operation[5];
        this.initOperations(pointLevel, machineLevel, excavatorLevel, mineLevel, factoryLevel);

        this.pointsPerSecond_label = new PointsPerSecond();
        ((PointsPerSecond) this.pointsPerSecond_label).startUpdateTimeline();
    }

    private void initOperations(int pointLevel, int machineLevel, int excavatorLevel, int mineLevel, int factoryLevel) {
        operations[0] = new PointOperation(pointLevel);
        operations[1] = new MachineOperation(machineLevel);
        operations[2] = new ExcavatorOperation(excavatorLevel);
        operations[3] = new MineOperation(mineLevel);
        operations[4] = new FactoryOperation(factoryLevel);

        for (int i = 0; i < operations.length; i++) {
            this.getChildren().add(this.operations[i].getData());
            this.operations[i].init();
        }
    }

    public int pointsPerSecond() {
        return ((MachineOperation) operations[1]).getPointsPerSecond() + ((ExcavatorOperation) operations[2]).getPointsPerSecond() +
                ((MineOperation) operations[3]).getPointsPerSecond() + ((FactoryOperation) operations[4]).getPointsPerSecond();
    }

    public Operation[] getOperations() {
        return this.operations;
    }

    public Label getPointsPerSecond_label() {
        return this.pointsPerSecond_label;
    }

    private class PointsPerSecond extends Label {

        public PointsPerSecond() {
            super("Points per second: " + new DecimalFormat().format(pointsPerSecond()));
            this.setPrefSize(300, 50);
            this.setAlignment(Pos.CENTER);
            this.setTranslateX(AppSettings.WINDOW_WIDTH - 300 - TRANSLATE);
            this.setTranslateY(TRANSLATE);
            this.setTextFill(Color.WHITE);
            this.setFont(Loader.loadFont(AppSettings.DEFAULT_FONT, 19));
        }

        private void showPointsPerSecond() {
            this.setText("Points per second: " + new DecimalFormat().format(pointsPerSecond()));
        }

        public void startUpdateTimeline() {
            Timeline timeline = new Timeline(new KeyFrame(Duration.millis(500), actionEvent -> {
                showPointsPerSecond();
                SoftknkioApp.matchfield.getPlayer().increaseScore(pointsPerSecond());
                SaveGameData.saveData();
            }));
            timeline.setCycleCount(Timeline.INDEFINITE);
            timeline.play();
        }
    }

    public void update() {
        for (int i = 0; i < operations.length; i++) {
            operations[i].getData().update();
        }
    }
}

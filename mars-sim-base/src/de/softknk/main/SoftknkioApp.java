package de.softknk.main;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.entity.GameWorld;
import com.almasb.fxgl.input.Input;
import com.almasb.fxgl.input.UserAction;
import de.codecentric.centerdevice.javafxsvg.SvgImageLoaderFactory;
import de.codecentric.centerdevice.javafxsvg.dimension.PrimitiveDimensionProvider;
import de.softknk.data.ReadGameData;
import de.softknk.data.SaveGameData;
import de.softknk.gui.Dashboard;
import de.softknk.model.entities.Matchfield;
import de.softknk.model.entities.Player;
import de.softknk.model.entities.Point;
import de.softknk.model.util.Loader;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.*;

import static com.almasb.fxgl.dsl.FXGL.*;

public class SoftknkioApp extends GameApplication {

    public static GameWorld gameWorld;
    public static Matchfield matchfield;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    protected void initSettings(GameSettings game) {
        game.setAppIcon(AppSettings.ICON);
        game.setVersion(AppSettings.VERSION);
        game.setTitle(AppSettings.TITLE);
        game.setWidth(AppSettings.WINDOW_WIDTH);
        game.setHeight(AppSettings.WINDOW_HEIGHT);
        game.setManualResizeEnabled(false);
    }

    @Override
    protected void initGame() {
        try {
            //load the current game state
            Optional<List<String>> optionalData = ReadGameData.readData();

            if (optionalData.isPresent()) {
                List<String> gameData = optionalData.get();

                gameWorld = getGameWorld();
                getGameScene().setBackgroundColor(Color.BLACK);

                Player player = new Player(gameData.get(0), Integer.parseInt(gameData.get(1)));

                Dashboard dashboard = new Dashboard(
                        Integer.parseInt(gameData.get(2)), //pointOperation level
                        Integer.parseInt(gameData.get(3)), //machineOperation level
                        Integer.parseInt(gameData.get(4)), //excavatorOperation level
                        Integer.parseInt(gameData.get(5)), //mineOperation level
                        Integer.parseInt(gameData.get(6))  //factoryOperation level
                );

                //set current point score
                Point.initScoreValue(Integer.parseInt(gameData.get(2)));

                matchfield = new Matchfield(player, dashboard);
                dashboard.update();

                initCollisionTimeline();

            } else {
                throw new ReadGameStateException();
            }
        } catch (ReadGameStateException e) {
            e.printStackTrace();
        }
    }

    private static class ReadGameStateException extends Exception {

        public ReadGameStateException() {
            System.err.println("Could not read game data!");
            System.exit(0);
        }
    }

    protected void initCollisionTimeline() {
        Timeline collisionUpdater = new Timeline(new KeyFrame(Duration.millis(40), actionEvent -> {
            matchfield.getPoints().forEach(matchfield.getPlayer()::collisionHandling);

            Player.collisionSet.forEach(point -> {
                point.removeFromWorld();
                matchfield.getPoints().remove(point);
                matchfield.addPoint();
                matchfield.getPlayer().increaseScore(Point.scoreValue());
            });

            Player.collisionSet.clear();
        }));
        collisionUpdater.setCycleCount(Timeline.INDEFINITE);
        collisionUpdater.play();
    }

    @Override
    protected void initUI() {
        getGameScene().addUINode(matchfield.getDashboard());
        getGameScene().addUINode(matchfield.getPlayer().getData());
        getGameScene().addUINode(matchfield.getDashboard().getPointsPerSecond_label());
    }

    @Override
    protected void initInput() {
        Input input = getInput();

        input.addAction(new UserAction("Mouse Movement") {
            @Override
            protected void onAction() {
              /*  if(getGameScene().getUiNodes().size() > 3)
                    getGameScene().removeUINode(getGameScene().getUiNodes().get(3)); */

                double dy = input.getMouseYWorld() - (matchfield.getPlayer().getStartY() + Player.RADIUS);
                double dx = input.getMouseXWorld() - (matchfield.getPlayer().getStartX() + Player.RADIUS);
                double angle = Math.atan2(dy, dx);

                matchfield.moveX(matchfield.getPlayer().getVelocity() * Math.cos(angle));
                matchfield.moveY(matchfield.getPlayer().getVelocity() * Math.sin(angle));

              /*  Line line = new Line(matchfield.getPlayer().getStartX() + Player.RADIUS, matchfield.getPlayer().getStartY() + Player.RADIUS, input.getMouseXWorld(), input.getMouseYWorld());
                line.setStroke(Color.WHITE);
                line.setStrokeWidth(1.5);
                getGameScene().addUINode(line); */
            }
        }, MouseButton.PRIMARY);

       /* input.addAction(new UserAction("Mark Rectangle") {
            @Override
            protected void onAction() {
                int cellX = Math.abs((int) (input.getMouseXWorld() - matchfield.getX()) / AppSettings.CELL_WIDTH);
                int cellY = Math.abs((int) (input.getMouseYWorld() - matchfield.getY()) / AppSettings.CELL_WIDTH);

                matchfield.getGridCells()[cellX][cellY].setFill(Color.RED);
            }
        }, MouseButton.SECONDARY); */

        input.addAction(new UserAction("Change Player Color") {
            @Override
            protected void onActionBegin() {
                matchfield.getPlayer().getData().changeColor();
            }
        }, KeyCode.C);

        input.addAction(new UserAction("Save Data") {
            @Override
            protected void onActionBegin() {
                SaveGameData.saveData();
            }
        }, KeyCode.M);

        input.addAction(new UserAction("Change Mode") {
            @Override
            protected void onActionBegin() {
                matchfield.changeMode();
            }
        }, KeyCode.L);

        input.addAction(new UserAction("Change Nickname") {
            @Override
            protected void onActionBegin() {
                TextInputDialog dialog = new TextInputDialog(matchfield.getPlayer().getNickname());
                dialog.setTitle("Change your nickname");
                dialog.setHeaderText("🅝🅘🅒🅚🅝🅐🅜🅔");
                dialog.setContentText("Please enter a new nickname:");
                ((Stage) (dialog.getDialogPane().getScene().getWindow())).getIcons().add(Loader.loadImage(AppSettings.ICON));
                dialog.setGraphic(new ImageView(Loader.loadImage(AppSettings.ICON)));

                Optional<String> result = dialog.showAndWait();
                result.ifPresent(nickname -> {
                    matchfield.getPlayer().setNickname(nickname);
                    SaveGameData.saveData();
                    input.clearAll();
                });
            }
        }, KeyCode.N);
    }
}
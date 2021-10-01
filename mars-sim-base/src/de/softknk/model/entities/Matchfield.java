package de.softknk.model.entities;

import com.almasb.fxgl.entity.Entity;
import de.softknk.main.AppSettings;
import de.softknk.main.SoftknkioApp;
import de.softknk.gui.Dashboard;
import de.softknk.model.util.EntityType;
import de.softknk.model.util.Moveable;
import javafx.geometry.Insets;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.List;

public class Matchfield extends Entity implements Moveable {

    private Player player;
    private List<Point> points;
    private final int pointAmount = 270;
    private Dashboard dashboard;
    private Mode mode;
    private GridPane grid;

    public Matchfield(Player player, Dashboard dashboard) {
        this.player = player;
        this.dashboard = dashboard;

        this.mode = Mode.DARK;

        grid = new GridPane();
   //     grid.setBorder(new Border(new BorderStroke(Color.WHITE, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
        grid.setPrefSize(AppSettings.MAP_WIDTH, AppSettings.MAP_HEIGHT);
        grid.setBackground(new Background(new BackgroundFill(this.mode.getGridColor(), CornerRadii.EMPTY, Insets.EMPTY)));

        this.setType(EntityType.MATCHFIELD);
        this.getViewComponent().addChild(grid);
        this.setPosition(-(AppSettings.MAP_WIDTH - AppSettings.WINDOW_WIDTH) / 2, -(AppSettings.MAP_HEIGHT - AppSettings.WINDOW_HEIGHT) / 2);
        this.initPoints();

        //add entities to game world
        SoftknkioApp.gameWorld.addEntity(this);
        this.points.forEach(point -> SoftknkioApp.gameWorld.addEntity(point));
        SoftknkioApp.gameWorld.addEntity(this.player);
    }

    public void changeMode() {
        if (this.mode == Mode.DARK)
            this.mode = Mode.LIGHT;
        else
            this.mode = Mode.DARK;

        this.grid.setBackground(new Background(new BackgroundFill(this.mode.getGridColor(), CornerRadii.EMPTY, Insets.EMPTY)));
        this.dashboard.setStyle(this.mode.getDashboardStyle());
        ((Circle) this.player.getViewComponent().getChildren().get(0)).setStroke(this.mode.getPlayerStroke());
    }

    private void initPoints() {
        this.points = new ArrayList<>();
        for (int i = 0; i < this.pointAmount; i++) {
            this.points.add(new Point());
        }
    }

    public void addPoint() {
        this.points.add(new Point());

        //add point to matchfield
        SoftknkioApp.gameWorld.removeEntity(this.player);
        SoftknkioApp.gameWorld.addEntity(this.points.get(this.points.size() - 1));
        SoftknkioApp.gameWorld.addEntity(this.player);
    }

    private void movementX(double pixel) {
        this.translateX(-pixel);
        this.player.moveX(pixel);
        this.points.forEach(point -> point.moveX(-pixel));
    }

    private void movementY(double pixel) {
        this.translateY(-pixel);
        this.player.moveY(pixel);
        this.points.forEach(point -> point.moveY(-pixel));
    }

    @Override
    public void moveX(double pixel) {
        if (pixel >= 0) {
            if (this.getX() + AppSettings.MAP_WIDTH + 22 > this.player.getX() + AppSettings.PLAYER_RADIUS * 2) {
                movementX(pixel);
            }
        } else {
            if (this.getX() < this.player.getX()) {
                movementX(pixel);
            }
        }
    }

    @Override
    public void moveY(double pixel) {
        if (pixel >= 0) {
            if (this.getY() + AppSettings.MAP_HEIGHT + 22 > this.player.getY() + AppSettings.PLAYER_RADIUS * 2) {
                movementY(pixel);
            }
        } else {
            if (this.getY() < this.player.getY()) {
                movementY(pixel);
            }
        }
    }

    /*
        setter and getter
     */

    public Player getPlayer() {
        return player;
    }

    public List<Point> getPoints() {
        return this.points;
    }

    public Dashboard getDashboard() {
        return this.dashboard;
    }

    /*
        MODE
     */

    private enum Mode {

        DARK(Color.BLACK, Color.rgb(180, 180, 180), "-fx-background-color: rgba(138, 109, 98, 0.65);", Color.WHITE),
        LIGHT(Color.WHITE, Color.rgb(30, 30, 30), "-fx-background-color: rgba(92, 73, 66, 0.65);", Color.rgb(50, 50, 50));

        private Color gridColor, cellColor, playerStroke;
        private String dashboardStyle;

        Mode(Color gridColor, Color cellColor, String dashboardStyle, Color playerStroke) {
            this.gridColor = gridColor;
            this.cellColor = cellColor;
            this.dashboardStyle = dashboardStyle;
            this.playerStroke = playerStroke;
        }

        public Color getGridColor() {
            return this.gridColor;
        }

        public Color getCellColor() {
            return this.cellColor;
        }

        public String getDashboardStyle() {
            return this.dashboardStyle;
        }

        public Color getPlayerStroke() {
            return this.playerStroke;
        }
    }
}
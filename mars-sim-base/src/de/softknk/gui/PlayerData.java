package de.softknk.gui;

import de.softknk.main.AppSettings;
import de.softknk.model.entities.Player;
import de.softknk.model.util.Loader;
import de.softknk.model.util.PlayerColor;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.TextAlignment;

import java.text.DecimalFormat;

public class PlayerData extends Label {

    private Player player;
    private int currentColor = 1;

    public PlayerData(Player player) {
        super(player.getNickname() + "\n" + new DecimalFormat().format(player.getScore()));

        this.setFont(Loader.loadFont(AppSettings.DEFAULT_FONT, 23));
        this.setTextFill(Color.WHITE);
        this.setPrefSize(Player.RADIUS * 2, Player.RADIUS * 2);
        this.setTranslateX(player.getX());
        this.setTranslateY(player.getY());
        this.setAlignment(Pos.CENTER);
        this.setTextAlignment(TextAlignment.CENTER);

        Circle circle = new Circle(Player.RADIUS, Player.RADIUS, Player.RADIUS, PlayerColor.DEFAULT);
        circle.setStroke(Color.WHITE);
        circle.setStrokeWidth(2.5);
        player.getViewComponent().addChild(circle);

        this.player = player;
    }

    public void update() {
        this.setText(player.getNickname() + "\n" + new DecimalFormat().format(player.getScore()));
    }

    public void changeColor() {
        ((Circle) this.player.getViewComponent().getChildren().get(0)).setFill(PlayerColor.random(this.currentColor));
        this.currentColor = (currentColor == 4) ? 0 : ++this.currentColor;
    }
}

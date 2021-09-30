package de.softknk.model.entities;

import com.almasb.fxgl.entity.Entity;
import de.softknk.gui.Dashboard;
import de.softknk.gui.PlayerData;
import de.softknk.main.AppSettings;
import de.softknk.model.util.EntityType;
import de.softknk.model.util.Moveable;
import de.softknk.model.util.Vector;

import java.util.HashSet;

public class Player extends Entity implements Moveable {

    public static final double RADIUS = AppSettings.PLAYER_RADIUS;
    private String nickname;
    private int score;
    private final double velocity = AppSettings.VELOCITY;

    //x and y save the virtual position of the player because the player doesn't physically move on the screen
    private double x, y;
    private double startX, startY;

    //saves all the points that are currently in a collision with the player
    public static HashSet<Point> collisionSet = new HashSet<>();
    private PlayerData data;

    public Player(String nickname, int score) {
        this.nickname = nickname;
        this.score = score;

        this.x = this.startX = AppSettings.WINDOW_WIDTH / 2;
        this.y = this.startY = AppSettings.WINDOW_HEIGHT / 2 - RADIUS;

        this.setType(EntityType.PLAYER);
        this.setPosition(this.x, this.y);

        this.data = new PlayerData(this);
    }

    public void collisionHandling(Point point) {
        if (isCollision(point))
            collisionSet.add(point);
    }

    private boolean isCollision(Point point) {
        //if the distance between the two centers (player and point) is less than the two radii then there is a collision!
        return (Vector.distanceBetween(new Vector.Vector2D(this.startX + RADIUS, this.startY + RADIUS),
                new Vector.Vector2D(point.getX() + point.RADIUS, point.getY() + point.RADIUS)) < point.RADIUS + RADIUS);
    }

    public void increaseScore(int score) {
        this.score += score;
        data.update();
    }

    public boolean buyLevel(int price) {
        if (isBuyable(price)) {
            this.score -= price;
            data.update();
            return true;
        } else {
            return false;
        }
    }

    private boolean isBuyable(int price) {
        return this.score >= price;
    }

    @Override
    public void moveX(double pixel) {
        this.x += pixel;
    }

    @Override
    public void moveY(double pixel) {
        this.y += pixel;
    }

    /*
        setter and getter
     */

    public void setNickname(String nickname) {
        this.nickname = nickname;
        data.update();
    }

    public String getNickname() {
        return nickname;
    }

    public int getScore() {
        return this.score;
    }

    public double getVelocity() {
        return velocity;
    }

    public PlayerData getData() {
        return this.data;
    }

    public double getStartX() {
        return this.startX;
    }

    public double getStartY() {
        return this.startY;
    }
}
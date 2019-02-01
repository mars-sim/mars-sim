package org.mars_sim.javafx.tools;

import java.util.Random;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

// from https://netopyr.com/2012/06/14/using-the-javafx-animationtimer/

public class FXSandbox extends Application {
    
    private static final int STAR_COUNT = 200;//00;
    
    private final Rectangle[] nodes = new Rectangle[STAR_COUNT];
    private final double[] angles = new double[STAR_COUNT];
    private final long[] start = new long[STAR_COUNT];
    
    private final Random random = new Random();

    @Override
    public void start(final Stage primaryStage) {
        for (int i=0; i<STAR_COUNT; i++) {
            nodes[i] = new Rectangle(1, 1, Color.WHITE);
            angles[i] = 2.0 * Math.PI * random.nextDouble();
            start[i] = random.nextInt(2000000000);
        }
        final Scene scene = new Scene(new Group(nodes), 800, 600, Color.BLACK);
        primaryStage.setScene(scene);
        primaryStage.show();
        
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                final double width = 0.5 * primaryStage.getWidth();
                final double height = 0.5 * primaryStage.getHeight();
                final double radius = Math.sqrt(2) * Math.max(width, height);
                for (int i=0; i<STAR_COUNT; i++) {
                    final Node node = nodes[i];
                    final double angle = angles[i];
                    final long t = (now - start[i]) % 2000000000;
                    final double d = t * radius / 2000000000.0;
                    node.setTranslateX(Math.cos(angle) * d + width);
                    node.setTranslateY(Math.sin(angle) * d + height);
                }
            }
        }.start();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
    
}
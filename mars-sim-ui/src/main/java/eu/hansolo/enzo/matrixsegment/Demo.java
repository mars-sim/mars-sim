/*
 * Copyright (c) 2015 by Gerrit Grunwald
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.hansolo.enzo.matrixsegment;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.Random;


public class Demo extends Application {
    private static final Random RND = new Random();
    private Color[] colors = {
        Color.RED,
        Color.LIME,
        Color.LIGHTBLUE,
        Color.CYAN,
        Color.MAGENTA
    };

    private MatrixSegment seg1;
    private MatrixSegment seg2;
    private MatrixSegment seg3;
    private MatrixSegment seg4;
    private MatrixSegment seg5;
    private MatrixSegment seg6;

    private SquareMatrixSegment seg7;
    private SquareMatrixSegment seg8;
    private SquareMatrixSegment seg9;
    private SquareMatrixSegment seg10;
    private SquareMatrixSegment seg11;
    private SquareMatrixSegment seg12;

    private long           interval;
    private long           lastTimerCall;
    private AnimationTimer timer;


    @Override public void init() {
        seg1  = MatrixSegmentBuilder.create().prefSize(142, 200).character("J").build();
        seg2  = MatrixSegmentBuilder.create().prefSize(142, 200).character("a").build();
        seg3  = MatrixSegmentBuilder.create().prefSize(142, 200).character("v").build();
        seg4  = MatrixSegmentBuilder.create().prefSize(142, 200).character("a").build();
        seg5  = MatrixSegmentBuilder.create().prefSize(142, 200).character("F").build();
        seg6  = MatrixSegmentBuilder.create().prefSize(142, 200).character("X").build();

        seg7  = SquareMatrixSegmentBuilder.create().prefSize(200, 200).character("J").build();
        seg8  = SquareMatrixSegmentBuilder.create().prefSize(200, 200).character("a").build();
        seg9  = SquareMatrixSegmentBuilder.create().prefSize(200, 200).character("v").build();
        seg10 = SquareMatrixSegmentBuilder.create().prefSize(200, 200).character("a").build();
        seg11 = SquareMatrixSegmentBuilder.create().prefSize(200, 200).character("F").build();
        seg12 = SquareMatrixSegmentBuilder.create().prefSize(200, 200).character("X").build();

        interval      = 2_000_000_000l;
        lastTimerCall = System.nanoTime();
        timer         = new AnimationTimer() {
            @Override public void handle(long now) {
                if (now > lastTimerCall + interval) {
                    Color color = colors[RND.nextInt(5)];
                    seg1.setColor(color);
                    seg2.setColor(color);
                    seg3.setColor(color);
                    seg4.setColor(color);
                    seg5.setColor(color);
                    seg6.setColor(color);
                    seg7.setColor(color);
                    seg8.setColor(color);
                    seg9.setColor(color);
                    seg10.setColor(color);
                    seg11.setColor(color);
                    seg12.setColor(color);
                    lastTimerCall = now;
                }
            }
        };
    }

    @Override public void start(Stage stage) {
        HBox matrixSegmentPane = new HBox();
        matrixSegmentPane.setSpacing(5);
        matrixSegmentPane.setPadding(new Insets(10, 10, 10, 10));
        matrixSegmentPane.getChildren().setAll(seg1, seg2, seg3, seg4, seg5, seg6);

        HBox squareMatrixSegmentPane = new HBox();
        squareMatrixSegmentPane.setSpacing(0);
        squareMatrixSegmentPane.setPadding(new Insets(10, 10, 10, 10));
        squareMatrixSegmentPane.getChildren().setAll(seg7, seg8, seg9, seg10, seg11, seg12);

        VBox pane = new VBox();
        pane.setSpacing(10);
        pane.getChildren().addAll(matrixSegmentPane, squareMatrixSegmentPane);

        Scene scene = new Scene(pane, Color.DARKGRAY);

        stage.setTitle("Enzo MatrixSegment");
        stage.setScene(scene);
        stage.show();

        timer.start();
    }

    public static void main(String[] args) {
        Application.launch(args);
    }
}



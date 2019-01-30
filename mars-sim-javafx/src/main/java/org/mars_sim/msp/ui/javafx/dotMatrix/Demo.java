
package org.mars_sim.msp.ui.javafx.dotMatrix;

/*
 * Copyright (c) 2017 by Gerrit Grunwald
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

import org.mars_sim.msp.ui.javafx.dotMatrix.DotMatrix.DotShape;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.scene.layout.StackPane;
import javafx.scene.Scene;


/**
 * User: hansolo
 * Date: 19.03.17
 * Time: 05:00
 */
public class Demo extends Application {
    private static final int            LIME = DotMatrix.convertToInt(Color.LIME);
    private static final int            RED  = DotMatrix.convertToInt(Color.RED);
    private              int            x;
    private              DotMatrix      matrix;
    private              String         text;
    private              int            textLength;
    private              int            textLengthInPixel;
    private              MatrixFont     matrixFont;
    private              int            offset;
    private              long           lastTimerCall;
    private              AnimationTimer timer;


    @Override public void init() {
        matrix            = DotMatrixBuilder.create()
                                            .prefSize(500, 50)
                                            .colsAndRows(128, 13)
                                            .dotOnColor(Color.rgb(255, 55, 0))
                                            .dotShape(DotShape.ROUND)
                                            .matrixFont(MatrixFont8x8.INSTANCE)
                                            .build();
        x                 = matrix.getCols() + 7;
        text              = "8x8 Font Round Dots (@hansolo_) ";
        textLength        = text.length();
        textLengthInPixel = textLength * 8;
        matrixFont        = matrix.getMatrixFont();
        offset            = 3;

        lastTimerCall = System.nanoTime();
        timer = new AnimationTimer() {
            @Override public void handle(final long now) {
                if (now > lastTimerCall + 10_000_000l) {
                    if (x < -textLengthInPixel) {
                        x = matrix.getCols() + 7;
                        if (matrixFont.equals(MatrixFont8x8.INSTANCE)) {
                            matrix.setMatrixFont(MatrixFont8x11.INSTANCE);
                            text       = "8x11 Font Square Dots (@hansolo_) ";
                            offset     = 1;
                            matrix.setDotShape(DotShape.SQUARE);
                        } else {
                            matrix.setMatrixFont(MatrixFont8x8.INSTANCE);
                            text       = "8x8 Font Round Dots (@hansolo_) ";
                            offset     = 3;
                            matrix.setDotShape(DotShape.ROUND);
                        }
                        textLength        = text.length();
                        textLengthInPixel = textLength * 8;
                    }
                    for (int i = 0 ; i < textLength ; i++) {
                        matrix.setCharAt(text.charAt(i), x + i * 8, offset, i % 2 == 0 ? LIME : RED);
                        //matrix.setDigitAt(RND.nextInt(9), x + i * 8, 4, i % 2 == 0 ? LIME : RED);
                    }
                    x--;
                    lastTimerCall = now;
                }
            }
        };
    }

    @Override public void start(Stage stage) {
        StackPane pane = new StackPane(matrix);
        pane.setPadding(new Insets(10));
        pane.setBackground(new Background(new BackgroundFill(Color.rgb(20, 20, 20), CornerRadii.EMPTY, Insets.EMPTY)));

        Scene scene = new Scene(pane);

        stage.setTitle("DotMatrix");
        stage.setScene(scene);
        stage.show();

        timer.start();
    }

    @Override public void stop() {
        System.exit(0);
    }

    public static void main(String[] args) {
        launch(args);
    }
}

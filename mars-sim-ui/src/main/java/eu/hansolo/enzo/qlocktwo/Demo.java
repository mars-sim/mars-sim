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

package eu.hansolo.enzo.qlocktwo;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;


public class Demo extends Application {
    private QlockTwo german;
    private QlockTwo english;
    private QlockTwo french;
    private QlockTwo spanish;
    private QlockTwo dutch;
    private QlockTwo german1;


    @Override public void init() {
        german  = QlockTwoBuilder.create().prefSize(400, 400).language(QlockTwo.Language.GERMAN).color(QlockTwo.QlockColor.BLACK_ICE_TEA).build();
        english = QlockTwoBuilder.create().prefSize(400, 400).language(QlockTwo.Language.ENGLISH).color(QlockTwo.QlockColor.BLUE_CANDY).build();
        french  = QlockTwoBuilder.create().prefSize(400, 400).language(QlockTwo.Language.FRENCH).color(QlockTwo.QlockColor.CHERRY_CAKE).build();
        spanish = QlockTwoBuilder.create().prefSize(400, 400).language(QlockTwo.Language.SPANISH).color(QlockTwo.QlockColor.FROZEN_BLACKBERRY).build();
        dutch   = QlockTwoBuilder.create().prefSize(400, 400).language(QlockTwo.Language.DUTCH).color(QlockTwo.QlockColor.LIME_JUICE).build();
        german1 = QlockTwoBuilder.create().secondsMode(true).prefSize(400, 400).language(QlockTwo.Language.GERMAN).color(QlockTwo.QlockColor.STAINLESS_STEEL).build();
    }

    @Override public void start(Stage stage) {
        GridPane pane = new GridPane();
        pane.setHgap(10);
        pane.setVgap(10);
        pane.add(german, 0, 0);
        pane.add(english, 1, 0);
        pane.add(french, 2, 0);
        pane.add(spanish, 0, 1);
        pane.add(dutch, 1, 1);
        pane.add(german1, 2, 1);

        Scene scene = new Scene(pane, Color.DARKGRAY);

        stage.setTitle("JavaFX QlockTwo");
        stage.setScene(scene);
        stage.show();
    }

    @Override public void stop() {
        Platform.exit();
    }

    public static void main(String[] args) {
        Application.launch(args);
    }
}



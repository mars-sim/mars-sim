/* Mars Simulation Project
 * ClockTool.java
 * @version 3.1.0 2016-06-21
 * @author Manny Kung
 */

/*
 * Copyright (c) 2014 by Gerrit Grunwald
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

//package org.mars_sim.msp.ui.swing.tool.time;
//
//import javafx.geometry.Insets;
//import javafx.scene.layout.Background;
//import javafx.scene.layout.BackgroundFill;
//import javafx.scene.layout.CornerRadii;
//import javafx.scene.paint.Color;
//import javafx.stage.Stage;
//import javafx.scene.layout.StackPane;
//import javafx.scene.Scene;
//import javafx.scene.image.Image;


/**
 * User: hansolo
 * Date: 12.09.14
 * Time: 00:37

public class ClockTool { //extends Applcation {
    private EarthMinimalClock earthEarthMinimalClock;

    public ClockTool() {  	
    //}
  
    //@Override public void init() {
        earthEarthMinimalClock = new EarthMinimalClock();
        //earthEarthMinimalClock.setClockBackgroundColor(Color.BLACK);
        //earthEarthMinimalClock.setClockBorderColor(Color.LIME);
        //earthEarthMinimalClock.setHourTextColor(Color.PURPLE);
        //earthEarthMinimalClock.setDateTextColor(Color.ORANGE);
        //earthEarthMinimalClock.setMinutesBackgroundColor(Color.YELLOW);
        //earthEarthMinimalClock.setMinutesTextColor(Color.BLUE);
        //earthEarthMinimalClock.setSecondsColor(Color.RED);
    //}

    //@Override public void start(Stage stage) {
        Stage stage = new Stage();
        StackPane pane = new StackPane();

        pane.getChildren().addAll(earthEarthMinimalClock);
        pane.setBackground(new Background(new BackgroundFill(Color.GOLDENROD,
                CornerRadii.EMPTY, Insets.EMPTY)));

        Scene scene = new Scene(pane, 320, 320);

 		stage.getIcons().add(new Image(this.getClass().getResource("/icons/lander_hab64.png").toExternalForm()));
        //stage.initStyle(StageStyle.UTILITY);
        //stage.initStyle(StageStyle.TRANSPARENT);
        //stage.initStyle(StageStyle.UNDECORATED);
        stage.setScene(scene);
        stage.show();
    }

    //@Override public void stop() {
    //    System.exit(0);
    //}

    public static void main(String[] args) {
        //launch(args);
    }
}
 */
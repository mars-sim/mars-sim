/*
 * Copyright (c) 2013 by Gerrit Grunwald
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

package org.mars_sim.javafx.tools;

/**
 * Created by
 * User: hansolo
 * Date: 01.07.13
 * Time: 07:10
 *

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.Random;

import eu.hansolo.enzo.notification.Notification;
import eu.hansolo.enzo.notification.Notification.Notifier;


public class NotificationDemo extends Application {
    private static final Random         RND           = new Random();
    
    private static String quote = "testing";
    
    private static final Notification[] NOTIFICATIONS = {
        new Notification("Info", quote, Notification.INFO_ICON),
        new Notification("Warning", "Attention, somethings wrong", Notification.WARNING_ICON),
        new Notification("Success", "Great it works", Notification.SUCCESS_ICON),
        new Notification("Error", "ZOMG", Notification.ERROR_ICON)
    };
    private Notification.Notifier notifier;
    private Button                button;


    // ******************** Initialization ************************************
    @Override public void init() {
        button = new Button("Notify");
        button.setOnAction(event -> {
            notifier.notify(NOTIFICATIONS[RND.nextInt(4)]);
        });
    }


    // ******************** Application start *********************************
    @Override public void start(Stage stage) {
        notifier = Notification.Notifier.INSTANCE;
        Notifier.setHeight(120);
        Notifier.setWidth(350);
        
        quote = "\" All the conditions necessary for murder are \n "
        		+ " met if you shut 2 men in a cabin measuring \n"
        		+ " 18'x20' and leave them together for 2 months.\"\n"
        		+ "                              - Valery Ryumin";			
		

		Notification n0 = new Notification("Info", quote, Notification.INFO_ICON);
		
		//n0.setWidth(400);
		//n0.setWidth(150);
			
		notifier.notify(n0);	
        
        StackPane pane = new StackPane();
        pane.setPadding(new Insets(10, 10, 10, 10));
        pane.getChildren().addAll(button);

        Scene scene = new Scene(pane);
        stage.setOnCloseRequest(observable -> notifier.stop());
        stage.setScene(scene);
        stage.show();
    }

    @Override public void stop() {
    }

    public static void main(String[] args) {
        launch(args);
    }
}
*/
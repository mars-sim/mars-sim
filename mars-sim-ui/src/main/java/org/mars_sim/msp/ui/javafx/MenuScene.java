/**
 * Mars Simulation Project
 * MainWindowFX.java
 * @version 3.08 2015-01-28
 * @author Lars Næsbye Christensen
 */

package org.mars_sim.msp.ui.javafx;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import org.mars_sim.msp.ui.javafx.MainWindowFX;;


public class MenuScene {
	
    public Scene createMenuScene()  {
	
    Group  root  =  new  Group();
    Scene  menuScene  =  new  Scene(root);
    menuScene.getStylesheets().addAll("/fxui/css/menuskin.css");
    BorderPane borderpane = new BorderPane();
    VBox box = new VBox();
    Text mspLabel = new Text("Mars Simulation Project");
    mspLabel.setFont(new Font(35));
    mspLabel.setTextAlignment(TextAlignment.CENTER);
    Button newButton = new Button("Start New Simulation");
    newButton.setOnAction(new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent arg0) {
        	MainWindowFX.changeScene(3);
        }
    });
    Button loadButton = new Button("Load Saved Simulation");
    loadButton.setOnAction(new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent arg0) {
        	MainWindowFX.changeScene(3);
        }
    });
    Button exitButton = new Button("Exit to OS");
    exitButton.setOnAction(new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent arg0) {
        	MainWindowFX.changeScene(3);
        }
    });
    box.getChildren().addAll(mspLabel, newButton, loadButton, exitButton);
    borderpane.setCenter(box);
    borderpane.setCursor(Cursor.HAND);
    root.getChildren().add(borderpane);
    

    return (menuScene);
}

}

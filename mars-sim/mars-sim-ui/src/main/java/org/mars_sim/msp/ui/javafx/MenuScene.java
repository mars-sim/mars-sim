/**
 * Mars Simulation Project
 * MainWindowFX.java
 * @version 3.08 2015-01-28
 * @author Lars Næsbye Christensen
 */

package org.mars_sim.msp.ui.javafx;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class MenuScene {
	
    public Scene createMenuScene()  {
	
    Group  root  =  new  Group();
    Scene  menuScene  =  new  Scene(root);
    menuScene.getStylesheets().addAll("/fxui/css/menuskin.css");
    ImageView bg1 = new ImageView();
    bg1.setImage(new Image("/images/mars.png"));        
    root.getChildren().add(bg1);

    Button continueButton = new Button("Press me to go to Main Scene");
    continueButton.setOnAction(new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent arg0) {
        	// Send scene switch to MainWindowFX
        }
    });
    root.getChildren().add(continueButton);
    

    return (menuScene);
}

}

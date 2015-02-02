/**
 * Mars Simulation Project
 * MainWindowFX.java
 * @version 3.08 2015-01-28
 * @author Lars Næsbye Christensen
 */

package org.mars_sim.msp.ui.javafx;

import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;


public class MainScene {

    public Scene createMainScene() {
        Group  root  =  new  Group();
        Scene  scene  =  new  Scene(root);
		scene.getStylesheets().addAll("/fxui/css/mainskin.css");		

        MainWindowFXMenu menuBar = new MainWindowFXMenu(null);
		//MainToolBar toolBar = new MainToolBar();
        ImageView bg1 = new ImageView();
        bg1.setImage(new Image("/images/splash.png"));  // in lieu of the interactive Mars map      
        root.getChildren().add(bg1);
        //root.getChildren().add(toolBar);

        root.getChildren().add(menuBar); 
//        root.getChildren().add(new Button("Press me to go to Settlement Scene"));

        return (scene);
    }
	
	
}

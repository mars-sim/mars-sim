/**
 * Mars Simulation Project
 * DashboardController.java
 * @version 3.1.0 2017-10-18
 * @author Manny Kung
 */
package org.mars_sim.msp.ui.javafx.dashboard;


import com.jfoenix.controls.JFXButton;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.ui.javafx.MainScene;

import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.util.Duration;

@SuppressWarnings("restriction")
public class DashboardController implements Initializable {

    @FXML
    private AnchorPane mainPane;
    @FXML
    private AnchorPane insertPane;
    @FXML
    private AnchorPane settlerPane;

    @FXML
    private VBox leftVBox;
    
    @FXML
    private JFXButton btnHome;
    @FXML
    private JFXButton btnSettlers;

    @FXML
    private Label insertLabel;
    
    private SettlersController controller;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            //settlerPane = FXMLLoader.load(getClass().getResource("/fxui/fxml/dashboard/settlers.fxml"));
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxui/fxml/dashboard/settlers.fxml"));
            settlerPane = fxmlLoader.load();
        	controller = fxmlLoader.<SettlersController>getController();
            insertLabel.setText("Settlers List");
            setNode(settlerPane);
            	
        } catch (IOException ex) {
            Logger.getLogger(DashboardController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

	public void setSize(int screen_width, int screen_height){
		int h = screen_height - MainScene.TAB_PANEL_HEIGHT - 20;
		mainPane.setPrefSize(screen_width, screen_height);
    	leftVBox.setPrefHeight(h);
    	if (controller != null)
    		controller.setSize(screen_width, h);
    }
    
    //Set selected node to a content holder
    private void setNode(Node node) {
    	//if (controller != null)
    	//	controller.updateSettlers();
        insertPane.getChildren().clear();
        insertPane.getChildren().add((Node) node);

        FadeTransition ft = new FadeTransition(Duration.millis(1500));
        ft.setNode(node);
        ft.setFromValue(0.1);
        ft.setToValue(1);
        ft.setCycleCount(1);
        ft.setAutoReverse(false);
        ft.play();
    }

    @FXML
    private void updateSettlers(ActionEvent event) {
    	if (controller != null)
    		controller.updateSettlers();
        setNode(settlerPane);
    }

}

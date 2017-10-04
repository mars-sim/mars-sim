
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
import javafx.util.Duration;

/**
 *
 * @author danml
 */
@SuppressWarnings("restriction")
public class DashboardController implements Initializable {

    @FXML
    private AnchorPane anchorPane;
    @FXML
    private AnchorPane holderPane;
    @FXML
    private VBox leftVBox;
    
    @FXML
    private JFXButton btnHome;
    @FXML
    private JFXButton btnContacts;

    private AnchorPane settlerPane;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            settlerPane = FXMLLoader.load(getClass().getResource("/fxui/fxml/dashboard/settlers.fxml"));
            setNode(settlerPane);
        } catch (IOException ex) {
            Logger.getLogger(DashboardController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

	public void setSize(int screen_width, int screen_height){
    	anchorPane.setPrefSize(screen_width, screen_height);
    	leftVBox.setPrefHeight(screen_height - MainScene.TAB_PANEL_HEIGHT - 20);
    }
    
    //Set selected node to a content holder
    private void setNode(Node node) {
        holderPane.getChildren().clear();
        holderPane.getChildren().add((Node) node);

        FadeTransition ft = new FadeTransition(Duration.millis(1500));
        ft.setNode(node);
        ft.setFromValue(0.1);
        ft.setToValue(1);
        ft.setCycleCount(1);
        ft.setAutoReverse(false);
        ft.play();
    }

    @FXML
    private void switchPeople(ActionEvent event) {
        setNode(settlerPane);
    }

}

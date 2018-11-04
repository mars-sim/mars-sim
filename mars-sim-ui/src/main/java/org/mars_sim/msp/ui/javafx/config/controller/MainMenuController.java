/**
 * Mars Simulation Project
 * MainMenuController.java
 * @version 3.1.0 2017-05-08
 * @author Manny Kung
 */

package org.mars_sim.msp.ui.javafx.config.controller;

import java.net.URL;
import java.util.ResourceBundle;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.ui.javafx.mainmenu.ControlledScreen;
import org.mars_sim.msp.ui.javafx.mainmenu.MainMenu;
import org.mars_sim.msp.ui.javafx.mainmenu.ScreensSwitcher;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
//import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class MainMenuController implements Initializable, ControlledScreen {

	@FXML Label rotationRate;
	@FXML Label buildLabel;

	//@FXML private Button button0, button1, button2;

    ScreensSwitcher switcher;
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }

    public void setScreenParent(ScreensSwitcher screenParent){
        switcher = screenParent;
        setBuild();
    }

    //@FXML
    //private void onMouseExited(ActionEvent event) {
    //	menu.setFont("-fx-text-fill: #FFB03B;");
    //}

    //@FXML
    //private void enlarge(ActionEvent event){
    //	button0.setStyle("-fx-background-color:#dae7f3;");
    //}

    @FXML
    private void goToOne(ActionEvent event){
    	switcher.getMainMenu().runNew(false, false);
    }

    @FXML
    private void goToTwo(ActionEvent event){
    	switcher.getMainMenu().runLoad(false);
    }

    @FXML
    private void goToThree(ActionEvent event){
    	switcher.getMainMenu().runMultiplayer();
    }

    @FXML
    private void goToScreen2(ActionEvent event){
       switcher.setScreen(MainMenu.screen2ID);
    }

    @FXML
    private void goToScreen3(ActionEvent event){
       switcher.setScreen(MainMenu.screen3ID);
    }

    @FXML
    private void exit(ActionEvent event){
    	switcher.exitDialog(switcher.getMainMenu().getStage());
    }

    @FXML
    private void setDefaultRotation(ActionEvent event) {
    	rotationRate.setText("500");
    	switcher.getMainMenu().getSpinningGlobe().setDefaultRotation();
    }

    public void setRotation(int rate) {
    	rotationRate.setText(rate+"");
    }
    
    public void setBuild() {
    	String build = Simulation.BUILD;
    	buildLabel.setText(build);
    }
}

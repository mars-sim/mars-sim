/**
 * Mars Simulation Project
 * Screen3Controller.java
 * @version 3.1.0 2017-05-08
 * @author Manny Kung
 */

package org.mars_sim.msp.ui.javafx.config.controller;


import java.net.URL;
import java.util.ResourceBundle;

import org.mars_sim.msp.ui.javafx.mainmenu.ControlledScreen;
import org.mars_sim.msp.ui.javafx.mainmenu.MainMenu;
import org.mars_sim.msp.ui.javafx.mainmenu.ScreensSwitcher;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;


@SuppressWarnings("restriction")
public class Screen3Controller implements Initializable, ControlledScreen {

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
    }

    @FXML
    private void goToScreen1(ActionEvent event){
       switcher.setScreen(MainMenu.screen1ID);
    }
    
    @FXML
    private void goToScreen2(ActionEvent event){
       switcher.setScreen(MainMenu.screen2ID);
    }
}

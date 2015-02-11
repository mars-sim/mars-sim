package org.mars_sim.msp;
 

import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;


public class Screen2Controller implements Initializable , ControlledScreen {

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
    private void goToScreen3(ActionEvent event){
       switcher.setScreen(MainMenu.screen3ID);
    }
}

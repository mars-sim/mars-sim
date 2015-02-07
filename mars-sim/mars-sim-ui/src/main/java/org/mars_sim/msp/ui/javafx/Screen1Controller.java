package org.mars_sim.msp.ui.javafx;
 
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

public class Screen1Controller implements Initializable, ControlledScreen {

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
    private void goToOne(ActionEvent event){
    	switcher.getMainWindowFX().runOne();
    }
    
    @FXML
    private void goToTwo(ActionEvent event){
    	switcher.getMainWindowFX().runTwo();
    }
    
    @FXML
    private void goToThree(ActionEvent event){
    	switcher.getMainWindowFX().runThree();
    }
    
    @FXML
    private void goToScreen2(ActionEvent event){
       switcher.setScreen(MainWindowFX.screen2ID);
    }
    
    @FXML
    private void goToScreen3(ActionEvent event){
       switcher.setScreen(MainWindowFX.screen3ID);
    }
}

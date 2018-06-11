/**
 * Mars Simulation Project
 * OneSettlerController.java
 * @version 3.1.0 2017-10-18
 * @author Manny Kung
 */
package org.mars_sim.msp.ui.javafx.dashboard;

import com.jfoenix.controls.JFXButton;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import javafx.scene.control.Label;

public class OneSettlerController implements Initializable {

    @FXML
    private Label name;
    @FXML
    private Label role;
    @FXML
    private Label job;
    @FXML
    private Label shift;
    @FXML
    private Label location;
    
    @FXML
    private JFXButton locator;
    @FXML
    private JFXButton activity;
  
    @Override
    public void initialize(URL url, ResourceBundle rb) {

    }

    public void setInfo(String[] info) {
    	name.setText(info[0]);
    	job.setText(info[1]);
    	role.setText(info[2]);
    	shift.setText(info[3]);
    	location.setText(info[4]);	
    }
}

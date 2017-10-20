/**
 * Mars Simulation Project
 * SettlersController.java
 * @version 3.1.0 2017-10-18
 * @author Manny Kung
 */
package org.mars_sim.msp.ui.javafx.dashboard;


import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.person.Person;
//import org.mars_sim.msp.ui.javafx.MainScene;
import javafx.collections.ObservableList;

import javafx.animation.FadeTransition;

//import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
//import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.AnchorPane;
import javafx.scene.control.ScrollPane;
import javafx.util.Duration;
import javafx.collections.FXCollections;

@SuppressWarnings("restriction")
public class SettlersController implements Initializable {

	private final static int LEFT_PANEL_WIDTH = 135;
	
    @FXML
    private VBox innerVBox;

    @FXML
    private ScrollPane scrollPane;
    @FXML
    private AnchorPane pane0;
    @FXML
    private AnchorPane pane1;
/*    
    @FXML
    private AnchorPane pane2;
    @FXML
    private AnchorPane pane3;
*/
 /*
    @FXML
    private AnchorPane settlersPane;

    @FXML
    private VBox leftVBox;
    
    @FXML
    private JFXButton btnHome;
    @FXML
    private JFXButton btnContacts;

    @FXML
    private Label insertLabel;
 */
    
    //private List<Person> people; 
    
    private String[] info;
    
    private UnitManager unitManager;

    private ObservableList<Person> people;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
    	unitManager = Simulation.instance().getUnitManager();
    	//people = new ArrayList<>(unitManager.getPeople());
    	people = FXCollections.observableArrayList(unitManager.getPeople());

    	loadSettlers();
    }

	public void setSize(int screen_width, int screen_height) {
		scrollPane.setPrefWidth(screen_width - LEFT_PANEL_WIDTH);
		scrollPane.setPrefHeight(screen_height);
    }
    
    //Set selected node to a content holder
    private void setNode(Node node) {//, Node node1) {	
    	innerVBox.getChildren().add((Node) node);      

        FadeTransition ft = new FadeTransition(Duration.millis(1500));
        ft.setNode(node);
        ft.setFromValue(0.1);
        ft.setToValue(1);
        ft.setCycleCount(1);
        ft.setAutoReverse(false);
        ft.play();

    }

    
    private void loadSettlers() {
    	innerVBox.getChildren().clear();
    	
    	info = new String[5];
      
    	for (Person p : people) {
    		info[0] = p.getName();
    		info[1] = p.getMind().getJob().getName(p.getGender());
    		info[2] = p.getRole().toString();
    		info[3] = p.getTaskSchedule().getShiftType().getName() + " Shift";
    		info[4] = p.getLocationTag().getSettlementName();//.getLocationName();
        	load(info);
    	}

/*    	
    	for (int i=0; i<8; i++) {
    		info[0] = "Manny";
    		info[1] = "Engineer";
    		info[2] = "Commander";
    		info[3] = "A Shift";;
    		info[4] = "Alpha Base" ;
        	load(info);
    	} 	
*/  	
    }
    
    private void load(String[] info) {
        try {
        	//FXMLLoader fxmlLoader = FXMLLoader.load(getClass().getResource("/fxui/fxml/dashboard/oneSettler.fxml"));
        	FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxui/fxml/dashboard/oneSettler.fxml"));
        	AnchorPane pane = fxmlLoader.load();
        	OneSettlerController controller = fxmlLoader.<OneSettlerController>getController();
        	setNode(pane);
            controller.setInfo(info);
        		
        } catch (IOException ex) {
            Logger.getLogger(SettlersController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
     
    public void updateSettlers() {
    	loadSettlers();
    }

}

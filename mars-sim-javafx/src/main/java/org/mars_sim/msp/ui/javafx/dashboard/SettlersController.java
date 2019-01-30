/**
 * Mars Simulation Project
 * SettlersController.java
 * @version 3.1.0 2017-10-18
 * @author Manny Kung
 */
package org.mars_sim.msp.ui.javafx.dashboard;


import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.structure.Settlement;

import javafx.event.EventHandler;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import javafx.scene.layout.AnchorPane;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.ScrollEvent;
import javafx.util.Duration;

public class SettlersController implements Initializable {

	private final static int LEFT_PANEL_WIDTH = 135;
	
	private final static String SHIFT = " Shift";
	
    @FXML
    private VBox innerVBox;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private AnchorPane settlersPane;
   
    private String[] profile;
    
    private UnitManager unitManager;
    
    private Settlement lastSelected;
    
    
    private Collection<Person> people;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
    	
    	speedUpScroll();
    	
    	unitManager = Simulation.instance().getUnitManager();

    	// Get all people from all settlement
    	people = unitManager.getPeople();
    	//people = new ArrayList<>(unitManager.getPeople());
    	
    	// load settlers without modifying people instance
    	loadSettlers(null);
    }

    public void speedUpScroll() {
//    	final IntegerProperty vValueProperty = new SimpleIntegerProperty(0);
//        final int steps = 20;
//
//        scrollPane.vvalueProperty().bind(vValueProperty);
//
//        scrollPane.setOnSwipeDown(new EventHandler<GestureEvent>() {
//
//            public void handle(GestureEvent event) {
//                // calculate the needed value here
//                vValueProperty.set(vValueProperty.get() + steps);
//            }
//        });
        
        scrollPane.setContent(innerVBox);

        innerVBox.setOnScroll(new EventHandler<ScrollEvent>() {
            @Override
            public void handle(ScrollEvent event) {
            	// deltaY makes the scrolling a bit faster
                double deltaY = event.getDeltaY()*150; 
                double width = scrollPane.getContent().getBoundsInLocal().getWidth();
                double vvalue = scrollPane.getVvalue();
                scrollPane.setVvalue(vvalue + -deltaY/width); 
                // deltaY/width to make the scrolling equally fast regardless of the actual width of the component
            }
        });
        
    }
    
	public void setSize(int screen_width, int screen_height) {
		scrollPane.setPrefWidth(screen_width - LEFT_PANEL_WIDTH);
		scrollPane.setPrefHeight(screen_height);
    }
    
    // Set selected node to a content holder
    private void setupNode(Node node) {	
    	innerVBox.getChildren().add((Node) node);      

        FadeTransition ft = new FadeTransition(Duration.millis(1000));
        ft.setNode(node);
        ft.setFromValue(0.1);
        ft.setToValue(1);
        ft.setCycleCount(1);
        ft.setAutoReverse(false);
        ft.play();

    }

    
    private void loadSettlers(Settlement s) {
    	
    	// if s is null, load people from all settlements
    	if (s == null)
    		people = unitManager.getPeople();
    	else
        	people = s.getAllAssociatedPeople();
    		
    	innerVBox.getChildren().clear();
    	
    	if (s != null && lastSelected != null) {
    		if (!s.getName().equals(lastSelected.getName())) {
		    	scrollPane.setHvalue(0.0);
		    	scrollPane.setVvalue(0.0);
	    	}
    	}
    	
    	profile = new String[5];
      
    	List<Person> list = new ArrayList<>();
    	list.addAll(people);
    	Collections.sort(list);
    	
    	for (Person p : list) {
    		profile[0] = p.getName();
    		profile[1] = p.getMind().getJob().getName(p.getGender());
    		profile[2] = p.getRole().toString();
    		profile[3] = p.getTaskSchedule().getShiftType().getName() + SHIFT;
    		profile[4] = p.getLocationTag().getSettlementName();//.getLocationName();
        	load(profile);
    	}
    	
       	// Store the selected settlement
    	lastSelected = s;
        

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
        	setupNode(pane);
            controller.setInfo(info);
        		
        } catch (IOException ex) {
            Logger.getLogger(SettlersController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
     
    public void updateSettlers(Settlement s) {

    	loadSettlers(s);
    }
}

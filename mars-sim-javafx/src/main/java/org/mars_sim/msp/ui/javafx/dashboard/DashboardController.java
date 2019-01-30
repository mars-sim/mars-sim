/**
 * Mars Simulation Project
 * DashboardController.java
 * @version 3.1.0 2017-10-18
 * @author Manny Kung
 */
package org.mars_sim.msp.ui.javafx.dashboard;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXToolbar;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.javafx.MainScene;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.structure.Settlement;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.control.Label;

import javafx.util.Duration;
import jiconfont.icons.font_awesome.FontAwesome;
import jiconfont.javafx.IconNode;

public class DashboardController implements Initializable {

	private final static String SETTLERS_IN = " associated settlers in ";
	private final static String ALL_SETTLERS = " associated settlers from all settlements";
	private final static String PALE_BLUE = "#a1aec4";
	private final static String INSIDE = "Inside : ";
	private final static String OUTSIDE = "  Outside : ";	
//	private final static String SECS = " seconds ago";
	
    private static final Integer STARTTIME = 0;
    
	private IntegerProperty timeSeconds = new SimpleIntegerProperty(STARTTIME);
	
	private int num = 0;
	
    private Timeline timeline;
    
    @FXML
    private Label timerLabel;
    @FXML
    private JFXButton btnRefresh;
    
    @FXML
    private JFXToolbar toolBar;
    
    @FXML
    private JFXButton btnMars;
    @FXML
    private Label insertLabel;
    @FXML
    private Label secondTextLabel;
    
    @FXML
    private AnchorPane mainPane;
    @FXML
    private AnchorPane insertPane;
    @FXML
    private AnchorPane settlersPane;
    
    @FXML
    private VBox firstVBox;
    @FXML
    private VBox buttonVBox;
    @FXML
    private HBox buttonHBox;
    
    @FXML
    private HBox toolbarHBox;

//    @FXML
//    private ListView<Settlement> listView; 

    
    private UnitManager unitManager;
    
    private SettlersController controller;
    
    private List<Settlement> settlements = new ArrayList<>();
    
 
    @Override
    public void initialize(URL url, ResourceBundle rb) {
    	
    	unitManager = Simulation.instance().getUnitManager();
        	
    	// Obtain a new list of settlements
    	settlements = getNewSettlements();
    	
//    	toolBar.setLeftItems(toolbarHBox);
    	
    	initSButtons();
    	
        try {
            //settlerPane = FXMLLoader.load(getClass().getResource("/fxui/fxml/dashboard/settlers.fxml"));
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxui/fxml/dashboard/settlers.fxml"));
            settlersPane = fxmlLoader.load();
        	controller = fxmlLoader.<SettlersController>getController();
        	// TODO: why insertLabel doesn't have the right alignment
            //insertLabel.setText(ALL_SETTLERS);
            loadNode(settlersPane);
            	
        	//if (controller != null)
        	//	controller.updateSettlers(null);
         	int total = unitManager.getTotalNumPeople();
         	int outside = unitManager.getOutsidePeople().size();
         	int inside = total - outside;
            insertLabel.setText(total +  ALL_SETTLERS + " (" + INSIDE + inside + OUTSIDE + outside + ") :");
            
        } catch (IOException ex) {
            Logger.getLogger(DashboardController.class.getName()).log(Level.SEVERE, "Cannot load settlers.fxml", ex);
        }
    }

    public void createTimer() {
        // Bind the timerLabel text property to the timeSeconds property
    	timerLabel = new Label();
        timerLabel.textProperty().bind(timeSeconds.asString());
        timerLabel.setTextFill(Color.WHITE);
        //timerLabel.setStyle("-fx-font-size: 4em;");

        timeSeconds.set(STARTTIME);
        timeline = new Timeline();
        timeline.getKeyFrames().add(
                new KeyFrame(Duration.seconds(STARTTIME+1),
                new KeyValue(timeSeconds, 0)));
        timeline.playFromStart();
        
        btnRefresh = new JFXButton();
        btnRefresh.setText("Refresh");
        btnRefresh.setOnAction(e -> handleRefresh());
//        btnRefresh.setOnAction(new EventHandler<ActionEvent>() {
//
//            public void handle(ActionEvent event) {
//                if (timeline != null) {
//                    timeline.stop();
//                }
//                timeSeconds.set(STARTTIME);
//                timeline = new Timeline();
//                timeline.getKeyFrames().add(
//                        new KeyFrame(Duration.seconds(STARTTIME+1),
//                        new KeyValue(timeSeconds, 0)));
//                timeline.playFromStart();
//            }
//        });
    }
    
    /*
     * Obtain a new list of settlements
     */
    public List<Settlement> getNewSettlements() {
       	//System.out.println("getNewSettlements()");
    	List<Settlement> l = new ArrayList<>();
    	l.addAll(unitManager.getSettlements());
    	Collections.sort(l);
    	return l;
    }
    
    
    /*	
     * Check if any settlements have been added/deleted
     */    
    public void checkSettlements() {
       	//System.out.println("checkSettlements()");
		// check if any settlements have been added/deleted
    	List<Settlement> newList = getNewSettlements();
		int newNum = newList.size();
		
		if (newNum != num || settlements != newList) {
	    	// Obtain a new list of settlements
	    	settlements = getNewSettlements();
			removeButtons();
			initSButtons();
//			num = newList.size();
		}
    }
	
    public void removeButtons() {
       	//System.out.println("removeButtons()");
    	buttonVBox.getChildren().clear();
    }
    
    public void initSButtons() {
       	//System.out.println("initSButtons()");
//    	num = unitManager.getSettlementNum();
    	num = settlements.size();
    	//buttonVBox.getChildren().add(btnMars);
    	
    	for (int i=0; i<num; i++) {
//    		final ImageView imageView = new ImageView(new Image("http://icons.iconarchive.com/icons/eponas-deeway/colobrush/128/heart-2-icon.png") );
    		IconNode icon = new IconNode(FontAwesome.USERS);//.ADDRESS_BOOK_O);
    		icon.setIconSize(17);
    		icon.setFill(Color.web(PALE_BLUE));
    		icon.setWrappingWidth(25);
    		//size="17.0" wrappingWidth="43.0"
    		// icon.setStroke(Color.WHITE);
    		JFXButton btn = new JFXButton(settlements.get(i).getName(), icon);	
    		//btn.setMaxSize(20, 20);
    		//btn.setGraphic(icon);
    		buttonVBox.getChildren().add(btn);		
    		btn.setLayoutX(10);
    		btn.setLayoutY(109);
    		btn.setPrefHeight(42);
    		//btn.setPrefWidth(250);
    		btn.setAlignment(Pos.CENTER_LEFT);
    		btn.setStyle("jfxbutton");
    		btn.setTextFill(Color.LIGHTBLUE);
    		// layoutX="10.0" layoutY="109.0" onAction="#handleS0" prefHeight="42.0" prefWidth="139.0" 
            // style="-fx-alignment: center-left;" styleClass="jfxbutton" text="s0" textFill="#a1aec4"
    		final int x = i;
    		btn.setOnAction(e -> handleList(settlements.get(x)));
    	}
    	
    }
    
	public void setSize(int screen_width, int screen_height){
		int h = screen_height - MainScene.TAB_PANEL_HEIGHT;
		mainPane.setPrefSize(screen_width, screen_height);
    	buttonVBox.setPrefHeight(h-30);
    	firstVBox.setPrefHeight(h);
    	if (controller != null)
    		controller.setSize(screen_width, h);
    }
    
    
    // Load selected node to a content holder
    private void loadNode(Node node) {
    	//System.out.println("loadNode()");
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

    private void handleList(Settlement s) {
       	//System.out.println("handleList()");
     	if (controller != null)
    		controller.updateSettlers(s);
     	int total = s.getNumCitizens();
     	int inside = s.getIndoorPeopleCount();
     	int outside = total - inside;
        insertLabel.setText(total + SETTLERS_IN + s.getName() + " (" + INSIDE + inside + OUTSIDE + outside + ")");
        loadNode(settlersPane);
    }
    
    
    private void handleRefresh() {
        if (timeline != null) {
            timeline.stop();
        }
//        timeSeconds.set(STARTTIME);
        timeline.playFromStart();
//        timeline = new Timeline();
//        timeline.getKeyFrames().add(
//                new KeyFrame(Duration.seconds(STARTTIME+1),
//                new KeyValue(timeSeconds, 0)));
//        timeline.playFromStart();
    }
    
    
    @FXML
    private void handleMars(ActionEvent event) {
    	//System.out.println("handleMars()");
    	//settlements = getNewSettlements();
    	if (controller != null)
    		controller.updateSettlers(null);
     	int total = unitManager.getTotalNumPeople();
     	int outside = unitManager.getOutsidePeople().size();
     	int inside = total - outside;
        insertLabel.setText(total +  ALL_SETTLERS + " (" + INSIDE + inside + OUTSIDE + outside + ") :");
        loadNode(settlersPane);
    }
    
//    @FXML
//    private void handleS0(ActionEvent event) {
//    	settlements = FXCollections.observableList(unitManager.getSettlementOList());
//    	if (controller != null)
//    		controller.updateSettlers(settlements.get(0));
//        insertLabel.setText(settlements.get(0).getName());
//        loadNode(settlersPane);
//    }
//    
//    @FXML
//    private void handleS1(ActionEvent event) {
//    	settlements = FXCollections.observableList(unitManager.getSettlementOList());
//    	if (controller != null)
//    		controller.updateSettlers(settlements.get(1));
//        insertLabel.setText(settlements.get(1).getName());
//        loadNode(settlersPane);
//    }
//    
//    @FXML
//    private void handleS2(ActionEvent event) {
//    	settlements = FXCollections.observableList(unitManager.getSettlementOList());
//    	if (controller != null)
//    		controller.updateSettlers(settlements.get(2));
//        insertLabel.setText(settlements.get(2).getName());
//        loadNode(settlersPane);
//    }
}

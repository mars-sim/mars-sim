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

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.ui.javafx.MainScene;

import javafx.animation.FadeTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ListCell;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;
import jiconfont.icons.FontAwesome;
import jiconfont.javafx.IconNode;

public class DashboardController implements Initializable {

	private final static String SETTLERS_IN = "Settlers in ";
	private final static String ALL_SETTLERS = "Settlers from all settlements";
	private final static String PALE_BLUE = "#a1aec4";
	
    @FXML
    private AnchorPane mainPane;
    @FXML
    private AnchorPane insertPane;
//    @FXML
//    private AnchorPane settlersPane;
    @FXML
    private VBox buttonBox;
    @FXML
    private JFXButton btnHome;
    @FXML
    private JFXButton btnSettlers;
    @FXML
    private ListView<Settlement> listView; 
    @FXML
    private Label insertLabel;
    
    private AnchorPane settlersPane;
    
    private UnitManager unitManager;
    
    private SettlersController controller;
    
    private ObservableList<Settlement> settlements;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
    	
    	unitManager = Simulation.instance().getUnitManager();
    	settlements = FXCollections.observableList(unitManager.getSettlementOList());
    	int num = unitManager.getSettlementNum();

//		final ImageView imageView = new ImageView(new Image("http://icons.iconarchive.com/icons/eponas-deeway/colobrush/128/heart-2-icon.png") );
		

    	for (int i=0; i<num; i++) {
    		IconNode icon = new IconNode(FontAwesome.USERS);//.ADDRESS_BOOK_O);
    		icon.setIconSize(17);
    		icon.setFill(Color.web(PALE_BLUE));
    		icon.setWrappingWidth(25);
    		//size="17.0" wrappingWidth="43.0"
    		// icon.setStroke(Color.WHITE);
    		JFXButton btn = new JFXButton(settlements.get(i).getName(), icon);	
    		//btn.setMaxSize(20, 20);
    		//btn.setGraphic(icon);
    		buttonBox.getChildren().add(btn);		
    		btn.setLayoutX(10);
    		btn.setLayoutY(109);
    		btn.setPrefHeight(42);
    		btn.setPrefWidth(250);
    		btn.setAlignment(Pos.CENTER_LEFT);
    		btn.setStyle("nav-button");
    		btn.setTextFill(Color.LIGHTBLUE);
    		// layoutX="10.0" layoutY="109.0" onAction="#handleS0" prefHeight="42.0" prefWidth="139.0" 
            // style="-fx-alignment: center-left;" styleClass="nav-button" text="s0" textFill="#a1aec4"
    		final int x = i;
    		btn.setOnAction(e -> handleList(settlements.get(x)));
    	}
    	
        try {
            //settlerPane = FXMLLoader.load(getClass().getResource("/fxui/fxml/dashboard/settlers.fxml"));
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxui/fxml/dashboard/settlers.fxml"));
            settlersPane = fxmlLoader.load();
        	controller = fxmlLoader.<SettlersController>getController();
        	// TODO: why insertLabel doesn't have the right alignment
            insertLabel.setText(ALL_SETTLERS);
            loadNode(settlersPane);
            	
        } catch (IOException ex) {
            Logger.getLogger(DashboardController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


    private void handleList(Settlement s) {
     	if (controller != null)
    		controller.updateSettlers(s);
        insertLabel.setText(SETTLERS_IN + s.getName());
        loadNode(settlersPane);
    }
    
//    public void updateListView() {
//    	settlements = FXCollections.observableList(unitManager.getSettlementOList());
//    	//settlementButtons = new JFXButton[num-1];
//    	listView = new ListView<Settlement>(settlements);
//    	System.out.println("# of settlements : " + settlements.size());
//    	listView.setCellFactory(param -> new ListCell<Settlement>() {
//    	    @Override
//    	    protected void updateItem(Settlement s, boolean empty) {
//    	        super.updateItem(s, empty);
//
//    	        if (empty || s == null || s.getName() == null) {
//    	            setText(null);
//    	        } else {
//    	            setText(s.getName());
//    	        }
//    	    }
//    	});
//    	
//    	listView.setOnMouseClicked(new EventHandler<MouseEvent>() {
//	        @Override
//	        public void handle(MouseEvent event) {
//	            System.out.println("handle on " + listView.getSelectionModel().getSelectedItem());
//	        }
//    	});
//    }
    
//	@FXML 
//	public void handleMouseClick(MouseEvent arg0) {
//	    System.out.println("handleMouseClick on " + listView.getSelectionModel().getSelectedItem());
//	}
	
	public void setSize(int screen_width, int screen_height){
		int h = screen_height - MainScene.TAB_PANEL_HEIGHT - 20;
		mainPane.setPrefSize(screen_width, screen_height);
    	buttonBox.setPrefHeight(h);
    	if (controller != null)
    		controller.setSize(screen_width, h);
    }
    
    // Load selected node to a content holder
    private void loadNode(Node node) {

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
    private void handleHome(ActionEvent event) {
    	settlements = FXCollections.observableList(unitManager.getSettlementOList());
    	if (controller != null)
    		controller.updateSettlers(settlements.get(0));
        insertLabel.setText(ALL_SETTLERS);
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

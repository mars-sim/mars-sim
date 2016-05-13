/**
 * Mars Simulation Project
 * TabPanelDashboard.java
 * @version 3.07 2014-12-01
 * @author Manny Kung
 */
package org.mars_sim.msp.ui.swing.unit_window.structure;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.structure.ObjectiveType;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;

import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.effect.Reflection;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

/**
 * Tab panel displaying general info regarding the settlement <br>
 */
@SuppressWarnings("restriction")
public class TabPanelDashboard
extends TabPanel {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Data members
	private JFXPanel panel;
	private Scene scene;
	private StackPane stack;
	private Label objLabel;

	private String[] objectives;
	//private ComboBox<ObjectiveType> cb;
	private VBox toggleBox = new VBox();

	private Settlement settlement;
	
	/**
	 * Constructor.
	 * @param settlement {@link Settlement} the settlement this tab panel is for.
	 * @param desktop {@link MainDesktopPane} the main desktop panel.
	 */
	@SuppressWarnings("restriction")
	public TabPanelDashboard(Settlement settlement, MainDesktopPane desktop) {
		// Use the TabPanel constructor
		super(
			Msg.getString("TabPanelDashboard.title"), //$NON-NLS-1$
			null,
			Msg.getString("TabPanelDashboard.title.tooltip"), //$NON-NLS-1$
			settlement, 
			desktop
		);

		// Initialize data members.
		this.settlement = settlement;
		this.objectives = settlement.getObjectiveArray();
		//this.cb = new ComboBox<ObjectiveType>();//FXCollections.observableArrayList(objArray)); 
		//this.cb.getItems().setAll(ObjectiveType.values());   
		//setupChoiceBox();
		
		setupToggleGroup();
              
		this.panel = new JFXPanel();
		
       	int width = 400;
		int height = 500;

        Platform.runLater(new Runnable(){
            @Override
            public void run() {
                stack = new StackPane();
                stack.setStyle(
             		   "-fx-border-style: 2px; "
             		   //"-fx-background-color: #231d12; "
                			//+ "-fx-background-color: transparent; "
                			+ "-fx-background-radius: 2px;"
             		   );
                
                scene = new Scene(stack, width, height);
                scene.setFill(Color.TRANSPARENT);//.BLACK);
                panel.setScene(scene);

                Label title = new Label(Msg.getString("TabPanelDashboard.title"));
                Reflection reflection = new Reflection();
                title.setEffect(reflection);
                reflection.setTopOffset(0.0);
                title.setPadding(new Insets(5,5,0,5));
                //title.setFont(new Font("Arial", 20));
                title.setFont(Font.font("Cambria", FontWeight.BOLD, 16));

                //title.setAlignment(Pos.TOP_CENTER);
                //title.setSpacing(10);
                objLabel = new Label();
                objLabel.setText(settlement.getObjective().toString());
                objLabel.setAlignment(Pos.TOP_CENTER);
                //objLabel.setPadding(new Insets(5,5,5,5));
                //hello.setFill(Color.WHEAT);

                Label label = new Label("New objective set.");
                label.setFont(Font.font("Cambria", FontWeight.NORMAL, 13));
                label.setPadding(new Insets(5,5,5,5));
                label.setTextFill(Color.DARKCYAN);              
                label.setVisible(false);
                
                //HBox hbox = new HBox();
                //hbox.getChildren().addAll(label, cb);//, objLabel);                
                //hbox.setAlignment(Pos.TOP_CENTER);
                //hbox.setPadding(new Insets(5,5,5,5));
       
                VBox vbox0 = new VBox();
                vbox0.getChildren().addAll(toggleBox, label);
                vbox0.setAlignment(Pos.TOP_CENTER);
                vbox0.setPadding(new Insets(5,5,5,5));                            
                
                VBox vbox = new VBox();
                vbox.setAlignment(Pos.TOP_CENTER);
                //vbox.getChildren().addAll(title, cb, hbox);
                vbox.getChildren().addAll(title, new Label(), vbox0);
                
                stack.getChildren().add(vbox);

            }
        });
  
        centerContentPanel.add(panel);   
 		this.setSize(new Dimension(width, height));
        this.setVisible(true);
        
/*        
		JPanel centerPanel = new JPanel(new BorderLayout());
		centerPanel.setBorder(new MarsPanelBorder());
		centerContentPanel.add(centerPanel, BorderLayout.CENTER);
*/		

	}

/*
	@SuppressWarnings("restriction")
	public void setupChoiceBox() {

		cb.setTooltip(new Tooltip(Msg.getString("TabPanelDashboard.cb.tooltip")));		
		cb.getSelectionModel().select(0);		
		cb.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(javafx.beans.value.ObservableValue<? extends Number> observable, 
					Number oldValue, Number newValue) {			
				String choice = objectives[newValue.intValue()];
				
				ObjectiveType type = null;
				if (choice.equals(ObjectiveType.CROP_FARM.toString()))	
					type = ObjectiveType.CROP_FARM;
				else if (choice.equals(ObjectiveType.MANUFACTURING.toString()))	
					type = ObjectiveType.MANUFACTURING;
				else if (choice.equals(ObjectiveType.RESEARCH_CENTER.toString()))	
					type = ObjectiveType.RESEARCH_CENTER;
				else if (choice.equals(ObjectiveType.TRADE_TOWN.toString()))	
					type = ObjectiveType.TRADE_TOWN;
				else if (choice.equals(ObjectiveType.TRANSPORTATION_HUB.toString()))	
					type = ObjectiveType.TRANSPORTATION_HUB;

				settlement.setObjective(type);
			}
		});
	}
	
*/	
	
	public void setupToggleGroup() {
		
		String header = "Settlement's Overall Objective";
		ToggleGroup group = new ToggleGroup();
		group.setUserData(header);
		
		//group.setTooltip(new Tooltip(Msg.getString("TabPanelDashboard.cb.tooltip")));

		//String a = ObjectiveType.CROP_FARM.toString();
		//String b = ObjectiveType.MANUFACTURING.toString();
		//String c = ObjectiveType.RESEARCH_CENTER.toString();
		//String d = ObjectiveType.TRADE_TOWN.toString();
		//String e = ObjectiveType.TRANSPORTATION_HUB.toString();
		
		String a = objectives[0];
		String b = objectives[1];
		String c = objectives[2];
		String d = objectives[3];
		String e = objectives[4];
		
		RadioButton ra = new RadioButton(a);
		RadioButton rb = new RadioButton(b);
		RadioButton rc = new RadioButton(c);
		RadioButton rd = new RadioButton(d);
		RadioButton re = new RadioButton(e);

		ra.setUserData(a);
		rb.setUserData(b);
		rc.setUserData(c);
		rd.setUserData(d);
		re.setUserData(e);
		
		ra.setToggleGroup(group);
		rb.setToggleGroup(group);
		rc.setToggleGroup(group);
		rd.setToggleGroup(group);
		re.setToggleGroup(group);

		VBox options = new VBox();
		options.getChildren().addAll(ra, rb, rc, rd, re);
		TitledPane titledPane = new TitledPane(header, options);
		//titledPane.setId("titledpane");
	    titledPane.setPrefSize(100, 100);
	    //VBox vbox = new VBox();
		toggleBox.getChildren().add(titledPane);
		
		
		ra.setSelected(true);
		
		group.selectedToggleProperty().addListener(new ChangeListener<Toggle>(){
		    public void changed(ObservableValue<? extends Toggle> ov,
		        Toggle old_toggle, Toggle new_toggle) {
		
	            if (group.getSelectedToggle() != null) {
	            	String choice = group.getSelectedToggle().getUserData().toString();
	
					ObjectiveType type = null;
					
					if (choice.equals(ObjectiveType.CROP_FARM.toString()))	
						type = ObjectiveType.CROP_FARM;
					else if (choice.equals(ObjectiveType.MANUFACTURING.toString()))	
						type = ObjectiveType.MANUFACTURING;
					else if (choice.equals(ObjectiveType.RESEARCH_CENTER.toString()))	
						type = ObjectiveType.RESEARCH_CENTER;
					else if (choice.equals(ObjectiveType.TRADE_TOWN.toString()))	
						type = ObjectiveType.TRADE_TOWN;
					else if (choice.equals(ObjectiveType.TRANSPORTATION_HUB.toString()))	
						type = ObjectiveType.TRANSPORTATION_HUB;
					
					settlement.setObjective(type);
	            }
		    }
		});
		
	}
	
	@Override
	public void update() {
		
	}

}
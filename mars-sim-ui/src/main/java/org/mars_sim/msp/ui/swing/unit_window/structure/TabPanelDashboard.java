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
import javafx.scene.control.Tooltip;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
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

	private String[] objArray;
	private ComboBox<ObjectiveType> cb;

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
		this.objArray = settlement.getObjArray();
		this.cb = new ComboBox<ObjectiveType>();//FXCollections.observableArrayList(objArray)); 
		this.cb.getItems().setAll(ObjectiveType.values()); 
       
		setupChoiceBox();
              
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

                Label label = new Label("Settlement's Overall Objective : ");
                label.setFont(Font.font("Cambria", FontWeight.NORMAL, 13));
                label.setPadding(new Insets(5,5,5,5));
                
                HBox hbox = new HBox();
                hbox.getChildren().addAll(label, cb);//, objLabel);
                hbox.setAlignment(Pos.TOP_CENTER);
                hbox.setPadding(new Insets(5,5,5,5));
                //HBox cbox = new HBox();
                //cbox.getChildren().addAll(new Label("New Strategy : "), objLabel);
                //cbox.setAlignment(Pos.TOP_CENTER);
                
                VBox vbox = new VBox();
                vbox.setAlignment(Pos.TOP_CENTER);
                //vbox.getChildren().addAll(title, cb, hbox);
                vbox.getChildren().addAll(title, new Label(), hbox);
                
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


	@SuppressWarnings("restriction")
	public void setupChoiceBox() {

		cb.setTooltip(new Tooltip(Msg.getString("TabPanelDashboard.cb.tooltip")));		
		cb.getSelectionModel().select(0);		
		cb.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(javafx.beans.value.ObservableValue<? extends Number> observable, 
					Number oldValue, Number newValue) {			
				String choice = objArray[newValue.intValue()];
				
				ObjectiveType type = null;
				if (choice.equals(ObjectiveType.CROP_FARM.toString()))	
					type = ObjectiveType.CROP_FARM;
				else if (choice.equals(ObjectiveType.MANUFACTURING.toString()))	
					type = ObjectiveType.MANUFACTURING;
				else if (choice.equals(ObjectiveType.RESEARCH_CENTER.toString()))	
					type = ObjectiveType.RESEARCH_CENTER;
				else if (choice.equals(ObjectiveType.TRADE_TOWN.toString()))	
					type = ObjectiveType.MANUFACTURING;
				else if (choice.equals(ObjectiveType.TRANSPORTATION_HUB.toString()))	
					type = ObjectiveType.TRANSPORTATION_HUB;

			}
		});
	}
	
	
	@Override
	public void update() {
		
	}

}
/**
 * Mars Simulation Project
 * ModifyTransportItemDialog.java
 * @version 3.07 2014-12-06
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.resupply;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javafx.embed.swing.SwingNode;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.mars_sim.msp.core.interplanetary.transport.Transportable;
import org.mars_sim.msp.core.interplanetary.transport.resupply.Resupply;
import org.mars_sim.msp.core.interplanetary.transport.settlement.ArrivingSettlement;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;

/**
 * A dialog for modifying transport items.
 * TODO externalize strings
 */
public class ModifyTransportItemDialog
//extends JDialog 
{

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Data members.
	private Transportable transportItem;
	private TransportItemEditingPanel editingPanel;

	//private TransportItemEditingStage editingStage;
	
	private Stage s;
	private JDialog d;
	
	/**
	 * Constructor.
	 * @param owner the owner of this dialog.
	 * @param title title of dialog.
	 * @param transportItem the transport item to modify.
	 */
	public ModifyTransportItemDialog(JFrame owner, String title, Transportable transportItem) {
		// Use JDialog constructor.
		//super(owner, "Modify Transport Item", true);
		
		d = new JDialog(owner, "Modify Transport Item", true);		
		// Initialize data members.
		this.transportItem = transportItem;

		// Set the layout.
		d.setLayout(new BorderLayout(0, 0));

		// Set the border.
		((JComponent) d.getContentPane()).setBorder(new MarsPanelBorder());

		init();
		
		d.getContentPane().add(editingPanel, BorderLayout.CENTER);

		// Create the button pane.
		JPanel buttonPane = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
		d.getContentPane().add(buttonPane, BorderLayout.SOUTH);

		// Create modify button.
		// 9/29/2014 by mkung: Changed button text from "Modify" to "Commit Changes"
		JButton modifyButton = new JButton("Commit Changes");
		modifyButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				// Modify transport item and close dialog.
				modifyTransportItem();
			}
		});
		buttonPane.add(modifyButton);

		// Create cancel button.
		// 9/29/2014 by mkung: Changed button text from "Cancel"  to "Discard Changes"
		JButton cancelButton = new JButton("Discard Changes");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				// Close dialog.
				d.dispose();
			}

		});
		buttonPane.add(cancelButton);

		
		d.setLocationRelativeTo(owner);
		// Finish and display dialog.
		d.pack();
		d.setResizable(false);
		d.setVisible(true);	
		
	}

	public ModifyTransportItemDialog(Stage stage, String title, Transportable transportItem) {
		// Use JDialog constructor.
		//super(stage, "Modify Transport Item", true);

		// Initialize data members.
		this.transportItem = transportItem;

		/*
		StackPane stackPane = new StackPane();
		StackPane stackPane = new StackPane();
		  stackPane.setMinSize(0, 0);
		  stackPane.setAlignment(Pos.CENTER);
		  stackPane.getChildren().add(editingStage);
		 */ 
		  
		Stage s = new Stage(StageStyle.TRANSPARENT);
        s.setTitle("Title");
        s.setResizable(false);
        s.initModality(Modality.APPLICATION_MODAL);
        s.initOwner(stage);
        s.initStyle(StageStyle.UTILITY);


		BorderPane bp = new BorderPane();
        Scene scene = new Scene(bp);
        s.setScene(scene);

		init();

		final SwingNode swingNode = new SwingNode();
		createSwingNode(swingNode);
		//.getChildren().add(editingPanel, BorderPane.CENTER);
		bp.setCenter(swingNode);
		
		// Create the button HBox.
		HBox hb = new HBox();
		bp.setBottom(hb);
		
		
		// Create modify button.
		// 9/29/2014 by mkung: Changed button text from "Modify" to "Commit Changes"
		Button modifyButtonFX = new Button("Commit Changes");
		modifyButtonFX.setStyle("-fx-font: 22 arial; -fx-base: #b6e7c9;");
		modifyButtonFX.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
			@Override
			    public void handle(javafx.event.ActionEvent e) {
			 		modifyTransportItem();
			    }
			});

		// Create cancel button.
		// 9/29/2014 by mkung: Changed button text from "Cancel"  to "Discard Changes"
		Button cancelButtonFX = new Button("Discard Changes");
		cancelButtonFX.setStyle("-fx-font: 22 arial; -fx-base: #b6e7c9;");
		cancelButtonFX.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
			@Override
		    public void handle(javafx.event.ActionEvent e) {
				s.close();
		    }
		});

		/*
		DropShadow shadow = new DropShadow();
		//Adding the shadow when the mouse cursor is on
		cancelButtonFX.javafx.event.addEventHandler(javafx.event.MouseEvent.MOUSE_ENTERED, 
		    new javafx.event.EventHandler<javafx.event.MouseEvent>() {
		        @Override public void handle(javafx.event.MouseEvent e) {
		        	cancelButtonFX.setEffect(shadow);
		        }
		});
		//Removing the shadow when the mouse cursor is off
		cancelButtonFX.addEventHandler(MouseEvent.MOUSE_EXITED, 
		    new EventHandler<MouseEvent>() {
		        @Override public void handle(MouseEvent e) {
		        	cancelButtonFX.setEffect(null);
		        }
		});
		*/
		hb.getChildren().addAll(modifyButtonFX, cancelButtonFX);
		
	}
	
	private void createSwingNode(final SwingNode swingNode) {
        SwingUtilities.invokeLater(() -> {
            swingNode.setContent(editingPanel);           
        });
    }
	
	
	public void init() {
		
		
		// Create editing panel.
		editingPanel = null;
		if (transportItem instanceof ArrivingSettlement) {
			editingPanel = new ArrivingSettlementEditingPanel((ArrivingSettlement) transportItem);
		}
		else if (transportItem instanceof Resupply) {
			editingPanel = new ResupplyMissionEditingPanel((Resupply) transportItem);
		}
		else {
			throw new IllegalStateException("Transport item: " + transportItem + " is not valid.");
		}
		
	}
		
	
	/**
	 * Modify the transport item and close the dialog.
	 */
	private void modifyTransportItem() {
		if ((editingPanel != null) && editingPanel.modifyTransportItem()) {
			if ( d !=null ) d.dispose();
			if ( s != null ) s.close();
		}
	}
}
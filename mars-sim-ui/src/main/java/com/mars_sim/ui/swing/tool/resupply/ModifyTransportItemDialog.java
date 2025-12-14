/*
 * Mars Simulation Project
 * ModifyTransportItemDialog.java
 * @date 2022-07-20
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.tool.resupply;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import com.mars_sim.core.Simulation;
import com.mars_sim.core.interplanetary.transport.Transportable;
import com.mars_sim.core.interplanetary.transport.resupply.Resupply;
import com.mars_sim.core.interplanetary.transport.settlement.ArrivingSettlement;

/**
 * A dialog for modifying transport items.
 */
@SuppressWarnings("serial")
public class ModifyTransportItemDialog extends JDialog {

	// Data members.
	private Transportable transportItem;
	private TransportItemEditingPanel editingPanel;
	private ResupplyWindow resupplyWindow;

	private JButton commitButton;
	private Simulation simulation;
	
	/**
	 * Constructor.
	 * 
	 * @param parent the parent frame
	 * @param resupplyWindow the resupply window
	 * @param title title of dialog
	 * @param transportItem the transport item to modify
	 * @param simulation the simulation instance
	 */
	public ModifyTransportItemDialog(Frame parent, ResupplyWindow resupplyWindow, String title, 
									Transportable transportItem, Simulation simulation) {

		// Use JDialog constructor
        super(parent, "Modify Mission", true); // true for modal

		// Initialize data members.
		this.transportItem = transportItem;
		this.resupplyWindow = resupplyWindow;
		this.simulation = simulation;

		this.setSize(560, 500);

		 // Create main panel
        JPanel mainPane = new JPanel(new BorderLayout());
        setContentPane(mainPane);

        initEditingPanel();

		mainPane.add(editingPanel, BorderLayout.CENTER);

		// Create the button pane.
		JPanel buttonPane = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));

		mainPane.add(buttonPane, BorderLayout.SOUTH);

		// Create commit button.
		commitButton = new JButton("Commit Changes");
		commitButton.addActionListener(e -> 
				// Modify transport item and close dialog.
				modifyTransportItem());
		buttonPane.add(commitButton);

		// Create cancel button.
		// Change button text from "Cancel"  to "Discard Changes"
		JButton cancelButton = new JButton("Discard Changes");
		cancelButton.addActionListener(e -> 
				// Close dialog.
				dispose());
		buttonPane.add(cancelButton);
		
		// Set dialog behavior
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setLocationRelativeTo(parent);
	}

	public void initEditingPanel() {

		// Create editing panel.
		editingPanel = switch (transportItem) {
   			case ArrivingSettlement as ->
  				new ArrivingSettlementEditingPanel(simulation, as, this, null);
    		case Resupply r -> new ResupplyMissionEditingPanel(simulation, r, this, null);
    		default -> throw new IllegalStateException("Transport item: " + transportItem + " is not valid.");
  		};
	}


	/**
	 * Modify the transport item and close the dialog.
	 */
	private void modifyTransportItem() {
		if ((editingPanel != null) && editingPanel.modifyTransportItem()) {
			dispose();
			resupplyWindow.refreshMission();
		}
	}
	
	public void setCommitButton(boolean value) {
		SwingUtilities.invokeLater(() -> commitButton.setEnabled(value));
	}
	
}

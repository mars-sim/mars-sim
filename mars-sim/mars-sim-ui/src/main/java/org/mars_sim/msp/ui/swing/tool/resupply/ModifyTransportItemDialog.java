/**
 * Mars Simulation Project
 * ModifyTransportItemDialog.java
 * @version 3.06 2014-09-29
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.resupply;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.mars_sim.msp.core.interplanetary.transport.Transportable;
import org.mars_sim.msp.core.interplanetary.transport.resupply.Resupply;
import org.mars_sim.msp.core.interplanetary.transport.settlement.ArrivingSettlement;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;

/**
 * A dialog for modifying transport items.
 * TODO externalize strings
 */
public class ModifyTransportItemDialog
extends JDialog {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Data members.
	private Transportable transportItem;
	private TransportItemEditingPanel editingPanel;

	/**
	 * Constructor.
	 * @param owner the owner of this dialog.
	 * @param title title of dialog.
	 * @param transportItem the transport item to modify.
	 */
	public ModifyTransportItemDialog(JFrame owner, String title, Transportable transportItem) {
		// Use JDialog constructor.
		super(owner, "Modify Transport Item", true);

		// Initialize data members.
		this.transportItem = transportItem;

		// Set the layout.
		setLayout(new BorderLayout(0, 0));

		// Set the border.
		((JComponent) getContentPane()).setBorder(new MarsPanelBorder());

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
		getContentPane().add(editingPanel, BorderLayout.CENTER);

		// Create the button pane.
		JPanel buttonPane = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
		getContentPane().add(buttonPane, BorderLayout.SOUTH);

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
				dispose();
			}

		});
		buttonPane.add(cancelButton);

		// Finish and display dialog.
		pack();
		setLocationRelativeTo(owner);
		setResizable(false);
		setVisible(true);
	}

	/**
	 * Modify the transport item and close the dialog.
	 */
	private void modifyTransportItem() {
		if ((editingPanel != null) && editingPanel.modifyTransportItem()) {
			dispose();
		}
	}
}
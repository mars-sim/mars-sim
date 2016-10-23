/**
 * Mars Simulation Project
 * ModifyTransportItemDialog.java
 * @version 3.08 2015-06-30
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.resupply;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

import org.mars_sim.msp.core.interplanetary.transport.Transportable;
import org.mars_sim.msp.core.interplanetary.transport.resupply.Resupply;
import org.mars_sim.msp.core.interplanetary.transport.settlement.ArrivingSettlement;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.ModalInternalFrame;

/**
 * A dialog for modifying transport items.
 * TODO externalize strings
 */
public class ModifyTransportItemDialog extends ModalInternalFrame {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Data members.
	private Transportable transportItem;
	private TransportItemEditingPanel editingPanel;
	private ResupplyWindow resupplyWindow;

	private JButton modifyButton;
	
	/**
	 * Constructor.
	 * @param owner the owner of this dialog.
	 * @param title title of dialog.
	 * @param transportItem the transport item to modify.
	 */
	//2015-03-21 Switched from using JFrame to using desktop in param
	public ModifyTransportItemDialog(MainDesktopPane desktop, ResupplyWindow resupplyWindow, String title, Transportable transportItem) {// , boolean isFX) {

		// Use ModalInternalFrame constructor
        super("Modify Mission");

		// Initialize data members.
		this.transportItem = transportItem;
		this.resupplyWindow = resupplyWindow;

		this.setSize(560,500);

		 // Create main panel
        JPanel mainPane = new JPanel(new BorderLayout());
        setContentPane(mainPane);

        initEditingPanel();

		mainPane.add(editingPanel, BorderLayout.CENTER);

		// Create the button pane.
		JPanel buttonPane = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));

		mainPane.add(buttonPane, BorderLayout.SOUTH);

		// Create modify button.
		// 9/29/2014 by mkung: Changed button text from "Modify" to "Commit Changes"
		modifyButton = new JButton("Commit Changes");
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

        // 2016-10-22 Add to its own tab pane
        if (desktop.getMainScene() != null)
        	desktop.getMainScene().getDesktops().get(2).add(this);
        else 
        	desktop.add(this);    

		Dimension desktopSize = desktop.getParent().getSize();
	    Dimension jInternalFrameSize = this.getSize();
	    int width = (desktopSize.width - jInternalFrameSize.width) / 2;
	    int height = (desktopSize.height - jInternalFrameSize.height) / 2;

	    setLocation(width, height);
	    setModal(true);
	    setVisible(true);
	}

	public void initEditingPanel() {

		// Create editing panel.
		editingPanel = null;
		if (transportItem instanceof ArrivingSettlement) {
			editingPanel = new ArrivingSettlementEditingPanel((ArrivingSettlement) transportItem, resupplyWindow, this, null);
		}
		else if (transportItem instanceof Resupply) {
			editingPanel = new ResupplyMissionEditingPanel((Resupply) transportItem, resupplyWindow, this, null);
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
			//if ( d != null ) d.dispose();
			//if ( s != null ) s.close();
			dispose();
		}
	}
	
	public void setModifyButton(boolean value) {
		modifyButton.setEnabled(value);
	}
	
}
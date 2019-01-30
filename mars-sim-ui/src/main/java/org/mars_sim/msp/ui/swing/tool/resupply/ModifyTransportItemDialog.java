/**
 * Mars Simulation Project
 * ModifyTransportItemDialog.java
 * @version 3.1.0 2017-02-03
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.resupply;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.SwingUtilities;

import org.mars_sim.msp.core.interplanetary.transport.Transportable;
import org.mars_sim.msp.core.interplanetary.transport.resupply.Resupply;
import org.mars_sim.msp.core.interplanetary.transport.settlement.ArrivingSettlement;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.ModalInternalFrame;

import com.alee.laf.button.WebButton;
import com.alee.laf.panel.WebPanel;

/**
 * A dialog for modifying transport items.
 * TODO externalize strings
 */
public class ModifyTransportItemDialog extends ModalInternalFrame {

	// Data members.
	private Transportable transportItem;
	private TransportItemEditingPanel editingPanel;
	private ResupplyWindow resupplyWindow;

	private WebButton commitButton;
	
	/**
	 * Constructor.
	 * @param owner the owner of this dialog.
	 * @param title title of dialog.
	 * @param transportItem the transport item to modify.
	 */
	public ModifyTransportItemDialog(MainDesktopPane desktop, ResupplyWindow resupplyWindow, String title, Transportable transportItem) {// , boolean isFX) {

		// Use ModalInternalFrame constructor
        super("Modify Mission");

		// Initialize data members.
		this.transportItem = transportItem;
		this.resupplyWindow = resupplyWindow;

		this.setSize(560,500);

		 // Create main panel
        WebPanel mainPane = new WebPanel(new BorderLayout());
        setContentPane(mainPane);

        initEditingPanel();

		mainPane.add(editingPanel, BorderLayout.CENTER);

		// Create the button pane.
		WebPanel buttonPane = new WebPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));

		mainPane.add(buttonPane, BorderLayout.SOUTH);

		// Create commit button.
		// Change button text from "Modify" to "Commit Changes"
		commitButton = new WebButton("Commit Changes");
		commitButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				// Modify transport item and close dialog.
				modifyTransportItem();
			}
		});
		buttonPane.add(commitButton);

		// Create cancel button.
		// Change button text from "Cancel"  to "Discard Changes"
		WebButton cancelButton = new WebButton("Discard Changes");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				// Close dialog.
				dispose();
			}

		});
		buttonPane.add(cancelButton);

        // Add to its own tab pane
//        if (desktop.getMainScene() != null)
//        	desktop.add(this);
//        	//desktop.getMainScene().getDesktops().get(2).add(this);
//        else 
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
			//resupplyWindow.setRunning(false);
		}
	}
	
	public void setCommitButton(boolean value) {
		System.out.println("set commit button to " + value);
		SwingUtilities.invokeLater(() -> commitButton.setEnabled(value));
	    //setVisible(true);
	}
	
}
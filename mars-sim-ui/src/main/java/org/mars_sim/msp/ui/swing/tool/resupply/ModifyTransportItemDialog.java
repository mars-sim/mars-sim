/**
 * Mars Simulation Project
 * ModifyTransportItemDialog.java
 * @version 3.08 2015-03-21
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.resupply;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;

import org.mars_sim.msp.core.interplanetary.transport.Transportable;
import org.mars_sim.msp.core.interplanetary.transport.resupply.Resupply;
import org.mars_sim.msp.core.interplanetary.transport.settlement.ArrivingSettlement;
import org.mars_sim.msp.ui.swing.MainDesktopPane;

/**
 * A dialog for modifying transport items.
 * TODO externalize strings
 */
//2015-03-21 Switched from extending JDialog to JinternalFrame
public class ModifyTransportItemDialog extends JInternalFrame {

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
	//2015-03-21 Switched from using JFrame to using desktop in param
	public ModifyTransportItemDialog(MainDesktopPane desktop, String title, Transportable transportItem) {// , boolean isFX) {

		// Use JInternalFrame constructor
        super("Modify Mission", false, true, false, true);

		// Initialize data members.
		this.transportItem = transportItem;

		this.setSize(500,500);
		
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
	    
	    //SwingUtilities.invokeLater(() -> desktop.add(this)) ; //createSwing(swingNode));
	    //if(isFX) 
	    desktop.add(this);	    
	    
		Dimension desktopSize = desktop.getParent().getSize();
	    Dimension jInternalFrameSize = this.getSize();
	    int width = (desktopSize.width - jInternalFrameSize.width) / 2;
	    int height = (desktopSize.height - jInternalFrameSize.height) / 2;
	    this.setLocation(width, height);
	    this.setVisible(true);

	}
	
	public void initEditingPanel() {	
		
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
			//if ( d != null ) d.dispose();
			//if ( s != null ) s.close();
			dispose();
		}
	}
}
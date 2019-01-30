/**
 * Mars Simulation Project
 * NewTransportItemDialog.java
 * @version 3.1.0 2017-02-03
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.resupply;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JComponent;

import org.mars_sim.msp.ui.swing.JComboBoxMW;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.ModalInternalFrame;

import com.alee.laf.button.WebButton;
import com.alee.laf.panel.WebPanel;

/**
 * A dialog for creating a new transport item.
 * TODO externalize strings
 */
public class NewTransportItemDialog extends ModalInternalFrame {

	// Transport item types.
	private static final String DEFAULT_MESSAGE = "Select Transport Item Type";
	private static final String RESUPPLY_MISSION = "New Resupply Mission";
	private static final String ARRIVING_SETTLEMENT = "New Arriving Settlement";

	// Data members.
	private TransportItemEditingPanel editingPanel;
	private WebPanel mainEditingPanel;
	private CardLayout mainEditingLayout;
	private WebPanel emptyPanel;
	private TransportItemEditingPanel resupplyMissionPanel;
	private TransportItemEditingPanel arrivingSettlementPanel;
	private WebButton createButton;

	private ResupplyWindow resupplyWindow;
	
	/**
	 * Constructor.
	 * @param MainDesktopPane desktop
	 * @param transportItem the transport item to modify.
	 */
	public NewTransportItemDialog(MainDesktopPane desktop, ResupplyWindow resupplyWindow) {

		// Use ModalInternalFrame constructor
        super("New Transport Item");
        this.resupplyWindow = resupplyWindow;

		setSize(580, 600);

		 // Create main panel
		WebPanel mainPane = new WebPanel(new BorderLayout());
        setContentPane(mainPane);

		// Set the border.
		((JComponent) getContentPane()).setBorder(new MarsPanelBorder());

		// Create transport type panel.
		WebPanel transportTypePanel = new WebPanel(new FlowLayout(10, 10, FlowLayout.CENTER));
		getContentPane().add(transportTypePanel, BorderLayout.NORTH);

		// Create combo box for determining transport item type.
		JComboBoxMW<String> typeBox = new JComboBoxMW<>();
		typeBox.addItem(DEFAULT_MESSAGE);
		typeBox.addItem(RESUPPLY_MISSION);
		typeBox.addItem(ARRIVING_SETTLEMENT);
		typeBox.setSelectedItem(DEFAULT_MESSAGE);
		typeBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				JComboBox<?> cb = (JComboBox<?>) evt.getSource();
				setEditingPanel((String) cb.getSelectedItem());
			}
		});
		transportTypePanel.add(typeBox);

		// Create main editing panel.
		mainEditingLayout = new CardLayout();
		mainEditingPanel = new WebPanel(mainEditingLayout);
		getContentPane().add(mainEditingPanel, BorderLayout.CENTER);

		// Create empty default panel.
		emptyPanel = new WebPanel();
		emptyPanel.setBorder(new MarsPanelBorder());
		mainEditingPanel.add(emptyPanel, DEFAULT_MESSAGE);

		// Create resupply mission editing panel.
		resupplyMissionPanel = new ResupplyMissionEditingPanel(null, resupplyWindow, null, this); // resupply cannot be null !
		mainEditingPanel.add(resupplyMissionPanel, RESUPPLY_MISSION);

		// Create arriving settlement editing panel.
		arrivingSettlementPanel = new ArrivingSettlementEditingPanel(null, resupplyWindow, null, this); // resupply cannot be null !
		mainEditingPanel.add(arrivingSettlementPanel, ARRIVING_SETTLEMENT);

		// Create the button pane.
		WebPanel buttonPane = new WebPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
		getContentPane().add(buttonPane, BorderLayout.SOUTH);

		// Create create button.
		createButton = new WebButton("Create");
		createButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				// Create transport item and close dialog.
				createTransportItem();
			}
		});
		createButton.setEnabled(false);
		buttonPane.add(createButton);

		// Create cancel button.
		WebButton cancelButton = new WebButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				// Close dialog.
				dispose();
				//resupplyWindow.setRunning(false);
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

	/**
	 * Set the editing panel.
	 * 
	 * @param panelKey the panel key string.
	 */
	private void setEditingPanel(String panelKey) {

		if (panelKey != null) {
			mainEditingLayout.show(mainEditingPanel, panelKey);

			if (panelKey.equals(DEFAULT_MESSAGE)) {
				editingPanel = null;
				createButton.setEnabled(false);
			}
			else if (panelKey.equals(RESUPPLY_MISSION)) {
				editingPanel = resupplyMissionPanel;
				createButton.setEnabled(true);
			}
			else if (panelKey.equals(ARRIVING_SETTLEMENT)) {
				editingPanel = arrivingSettlementPanel;
				createButton.setEnabled(true);
			}
		}
	}

	public void setCreateButton(boolean value) {
		createButton.setEnabled(value);
	}

	/**
	 * Create the new transport item and close the dialog.
	 */
	private void createTransportItem() {
		if ((editingPanel != null) && editingPanel.createTransportItem()) {
			dispose();
		}
	}

	/**
	 * Prepare this window for deletion.
	 */
	public void destroy() {

		editingPanel  = null;
		mainEditingPanel  = null;
		mainEditingLayout  = null;
		emptyPanel  = null;
		resupplyMissionPanel  = null;
		arrivingSettlementPanel  = null;
		createButton  = null;
		resupplyWindow  = null;
	}



}
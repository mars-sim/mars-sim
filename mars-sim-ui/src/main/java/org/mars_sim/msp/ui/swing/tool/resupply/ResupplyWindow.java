/**
 * Mars Simulation Project
 * ResupplyWindow.java
 * @version 3.1.0 2017-02-03
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.resupply;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.interplanetary.transport.Transportable;
import org.mars_sim.msp.core.interplanetary.transport.resupply.Resupply;
import org.mars_sim.msp.core.interplanetary.transport.settlement.ArrivingSettlement;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MainWindow;
import org.mars_sim.msp.ui.swing.toolWindow.ToolWindow;

import com.alee.laf.button.WebButton;
import com.alee.laf.panel.WebPanel;

/**
 * Window for the resupply tool.
 * TODO externalize strings
 */

public class ResupplyWindow
extends ToolWindow
implements ListSelectionListener {

	/** Tool name. */
	public static final String NAME = "Resupply Tool";

	// Data members
	//private boolean isRunning = false;
	private IncomingListPanel incomingListPane;
	private ArrivedListPanel arrivedListPane;
	private TransportDetailPanel detailPane;
	private WebButton modifyButton;
	private WebButton cancelButton;

	private MainDesktopPane desktop;
//	private MainScene mainScene;

	/**
	 * Constructor.
	 * @param desktop the main desktop panel.
	 */
	public ResupplyWindow(MainDesktopPane desktop) {
		// Use the ToolWindow constructor.
		super(NAME, desktop);

		this.desktop = desktop;
//		MainWindow mw = desktop.getMainWindow();
//		mainScene = desktop.getMainScene();

		// Create main panel.
		WebPanel mainPane = new WebPanel(new BorderLayout());
		mainPane.setBorder(MainDesktopPane.newEmptyBorder());
		setContentPane(mainPane);

		// Create list panel.
		WebPanel listPane = new WebPanel(new GridLayout(2, 1));
		mainPane.add(listPane, BorderLayout.WEST);

		// Create incoming list panel.
		incomingListPane = new IncomingListPanel();
		incomingListPane.getIncomingList().addListSelectionListener(this);
		listPane.add(incomingListPane);

		// Create arrived list panel.
		arrivedListPane = new ArrivedListPanel();
		listPane.add(arrivedListPane);

		// Set incoming and arrived list panels to listen to each other's list selections.
		incomingListPane.getIncomingList().addListSelectionListener(arrivedListPane);
		arrivedListPane.getArrivedList().addListSelectionListener(incomingListPane);

		// Create detail panel.
		detailPane = new TransportDetailPanel(desktop);
		incomingListPane.getIncomingList().addListSelectionListener(detailPane);
		arrivedListPane.getArrivedList().addListSelectionListener(detailPane);
		mainPane.add(detailPane, BorderLayout.CENTER);

		// Create button panel.
		WebPanel buttonPane = new WebPanel(new FlowLayout(FlowLayout.CENTER));
		mainPane.add(buttonPane, BorderLayout.SOUTH);

		// Create new button.
		// Change button text from "New"  to "New Mission"
		WebButton newButton = new WebButton("New Mission");
		newButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				createNewTransportItem();
			}
		});
		buttonPane.add(newButton);

		// Create modify button.
		// Change button text from "Modify"  to "Modify Mission"
		modifyButton = new WebButton("Modify Mission");
		modifyButton.setEnabled(false);
		modifyButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				modifyTransportItem();
			}
		});
		buttonPane.add(modifyButton);

		// Create cancel button.
		// Change button text from "Discard"  to "Discard Mission"
		cancelButton = new WebButton("Discard Mission");
		cancelButton.setEnabled(false);
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				cancelTransportItem();
			}
		});
		buttonPane.add(cancelButton);

		setResizable(false);
		setMaximizable(true);

//		if (desktop.getMainScene() != null) {
//			//setClosable(false);
//			setMinimumSize(new Dimension(640, 640));
//			setSize(new Dimension(768, 640));
//		}
//		else
			setMinimumSize(new Dimension(640, 640));
			setSize(new Dimension(768, 640));
		setVisible(true);
		pack();

		Dimension desktopSize = desktop.getSize();
	    Dimension jInternalFrameSize = this.getSize();
	    int width = (desktopSize.width - jInternalFrameSize.width) / 2;
	    int height = (desktopSize.height - jInternalFrameSize.height) / 2;
	    setLocation(width, height);

	}

	/**
	 * Opens a create dialog.
	 */
	private void createNewTransportItem() {
//		if (mainScene != null )  {
//			double previous = mainScene.slowDownTimeRatio();	
//			new NewTransportItemDialog(desktop, this);
//			mainScene.speedUpTimeRatio(previous);
//		} 
//		else {
			new NewTransportItemDialog(desktop, this);
//		}

	}

	/**
	 * Determines if swing or javaFX is in used when loading the modify dialog
	 */
	private void modifyTransportItem() {
//		if (mainScene != null)  {
//			double previous = mainScene.slowDownTimeRatio();	
//			modifyTransport();
//			mainScene.speedUpTimeRatio(previous);
//		} else {
			modifyTransport();
//		}

	}

	/**
	 * Loads modify dialog for the currently selected transport item.
	 */
	private void modifyTransport() {
		// Get currently selected incoming transport item.
		Transportable transportItem = (Transportable) incomingListPane.getIncomingList().getSelectedValue();

		if ((transportItem != null)) {
			if (transportItem instanceof Resupply) {
				// Create modify resupply mission dialog.
				Resupply resupply = (Resupply) transportItem;
				String title = "Modify Resupply Mission";
				//new ModifyTransportItemDialog(mw.getFrame(), title, resupply);
				new ModifyTransportItemDialog(desktop, this, title, resupply);
				//isRunning = true;
			}
			else if (transportItem instanceof ArrivingSettlement) {
				// Create modify arriving settlement dialog.
				ArrivingSettlement settlement = (ArrivingSettlement) transportItem;
				String title = "Modify Arriving Settlement";
				//new ModifyTransportItemDialog(mw.getFrame(), title, settlement);
				new ModifyTransportItemDialog(desktop, this, title, settlement);
				//isRunning = true;
			}
		}
	}

	/**
	 * Cancels the currently selected transport item.
	 */
	private void cancelTransportItem() {
		String msg = "Note: you have highlighted a mission on the top-left box 'Incoming Transport Items' and clicked on the 'Discard Mission' button.";

//		if (mainScene != null) {
//			Platform.runLater(() -> {
//				askFX(msg);
//			});
//		}
//		else {
			// Add a dialog box asking the user to confirm "discarding" the mission
			JDialog.setDefaultLookAndFeelDecorated(true);
			final int response = JOptionPane.showConfirmDialog(null, msg, "Confirm",
					JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
			if (response == JOptionPane.NO_OPTION) {
				// "No" button click, do nothing
			} else if (response == JOptionPane.YES_OPTION) {
				// "Yes" button clicked and go ahead with discarding this mission
				Transportable transportItem = (Transportable) incomingListPane.getIncomingList().getSelectedValue();
				if (transportItem != null) {
					// call cancelTransportItem() in TransportManager Class to cancel the selected transport item.
					Simulation.instance().getTransportManager().cancelTransportItem(transportItem);
				}
			} else if (response == JOptionPane.CLOSED_OPTION) {
				// Close the dialogbox, do nothing
			}
//		}
	}

//	/**
//	 * Asks users for the confirmation of discarding a transport mission in an alert dialog
//	 * @param msg
//	 */
//	public synchronized void askFX(String msg) {
//		Alert alert = new Alert(AlertType.CONFIRMATION);
//		alert.setTitle("Resupply Tool");
//    	alert.initOwner(mainScene.getStage());
//		alert.initModality(Modality.NONE);
//		//alert.initModality(Modality.APPLICATION_MODAL);  f
//		//alert.initModality(Modality.WINDOW_MODAL);
//		alert.setHeaderText("Discard this transport/resupply mission ?");
//		alert.setContentText(msg);
//
//		ButtonType buttonTypeYes = new ButtonType("Yes");
//		ButtonType buttonTypeNo = new ButtonType("No");
//		alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo);
//
//		alert.showAndWait().ifPresent(response -> {
//		     if (response == buttonTypeYes) {
//				Transportable transportItem = (Transportable) incomingListPane.getIncomingList().getSelectedValue();
//				if (transportItem != null) {
//					// call cancelTransportItem() in TransportManager Class to cancel the selected transport item.
//					Simulation.instance().getTransportManager().cancelTransportItem(transportItem);
//				}
//		     }
//
//		});
//   }

	@Override
	public void valueChanged(ListSelectionEvent evt) {
		if (!evt.getValueIsAdjusting()) {
			JList<?> incomingList = (JList<?>) evt.getSource();
			Object selected = incomingList.getSelectedValue();
			if (selected != null) {
				// Incoming transport item is selected,
				// so enable modify and cancel buttons.
				modifyButton.setEnabled(true);
				cancelButton.setEnabled(true);
			}
			else {
				// Incoming transport item is unselected,
				// so disable modify and cancel buttons.
				modifyButton.setEnabled(false);
				cancelButton.setEnabled(false);
			}
		}
	}

	public IncomingListPanel getIncomingListPane() {
		return incomingListPane;
	}

	public void setModifyButton(boolean value) {
		modifyButton.setEnabled(value);
	}

	//public boolean isRunning() {
	//	return isRunning;
	//}

	//public void setRunning(boolean value){
	//	isRunning = value;
	//}

	/**
	 * Prepare this window for deletion.
	 */
	@Override
	public void destroy() {
		incomingListPane.destroy();
		arrivedListPane.destroy();
		detailPane.destroy();
	}
}
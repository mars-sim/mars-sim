/**
 * Mars Simulation Project
 * ResupplyWindow.java
 * @version 3.07 2014-12-06
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.resupply;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.interplanetary.transport.Transportable;
import org.mars_sim.msp.core.interplanetary.transport.resupply.Resupply;
import org.mars_sim.msp.core.interplanetary.transport.settlement.ArrivingSettlement;
import org.mars_sim.msp.ui.javafx.MainScene;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MainWindow;
import org.mars_sim.msp.ui.swing.tool.ToolWindow;

/**
 * Window for the resupply tool.
 * TODO externalize strings
 */
public class ResupplyWindow
extends ToolWindow
implements ListSelectionListener {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** Tool name. */
	public static final String NAME = "Resupply Tool";

	// Data members
	private IncomingListPanel incomingListPane;
	private ArrivedListPanel arrivedListPane;
	private TransportDetailPanel detailPane;
	private JButton modifyButton;
	private JButton cancelButton;

	/**
	 * Constructor.
	 * @param desktop the main desktop panel.
	 */
	public ResupplyWindow(MainDesktopPane desktop) {

		// Use the ToolWindow constructor.
		super(NAME, desktop);

		// Create main panel.
		JPanel mainPane = new JPanel(new BorderLayout());
		mainPane.setBorder(MainDesktopPane.newEmptyBorder());
		setContentPane(mainPane);

		// Create list panel.
		JPanel listPane = new JPanel(new GridLayout(2, 1));
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
		detailPane = new TransportDetailPanel();
		incomingListPane.getIncomingList().addListSelectionListener(detailPane);
		arrivedListPane.getArrivedList().addListSelectionListener(detailPane);
		mainPane.add(detailPane, BorderLayout.CENTER);

		// Create button panel.
		JPanel buttonPane = new JPanel(new FlowLayout(FlowLayout.CENTER));
		mainPane.add(buttonPane, BorderLayout.SOUTH);

		// Create new button.
		// 9/29/2014 modified by mk: Changed button text from "New"  to "New Mission"
		JButton newButton = new JButton("New Mission");
		newButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				createNewTransportItem();
			}
		});
		buttonPane.add(newButton);

		// Create modify button.
		// 9/29/2014 modified by mkung: Changed button text from "Modify"  to "Modify Mission"
		modifyButton = new JButton("Modify Mission");
		modifyButton.setEnabled(false);
		modifyButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				modifyTransportItem();
			}
		});
		buttonPane.add(modifyButton);

		// Create cancel button.
		// 9/29/2014 modified by mkung: Changed button text from "Discard"  to "Discard Mission"
		cancelButton = new JButton("Discard Mission");
		cancelButton.setEnabled(false);
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				cancelTransportItem();
			}
		});
		buttonPane.add(cancelButton);

		pack();
	}

	/**
	 * Opens a create dialog.
	 */
	private void createNewTransportItem() {
		
		MainWindow mw = desktop.getMainWindow();

		if (mw !=null )  {
			// Pause simulation.
			mw.pauseSimulation();	
			// Create new transportItem dialog.
			new NewTransportItemDialog(mw.getFrame());	
			// Unpause simulation.
			mw.unpauseSimulation();
		}
		
		MainScene ms = desktop.getMainScene();
		/*
		if (ms !=null )  {
			// Pause simulation.
			ms.pauseSimulation();	
			// Create new transportItem dialog.
			new NewTransportItemStage(ms.getStage());	
			// Unpause simulation.
			ms.unpauseSimulation();
		}
		*/
	}

	/**
	 * Opens a modify dialog for the currently selected transport item.
	 */
	private void modifyTransportItem() {
		
		MainWindow mw = desktop.getMainWindow();
		MainScene ms = desktop.getMainScene();
		
		if (mw !=null )  {
			// Pause simulation.
			mw.pauseSimulation();
				
			// Get currently selected incoming transport item.
			Transportable transportItem = (Transportable) incomingListPane.getIncomingList().getSelectedValue();
	
			if ((transportItem != null)) {
				if (transportItem instanceof Resupply) {
					// Create modify resupply mission dialog.
					Resupply resupply = (Resupply) transportItem;
					String title = "Modify Resupply Mission";
					new ModifyTransportItemDialog(mw.getFrame(), title, resupply);
				}
				else if (transportItem instanceof ArrivingSettlement) {
					// Create modify arriving settlement dialog.
					ArrivingSettlement settlement = (ArrivingSettlement) transportItem;
					String title = "Modify Arriving Settlement";
					new ModifyTransportItemDialog(mw.getFrame(), title, settlement);
				}
			}

			// Unpause simulation.
			mw.unpauseSimulation();
		}
		

		if (ms !=null )  {
			// Pause simulation.
			ms.pauseSimulation();
				
			// Get currently selected incoming transport item.
			Transportable transportItem = (Transportable) incomingListPane.getIncomingList().getSelectedValue();
	
			if ((transportItem != null)) {
				if (transportItem instanceof Resupply) {
					// Create modify resupply mission dialog.
					Resupply resupply = (Resupply) transportItem;
					String title = "Modify Resupply Mission";
					new ModifyTransportItemDialog(ms.getStage(), title, resupply);
				}
				else if (transportItem instanceof ArrivingSettlement) {
					// Create modify arriving settlement dialog.
					ArrivingSettlement settlement = (ArrivingSettlement) transportItem;
					String title = "Modify Arriving Settlement";
					new ModifyTransportItemDialog(ms.getStage(), title, settlement);
				}
			}

			// Unpause simulation.
			ms.unpauseSimulation();
		}
	}

	/**
	 * Cancels the currently selected transport item.
	 */
	private void cancelTransportItem() {     
		// 2014-10-04 Added a dialog box asking the user to confirm "discarding" the mission
		// 2015-01-07 Modified to pause the EDT until the dialog box is dismissed
		
		JDialog.setDefaultLookAndFeelDecorated(true);
		final int response = JOptionPane.showConfirmDialog(null, "Are you sure you want to discard the highlighted mission?", "Confirm",
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
	}

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
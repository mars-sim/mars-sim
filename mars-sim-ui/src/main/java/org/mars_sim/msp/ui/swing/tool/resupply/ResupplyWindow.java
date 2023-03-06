/*
 * Mars Simulation Project
 * ResupplyWindow.java
 * @date 2021-09-20
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.resupply;

import java.awt.BorderLayout;
import java.awt.Dimension;
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
import org.mars_sim.msp.core.time.ClockPulse;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.toolwindow.ToolWindow;


/**
 * Window for the resupply tool.
 * TODO externalize strings
 */

@SuppressWarnings("serial")
public class ResupplyWindow
extends ToolWindow
implements ListSelectionListener {

	/** Tool name. */
	public static final String NAME = "Resupply Tool";
	public static final String ICON = "resupply";

	// Data members
	//private boolean isRunning = false;
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
		detailPane = new TransportDetailPanel(desktop);
		incomingListPane.getIncomingList().addListSelectionListener(detailPane);
		arrivedListPane.getArrivedList().addListSelectionListener(detailPane);
		mainPane.add(detailPane, BorderLayout.CENTER);

		// Create button panel.
		JPanel buttonPane = new JPanel(new FlowLayout(FlowLayout.CENTER));
		mainPane.add(buttonPane, BorderLayout.SOUTH);

		// Create new button.
		// Change button text from "New"  to "New Mission"
		JButton newButton = new JButton("New Mission");
		newButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				createNewTransportItem();
			}
		});
		buttonPane.add(newButton);

		// Create modify button.
		// Change button text from "Modify"  to "Modify Mission"
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
		// Change button text from "Discard"  to "Discard Mission"
		cancelButton = new JButton("Discard Mission");
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

		setSize(new Dimension(900, 800));
		setVisible(true);
		pack();
	}

	/**
	 * Time has changed
	 * @param pulse Clock change
	 */
	@Override
	public void update(ClockPulse pulse) {
		detailPane.update(pulse);
	}

	/**
	 * Opens a create dialog.
	 */
	private void createNewTransportItem() {
		new NewTransportItemDialog(desktop, this);
	}

	/**
	 * Loads the modify dialog.
	 */
	private void modifyTransportItem() {
		modifyTransport();
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
				new ModifyTransportItemDialog(desktop, this, title, resupply);

			}
			else if (transportItem instanceof ArrivingSettlement) {
				// Create modify arriving settlement dialog.
				ArrivingSettlement settlement = (ArrivingSettlement) transportItem;
				String title = "Modify Arriving Settlement";
				new ModifyTransportItemDialog(desktop, this, title, settlement);
			}
		}
	}

	/**
	 * Cancels the currently selected transport item.
	 */
	private void cancelTransportItem() {
		String msg = "Note: you have highlighted a mission on the top-left box 'Incoming Transport Items' and clicked on the 'Discard Mission' button.";

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

	public IncomingListPanel getIncomingListPane() {
		return incomingListPane;
	}

	public void setModifyButton(boolean value) {
		modifyButton.setEnabled(value);
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

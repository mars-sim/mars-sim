/*
 * Mars Simulation Project
 * TabPanelTow.java
 * @date 2024-07-24
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.unit_window.vehicle;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.vehicle.StatusType;
import com.mars_sim.core.vehicle.Towing;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.MainDesktopPane;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.unit_window.TabPanel;


@SuppressWarnings("serial")
public class TabPanelTow extends TabPanel {

	private static final String TOW_ICON = "tow";
	
	// Data members
	private JPanel towingLabelPanel;
	private JLabel towingTextLabel;
	private JButton towingButton;
	private JPanel towedLabelPanel;
	private JLabel towedTextLabel;
	private JButton towedButton;
	
	/** The Vehicle instance. */
	private Vehicle vehicle;

	public TabPanelTow(Vehicle unit, MainDesktopPane desktop) {
		// Use TabPanel constructor.
		super(
			null,
			ImageLoader.getIconByName(TOW_ICON),
			Msg.getString("TabPanelTow.title"), //$NON-NLS-1$
			desktop
		);

		vehicle = unit;
	}

	@Override
	protected void buildUI(JPanel content) {
		JPanel mainPane = new JPanel();
		mainPane.setLayout(new BoxLayout(mainPane, BoxLayout.Y_AXIS));
		content.add(mainPane, BorderLayout.NORTH);
		
		if (vehicle instanceof Towing) {

			// Create towing label panel.
			towingLabelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
			mainPane.add(towingLabelPanel);
			
			// Create towing label.
			JLabel towLabel = new JLabel(Msg.getString("TabPanelTow.towing"), SwingConstants.CENTER); //$NON-NLS-1$
			towLabel.setFont(StyleManager.getLabelFont());
			towingLabelPanel.add(towLabel);

			// Create the towing button.
			towingButton = new JButton();
			towingButton.addActionListener(e ->
					getDesktop().showDetails(((Towing) vehicle).getTowedVehicle()));

			// Create the towing text label.
			towingTextLabel = new JLabel(Msg.getString("TabPanelTow.none"), SwingConstants.LEFT); //$NON-NLS-1$
			
			// Add the towing button or towing text label depending on the situation.
			Vehicle towedVehicle = ((Towing) vehicle).getTowedVehicle();
			if (towedVehicle != null) {
				towingButton.setText(towedVehicle.getName());
				addTowingButton();
			}
			else addTowingTextLabel();
		}
		
		else if (vehicle.haveStatusType(StatusType.TOWED)) {
			
			// Create towed label panel.
			towedLabelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
			mainPane.add(towedLabelPanel);

			// Create towed label.
			JLabel towedLabel = new JLabel(Msg.getString("TabPanelTow.towedBy"), SwingConstants.CENTER); //$NON-NLS-1$
			towedLabel.setFont(StyleManager.getLabelFont());
			towedLabelPanel.add(towedLabel);

			// Create the towed button.
			towedButton = new JButton();
			towedButton.addActionListener(e ->
					getDesktop().showDetails(vehicle.getTowingVehicle()));

			// Create towed text label.
			towedTextLabel = new JLabel(Msg.getString("TabPanelTow.none"), SwingConstants.LEFT); //$NON-NLS-1$
			
			// Add the towed button or towed text label depending on the situation.
			if (vehicle.getTowingVehicle() != null) {
				towedButton.setText(vehicle.getTowingVehicle().getName());
				addTowedButton();
			}
			else addTowedTextLabel();
		}
	}

	/**
	 * Adds the towing button to the towing label panel.
	 */
	private void addTowingButton() {
		try {
			Component lastComponent = towingLabelPanel.getComponent(1);
			if (lastComponent == towingTextLabel) {
				towingLabelPanel.remove(towingTextLabel);
				towingLabelPanel.add(towingButton);
			}
		}
		catch (ArrayIndexOutOfBoundsException e) {
			towingLabelPanel.add(towingButton);
		}
	}

	/**
	 * Adds the towing text label to the towing label panel.
	 */
	private void addTowingTextLabel() {
		Component lastComponent = towingLabelPanel.getComponent(0);
		if ((lastComponent != null) && lastComponent == towingButton) {
			towingLabelPanel.remove(towingButton);
			towingLabelPanel.add(towingTextLabel);
		}
		else {
			towingLabelPanel.add(towingTextLabel);
		}
	}

	/**
	 * Adds the towed button to the towed label panel.
	 */
	private void addTowedButton() {
		try {
			Component lastComponent = towedLabelPanel.getComponent(1);
			if (lastComponent == towedTextLabel) {
				towedLabelPanel.remove(towedTextLabel);
				towedLabelPanel.add(towedButton);
			}
		}
		catch (ArrayIndexOutOfBoundsException e) {
			towedLabelPanel.add(towedButton);
		}
	}

	/**
	 * Adds the towed text label to the towed label panel.
	 */
	private void addTowedTextLabel() {
		try {
			Component lastComponent = towedLabelPanel.getComponent(1);
			if (lastComponent == towedButton) {
				towedLabelPanel.remove(towedButton);
				towedLabelPanel.add(towedTextLabel);
			}
		}
		catch (ArrayIndexOutOfBoundsException e) {
			towedLabelPanel.add(towedTextLabel);
		}
	}

	/**
	 * Updates the info on this panel.
	 */
	@Override
	public void update() {
		
		if (vehicle instanceof Towing) {
			// Update towing button or towing text label as necessary.
			Vehicle towedVehicle = ((Towing) vehicle).getTowedVehicle();
			if (towedVehicle != null) {
				towingButton.setText(towedVehicle.getName());
				addTowingButton();
			}
			else addTowingTextLabel();
		}
		
		else if (vehicle.haveStatusType(StatusType.TOWED)) {
			// Update towed button or towed text label as necessary.
			Vehicle towingVehicle = vehicle.getTowingVehicle();
			if (towingVehicle != null) {
				towedButton.setText(towingVehicle.getName());
				addTowedButton();
			}
			else addTowedTextLabel();
		}
	}
	
	@Override
	public void destroy() {
		super.destroy();
		
		towingLabelPanel = null; 
		towingTextLabel = null; 
		towingButton = null; 
		towedLabelPanel = null; 
		towedTextLabel = null; 
		towedButton = null; 
	}
}

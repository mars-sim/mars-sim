/**
 * Mars Simulation Project
 * NavigationTabPanel.java
 * @version 3.1.0 2019-09-20
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.vehicle;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.vehicle.Towing;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;

import com.alee.laf.button.WebButton;
import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;
@SuppressWarnings("serial")
public class TabPanelTow extends TabPanel {

	private static final Font font = new Font("Monospaced", Font.BOLD, 12);

	// Data members
	private WebPanel towingLabelPanel;
	private WebLabel towingTextLabel;
	private WebButton towingButton;
	private WebPanel towedLabelPanel;
	private WebLabel towedTextLabel;
	private WebButton towedButton;

	/** Is UI constructed. */
	private boolean uiDone = false;
	
	/** The Vehicle instance. */
	private Vehicle vehicle;
	
	public TabPanelTow(Unit unit, MainDesktopPane desktop) {
		// Use TabPanel constructor.
		super(
			Msg.getString("TabPanelTow.title"), //$NON-NLS-1$
			null,
			Msg.getString("TabPanelTow.tooltip"), //$NON-NLS-1$
			unit,
			desktop
		);

      vehicle = (Vehicle) unit;

	}

	public boolean isUIDone() {
		return uiDone;
	}
	
	public void initializeUI() {
		uiDone = true;
			
		// Create towing label.
		WebPanel panel = new WebPanel(new FlowLayout(FlowLayout.CENTER));
		WebLabel titleLabel = new WebLabel(Msg.getString("TabPanelTow.title"), WebLabel.CENTER); //$NON-NLS-1$
		titleLabel.setFont(new Font("Serif", Font.BOLD, 16));
		panel.add(titleLabel);
		topContentPanel.add(panel);
		

		if (unit instanceof Towing) {

			// Create towing label panel.
			towingLabelPanel = new WebPanel(new FlowLayout(FlowLayout.CENTER));
//			towingLabelPanel.setBorder(new MarsPanelBorder());
			topContentPanel.add(towingLabelPanel);
			
			// Create towing label.
			WebLabel towLabel = new WebLabel("  " + Msg.getString("TabPanelTow.towing"), WebLabel.CENTER); //$NON-NLS-1$
			towLabel.setFont(font);
			towingLabelPanel.add(towLabel);

			// Create the towing button.
			towingButton = new WebButton();
			towingButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					getDesktop().openUnitWindow(((Towing) getUnit()).getTowedVehicle(), false);
				}
			});

			// Create the towing text label.
			towingTextLabel = new WebLabel(Msg.getString("TabPanelTow.none"), WebLabel.LEFT); //$NON-NLS-1$
			towingTextLabel.setFont(font);
			
			// Add the towing button or towing text label depending on the situation.
			Vehicle towedVehicle = ((Towing) unit).getTowedVehicle();
			if (towedVehicle != null) {
				towingButton.setText(towedVehicle.getName());
				addTowingButton();
			}
			else addTowingTextLabel();
		}

		Vehicle vehicle = (Vehicle) unit;

		// Create towed label panel.
		towedLabelPanel = new WebPanel(new FlowLayout(FlowLayout.CENTER));
//		towedLabelPanel.setBorder(new MarsPanelBorder());
		topContentPanel.add(towedLabelPanel);

		// Create towed label.
		WebLabel towedLabel = new WebLabel(Msg.getString("TabPanelTow.towedBy"), WebLabel.CENTER); //$NON-NLS-1$
		towedLabel.setFont(font);
		towedLabelPanel.add(towedLabel);

		// Create the towed button.
		towedButton = new WebButton();
		towedButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				getDesktop().openUnitWindow(((Vehicle) getUnit()).getTowingVehicle(), false);
			}
		});

		// Create towed text label.
		towedTextLabel = new WebLabel(Msg.getString("TabPanelTow.none"), WebLabel.LEFT); //$NON-NLS-1$
		towedTextLabel.setFont(font);
		
		// Add the towed button or towed text label depending on the situation.
		if (vehicle.getTowingVehicle() != null) {
			towedButton.setText(vehicle.getTowingVehicle().getName());
			addTowedButton();
		}
		else addTowedTextLabel();
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
		try {
			Component lastComponent = towingLabelPanel.getComponent(1);
			if (lastComponent == towingButton) {
				towingLabelPanel.remove(towingButton);
				towingLabelPanel.add(towingTextLabel);
			}
		}
		catch (ArrayIndexOutOfBoundsException e) {
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
	public void update() {
		if (!uiDone)
			initializeUI();
		
		if (unit instanceof Towing) {
			// Update towing button or towing text label as necessary.
			Vehicle towedVehicle = ((Towing) unit).getTowedVehicle();
			if (towedVehicle != null) {
				towingButton.setText(towedVehicle.getName());
				addTowingButton();
			}
			else addTowingTextLabel();
		}

		// Update towed button or towed text label as necessary.
		Vehicle towingVehicle = ((Vehicle) unit).getTowingVehicle();
		if (towingVehicle != null) {
			towedButton.setText(towingVehicle.getName());
			addTowedButton();
		}
		else addTowedTextLabel();
	}
	
	public void destroy() {
		towingLabelPanel = null; 
		towingTextLabel = null; 
		towingButton = null; 
		towedLabelPanel = null; 
		towedTextLabel = null; 
		towedButton = null; 
	}
}
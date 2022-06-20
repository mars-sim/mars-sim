/**
 * Mars Simulation Project
 * NavigationTabPanel.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.vehicle;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

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

	private static final Font f = new Font("Monospaced", Font.BOLD, 12);

	// Data members
	private WebPanel towingLabelPanel;
	private WebLabel towingTextLabel;
	private WebButton towingButton;
	private WebPanel towedLabelPanel;
	private WebLabel towedTextLabel;
	private WebButton towedButton;

	
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

	@Override
	protected void buildUI(JPanel content) {
		JPanel mainPane = new JPanel();
		mainPane.setLayout(new BoxLayout(mainPane, BoxLayout.Y_AXIS));
		content.add(mainPane, BorderLayout.NORTH);
		
		if (vehicle instanceof Towing) {

			// Create towing label panel.
			towingLabelPanel = new WebPanel(new FlowLayout(FlowLayout.CENTER));
			mainPane.add(towingLabelPanel);
			
			// Create towing label.
			WebLabel towLabel = new WebLabel("  " + Msg.getString("TabPanelTow.towing"), WebLabel.CENTER); //$NON-NLS-1$
			towLabel.setFont(f);
			towingLabelPanel.add(towLabel);

			// Create the towing button.
			towingButton = new WebButton();
			towingButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					getDesktop().openUnitWindow(((Towing) vehicle).getTowedVehicle(), false);
				}
			});

			// Create the towing text label.
			towingTextLabel = new WebLabel(Msg.getString("TabPanelTow.none"), WebLabel.LEFT); //$NON-NLS-1$
			towingTextLabel.setFont(f);
			
			// Add the towing button or towing text label depending on the situation.
			Vehicle towedVehicle = ((Towing) vehicle).getTowedVehicle();
			if (towedVehicle != null) {
				towingButton.setText(towedVehicle.getName());
				addTowingButton();
			}
			else addTowingTextLabel();
		}

		// Create towed label panel.
		towedLabelPanel = new WebPanel(new FlowLayout(FlowLayout.CENTER));
		mainPane.add(towedLabelPanel);

		// Create towed label.
		WebLabel towedLabel = new WebLabel(Msg.getString("TabPanelTow.towedBy"), WebLabel.CENTER); //$NON-NLS-1$
		towedLabel.setFont(f);
		towedLabelPanel.add(towedLabel);

		// Create the towed button.
		towedButton = new WebButton();
		towedButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				getDesktop().openUnitWindow(vehicle.getTowingVehicle(), false);
			}
		});

		// Create towed text label.
		towedTextLabel = new WebLabel(Msg.getString("TabPanelTow.none"), WebLabel.LEFT); //$NON-NLS-1$
		towedTextLabel.setFont(f);
		
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
		Component lastComponent = towingLabelPanel.getFirstComponent();
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

		// Update towed button or towed text label as necessary.
		Vehicle towingVehicle = vehicle.getTowingVehicle();
		if (towingVehicle != null) {
			towedButton.setText(towingVehicle.getName());
			addTowedButton();
		}
		else addTowedTextLabel();
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

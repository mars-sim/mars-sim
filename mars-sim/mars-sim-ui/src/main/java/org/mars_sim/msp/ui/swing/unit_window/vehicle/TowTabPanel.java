/**
 * Mars Simulation Project
 * NavigationTabPanel.java
 * @version 3.00 2010-08-105
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.unit_window.vehicle;

import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.vehicle.Towing;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TowTabPanel extends TabPanel {

	// Data members
	private JPanel towingLabelPanel;
	private JLabel towingTextLabel;
	private JButton towingButton;
	private JPanel towedLabelPanel;
	private JLabel towedTextLabel;
	private JButton towedButton;
	
	public TowTabPanel(Unit unit, MainDesktopPane desktop) {
		// Use TabPanel constructor.
		super("Tow", null, "Tow", unit, desktop);
		
		if (unit instanceof Towing) {
			
			// Create towing label panel.
			towingLabelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			towingLabelPanel.setBorder(new MarsPanelBorder());
			topContentPanel.add(towingLabelPanel);
			
			// Create towing label.
			JLabel towingLabel = new JLabel("Towing: ", JLabel.CENTER);
			towingLabelPanel.add(towingLabel);
			
			// Create the towing button.
			towingButton = new JButton();
			towingButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					getDesktop().openUnitWindow(((Towing) getUnit()).getTowedVehicle(), false);
				}
			});
			
			// Create the towing text label.
			towingTextLabel = new JLabel("None", JLabel.LEFT);
			
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
		towedLabelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		towedLabelPanel.setBorder(new MarsPanelBorder());
		topContentPanel.add(towedLabelPanel);
		
		// Create towed label.
		JLabel towedLabel = new JLabel("Towed by: ", JLabel.CENTER);
		towedLabelPanel.add(towedLabel);
		
		// Create the towed button.
		towedButton = new JButton();
		towedButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				getDesktop().openUnitWindow(((Vehicle) getUnit()).getTowingVehicle(), false);
			}
		});
		
		// Create towed text label.
		towedTextLabel = new JLabel("None", JLabel.LEFT);
		
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
}
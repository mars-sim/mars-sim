/*
 * Mars Simulation Project
 * BuildingPanelAstronomicalObservation.java
 * @date 2021-10-06
 * @author Sebastien Venot
 */
package org.mars_sim.msp.ui.swing.unit_window.structure.building;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.structure.building.function.AstronomicalObservation;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.tool.SpringUtilities;

import com.alee.laf.button.WebButton;
import com.alee.laf.panel.WebPanel;
import com.alee.managers.tooltip.TooltipManager;
import com.alee.managers.tooltip.TooltipWay;

/**
 * A panel for the astronomical observation building function.
 */
@SuppressWarnings("serial")
public class BuildingPanelAstronomicalObservation
extends BuildingFunctionPanel {

	// Data members
	private int currentObserversAmount;

	private JTextField observersLabel;

	private AstronomicalObservation function;

	/**
	 * Constructor.
	 * @param observatory the astronomical observatory building function.
	 * @param desktop the main desktop.
	 */
	public BuildingPanelAstronomicalObservation(AstronomicalObservation observatory, 
			MainDesktopPane desktop) {
		// User BuildingFunctionPanel constructor.
		super(Msg.getString("BuildingPanelAstronomicalObservation.title"), observatory.getBuilding(), desktop);

		function = observatory;
		currentObserversAmount = function.getObserverNum();
	}
	
	/**
	 * Build the UI
	 */
	@Override
	protected void buildUI(JPanel center) {
		
		// Prepare label panelAstronomicalObservation
		WebPanel labelPanel = new WebPanel(new SpringLayout());
		center.add(labelPanel, BorderLayout.NORTH);

		// Observer number label
		observersLabel = addTextField(labelPanel, Msg.getString("BuildingPanelAstronomicalObservation.numberOfObservers"),
									  currentObserversAmount, null);

		// Observer capacityLabel
		addTextField(labelPanel, Msg.getString("BuildingPanelAstronomicalObservation.observerCapacity"),
					 function.getObservatoryCapacity(), null);
		
		labelPanel.setOpaque(false);
		labelPanel.setBackground(new Color(0,0,0,128));
		
		//Lay out the spring panel.
		SpringUtilities.makeCompactGrid(labelPanel,
		                                2, 2, //rows, cols
		                                65, 5,        //initX, initY
		                                3, 1);       //xPad, yPad
		
      	// Create the button panel.
		WebPanel buttonPane = new WebPanel(new FlowLayout(FlowLayout.CENTER));
		
		// Create the orbit viewer button.
		WebButton starMap = new WebButton();
		starMap.setIcon(desktop.getMainWindow().getTelescopeIcon());// ImageLoader.getIcon(Msg.getString("img.starMap"))); //$NON-NLS-1$
		TooltipManager.setTooltip(starMap, "Open the Orbit Viewer", TooltipWay.up);

		starMap.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					desktop.getMainWindow().openOrbitViewer();
				}
			});
		buttonPane.add(starMap);
		center.add(buttonPane, BorderLayout.CENTER);
	}

	@Override
	public void update() {
		if (currentObserversAmount != function.getObserverNum()) {
			currentObserversAmount = function.getObserverNum();
			observersLabel.setText(Integer.toString(currentObserversAmount));
		}
	}
}

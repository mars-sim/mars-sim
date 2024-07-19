/*
 * Mars Simulation Project
 * CollectResourcesMissionCustomInfoPanel.java
 * @date 2024-07-18
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.tool.mission;

import java.awt.BorderLayout;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JLabel;

import com.mars_sim.core.UnitEvent;
import com.mars_sim.core.UnitEventType;
import com.mars_sim.core.UnitListener;
import com.mars_sim.core.person.ai.mission.CollectResourcesMission;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.person.ai.mission.MissionEvent;
import com.mars_sim.core.resource.AmountResource;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.vehicle.Rover;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.utils.AttributePanel;

/**
 * A panel for displaying collect resources mission information.
 */
@SuppressWarnings("serial")
public class CollectResourcesMissionCustomInfoPanel
extends MissionCustomInfoPanel implements UnitListener {

	// Data members.
	private Set<AmountResource> resourcesCollected = new HashSet<>();
	
	private JLabel[] amountLabels = null;
	
	private Rover missionRover;
	private CollectResourcesMission mission;
	
	/**
	 * Constructor.
	 */
	public CollectResourcesMissionCustomInfoPanel(int [] resourceIds) {
		// Use MissionCustomInfoPanel constructor.
		super();

		// Set layout.
		setLayout(new BorderLayout());

		// Create content panel.
		AttributePanel collectionPanel = new AttributePanel(resourceIds.length);
		collectionPanel.setBorder(StyleManager.createLabelBorder("Resource Collected"));
		add(collectionPanel, BorderLayout.NORTH);
				
		amountLabels = new JLabel[resourceIds.length];
		for (int i=0; i<resourceIds.length; i++) {
			AmountResource ar = ResourceUtil.findAmountResource(resourceIds[i]);
			resourcesCollected.add(ar);
			amountLabels[i] = collectionPanel.addRow(ar.getName(), StyleManager.DECIMAL_KG.format(0D));
		}
	}


	@Override
	public void updateMission(Mission newMission) {
		if (newMission instanceof CollectResourcesMission crMission) {

			// Set the mission and mission rover.
			this.mission = crMission;
			
			// Remove as unit listener to any existing rovers.
			if (missionRover != null) {
				missionRover.removeUnitListener(this);
			}

			if (this.mission.getRover() != null) {
				missionRover = this.mission.getRover();
				// Register as unit listener for mission rover.
				missionRover.addUnitListener(this);
			}

			// Update the collection value label.
			updateCollectionValueLabel();
			
			repaint();
		}
	}

	@Override
	public void updateMissionEvent(MissionEvent e) {
		// Do nothing.
	}

	@Override
	public void unitUpdate(UnitEvent event) {
		if (UnitEventType.INVENTORY_RESOURCE_EVENT == event.getType()) {
			Object source = event.getTarget();
			if (source instanceof AmountResource) {
				if (resourcesCollected.contains(source)){
					updateCollectionValueLabel();
				}
			}
		}
	}

	/**
	 * Updates the collection value label.
	 */
	private void updateCollectionValueLabel() {

		Map<Integer, Double> collected = mission.getCumulativeCollectedByID();

		int i = 0;
		for (AmountResource resourceId : resourcesCollected) {
			double amount = collected.getOrDefault(resourceId.getID(), 0D);
			amountLabels[i++].setText(StyleManager.DECIMAL_KG.format(amount));
		}
	}
}

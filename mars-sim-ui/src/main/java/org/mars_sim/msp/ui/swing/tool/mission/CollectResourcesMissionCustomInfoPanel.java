/*
 * Mars Simulation Project
 * CollectResourcesMissionCustomInfoPanel.java
 * @date 2021-11-29
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.mission;

import java.awt.BorderLayout;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JLabel;

import org.mars_sim.msp.core.UnitEvent;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.UnitListener;
import org.mars_sim.msp.core.person.ai.mission.CollectResourcesMission;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionEvent;
import org.mars_sim.msp.core.person.ai.mission.MissionType;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.ui.swing.StyleManager;
import org.mars_sim.msp.ui.swing.utils.AttributePanel;

/**
 * A panel for displaying collect resources mission information.
 */
@SuppressWarnings("serial")
public class CollectResourcesMissionCustomInfoPanel
extends MissionCustomInfoPanel
implements UnitListener {

	// Data members.
	private CollectResourcesMission mission;

	private Rover missionRover;
	private JLabel[] amountLabels = null;
	private Set<AmountResource> resourcesCollected = new HashSet<>();

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
		collectionPanel.setBorder(StyleManager.createLabelBorder("Resource Collected - Aboard Vehicle"));
		add(collectionPanel, BorderLayout.CENTER);
				
		amountLabels = new JLabel[resourceIds.length];
		for (int i=0; i<resourceIds.length; i++) {
			AmountResource ar = ResourceUtil.findAmountResource(resourceIds[i]);
			resourcesCollected.add(ar);
			
			amountLabels[i] = collectionPanel.addTextField(ar.getName(), StyleManager.DECIMAL_KG.format(0D), null);
		}
	}


	@Override
	public void updateMission(Mission mission) {
		if (mission.getMissionType() == MissionType.COLLECT_ICE
				|| mission.getMissionType() == MissionType.COLLECT_REGOLITH) {
			// Remove as unit listener to any existing rovers.
			if (missionRover != null) {
				missionRover.removeUnitListener(this);
			}

			// Set the mission and mission rover.
			this.mission = (CollectResourcesMission) mission;
			if (this.mission.getRover() != null) {
				missionRover = this.mission.getRover();
				// Register as unit listener for mission rover.
				missionRover.addUnitListener(this);
			}

			// Update the collection value label.
			updateCollectionValueLabel();
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

		Map<Integer, Double> collected = mission.getResourcesCollected();

		int i = 0;
		for (AmountResource resourceId : resourcesCollected) {
			double amount = collected.getOrDefault(resourceId.getID(), 0D);
			amountLabels[i++].setText(StyleManager.DECIMAL_KG.format(amount));
		}
	}
}

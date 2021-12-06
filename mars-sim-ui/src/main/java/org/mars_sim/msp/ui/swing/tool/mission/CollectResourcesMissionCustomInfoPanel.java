/*
 * Mars Simulation Project
 * CollectResourcesMissionCustomInfoPanel.java
 * @date 2021-11-29
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.mission;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.SpringLayout;

import org.mars_sim.msp.core.UnitEvent;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.UnitListener;
import org.mars_sim.msp.core.person.ai.mission.CollectResourcesMission;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionEvent;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.tool.Conversion;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.ui.swing.tool.SpringUtilities;

import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;

/**
 * A panel for displaying collect resources mission information.
 */
@SuppressWarnings("serial")
public class CollectResourcesMissionCustomInfoPanel
extends MissionCustomInfoPanel
implements UnitListener {

	private static final String KG_FORMAT = "%.2f kg";
	// Data members.
	private CollectResourcesMission mission;

	private Rover missionRover;
	private WebLabel[] amountLabels = null;
	private List<AmountResource> resourcesCollected = new ArrayList<>();

	/**
	 * Constructor.
	 */
	public CollectResourcesMissionCustomInfoPanel(int [] resourceIds) {
		// Use MissionCustomInfoPanel constructor.
		super();

		// Set layout.
		setLayout(new BorderLayout());

		// Create content panel.
		WebPanel collectionPanel = new WebPanel(new SpringLayout());
		add(collectionPanel, BorderLayout.CENTER);

		amountLabels = new WebLabel[resourceIds.length];
		
		for (int i=0; i<resourceIds.length; i++) {
			AmountResource ar = ResourceUtil.findAmountResource(resourceIds[i]);
			resourcesCollected.add(ar);
			
			WebLabel label = new WebLabel(String.format("%12s:    ", Conversion.capitalize(ar.getName())),
					                                    JLabel.LEFT); //$NON-NLS-1$
			label.setAlignmentX(Component.LEFT_ALIGNMENT);
			collectionPanel.add(label);

			WebLabel l = new WebLabel(String.format(KG_FORMAT, 0D), JLabel.LEFT);
			amountLabels[i] = l;
			collectionPanel.add(l);
		}

		// Prepare SpringLayout.
		SpringUtilities.makeCompactGrid(collectionPanel,
				resourceIds.length, 2, // rows, cols
				100, 5, // initX, initY
				30, 4); // xPad, yPad
	}


	@Override
	public void updateMission(Mission mission) {
		if (mission instanceof CollectResourcesMission) {
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
			amountLabels[i++].setText(String.format(KG_FORMAT, amount));
		}
	}
}

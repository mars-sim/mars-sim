/**
 * Mars Simulation Project
 * CollectResourcesMissionCustomInfoPanel.java
 * @version 3.1.0 2017-11-01
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.mission;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import org.mars_sim.msp.core.Msg;
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

import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;

/**
 * A panel for displaying collect resources mission information.
 */
@SuppressWarnings("serial")
public class CollectResourcesMissionCustomInfoPanel
extends MissionCustomInfoPanel
implements UnitListener {

	// Data members.
	private double resourceAmountCache;
	
	private CollectResourcesMission mission;
	private AmountResource resource;
	private AmountResource[] REGOLITH_TYPES;
	
	
	private Rover missionRover;
	private WebLabel collectionValueLabel;


	/**
	 * Constructor.
	 */
	public CollectResourcesMissionCustomInfoPanel(AmountResource resource) {
		// Use MissionCustomInfoPanel constructor.
		super();

		// Initialize data members.
		this.resource = resource;
		
		if (resource == ResourceUtil.regolithAR) {
			REGOLITH_TYPES = new AmountResource[] {
					ResourceUtil.regolithBAR,
					ResourceUtil.regolithCAR,
					ResourceUtil.regolithDAR
			};
		}

		// Set layout.
		setLayout(new BorderLayout());

		// Create content panel.
		WebPanel contentPanel = new WebPanel(new GridLayout(1, 2));
		add(contentPanel, BorderLayout.NORTH);

		// Create collection title label.
		String resourceString = resource.getName().substring(0, 1).toUpperCase() + 
				resource.getName().substring(1);
		WebLabel collectionTitleLabel = new WebLabel(
				Msg.getString("CollectResourcesMissionCustomInfoPanel.totalCollected", 
						Conversion.capitalize(resourceString))); //$NON-NLS-1$
		contentPanel.add(collectionTitleLabel);

		// Create collection value label.
		collectionValueLabel = new WebLabel(
			Msg.getString(
				"CollectResourcesMissionCustomInfoPanel.kilograms", //$NON-NLS-1$
				Integer.toString(0)
			),
			WebLabel.LEFT
		);
		contentPanel.add(collectionValueLabel);
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

//			resourceAmountCache = this.mission.getTotalCollectedResources();

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
				if (resource.equals(event.getTarget())){
					updateCollectionValueLabel(); 
				}
				for (AmountResource ar : REGOLITH_TYPES) {
					if (ar.equals(event.getTarget())) {
						updateCollectionValueLabel(); 
					}
				}
			}
				
//			else if (source instanceof Integer) {
//				if ((Integer)source < ResourceUtil.FIRST_ITEM_RESOURCE_ID)
//					updateCollectionValueLabel();
//			}
//			if (resource.equals(event.getTarget())) {
//				updateCollectionValueLabel();   
//			}
		}
	}

	/**
	 * Updates the collection value label.
	 */
	private void updateCollectionValueLabel() {
		double resourceAmount = mission.getTotalCollectedResources();
		if (missionRover != null) {
//			resourceAmount = missionRover.getInventory().getAmountResourceStored(resource, true);
			if (resourceAmountCache < resourceAmount) {
				resourceAmountCache = resourceAmount;
			}
			else {
				resourceAmount = resourceAmountCache;
			}
		}
		else {
			resourceAmount = resourceAmountCache;
		}

		// Update collection value label.
		collectionValueLabel.setText(
			Msg.getString("CollectResourcesMissionCustomInfoPanel.kilograms", //$NON-NLS-1$
				Integer.toString((int) resourceAmount)
			)
		);
	}
}
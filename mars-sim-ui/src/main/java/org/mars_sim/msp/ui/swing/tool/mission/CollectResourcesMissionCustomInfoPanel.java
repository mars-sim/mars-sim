/*
 * Mars Simulation Project
 * CollectResourcesMissionCustomInfoPanel.java
 * @date 2021-11-29
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.mission;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.SpringLayout;

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

	// Data members.
	private CollectResourcesMission mission;
	private AmountResource resource;
	private AmountResource[] REGOLITH_TYPES;

	private Rover missionRover;
	private WebLabel collectionValueLabel;
	private WebLabel[] regolithLabels = new WebLabel[4];

	/**
	 * Constructor.
	 */
	public CollectResourcesMissionCustomInfoPanel(AmountResource resource) {
		// Use MissionCustomInfoPanel constructor.
		super();

		// Initialize data members.
		this.resource = resource;

		// Set layout.
		setLayout(new BorderLayout());

		// Create title panel.
//		WebPanel titlePanel = new WebPanel(new BorderLayout());
//		add(titlePanel, BorderLayout.NORTH);
//
//		WebLabel titleLabel = new WebLabel("Resources Collected", JLabel.LEFT);
//		titlePanel.add(titleLabel);

		if (resource == ResourceUtil.regolithAR) {
			REGOLITH_TYPES = new AmountResource[] {
					ResourceUtil.regolithAR,
					ResourceUtil.regolithBAR,
					ResourceUtil.regolithCAR,
					ResourceUtil.regolithDAR
			};

			// Create content panel.
			WebPanel regolithPanel = new WebPanel(new SpringLayout());
			add(regolithPanel, BorderLayout.CENTER);

			for (int i=0; i<4; i++) {
				if (i == 0) {
					WebLabel label0 = new WebLabel("  Regolith:    ", JLabel.LEFT); //$NON-NLS-1$
					label0.setAlignmentX(Component.LEFT_ALIGNMENT);
					regolithPanel.add(label0);

					WebLabel l = new WebLabel(0.0 + " kg", JLabel.LEFT);
					regolithLabels[i] = l;
					regolithPanel.add(l);
				}
				else if (i == 1) {
					WebLabel label1 = new WebLabel("  Regolith-B:  ", JLabel.LEFT); //$NON-NLS-1$
					label1.setAlignmentX(Component.LEFT_ALIGNMENT);
					regolithPanel.add(label1);

					WebLabel l = new WebLabel(0.0 + " kg", JLabel.LEFT);
					regolithLabels[i] = l;
					regolithPanel.add(l);
				}
				else if (i == 2) {
					WebLabel label2 = new WebLabel("  Regolith-C:  ", JLabel.LEFT); //$NON-NLS-1$
					label2.setAlignmentX(Component.LEFT_ALIGNMENT);
					regolithPanel.add(label2);

					WebLabel l = new WebLabel(0.0 + " kg", JLabel.LEFT);
					regolithLabels[i] = l;
					regolithPanel.add(l);
				}
				else if (i == 3) {
					WebLabel label3 = new WebLabel("  Regolith-D:  ", JLabel.LEFT); //$NON-NLS-1$
					label3.setAlignmentX(Component.LEFT_ALIGNMENT);
					regolithPanel.add(label3);

					WebLabel l = new WebLabel(0.0 + " kg", JLabel.LEFT);
					regolithLabels[i] = l;
					regolithPanel.add(l);
				}
			}

			// Prepare SpringLayout.
			SpringUtilities.makeCompactGrid(regolithPanel,
					4, 2, // rows, cols
					100, 5, // initX, initY
					30, 4); // xPad, yPad

		}
		else {
			// Create content panel.
			WebPanel contentPanel = new WebPanel(new SpringLayout());
			add(contentPanel, BorderLayout.CENTER);

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
					Double.toString(0.0)
				),
				JLabel.LEFT
			);
			contentPanel.add(collectionValueLabel);

			// Prepare SpringLayout.
			SpringUtilities.makeCompactGrid(contentPanel,
					1, 2, // rows, cols
					100, 5, // initX, initY
					30, 4); // xPad, yPad
		}
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
				if (resource.equals(event.getTarget())){
					updateCollectionValueLabel();
				}
				for (AmountResource ar : REGOLITH_TYPES) {
					if (ar.equals(event.getTarget())) {
						updateCollectionValueLabel();
					}
				}
			}
		}
	}

	/**
	 * Updates the collection value label.
	 */
	private void updateCollectionValueLabel() {

		if (resource == ResourceUtil.iceAR) {
			double resourceAmount = mission.getIceCollected();

			// Update collection value label.
			collectionValueLabel.setText(
				Msg.getString("CollectResourcesMissionCustomInfoPanel.kilograms", //$NON-NLS-1$
						Math.round(resourceAmount*10D)/10D));
		}

		else {
			double[] resourcesAmount = mission.getRegolithCollected();

			for (int i=0; i<4; i++) {
				if (i == 0) {
					regolithLabels[i].setText(Math.round(resourcesAmount[i]*10D)/10D + " kg");
				}
				else if (i == 1) {
					regolithLabels[i].setText(Math.round(resourcesAmount[i]*10D)/10D + " kg");
				}
				else if (i == 2) {
					regolithLabels[i].setText(Math.round(resourcesAmount[i]*10D)/10D + " kg");
				}
				else if (i == 3) {
					regolithLabels[i].setText(Math.round(resourcesAmount[i]*10D)/10D + " kg");
				}
			}
		}
	}
}

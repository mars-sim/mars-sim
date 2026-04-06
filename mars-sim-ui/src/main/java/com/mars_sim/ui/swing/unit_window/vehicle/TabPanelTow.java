/*
 * Mars Simulation Project
 * TabPanelTow.java
 * @date 2024-07-24
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.unit_window.vehicle;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import com.mars_sim.core.EntityEvent;
import com.mars_sim.core.EntityEventType;
import com.mars_sim.core.EntityListener;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.vehicle.StatusType;
import com.mars_sim.core.vehicle.Towing;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.components.EntityLabel;
import com.mars_sim.ui.swing.entitywindow.EntityTabPanel;
import com.mars_sim.ui.swing.utils.AttributePanel;


@SuppressWarnings("serial")
class TabPanelTow extends EntityTabPanel<Vehicle> 
		implements EntityListener {

	private static final String TOW_ICON = "tow";
	
	// Data members
	private EntityLabel towingLabel;
	private EntityLabel towedByLabel;

	public TabPanelTow(Vehicle unit, UIContext context) {
		// Use TabPanel constructor.
		super(
			Msg.getString("TabPanelTow.title"),
			ImageLoader.getIconByName(TOW_ICON),
			Msg.getString("TabPanelTow.title"),
			context, unit
		);
	}

	@Override
	protected void buildUI(JPanel content) {
		var mainPane = new AttributePanel();
		content.add(mainPane, BorderLayout.NORTH);
		
		towingLabel = new EntityLabel(getContext());
		mainPane.addLabelledItem(Msg.getString("TabPanelTow.towing"), towingLabel);

		towedByLabel = new EntityLabel(getContext());
		mainPane.addLabelledItem(Msg.getString("TabPanelTow.towedBy"), towedByLabel);

		refreshStatus();
	}

	/**
	 * Listen for status changes to trigger towing updates
	 * @param event Details of what changed
	 */
	@Override
	public void entityUpdate(EntityEvent event) {
		
		// Update towed by info
		if (event.getType().equals(EntityEventType.STATUS_EVENT)) {
			refreshStatus();
		}
	}

	/**
	 * Refresh the towing/towed by info
	 */
	private void refreshStatus() {
		var vehicle = getEntity();
		if (vehicle.haveStatusType(StatusType.TOWED)) {
			towedByLabel.setEntity(vehicle.getTowingVehicle());
		}
		else {
			towedByLabel.setEntity(null);
		}

		if (vehicle instanceof Towing t && vehicle.haveStatusType(StatusType.TOWING)) {
			towingLabel.setEntity(t.getTowedVehicle());
		}
		else {
			towingLabel.setEntity(null);
		}
	}
}

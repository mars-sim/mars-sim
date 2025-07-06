/*
 * Mars Simulation Project
 * TabPanelTow.java
 * @date 2024-07-24
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.unit_window.vehicle;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.vehicle.StatusType;
import com.mars_sim.core.vehicle.Towing;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.MainDesktopPane;
import com.mars_sim.ui.swing.components.EntityLabel;
import com.mars_sim.ui.swing.unit_window.TabPanel;
import com.mars_sim.ui.swing.utils.AttributePanel;


@SuppressWarnings("serial")
public class TabPanelTow extends TabPanel {

	private static final String TOW_ICON = "tow";
	
	// Data members
	private EntityLabel towingLabel;
	private EntityLabel towedByLabel;
	
	/** The Vehicle instance. */
	private Vehicle vehicle;

	public TabPanelTow(Vehicle unit, MainDesktopPane desktop) {
		// Use TabPanel constructor.
		super(
			null,
			ImageLoader.getIconByName(TOW_ICON),
			Msg.getString("TabPanelTow.title"), //$NON-NLS-1$
			desktop
		);

		vehicle = unit;
	}

	@Override
	protected void buildUI(JPanel content) {
		var mainPane = new AttributePanel();
		content.add(mainPane, BorderLayout.NORTH);
		
		towingLabel = new EntityLabel(getDesktop());
		mainPane.addLabelledItem(Msg.getString("TabPanelTow.towing"), towingLabel);

		towedByLabel = new EntityLabel(getDesktop());
		mainPane.addLabelledItem(Msg.getString("TabPanelTow.towedBy"), towedByLabel);

		update();
	}

	/**
	 * Updates the info on this panel.
	 */
	@Override
	public void update() {
		
		if (vehicle instanceof Towing t) {
			towingLabel.setEntity(t.getTowedVehicle());
		}
		
		if (vehicle.haveStatusType(StatusType.TOWED)) {
			towedByLabel.setEntity(vehicle.getTowingVehicle());
		}
		else {
			towedByLabel.setEntity(null);
		}
	}
}

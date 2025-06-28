/*
 * Mars Simulation Project
 * MissionTab.java
 * @date 2021-12-07
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.tool.monitor;

import java.util.List;

import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.person.ai.mission.VehicleMission;
import com.mars_sim.ui.swing.tool.mission.MissionWindow;

/**
 * This class represents a mission table displayed within the Monitor Window.
 */
public class MissionTab extends TableTab {

	/**
	 * Constructor.
	 * 
	 * @throws Exception
	 */
	public MissionTab(final MonitorWindow window) {
		// Use TableTab constructor
		super(window, new MissionTableModel(window.getDesktop().getSimulation()), true, true, MissionWindow.ICON);
				
		setEntityDriven(true);
		setNavigatable(true);
	}


	/**
	 * Gets the Coordinates of the selected Mission.
	 * 
	 * @return Coordinates, maybe null
	 */
	@Override
    public Coordinates getSelectedCoordinates() {
		List<?> rows = getSelection();
		if (!rows.isEmpty() && (rows.get(0) instanceof VehicleMission m)) {
			return m.getCurrentMissionLocation();
		}
		return null;
    }
}

/**
 * Mars Simulation Project
 * MissionTab.java
* @version 3.1.0 2017-09-14
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.monitor;

import java.util.Iterator;
import java.util.List;

import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionMember;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.tool.mission.MissionWindow;

/**
 * This class represents a mission table displayed within the Monitor Window.
 */
public class MissionTab extends TableTab {

	/**
	 * Constructor.
	 */
	public MissionTab(final MonitorWindow window) {
		// Use TableTab constructor
		super(window, new MissionTableModel(), true, true, MonitorWindow.MISSION_ICON);
	}

	/**
	 * Display selected mission in mission tool.
	 * 
	 * @param desktop the main desktop.
	 */
	public void displayMission(MainDesktopPane desktop) {
		List<?> selection = getSelection();
		if (selection.size() > 0) {
			Object selected = selection.get(0);
			if (selected instanceof Mission) {
				((MissionWindow) desktop.getToolWindow(MissionWindow.NAME)).selectMission((Mission) selected);
				desktop.openToolWindow(MissionWindow.NAME);
			}
		}
	}

	/**
	 * Center the map on the first selected row.
	 * 
	 * @param desktop Main window of application.
	 */
	public void centerMap(MainDesktopPane desktop) {
		List<?> rows = getSelection();
		Iterator<?> it = rows.iterator();
		if (it.hasNext()) {
			Mission mission = (Mission) it.next();
			if (mission.getMembersNumber() > 0) {
				MissionMember member = (MissionMember) mission.getMembers().toArray()[0];
				desktop.centerMapGlobe(member.getCoordinates());
			}
		}
	}
}
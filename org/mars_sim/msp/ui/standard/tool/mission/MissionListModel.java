/**
 * Mars Simulation Project
 * MissionListModel.java
 * @version 2.81 2007-08-27
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard.tool.mission;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.AbstractListModel;

import org.mars_sim.msp.simulation.Simulation;
import org.mars_sim.msp.simulation.person.ai.mission.Mission;
import org.mars_sim.msp.simulation.person.ai.mission.MissionEvent;
import org.mars_sim.msp.simulation.person.ai.mission.MissionListener;
import org.mars_sim.msp.simulation.person.ai.mission.MissionManagerListener;
import org.mars_sim.msp.simulation.person.ai.mission.MissionManager;

/**
 * List model for the mission list.
 */
public class MissionListModel extends AbstractListModel implements
		MissionManagerListener, MissionListener {

	// Private members.
	private List<Mission> missions;
	
	/**
	 * Constructor
	 */
	public MissionListModel() {
		missions = new ArrayList<Mission>();
		
		// Add all current missions.
		MissionManager manager = Simulation.instance().getMissionManager();
		Iterator<Mission> i = manager.getMissions().iterator();
		while (i.hasNext()) addMission(i.next());
		
		// Add list as mission manager listener.
		manager.addListener(this);
	}
	
	/**
	 * Adds a mission to this list.
	 * @param mission the mission to add.
	 */
	public void addMission(Mission mission) {
		if (!missions.contains(mission)) {
			missions.add(mission);
			mission.addMissionListener(this);
			fireIntervalAdded(this, missions.size() - 1, missions.size() - 1);
		}
	}

	/**
	 * Removes a mission from this list.
	 * @param mission mission to remove.
	 */
	public void removeMission(Mission mission) {
		if (missions.contains(mission)) {
			int index = missions.indexOf(mission);
			missions.remove(mission);
			mission.removeMissionListener(this);
			fireIntervalRemoved(this, index, index);
		}
	}
	
	/**
	 * Catch mission update event.
	 * @param event the mission event.
	 */
	public void missionUpdate(MissionEvent event) {
		if (event.getType().equals(Mission.DESCRIPTION_EVENT)) {
			int index = missions.indexOf(event.getSource());
			if ((index > -1) && (index < missions.size())) fireContentsChanged(this, index, index);
		}
	}

	/**
	 * Gets the list size.
	 * @return size.
	 */
	public int getSize() {
		return missions.size();
	}

	/**
	 * Gets the list element at a given index.
	 * @param index the index.
	 * @return the object at the index or null if one.
	 */
	public Object getElementAt(int index) {
		try {
			return missions.get(index);
		}
		catch (IndexOutOfBoundsException e) {
			return null;
		}
	}
	
	/**
	 * Checks if the list contains a given mission.
	 * @param mission the mission to check for.
	 * @return true if list contains the mission.
	 */
	public boolean containsMission(Mission mission) {
		if ((missions != null) && missions.contains(mission)) return true;
		else return false;
	}
	
	/**
	 * Gets the index a given mission is at.
	 * @param mission the mission to check for.
	 * @return the index for the mission or -1 if not in list.
	 */
	public int getMissionIndex(Mission mission) {
		if (containsMission(mission)) return missions.indexOf(mission);
		else return -1;
	}
	
	/**
	 * Prepares the list for deletion.
	 */
	public void destroy() {
		missions.clear();
		missions = null;
		Simulation.instance().getMissionManager().removeListener(this);
	}
}
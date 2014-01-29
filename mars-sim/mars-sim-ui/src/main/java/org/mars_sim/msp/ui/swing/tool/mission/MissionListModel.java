/**
 * Mars Simulation Project
 * MissionListModel.java
 * @version 3.06 2014-01-29
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.tool.mission;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.person.ai.mission.*;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
			SwingUtilities.invokeLater(new MissionListUpdater(
					MissionListUpdater.ADD, this, missions.size() - 1));
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
			SwingUtilities.invokeLater(new MissionListUpdater(
					MissionListUpdater.REMOVE, this, index));
		}
	}
	
	/**
	 * Catch mission update event.
	 * @param event the mission event.
	 */
	public void missionUpdate(MissionEvent event) {
		if (event.getType().equals(Mission.DESCRIPTION_EVENT)) {
			int index = missions.indexOf(event.getSource());
			if ((index > -1) && (index < missions.size())) {
				SwingUtilities.invokeLater(new MissionListUpdater(
						MissionListUpdater.CHANGE, this, index));
			}
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
        return (missions != null) && missions.contains(mission);
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
	
	/**
	 * Inner class for updating the mission list.
	 */
	private class MissionListUpdater implements Runnable {
		
		private static final int ADD = 0;
		private static final int REMOVE = 1;
		private static final int CHANGE = 2;
		
		private int mode;
		private MissionListModel model;
		private int row;
		
		private MissionListUpdater(int mode, MissionListModel model, int row) {
			this.mode = mode;
			this.model = model;
			this.row = row;
		}
		
		public void run() {
			switch (mode) {
				case ADD : {
					fireIntervalAdded(model, row, row);
				} break;
				case REMOVE : {
					fireIntervalRemoved(model, row, row);
				} break;
				case CHANGE : {
					fireContentsChanged(model, row, row);
				} break;
			}
		}
	}
}
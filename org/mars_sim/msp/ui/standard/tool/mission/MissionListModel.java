/**
 * Mars Simulation Project
 * MissionListModel.java
 * @version 2.80 2006-08-04
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard.tool.mission;

import java.util.List;
import javax.swing.AbstractListModel;

import org.mars_sim.msp.simulation.Simulation;
import org.mars_sim.msp.simulation.person.ai.mission.Mission;
import org.mars_sim.msp.simulation.person.ai.mission.MissionListener;
import org.mars_sim.msp.simulation.person.ai.mission.MissionManager;

public class MissionListModel extends AbstractListModel implements
		MissionListener {

	private List missions;
	
	public MissionListModel() {
		MissionManager manager = Simulation.instance().getMissionManager();
		missions = manager.getMissions();
		manager.addListener(this);
	}
	
	public void addMission(Mission mission) {
		if (!missions.contains(mission)) {
			missions.add(mission);
			fireIntervalAdded(this, missions.size() - 1, missions.size() - 1);
		}
	}

	public void removeMission(Mission mission) {
		if (missions.contains(mission)) {
			int index = missions.indexOf(mission);
			missions.remove(mission);
			fireIntervalRemoved(this, index, index);
		}
	}
	
	public void update() {
		fireContentsChanged(this, 0, missions.size());
	}

	public int getSize() {
		return missions.size();
	}

	public Object getElementAt(int index) {
		if ((index > -1) && (index < missions.size())) return missions.get(index);
		else throw new IllegalArgumentException("index: " + index + " not in valid bounds.");
	}
}
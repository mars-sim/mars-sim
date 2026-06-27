/*
 * Mars Simulation Project
 * MissionTableModel.java
 * @date 2025-10-16
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.tool.monitor;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import com.mars_sim.core.EntityEvent;
import com.mars_sim.core.mission.MissionControl;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.utils.model.BaseMissionModel;

/**
 * This class model how mission data is organized and displayed
 * within the Monitor Window for all settlements.
 */
@SuppressWarnings("serial")
class MissionTableModel extends BaseMissionModel implements MonitorModel {

	private static final String MISSIONS = Msg.getString("mission.plural");
	private Set<Settlement> settlements = Collections.emptySet();

	/**
	 * Constructor 1.
	 */
	public MissionTableModel() {
		super(NAME, PHASE, DATE_FILED, DATE_EMBARKED, DATE_COMPLETED, SETTLEMENT, LEADER,
				DESIGNATION, VEHICLE, MEMBER_NUM,
				REMAINING_TO_NAVPOINT, REMAINING_TO_END,
				ACTUAL_TRAVELLED);
	}
	
	@Override
	public String getName() {
		return MISSIONS;
	}

	@Override
	public int getSettlementColumn() {
		return 5;
	}

	/**
	 * Sets the settlement filter.
	 */
	@Override
	public boolean setSettlementFilter(Set<Settlement> filter) {
		
		settlements.forEach(s -> s.removeEntityListener(this));
		Collection<Mission> missions = filter.stream()
				.flatMap(s -> s.getMissionControl().getAllMissions().stream())
				.toList();
	
		setEntities(missions);

		// Change listeners to match the new filter
		settlements = filter;
		settlements.forEach(s -> s.addEntityListener(this));
		return true;
	}

	/**
	 * Catches mission update event.
	 *
	 * @param event the entity event.
	 */
	@Override
	public void entityUpdate(EntityEvent event) {
		if (event.getSource() instanceof Settlement) {
			if (event.getType().equals(MissionControl.MISSION_ADD)) {
				var m = (Mission) event.getTarget();

				// Should never fail but good to check
				if (settlements.contains(m.getAssociatedSettlement())) {
					addEntity(m);
				}
			}
			else if (event.getType().equals(MissionControl.MISSION_REMOVED)) {
				removeEntity((Mission) event.getTarget());
			}
		}
		else {
			super.entityUpdate(event);
		}
	}

	/**
	 * Releases the model and removes all settlementlisteners.
	 */
    @Override
    public void release() {
        settlements.forEach(s -> s.removeEntityListener(this));
        super.release();
    }
}
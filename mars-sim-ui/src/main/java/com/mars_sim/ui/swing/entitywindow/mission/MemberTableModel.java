/*
 * Mars Simulation Project
 * MemberTableModel.java
 * @date 2026-05-21
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.entitywindow.mission;

import java.util.Set;

import com.mars_sim.core.EntityEventType;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.person.ai.mission.VehicleMission;
import com.mars_sim.core.person.ai.task.util.TaskManager;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.unit.MobileUnit;
import com.mars_sim.core.vehicle.Crewable;
import com.mars_sim.core.vehicle.Rover;
import com.mars_sim.ui.swing.components.ColumnSpec;
import com.mars_sim.ui.swing.utils.model.BaseWorkerModel;

/**
 * Table model for mission members. Shows them on board or in an airlock
 */
public class MemberTableModel extends BaseWorkerModel {
	private static final int BOARDED_VAL = 201;
	private static final int AIRLOCK_VAL = 202;

	protected static final EntityColumnSpec BOARDED = new EntityColumnSpec(new ColumnSpec(BOARDED_VAL, Msg.getString("MainDetailPanel.column.boarded"),
                                                    Boolean.class), Set.of(MobileUnit.CONTAINER_EVENT));	
	protected static final EntityColumnSpec AIRLOCK = new EntityColumnSpec(new ColumnSpec(AIRLOCK_VAL, Msg.getString("MainDetailPanel.column.airlock"),
													Boolean.class), Set.of(EntityEventType.TASK_SUBTASK_EVENT, EntityEventType.STATUS_EVENT, EntityEventType.SPEED_EVENT, TaskManager.TASK_EVENT));											
	
	// Private members.
	private Mission mission = null;
	private Crewable v = null;
	
	/**
	 * Constructor.
	 */
	public MemberTableModel(Mission mission) {
		super(NAME, TASK, BOARDED, AIRLOCK);
        this.mission = mission;
		if ((mission instanceof VehicleMission vm)
                && (vm.getVehicle() instanceof Crewable c)) {
			v = c;
		}
		updateOccupantList();
	}

    public MemberTableModel(Crewable crewable) {
        super(NAME, TASK, BOARDED, AIRLOCK);
        this.v = crewable;

        updateOccupantList();
    }

	@Override
	protected Object getEntityValue(Worker entity, int valueIndex) {
		return switch(valueIndex) {
			case BOARDED_VAL -> isBoarded(entity);
			case AIRLOCK_VAL -> isInAirlock(entity);
			default -> BaseWorkerModel.getWorkerValue(entity, valueIndex);
		};
	}
	
	/**
	 * Has this member boarded the vehicle ?
	 *
	 * @param member Worker member.
	 * @return Is the worker boarded ?
	 */
	private boolean isBoarded(Worker member) {
		if (member instanceof Person p && v != null) {
			return v.isCrewmember(p);
		}
		return false;
	}
	
	/**
	 * Is this member currently in vehicle's airlock ?
	 *
	 * @param member	 Worker member.
	 * @return Is the worker in the airlock ?
	 */
	private boolean isInAirlock(Worker member) {
		return (member instanceof Person p && v instanceof Rover r && r.isInAirlock(p));
	}

    /**
     * Set the mission for this model.
     * @param mission
     */
    public void setMission(Mission mission) {
        this.mission = mission;
        updateOccupantList();
    }

    
    public Mission getMission() {
        return mission;
    }
    
	/**
	 * Updates the occupant list.
	 */
	public void updateOccupantList() {
        if (mission != null) {
            setEntities(mission.getMembers());
        } else if (v != null) {
            setEntities(v.getCrew());
        }
        else {
            setEntities(Set.of());
        }
	}
}
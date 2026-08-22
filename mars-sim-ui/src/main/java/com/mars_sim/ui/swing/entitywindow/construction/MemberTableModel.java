/*
 * Mars Simulation Project
 * MemberTableModel.java
 * @date 2026-05-21
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.entitywindow.construction;

import java.util.Set;

import com.mars_sim.core.EntityEventType;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.mission.ConstructionMission;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.unit.MobileUnit;
import com.mars_sim.core.vehicle.Crewable;
import com.mars_sim.core.vehicle.LightUtilityVehicle;
import com.mars_sim.ui.swing.components.ColumnSpec;
import com.mars_sim.ui.swing.utils.model.BaseWorkerModel;

/**
 * Table model for mission members. Shows them on board or in an airlock
 */
@SuppressWarnings("serial")
public class MemberTableModel extends BaseWorkerModel {
	private static final int MISSION_MEMBER_VAL = 201;
	private static final int BOARDED_VAL = 202;
	private static final int WORK_TIME_VAL = 204;

	protected static final EntityColumnSpec MISSION_MEMBER = new EntityColumnSpec(new ColumnSpec(MISSION_MEMBER_VAL, Msg.getString("MainDetailPanel.column.missionMember"),
            										Boolean.class), Set.of(
            												Mission.ADD_MEMBER_EVENT,
            												Mission.REMOVE_MEMBER_EVENT,
            												Mission.END_MISSION_EVENT));	
	protected static final EntityColumnSpec BOARDED = new EntityColumnSpec(new ColumnSpec(BOARDED_VAL, Msg.getString("MainDetailPanel.column.boarded"),
                                                    Boolean.class), Set.of(MobileUnit.CONTAINER_EVENT));	
	protected static final EntityColumnSpec WORK_TIME = new EntityColumnSpec(new ColumnSpec(WORK_TIME_VAL, Msg.getString("MainDetailPanel.column.airlock"),
													Boolean.class), Set.of(EntityEventType.WORK_TIME_EVENT));											
	
	// Private members.
	private Mission mission = null;
	private Crewable v = null;
	
	/**
	 * Constructor.
	 */
	public MemberTableModel(Mission mission) {
		super(NAME, TASK, MISSION_MEMBER, BOARDED, WORK_TIME);
        this.mission = mission;
	
		updateOccupantList();
	}

    public MemberTableModel(Crewable crewable) {
        super(NAME, TASK, MISSION_MEMBER, BOARDED, WORK_TIME);
        this.v = crewable;

        updateOccupantList();
    }

	@Override
	protected Object getEntityValue(Worker entity, int valueIndex) {
		return switch(valueIndex) {
			case MISSION_MEMBER_VAL -> isMissionMember(entity);
			case BOARDED_VAL -> isBoarded(entity);
			case WORK_TIME_VAL -> getWorkTime(entity);
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
		if (v != null) {
			if (v instanceof Crewable c) {
				if (member instanceof Person p)
					return c.isCrewmember(p);
				else if (member instanceof Robot r)
					return c.isRobotCrewmember(r);
			}
			else if (v instanceof LightUtilityVehicle luv) {
				return luv.isCrewmember(member);
			}
		}
		return false;
	}

	/**
	 * Gets the work time.
	 *
	 * @param member	 Worker member.
	 * @return Is the worker in the airlock ?
	 */
	private double getWorkTime(Worker member) {
		double time = 0;
		if (mission instanceof ConstructionMission cm) {
			time = cm.getObjective().getWorkTime(member.getIdentifier());
		}
		return time;
	}

	/**
	 * Is this a mission member ?
	 *
	 * @param member	 Worker member.
	 * @return Is the worker in the airlock ?
	 */
	private boolean isMissionMember(Worker member) {
		return (member.getMission() != null && member.getMission().equals(mission));
	}
	
    /**
     * Sets the mission for this model.
     * 
     * @param mission
     */
    public void setMission(Mission mission) {
        this.mission = mission;
        updateOccupantList();
    }

    
    /**
     * Gets the mission.
     * 
     * @return
     */
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
/**
 * Mars Simulation Project
 * Role.java
 * @version 3.1.0 2017-08-30
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.time.MarsClock;

public class Role implements Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

	private Person person;

	private MarsClock clock;

	private RoleType roleType;

    private Map<RoleType, MarsClock> roleHistory = new ConcurrentHashMap<>();

    // TODO: Use more methods of parallel operation in ConcurrentHashMap.
    // see https://dzone.com/articles/concurrenthashmap-in-java8
    // see https://dzone.com/articles/how-concurrenthashmap-works-internally-in-java
    // see https://dzone.com/articles/concurrenthashmap-isnt-always-enough

	public Role(Person person) {
		this.person = person;

	}

	/**
	 * Gets the type of role.
	 * @return role type
	 */
	public RoleType getType() {
		return roleType;
	}

	/**
	 * Releases the old role type.
	 * @param role type
	 */
	public void relinquishOldRoleType(RoleType oldType) {

		if (oldType != null) {
		    person.getAssociatedSettlement().getChainOfCommand().releaseRoleTypeMap(oldType);
		}
	}

	/**
	 * Sets new role type.
	 * @param role type
	 */
	public void setNewRoleType(RoleType newType) {
        RoleType oldType = roleType;//getType();

	    if (newType != oldType) {
	        this.roleType = newType;
	        person.getAssociatedSettlement().getChainOfCommand().addRoleTypeMap(newType);
	        person.fireUnitUpdate(UnitEventType.ROLE_EVENT, newType);
	        relinquishOldRoleType(oldType);

	       	// 2016-05-02 Added saving roleHistory
	       	if (clock == null)
        		clock = Simulation.instance().getMasterClock().getMarsClock();
	       	roleHistory.put(newType, clock);
	    }
/*
		if (type == RoleType.SAFETY_SPECIALIST)
			person.getSettlement().getChainOfCommand().addRole(type);
		else if (type == RoleType.ENGINEERING_SPECIALIST)
			person.getSettlement().addEngr();
		else if (type == RoleType.RESOURCE_SPECIALIST)
			person.getSettlement().addResource();
		else if (type == RoleType.MISSION_SPECIALIST)
			person.getSettlement().addMission();
		else if (type == RoleType.SCIENCE_SPECIALIST)
			person.getSettlement().addScience();
		else if (type == RoleType.LOGISTIC_SPECIALIST)
			person.getSettlement().addLogistic();
		else if (type == RoleType.AGRICULTURE_SPECIALIST)
			person.getSettlement().addAgri();
*/
	}

	/**
	 * Override {@link Object#toString()} method.
	 */
	@Override
	public String toString() {
		return roleType.getName();
	}

}

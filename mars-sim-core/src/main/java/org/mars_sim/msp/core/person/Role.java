/**
 * Mars Simulation Project
 * Role.java
 * @version 3.08 2015-05-19
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person;

import java.io.Serializable;

import org.mars_sim.msp.core.UnitEventType;

public class Role implements Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

	String name;
	Person person;
	RoleType type;

	public Role(Person person) { //, RoleType type) {
		//this.type = type;
		this.person = person;
	}

	/**
	 * Gets the type of role.
	 * @return role type
	 */
	public RoleType getType() {
		return type;
	}

	public void relinquishOldRoleType() {
		RoleType type = getType();
		if (type != null) {
		    person.getAssociatedSettlement().getChainOfCommand().releaseJobRoleMap(type);
		}
	}

	/**
	 * Sets the type of role.
	 * @param role type
	 */
	public void setNewRoleType(RoleType type) {

	    if (type != getType()) {
	        relinquishOldRoleType();
//	        System.out.println("New RoleType is "+ type);
	        this.type = type;
	        person.getAssociatedSettlement().getChainOfCommand().addJobRoleMap(type);
	        person.fireUnitUpdate(UnitEventType.ROLE_EVENT, type);
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
		return type.getName();
	}

}

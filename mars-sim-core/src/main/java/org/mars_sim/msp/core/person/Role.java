/**
 * Mars Simulation Project
 * Role.java
 * @version 3.08 2015-04-28
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person;

import java.io.Serializable;

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

	/**
	 * Sets the type of role.
	 * @param role type
	 */
	public void setRoleType(RoleType type) {
    	//System.out.println("RoleType is "+ type);
		this.type = type;
	}

	/**
	 * Override {@link Object#toString()} method.
	 */
	@Override
	public String toString() {
		return type.getName();
	}

}

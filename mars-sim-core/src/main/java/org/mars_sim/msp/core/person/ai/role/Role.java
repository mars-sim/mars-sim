/**
 * Mars Simulation Project
 * Role.java
 * @version 3.2.0 2021-06-20
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.ai.role;

import java.io.Serializable;
import java.util.List;

import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.data.History;
import org.mars_sim.msp.core.data.History.HistoryItem;
import org.mars_sim.msp.core.person.Person;

public class Role implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	
	private Person person;

	private RoleType roleType;

	private History<RoleType> roleHistory = new History<>();

	// TODO: Use more methods of parallel operation in ConcurrentHashMap.
	// see https://dzone.com/articles/concurrenthashmap-in-java8
	// see https://dzone.com/articles/how-concurrenthashmap-works-internally-in-java
	// see https://dzone.com/articles/concurrenthashmap-isnt-always-enough

	public Role(Person person) {
		this.person = person;
	}

	/**
	 * Gets the type of role.
	 * 
	 * @return role type
	 */
	public RoleType getType() {
		return roleType;
	}

	/**
	 * Releases the old role type.
	 * 
	 * @param role type
	 */
	public void relinquishOldRoleType() {

		if (roleType != null) {
			if (person.getAssociatedSettlement() != null)		
				person.getAssociatedSettlement().getChainOfCommand().releaseRole(roleType);
			else 
				person.getBuriedSettlement().getChainOfCommand().releaseRole(roleType);
		}
	}

	/**
	 * Changes the role type.
	 *                                                                                                                                                                                                                
	 * @param newType the new role
	 */
	public void changeRoleType(RoleType newType) {
		RoleType oldType = roleType;

		if (newType == null) {
			throw new IllegalArgumentException("New roletype cannot be null.");
		}

		if (newType != oldType) {
			// Note : if this is a leadership role, only one person should occupy this position 
			List<Person> predecessors = null;
			if (newType.isChief() || newType.isCouncil()) {
				// Find a list of predecessors who are occupying this role
				predecessors = person.getAssociatedSettlement().getChainOfCommand().findPeopleWithRole(newType);
				if (!predecessors.isEmpty()) {
					// Predecessors to seek for a new role to fill
					predecessors.get(0).getRole().obtainNewRole();
				}
			}
			
			// Turn in the old role
			relinquishOldRoleType();

			// Set the role type of this person to the new role type
			roleType = newType;
			roleHistory.add(roleType);
			
			// Save the role in the settlement Registry
			person.getAssociatedSettlement().getChainOfCommand().registerRole(roleType);

			// Records the role change and fire unit update
			person.fireUnitUpdate(UnitEventType.ROLE_EVENT, roleType);
		}
	}

	/**
	 * How has this Perosns role assignment changed over time
	 * @return
	 */
	public List<HistoryItem<RoleType>> getChanges() {
		return roleHistory.getChanges();
	}
	
	/**
	 * Obtains a new role 
	 * 
	 * @param s
	 */
	public void obtainNewRole() {
		// Find the best role
		RoleType roleType = RoleUtil.findBestRole(person);	
		// Finalize setting a person's new role
		changeRoleType(roleType);
	}
	
	/**
	 * Override {@link Object#toString()} method.
	 */
	@Override
	public String toString() {
		return roleType.getName();
	}
}

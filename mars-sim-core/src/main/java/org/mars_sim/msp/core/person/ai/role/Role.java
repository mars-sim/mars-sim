/**
 * Mars Simulation Project
 * Role.java
 * @version 3.1.0 2017-08-30
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.ai.role;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.time.MarsClock;

public class Role implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
//	private static Logger logger = Logger.getLogger(Role.class.getName());
//	private static String loggerName = logger.getName();
//	private static String sourceName = loggerName.substring(loggerName.lastIndexOf(".") + 1, loggerName.length());
	
	private Person person;

	private RoleType roleType;

	private Map<RoleType, MarsClock> roleHistory = new ConcurrentHashMap<>();

	private static MarsClock marsClock = Simulation.instance().getMasterClock().getMarsClock();

	// TODO: Use more methods of parallel operation in ConcurrentHashMap.
	// see https://dzone.com/articles/concurrenthashmap-in-java8
	// see https://dzone.com/articles/how-concurrenthashmap-works-internally-in-java
	// see https://dzone.com/articles/concurrenthashmap-isnt-always-enough

	public Role(Person person) {
		this.person = person;
		
//		marsClock = Simulation.instance().getMasterClock().getMarsClock();
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
	 * Sets the new role type.
	 *                                                                                                                                                                                                                
	 * @param role type
	 */
	public void setNewRoleType(RoleType newType) {
		RoleType oldType = roleType;

		if (newType != oldType) {
			// Note : if this is a leadership role, only one person should occupy this position 
			List<Person> predecessors = null;
			if (RoleUtil.isLeadershipRole(newType)) {
				// Find a list of predecessors who are occupying this role
				predecessors = person.getAssociatedSettlement().getChainOfCommand().findPeopleWithRole(newType);
				if (predecessors != null) {
					for (Person p: predecessors) {
						// Predecessors to seek for a new role to fill
						p.getRole().obtainRole();
					}
				}
			}
			
			this.roleType = newType;
			
			// Save the role in the settlement Registry
			person.getAssociatedSettlement().getChainOfCommand().registerRole(newType);

			// Turn in the old role
			relinquishOldRoleType();

//			if (oldType != null) {				
//				String s = String.format("[%s] %18s (Role) : %s -> %s",
//						person.getLocationTag().getLocale(), 
//						person.getName(), 
//						oldType,
//						newType);
//				
//				LogConsolidated.log(Level.CONFIG, 0, sourceName, s);
//			}
//
//			else {
//				String s = String.format("[%s] %18s (Role) -> %s",
//						person.getLocationTag().getLocale(), 
//						person.getName(), 
//						newType);
//				
//				LogConsolidated.log(Level.CONFIG, 0, sourceName, s);
//			}
			
			// Save the new role in roleHistory
			roleHistory.put(newType, marsClock);
			// Fire the role event
			person.fireUnitUpdate(UnitEventType.ROLE_EVENT, newType);
		}
	}

	/**
	 * Obtains a role 
	 * 
	 * @param s
	 */
	public void obtainRole() {
		// Find the best role
		RoleType roleType = RoleUtil.findBestRole(person);	
		// Finalize setting a person's new role
		person.getRole().setNewRoleType(roleType);
	}
	
	/**
	 * Reloads instances after loading from a saved sim
	 * 
	 * @param clock
	 */
	public static void initializeInstances(MarsClock clock) {
		marsClock = clock;
	}
	
	/**
	 * Override {@link Object#toString()} method.
	 */
	@Override
	public String toString() {
		return roleType.getName();
	}

	public void destroy() {
		person = null;
		roleType = null;
		roleHistory = null;
		marsClock = null;
	}
}

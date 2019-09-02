/**
 * Mars Simulation Project
 * Role.java
 * @version 3.1.0 2017-08-30
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.ai.role;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.LogConsolidated;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.structure.ChainOfCommand;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.time.MarsClock;

public class Role implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static Logger logger = Logger.getLogger(Role.class.getName());
	private static String loggerName = logger.getName();
	private static String sourceName = loggerName.substring(loggerName.lastIndexOf(".") + 1, loggerName.length());
	
	private Person person;

	private RoleType roleType;

	private Map<RoleType, MarsClock> roleHistory = new ConcurrentHashMap<>();

	private static MarsClock marsClock;

	// TODO: Use more methods of parallel operation in ConcurrentHashMap.
	// see https://dzone.com/articles/concurrenthashmap-in-java8
	// see https://dzone.com/articles/how-concurrenthashmap-works-internally-in-java
	// see https://dzone.com/articles/concurrenthashmap-isnt-always-enough

	public Role(Person person) {
		this.person = person;
		
		if (Simulation.instance().getMasterClock() != null) {
			// check for null in order to pass the LoadVehicleTest.java maven test
			marsClock = Simulation.instance().getMasterClock().getMarsClock();
		}
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
			this.roleType = newType;
			
			// Save the role in the settlement Registry
			person.getAssociatedSettlement().getChainOfCommand().registerRole(newType);
			// Fire the role event
			person.fireUnitUpdate(UnitEventType.ROLE_EVENT, newType);
			// Turn in the old role
			relinquishOldRoleType();

			if (oldType != null)
				LogConsolidated.log(Level.CONFIG, 0, sourceName,
					"[" + person.getLocationTag().getLocale() + "] " + person.getName() 
					+ " relinquished the " + oldType + " role"
					+ " and took over the " + newType + " role.");
			else
				LogConsolidated.log(Level.CONFIG, 0, sourceName,
						"[" + person.getLocationTag().getLocale() + "] " + person.getName() 
						+ " took over the " + newType + " role.");
			
			// Save the new role in roleHistory
			roleHistory.put(newType, marsClock);
		}
	}

	/**
	 * Obtains a role 
	 * 
	 * @param s
	 */
	public void obtainRole(Settlement s) {
		ChainOfCommand cc = s.getChainOfCommand();
		cc.set7Divisions(true);
		// Assign a role
		cc.assignSpecialistRole(person);	
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

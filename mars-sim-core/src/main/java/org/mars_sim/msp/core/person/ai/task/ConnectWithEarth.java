/**
 * Mars Simulation Project
 * ConnectWithEarth.java
 * @version 3.1.2 2020-09-02
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.ai.task;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.LogConsolidated;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.role.RoleType;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.person.ai.task.utils.TaskPhase;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.structure.building.function.Communication;
import org.mars_sim.msp.core.vehicle.Rover;

/**
 * The ConnectWithEarth class is a task of connecting with Earth's family,
 * relatives and friends
 */
public class ConnectWithEarth extends Task implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(ConnectWithEarth.class.getName());

	private static String sourceName = logger.getName().substring(logger.getName().lastIndexOf(".") + 1,
			logger.getName().length());

	/** Task name */
	private static final String NAME = Msg.getString("Task.description.connectWithEarth"); //$NON-NLS-1$

	/** Task phases. */
	private static final TaskPhase CONNECTING_EARTH = new TaskPhase(Msg.getString("Task.phase.connectingWithEarth")); //$NON-NLS-1$

	// Static members
	/** The stress modified per millisol. */
	private static final double STRESS_MODIFIER = -.5D;

	// Data members
	private boolean proceed = false;
	/** The Communication building the person is using. */
	private Communication comm;

	public RoleType roleType;

	/**
	 * Constructor. This is an effort-driven task.
	 * 
	 * @param person the person performing the task.
	 */
	public ConnectWithEarth(Person person) {
		// Use Task constructor.
		super(NAME, person, true, false, STRESS_MODIFIER, true, 10D + RandomUtil.getRandomDouble(-5D, 5D));

		if (person.isInSettlement()) {
			// set the boolean to true so that it won't be done again today
//			person.getPreference().setTaskDue(this, true);
			
			// If person is in a settlement, try to find an comm facility.
			Building bldg = BuildingManager.getAvailableCommBuilding(person);
			if (bldg != null) {
				// Walk to the facility.
				walkToTaskSpecificActivitySpotInBuilding(bldg, false);
				comm = bldg.getComm();
			} 
			
			else {
				// Find an admin facility.
				bldg = BuildingManager.getAvailableAdminBuilding(person);
				if (bldg != null) {
					// Walk to the facility.
					walkToTaskSpecificActivitySpotInBuilding(bldg, false);
				} 
				
				else {
					// Go back to his quarters
					Building quarters = person.getQuarters();
					if (quarters != null) {
						walkToBed(quarters, person, true);
					}
				}
			}
			
			proceed = true;
			
		} 
		
		else if (person.isInVehicle()) {

			if (person.getVehicle() instanceof Rover) {
				walkToPassengerActivitySpotInRover((Rover) person.getVehicle(), true);

				// set the boolean to true so that it won't be done again today
				person.getPreference().setTaskDue(this, true);
			}
			
			proceed = true;
		} 

		if (proceed) {
			String act = "";
			double rand = RandomUtil.getRandomInt(5);
			if (rand == 0)
				act = "checking personal v-messages ";
			else if (rand == 1)
				act = "watching Earth news ";
			else if (rand == 2)
				act = "browsing MarsNet ";
			else if (rand == 3)
				act = "watching Earth TV ";
			else if (rand == 4)
				act = "watching Earth movies ";
			else if (rand == 5)
				act = "browsing Earth net ";
			
			LogConsolidated.log(logger, Level.INFO, 30_000, sourceName, "[" + person.getLocale() + "] "
					+ person + " was " + act + " in " + person.getImmediateLocation());
			
			// Initialize phase
			addPhase(CONNECTING_EARTH);
			setPhase(CONNECTING_EARTH);
		}
	}

	@Override
	public FunctionType getLivingFunction() {
		return FunctionType.COMMUNICATION;
	}

	@Override
	public double performMappedPhase(double time) {
		if (getPhase() == null) {
			throw new IllegalArgumentException("Task phase is null");
		} else if (CONNECTING_EARTH.equals(getPhase())) {
			return connectingEarth(time);
		} else {
			return time;
		}
	}

	/**
	 * Performs the connecting with earth phase.
	 * 
	 * @param time the amount of time (millisols) to perform the phase.
	 * @return the amount of time (millisols) left over after performing the phase.
	 */
	private double connectingEarth(double time) {
		return 0D;
	}

	@Override
	protected void addExperience(double time) {
		// This task adds no experience.
	}

	@Override
	public void endTask() {
		super.endTask();

		// Remove person from comm function so others can use it.
		if (comm != null && comm.getNumUser() > 0) {
			comm.removeUser();
		}
	}

	@Override
	public int getEffectiveSkillLevel() {
		return 0;
	}

	@Override
	public List<SkillType> getAssociatedSkills() {
		List<SkillType> results = new ArrayList<SkillType>(0);
		return results;
	}

	@Override
	public void destroy() {
		super.destroy();
		roleType= null;
		comm = null;
	}
}

/**
 * Mars Simulation Project
 * WriteReportMeta.java
 * @version 3.08 2015-06-06
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.RoleType;
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.person.ai.task.WriteReport;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.building.Building;

/**
 * Meta task for the WriteReport task.
 */
public class WriteReportMeta implements MetaTask {

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.writeReport"); //$NON-NLS-1$

    public RoleType roleType;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Task constructInstance(Person person) {
        return new WriteReport(person);
    }

    @Override
    public double getProbability(Person person) {

        double result = 0D;

        if (person.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {

            // Probability affected by the person's stress and fatigue.
            PhysicalCondition condition = person.getPhysicalCondition();
            if (condition.getFatigue() < 1200D && condition.getStress() < 75D) {

	            // Get an available office space.
	            Building building = WriteReport.getAvailableOffice(person);

	            if (building != null) {
	                result *= TaskProbabilityUtil.getCrowdingProbabilityModifier(person, building);
	                result *= TaskProbabilityUtil.getRelationshipModifier(person, building);
	            }

	            // Note: if an office space is not available, one can still write reports

	            // Modify if working out is the person's favorite activity.
	            if (person.getFavorite().getFavoriteActivity().equalsIgnoreCase("Administration")) {
	                result += 50D;
	            }

	            if (roleType == null)
	            	roleType = person.getRole().getType();

	            if (roleType.equals(RoleType.PRESIDENT)
	                	|| roleType.equals(RoleType.MAYOR)
	            		|| roleType.equals(RoleType.COMMANDER) )
	            	result += 150D;

	            else if (roleType.equals(RoleType.CHIEF_OF_AGRICULTURE)
	            	|| roleType.equals(RoleType.CHIEF_OF_ENGINEERING)
	            	|| roleType.equals(RoleType.CHIEF_OF_LOGISTICS_N_OPERATIONS)
	            	|| roleType.equals(RoleType.CHIEF_OF_MISSION_PLANNING)
	            	|| roleType.equals(RoleType.CHIEF_OF_SAFETY_N_HEALTH)
	            	|| roleType.equals(RoleType.CHIEF_OF_SCIENCE)
	            	|| roleType.equals(RoleType.CHIEF_OF_SUPPLY) )
	            	result += 100D;

	            // Effort-driven task modifier.
	            result *= person.getPerformanceRating();

	            //System.out.println("WriteReport's result is : " +result);
            }
        }

        return result;
    }

	@Override
	public Task constructInstance(Robot robot) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getProbability(Robot robot) {
		// TODO Auto-generated method stub
		return 0;
	}
}
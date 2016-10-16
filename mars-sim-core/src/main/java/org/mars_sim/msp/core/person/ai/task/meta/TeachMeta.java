/**
 * Mars Simulation Project
 * TeachMeta.java
 * @version 3.08 2015-06-08
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.io.Serializable;
import java.util.Collection;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.person.ai.task.Teach;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;

/**
 * Meta task for the Teach task.
 */
public class TeachMeta implements MetaTask, Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;
    
    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.teach"); //$NON-NLS-1$

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Task constructInstance(Person person) {
        return new Teach(person);
    }

    @Override
    public double getProbability(Person person) {

        double result = 0D;

        if (person.getLocationSituation() == LocationSituation.IN_SETTLEMENT
            	|| person.getLocationSituation() == LocationSituation.IN_VEHICLE) {

	        // Find potential students.
	        Collection<Person> potentialStudents = Teach.getBestStudents(person);
	        if (potentialStudents.size() > 0) {

	            result = 50D;

	            Person student = (Person) potentialStudents.toArray()[0];
                Building building = BuildingManager.getBuilding(student);

                if (building != null) {

                    result *= TaskProbabilityUtil.getCrowdingProbabilityModifier(person,
                            building);
                    result *= TaskProbabilityUtil.getRelationshipModifier(person, building);

                }
                
    	        // 2015-06-07 Added Preference modifier
    	        if (result > 0)
    	         	result = result + result * person.getPreference().getPreferenceScore(this)/5D;
    	    	
    	        if (result < 0) result = 0;

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
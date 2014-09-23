/**
 * Mars Simulation Project
 * TeachMeta.java
 * @version 3.07 2014-09-18
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.util.Collection;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.person.ai.task.Teach;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;

/**
 * Meta task for the Teach task.
 */
public class TeachMeta implements MetaTask {
    
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

        // Find potential students.
        Collection<Person> potentialStudents = Teach.getBestStudents(person);
        if (potentialStudents.size() > 0) {
            result = 50D;

            // If teacher is in a settlement, use crowding modifier.
            if (person.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {
                Person student = (Person) potentialStudents.toArray()[0];
                Building building = BuildingManager.getBuilding(student);
                if (building != null) {
                    result *= TaskProbabilityUtil.getCrowdingProbabilityModifier(person,
                            building);
                    result *= TaskProbabilityUtil.getRelationshipModifier(person, building);
                } 
                else {
                    result = 0D;
                }
            }
        }

        return result;
    }
}
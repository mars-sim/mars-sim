/**
 * Mars Simulation Project
 * AnxietyMedication.java
 * @version 3.1.0 2016-10-20
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.health;

import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;

import java.io.Serializable;

/**
 * A medication that reduces stress.
 */
public class AnxietyMedication extends Medication implements Serializable {

	private static final long serialVersionUID = 1L;

	// The name of the medication.
    public static final String NAME = "Anxiety Medication";
    
    // Stress reduction amount.
    private static final double STRESS_REDUCTION = -1.0D;
    
    // Duration (millisols).
    private static final double DURATION = 200D;
    
    private PhysicalCondition condition;
    
    /**
     * Constructor
     * @param person the person taking the medication.
     */
    public AnxietyMedication(Person person) {
        // Use Medication constructor.
        super(NAME, DURATION, person);
        
        condition = getPerson().getPhysicalCondition();
    }
    
    @Override
    public void timePassing(double time) {
        super.timePassing(time);
        
        // Reduce person's stress.
        condition.setStress(condition.getStress() + (STRESS_REDUCTION * time));
    }
}
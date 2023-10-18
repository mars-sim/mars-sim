/**
 * Mars Simulation Project
 * AnxietyMedication.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package com.mars_sim.core.person.health;

import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.PhysicalCondition;
import com.mars_sim.core.time.ClockPulse;


/**
 * A medication that reduces stress.
 */
public class AnxietyMedication extends Medication {

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
    public boolean timePassing(ClockPulse pulse) {
        super.timePassing(pulse);
        
        // Reduce person's stress.
        condition.reduceStress(STRESS_REDUCTION * pulse.getElapsed());
        
        return true;
    }
}

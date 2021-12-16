/**
 * Mars Simulation Project
 * AnxietyMedication.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.health;

import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.time.ClockPulse;

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
    public boolean timePassing(ClockPulse pulse) {
        super.timePassing(pulse);
        
        // Reduce person's stress.
        condition.addStress(STRESS_REDUCTION * pulse.getElapsed());
        
        return true;
    }
}

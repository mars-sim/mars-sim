/*
 * Mars Simulation Project
 * RadioProtectiveAgent.java
 * @date 2022-11-01
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.health;

import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.time.ClockPulse;
import org.mars_sim.msp.core.tool.RandomUtil;

/**
 * A medication that relieves radiation exposure sickness.
 */
public class RadioProtectiveAgent extends Medication {

	private static final long serialVersionUID = 1L;

    // The name of the medication.
    public static final String NAME = "Radioprotective Agent";
    		
    // Stress reduction amount.
    private static final double REDUCTION = .0005D;
    
    // Duration (millisols).
    private static final double DURATION = 200D;
    
    // Note: Research which medication is more effective under what situation
    // private static final String[] AGENTS = new String[] {"Amifostine", "Melatonin", "Genistein"};
    
    private RadiationExposure exposure;
    
    /**
     * Constructor
     * @param person the person taking the medication.
     */
    public RadioProtectiveAgent(Person person) {
        // Use Medication constructor.
        super(NAME, DURATION, person);
        
        exposure = getPerson().getPhysicalCondition().getRadiationExposure();
    }
    
    @Override
    public boolean timePassing(ClockPulse pulse) {
        super.timePassing(pulse);
        
        int region = RandomUtil.getRandomInt(2);
        exposure.reduceDose(BodyRegionType.values()[region], pulse.getElapsed() * REDUCTION);
        return true;
    }
}

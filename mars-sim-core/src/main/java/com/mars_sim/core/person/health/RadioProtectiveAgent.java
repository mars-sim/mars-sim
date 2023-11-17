/*
 * Mars Simulation Project
 * RadioProtectiveAgent.java
 * @date 2022-11-01
 * @author Manny Kung
 */
package com.mars_sim.core.person.health;

import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.health.RadiationExposure.DoseHistory;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.tools.util.RandomUtil;

/**
 * A medication that relieves radiation exposure sickness.
 */
public class RadioProtectiveAgent extends Medication {

	private static final long serialVersionUID = 1L;

    // The name of the medication.
    public static final String NAME = "Radioprotective Agent";
    		
    // Stress reduction amount.
    private static final double REDUCTION = .01D;
    
    // Duration (millisols).
    private static final double DURATION = 3000D;
    
    // Note: Research which medication is more effective under what situation
    // private static final String[] AGENTS = new String[] {"Amifostine", "Melatonin", "Genistein"};
    
    private RadiationExposure exposure;
    
    private ComplaintType complaintType = ComplaintType.RADIATION_SICKNESS;
    
    /**
     * Constructor.
     * 
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
        
        // Find an region
        BodyRegionType region = getExposedRegion();
        
        double doseNeutrailized = pulse.getElapsed() * REDUCTION;
        double rand = RandomUtil.getRandomDouble(doseNeutrailized / 1.5, doseNeutrailized * 1.5);
        
        // Reduce the dose with medication
        exposure.reduceDose(region, 0, rand);
        exposure.reduceDose(region, 1, rand / 4);
        exposure.reduceDose(region, 2, rand / 16);
        return true;
    }
    
	/*
	 * Gets the body region that has exposure exceeding the limit.
	 */
	private BodyRegionType getExposedRegion() {

		// Compare if any element in a person's cumulativeDoses matrix exceeds the limit
		for (BodyRegionType type : BodyRegionType.values()) {
			
			DoseHistory active = exposure.getDose()[type.ordinal()];
			DoseHistory limit = exposure.getDoseLimits()[type.ordinal()];
			
			if (active.thirtyDayHigherThan(limit)) {
				return type;
			}
			
			if (active.annualHigherThan(limit)) {
				return type;
			}

			if (active.careerHigherThan(limit)) {
				return type;
			}
		}
		return null;
	}
	
	public ComplaintType getComplaintType() {
		return complaintType;
	}
}

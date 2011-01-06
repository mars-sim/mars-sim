/**
 * Mars Simulation Project
 * Meteorologist.java
 * @version 3.00 2010-08-10
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.job;

import org.mars_sim.msp.core.person.NaturalAttributeManager;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.Skill;
import org.mars_sim.msp.core.person.ai.mission.BuildingConstructionMission;
import org.mars_sim.msp.core.person.ai.mission.BuildingSalvageMission;
import org.mars_sim.msp.core.person.ai.mission.RescueSalvageVehicle;
import org.mars_sim.msp.core.person.ai.mission.TravelToSettlement;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.Research;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

/** 
 * The Meteorologist class represents a job for a meteorologist.
 */
public class Meteorologist extends Job implements Serializable {

    private static String CLASS_NAME = "org.mars_sim.msp.simulation.person.ai.job.Meteorologist";
    
    private static Logger logger = Logger.getLogger(CLASS_NAME);

    /**
     * Constructor
     */
    public Meteorologist() {
        // Use Job constructor
        super("Meteorologist");
        
        // Add meteorologist-related tasks.
        
        // Add meteorologist-related missions.
        jobMissionStarts.add(TravelToSettlement.class);
        jobMissionJoins.add(TravelToSettlement.class);  
        jobMissionStarts.add(RescueSalvageVehicle.class);
        jobMissionJoins.add(RescueSalvageVehicle.class);
        jobMissionJoins.add(BuildingConstructionMission.class);
        jobMissionJoins.add(BuildingSalvageMission.class);
    }
    
    @Override
    public double getCapability(Person person) {
        double result = 0D;
        
        int meteorologySkill = person.getMind().getSkillManager().getSkillLevel(Skill.METEOROLOGY);
        result = meteorologySkill;
        
        NaturalAttributeManager attributes = person.getNaturalAttributeManager();
        int academicAptitude = attributes.getAttribute(NaturalAttributeManager.ACADEMIC_APTITUDE);
        result+= result * ((academicAptitude - 50D) / 100D);
        
        if (person.getPhysicalCondition().hasSeriousMedicalProblems()) result = 0D;
        
        return result;
    }

    @Override
    public double getSettlementNeed(Settlement settlement) {
        double result = 0D;
        
        // Add (labspace * tech level / 2) for all labs with meteorology specialities.
        List<Building> laboratoryBuildings = settlement.getBuildingManager().getBuildings(Research.NAME);
        Iterator<Building> i = laboratoryBuildings.iterator();
        while (i.hasNext()) {
            Building building = i.next();
//            try {
                Research lab = (Research) building.getFunction(Research.NAME);
                if (lab.hasSpeciality(Skill.METEOROLOGY)) 
                    result += (lab.getLaboratorySize() * lab.getTechnologyLevel() / 2D);
//            }
//            catch (BuildingException e) {
//                logger.log(Level.SEVERE,"Issues in getSettlementNeeded", e);
//            }
        }

        result *= 5D;
        
        return result;  
    }
}
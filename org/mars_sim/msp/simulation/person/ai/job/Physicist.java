/**
 * Mars Simulation Project
 * Physicist.java
 * @version 2.87 2009-06-18
 * @author Scott Davis
 */
package org.mars_sim.msp.simulation.person.ai.job;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.simulation.person.NaturalAttributeManager;
import org.mars_sim.msp.simulation.person.Person;
import org.mars_sim.msp.simulation.person.ai.Skill;
import org.mars_sim.msp.simulation.person.ai.mission.BuildingConstructionMission;
import org.mars_sim.msp.simulation.person.ai.mission.RescueSalvageVehicle;
import org.mars_sim.msp.simulation.person.ai.mission.TravelToSettlement;
import org.mars_sim.msp.simulation.person.ai.task.ResearchPhysics;
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.simulation.structure.building.Building;
import org.mars_sim.msp.simulation.structure.building.BuildingException;
import org.mars_sim.msp.simulation.structure.building.function.Research;

/** 
 * The Physicist class represents a job for a physicist.
 */
public class Physicist extends Job implements Serializable {

    private static String CLASS_NAME = "org.mars_sim.msp.simulation.person.ai.job.Physicist";
    
    private static Logger logger = Logger.getLogger(CLASS_NAME);

    /**
     * Constructor
     */
    public Physicist() {
        // Use Job constructor
        super("Physicist");
        
        // Add physicist-related tasks.
        jobTasks.add(ResearchPhysics.class);
        
        // Add physicist-related missions.
        jobMissionStarts.add(TravelToSettlement.class);
        jobMissionJoins.add(TravelToSettlement.class);  
        jobMissionStarts.add(RescueSalvageVehicle.class);
        jobMissionJoins.add(RescueSalvageVehicle.class);
        jobMissionJoins.add(BuildingConstructionMission.class);
    }
    
    @Override
    public double getCapability(Person person) {
        double result = 0D;
        
        int physicsSkill = person.getMind().getSkillManager().getSkillLevel(Skill.PHYSICS);
        result = physicsSkill;
        
        NaturalAttributeManager attributes = person.getNaturalAttributeManager();
        int academicAptitude = attributes.getAttribute(NaturalAttributeManager.ACADEMIC_APTITUDE);
        result+= result * ((academicAptitude - 50D) / 100D);
        
        if (person.getPhysicalCondition().hasSeriousMedicalProblems()) result = 0D;
        
        return result;
    }

    @Override
    public double getSettlementNeed(Settlement settlement) {
        double result = 0D;
        
        // Add (labspace * tech level) for all labs with physics specialities.
        List laboratoryBuildings = settlement.getBuildingManager().getBuildings(Research.NAME);
        Iterator i = laboratoryBuildings.iterator();
        while (i.hasNext()) {
            Building building = (Building) i.next();
            try {
                Research lab = (Research) building.getFunction(Research.NAME);
                if (lab.hasSpeciality(Skill.PHYSICS)) 
                    result += (lab.getLaboratorySize() * lab.getTechnologyLevel());
            }
            catch (BuildingException e) {
                logger.log(Level.SEVERE,"Issues in getSettlementNeeded", e);
            }
        }

        return result;  
    }
}
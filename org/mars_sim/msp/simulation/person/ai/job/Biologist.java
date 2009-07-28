/**
 * Mars Simulation Project
 * Biologist.java
 * @version 2.87 2009-06-18
 * @author Scott Davis
 */
package org.mars_sim.msp.simulation.person.ai.job;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.simulation.Simulation;
import org.mars_sim.msp.simulation.person.NaturalAttributeManager;
import org.mars_sim.msp.simulation.person.Person;
import org.mars_sim.msp.simulation.person.ai.Skill;
import org.mars_sim.msp.simulation.person.ai.mission.BuildingConstructionMission;
import org.mars_sim.msp.simulation.person.ai.mission.Exploration;
import org.mars_sim.msp.simulation.person.ai.mission.Mission;
import org.mars_sim.msp.simulation.person.ai.mission.MissionManager;
import org.mars_sim.msp.simulation.person.ai.mission.RescueSalvageVehicle;
import org.mars_sim.msp.simulation.person.ai.mission.RoverMission;
import org.mars_sim.msp.simulation.person.ai.mission.TravelToSettlement;
import org.mars_sim.msp.simulation.person.ai.task.ResearchBiology;
import org.mars_sim.msp.simulation.resource.AmountResource;
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.simulation.structure.building.Building;
import org.mars_sim.msp.simulation.structure.building.BuildingException;
import org.mars_sim.msp.simulation.structure.building.function.Research;
import org.mars_sim.msp.simulation.vehicle.Rover;
import org.mars_sim.msp.simulation.vehicle.Vehicle;

/** 
 * The Biologist class represents a job for a biologist.
 */
public class Biologist extends Job implements Serializable {

    private static String CLASS_NAME = "org.mars_sim.msp.simulation.person.ai.job.Biologist";
    
    private static Logger logger = Logger.getLogger(CLASS_NAME);

    /**
     * Constructor
     */
    public Biologist() {
        // Use Job constructor
        super("Biologist");
        
        // Add biologist-related tasks.
        jobTasks.add(ResearchBiology.class);
        
        // Add biologist-related missions.
        jobMissionJoins.add(Exploration.class);
        jobMissionStarts.add(TravelToSettlement.class);
        jobMissionJoins.add(TravelToSettlement.class);  
        jobMissionStarts.add(RescueSalvageVehicle.class);
        jobMissionJoins.add(RescueSalvageVehicle.class);
        jobMissionJoins.add(BuildingConstructionMission.class);
    }
    
    @Override
    public double getCapability(Person person) {
        double result = 0D;
        
        int biologySkill = person.getMind().getSkillManager().getSkillLevel(Skill.BIOLOGY);
        result = biologySkill;
        
        NaturalAttributeManager attributes = person.getNaturalAttributeManager();
        int academicAptitude = attributes.getAttribute(NaturalAttributeManager.ACADEMIC_APTITUDE);
        result+= result * ((academicAptitude - 50D) / 100D);
        
        if (person.getPhysicalCondition().hasSeriousMedicalProblems()) result = 0D;
        
        return result;
    }

    @Override
    public double getSettlementNeed(Settlement settlement) {
        double result = 0D;
        
        // Add (labspace * tech level) for all labs with biology specialities.
        List laboratoryBuildings = settlement.getBuildingManager().getBuildings(Research.NAME);
        Iterator i = laboratoryBuildings.iterator();
        while (i.hasNext()) {
            Building building = (Building) i.next();
            try {
                Research lab = (Research) building.getFunction(Research.NAME);
                if (lab.hasSpeciality(Skill.BIOLOGY)) 
                    result += (lab.getLaboratorySize() * lab.getTechnologyLevel());
            }
            catch (BuildingException e) {
                logger.log(Level.SEVERE,"Issues in getSettlementNeeded", e);
            }
        }
        /*
        // Add number of exploration-capable rovers parked at the settlement.
        Iterator<Vehicle> j = settlement.getParkedVehicles().iterator();
        while (j.hasNext()) {
            Vehicle vehicle = j.next();
            if (vehicle instanceof Rover) {
                try {
                    if (vehicle.getInventory().hasAmountResourceCapacity(
                            AmountResource.findAmountResource("rock samples"))) result++;
                }
                catch (Exception e) {
                    logger.log(Level.SEVERE,"Issues in getSettlementNeeded", e);
                }
            }
        }
        */
        /*
        // Add number of exploration-capable rovers out on missions for the settlement.
        MissionManager missionManager = Simulation.instance().getMissionManager();
        Iterator k = missionManager.getMissionsForSettlement(settlement).iterator();
        while (k.hasNext()) {
            Mission mission = (Mission) k.next();
            if (mission instanceof RoverMission) {
                Rover rover = ((RoverMission) mission).getRover();
                try {
                    if ((rover != null) && rover.getInventory().hasAmountResourceCapacity(
                            AmountResource.findAmountResource("rock samples"))) result++;
                }
                catch (Exception e) {
                    logger.log(Level.SEVERE,"Issues in getSettlementNeeded", e);
                }
            }
        }
        */
        result *= 5D;
        
        return result;  
    }
}
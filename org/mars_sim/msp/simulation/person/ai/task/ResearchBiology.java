/**
 * Mars Simulation Project
 * ResearchBiology.java
 * @version 2.87 2009-06-13
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.person.ai.task;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.simulation.Inventory;
import org.mars_sim.msp.simulation.Lab;
import org.mars_sim.msp.simulation.RandomUtil;
import org.mars_sim.msp.simulation.Unit;
import org.mars_sim.msp.simulation.person.Person;
import org.mars_sim.msp.simulation.person.ai.Skill;
import org.mars_sim.msp.simulation.person.ai.job.Job;
import org.mars_sim.msp.simulation.person.ai.mission.Exploration;
import org.mars_sim.msp.simulation.person.ai.mission.Mission;
import org.mars_sim.msp.simulation.resource.AmountResource;
import org.mars_sim.msp.simulation.structure.building.Building;
import org.mars_sim.msp.simulation.structure.building.BuildingException;
import org.mars_sim.msp.simulation.structure.building.function.Research;

/** 
 * The ResearchBiology class is a task for researching biological science.
 */
public class ResearchBiology extends ResearchScience implements Serializable {

    private static String CLASS_NAME = "org.mars_sim.msp.simulation.person.ai.task.ResearchBiology";
    
    private static Logger logger = Logger.getLogger(CLASS_NAME);

    // Does the researcher have a rock sample to study?
    private boolean hasRockSample;
    
    /** 
     * Constructor
     * This is an effort driven task.
     * @param person the person to perform the task
     * @throws Exception if error constructing task.
     */
    public ResearchBiology(Person person) throws Exception {
        super(Skill.BIOLOGY, person);
        
        // Check if researcher has a rock sample to study.
        Unit container = person.getContainerUnit();
        if (container != null) {
            AmountResource rockSamples = AmountResource.findAmountResource("rock samples");
            Inventory inv = container.getInventory();
            double totalRockSampleMass = inv.getAmountResourceStored(rockSamples);
            if (totalRockSampleMass > 0D) {
                hasRockSample = true;
                double rockSampleMass = RandomUtil.getRandomDouble(ExploreSite.AVERAGE_ROCK_SAMPLE_MASS * 2D);
                if (rockSampleMass > totalRockSampleMass) rockSampleMass = totalRockSampleMass;
                inv.retrieveAmountResource(rockSamples, rockSampleMass);
            }
        }
    }
    
    /** 
     * Returns the weighted probability that a person might perform this task.
     * @param person the person to perform the task
     * @return the weighted probability that a person might perform this task
     */
    public static double getProbability(Person person) {
        double result = 0D;

        try {
            Lab lab = getLocalLab(person, Skill.BIOLOGY);
            if (lab != null) {
                result = 25D; 
        
                // Check for crowding modifier.
                if (person.getLocationSituation().equals(Person.INSETTLEMENT)) {
                    try {
                        Building labBuilding = ((Research) lab).getBuilding();  
                        if (labBuilding != null) {
                            result *= Task.getCrowdingProbabilityModifier(person, labBuilding);     
                            result *= Task.getRelationshipModifier(person, labBuilding);
                        }
                        else result = 0D;       
                    }
                    catch (BuildingException e) {
                        logger.log(Level.SEVERE,"ResearchBiology.getProbability(): " + e.getMessage());
                    }
                }
            }
            
            // Check if rock samples are available.
            Unit container = person.getContainerUnit();
            if (container != null) {
                Inventory inv = container.getInventory();
                AmountResource rockSamples = AmountResource.findAmountResource("rock samples");
                if (inv.getAmountResourceStored(rockSamples) > 0D) result *= 10D;
            }
            
            // Check if on exploration mission.
            Mission mission = person.getMind().getMission();
            if ((mission != null) && (mission instanceof Exploration)) result *= 10D;
        }
        catch (Exception e) {
            e.printStackTrace(System.err);
        }
        
        // Effort-driven task modifier.
        result *= person.getPerformanceRating();
        
        // Job modifier.
        Job job = person.getMind().getJob();
        if (job != null) result *= job.getStartTaskProbabilityModifier(ResearchBiology.class);       

        return result;
    }
    
    @Override
    protected double researchingPhase(double time) throws Exception {
        
        double remainingTime = super.researchingPhase(time);
        
        // Study rock samples if they are available.
        // Rock sample study double experience and increase chance of 
        // mineral concentration estimation improvement.
        if (hasRockSample) {
            addExperience(time);
        }
        
        return remainingTime;
    }
}
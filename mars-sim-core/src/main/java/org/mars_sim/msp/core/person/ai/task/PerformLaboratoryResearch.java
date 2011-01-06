/**
 * Mars Simulation Project
 * PerformLaboratoryResearch.java
 * @version 3.00 2010-08-10
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import org.mars_sim.msp.core.Lab;
import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.malfunction.MalfunctionManager;
import org.mars_sim.msp.core.malfunction.Malfunctionable;
import org.mars_sim.msp.core.person.NaturalAttributeManager;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillManager;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.science.Science;
import org.mars_sim.msp.core.science.ScienceUtil;
import org.mars_sim.msp.core.science.ScientificStudy;
import org.mars_sim.msp.core.science.ScientificStudyManager;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.Research;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.core.vehicle.Vehicle;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A task for performing research for a scientific study in a laboratory.
 */
public class PerformLaboratoryResearch extends Task implements 
        ResearchScientificStudy, Serializable {

    private static String CLASS_NAME = "org.mars_sim.msp.simulation.person.ai." + 
            "task.PerformLaboratoryResearch";
    private static Logger logger = Logger.getLogger(CLASS_NAME);
    
    // The stress modified per millisol.
    private static final double STRESS_MODIFIER = -.1D; 
    
    // Task phase.
    private static final String RESEARCHING = "Researching";
    
    // Data members.
    private ScientificStudy study; // The scientific study the person is researching for.
    private Lab lab;         // The laboratory the person is working in.
    private Science science;  // The science that is being researched.
    private MalfunctionManager malfunctions; // The lab's associated malfunction manager.
    private Person researchAssistant; // The research assistant.
    
    /**
     * Constructor
     * @param person the person performing the task.
     * @throws Exception if error constructing the class.
     */
    public PerformLaboratoryResearch(Person person) {
        // Use task constructor.
        super("Perform Laboratory Research", person, true, false, STRESS_MODIFIER, 
                true, RandomUtil.getRandomDouble(100D));
        
        researchAssistant = null;
        
        // Determine study.
        study = determineStudy();
        if (study != null) {
            science = getScience(person, study);
            if (science != null) {
                setDescription("Perform " + science.getName().toLowerCase() + 
                        " laboratory research");
                lab = getLocalLab(person, science);
                if (lab != null) {
                    addPersonToLab();
                }
                else {
                    logger.info("lab could not be determined.");
                    endTask();
                }
            }
            else {
                logger.info("science could not be determined");
                endTask();
            }
        }
        else {
            logger.info("study could not be determined");
            endTask();
        }
        
        // Initialize phase
        addPhase(RESEARCHING);
        setPhase(RESEARCHING);
    }
    
    /** 
     * Returns the weighted probability that a person might perform this task.
     * @param person the person to perform the task
     * @return the weighted probability that a person might perform this task
     */
    public static double getProbability(Person person) {
        double result = 0D;
        
        // Add probability for researcher's primary study (if any).
        ScientificStudyManager studyManager = Simulation.instance().getScientificStudyManager();
        ScientificStudy primaryStudy = studyManager.getOngoingPrimaryStudy(person);
        if ((primaryStudy != null) && ScientificStudy.RESEARCH_PHASE.equals(primaryStudy.getPhase())) {
            if (!primaryStudy.isPrimaryResearchCompleted()) {
                try {
                    Lab lab = getLocalLab(person, primaryStudy.getScience());
                    if (lab != null) {
                        double primaryResult = 100D;
                    
                        // Get lab building crowding modifier.
                        primaryResult *= getLabCrowdingModifier(person, lab);
                    
                        // If researcher's current job isn't related to study science, divide by two.
                        Job job = person.getMind().getJob();
                        if (job != null) {
                            Science jobScience = ScienceUtil.getAssociatedScience(job);
                            if (!primaryStudy.getScience().equals(jobScience)) primaryResult /= 2D;
                        }
                    
                        result += primaryResult;
                    }
                }
                catch (Exception e) {
                    logger.severe("getProbability(): " + e.getMessage());
                }
            }
        }
        
        // Add probability for each study researcher is collaborating on.
        Iterator<ScientificStudy> i = studyManager.getOngoingCollaborativeStudies(person).iterator();
        while (i.hasNext()) {
            ScientificStudy collabStudy = i.next();
            if (ScientificStudy.RESEARCH_PHASE.equals(collabStudy.getPhase())) {
                if (!collabStudy.isCollaborativeResearchCompleted(person)) {
                    try {
                        Science collabScience = collabStudy.getCollaborativeResearchers().get(person);
                    
                        Lab lab = getLocalLab(person, collabScience);
                        if (lab != null) {
                            double collabResult = 50D;
                        
                            // Get lab building crowding modifier.
                            collabResult *= getLabCrowdingModifier(person, lab);
                        
                            // If researcher's current job isn't related to study science, divide by two.
                            Job job = person.getMind().getJob();
                            if (job != null) {
                                Science jobScience = ScienceUtil.getAssociatedScience(job);
                                if (!collabScience.equals(jobScience)) collabResult /= 2D;
                            }
                        
                            result += collabResult;
                        }
                    }
                    catch (Exception e) {
                        logger.severe("getProbability(): " + e.getMessage());
                    }
                }
            }
        }
        
        // Effort-driven task modifier.
        result *= person.getPerformanceRating();
        
        return result;
    }
    
    /**
     * Gets the crowding modifier for a researcher to use a given laboratory building.
     * @param researcher the researcher.
     * @param lab the laboratory.
     * @return crowding modifier.
     * @throws BuildingException if error determining lab building.
     */
    private static double getLabCrowdingModifier(Person researcher, Lab lab) 
            {
        double result = 1D;
        if (researcher.getLocationSituation().equals(Person.INSETTLEMENT)) {
            Building labBuilding = ((Research) lab).getBuilding();  
            if (labBuilding != null) {
                result *= Task.getCrowdingProbabilityModifier(researcher, labBuilding);     
                result *= Task.getRelationshipModifier(researcher, labBuilding);
            }
        }
        return result;
    }
    
    /**
     * Determines the scientific study that will be researched.
     * @return study or null if none available.
     */
    private ScientificStudy determineStudy() {
        ScientificStudy result = null;
        
        List<ScientificStudy> possibleStudies = new ArrayList<ScientificStudy>();
        
        // Add primary study if in research phase.
        ScientificStudyManager manager = Simulation.instance().getScientificStudyManager();
        ScientificStudy primaryStudy = manager.getOngoingPrimaryStudy(person);
        if (primaryStudy != null) {
            if (ScientificStudy.RESEARCH_PHASE.equals(primaryStudy.getPhase()) && 
                    !primaryStudy.isPrimaryResearchCompleted()) {
                // Primary study added twice to double chance of random selection.
                possibleStudies.add(primaryStudy);
                possibleStudies.add(primaryStudy);
            }
        }
        
        // Add all collaborative studies in research phase.
        Iterator<ScientificStudy> i = manager.getOngoingCollaborativeStudies(person).iterator();
        while (i.hasNext()) {
            ScientificStudy collabStudy = i.next();
            if (ScientificStudy.RESEARCH_PHASE.equals(collabStudy.getPhase()) && 
                    !collabStudy.isCollaborativeResearchCompleted(person)) 
                possibleStudies.add(collabStudy);
        }
        
        // Randomly select study.
        if (possibleStudies.size() > 0) {
            int selected = RandomUtil.getRandomInt(possibleStudies.size() - 1);
            result = possibleStudies.get(selected);
        }
        
        return result;
    }
    
    /**
     * Gets the field of science that the researcher is involved with in a study.
     * @param researcher the researcher.
     * @param study the scientific study.
     * @return the field of science or null if researcher is not involved with study.
     */
    private static Science getScience(Person researcher, ScientificStudy study) {
        Science result = null;
        
        if (study.getPrimaryResearcher().equals(researcher)) {
            result = study.getScience();
        }
        else if (study.getCollaborativeResearchers().containsKey(researcher)) {
            result = study.getCollaborativeResearchers().get(researcher);
        }
        
        return result;
    }
    
    /**
     * Gets a local lab for scientific research.
     * @param person the person checking for the lab.
     * @param science the science to research.
     * @return laboratory found or null if none.
     * @throws Exception if error getting a lab.
     */
    private static Lab getLocalLab(Person person, Science science) {
        Lab result = null;
        
        String location = person.getLocationSituation();
        if (location.equals(Person.INSETTLEMENT)) result = getSettlementLab(person, science);
        else if (location.equals(Person.INVEHICLE)) result = getVehicleLab(person.getVehicle(), science);
        
        return result;
    }
    
    /**
     * Gets a settlement lab to research a particular science.
     * @param person the person looking for a lab.
     * @param science the science to research.
     * @return a valid research lab.
     */
    private static Lab getSettlementLab(Person person, Science science) {
        Lab result = null;
        
//        try {
            BuildingManager manager = person.getSettlement().getBuildingManager();
            List<Building> labBuildings = manager.getBuildings(Research.NAME);
            labBuildings = getSettlementLabsWithSpeciality(science, labBuildings);
            labBuildings = BuildingManager.getNonMalfunctioningBuildings(labBuildings);
            labBuildings = getSettlementLabsWithAvailableSpace(labBuildings);
            labBuildings = BuildingManager.getLeastCrowdedBuildings(labBuildings);
            labBuildings = BuildingManager.getBestRelationshipBuildings(person, labBuildings);
        
            if (labBuildings.size() > 0) {
                Building building = labBuildings.get(0);
                result = (Research) building.getFunction(Research.NAME);
            }
//        }
//        catch (BuildingException e) {
//            logger.log(Level.SEVERE,"getSettlementLab(): " + e.getMessage());
//        }
        
        return result;
    }
    
    /**
     * Gets a list of research buildings with available research space from a list of buildings 
     * with the research function.
     * @param buildingList list of buildings with research function.
     * @return research buildings with available lab space.
     * @throws BuildingException if building list contains buildings without research function.
     */
    private static List<Building> getSettlementLabsWithAvailableSpace(List<Building> buildingList) 
            {
        List<Building> result = new ArrayList<Building>();
        
        Iterator<Building> i = buildingList.iterator();
        while (i.hasNext()) {
            Building building = i.next();
            Research lab = (Research) building.getFunction(Research.NAME);
            if (lab.getResearcherNum() < lab.getLaboratorySize()) result.add(building);
        }
        
        return result;
    }
    
    /**
     * Gets a list of research buildings with a given science speciality from a list of 
     * buildings with the research function.
     * @param science the science speciality.
     * @param buildingList list of buildings with research function.
     * @return research buildings with science speciality.
     * @throws BuildingException if building list contains buildings without research function.
     */
    private static List<Building> getSettlementLabsWithSpeciality(Science science, List<Building> buildingList) 
            {
        List<Building> result = new ArrayList<Building>();
        
        Iterator<Building> i = buildingList.iterator();
        while (i.hasNext()) {
            Building building = i.next();
            Research lab = (Research) building.getFunction(Research.NAME);
            if (lab.hasSpeciality(science.getName())) result.add(building);
        }
        
        return result;
    }
    
    /**
     * Gets an available lab in a vehicle.
     * Returns null if no lab is currently available.
     * @param vehicle the vehicle
     * @param science the science to research.
     * @return available lab
     */
    private static Lab getVehicleLab(Vehicle vehicle, Science science) {
        
        Lab result = null;
        
        if (vehicle instanceof Rover) {
            Rover rover = (Rover) vehicle;
            if (rover.hasLab()) {
                Lab lab = rover.getLab();
                boolean availableSpace = (lab.getResearcherNum() < lab.getLaboratorySize());
                boolean speciality = lab.hasSpeciality(science.getName());
                boolean malfunction = (rover.getMalfunctionManager().hasMalfunction());
                if (availableSpace && speciality && !malfunction) result = lab;
            }
        }
        
        return result;
    }
    
    /**
     * Adds a person to a lab.
     */
    private void addPersonToLab() {
        
        try {
            String location = person.getLocationSituation();
            if (location.equals(Person.INSETTLEMENT)) {
                Building labBuilding = ((Research) lab).getBuilding();
                BuildingManager.addPersonToBuilding(person, labBuilding);
                lab.addResearcher();
                malfunctions = labBuilding.getMalfunctionManager();
            }
            else if (location.equals(Person.INVEHICLE)) {
                lab.addResearcher();
                malfunctions = person.getVehicle().getMalfunctionManager();
            }
        }
        catch (Exception e) {
            logger.log(Level.SEVERE,"addPersonToLab(): " + e.getMessage());
        }
    }
    
    @Override
    protected void addExperience(double time) {
        // Add experience to relevant science skill
        // (1 base experience point per 25 millisols of research time)
        // Experience points adjusted by person's "Academic Aptitude" attribute.
        double newPoints = time / 25D;
        int academicAptitude = person.getNaturalAttributeManager().getAttribute(
            NaturalAttributeManager.ACADEMIC_APTITUDE);
        newPoints += newPoints * ((double) academicAptitude - 50D) / 100D;
        newPoints *= getTeachingExperienceModifier();
        String scienceSkill = ScienceUtil.getAssociatedSkill(science);
        person.getMind().getSkillManager().addExperience(scienceSkill, newPoints);
    }
    
    /**
     * Gets the effective research time based on the person's science skill.
     * @param time the real amount of time (millisol) for research.
     * @return the effective amount of time (millisol) for research.
     */
    private double getEffectiveResearchTime(double time) {
        // Determine effective research time based on the science skill.
        double researchTime = time;
        int scienceSkill = getEffectiveSkillLevel();
        if (scienceSkill == 0) researchTime /= 2D;
        if (scienceSkill > 1) researchTime += researchTime * (.2D * scienceSkill);
        
        // Modify by tech level of laboratory.
        int techLevel = lab.getTechnologyLevel();
        if (techLevel > 0) researchTime *= techLevel;
        
        // If research assistant, modify by assistant's effective skill.
        if (hasResearchAssistant()) {
            SkillManager manager = researchAssistant.getMind().getSkillManager();
            int assistantSkill = manager.getEffectiveSkillLevel(ScienceUtil.getAssociatedSkill(science));
            if (scienceSkill > 0) researchTime *= 1D + ((double) assistantSkill / (double) scienceSkill);
        }
        
        return researchTime;
    }

    @Override
    public List<String> getAssociatedSkills() {
        List<String> results = new ArrayList<String>(1);
        String scienceSkill = ScienceUtil.getAssociatedSkill(science);
        results.add(scienceSkill);
        return results;
    }

    @Override
    public int getEffectiveSkillLevel() {
        String scienceSkill = ScienceUtil.getAssociatedSkill(science);
        SkillManager manager = person.getMind().getSkillManager();
        return manager.getEffectiveSkillLevel(scienceSkill);
    }

    @Override
    protected double performMappedPhase(double time) {
        if (getPhase() == null) throw new IllegalArgumentException("Task phase is null");
        if (RESEARCHING.equals(getPhase())) return researchingPhase(time);
        else return time;
    }
    
    /**
     * Performs the researching phase.
     * @param time the amount of time (millisols) to perform the phase.
     * @return the amount of time (millisols) left over after performing the phase.
     * @throws Exception if error performing the phase.
     */
    protected double researchingPhase(double time) {
        
        // If person is incapacitated, end task.
        if (person.getPerformanceRating() == 0D) endTask();
        
        // Check for laboratory malfunction.
        if (malfunctions.hasMalfunction()) endTask();
        
        // Check if research in study is completed.
        boolean isPrimary = study.getPrimaryResearcher().equals(person);
        if (isPrimary) {
            if (study.isPrimaryResearchCompleted()) endTask();
        }
        else {
            if (study.isCollaborativeResearchCompleted(person)) endTask();
        }
        
        if (isDone()) return time;
        
        // Add research work time to study.
        double researchTime = getEffectiveResearchTime(time);
        if (isPrimary) study.addPrimaryResearchWorkTime(researchTime);
        else study.addCollaborativeResearchWorkTime(person, researchTime);
        
        // Add experience
        addExperience(time);
        
        // Check for lab accident.
        checkForAccident(time);
        
        return 0D;
    }
    
    /**
     * Check for accident in laboratory.
     * @param time the amount of time researching (in millisols)
     */
    private void checkForAccident(double time) {

        double chance = .001D;

        // Science skill modification.
        String scienceSkill = ScienceUtil.getAssociatedSkill(science);
        int skill = person.getMind().getSkillManager().getEffectiveSkillLevel(scienceSkill);
        if (skill <= 3) chance *= (4 - skill);
        else chance /= (skill - 2);

        Malfunctionable entity = null;
        if (lab instanceof Research) entity = ((Research) lab).getBuilding();
        else entity = person.getVehicle();
        
        if (entity != null) {
         
            // Modify based on the entity's wear condition.
            chance *= entity.getMalfunctionManager().getWearConditionAccidentModifier();
            
            if (RandomUtil.lessThanRandPercent(chance * time)) {
                logger.info(person.getName() + " has a lab accident while doing " + science + " research.");
                entity.getMalfunctionManager().accident();
            }
        }
    }
    
    /**
     * Ends the task and performs any final actions.
     */
    public void endTask() {
        super.endTask();
        
        // Remove person from lab so others can use it.
        try {
            if (lab != null) lab.removeResearcher();
        }
        catch(Exception e) {}
    }
    
    /**
     * Gets the scientific field that is being researched for the study.
     * @return scientific field.
     */
    public Science getResearchScience() {
        return science;
    }
    
    /**
     * Gets the researcher who is being assisted.
     * @return researcher.
     */
    public Person getResearcher() {
        return person;
    }
    
    /**
     * Checks if there is a research assistant.
     * @return research assistant.
     */
    public boolean hasResearchAssistant() {
        return (researchAssistant != null);
    }
    
    /**
     * Gets the research assistant.
     * @return research assistant or null if none.
     */
    public Person getResearchAssistant() {
        return researchAssistant;
    }
    
    /**
     * Sets the research assistant.
     * @param researchAssistant the research assistant.
     */
    public void setResearchAssistant(Person researchAssistant) {
        this.researchAssistant = researchAssistant;
    }
}
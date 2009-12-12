/**
 * Mars Simulation Project
 * ObserveAstronomicalObjects.java
 * @version 2.87 2009-11-12
 * @author Sebastien Venot
 */
package org.mars_sim.msp.core.person.ai.task;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.mars.SurfaceFeatures;
import org.mars_sim.msp.core.person.NaturalAttributeManager;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillManager;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.science.Science;
import org.mars_sim.msp.core.science.ScienceUtil;
import org.mars_sim.msp.core.science.ScientificStudy;
import org.mars_sim.msp.core.science.ScientificStudyManager;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingException;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.AstronomicalObservation;

/**
 * A task for observing the night sky with an astronomical observatory.
 */
public class ObserveAstronomicalObjects extends Task implements 
        ResearchScientificStudy, Serializable {

    private static String CLASS_NAME = 
            "org.mars_sim.msp.simulation.person.ai.task.ObserveAstronomicalObjects";
    private static Logger logger = Logger.getLogger(CLASS_NAME);

    //  The stress modified per millisol.
    private static final double STRESS_MODIFIER = -.2D; 
    
    // Task phase.
    private static final String OBSERVING = "Observing";
    
    // Data members.
    private ScientificStudy study; // The scientific study the person is researching for.
    private AstronomicalObservation observatory; // The observatory the person is using.
    private Person researchAssistant; // The research assistant.
    
    /**
     * Constructor
     * @param person the person performing the task.
     * @throws Exception if error constructing the task.
     */
    public ObserveAstronomicalObjects(Person person) throws Exception {
        // Use task constructor.
        super("Observe Night Sky with Telescope", person, true, false, STRESS_MODIFIER, 
                true, RandomUtil.getRandomDouble(300D));
        
        // Determine study.
        study = determineStudy();
        if (study != null) {
            // Determine observatory to use.
            observatory = determineObservatory(person);
            if (observatory != null) {
                // Add person to observatory building.
                Building observatoryBuilding = observatory.getBuilding();
                BuildingManager.addPersonToBuilding(person, observatoryBuilding);
                observatory.addObserver();
            }
            else {
                logger.info("observatory could not be determined.");
                endTask();
            }
        }
        
        // Initialize phase
        addPhase(OBSERVING);
        setPhase(OBSERVING);
    }
    
    /** 
     * Returns the weighted probability that a person might perform this task.
     * @param person the person to perform the task
     * @return the weighted probability that a person might perform this task
     */
    public static double getProbability(Person person) {
        double result = 0D;
        
        // Get local observatory if available.
        AstronomicalObservation observatory = determineObservatory(person);
        if (observatory != null) {
            
            // Check if it is completely dark outside.
            SurfaceFeatures surface = Simulation.instance().getMars().getSurfaceFeatures();
            double sunlight = surface.getSurfaceSunlight(person.getCoordinates());
            if (sunlight == 0D) {
                
                Science astronomy = ScienceUtil.getScience(Science.ASTRONOMY);
                
                // Add probability for researcher's primary study (if any).
                ScientificStudyManager studyManager = Simulation.instance().getScientificStudyManager();
                ScientificStudy primaryStudy = studyManager.getOngoingPrimaryStudy(person);
                if ((primaryStudy != null) && ScientificStudy.RESEARCH_PHASE.equals(primaryStudy.getPhase())) {
                    if (!primaryStudy.isPrimaryResearchCompleted()) {
                        if (astronomy.equals(primaryStudy.getScience())) {
                            try {
                                double primaryResult = 200D;
                            
                                // Get observatory building crowding modifier.
                                primaryResult *= getObservatoryCrowdingModifier(person, observatory);
                        
                                // If researcher's current job isn't related to astronomy, divide by two.
                                Job job = person.getMind().getJob();
                                if (job != null) {
                                    Science jobScience = ScienceUtil.getAssociatedScience(job);
                                    if (!astronomy.equals(jobScience)) primaryResult /= 2D;
                                }
                        
                                result += primaryResult;
                            }
                            catch (Exception e) {
                                logger.severe("getProbability(): " + e.getMessage());
                            }
                        }
                    }
                }
                
                // Add probability for each study researcher is collaborating on.
                Iterator<ScientificStudy> i = studyManager.getOngoingCollaborativeStudies(person).iterator();
                while (i.hasNext()) {
                    ScientificStudy collabStudy = i.next();
                    if (ScientificStudy.RESEARCH_PHASE.equals(collabStudy.getPhase())) {
                        if (!collabStudy.isCollaborativeResearchCompleted(person)) {
                            if (astronomy.equals(collabStudy.getCollaborativeResearchers().get(person))) {
                                try {
                                    double collabResult = 100D;
                                
                                    // Get observatory building crowding modifier.
                                    collabResult *= getObservatoryCrowdingModifier(person, observatory);
                                
                                    // If researcher's current job isn't related to astronomy, divide by two.
                                    Job job = person.getMind().getJob();
                                    if (job != null) {
                                        Science jobScience = ScienceUtil.getAssociatedScience(job);
                                        if (!astronomy.equals(jobScience)) collabResult /= 2D;
                                    }
                                
                                    result += collabResult;
                                }
                                catch (Exception e) {
                                    logger.severe("getProbability(): " + e.getMessage());
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Effort-driven task modifier.
        result *= person.getPerformanceRating();
        
        return result;
    }
    
    /**
     * Gets the perferred local astronomical observatory for an observer.
     * @param observer the observer.
     * @return observatory or null if none found.
     */
    private static AstronomicalObservation determineObservatory(Person observer) {
        AstronomicalObservation result = null;
        
        if (Person.INSETTLEMENT.equals(observer.getLocationSituation())) {
            try {
                BuildingManager manager = observer.getSettlement().getBuildingManager();
                List<Building> observatoryBuildings = manager.getBuildings(AstronomicalObservation.NAME);
                observatoryBuildings = BuildingManager.getNonMalfunctioningBuildings(observatoryBuildings);
                observatoryBuildings = getObservatoriesWithAvailableSpace(observatoryBuildings);
                observatoryBuildings = BuildingManager.getLeastCrowdedBuildings(observatoryBuildings);
                observatoryBuildings = BuildingManager.getBestRelationshipBuildings(observer, observatoryBuildings);
            
                if (observatoryBuildings.size() > 0) {
                    Building building = (Building) observatoryBuildings.get(0);
                    result = (AstronomicalObservation) building.getFunction(AstronomicalObservation.NAME);
                }
            }
            catch (BuildingException e) {
                logger.log(Level.SEVERE, "determineObservatory(): " + e.getMessage());
            }
        }
        
        return result;
    }
    
    /**
     * Gets the crowding modifier for an observer to use a given observatory building.
     * @param observer the observer.
     * @param observatory the astronomical observatory.
     * @return crowding modifier.
     * @throws BuildingException if error determining observatory building.
     */
    private static double getObservatoryCrowdingModifier(Person observer, AstronomicalObservation observatory) 
            throws BuildingException {
        double result = 1D;
        if (observer.getLocationSituation().equals(Person.INSETTLEMENT)) {
            Building observatoryBuilding = observatory.getBuilding();  
            if (observatoryBuilding != null) {
                result *= Task.getCrowdingProbabilityModifier(observer, observatoryBuilding);     
                result *= Task.getRelationshipModifier(observer, observatoryBuilding);
            }
        }
        return result;
    }
    
    /**
     * Gets a list of observatory buildings with available research space from a list of observatory buildings.
     * @param buildingList list of buildings with astronomical observation function.
     * @return observatory buildings with available observatory space.
     * @throws BuildingException if building list contains buildings without astronomical observation function.
     */
    private static List<Building> getObservatoriesWithAvailableSpace(List<Building> buildingList) 
            throws BuildingException {
        List<Building> result = new ArrayList<Building>();
        
        Iterator<Building> i = buildingList.iterator();
        while (i.hasNext()) {
            Building building = i.next();
            AstronomicalObservation observatory = (AstronomicalObservation) building.getFunction(
                    AstronomicalObservation.NAME);
            if (observatory.getObserverNum() < observatory.getObservatoryCapacity()) 
                result.add(building);
        }
        
        return result;
    }
    
    /**
     * Determines the scientific study for the observations.
     * @return study or null if none available.
     */
    private ScientificStudy determineStudy() {
        ScientificStudy result = null;
        
        Science astronomy = ScienceUtil.getScience(Science.ASTRONOMY);
        List<ScientificStudy> possibleStudies = new ArrayList<ScientificStudy>();
        
        // Add primary study if in research phase.
        ScientificStudyManager manager = Simulation.instance().getScientificStudyManager();
        ScientificStudy primaryStudy = manager.getOngoingPrimaryStudy(person);
        if (primaryStudy != null) {
            if (ScientificStudy.RESEARCH_PHASE.equals(primaryStudy.getPhase()) && 
                    !primaryStudy.isPrimaryResearchCompleted()) {
                if (astronomy.equals(primaryStudy.getScience())) {
                    // Primary study added twice to double chance of random selection.
                    possibleStudies.add(primaryStudy);
                    possibleStudies.add(primaryStudy);
                }
            }
        }
        
        // Add all collaborative studies in research phase.
        Iterator<ScientificStudy> i = manager.getOngoingCollaborativeStudies(person).iterator();
        while (i.hasNext()) {
            ScientificStudy collabStudy = i.next();
            if (ScientificStudy.RESEARCH_PHASE.equals(collabStudy.getPhase()) && 
                    !collabStudy.isCollaborativeResearchCompleted(person)) {
                if (astronomy.equals(collabStudy.getCollaborativeResearchers().get(person)))
                    possibleStudies.add(collabStudy);
            }
        }
        
        // Randomly select study.
        if (possibleStudies.size() > 0) {
            int selected = RandomUtil.getRandomInt(possibleStudies.size() - 1);
            result = possibleStudies.get(selected);
        }
        
        return result;
    }

    @Override
    protected void addExperience(double time) {
        // Add experience to astronomy skill
        // (1 base experience point per 25 millisols of research time)
        // Experience points adjusted by person's "Academic Aptitude" attribute.
        double newPoints = time / 25D;
        int academicAptitude = person.getNaturalAttributeManager().getAttribute(
            NaturalAttributeManager.ACADEMIC_APTITUDE);
        newPoints += newPoints * ((double) academicAptitude - 50D) / 100D;
        newPoints *= getTeachingExperienceModifier();
        Science astronomyScience = ScienceUtil.getScience(Science.ASTRONOMY);
        String astronomySkill = ScienceUtil.getAssociatedSkill(astronomyScience);
        person.getMind().getSkillManager().addExperience(astronomySkill, newPoints);
    }

    @Override
    public List<String> getAssociatedSkills() {
        List<String> results = new ArrayList<String>(1);
        Science astronomyScience = ScienceUtil.getScience(Science.ASTRONOMY);
        String astronomySkill = ScienceUtil.getAssociatedSkill(astronomyScience);
        results.add(astronomySkill);
        return results;
    }

    @Override
    public int getEffectiveSkillLevel() {
        Science astronomyScience = ScienceUtil.getScience(Science.ASTRONOMY);
        String astronomySkill = ScienceUtil.getAssociatedSkill(astronomyScience);
        SkillManager manager = person.getMind().getSkillManager();
        return manager.getEffectiveSkillLevel(astronomySkill);
    }

    @Override
    protected double performMappedPhase(double time) throws Exception {
        if (getPhase() == null) throw new IllegalArgumentException("Task phase is null");
        if (OBSERVING.equals(getPhase())) return observingPhase(time);
        else return time;
    }
    
    /**
     * Performs the observing phase.
     * @param time the amount of time (millisols) to perform the phase.
     * @return the amount of time (millisols) left over after performing the phase.
     * @throws Exception if error performing the phase.
     */
    protected double observingPhase(double time) throws Exception {
        
        // If person is incapacitated, end task.
        if (person.getPerformanceRating() == 0D) endTask();
        
        // Check for observatory malfunction.
        if (observatory.getBuilding().getMalfunctionManager().hasMalfunction())
            endTask();
        
        //check sunlight and end the task if sunrise
        SurfaceFeatures surface = Simulation.instance().getMars().getSurfaceFeatures();
        double sunlight = surface.getSurfaceSunlight(person.getCoordinates()); 
        if(sunlight > 0) endTask();
        

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
        double observingTime = getEffectiveObservingTime(time);
        if (isPrimary) study.addPrimaryResearchWorkTime(observingTime);
        else study.addCollaborativeResearchWorkTime(person, observingTime);
        
        // Add experience
        addExperience(observingTime);
        
        // Check for lab accident.
        checkForAccident(time);
        
        return 0D;
    }
    
    /**
     * Gets the effective observing time based on the person's astronomy skill.
     * @param time the real amount of time (millisol) for observing.
     * @return the effective amount of time (millisol) for observing.
     */
    private double getEffectiveObservingTime(double time) {
        // Determine effective observing time based on the astronomy skill.
        double observingTime = time;
        int astronomySkill = getEffectiveSkillLevel();
        if (astronomySkill == 0) observingTime /= 2D;
        if (astronomySkill > 1) observingTime += observingTime * (.2D * astronomySkill);
        
        // Modify by tech level of observatory.
        int techLevel = observatory.getTechnologyLevel();
        if (techLevel > 0) observingTime *= techLevel;
        
        // If research assistant, modify by assistant's effective skill.
        if (hasResearchAssistant()) {
            SkillManager manager = getResearchAssistant().getMind().getSkillManager();
            Science astronomyScience = ScienceUtil.getScience(Science.ASTRONOMY);
            int assistantSkill = manager.getEffectiveSkillLevel(ScienceUtil.getAssociatedSkill(astronomyScience));
            if (astronomySkill > 0) observingTime *= 1D + ((double) assistantSkill / (double) astronomySkill);
        }
        
        return observingTime;
    }
    
    /**
     * Check for accident in observatory.
     * @param time the amount of time researching (in millisols)
     */
    private void checkForAccident(double time) {

        double chance = .001D;

        // Astronomy skill modification.
        Science astronomyScience = ScienceUtil.getScience(Science.ASTRONOMY);
        String astronomySkill = ScienceUtil.getAssociatedSkill(astronomyScience);
        int skill = person.getMind().getSkillManager().getEffectiveSkillLevel(astronomySkill);
        if (skill <= 3) chance *= (4 - skill);
        else chance /= (skill - 2);

        if (RandomUtil.lessThanRandPercent(chance * time)) {
            logger.info(person.getName() + " has a observatory accident while observing astronomical objects.");
            if (person.getLocationSituation().equals(Person.INSETTLEMENT)) 
                observatory.getBuilding().getMalfunctionManager().accident();
            else if (person.getLocationSituation().equals(Person.INVEHICLE)) 
                person.getVehicle().getMalfunctionManager().accident(); 
        }
    }
    
    /**
     * Ends the task and performs any final actions.
     */
    public void endTask() {
        super.endTask();
        
        // Remove person from observator so others can use it.
        try {
            if (observatory != null) observatory.removeObserver();
        }
        catch(Exception e) {}
    }
    
    /**
     * Gets the scientific field that is being researched for the study.
     * @return scientific field.
     */
    public Science getResearchScience() {
        return ScienceUtil.getScience(Science.ASTRONOMY);
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
/**
 * Mars Simulation Project
 * BuildingConstructionMission.java
 * @version 2.85 2008-08-24
 * @author Scott Davis
 */
package org.mars_sim.msp.simulation.person.ai.mission;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.simulation.Inventory;
import org.mars_sim.msp.simulation.person.Person;
import org.mars_sim.msp.simulation.person.ai.Skill;
import org.mars_sim.msp.simulation.person.ai.job.Job;
import org.mars_sim.msp.simulation.resource.Part;
import org.mars_sim.msp.simulation.resource.Resource;
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.simulation.structure.construction.ConstructionManager;
import org.mars_sim.msp.simulation.structure.construction.ConstructionSite;
import org.mars_sim.msp.simulation.structure.construction.ConstructionStage;
import org.mars_sim.msp.simulation.structure.construction.ConstructionStageInfo;
import org.mars_sim.msp.simulation.structure.construction.ConstructionValues;
import org.mars_sim.msp.simulation.structure.construction.ConstructionVehicleType;
import org.mars_sim.msp.simulation.vehicle.Crewable;
import org.mars_sim.msp.simulation.vehicle.GroundVehicle;
import org.mars_sim.msp.simulation.vehicle.LightUtilityVehicle;
import org.mars_sim.msp.simulation.vehicle.Vehicle;

/**
 * Mission for construction a stage for a settlement building.
 */
public class BuildingConstructionMission extends Mission implements Serializable {

    private static String CLASS_NAME = 
        "org.mars_sim.msp.simulation.person.ai.mission.BuildingConstructionMission";
    private static Logger logger = Logger.getLogger(CLASS_NAME);
    
    // Default description.
    public static final String DEFAULT_DESCRIPTION = "Construct Building";
    
    // Mission phases
    final public static String PREPARE_SITE_PHASE = "Prepare Site";
    final public static String CONSTRUCTION_PHASE = "Construction";
    
    // Number of mission members.
    private static final int MIN_PEOPLE = 3;
    private static final int MAX_PEOPLE = 10;
    
    // Light utility vehicle attachment parts for construction.
    public static final String SOIL_COMPACTOR = "soil compactor";
    public static final String BACKHOE = "backhoe";
    public static final String BULLDOZER_BLADE = "bulldozer blade";
    public static final String CRANE_BOOM = "crane boom";
    
    private Settlement settlement;
    private ConstructionSite constructionSite;
    private ConstructionStage constructionStage;
    private List<GroundVehicle> constructionVehicles;
    
    /**
     * Constructor
     * @param startingPerson the person starting the mission.
     * @throws MissionException if error creating mission.
     */
    public BuildingConstructionMission(Person startingPerson) throws MissionException {
        // Use Mission constructor.
        super(DEFAULT_DESCRIPTION, startingPerson, MIN_PEOPLE);
        
        if (!isDone()) {
            // Sets the settlement.
            settlement = startingPerson.getSettlement();
            
            // Sets the mission capacity.
            setMissionCapacity(MAX_PEOPLE);
            int availableSuitNum = Mission.getNumberAvailableEVASuitsAtSettlement(settlement);
            if (availableSuitNum < getMissionCapacity()) setMissionCapacity(availableSuitNum);
            
            // Recruit additional people to mission.
            recruitPeopleForMission(startingPerson);
            
            try {
                // Determine construction site and stage.
                int constructionSkill = startingPerson.getMind().getSkillManager().getEffectiveSkillLevel(
                        Skill.CONSTRUCTION);
                ConstructionManager manager = settlement.getConstructionManager();
                ConstructionValues values = manager.getConstructionValues();
                double existingSitesValue = values.getAllConstructionSitesValue(constructionSkill);
                double newSiteValue = values.getNewConstructionSiteValue(constructionSkill);
                
                if (existingSitesValue > newSiteValue) {
                    // Determine which existing construction site to work on.
                    double topSiteValue = 0D;
                    Iterator<ConstructionSite> i = manager.getConstructionSitesNeedingMission().iterator();
                    while (i.hasNext()) {
                        ConstructionSite site = i.next();
                        double siteValue = values.getConstructionSiteValue(site, constructionSkill);
                        if (siteValue > topSiteValue) {
                            constructionSite = site;
                            topSiteValue = siteValue;
                        }
                    }
                }
                else {
                    // Create new site.
                    constructionSite = manager.createNewConstructionSite();
                }
                
                if (constructionSite != null) {
                    
                    // Determine new stage to work on.
                    if (constructionSite.hasUnfinishedStage()) {
                        constructionStage = constructionSite.getCurrentConstructionStage(); 
                    }
                    else {
                        ConstructionStageInfo stageInfo = null;
                        double topStageInfoValue = 0D;
                        Map<ConstructionStageInfo, Double> stageValues = 
                            values.getNewConstructionStageValues(constructionSite, constructionSkill);
                        Iterator<ConstructionStageInfo> i = stageValues.keySet().iterator();
                        while (i.hasNext()) {
                            ConstructionStageInfo info = i.next();
                            double infoValue = stageValues.get(info);
                            if (infoValue > topStageInfoValue) {
                                stageInfo = info;
                                topStageInfoValue = infoValue;
                            }
                        }
                        
                        if (stageInfo != null) {
                            constructionStage = new ConstructionStage(stageInfo);
                            constructionSite.addNewStage(constructionStage);
                        }
                        else {
                            endMission("New construction stage could not be determined.");
                        }
                    }
                    
                    constructionSite.setUndergoingConstruction(true);
                }
                else {
                    endMission("Construction site could not be found or created.");
                }
                
                // Reserve construction vehicles.
                if (constructionStage != null) {
                    constructionVehicles = new ArrayList<GroundVehicle>();
                    Iterator<ConstructionVehicleType> i = constructionStage.getInfo().getVehicles().iterator();
                    while (i.hasNext()) {
                        ConstructionVehicleType vehicleType = i.next();
                        // Only handle light utility vehicles for now.
                        if (vehicleType.getVehicleClass() == LightUtilityVehicle.class) {
                            LightUtilityVehicle luv = reserveLightUtilityVehicle();
                            if (luv != null) constructionVehicles.add(luv); 
                            else endMission("Light utility vehicle not available.");
                        }
                    }
                }
            }
            catch (Exception e) {
                logger.log(Level.SEVERE, "Error determining construction sites.");
                throw new MissionException("Error determining construction sites.", e);
            }
        }
        
        // Add phases.
        addPhase(PREPARE_SITE_PHASE);
        addPhase(CONSTRUCTION_PHASE);
        
        // Set initial mission phase.
        setPhase(PREPARE_SITE_PHASE);
        setPhaseDescription("Preparing construction site at " + settlement.getName());
    }
    
    /** 
     * Gets the weighted probability that a given person would start this mission.
     * @param person the given person
     * @return the weighted probability
     */
    public static double getNewMissionProbability(Person person) {
        
        double result = 0D;
        
        // Determine job modifier.
        Job job = person.getMind().getJob();
        double jobModifier = 0D;
        if (job != null) 
            jobModifier = job.getStartMissionProbabilityModifier(BuildingConstructionMission.class); 
        
        // Check if person is in a settlement.
        boolean inSettlement = person.getLocationSituation().equals(Person.INSETTLEMENT);
        
        if (inSettlement && (jobModifier > 0D)) {
            Settlement settlement = person.getSettlement();
        
            // Check if available light utility vehicles.
            boolean reservableLUV = isLUVAvailable(settlement);
            
            // Check if LUV attachment parts available.
            boolean availableAttachmentParts = areAvailableAttachmentParts(settlement);
            
            if (reservableLUV && availableAttachmentParts) {
                
                try {
                    int constructionSkill = person.getMind().getSkillManager().getEffectiveSkillLevel(
                            Skill.CONSTRUCTION);
                    ConstructionValues values =  settlement.getConstructionManager().getConstructionValues();
                    double constructionValue = values.getSettlementConstructionValue(constructionSkill);
                    result = constructionValue / 1000D;
                }
                catch (Exception e) {
                    logger.log(Level.SEVERE, "Error getting mining site.", e);
                }
            }       
            
            // Check if min number of EVA suits at settlement.
            if (Mission.getNumberAvailableEVASuitsAtSettlement(person.getSettlement()) < MIN_PEOPLE) 
                result = 0D;
        }
        
        return result;
    }
    
    /**
     * Checks if a light utility vehicle (LUV) is available for the mission.
     * @param settlement the settlement to check.
     * @return true if LUV available.
     */
    private static boolean isLUVAvailable(Settlement settlement) {
        boolean result = false;
        
        Iterator<Vehicle> i = settlement.getParkedVehicles().iterator();
        while (i.hasNext()) {
            Vehicle vehicle = i.next();
            
            if (vehicle instanceof LightUtilityVehicle) {
                boolean usable = true;
                if (vehicle.isReserved()) usable = false;
                if (!vehicle.getStatus().equals(Vehicle.PARKED)) usable = false;
                if (((Crewable) vehicle).getCrewNum() > 0) usable = false;
                if (usable) result = true;
            }
        }
        
        return result;
    }
    
    /**
     * Checks if the required attachment parts are available.
     * @param settlement the settlement to check.
     * @return true if available attachment parts.
     */
    private static boolean areAvailableAttachmentParts(Settlement settlement) {
        boolean result = true;
        
        Inventory inv = settlement.getInventory();
        
        try {
            Part soilCompactor = (Part) Part.findItemResource(SOIL_COMPACTOR);
            if (!inv.hasItemResource(soilCompactor)) result = false;
            Part backhoe = (Part) Part.findItemResource(BACKHOE);
            if (!inv.hasItemResource(backhoe)) result = false;
            Part bulldozerBlade = (Part) Part.findItemResource(BULLDOZER_BLADE);
            if (!inv.hasItemResource(bulldozerBlade)) result = false;
            Part craneBoom = (Part) Part.findItemResource(CRANE_BOOM);
            if (!inv.hasItemResource(craneBoom)) result = false;
        }
        catch (Exception e) {
            logger.log(Level.SEVERE, "Error in getting parts.");
        }
        
        return result;
    }
    
    @Override
    protected void determineNewPhase() throws MissionException {
        // TODO Auto-generated method stub

    }

    @Override
    protected void performPhase(Person person) throws MissionException {
        // TODO Auto-generated method stub

    }

    @Override
    public Settlement getAssociatedSettlement() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<Resource, Number> getResourcesNeededForRemainingMission(
            boolean useBuffer, boolean parts) throws MissionException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<Class, Integer> getEquipmentNeededForRemainingMission(
            boolean useBuffer) throws MissionException {
        // TODO Auto-generated method stub
        return null;
    }
    
    /**
     * Reserves a light utility vehicle for the mission.
     * @return reserved light utility vehicle or null if none.
     */
    private LightUtilityVehicle reserveLightUtilityVehicle() {
        LightUtilityVehicle result = null;
        
        Iterator<Vehicle> i = getAssociatedSettlement().getParkedVehicles().iterator();
        while (i.hasNext() && (result == null)) {
            Vehicle vehicle = i.next();
            
            if (vehicle instanceof LightUtilityVehicle) {
                LightUtilityVehicle luvTemp = (LightUtilityVehicle) vehicle;
                if (luvTemp.getStatus().equals(Vehicle.PARKED) && !luvTemp.isReserved() 
                        && (luvTemp.getCrewNum() == 0)) {
                    result = luvTemp;
                    luvTemp.setReservedForMission(true);
                }
            }
        }
        
        return result;
    }
}
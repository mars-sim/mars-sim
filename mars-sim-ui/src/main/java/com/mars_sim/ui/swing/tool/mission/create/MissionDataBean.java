/*
 * Mars Simulation Project
 * MissionDataBean.java
 * @date 2021-08-28
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.tool.mission.create;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.mars_sim.core.Simulation;
import com.mars_sim.core.building.construction.ConstructionSite;
import com.mars_sim.core.environment.MineralSite;
import com.mars_sim.core.goods.Good;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.mission.AreologyFieldStudy;
import com.mars_sim.core.person.ai.mission.BiologyFieldStudy;
import com.mars_sim.core.person.ai.mission.ConstructionMission;
import com.mars_sim.core.person.ai.mission.CollectIce;
import com.mars_sim.core.person.ai.mission.CollectRegolith;
import com.mars_sim.core.person.ai.mission.Delivery;
import com.mars_sim.core.person.ai.mission.EmergencySupply;
import com.mars_sim.core.person.ai.mission.Exploration;
import com.mars_sim.core.person.ai.mission.MeteorologyFieldStudy;
import com.mars_sim.core.person.ai.mission.Mining;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.person.ai.mission.MissionManager;
import com.mars_sim.core.person.ai.mission.MissionType;
import com.mars_sim.core.person.ai.mission.RescueSalvageVehicle;
import com.mars_sim.core.person.ai.mission.Trade;
import com.mars_sim.core.person.ai.mission.TravelToSettlement;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.science.ScientificStudy;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.vehicle.Drone;
import com.mars_sim.core.vehicle.GroundVehicle;
import com.mars_sim.core.vehicle.LightUtilityVehicle;
import com.mars_sim.core.vehicle.Rover;
import com.mars_sim.core.vehicle.Vehicle;

/**
 * Mission data holder bean.
 */
public class MissionDataBean {

    private MissionType missionType;
	private String type = "";
	private String description = "";
	private String designation = "";
	
	private Settlement startingSettlement;
	private Settlement destinationSettlement;
    private Settlement constructionSettlement;
    
	private Drone drone;
	private Rover rover;
	private Vehicle rescueVehicle;
	private LightUtilityVehicle luv;
	
	private Coordinates fieldSite;
	private Coordinates prospectingSite;
	
	private Coordinates[] explorationSites;
	
	private MineralSite miningSite;
    
    private ConstructionSite constructionSite;
  
    private Person leadResearcher;
    private ScientificStudy study;
    
	private Collection<Worker> mixedMembers = new HashSet<>();
	private Collection<Worker> botMembers = new HashSet<>();
    private List<GroundVehicle> constructionVehicles;
	private Map<Good, Integer> sellGoods;
	private Map<Good, Integer> buyGoods;
	
    private static MissionManager missionManager = Simulation.instance().getMissionManager();
    
	/**
	 * Creates a mission from the mission data.
	 */
    public void createMission() {
    	// Note: how to resolve the situation when rover is no longer available ?
    	
	    Mission mission = null;
	    switch (missionType) {
			case MissionType.AREOLOGY ->
					mission = new AreologyFieldStudy(mixedMembers, leadResearcher, study,
							rover, fieldSite);
			case MissionType.BIOLOGY ->
					mission = new BiologyFieldStudy(mixedMembers, leadResearcher, study,
							rover, fieldSite);
			case MissionType.METEOROLOGY ->
					mission = new MeteorologyFieldStudy(mixedMembers, leadResearcher, study,
							rover, fieldSite);
			case MissionType.CONSTRUCTION ->
					mission = new ConstructionMission(mixedMembers, constructionSettlement, constructionSite,
							constructionVehicles);
			case MissionType.COLLECT_ICE -> {
					List<Coordinates> collectionSites = new ArrayList<>(1);
					collectionSites.add(prospectingSite);
					mission = new CollectIce(mixedMembers, collectionSites, rover);
				}
			case MissionType.COLLECT_REGOLITH -> {
					List<Coordinates> collectionSites = new ArrayList<>(1);
					collectionSites.add(prospectingSite);
					mission = new CollectRegolith(mixedMembers, collectionSites, rover);
				}
			case MissionType.DELIVERY -> {
					Person startingMember = null;
					for (Worker mm: mixedMembers) {
						if (mm instanceof Person p)
							startingMember = p;
					}
				
					mission = new Delivery(startingMember, mixedMembers, destinationSettlement, drone,
							sellGoods, buyGoods);
				}
			case MissionType.EMERGENCY_SUPPLY ->
					mission = new EmergencySupply(mixedMembers, destinationSettlement,
							sellGoods, rover);
			case MissionType.EXPLORATION -> {
					List<Coordinates> collectionSites = new ArrayList<>(explorationSites.length);
					collectionSites.addAll(Arrays.asList(explorationSites));
					mission = new Exploration(mixedMembers, collectionSites, rover);
				}
			case MissionType.MINING ->
					mission = new Mining(mixedMembers, miningSite, rover, luv);
			case MissionType.RESCUE_SALVAGE_VEHICLE ->
					mission = new RescueSalvageVehicle(mixedMembers, rescueVehicle, rover);
			case MissionType.TRADE ->
					mission = new Trade(mixedMembers, destinationSettlement, rover,
							sellGoods, buyGoods);
			case MissionType.TRAVEL_TO_SETTLEMENT ->
					mission = new TravelToSettlement(mixedMembers, destinationSettlement, rover);
			default -> throw new IllegalStateException("Mission type: " + type + " unknown");
		}

	    missionManager.addMission(mission);
	}
 
	/**
	 * Gets mission types.
	 * @return array of mission type strings.
	 */
    protected static MissionType[] getMissionTypes() {
    	return MissionType.values();
	}
    

	/**
	 * Gets mission description based on a mission type.
	 * 
	 * @param missionType the mission type.
	 * @return the mission description.
	 */
    protected static String getMissionDescription(MissionType missionType) {
    	return missionType.getName();
	}

	/**
	 * Gets the mission type.
	 * @return type.
	 */
    protected String getType() {
		return type;
	}

	/**
	 * Sets the mission type.
	 * @param type the mission type.
	 */
    protected void setType(String type) {
		this.type = type;
	}

	/**
	 * Gets the mission type enum.
	 * @return missionType enum.
	 */
    public MissionType getMissionType() {
		return missionType;
	}

	/**
	 * Sets the mission type enum.
	 * 
	 * @param missionType the mission type enum.
	 */
    public void setMissionType(MissionType missionType) {
    	this.missionType = missionType;
    }
    
	/**
	 * Gets the mission description.
	 * 
	 * @return description.
	 */
    protected String getDescription() {
		return description;
	}

	/**
	 * Gets the mission designation.
	 * 
	 * @return designation.
	 */
    protected String getDesignation() {
		return designation;
	}

	/**
	 * Sets the mission description.
	 * 
	 * @param description the mission description.
	 */
    public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Gets the starting settlement.
	 * 
	 * @return settlement.
	 */
    public Settlement getStartingSettlement() {
		return startingSettlement;
	}

	/**
	 * Sets the starting settlement.
	 * 
	 * @param startingSettlement starting settlement.
	 */
    public void setStartingSettlement(Settlement startingSettlement) {
		this.startingSettlement = startingSettlement;
	}

	/**
	 * Gets the rover.
	 * 
	 * @return rover.
	 */
    public Rover getRover() {
		return rover;
	}

	/**
	 * Sets the rover.
	 * 
	 * @param rover the rover.
	 */
    public void setRover(Rover rover) {
		this.rover = rover;
	}

	/**
	 * Gets the drone.
	 * 
	 * @return drone.
	 */
    public Drone getDrone() {
		return drone;
	}

	/**
	 * Sets the drone.
	 * 
	 * @param drone the drone.
	 */
    public void setDrone(Drone drone) {
		this.drone = drone;
	}
    
	/**
	 * Gets the mission members.
	 * 
	 * @return the members.
	 */
    protected Collection<Worker> getMixedMembers() {
		return mixedMembers;
	}
    
	/**
	 * Gets both person and robot mission members.
	 * 
	 * @return the members.
	 */
    protected Collection<Worker> getAllMembers() {
    	Collection<Worker> all = mixedMembers;
    	all.addAll(botMembers);
		return all;
	}

    
	/**
	 * Sets the mission members.
	 * 
	 * @param members the members.
	 */
    public void setBotMembers(Collection<Worker> mm) {
    	this.botMembers = mm;
	}
    
	/**
	 * Sets the mission members.
	 * 
	 * @param members the members.
	 */
    public void setMixedMembers(Collection<Worker> mm) {
    	this.mixedMembers = mm;
	}
    
	/**
	 * Gets the destination settlement.
	 * 
	 * @return destination settlement.
	 */
    public Settlement getDestinationSettlement() {
		return destinationSettlement;
	}

	/**
	 * Sets the destination settlement.
	 * 
	 * @param destinationSettlement the destination settlement.
	 */
    public void setDestinationSettlement(Settlement destinationSettlement) {
		this.destinationSettlement = destinationSettlement;
	}

	/**
	 * Sets the rescue vehicle.
	 * 
	 * @param vehicle the target of the rescue
	 */
    public void setRescueVehicle(Vehicle vehicle) {
		this.rescueVehicle = vehicle;
	}

	/**
	 * Set the prospecting site for a collection Mission.
	 * @param navpoint the prospecting site coordinates.
	 */
    public void setProspectingSite(Coordinates navpoint) {
        this.prospectingSite = navpoint;
    }

	/**
	 * Gets the exploration sites.
	 * 
	 * @return exploration sites.
	 */
    protected Coordinates[] getExplorationSites() {
		return explorationSites;
	}

	/**
	 * Sets the exploration sites.
	 * 
	 * @param explorationSites the exploration sites.
	 */
    protected void setExplorationSites(Coordinates[] explorationSites) {
		this.explorationSites = explorationSites;
	}

	/**
	 * Gets the sell goods.
	 * 
	 * @return map of goods and integer amounts.
	 */
    protected Map<Good, Integer> getSellGoods() {
		return sellGoods;
	}

	/**
	 * Sets the sell goods.
	 * 
	 * @param sellGoods map of goods and integer amounts.
	 */
    public void setSellGoods(Map<Good, Integer> sellGoods) {
		this.sellGoods = sellGoods;
	}

	/**
	 * Gets the buy goods.
	 * 
	 * @return map of goods and integer amounts.
	 */
    protected Map<Good, Integer> getBuyGoods() {
		return buyGoods;
	}

	/**
	 * Sets the buy goods.
	 * 
	 * @param buyGoods map of goods and integer amounts.
	 */
	public void setBuyGoods(Map<Good, Integer> buyGoods) {
		this.buyGoods = buyGoods;
	}

	/**
	 * Gets the light utility vehicle.
	 * 
	 * @return light utility vehicle
	 */
	protected LightUtilityVehicle getLUV() {
		return luv;
	}

	/**
	 * Sets the light utility vehicle.
	 * 
	 * @param luv the light utility vehicle
	 */
	public void setLUV(LightUtilityVehicle luv) {
		this.luv = luv;
	}

	/**
	 * Gets the mining site.
	 * 
	 * @return mining site.
	 */
	protected MineralSite getMiningSite() {
		return miningSite;
	}

	/**
	 * Sets the mining site.
	 * 
	 * @param miningSite the mining site.
	 */
	public void setMiningSite(MineralSite miningSite) {
		this.miningSite = miningSite;
	}

    /**
     * Gets the construction settlement.
     * 
     * @return settlement.
     */
    protected Settlement getConstructionSettlement() {
        return constructionSettlement;
    }

    /**
     * Sets the construction settlement.
     * 
     * @param constructionSettlement the construction settlement.
     */
    protected void setConstructionSettlement(Settlement constructionSettlement) {
        this.constructionSettlement = constructionSettlement;
    }

    /**
     * Gets the construction site.
     * 
     * @return construction site.
     */
    protected ConstructionSite getConstructionSite() {
        return constructionSite;
    }

    /**
     * Sets the construction site.
     * 
     * @param constructionSite the construction site.
     */
    public void setConstructionSite(ConstructionSite constructionSite) {
        this.constructionSite = constructionSite;
    }

    /**
     * Gets the construction vehicles.
     * 
     * @return list of ground vehicles.
     */
    protected List<GroundVehicle> getConstructionVehicles() {
        return constructionVehicles;
    }

    /**
     * Sets the construction vehicles.
     * 
     * @param constructionVehicles list of ground vehicles.
     */
    protected void setConstructionVehicles(List<GroundVehicle> constructionVehicles) {
        this.constructionVehicles = constructionVehicles;
    }

    /**
     * Gets the field site.
     * 
     * @return field site location.
     */
    protected Coordinates getFieldSite() {
        return fieldSite;
    }

    /**
     * Sets the field site.
     * 
     * @param fieldSite the field site location.
     */
    protected void setFieldSite(Coordinates fieldSite) {
        this.fieldSite = fieldSite;
    }

    /**
     * Gets the lead researcher for the mission.
     * 
     * @return lead researcher.
     */
    protected Person getLeadResearcher() {
        return leadResearcher;
    }

    /**
     * Sets the lead researcher for the mission.
     * 
     * @param leadResearcher the lead researcher.
     */
    protected void setLeadResearcher(Person leadResearcher) {
        this.leadResearcher = leadResearcher;
    }

    /**
     * Gets the scientific study.
     * 
     * @return the scientific study.
     */
    protected ScientificStudy getStudy() {
        return study;
    }

    /**
     * Sets the scientific study.
     * 
     * @param study the scientific study.
     */
    protected void setScientificStudy(ScientificStudy study) {
        this.study = study;
    }

    /**
     * Gets the emergency resources map.
     * 
     * @return map of resources and amounts (kg).
     */
    protected Map<Good, Integer> getEmergencyGoods() {
        return sellGoods;
    }

    /**
     * Sets the emergency resources.
     * 
     * @param emergencyGoods map of resources and amounts (kg).
     */
    protected void setEmergencyGoods(Map<Good, Integer> emergencyGoods) {
        this.sellGoods = emergencyGoods;
    }

	protected boolean isScientificMission() {
        return missionType == MissionType.AREOLOGY
                || missionType == MissionType.BIOLOGY
                || missionType == MissionType.METEOROLOGY;
	}

	protected boolean isMiningMission() {
		return missionType == MissionType.MINING;
	}

	protected boolean isExplorationMission() {
    	return missionType == MissionType.EXPLORATION;
	}

	protected boolean isTradeMission() {
    	return missionType == MissionType.TRADE;
	}

	protected boolean isDeliveryMission() {
    	return missionType == MissionType.DELIVERY;
	}

	protected boolean isEmergencySupplyMission() {
    	return missionType == MissionType.EMERGENCY_SUPPLY;
	}

	protected boolean isTravelMission() {
    	return missionType == MissionType.TRAVEL_TO_SETTLEMENT;
	}

	protected boolean isConstructionMission() {
    	return missionType == MissionType.CONSTRUCTION;
	}

	protected boolean requiresFieldSite() {
		return isScientificMission();
	}

	protected boolean requiresDestinationSettlement() {
		return  ( isDeliveryMission() || isTradeMission() || isEmergencySupplyMission() || isTravelMission() );
	}

	protected boolean isProspectingMission() {
		return  ( missionType == MissionType.COLLECT_REGOLITH
				|| missionType == MissionType.COLLECT_ICE);
	}

	/**
	 * Describes if the mission requires a vehicle for transportation or is executed
	 * on-site.
	 * 
	 * @return true, if a mission takes place outside the starting base
	 */
	protected boolean isRemoteMission() {
        return missionType != MissionType.CONSTRUCTION;
	}

	/**
	 * Missions that require meeting another rover.
	 * 
	 * @return true, when a on-ground rendezvous is needed
	 */
	protected boolean isRescueRendezvousMission() {
		return missionType == MissionType.RESCUE_SALVAGE_VEHICLE;
	}
}

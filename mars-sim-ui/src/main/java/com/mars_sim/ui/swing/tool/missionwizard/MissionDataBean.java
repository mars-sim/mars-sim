/*
 * Mars Simulation Project
 * MissionDataBean.java
 * @date 2021-08-28
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.tool.missionwizard;

import java.util.ArrayList;
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
import com.mars_sim.core.person.ai.mission.CollectIce;
import com.mars_sim.core.person.ai.mission.CollectRegolith;
import com.mars_sim.core.person.ai.mission.ConstructionMission;
import com.mars_sim.core.person.ai.mission.Delivery;
import com.mars_sim.core.person.ai.mission.EmergencySupply;
import com.mars_sim.core.person.ai.mission.Exploration;
import com.mars_sim.core.person.ai.mission.MeteorologyFieldStudy;
import com.mars_sim.core.person.ai.mission.Mining;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.person.ai.mission.MissionType;
import com.mars_sim.core.person.ai.mission.RescueSalvageVehicle;
import com.mars_sim.core.person.ai.mission.Trade;
import com.mars_sim.core.person.ai.mission.TravelToSettlement;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.robot.Robot;
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
class MissionDataBean {

    private MissionType missionType;
	private String type = "";
	private String description = "";
	
	private Settlement startingSettlement;
	private Settlement destinationSettlement;
    
	private Drone drone;
	private Rover rover;
	private Vehicle rescueVehicle;
	private LightUtilityVehicle luv;
		
	private List<Coordinates> routePoints;
	
	private MineralSite miningSite;
    
    private ConstructionSite constructionSite;
  
    private ScientificStudy study;
    
	private List<Person> personMembers = new ArrayList<>();
	private List<Robot> botMembers = new ArrayList<>();
    private List<GroundVehicle> constructionVehicles;
	private Map<Good, Integer> sellGoods;
	private Map<Good, Integer> buyGoods;
	    
	/**
	 * Creates a mission from the mission data.
	 * @return the created mission.
	 */
    public Mission createMission() {
    	// Note: how to resolve the situation when rover is no longer available ?
    	List<Worker> mixedMembers = new ArrayList<>();
		if (personMembers != null)
			mixedMembers.addAll(personMembers);
		if (botMembers != null)
			mixedMembers.addAll(botMembers);

	    Mission mission = null;
	    switch (missionType) {
			case MissionType.AREOLOGY ->
					mission = new AreologyFieldStudy(mixedMembers, study,
							rover, routePoints.get(0));
			case MissionType.BIOLOGY ->
					mission = new BiologyFieldStudy(mixedMembers, study,
							rover, routePoints.get(0));
			case MissionType.METEOROLOGY ->
					mission = new MeteorologyFieldStudy(mixedMembers, study,
							rover, routePoints.get(0));
			case MissionType.CONSTRUCTION ->
					mission = new ConstructionMission(mixedMembers, startingSettlement, constructionSite,
							constructionVehicles);
			case MissionType.COLLECT_ICE ->
					mission = new CollectIce(mixedMembers, routePoints, rover);
			case MissionType.COLLECT_REGOLITH ->
					mission = new CollectRegolith(mixedMembers, routePoints, rover);
			case MissionType.DELIVERY ->
					mission = new Delivery(mixedMembers, destinationSettlement, drone,
							sellGoods, buyGoods);
			case MissionType.EMERGENCY_SUPPLY ->
					mission = new EmergencySupply(mixedMembers, destinationSettlement,
							sellGoods, rover);
			case MissionType.EXPLORATION ->
					mission = new Exploration(mixedMembers, routePoints, rover);
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

		var missionManager = Simulation.instance().getMissionManager();
	    missionManager.addMission(mission);

		return mission;
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
	 * Sets the mission members.
	 * 
	 * @param members the members.
	 */
    public void setBotMembers(List<Robot> mm) {
    	this.botMembers = mm;
	}
    
	/**
	 * Sets the mission members.
	 * 
	 * @param members the members.
	 */
    public void setPersonMembers(List<Person> mm) {
    	this.personMembers = mm;
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
	 * Sets the points on a route.
	 * 
	 * @param points the route points.
	 */
    public void setRoutePoints(List<Coordinates> points) {
		this.routePoints = points;
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
	 * Sets the buy goods.
	 * 
	 * @param buyGoods map of goods and integer amounts.
	 */
	public void setBuyGoods(Map<Good, Integer> buyGoods) {
		this.buyGoods = buyGoods;
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
	 * Sets the mining site.
	 * 
	 * @param miningSite the mining site.
	 */
	public void setMiningSite(MineralSite miningSite) {
		this.miningSite = miningSite;
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
     * Sets the scientific study.
     * 
     * @param study the scientific study.
     */
    public void setScientificStudy(ScientificStudy study) {
        this.study = study;
    }
}

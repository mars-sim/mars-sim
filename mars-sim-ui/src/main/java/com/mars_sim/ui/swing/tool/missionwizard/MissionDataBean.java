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
import java.util.logging.Logger;

import com.mars_sim.core.building.construction.ConstructionSite;
import com.mars_sim.core.environment.Landmark;
import com.mars_sim.core.environment.MineralSite;
import com.mars_sim.core.goods.Good;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.mission.MetaMission;
import com.mars_sim.core.mission.MetaMissionRegistry;
import com.mars_sim.core.mission.MissionCreationException;
import com.mars_sim.core.mission.predefined.LandmarkMetaMission;
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

	private static Logger logger = Logger.getLogger(MissionDataBean.class.getName());
	
	private MetaMission meta;
	private String type = "";
	
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

	private Landmark landmark;
	    
	/**
	 * Creates a mission from the mission data.
	 * @return the created mission.
	 */
    public Mission createMission() {
    	// Note: how to resolve the situation when rover is no longer available ?
    	List<Worker> mixedMembers = new ArrayList<>(personMembers);
		if (botMembers != null)
			mixedMembers.addAll(botMembers);

		// Create the mission roster;this is for the new single constructor per Mission pattern
		var roster = new MetaMission.Roster(getLeader(), getWorkerMembers(), rover);

	    try {
			Mission mission = switch (meta.getType()) {
				case MissionType.AREOLOGY -> new AreologyFieldStudy(mixedMembers, study,
														rover, routePoints.get(0));
				case MissionType.BIOLOGY -> new BiologyFieldStudy(mixedMembers, study,
														rover, routePoints.get(0));
				case MissionType.METEOROLOGY -> new MeteorologyFieldStudy(mixedMembers, study,
														rover, routePoints.get(0));
				case MissionType.CONSTRUCTION -> new ConstructionMission(mixedMembers, startingSettlement, constructionSite,
														constructionVehicles);
				case MissionType.COLLECT_ICE -> new CollectIce(mixedMembers, routePoints, rover);
				case MissionType.COLLECT_REGOLITH -> new CollectRegolith(mixedMembers, routePoints, rover);
				case MissionType.DELIVERY -> new Delivery(mixedMembers, destinationSettlement, drone,
														sellGoods, buyGoods);
				case MissionType.EMERGENCY_SUPPLY -> new EmergencySupply(mixedMembers, destinationSettlement,
														sellGoods, rover);
				case MissionType.EXPLORATION -> new Exploration(mixedMembers, routePoints, rover);
				case MissionType.MINING -> new Mining(mixedMembers, miningSite, rover, luv);
				case MissionType.RESCUE_SALVAGE_VEHICLE -> new RescueSalvageVehicle(mixedMembers, rescueVehicle, rover);
				case MissionType.TRADE -> new Trade(mixedMembers, destinationSettlement, rover,
														sellGoods, buyGoods);
				case MissionType.TRAVEL_TO_SETTLEMENT -> new TravelToSettlement(roster, destinationSettlement, false);
				case MissionType.TEST_DRIVE -> meta.constructInstance(roster, false);
				case MissionType.VISIT_LANDMARK -> ((LandmarkMetaMission)meta).constructInstance(roster, landmark, false);
				default -> throw new IllegalStateException("Mission type: " + type + " unknown");
			};

			startingSettlement.getMissionControl().addMission(mission);

			return mission;
		} catch (MissionCreationException e) {
			logger.severe("Error creating mission: " + e.getMessage());
			return null;
		}
	}

    public MissionType getMissionType() {
		return meta.getType();
	}

	/**
	 * Meta mission is the mission type object that contains the mission name and other information.
	 */
	public MetaMission getMetaMission() {
		return meta;
	}

	/**
	 * Sets the mission type enum.
	 * 
	 * @param missionType the mission type enum.
	 */
    public void setMissionType(MissionType missionType) {
		this.meta = MetaMissionRegistry.getMetaMission(missionType);
		if (this.meta == null) {
			// This should never happen
			throw new IllegalArgumentException("No meta mission found for mission type: " + missionType);
		}
    }
	
    public Settlement getStartingSettlement() {
		return startingSettlement;
	}

    public void setStartingSettlement(Settlement startingSettlement) {
		this.startingSettlement = startingSettlement;
	}

    public Rover getRover() {
		return rover;
	}

    public void setRover(Rover rover) {
		this.rover = rover;
	}

    public Drone getDrone() {
		return drone;
	}

    public void setDrone(Drone drone) {
		this.drone = drone;
	}

    public void setBotMembers(List<Robot> mm) {
    	this.botMembers = mm;
	}
    
    public void setPersonMembers(List<Person> mm) {
    	this.personMembers = mm;
	}
    
	/**
	 * Leader is the first Person selected.
	 */
	public Person getLeader() {
		if (personMembers != null && !personMembers.isEmpty()) {
			return personMembers.get(0);
		}
		return null;
	}

	/**
	 * This is a combination of the Person & Bot members minus the leader.
	 */
	public List<Worker> getWorkerMembers() {
		List<Worker> members = new ArrayList<>();
		if (personMembers != null && personMembers.size() > 1) {
			members.addAll(personMembers.subList(1, personMembers.size()));
		}
		if (botMembers != null) {
			members.addAll(botMembers);
		}
		return members;
	}

    public Settlement getDestinationSettlement() {
		return destinationSettlement;
	}

    public void setDestinationSettlement(Settlement destinationSettlement) {
		this.destinationSettlement = destinationSettlement;
	}

    public void setRescueVehicle(Vehicle vehicle) {
		this.rescueVehicle = vehicle;
	}

	public Vehicle getRescueVehicle() {
		return rescueVehicle;
	}

    public void setRoutePoints(List<Coordinates> points) {
		this.routePoints = points;
	}

    public void setSellGoods(Map<Good, Integer> sellGoods) {
		this.sellGoods = sellGoods;
	}

	public void setBuyGoods(Map<Good, Integer> buyGoods) {
		this.buyGoods = buyGoods;
	}

	public void setLUV(LightUtilityVehicle luv) {
		this.luv = luv;
	}

	public Vehicle getLUV() {
		return luv;
	}
	
	public void setMiningSite(MineralSite miningSite) {
		this.miningSite = miningSite;
	}

	public MineralSite getMiningSite() {
		return miningSite;
	}

    public void setConstructionSite(ConstructionSite constructionSite) {
        this.constructionSite = constructionSite;
    }

	public ConstructionSite getConstructionSite() {
		return constructionSite;
	}

    public void setScientificStudy(ScientificStudy study) {
        this.study = study;
    }

	public ScientificStudy getScientificStudy() {
		return study;	
	}

	public void setLandmark(Landmark landmark) {
		this.landmark = landmark;	
	}

	public Landmark getLandmark() {
		return landmark;	
	}
}

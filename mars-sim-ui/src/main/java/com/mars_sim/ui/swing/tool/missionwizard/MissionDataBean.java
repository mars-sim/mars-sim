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

import com.mars_sim.core.building.construction.ConstructionSite;
import com.mars_sim.core.environment.Landmark;
import com.mars_sim.core.environment.MineralSite;
import com.mars_sim.core.goods.Good;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.mission.MetaMission;
import com.mars_sim.core.mission.MetaMissionRegistry;
import com.mars_sim.core.mission.predefined.ExplorationMeta;
import com.mars_sim.core.mission.predefined.LandmarkMetaMission;
import com.mars_sim.core.mission.predefined.TestDriveMetaMission;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.person.ai.mission.MissionType;
import com.mars_sim.core.person.ai.mission.meta.AreologyFieldStudyMeta;
import com.mars_sim.core.person.ai.mission.meta.BiologyFieldStudyMeta;
import com.mars_sim.core.person.ai.mission.meta.CollectIceMeta;
import com.mars_sim.core.person.ai.mission.meta.CollectRegolithMeta;
import com.mars_sim.core.person.ai.mission.meta.ConstructionMissionMeta;
import com.mars_sim.core.person.ai.mission.meta.DeliveryMeta;
import com.mars_sim.core.person.ai.mission.meta.EmergencySupplyMeta;
import com.mars_sim.core.person.ai.mission.meta.MeteorologyFieldStudyMeta;
import com.mars_sim.core.person.ai.mission.meta.MiningMeta;
import com.mars_sim.core.person.ai.mission.meta.RescueSalvageVehicleMeta;
import com.mars_sim.core.person.ai.mission.meta.TradeMeta;
import com.mars_sim.core.person.ai.mission.meta.TravelToSettlementMeta;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.science.ScientificStudy;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.vehicle.Drone;
import com.mars_sim.core.vehicle.LightUtilityVehicle;
import com.mars_sim.core.vehicle.Vehicle;

/**
 * Mission data holder bean.
 */
class MissionDataBean {
	
	private MetaMission meta;
	private String type = "";
	
	private Settlement startingSettlement;
	private Settlement destination;
    
	private Vehicle rover;
	private Vehicle rescueVehicle;
	private LightUtilityVehicle luv;
		
	private List<Coordinates> routePoints;
	
	private MineralSite miningSite;
    
    private ConstructionSite constructionSite;
  
    private ScientificStudy study;
    
	private List<Person> personMembers = new ArrayList<>();
	private List<Robot> botMembers = new ArrayList<>();
    private List<LightUtilityVehicle> constructionVehicles;
	private Map<Good, Integer> sellGoods;
	private Map<Good, Integer> buyGoods;

	private Landmark landmark;
	private List<MineralSite> exploration;
	    
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
		boolean needsReview = false;  // This could be user selected in the future
		Mission mission = switch (meta) {
			case AreologyFieldStudyMeta m -> m
					.constructInstance(roster, study, routePoints.get(0));
			case BiologyFieldStudyMeta m -> m
					.constructInstance(roster, study, routePoints.get(0));
			case MeteorologyFieldStudyMeta m -> m
					.constructInstance(roster, study, routePoints.get(0));
			case ConstructionMissionMeta m -> m
					.constructInstance(mixedMembers, startingSettlement, constructionSite, constructionVehicles);
			case CollectIceMeta m -> m.constructInstance(roster, routePoints);
			case CollectRegolithMeta m -> m.constructInstance(roster, routePoints);
			case DeliveryMeta m -> m
					.constructInstance(mixedMembers, destination, (Drone) rover, sellGoods, buyGoods);
			case EmergencySupplyMeta m -> m
					.constructInstance(roster, destination, sellGoods);
			case ExplorationMeta m -> m.constructInstance(roster, needsReview, exploration);
			case MiningMeta m -> m.constructInstance(roster, miningSite, luv);
			case RescueSalvageVehicleMeta m -> m.constructInstance(roster, rescueVehicle);
			case TradeMeta m -> m
					.constructInstance(roster, destination, sellGoods, buyGoods);
			case TravelToSettlementMeta m -> m.constructInstance(roster, destination, needsReview);
			case TestDriveMetaMission m -> m.constructInstance(roster, needsReview);
			case LandmarkMetaMission m -> m.constructInstance(roster, landmark, needsReview);
			default -> throw new IllegalStateException("Mission type: " + type + " unknown");
		};

		if (mission != null) {
			startingSettlement.getMissionControl().addMission(mission);
		}
		return mission;
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

    public Vehicle getVehicle() {
		return rover;
	}

    public void setVehicle(Vehicle rover) {
		this.rover = rover;
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
		return destination;
	}

    public void setDestinationSettlement(Settlement destinationSettlement) {
		this.destination = destinationSettlement;
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

	public void setConstructionVehicles(List<LightUtilityVehicle> constructionVehicles) {
		this.constructionVehicles = constructionVehicles;
	}

	public List<LightUtilityVehicle> getConstructionVehicles() {
		return constructionVehicles;
	}
	
	public void setExplorationSites(List<MineralSite> sites) {
		this.exploration = sites;
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

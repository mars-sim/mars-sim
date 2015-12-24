/**
 * Mars Simulation Project
 * MissionDataBean.java
 * @version 3.08 2015-07-08
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.mission.create;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.mars.ExploredLocation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.mission.*;
import org.mars_sim.msp.core.science.ScientificStudy;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.construction.ConstructionSite;
import org.mars_sim.msp.core.structure.construction.ConstructionStageInfo;
import org.mars_sim.msp.core.structure.goods.Good;
import org.mars_sim.msp.core.vehicle.GroundVehicle;
import org.mars_sim.msp.core.vehicle.LightUtilityVehicle;
import org.mars_sim.msp.core.vehicle.Rover;

import java.util.*;

/**
 * Mission data holder bean.
 */
class MissionDataBean {

	// Mission type strings.
	final static String TRAVEL_MISSION = Msg.getString("Mission.description.travelToSettlement"); //$NON-NLS-1$
	final static String EXPLORATION_MISSION = Msg.getString("Mission.description.exploration"); //$NON-NLS-1$
	final static String ICE_MISSION = Msg.getString("Mission.description.collectIce"); //$NON-NLS-1$
	final static String REGOLITH_MISSION = Msg.getString("Mission.description.collectRegolith"); //$NON-NLS-1$
	final static String RESCUE_MISSION = Msg.getString("Mission.description.rescueSalvageVehicle"); //$NON-NLS-1$
	final static String TRADE_MISSION = Msg.getString("Mission.description.trade"); //$NON-NLS-1$
	final static String MINING_MISSION = Msg.getString("Mission.description.mining"); //$NON-NLS-1$
    final static String CONSTRUCTION_MISSION = Msg.getString("Mission.description.buildingConstructionMission"); //$NON-NLS-1$
    final static String AREOLOGY_FIELD_MISSION = Msg.getString("Mission.description.areologyStudyFieldMission"); //$NON-NLS-1$
    final static String BIOLOGY_FIELD_MISSION = Msg.getString("Mission.description.biologyStudyFieldMission"); //$NON-NLS-1$
    final static String SALVAGE_MISSION = Msg.getString("Mission.description.salvageBuilding"); //$NON-NLS-1$
    final static String EMERGENCY_SUPPLY_MISSION = Msg.getString("Mission.description.emergencySupplyMission"); //$NON-NLS-1$

	// Data members.
	private String type;
	private String description;
	private Settlement startingSettlement;
	private Rover rover;
	private Collection<MissionMember> mixedMembers;
	private Collection<Person> members;
	private Settlement destinationSettlement;
	private Rover rescueRover;
	private LightUtilityVehicle luv;
	private Coordinates iceCollectionSite;
	private Coordinates regolithCollectionSite;
	private Coordinates[] explorationSites;
	private ExploredLocation miningSite;
	private Map<Good, Integer> sellGoods;
	private Map<Good, Integer> buyGoods;
    private Settlement constructionSettlement;
    private ConstructionSite constructionSite;
    private ConstructionStageInfo constructionStageInfo;
    private double constructionSiteXLoc;
    private double constructionSiteYLoc;
    private double constructionSiteFacing;
    private List<GroundVehicle> constructionVehicles;
    private Settlement salvageSettlement;
    private ConstructionSite salvageSite;
    private Building salvageBuilding;
    private List<GroundVehicle> salvageVehicles;
    private Coordinates fieldSite;
    private Person leadResearcher;
    private ScientificStudy study;
    private Map<Good, Integer> emergencyGoods;

	/**
	 * Creates a mission from the mission data.
	 */
	void createMission() {

	    Mission mission = null;
	    if (TRAVEL_MISSION.equals(type)) {
	        mission = new TravelToSettlement(mixedMembers, startingSettlement, destinationSettlement, rover,
	                description);
	    }
	    else if (RESCUE_MISSION.equals(type)) {
	        mission = new RescueSalvageVehicle(mixedMembers, startingSettlement, rescueRover, rover, description);
	    }
	    else if (ICE_MISSION.equals(type)) {
	        List<Coordinates> collectionSites = new ArrayList<Coordinates>(1);
	        collectionSites.add(iceCollectionSite);
	        mission = new CollectIce(mixedMembers, startingSettlement, collectionSites, rover, description);
	    }
	    else if (REGOLITH_MISSION.equals(type)) {
	        List<Coordinates> collectionSites = new ArrayList<Coordinates>(1);
	        collectionSites.add(regolithCollectionSite);
	        mission = new CollectRegolith(mixedMembers, startingSettlement, collectionSites, rover, description);
	    }
	    else if (EXPLORATION_MISSION.equals(type)) {
	        List<Coordinates> collectionSites = new ArrayList<Coordinates>(explorationSites.length);
	        collectionSites.addAll(Arrays.asList(explorationSites));
	        mission = new Exploration(mixedMembers, startingSettlement, collectionSites, rover, description);
	    }
	    else if (TRADE_MISSION.equals(type)) {
	        mission = new Trade(mixedMembers, startingSettlement, destinationSettlement, rover, description,
	                sellGoods, buyGoods);
	    }
	    else if (MINING_MISSION.equals(type)) {
	        mission = new Mining(mixedMembers, startingSettlement, miningSite, rover, luv, description);
	    }
	    else if (CONSTRUCTION_MISSION.equals(type)) {
	        mission = new BuildingConstructionMission(mixedMembers, constructionSettlement, constructionSite,
	                constructionStageInfo, constructionSiteXLoc, constructionSiteYLoc, constructionSiteFacing,
	                constructionVehicles);
	        // 2015-12-23 Added fireUnitUpdate()
	        //constructionSettlement.fireUnitUpdate(UnitEventType.START_MANUAL_CONSTRUCTION_WIZARD_EVENT, mission);
	    }

	    else if (AREOLOGY_FIELD_MISSION.equals(type)) {
	        mission = new AreologyStudyFieldMission(members, startingSettlement, leadResearcher, study,
	                rover, fieldSite, description);
	    }
	    else if (BIOLOGY_FIELD_MISSION.equals(type)) {
	        mission = new BiologyStudyFieldMission(members, startingSettlement, leadResearcher, study,
	                rover, fieldSite, description);
	    }
	    else if (SALVAGE_MISSION.equals(type)) {
	        mission = new BuildingSalvageMission(mixedMembers, salvageSettlement, salvageBuilding, salvageSite,
	                salvageVehicles);
	    }
	    else if (EMERGENCY_SUPPLY_MISSION.equals(type)) {
	        mission = new EmergencySupplyMission(members, startingSettlement, destinationSettlement,
	                emergencyGoods, rover, description);
	    }
	    else throw new IllegalStateException("mission type: " + type + " unknown");

	    MissionManager manager = Simulation.instance().getMissionManager();
	    manager.addMission(mission);
	}

	/**
	 * Gets mission types.
	 * @return array of mission type strings.
	 */
	static String[] getMissionTypes() {
		String[] result = { TRAVEL_MISSION, EXPLORATION_MISSION, ICE_MISSION, REGOLITH_MISSION,
				AREOLOGY_FIELD_MISSION, BIOLOGY_FIELD_MISSION, RESCUE_MISSION, TRADE_MISSION,
                MINING_MISSION, CONSTRUCTION_MISSION, SALVAGE_MISSION, EMERGENCY_SUPPLY_MISSION };
		return result;
	}

	/**
	 * Gets mission description based on a mission type.
	 * @param missionType the mission type.
	 * @return the mission description.
	 */
	static String getMissionDescription(String missionType) {
		String result = "";
		if (missionType.equals(TRAVEL_MISSION)) {
		    result = TravelToSettlement.DEFAULT_DESCRIPTION;
		}
		else if (missionType.equals(EXPLORATION_MISSION)) {
		    result = Exploration.DEFAULT_DESCRIPTION;
		}
		else if (missionType.equals(ICE_MISSION)) {
		    result = CollectIce.DEFAULT_DESCRIPTION;
		}
		else if (missionType.equals(REGOLITH_MISSION)) {
		    result = CollectRegolith.DEFAULT_DESCRIPTION;
		}
		else if (missionType.equals(RESCUE_MISSION)) {
		    result = RescueSalvageVehicle.DEFAULT_DESCRIPTION;
		}
		else if (missionType.equals(TRADE_MISSION)) {
		    result = Trade.DEFAULT_DESCRIPTION;
		}
		else if (missionType.equals(MINING_MISSION)) {
		    result = Mining.DEFAULT_DESCRIPTION;
		}
        else if (missionType.equals(CONSTRUCTION_MISSION)) {
            result = BuildingConstructionMission.DEFAULT_DESCRIPTION;
        }
        else if (missionType.equals(AREOLOGY_FIELD_MISSION)) {
            result = AreologyStudyFieldMission.DEFAULT_DESCRIPTION;
        }
        else if (missionType.equals(BIOLOGY_FIELD_MISSION)) {
            result = BiologyStudyFieldMission.DEFAULT_DESCRIPTION;
        }
        else if (missionType.equals(SALVAGE_MISSION)) {
            result = BuildingSalvageMission.DEFAULT_DESCRIPTION;
        }
        else if (missionType.equals(EMERGENCY_SUPPLY_MISSION)) {
            result = EmergencySupplyMission.DEFAULT_DESCRIPTION;
        }
		return result;
	}

	/**
	 * Gets the mission type.
	 * @return type.
	 */
	String getType() {
		return type;
	}

	/**
	 * Sets the mission type.
	 * @param type the mission type.
	 */
	void setType(String type) {
		this.type = type;
	}

	/**
	 * Gets the mission description.
	 * @return description.
	 */
	String getDescription() {
		return description;
	}

	/**
	 * Sets the mission description.
	 * @param description the mission description.
	 */
	void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Gets the starting settlement.
	 * @return settlement.
	 */
	Settlement getStartingSettlement() {
		return startingSettlement;
	}

	/**
	 * Sets the starting settlement.
	 * @param startingSettlement starting settlement.
	 */
	void setStartingSettlement(Settlement startingSettlement) {
		this.startingSettlement = startingSettlement;
	}

	/**
	 * Gets the rover.
	 * @return rover.
	 */
	Rover getRover() {
		return rover;
	}

	/**
	 * Sets the rover.
	 * @param rover the rover.
	 */
	void setRover(Rover rover) {
		this.rover = rover;
	}

	/**
	 * Gets the mission members.
	 * @return the members.
	 */
	Collection<Person> getMembers() {
		return members;

	}

	Collection<MissionMember> getMixedMembers() {
		return mixedMembers;
	}
	/**
	 * Sets the mission members.
	 * @param members the members.
	 */
	void setMembers(Collection<Person> members) {
		this.members = members;
	}

	void setMixedMembers(Collection<MissionMember> mixedMembers) {
		this.mixedMembers = mixedMembers;
	}
	/**
	 * Gets the destination settlement.
	 * @return destination settlement.
	 */
	Settlement getDestinationSettlement() {
		return destinationSettlement;
	}

	/**
	 * Sets the destination settlement.
	 * @param destinationSettlement the destination settlement.
	 */
	void setDestinationSettlement(Settlement destinationSettlement) {
		this.destinationSettlement = destinationSettlement;
	}

	/**
	 * Gets the rescue rover.
	 * @return the rescue rover.
	 */
	Rover getRescueRover() {
		return rescueRover;
	}

	/**
	 * Sets the rescue rover.
	 * @param rescueRover the rescue rover.
	 */
	void setRescueRover(Rover rescueRover) {
		this.rescueRover = rescueRover;
	}

	/**
	 * Gets the ice collection site.
	 * @return ice collection site.
	 */
	Coordinates getIceCollectionSite() {
		return iceCollectionSite;
	}

	/**
	 * Sets the ice collection site.
	 * @param iceCollectionSite the ice collection site.
	 */
	void setIceCollectionSite(Coordinates iceCollectionSite) {
		this.iceCollectionSite = iceCollectionSite;
	}

	/**
	 * Gets the regolith collection site.
	 * @return regolith collection site.
	 */
	Coordinates getRegolithCollectionSite() {
		return regolithCollectionSite;
	}

	/**
	 * Sets the regolith collection site.
	 * @param regolithCollectionSite the regolith collection site.
	 */
	void setRegolithCollectionSite(Coordinates regolithCollectionSite) {
		this.regolithCollectionSite = regolithCollectionSite;
	}

	/**
	 * Gets the exploration sites.
	 * @return exploration sites.
	 */
	Coordinates[] getExplorationSites() {
		return explorationSites;
	}

	/**
	 * Sets the exploration sites.
	 * @param explorationSites the exploration sites.
	 */
	void setExplorationSites(Coordinates[] explorationSites) {
		this.explorationSites = explorationSites;
	}

	/**
	 * Gets the sell goods.
	 * @return map of goods and integer amounts.
	 */
	Map<Good, Integer> getSellGoods() {
		return sellGoods;
	}

	/**
	 * Sets the sell goods.
	 * @param sellGoods map of goods and integer amounts.
	 */
	void setSellGoods(Map<Good, Integer> sellGoods) {
		this.sellGoods = sellGoods;
	}

	/**
	 * Gets the buy goods.
	 * @return map of goods and integer amounts.
	 */
	Map<Good, Integer> getBuyGoods() {
		return buyGoods;
	}

	/**
	 * Sets the buy goods.
	 * @param sellGoods map of goods and integer amounts.
	 */
	void setBuyGoods(Map<Good, Integer> buyGoods) {
		this.buyGoods = buyGoods;
	}

	/**
	 * Gets the light utility vehicle.
	 * @return light utility vehicle
	 */
	LightUtilityVehicle getLUV() {
		return luv;
	}

	/**
	 * Sets the light utility vehicle
	 * @param luv the light utility vehicle
	 */
	void setLUV(LightUtilityVehicle luv) {
		this.luv = luv;
	}

	/**
	 * Gets the mining site.
	 * @return mining site.
	 */
	ExploredLocation getMiningSite() {
		return miningSite;
	}

	/**
	 * Sets the mining site.
	 * @param miningSite the mining site.
	 */
	void setMiningSite(ExploredLocation miningSite) {
		this.miningSite = miningSite;
	}

    /**
     * Gets the salvage settlement.
     * @return settlement.
     */
    Settlement getSalvageSettlement() {
        return salvageSettlement;
    }

    /**
     * Sets the salvage settlement.
     * @param salvageSettlement the salvage settlement.
     */
    void setSalvageSettlement(Settlement salvageSettlement) {
        this.salvageSettlement = salvageSettlement;
    }

    /**
     * Gets the salvage site.
     * @return salvage site.
     */
    ConstructionSite getSalvageSite() {
        return salvageSite;
    }

    /**
     * Sets the salvage site.
     * @param salvageSite the salvage site.
     */
    void setSalvageSite(ConstructionSite salvageSite) {
        this.salvageSite = salvageSite;
    }

    /**
     * Gets the salvage building.
     * @return salvage building.
     */
    Building getSalvageBuilding() {
        return salvageBuilding;
    }

    /**
     * Sets the salvage building.
     * @param salvageBuilding the salvage building.
     */
    void setSalvageBuilding(Building salvageBuilding) {
        this.salvageBuilding = salvageBuilding;
    }

    /**
     * Gets the salvage vehicles.
     * @return list of ground vehicles.
     */
    List<GroundVehicle> getSalvageVehicles() {
        return salvageVehicles;
    }

    /**
     * Sets the salvage vehicles.
     * @param salvageVehicles list of ground vehicles.
     */
    void setSalvageVehicles(List<GroundVehicle> salvageVehicles) {
        this.salvageVehicles = salvageVehicles;
    }

    /**
     * Gets the construction settlement.
     * @return settlement.
     */
    Settlement getConstructionSettlement() {
        return constructionSettlement;
    }

    /**
     * Sets the construction settlement.
     * @param constructionSettlement the construction settlement.
     */
    void setConstructionSettlement(Settlement constructionSettlement) {
        this.constructionSettlement = constructionSettlement;
    }

    /**
     * Gets the construction site.
     * @return construction site.
     */
    ConstructionSite getConstructionSite() {
        return constructionSite;
    }

    /**
     * Sets the construction site.
     * @param constructionSite the construction site.
     */
    void setConstructionSite(ConstructionSite constructionSite) {
        this.constructionSite = constructionSite;
    }

    /**
     * Gets the construction stage info.
     * @return construction stage info.
     */
    ConstructionStageInfo getConstructionStageInfo() {
        return constructionStageInfo;
    }

    /**
     * Sets the construction stage info.
     * @param constructionStageInfo the construction stage info.
     */
    void setConstructionStageInfo(ConstructionStageInfo constructionStageInfo) {
        this.constructionStageInfo = constructionStageInfo;
    }

    /**
     * Gets the construction site X location.
     * @return X location (meters).
     */
    double getConstructionSiteXLocation() {
        return constructionSiteXLoc;
    }

    /**
     * Sets the construction site X location.
     * @param constructionSiteXLoc X location (meters).
     */
    void setConstructionSiteXLocation(double constructionSiteXLoc) {
        this.constructionSiteXLoc = constructionSiteXLoc;
    }

    /**
     * Gets the construction site Y location.
     * @return Y location (meters).
     */
    double getConstructionSiteYLocation() {
        return constructionSiteYLoc;
    }

    /**
     * Sets the construction site Y location.
     * @param constructionSiteYLoc Y Location (meters).
     */
    void setConstructionSiteYLocation(double constructionSiteYLoc) {
        this.constructionSiteYLoc = constructionSiteYLoc;
    }

    /**
     * Gets the construction site facing.
     * @return the construction site facing (degrees clockwise from North).
     */
    double getConstructionSiteFacing() {
        return constructionSiteFacing;
    }

    /**
     * Sets the construction site facing.
     * @param constructionSiteFacing facing (degrees clockwise from North).
     */
    void setConstructionSiteFacing(double constructionSiteFacing) {
        this.constructionSiteFacing = constructionSiteFacing;
    }

    /**
     * Gets the construction vehicles.
     * @return list of ground vehicles.
     */
    List<GroundVehicle> getConstructionVehicles() {
        return constructionVehicles;
    }

    /**
     * Sets the construction vehicles.
     * @param constructionVehicles list of ground vehicles.
     */
    void setConstructionVehicles(List<GroundVehicle> constructionVehicles) {
        this.constructionVehicles = constructionVehicles;
    }

    /**
     * Gets the field site.
     * @return field site location.
     */
    Coordinates getFieldSite() {
        return fieldSite;
    }

    /**
     * Sets the field site.
     * @param fieldSite the field site location.
     */
    void setFieldSite(Coordinates fieldSite) {
        this.fieldSite = fieldSite;
    }

    /**
     * Gets the lead researcher for the mission.
     * @return lead researcher.
     */
    Person getLeadResearcher() {
        return leadResearcher;
    }

    /**
     * Sets the lead researcher for the mission.
     * @param leadResearcher the lead researcher.
     */
    void setLeadResearcher(Person leadResearcher) {
        this.leadResearcher = leadResearcher;
    }

    /**
     * Gets the scientific study.
     * @return the scientific study.
     */
    ScientificStudy getStudy() {
        return study;
    }

    /**
     * Sets the scientific study.
     * @param study the scientific study.
     */
    void setScientificStudy(ScientificStudy study) {
        this.study = study;
    }

    /**
     * Gets the emergency resources map.
     * @return map of resources and amounts (kg).
     */
    Map<Good, Integer> getEmergencyGoods() {
        return emergencyGoods;
    }

    /**
     * Sets the emergency resources.
     * @param emergencyResources map of resources and amounts (kg).
     */
    void setEmergencyGoods(Map<Good, Integer> emergencyGoods) {
        this.emergencyGoods = emergencyGoods;
    }
}
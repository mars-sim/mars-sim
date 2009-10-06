/**
 * Mars Simulation Project
 * MissionDataBean.java
 * @version 2.87 2009-10-01
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard.tool.mission.create;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.mars_sim.msp.simulation.Coordinates;
import org.mars_sim.msp.simulation.Simulation;
import org.mars_sim.msp.simulation.mars.ExploredLocation;
import org.mars_sim.msp.simulation.person.Person;
import org.mars_sim.msp.simulation.person.ai.mission.AreologyStudyFieldMission;
import org.mars_sim.msp.simulation.person.ai.mission.BiologyStudyFieldMission;
import org.mars_sim.msp.simulation.person.ai.mission.BuildingConstructionMission;
import org.mars_sim.msp.simulation.person.ai.mission.CollectIce;
import org.mars_sim.msp.simulation.person.ai.mission.CollectRegolith;
import org.mars_sim.msp.simulation.person.ai.mission.Exploration;
import org.mars_sim.msp.simulation.person.ai.mission.Mining;
import org.mars_sim.msp.simulation.person.ai.mission.Mission;
import org.mars_sim.msp.simulation.person.ai.mission.MissionException;
import org.mars_sim.msp.simulation.person.ai.mission.MissionManager;
import org.mars_sim.msp.simulation.person.ai.mission.RescueSalvageVehicle;
import org.mars_sim.msp.simulation.person.ai.mission.Trade;
import org.mars_sim.msp.simulation.person.ai.mission.TravelToSettlement;
import org.mars_sim.msp.simulation.science.ScientificStudy;
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.simulation.structure.construction.ConstructionSite;
import org.mars_sim.msp.simulation.structure.construction.ConstructionStageInfo;
import org.mars_sim.msp.simulation.structure.goods.Good;
import org.mars_sim.msp.simulation.vehicle.GroundVehicle;
import org.mars_sim.msp.simulation.vehicle.LightUtilityVehicle;
import org.mars_sim.msp.simulation.vehicle.Rover;

/**
 * Mission data holder bean.
 */
class MissionDataBean {

	// Mission type strings.
	final static String TRAVEL_MISSION = "Travel to Settlement";
	final static String EXPLORATION_MISSION = "Mineral Exploration";
	final static String ICE_MISSION = "Ice Prospecting";
	final static String REGOLITH_MISSION = "Regolith Prospecting";
	final static String RESCUE_MISSION = "Rescue/Salvage Vehicle";
	final static String TRADE_MISSION = "Trade";
	final static String MINING_MISSION = "Mining";
    final static String CONSTRUCTION_MISSION = "Building Construction";
    final static String AREOLOGY_FIELD_MISSION = "Areology Study Field Mission";
    final static String BIOLOGY_FIELD_MISSION = "Biology Study Field Mission";

	// Data members.
	private String type;
	private String description;
	private Settlement startingSettlement;
	private Rover rover;
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
    private List<GroundVehicle> constructionVehicles;
    private Coordinates fieldSite;
    private Person leadResearcher;
    private ScientificStudy study;
	
	/**
	 * Creates a mission from the mission data.
	 */
	void createMission() {
		try {
			Mission mission = null;
			if (TRAVEL_MISSION.equals(type)) {
				mission = new TravelToSettlement(members, startingSettlement, destinationSettlement, rover, 
                        description);
            }
			else if (RESCUE_MISSION.equals(type)) {
				mission = new RescueSalvageVehicle(members, startingSettlement, rescueRover, rover, description);
            }
			else if (ICE_MISSION.equals(type)) {
				List<Coordinates> collectionSites = new ArrayList<Coordinates>(1);
				collectionSites.add(iceCollectionSite);
				mission = new CollectIce(members, startingSettlement, collectionSites, rover, description);
			}
			else if (REGOLITH_MISSION.equals(type)) {
				List<Coordinates> collectionSites = new ArrayList<Coordinates>(1);
				collectionSites.add(regolithCollectionSite);
				mission = new CollectRegolith(members, startingSettlement, collectionSites, rover, description);
			}
			else if (EXPLORATION_MISSION.equals(type)) {
				List<Coordinates> collectionSites = new ArrayList<Coordinates>(explorationSites.length);
				for (int x = 0; x < explorationSites.length; x++) collectionSites.add(explorationSites[x]);
				mission = new Exploration(members, startingSettlement, collectionSites, rover, description);
			}
			else if (TRADE_MISSION.equals(type)) {
				mission = new Trade(members, startingSettlement, destinationSettlement, rover, description, 
                        sellGoods, buyGoods);
            }
			else if (MINING_MISSION.equals(type)) {
				mission = new Mining(members, startingSettlement, miningSite, rover, luv, description);
            }
            else if (CONSTRUCTION_MISSION.equals(type)) {
                mission = new BuildingConstructionMission(members, constructionSettlement, constructionSite, 
                        constructionStageInfo, constructionVehicles);
            }
            else if (AREOLOGY_FIELD_MISSION.equals(type)) {
                mission = new AreologyStudyFieldMission(members, startingSettlement, leadResearcher, study, 
                        rover, fieldSite, description);
            }
            else if (BIOLOGY_FIELD_MISSION.equals(type)) {
                mission = new BiologyStudyFieldMission(members, startingSettlement, leadResearcher, study, 
                        rover, fieldSite, description);
            }
            else throw new MissionException(null, "mission type: " + type + " unknown");
		
			MissionManager manager = Simulation.instance().getMissionManager();
			manager.addMission(mission);
		}
		catch (MissionException e) {
			e.printStackTrace(System.err);
		}
	}
	
	/**
	 * Gets mission types.
	 * @return array of mission type strings.
	 */
	static final String[] getMissionTypes() {
		String[] result = { TRAVEL_MISSION, EXPLORATION_MISSION, ICE_MISSION, REGOLITH_MISSION, 
				AREOLOGY_FIELD_MISSION, BIOLOGY_FIELD_MISSION, RESCUE_MISSION, TRADE_MISSION, 
                MINING_MISSION, CONSTRUCTION_MISSION };
		return result;
	}
	
	/**
	 * Gets mission description based on a mission type.
	 * @param missionType the mission type.
	 * @return the mission description.
	 */
	static final String getMissionDescription(String missionType) {
		String result = "";
		if (missionType.equals(TRAVEL_MISSION)) result = TravelToSettlement.DEFAULT_DESCRIPTION;
		else if (missionType.equals(EXPLORATION_MISSION)) result = Exploration.DEFAULT_DESCRIPTION;
		else if (missionType.equals(ICE_MISSION)) result = CollectIce.DEFAULT_DESCRIPTION;
		else if (missionType.equals(REGOLITH_MISSION)) result = CollectRegolith.DEFAULT_DESCRIPTION;
		else if (missionType.equals(RESCUE_MISSION)) result = RescueSalvageVehicle.DEFAULT_DESCRIPTION;
		else if (missionType.equals(TRADE_MISSION)) result = Trade.DEFAULT_DESCRIPTION;
		else if (missionType.equals(MINING_MISSION)) result = Mining.DEFAULT_DESCRIPTION;
        else if (missionType.equals(CONSTRUCTION_MISSION)) 
            result = BuildingConstructionMission.DEFAULT_DESCRIPTION;
        else if (missionType.equals(AREOLOGY_FIELD_MISSION)) 
            result = AreologyStudyFieldMission.DEFAULT_DESCRIPTION;
        else if (missionType.equals(BIOLOGY_FIELD_MISSION)) 
            result = BiologyStudyFieldMission.DEFAULT_DESCRIPTION;
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
	
	/**
	 * Sets the mission members.
	 * @param members the members.
	 */
	void setMembers(Collection<Person> members) {
		this.members = members;
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
}
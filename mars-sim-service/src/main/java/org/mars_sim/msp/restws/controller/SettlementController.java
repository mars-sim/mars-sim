package org.mars_sim.msp.restws.controller;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.mission.MissionManager;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.restws.mapper.BuildingMapper;
import org.mars_sim.msp.restws.mapper.InventoryMapper;
import org.mars_sim.msp.restws.mapper.MissionSummaryMapper;
import org.mars_sim.msp.restws.mapper.PersonSummaryMapper;
import org.mars_sim.msp.restws.mapper.RobotSummaryMapper;
import org.mars_sim.msp.restws.mapper.SettlementDetailsMapper;
import org.mars_sim.msp.restws.mapper.SettlementSummaryMapper;
import org.mars_sim.msp.restws.mapper.VehicleSummaryMapper;
import org.mars_sim.msp.restws.model.BuildingDetails;
import org.mars_sim.msp.restws.model.MissionSummary;
import org.mars_sim.msp.restws.model.PagedList;
import org.mars_sim.msp.restws.model.PersonSummary;
import org.mars_sim.msp.restws.model.RobotSummary;
import org.mars_sim.msp.restws.model.SettlementDetails;
import org.mars_sim.msp.restws.model.SettlementSummary;
import org.mars_sim.msp.restws.model.StoredAmount;
import org.mars_sim.msp.restws.model.StoredItem;
import org.mars_sim.msp.restws.model.VehicleSummary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;

/**
 * This provides a REST controller that deliveries information on Settlement entities.
 */
@RestController()
public class SettlementController extends BaseController {
	private static Log log = LogFactory.getLog(SettlementController.class);

	@Autowired
	private UnitManager manager; 
	
	@Autowired
	private MissionManager missionManager;
	
	@Autowired
    private SettlementSummaryMapper mapper;
	
	@Autowired
	private SettlementDetailsMapper settlementMapper;
	
	@Autowired
	private PersonSummaryMapper personMapper;

	@Autowired
	private VehicleSummaryMapper vehiclesMapper;

	@Autowired
	private BuildingMapper buildingMapper;

	@Autowired
	private InventoryMapper inventoryMapper;

	@Autowired
	private MissionSummaryMapper missionMapper;
	
	@Autowired
	private RobotSummaryMapper robotMapper;
	
	/**
	 * Need a better way to find settlements
	 * @param settlementId
	 * @return Found Settlement
	 */
	private Settlement findSettlement(int settlementId) {
		// Need a better way to find people
		Iterator<Settlement> it = manager.getSettlements().iterator();
		while (it.hasNext()) {
			Settlement item = it.next();
			if (item.getIdentifier() == settlementId) {
				return item;
			}
		}
		log.error("Settlement not found: id=" + settlementId);
		throw new NotFoundException("Settlement", settlementId);
	}

	@ApiOperation(value = "get All Settlement", nickname = "getSettlements")
    @RequestMapping(method=RequestMethod.GET, path="/settlements", produces = "application/json")
    public PagedList<SettlementSummary> getSettlements(@RequestParam(value="page", defaultValue="1") int page,
    								   @RequestParam(value="size", defaultValue="10") int pageSize) {
    	Collection<Settlement> allSettlements = manager.getSettlements();
    	List<Settlement> filtered = filter(allSettlements, page, pageSize);
    	
		return new PagedList<SettlementSummary>(mapper.settlementsToSettlementSummarys(filtered),
				 								page, pageSize, allSettlements.size());
    }

	@ApiOperation(value = "get Settlement by Id", nickname = "getSettlement")
	@RequestMapping(method = RequestMethod.GET, path="/settlements/{id}", produces = "application/json")
    public SettlementDetails getSettlement(@PathVariable(value="id") int settlementId) {
		
        return settlementMapper.settlementToSettlementDetails(findSettlement(settlementId));
    }
	
	@ApiOperation(value = "get Settlement Persons", nickname = "getSettlementPersons")
	@RequestMapping(method = RequestMethod.GET, path="/settlements/{id}/persons", produces = "application/json")
    public List<PersonSummary> getPersons(@PathVariable(value="id") int settlementId) {
		
        Settlement found = findSettlement(settlementId);
        
        return personMapper.personsToPersonSummarys(
        		new ArrayList<Person>(found.getIndoorPeople()));
    }
	
	@ApiOperation(value = "get Settlement Vehicles", nickname = "getSettlementVehicles")
	@RequestMapping(method = RequestMethod.GET, path="/settlements/{id}/vehicles", produces = "application/json")
    public List<VehicleSummary> getVehicles(@PathVariable(value="id") int settlementId) {
		
        Settlement found = findSettlement(settlementId);
        
        return vehiclesMapper.vehiclesToVehicleSummarys(
        		new ArrayList<Vehicle>(found.getParkedVehicles()));
    }

	@ApiOperation(value = "get Settlement Building", nickname = "getSettlementBuildings")
	@RequestMapping(method = RequestMethod.GET, path="/settlements/{id}/buildings", produces = "application/json")
    public List<BuildingDetails> getBuildings(@PathVariable(value="id") int settlementId) {
		
        Settlement found = findSettlement(settlementId);
        
        return buildingMapper.buildingtoBuildingfDetails(found.getBuildingManager().getBuildingsNickNames());
    }
	

	@ApiOperation(value = "get Settlement Missions", nickname = "getSettlementMissions")
	@RequestMapping(method = RequestMethod.GET, path="/settlements/{id}/missions", produces = "application/json")
    public List<MissionSummary> getMissions(@PathVariable(value="id") int settlementId) {
		
        Settlement found = findSettlement(settlementId);
        
        return missionMapper.missionsToMissionSummarys(missionManager.getMissionsForSettlement(found));
    }
	
	@ApiOperation(value = "get Settlement Resources", nickname = "getSettlementResources")
	@RequestMapping(method = RequestMethod.GET, path="/settlements/{id}/resources", produces = "application/json")
    public List<StoredAmount> getResources(@PathVariable(value="id") int settlementId) {
		
        Settlement found = findSettlement(settlementId);
        
        return inventoryMapper.getAmounts(found.getInventory());
    }
	
	@ApiOperation(value = "get Settlement Items", nickname = "getSettlementItems")
	@RequestMapping(method = RequestMethod.GET, path="/settlements/{id}/items", produces = "application/json")
    public List<StoredItem> getItems(@PathVariable(value="id") int settlementId) {
		
        Settlement found = findSettlement(settlementId);
        
        return inventoryMapper.getItems(found.getInventory());
    }

	@ApiOperation(value = "get Settlement Robots", nickname = "getSettlementRobots")
	@RequestMapping(method = RequestMethod.GET, path="/settlements/{id}/robots", produces = "application/json")
    public List<RobotSummary> getRobots(@PathVariable(value="id") int settlementId) {
		
        Settlement found = findSettlement(settlementId);
        
        return robotMapper.robotsToRobotSummarys(found.getRobots());
    }
}

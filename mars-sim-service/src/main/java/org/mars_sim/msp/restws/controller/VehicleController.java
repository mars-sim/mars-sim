package org.mars_sim.msp.restws.controller;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.restws.mapper.InventoryMapper;
import org.mars_sim.msp.restws.mapper.PersonSummaryMapper;
import org.mars_sim.msp.restws.mapper.VehicleDetailsMapper;
import org.mars_sim.msp.restws.mapper.VehicleSummaryMapper;
import org.mars_sim.msp.restws.model.PagedList;
import org.mars_sim.msp.restws.model.PersonSummary;
import org.mars_sim.msp.restws.model.StoredAmount;
import org.mars_sim.msp.restws.model.StoredItem;
import org.mars_sim.msp.restws.model.VehicleDetails;
import org.mars_sim.msp.restws.model.VehicleSummary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;


@RestController()
public class VehicleController extends BaseController {
	private static Log log = LogFactory.getLog(VehicleController.class);

	@Autowired
    private UnitManager vehicleManager;
	
	@Autowired
	private VehicleDetailsMapper detailsMapper;

	@Autowired
	private InventoryMapper inventoryMapper;

	@Autowired
	private VehicleSummaryMapper summaryMapper;

	@Autowired
	private PersonSummaryMapper personSummaryMapper;
	
	/**
	 * Find a Vehicle  entity by the unique identifier
	 * @param vehicleId
	 * @return
	 */
	private Vehicle getVehicle(int vehicleId) {
		// Need a better way to find people
		Iterator<Vehicle> it = vehicleManager.getVehicles().iterator();
		while (it.hasNext()) {
			Vehicle v = it.next();
			if (v.getIdentifier() == vehicleId) {
				return v;
			}
		}
		log.error("There is no Vehicle with id=" + vehicleId);
		
		throw new NotFoundException("Vehicle", vehicleId);
	}
	
	
	@ApiOperation(value = "get Vehicle by Id", nickname = "getVehicle")
	@RequestMapping(method = RequestMethod.GET, path="/vehicles/{id}", produces = "application/json")
    public VehicleDetails  getVehicleDetails(@PathVariable(value="id") int vehicleId) {
		
        return detailsMapper.vehicleToVehicleDetails(getVehicle(vehicleId));
    }
	
	@ApiOperation(value = "get All Vehicles", nickname = "getVehicles")
    @RequestMapping(method=RequestMethod.GET, path="/vehicles", produces = "application/json")
    public PagedList<VehicleSummary> getVehicles(@RequestParam(value="page", defaultValue="1") int page,
    								   @RequestParam(value="size", defaultValue="10") int pageSize) {
		Collection<Vehicle> allVehicles = vehicleManager.getVehicles();
    	List<Vehicle> filtered = filter(allVehicles, page, pageSize); 
		
		return new PagedList<VehicleSummary>(summaryMapper.vehiclesToVehicleSummarys(filtered),
											page, pageSize, allVehicles.size());
    }
	
	@ApiOperation(value = "get Vehicle Resources", nickname = "getVehicleResources")
	@RequestMapping(method = RequestMethod.GET, path="/vehicles/{id}/resources", produces = "application/json")
    public List<StoredAmount> getResources(@PathVariable(value="id") int vehicleId) {
		        
        return inventoryMapper.getAmounts(getVehicle(vehicleId).getInventory());
    }
	
	@ApiOperation(value = "get Vehicle Items", nickname = "getVehicleItems")
	@RequestMapping(method = RequestMethod.GET, path="/vehicles/{id}/items", produces = "application/json")
    public List<StoredItem> getItems(@PathVariable(value="id") int vehicleId) {
		
        return inventoryMapper.getItems(getVehicle(vehicleId).getInventory());
    }
	
	@ApiOperation(value = "get Vehicle Persons", nickname = "getVehiclePersons")
	@RequestMapping(method = RequestMethod.GET, path="/vehicles/{id}/persons", produces = "application/json")
    public List<PersonSummary> getPersons(@PathVariable(value="id") int vehicleId) {

        // THis is wrong and should be using Crewable
        return personSummaryMapper.personsToPersonSummarys(
        		new ArrayList<Person>(getVehicle(vehicleId).getAffectedPeople()));
    }
}

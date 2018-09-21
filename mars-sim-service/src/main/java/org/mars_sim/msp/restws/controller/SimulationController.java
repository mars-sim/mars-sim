package org.mars_sim.msp.restws.controller;

import java.util.ArrayList;
import java.util.List;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.events.HistoricalEvent;
import org.mars_sim.msp.core.events.HistoricalEventManager;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.time.MasterClock;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.restws.model.EntityReference;
import org.mars_sim.msp.restws.model.EventDTO;
import org.mars_sim.msp.restws.model.PagedList;
import org.mars_sim.msp.restws.model.SimulationDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;

@RestController
public class SimulationController {

	@Autowired
	private Simulation simulation;
	
	@ApiOperation(value = "get Details", nickname = "getDetails")
	@RequestMapping(method = RequestMethod.GET, path="/simulation", produces = "application/json")
    public SimulationDetails getDetails() {
        MasterClock clock = simulation.getMasterClock();
        
    	
    	return new SimulationDetails(clock.getUpTimer().getUptime(),
    								 clock.getMarsClock().getDateTimeStamp(),
    								 clock.isPaused());
    }

	@ApiOperation(value = "Pause/Unpause simulation", nickname = "pauseSimulation")
	@RequestMapping(method = RequestMethod.PUT, path="/simulation/pause", produces = "application/json")
	public SimulationDetails switchPaused() {
        MasterClock clock = simulation.getMasterClock();
        
        //Flip paused state
        boolean isPaused = clock.isPaused();
        clock.setPaused(!isPaused, false);
		
		return getDetails();
		
	}

	/**
	 * This creates a list of historical events.
	 * @param first Index of first event
	 * @param limit Maximum of events to return
	 * @param typeFilter Optional filter on the types of events
	 * @return
	 */
	@ApiOperation(value = "get latest events", nickname = "getEvents")
    @RequestMapping(method=RequestMethod.GET, path="/simulation/events", produces = "application/json")
    public PagedList<EventDTO> getEvents(@RequestParam(value="page", defaultValue="1") int page,
    								     @RequestParam(value="pageSize", defaultValue="10") int pageSize,
    								     @RequestParam(value="type", defaultValue="*") String typeFilter) {
		HistoricalEventManager manager = simulation.getEventManager();
		List<EventDTO> events = new ArrayList<EventDTO>();
		
		// Have to built the DTO list manually to handle the source property in the Historical Event
//		for (int idx = (page-1) * pageSize; (idx < manager.size()) && (events.size() < pageSize); idx++) {
//			HistoricalEvent event = manager.getEvent(idx);
//			Object source = event.getSource();
//			EntityReference entityRef = null;
//			String entityType = null;
//
//			// A bit messy this code
//			if (source instanceof Vehicle) {
//				Unit unit = (Unit) source;
//				entityRef = new EntityReference(unit.getIdentifier(), unit.getName());
//				entityType = "vehicle";
//			}
//			else if (source instanceof Person) {
//				Unit unit = (Unit) source;
//				entityRef = new EntityReference(unit.getIdentifier(), unit.getName());
//				entityType = "person";
//			}
//			else if (source instanceof Settlement) {
//				Unit unit = (Unit) source;
//				entityRef = new EntityReference(unit.getIdentifier(), unit.getName());
//				entityType = "settlement";
//			}
//			else if (source instanceof Mission) {
//				Mission unit = (Mission) source;
//				entityRef = new EntityReference(unit.getIdentifier(), unit.getName());
//				entityType = "mission";
//			}
//			
//			events.add(new EventDTO(idx, event.getType().getName(), event.getDescription(), event.getTimestamp().getDateTimeStamp(),
//					   entityRef, entityType));
//		}
		
		return new PagedList<EventDTO>(events, page, events.size(), manager.getEvents().size());
	}
	
}

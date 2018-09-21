package org.mars_sim.msp.restws.controller;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.mission.AreologyStudyFieldMission;
import org.mars_sim.msp.core.person.ai.mission.BiologyStudyFieldMission;
import org.mars_sim.msp.core.person.ai.mission.EmergencySupplyMission;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionManager;
import org.mars_sim.msp.core.person.ai.mission.RescueSalvageVehicle;
import org.mars_sim.msp.core.person.ai.mission.Trade;
import org.mars_sim.msp.core.person.ai.mission.TravelToSettlement;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.restws.mapper.MissionDetailsMapper;
import org.mars_sim.msp.restws.mapper.MissionSummaryMapper;
import org.mars_sim.msp.restws.mapper.PersonSummaryMapper;
import org.mars_sim.msp.restws.mapper.RobotSummaryMapper;
import org.mars_sim.msp.restws.model.MissionDetails;
import org.mars_sim.msp.restws.model.MissionSummary;
import org.mars_sim.msp.restws.model.PagedList;
import org.mars_sim.msp.restws.model.PersonSummary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;

/**
 * This provides a REST controller that deliveries information on Mission entities.
 * The missions entity is small enough such that it only needs a Summary DTO.
 */
@RestController()
public class MissionController extends BaseController {

	private Log log = LogFactory.getLog(MissionController.class);
	
	@Autowired
	private MissionManager manager;
	
	@Autowired
    private MissionSummaryMapper mapper;
	
	@Autowired
	private MissionDetailsMapper detailsMapper;
	
	@Autowired
	private PersonSummaryMapper personMapper;
	
	@Autowired
	private RobotSummaryMapper robotMapper;
	
	/**
	 * Need a better way to find missions
	 * @param missionId
	 * @return Found Mission
	 */
	private Mission findMission(int missionId) {
		// Need a better way to find people
		Iterator<Mission> it = manager.getMissions().iterator();
		while (it.hasNext()) {
			Mission item = it.next();
			if (item.getIdentifier() == missionId) {
				return item;
			}
		}
		
		log.error("Mission " + missionId + " not found");
		throw new NotFoundException("Missions", missionId);
	}

	@ApiOperation(value = "get All Missions", nickname = "getMissions")
    @RequestMapping(method=RequestMethod.GET, path="/missions", produces = "application/json")
    public PagedList<MissionSummary> getMissions(@RequestParam(value="page", defaultValue="1") int page,
    								   @RequestParam(value="size", defaultValue="10") int pageSize) {
    	List<Mission> allMissions = manager.getMissions();
		List<Mission> filtered = filter(allMissions, page, pageSize);
    	
    	
		return new PagedList<MissionSummary>(mapper.missionsToMissionSummarys(filtered),
				 								page, pageSize, allMissions.size());
    }

	/**
	 * This method converts the various Mission subtypes into the MissionDetails object.
	 * @param missionId
	 * @return
	 */
	@ApiOperation(value = "get Mission by Id", nickname = "getMission")
	@RequestMapping(method = RequestMethod.GET, path="/missions/{id}", produces = "application/json")
    public MissionDetails getMission(@PathVariable(value="id") int missionId) {
		
		Mission found = findMission(missionId);
		MissionDetails details = null;
				
		log.debug("Found Mission of type " + found.getClass().getName());
		
		// The most specific Class must appear first
		if (AreologyStudyFieldMission.class.isInstance(found)) {
			details = detailsMapper.areologyStudyFieldMissionToMissionDetails((AreologyStudyFieldMission)found);
		}
		else if (BiologyStudyFieldMission.class.isInstance(found)) {
			details = detailsMapper.biologyStudyFieldMissionToMissionDetails((BiologyStudyFieldMission)found);
		}
		else if (EmergencySupplyMission.class.isInstance(found)) {
			details = detailsMapper.emergencySupplyToMissionDetails((EmergencySupplyMission)found);
		}
		else if (RescueSalvageVehicle.class.isInstance(found)) {
			details = detailsMapper.rescueSalvageVehicleToMissionDetails((RescueSalvageVehicle)found);
		}		
		else if (Trade.class.isInstance(found)) {
			details = detailsMapper.tradeToMissionDetails((Trade)found);
		}
		else if (TravelToSettlement.class.isInstance(found)) {
			details = detailsMapper.travelToSettlementToMissionDetails((TravelToSettlement)found);
		}
		else if (VehicleMission.class.isInstance(found)) {
			details = detailsMapper.vehicleMissionToMissionDetails((VehicleMission)found);
		}
		else {
			details = detailsMapper.missionToMissionDetails(found);
		}
        return details;
    }


	@ApiOperation(value = "get Mission Persons", nickname = "getMissionPersons")
	@RequestMapping(method = RequestMethod.GET, path="/missions/{id}/persons", produces = "application/json")
    public List<PersonSummary> getPersons(@PathVariable(value="id") int missionId) {
		
        Mission found = findMission(missionId);
        
        return personMapper.personsToPersonSummarys(
        		new ArrayList<Person>(found.getPeople()));
    }

	
}

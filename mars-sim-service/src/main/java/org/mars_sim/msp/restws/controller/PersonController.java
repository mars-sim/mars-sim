package org.mars_sim.msp.restws.controller;


import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.restws.mapper.InventoryMapper;
import org.mars_sim.msp.restws.mapper.PersonDetailsMapper;
import org.mars_sim.msp.restws.mapper.PersonSummaryMapper;
import org.mars_sim.msp.restws.model.PagedList;
import org.mars_sim.msp.restws.model.PersonDetails;
import org.mars_sim.msp.restws.model.PersonSummary;
import org.mars_sim.msp.restws.model.StoredAmount;
import org.mars_sim.msp.restws.model.StoredItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;


@RestController()
public class PersonController extends BaseController {
	/** initialized logger for this class. */
	private static Log log = LogFactory.getLog(PersonController.class);
	
	@Autowired
    private UnitManager personManager;
	
	@Autowired
    private PersonSummaryMapper summaryMapper;
	
	@Autowired
	private PersonDetailsMapper personMapper;

	@Autowired
	private InventoryMapper inventoryMapper;
	
	/**
	 * Find a Person entity by the unique identifer
	 * @param personId
	 * @return
	 */
	private Person getPerson(int personId) {
		// Need a better way to find people
		Iterator<Person> it = personManager.getPeople().iterator();
		while (it.hasNext()) {
			Person person = it.next();
			if (person.getIdentifier() == personId) {
				return person;
			}
		}
		log.error("Can not find " + personId);
		throw new NotFoundException("Person", personId);
	}
	
	
	@ApiOperation(value = "get Person by Id", nickname = "getPerson")
	@RequestMapping(method = RequestMethod.GET, path="/persons/{id}", produces = "application/json")
    public PersonDetails personDetails(@PathVariable(value="id") int personId) {
		
        return personMapper.personToPersonDetail(getPerson(personId));
    }
	
	@ApiOperation(value = "get All Persons", nickname = "getPersons")
    @RequestMapping(method=RequestMethod.GET, path="/persons", produces = "application/json")
    public PagedList<PersonSummary> persons(@RequestParam(value="page", defaultValue="1") int page,
    								   @RequestParam(value="size", defaultValue="10") int pageSize) {
    	Collection<Person> allPerson = personManager.getPeople();  	
    	List<Person> filtered = filter(allPerson, page, pageSize);
		return new PagedList<PersonSummary>(summaryMapper.personsToPersonSummarys(filtered),
											page, pageSize, allPerson.size());
    }
	
	@ApiOperation(value = "get Person Resources", nickname = "getPersonResources")
	@RequestMapping(method = RequestMethod.GET, path="/persons/{id}/resources", produces = "application/json")
    public List<StoredAmount> getResources(@PathVariable(value="id") int personId) {
		        
        return inventoryMapper.getAmounts(getPerson(personId).getInventory());
    }
	
	@ApiOperation(value = "get Person Items", nickname = "getPersonItems")
	@RequestMapping(method = RequestMethod.GET, path="/persons/{id}/items", produces = "application/json")
    public List<StoredItem> getItems(@PathVariable(value="id") int personId) {
		
        return inventoryMapper.getItems(getPerson(personId).getInventory());
    }
}

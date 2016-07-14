package org.mars_sim.msp.restws.mapper;


import java.util.Collection;
import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.restws.model.PersonSummary;

/**
 * This provides a mapper from a msp-core Person entity to the lightweight PersonSummary DTO.
 */
@Mapper(componentModel="spring")
public interface PersonSummaryMapper {
	@Mappings({
		@Mapping(source="identifier", target="id"),
		@Mapping(source="mind.taskManager.taskName", target="task")
	})
	PersonSummary personToPersonSummary(Person person);
	
	/**
	 * Uses the above mapper to map between two lists of the entity and DTO.
	 * @param persons
	 * @return
	 */
	List<PersonSummary> personsToPersonSummarys(Collection<Person> persons);
}

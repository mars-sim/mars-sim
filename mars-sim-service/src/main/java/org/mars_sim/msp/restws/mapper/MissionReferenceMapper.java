package org.mars_sim.msp.restws.mapper;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.restws.model.EntityReference;

/**
 * This provides a mapper from a reference to a msp-core Unit..
 */
@Mapper(componentModel="spring")
public interface MissionReferenceMapper {
	@Mappings({
		@Mapping(source="identifier", target="id"),
	})
	EntityReference missionToEntityReference(Mission reference);
}
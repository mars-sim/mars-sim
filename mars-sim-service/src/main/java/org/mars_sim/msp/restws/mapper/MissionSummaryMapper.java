package org.mars_sim.msp.restws.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.restws.model.MissionSummary;

@Mapper(componentModel="spring",
		uses=UnitReferenceMapper.class)
public interface MissionSummaryMapper {

	@Mappings({
		@Mapping(source="identifier", target="id"),
		@Mapping(source="peopleNumber", target="numPersons"),
		@Mapping(source="phase.name", target="phase")
	})
	MissionSummary missionToMissionSummary(Mission mission);
	
	List<MissionSummary> missionsToMissionSummarys(List<Mission> asList);
}

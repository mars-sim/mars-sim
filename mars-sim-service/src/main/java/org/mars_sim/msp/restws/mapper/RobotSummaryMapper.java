package org.mars_sim.msp.restws.mapper;


import java.util.Collection;
import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.restws.model.RobotSummary;

/**
 * This provides a mapper from a msp-core Person entity to the lightweight PersonSummary DTO.
 */
@Mapper(componentModel="spring")
public interface RobotSummaryMapper {
	@Mappings({
		@Mapping(source="identifier", target="id"),
		@Mapping(source="botMind.botTaskManager.taskName", target="task"),
		@Mapping(source="robotType.displayName", target="type")
	})
	RobotSummary robotToRobotSummary(Robot robot);
	
	/**
	 * Uses the above mapper to map between two lists of the entity and DTO.
	 * @param robots
	 * @return
	 */
	List<RobotSummary> robotsToRobotSummarys(Collection<Robot> robots);
}

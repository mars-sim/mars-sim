package org.mars_sim.msp.restws.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.restws.model.RobotDetails;

@Mapper(componentModel="spring")
public interface RobotDetailsMapper {

	@Mappings({
		@Mapping(source="identifier", target="id"),
		@Mapping(source="botMind.botTaskManager.taskName", target="task"),
		@Mapping(source="botMind.botTaskManager.task.phase.name", target="taskPhase"),
		@Mapping(source="botMind.personalityType.typeString", target="personalityType"),
		@Mapping(source="robotType.displayName", target="type")
	})	
	RobotDetails robotToRobotDetails(Robot robot);
}

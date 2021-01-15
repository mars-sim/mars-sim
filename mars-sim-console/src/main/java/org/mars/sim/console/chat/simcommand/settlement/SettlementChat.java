package org.mars.sim.console.chat.simcommand.settlement;

import java.util.Arrays;
import java.util.List;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.command.InteractiveChatCommand;
import org.mars.sim.console.chat.simcommand.ConnectedUnitCommand;
import org.mars.sim.console.chat.simcommand.StructuredResponse;
import org.mars.sim.console.chat.simcommand.UnitLocationCommand;
import org.mars_sim.msp.core.structure.Settlement;

/**
 * Represents a connection to a Settlement.
 */
public class SettlementChat extends ConnectedUnitCommand {
	public static final String SETTLEMENT_GROUP = "Settlement";

	private static final List<ChatCommand> COMMANDS = Arrays.asList(AirlockCommand.AIRLOCK,
																	BedCommand.BED,
																	CountryCommand.COUNTRY,
																	DashboardCommand.DASHBOARD,
																	JobDemandCommand.DEMAND,
																	JobProspectCommand.PROSPECT,
																	JobRosterCommand.ROSTER,
																	MissionNowCommand.MISSION_NOW,
																	MissionRadiusCommand.RADIUS,
																	PeopleCommand.PEOPLE,
																	LevelCommand.LEVEL,
																	new UnitLocationCommand(SETTLEMENT_GROUP),
																	ObjectiveCommand.OBJECTIVE,
																	ResearcherCommand.RESEARCHER,
																	ResourceCommand.RESOURCE,
																	RobotCommand.ROBOT,
																	RoleCommand.ROLE,
																	ScienceCommand.SCIENCE,
																	SocialCommand.SOCIAL,
																	StudyCommand.STUDY,
																	TaskCommand.TASK,
																	VehicleCommand.VEHICLE,
																	WeatherCommand.WEATHER);


	public SettlementChat(Settlement settlement, InteractiveChatCommand parent) {
		super(settlement, COMMANDS, parent);
	}

	@Override
	public String getIntroduction() {
		Settlement settlement = getSettlement();
		
		StructuredResponse response = new StructuredResponse();
		response.append("Connected to " + settlement.getName() + "\n\n");
		
		// Reuse the dashboard
		DashboardCommand.DASHBOARD.generatedDashboard(settlement, response);
		
		return response.getOutput();
	}

	public Settlement getSettlement() {
		return (Settlement) getUnit();
	}
}

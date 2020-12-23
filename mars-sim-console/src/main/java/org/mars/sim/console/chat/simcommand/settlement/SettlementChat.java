package org.mars.sim.console.chat.simcommand.settlement;

import java.util.Arrays;
import java.util.List;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.simcommand.ConnectedUnitCommand;
import org.mars.sim.console.chat.simcommand.StructuredResponse;
import org.mars_sim.msp.core.structure.Settlement;

/**
 * Represents a connection to a Settlement.
 */
public class SettlementChat extends ConnectedUnitCommand {
	private static final List<ChatCommand> COMMANDS = Arrays.asList(BedCommand.BED,
																	DashboardCommand.DASHBOARD,
																	JobDemandCommand.DEMAND,
																	JobProspectCommand.PROSPECT,
																	JobRosterCommand.ROSTER,																	PeopleCommand.PEOPLE,
																	LevelCommand.LEVEL,
																	ObjectiveCommand.OBJECTIVE,
																	ResearcherCommand.RESEARCHER,
																	ResourceCommand.RESOURCE,
																	RobotCommand.ROBOT,
																	RoleCommand.ROLE,
																	ScienceCommand.SCIENCE,
																	SocialCommand.SOCIAL,
																	TaskCommand.TASK,
																	VehicleCommand.VEHICLE,
																	WeatherCommand.WEATHER);

	public static final String SETTLEMENT_GROUP = "Settlement";

	public SettlementChat(Settlement settlement) {
		super(settlement, COMMANDS);
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

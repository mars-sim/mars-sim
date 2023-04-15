/**
 * Mars Simulation Project
 * SettlementChat.java
 * @version 3.1.2 2020-12-30
 * @author Barry Evans
 */

package org.mars.sim.console.chat.simcommand.settlement;

import java.util.Arrays;
import java.util.List;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.command.InteractiveChatCommand;
import org.mars.sim.console.chat.simcommand.ConnectedUnitCommand;
import org.mars.sim.console.chat.simcommand.StructuredResponse;
import org.mars.sim.console.chat.simcommand.unit.EquipmentCommand;
import org.mars.sim.console.chat.simcommand.unit.InventoryCommand;
import org.mars.sim.console.chat.simcommand.unit.MalfunctionCreateCommand;
import org.mars.sim.console.chat.simcommand.unit.ResourceHolderRefillCommand;
import org.mars.sim.console.chat.simcommand.unit.UnitLocationCommand;
import org.mars.sim.console.chat.simcommand.unit.UnitSunlightCommand;
import org.mars_sim.msp.core.structure.Settlement;

/**
 * Represents a connection to a Settlement.
 */
public class SettlementChat extends ConnectedUnitCommand {
	public static final String SETTLEMENT_GROUP = "Settlement";

	private static final List<ChatCommand> COMMANDS = Arrays.asList(AirlockCommand.AIRLOCK,
																	BacklogCommand.BACKLOG,
																	BedCommand.BED,
																	BuildingCommand.BUILDING,
																	CropCommand.CROP,
																	DashboardCommand.DASHBOARD,
																	FishCommand.FISH,
																	FutureEventCommand.FUTURE,
																	JobDemandCommand.DEMAND,
																	JobProspectCommand.PROSPECT,
																	JobRosterCommand.ROSTER,
																	ManufactureCommand.MANUFACTURE,
																	MealCommand.MEALS,
																	MissionNowCommand.MISSION_NOW,
																	MissionRadiusCommand.RADIUS,
																	MissionCreateCommand.MISSION,
																	PeopleCommand.PEOPLE,
																	LevelCommand.LEVEL,
																	TradeCommand.TRADE,
																	new ResourceHolderRefillCommand(SETTLEMENT_GROUP),
																	new EquipmentCommand(SETTLEMENT_GROUP),
																	new InventoryCommand(SETTLEMENT_GROUP),
																	new UnitLocationCommand(SETTLEMENT_GROUP),
																	new UnitSunlightCommand(SETTLEMENT_GROUP),
																	MalfunctionCommand.MALFUNCTION,
																	new MalfunctionCreateCommand(SETTLEMENT_GROUP),
																	ResourceProcessCommand.PROCESS,
																	ObjectiveCommand.OBJECTIVE,
																	RadiationCommand.RADIATION,
																	ResearcherCommand.RESEARCHER,
																	ResourceCommand.RESOURCE,
																	RobotCommand.ROBOT,
																	RoleCommand.ROLE,
																	ScienceCommand.SCIENCE,
																	ShiftsCommand.SHIFTS,
																	SocialCommand.SOCIAL,
																	StudyCommand.STUDY,
																	TaskCommand.TASK,
																	VehicleCommand.VEHICLE,
																	WasteProcessCommand.WASTE,
																	WeatherCommand.WEATHER);


	public SettlementChat(Settlement settlement, InteractiveChatCommand parent) {
		super(settlement, COMMANDS, parent);
	}

	@Override
	public String getIntroduction() {
		Settlement settlement = getSettlement();
		
		StructuredResponse response = new StructuredResponse();
		response.appendText("Connected to " + settlement.getName());
		
		// Reuse the dashboard
		DashboardCommand.DASHBOARD.generatedDashboard(settlement, response);
		
		return response.getOutput();
	}

	public Settlement getSettlement() {
		return (Settlement) getUnit();
	}
}

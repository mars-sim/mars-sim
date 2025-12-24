/*
 * Mars Simulation Project
 * SettlementChat.java
 * @date 2023-07-22
 * @author Barry Evans
 */

package com.mars_sim.console.chat.simcommand.settlement;

import java.util.Arrays;
import java.util.List;

import com.mars_sim.console.chat.ChatCommand;
import com.mars_sim.console.chat.command.InteractiveChatCommand;
import com.mars_sim.console.chat.simcommand.ConnectedUnitCommand;
import com.mars_sim.console.chat.simcommand.StructuredResponse;
import com.mars_sim.console.chat.simcommand.unit.EquipmentCommand;
import com.mars_sim.console.chat.simcommand.unit.InventoryCommand;
import com.mars_sim.console.chat.simcommand.unit.MalfunctionCreateCommand;
import com.mars_sim.console.chat.simcommand.unit.PartFurnishedCommand;
import com.mars_sim.console.chat.simcommand.unit.ResourceHolderRefillCommand;
import com.mars_sim.console.chat.simcommand.unit.UnitLocationCommand;
import com.mars_sim.core.structure.Settlement;

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
																	ExploredCommand.EXPLORED,
																	FishCommand.FISH,
																	AlgaeCommand.ALGAE,
																	JobDemandCommand.DEMAND,
																	JobProspectCommand.PROSPECT,
																	JobRosterCommand.ROSTER,
																	ManufactureCommand.MANUFACTURE,
																	MealCommand.MEALS,
																	MissionNowCommand.MISSION_NOW,
																	MissionCreateCommand.MISSION,
																	PeopleCommand.PEOPLE,
																	LevelCommand.LEVEL,
																	TradeCommand.TRADE,
																	new ResourceHolderRefillCommand(SETTLEMENT_GROUP),
																	new PartFurnishedCommand(SETTLEMENT_GROUP),
																	new EquipmentCommand(SETTLEMENT_GROUP),
																	new InventoryCommand(SETTLEMENT_GROUP),
																	new UnitLocationCommand(SETTLEMENT_GROUP),
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
																	SiteCommand.SITE,
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

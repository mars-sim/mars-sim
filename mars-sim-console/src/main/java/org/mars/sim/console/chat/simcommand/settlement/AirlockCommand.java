package org.mars.sim.console.chat.simcommand.settlement;

import java.util.List;
import java.util.stream.Collectors;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.simcommand.CommandHelper;
import org.mars.sim.console.chat.simcommand.StructuredResponse;
import org.mars_sim.msp.core.structure.Airlock;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.FunctionType;

public class AirlockCommand extends AbstractSettlementCommand {

	public static final ChatCommand AIRLOCK = new AirlockCommand();
	
	private AirlockCommand() {
		super("ai", "airlocks", "Status of all airlocks");

	}

	@Override
	protected boolean execute(Conversation context, String input, Settlement settlement) {
		StructuredResponse response = new StructuredResponse();
		
		BuildingManager bm = settlement.getBuildingManager();
		List<Airlock> i = bm.getBuildings(FunctionType.EVA).stream()
								.map(b -> b.getEVA().getAirlock())
								.collect(Collectors.toList());
		CommandHelper.outputAirlock(response, i);

		context.println(response.getOutput());
		return true;
	}
}

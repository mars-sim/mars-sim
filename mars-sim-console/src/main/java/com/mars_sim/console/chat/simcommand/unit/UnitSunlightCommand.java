/**
 * Mars Simulation Project
 * UnitSunlightCommand.java
 * @date 16-07-2022
 * @author Barry Evans
 */

package com.mars_sim.console.chat.simcommand.unit;

import com.mars_sim.console.chat.Conversation;
import com.mars_sim.console.chat.simcommand.StructuredResponse;
import com.mars_sim.core.Unit;
import com.mars_sim.core.environment.SurfaceFeatures;
import com.mars_sim.core.map.location.Coordinates;

/**
 * Command to show Sunlight details for a Unit
 */
public class UnitSunlightCommand extends AbstractUnitCommand {


	public UnitSunlightCommand(String groupName) {
		super(groupName, "su", "sunlight", "Show Sunlight details at Unit's location");
	}

	/** 
	 * Output the current immediate location of the Unit
	 */
	@Override
	protected boolean execute(Conversation context, String input, Unit source) {
		Coordinates locn = source.getCoordinates();

		StructuredResponse response = new StructuredResponse();

		SurfaceFeatures surfaceFeatures = context.getSim().getSurfaceFeatures();

		response.appendLabeledString("Sunlight Irradiance", String.format("%.2f Wm-2",
														surfaceFeatures.getSolarIrradiance(locn)));
		response.appendLabeledString("Dark Polar Region", (surfaceFeatures.inDarkPolarRegion(locn) ?
															"Yes" : "No"));
		response.appendLabeledString("Recent Sunrise", surfaceFeatures.getOrbitInfo().getSunrise(locn).getTruncatedDateTimeStamp());

		response.appendLabeledString("Recent Sunset", surfaceFeatures.getOrbitInfo().getSunset(locn).getTruncatedDateTimeStamp());

		context.println(response.getOutput());
		return true;
	}
}

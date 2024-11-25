/**
 * Mars Simulation Project
 * UnitSunlightCommand.java
 * @date 16-07-2022
 * @author Barry Evans
 */

package com.mars_sim.console.chat.simcommand;

import com.mars_sim.console.chat.Conversation;
import com.mars_sim.core.environment.SurfaceFeatures;
import com.mars_sim.core.map.location.Coordinates;

/**
 * Command to show Sunlight details at a Coordinates
 */
public class SunlightCommand extends CoordinatesCommand {

	public static final SunlightCommand SUNLIGHT = new SunlightCommand();

	private SunlightCommand() {
		super("su", "sunlight", "Show Sunlight details at Unit's location",
					"Location");
	}

	/** 
	 * Output the current immediate location of the Unit
	 */
	@Override
	protected boolean execute(Conversation context, Coordinates locn) {

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

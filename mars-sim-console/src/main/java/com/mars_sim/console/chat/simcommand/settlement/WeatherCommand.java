/**
 * Mars Simulation Project
 * WeatherCommand.java
 * @date 2023-11-09
 * @author Barry Evans
 */

package com.mars_sim.console.chat.simcommand.settlement;

import java.util.Arrays;
import java.util.List;

import com.mars_sim.console.chat.ChatCommand;
import com.mars_sim.console.chat.Conversation;
import com.mars_sim.console.chat.ConversationRole;
import com.mars_sim.console.chat.simcommand.CommandHelper;
import com.mars_sim.console.chat.simcommand.StructuredResponse;
import com.mars_sim.core.Simulation;
import com.mars_sim.core.environment.DustStorm;
import com.mars_sim.core.environment.DustStormType;
import com.mars_sim.core.environment.OrbitInfo;
import com.mars_sim.core.environment.SurfaceFeatures;
import com.mars_sim.core.environment.Weather;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.mapdata.location.Coordinates;

public class WeatherCommand extends AbstractSettlementCommand {
	public static final ChatCommand WEATHER = new WeatherCommand();

	private static final String AIR_DENSITY = "%.2f g/m\u00B3";
	private static final String SOLAR_IRR =  "%.2f W/m\u00B2";
	
	private WeatherCommand() {
		super("w", "weather", "Settlement weather");
	}

	private void outputDustStorm(DustStorm ds, StructuredResponse response) {
		response.appendHeading("Dust Storm - " + ds.getName());
		response.appendLabeledString("Type", ds.getType().getName());
		response.appendLabeledString("Speed", String.format(CommandHelper.MS_FORMAT, ds.getSpeed()));
		response.appendLabeledString("Size", String.format(CommandHelper.KM_FORMAT, (double)ds.getSize()));
	}

	@Override
	protected boolean execute(Conversation context, String input, Settlement settlement) {
		StructuredResponse response = new StructuredResponse();
		Simulation sim = context.getSim();
		SurfaceFeatures surfaceFeatures = sim.getSurfaceFeatures();
		Weather weather = sim.getWeather();
		OrbitInfo orbitInfo = sim.getOrbitInfo();

		Coordinates location = settlement.getCoordinates();
		response.appendLabeledString("Location", location.toString());

		double t = weather.getTemperature(location);
		response.appendLabeledString("Outside temperature", String.format(CommandHelper.CELSIUS_FORMAT, t));

		double p = weather.getAirPressure(location);
		response.appendLabeledString("Air Pressure", String.format(CommandHelper.KPA_FORMAT, p));

		double ad = weather.getAirDensity(location);
		response.appendLabeledString("Air Density", String.format(AIR_DENSITY, ad));

		response.appendLabeledString("Wind Speed", String.format(CommandHelper.MS_FORMAT,
						 weather.getWindSpeed(location)));

		double wd = weather.getWindDirection(location);
		response.appendLabeledString("Wind Direction", String.format(CommandHelper.DEG_FORMAT, wd));

		double od = surfaceFeatures.getOpticalDepth(location);
		response.appendLabeledString("Optical Depth", String.format(CommandHelper.DOUBLE_FORMAT, od));
			
		double sda = orbitInfo.getSolarDeclinationAngleInDeg();
		response.appendLabeledString("Solar Declination Angle", String.format(CommandHelper.DEG_FORMAT, sda));

		double si = surfaceFeatures.getSolarIrradiance(location);
		response.appendLabeledString("Solar Irradiance", String.format(SOLAR_IRR, si));
		
		DustStorm ds = settlement.getDustStorm();
		if (ds != null) {
			outputDustStorm(ds, response);
		}
		context.println(response.getOutput());

		// Expert can create a Dust Storm
		if ((ds == null) && context.getRoles().contains(ConversationRole.EXPERT)) {
			// Offer the option to create a Dust Storm
			String change = context.getInput("Create a new Dust Storm (Y/N)?");
			if ("Y".equalsIgnoreCase(change)) {
				List<String> options = Arrays.stream(DustStormType.values())
												.map(DustStormType::getName).toList();
				int choice = CommandHelper.getOptionInput(context, options, "Which type of storm?");
				if (choice >= 0) {
					ds = weather.createStorm(settlement, DustStormType.values()[choice]);

					StructuredResponse dsResponse = new StructuredResponse();
					outputDustStorm(ds, dsResponse);
					context.println(dsResponse.getOutput());
				}
			}
		}

		return true;
	}
}

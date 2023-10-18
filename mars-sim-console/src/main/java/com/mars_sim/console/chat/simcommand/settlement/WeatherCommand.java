/**
 * Mars Simulation Project
 * WeatherCommand.java
 * @version 3.1.2 2020-12-30
 * @author Barry Evans
 */

package com.mars_sim.console.chat.simcommand.settlement;

import java.text.DecimalFormat;
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
import com.mars_sim.tools.Msg;

public class WeatherCommand extends AbstractSettlementCommand {
	public static final ChatCommand WEATHER = new WeatherCommand();

	static DecimalFormat fmt2 = new DecimalFormat("#0.00");

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
		String tt = fmt2.format(t) + Msg.getString("direction.degreeSign"); //$NON-NLS-1$
		response.appendLabeledString("Outside temperature", tt);

		double p = weather.getAirPressure(location);
		String pp = fmt2.format(p) + " " + Msg.getString("pressure.unit.kPa"); //$NON-NLS-1$
		response.appendLabeledString("Air Pressure", pp);

		double ad = weather.getAirDensity(location);
		String aad = fmt2.format(ad) + " " + Msg.getString("airDensity.unit.gperm3"); //$NON-NLS-1$
		response.appendLabeledString("Air Density", aad);

		response.appendLabeledString("Wind Speed", String.format(CommandHelper.MS_FORMAT,
						 weather.getWindSpeed(location)));

		double wd = weather.getWindDirection(location);
		String wwd = fmt2.format(wd) + Msg.getString("windDirection.unit.deg"); //$NON-NLS-1$
		response.appendLabeledString("Wind Direction", wwd);

		double od = surfaceFeatures.getOpticalDepth(location);
		response.appendLabeledString("Optical Depth", fmt2.format(od));

		double sda = orbitInfo.getSolarDeclinationAngleDegree();
		String ssda = fmt2.format(sda) + Msg.getString("direction.degreeSign"); //$NON-NLS-1$
		response.appendLabeledString("Solar Declination Angle", ssda);

		double si = surfaceFeatures.getSolarIrradiance(location);
		String ssi = fmt2.format(si) + " " + Msg.getString("solarIrradiance.unit"); //$NON-NLS-1$
		response.appendLabeledString("Solar Irradiance", ssi);
		
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

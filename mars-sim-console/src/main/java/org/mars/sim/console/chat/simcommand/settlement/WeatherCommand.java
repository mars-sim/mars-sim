/**
 * Mars Simulation Project
 * WeatherCommand.java
 * @version 3.1.2 2020-12-30
 * @author Barry Evans
 */

package org.mars.sim.console.chat.simcommand.settlement;

import java.text.DecimalFormat;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.simcommand.StructuredResponse;
import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.environment.OrbitInfo;
import org.mars_sim.msp.core.environment.SurfaceFeatures;
import org.mars_sim.msp.core.environment.Weather;
import org.mars_sim.msp.core.structure.Settlement;

public class WeatherCommand extends AbstractSettlementCommand {
	public static final ChatCommand WEATHER = new WeatherCommand();

	static DecimalFormat fmt2 = new DecimalFormat("#0.00");

	private WeatherCommand() {
		super("w", "weather", "Settlement weather");
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
		String tt = fmt2.format(t) + Msg.getString("direction.degreeSign"); //$NON-NLS-1$;
		response.appendLabeledString("Outside temperature", tt);

		double p = weather.getAirPressure(location);
		String pp = fmt2.format(p) + " " + Msg.getString("pressure.unit.kPa"); //$NON-NLS-1$
		response.appendLabeledString("Air Pressure", pp);

		double ad = weather.getAirDensity(location);
		String aad = fmt2.format(ad) + " " + Msg.getString("airDensity.unit.gperm3"); //$NON-NLS-1$
		response.appendLabeledString("Air Density", aad);

		double ws = weather.getWindSpeed(location);
		String wws = fmt2.format(ws) + " " + Msg.getString("windspeed.unit.meterpersec"); //$NON-NLS-1$
		response.appendLabeledString("Wind Speed", wws);

		double wd = weather.getWindDirection(location);
		String wwd = fmt2.format(wd) + Msg.getString("windDirection.unit.deg"); //$NON-NLS-1$
		response.appendLabeledString("Wind Direction", wwd);

		double od = surfaceFeatures.getOpticalDepth(location);
		response.appendLabeledString("Optical Depth", fmt2.format(od));

		double sda = orbitInfo.getSolarDeclinationAngleDegree();
		String ssda = fmt2.format(sda) + Msg.getString("direction.degreeSign"); //$NON-NLS-1$;
		response.appendLabeledString("Solar Declination Angle", ssda);

		double si = surfaceFeatures.getSolarIrradiance(location);
		String ssi = fmt2.format(si) + " " + Msg.getString("solarIrradiance.unit"); //$NON-NLS-1$
		response.appendLabeledString("Solar Irradiance", ssi);
		
		context.println(response.getOutput());
		return true;
	}
}

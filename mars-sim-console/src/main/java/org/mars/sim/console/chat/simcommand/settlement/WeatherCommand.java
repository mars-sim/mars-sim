package org.mars.sim.console.chat.simcommand.settlement;

import java.text.DecimalFormat;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.simcommand.StructuredResponse;
import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.mars.OrbitInfo;
import org.mars_sim.msp.core.mars.SurfaceFeatures;
import org.mars_sim.msp.core.mars.Weather;
import org.mars_sim.msp.core.structure.Settlement;

public class WeatherCommand extends AbstractSettlementCommand {
	public static final ChatCommand WEATHER = new WeatherCommand();

	static DecimalFormat fmt2 = new DecimalFormat("#0.00");

	private WeatherCommand() {
		super("w", "weather", "Settlement weather");
	}

	@Override
	protected void execute(Conversation context, String input, Settlement settlement) {
		StructuredResponse response = new StructuredResponse();
		Simulation sim = context.getSim();
		SurfaceFeatures surfaceFeatures = sim.getMars().getSurfaceFeatures();
		Weather weather = sim.getMars().getWeather();
		OrbitInfo orbitInfo = sim.getMars().getOrbitInfo();
		
		String DEGREE_CELSIUS = Msg.getString("direction.degreeSign"); //$NON-NLS-1$
		String DEGREE = Msg.getString("direction.degreeSign"); //$NON-NLS-1$

		Coordinates location = settlement.getCoordinates();
		response.appendLabeledString("Location", location.toString());

		double t = weather.getTemperature(location);
		String tt = fmt2.format(t) + DEGREE_CELSIUS;
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

//		double sza = orbitInfo.getSolarZenithAngle(location);
//		String ssza = fmt2.format(sza * RADIANS_TO_DEGREES) + DEGREE;
//		response.appendLabeledString("Solar Zenith Angle", ssza);

		double sda = orbitInfo.getSolarDeclinationAngleDegree();
		String ssda = fmt2.format(sda) + DEGREE;
		response.appendLabeledString("Solar Declination Angle", ssda);

		double si = surfaceFeatures.getSolarIrradiance(location);
		String ssi = fmt2.format(si) + " " + Msg.getString("solarIrradiance.unit"); //$NON-NLS-1$
		response.appendLabeledString("Solar Irradiance", ssi);
		
		context.println(response.getOutput());
	}

}

/*
 * Mars Simulation Project
 * TabPanelWeather.java
 * @date 2024-07-17
 * @author Manny Kung
 */
package com.mars_sim.ui.swing.unit_window.structure;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.mars_sim.core.Simulation;
import com.mars_sim.core.environment.OrbitInfo;
import com.mars_sim.core.environment.SurfaceFeatures;
import com.mars_sim.core.environment.Weather;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.TemporalComponent;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.entitywindow.EntityTabPanel;
import com.mars_sim.ui.swing.utils.AttributePanel;
import com.mars_sim.ui.swing.utils.SwingHelper;

/**
 * The TabPanelWeather is a tab panel for location information.
 */
@SuppressWarnings("serial")
class TabPanelWeather extends EntityTabPanel<Settlement> implements TemporalComponent {

	private static final String WEATHER_ICON = "weather";
	private static final double RADIANS_TO_DEGREES = 180/Math.PI;

	private int windDirectionCache;
	
	private double airPressureCache;
	private double temperatureCache;
	private double windSpeedCache;
	private double airDensityCache;
	private double opticalDepthCache;
	private double zenithAngleCache;
	private double solarDeclinationCache;
	private double solarIrradianceCache;

	private JLabel airDensityLabel;
	private JLabel pressureTF;
	private JLabel solarIrradianceLabel;
	private JLabel windSpeedLabel;
	private JLabel windDirLabel;
	private JLabel opticalDepthLabel;
	private JLabel zenithAngleLabel;
	private JLabel solarDeclinationLabel;

	private JLabel temperatureLabel;
	
	private Weather weather;
	private SurfaceFeatures surfaceFeatures;
	private OrbitInfo orbitInfo;
		
    /**
     * Constructor.
     * 
     * @param unit the unit to display.
     * @param context The UI context.
     */
    public TabPanelWeather(Settlement unit, UIContext context) {
        super(
        	Msg.getString("TabPanelWeather.title"), //$NON-NLS-1$
    		ImageLoader.getIconByName(WEATHER_ICON), null,
			context, unit
    	);

		Simulation sim = context.getSimulation();
		weather = sim.getWeather();
		surfaceFeatures = sim.getSurfaceFeatures();
		orbitInfo = sim.getOrbitInfo();
	}
	
	@Override
	protected void buildUI(JPanel content) {

        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        content.add(mainPanel, BorderLayout.NORTH);
		Settlement s = getEntity();

		// Create location panel
		AttributePanel locnPanel = new AttributePanel();
		locnPanel.setBorder(SwingHelper.createLabelBorder("Location"));
		locnPanel.addTextField("Lat", s.getLocation().getFormattedLatitudeString(), null);
		locnPanel.addTextField("Lon", s.getLocation().getFormattedLongitudeString(), null);
		locnPanel.addTextField("Zone", s.getTimeZone().getId(), null);

      	// Create weatherPanel
        JPanel centerEastPanel = new JPanel(new BorderLayout(5, 5));
        centerEastPanel.setBorder(new EmptyBorder(2, 2, 2, 2));
        centerEastPanel.add(locnPanel, BorderLayout.NORTH);
        
        JPanel eastPanel = new JPanel(new BorderLayout(0, 10));       
        mainPanel.add(eastPanel, BorderLayout.EAST);
        eastPanel.add(centerEastPanel, BorderLayout.CENTER);

    	// Prepare temperature panel
        JPanel temperaturePanel = new JPanel(new FlowLayout());
        centerEastPanel.add(temperaturePanel, BorderLayout.CENTER);

        JPanel metricsPanel = new JPanel(new BorderLayout(5, 5));
        mainPanel.add(metricsPanel, BorderLayout.CENTER);
        
        JPanel atmPanel = new JPanel(new BorderLayout(5, 5));
        metricsPanel.add(atmPanel, BorderLayout.NORTH);
        
        // Create spring layout panel
        AttributePanel atmSpringPanel = new AttributePanel(5);
        atmPanel.add(atmSpringPanel, BorderLayout.NORTH);
        atmPanel.setBorder(SwingHelper.createLabelBorder("Atmospheric"));
           
        temperatureLabel = atmSpringPanel.addTextField("Temperature", "", null);
        pressureTF =  atmSpringPanel.addTextField(Msg.getString("TabPanelWeather.airPressure.label"), "", null);
        airDensityLabel = atmSpringPanel.addTextField(Msg.getString("TabPanelWeather.airDensity.label"), "", null);
        windSpeedLabel = atmSpringPanel.addTextField(Msg.getString("TabPanelWeather.windspeed.label"), "", null);
        windDirLabel = atmSpringPanel.addTextField(Msg.getString("TabPanelWeather.windDirection.label"), "", null);
        
        JPanel sunPanel = new JPanel(new BorderLayout(5, 5));
        metricsPanel.add(sunPanel, BorderLayout.CENTER);
        
        // Create spring layout panel
        AttributePanel sunSpringPanel = new AttributePanel(4);
        sunPanel.add(sunSpringPanel, BorderLayout.NORTH);
        sunPanel.setBorder(SwingHelper.createLabelBorder("Solar"));
        
        solarIrradianceLabel = sunSpringPanel.addTextField(Msg.getString("TabPanelWeather.solarIrradiance.label"), "", null);
        opticalDepthLabel = sunSpringPanel.addTextField(Msg.getString("TabPanelWeather.opticalDepth.label"), "", null);
        zenithAngleLabel = sunSpringPanel.addTextField(Msg.getString("TabPanelWeather.zenithAngle.label"), "", null);
        solarDeclinationLabel = sunSpringPanel.addTextField(Msg.getString("TabPanelWeather.solarDeclination.label"), "", null);
   
        refreshDetails();
	}

    /**
     * Updates the info on this panel.
     */
	@Override
	public void clockUpdate(ClockPulse pulse) {
		refreshDetails();
	}

	/**
	 * Updates the info on this panel.
	 */
	private void refreshDetails() {
		var locn = getEntity().getLocation();

		double p = weather.getAirPressure(locn);
		if (airPressureCache != p) {
			airPressureCache = p;
			pressureTF.setText(StyleManager.DECIMAL_KPA.format(airPressureCache));
		}

		double t = weather.getTemperature(locn);
		if (temperatureCache != t) {
			temperatureCache = t;
			temperatureLabel.setText(StyleManager.DECIMAL_CELCIUS.format(temperatureCache));
		}

		int wd = weather.getWindDirection(locn);
		if (windDirectionCache != wd) {
			windDirectionCache = wd;
			windDirLabel.setText(StyleManager.DECIMAL_DEG.format(windDirectionCache));
		}

		double s = weather.getWindSpeed(locn);
		if (windSpeedCache != s) {
			windSpeedCache = s;
			windSpeedLabel.setText(StyleManager.DECIMAL_M_S.format(windSpeedCache));
		}

		double ad = weather.getAirDensity(locn);
		if (airDensityCache != ad) {
			airDensityCache = ad;
			airDensityLabel.setText(StyleManager.DECIMAL_G_M3.format(airDensityCache));
		}

		double od = surfaceFeatures.getOpticalDepth(locn);
		if (opticalDepthCache != od) {
			opticalDepthCache = od;
			opticalDepthLabel.setText(StyleManager.DECIMAL_PLACES2.format(opticalDepthCache));
		}

		double za = orbitInfo.getSolarZenithAngle(locn);
		if (zenithAngleCache != za) {
			zenithAngleCache = za;
			zenithAngleLabel.setText(StyleManager.DECIMAL_DEG.format(zenithAngleCache * RADIANS_TO_DEGREES));
		}

		double sd = orbitInfo.getSolarDeclinationAngleInDeg();
		if (solarDeclinationCache != sd) {
			solarDeclinationCache = sd;
			solarDeclinationLabel.setText(StyleManager.DECIMAL_DEG.format(solarDeclinationCache));
		}

		double ir = surfaceFeatures.getSolarIrradiance(locn);
		if (solarIrradianceCache != ir) {
			solarIrradianceCache = ir;
			solarIrradianceLabel.setText(StyleManager.DECIMAL_W_M2.format(solarIrradianceCache));
		}
    }
}

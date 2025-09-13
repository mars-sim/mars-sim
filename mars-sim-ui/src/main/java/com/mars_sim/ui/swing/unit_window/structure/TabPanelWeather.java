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
import com.mars_sim.core.time.MasterClock;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.MainDesktopPane;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.unit_window.TabPanel;
import com.mars_sim.ui.swing.utils.AttributePanel;

/**
 * The TabPanelWeather is a tab panel for location information.
 */
@SuppressWarnings("serial")
public class TabPanelWeather
extends TabPanel {

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
	private MasterClock masterClock;
		
    /**
     * Constructor.
     * 
     * @param unit the unit to display.
     * @param desktop the main desktop.
     */
    public TabPanelWeather(Settlement unit, MainDesktopPane desktop) {
        // Use the TabPanel constructor
        super(
        	Msg.getString("TabPanelWeather.title"), //$NON-NLS-1$
    		ImageLoader.getIconByName(WEATHER_ICON),
    		Msg.getString("TabPanelWeather.title"), //$NON-NLS-1$
    		unit, desktop
    	);

		Simulation sim = desktop.getSimulation();
		weather = sim.getWeather();
		surfaceFeatures = sim.getSurfaceFeatures();
		orbitInfo = sim.getOrbitInfo();
		masterClock = sim.getMasterClock();
	}
	
	@Override
	protected void buildUI(JPanel content) {

        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        content.add(mainPanel, BorderLayout.NORTH);
		Settlement s = getSettlement();

		// Create location panel
		AttributePanel locnPanel = new AttributePanel();
		addBorder(locnPanel, "Location");
		locnPanel.addRow("Lat", s.getLocation().getFormattedLatitudeString());
		locnPanel.addRow("Lon", s.getLocation().getFormattedLongitudeString());
		locnPanel.addRow("Zone", s.getTimeZone().getId());


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
        addBorder(atmPanel, "Atmospheric");
           
        temperatureLabel = atmSpringPanel.addRow("Temperature", "");
        pressureTF =  atmSpringPanel.addRow(Msg.getString("TabPanelWeather.airPressure.label"), "");
        airDensityLabel = atmSpringPanel.addRow(Msg.getString("TabPanelWeather.airDensity.label"), "");
        windSpeedLabel = atmSpringPanel.addRow(Msg.getString("TabPanelWeather.windspeed.label"), "");
        windDirLabel = atmSpringPanel.addRow(Msg.getString("TabPanelWeather.windDirection.label"), "");
        
        JPanel sunPanel = new JPanel(new BorderLayout(5, 5));
        metricsPanel.add(sunPanel, BorderLayout.CENTER);
        
        // Create spring layout panel
        AttributePanel sunSpringPanel = new AttributePanel(4);
        sunPanel.add(sunSpringPanel, BorderLayout.NORTH);
        addBorder(sunPanel, "Solar");
        
        solarIrradianceLabel = sunSpringPanel.addRow(Msg.getString("TabPanelWeather.solarIrradiance.label"), "");
        opticalDepthLabel = sunSpringPanel.addRow(Msg.getString("TabPanelWeather.opticalDepth.label"), "");
        zenithAngleLabel = sunSpringPanel.addRow(Msg.getString("TabPanelWeather.zenithAngle.label"), "");
        solarDeclinationLabel = sunSpringPanel.addRow(Msg.getString("TabPanelWeather.solarDeclination.label"), "");
   
        update();
	}

	private Settlement getSettlement() {
		return (Settlement)getUnit();
	}

    /**
     * Updates the info on this panel.
     */
	@Override
    public void update() {
        if (!masterClock.isPaused()) {
			var locn = getSettlement().getLocation();

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
}

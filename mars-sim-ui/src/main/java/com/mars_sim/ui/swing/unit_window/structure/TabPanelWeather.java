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
import com.mars_sim.ui.swing.components.JDoubleLabel;
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
	
	private JDoubleLabel airDensityLabel;
	private JDoubleLabel pressureTF;
	private JDoubleLabel solarIrradianceLabel;
	private JDoubleLabel windSpeedLabel;
	private JLabel windDirLabel;
	private JDoubleLabel opticalDepthLabel;
	private JDoubleLabel zenithAngleLabel;
	private JDoubleLabel solarDeclinationLabel;
	private JDoubleLabel temperatureLabel;
	
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
        var locn = getEntity().getLocation();
           
        temperatureLabel = new JDoubleLabel(StyleManager.DECIMAL_CELCIUS, weather.getTemperature(locn));
        atmSpringPanel.addLabelledItem("Temperature", temperatureLabel, null);
        
        pressureTF = new JDoubleLabel(StyleManager.DECIMAL_KPA, weather.getAirPressure(locn));
        atmSpringPanel.addLabelledItem(Msg.getString("TabPanelWeather.airPressure.label"), pressureTF, null);
        
        airDensityLabel = new JDoubleLabel(StyleManager.DECIMAL_G_M3, weather.getAirDensity(locn));
        atmSpringPanel.addLabelledItem(Msg.getString("TabPanelWeather.airDensity.label"), airDensityLabel, null);
        
        windSpeedLabel = new JDoubleLabel(StyleManager.DECIMAL_M_S, weather.getWindSpeed(locn));
        atmSpringPanel.addLabelledItem(Msg.getString("TabPanelWeather.windspeed.label"), windSpeedLabel, null);
        
        windDirLabel = atmSpringPanel.addTextField(Msg.getString("TabPanelWeather.windDirection.label"), "", null);
        
        JPanel sunPanel = new JPanel(new BorderLayout(5, 5));
        metricsPanel.add(sunPanel, BorderLayout.CENTER);
        
        // Create spring layout panel
        AttributePanel sunSpringPanel = new AttributePanel(4);
        sunPanel.add(sunSpringPanel, BorderLayout.NORTH);
        sunPanel.setBorder(SwingHelper.createLabelBorder("Solar"));
                
        solarIrradianceLabel = new JDoubleLabel(StyleManager.DECIMAL_W_M2, surfaceFeatures.getSolarIrradiance(locn));
        sunSpringPanel.addLabelledItem(Msg.getString("TabPanelWeather.solarIrradiance.label"), solarIrradianceLabel, null);
        
        opticalDepthLabel = new JDoubleLabel(StyleManager.DECIMAL_PLACES2, surfaceFeatures.getOpticalDepth(locn));
        sunSpringPanel.addLabelledItem(Msg.getString("TabPanelWeather.opticalDepth.label"), opticalDepthLabel, null);
        
        zenithAngleLabel = new JDoubleLabel(StyleManager.DECIMAL_DEG, orbitInfo.getSolarZenithAngle(locn) * RADIANS_TO_DEGREES);
        sunSpringPanel.addLabelledItem(Msg.getString("TabPanelWeather.zenithAngle.label"), zenithAngleLabel, null);
        
        solarDeclinationLabel = new JDoubleLabel(StyleManager.DECIMAL_DEG, orbitInfo.getSolarDeclinationAngleInDeg());
        sunSpringPanel.addLabelledItem(Msg.getString("TabPanelWeather.solarDeclination.label"), solarDeclinationLabel, null);
   
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

		pressureTF.setValue(weather.getAirPressure(locn));
		temperatureLabel.setValue(weather.getTemperature(locn));
		windSpeedLabel.setValue(weather.getWindSpeed(locn));
		airDensityLabel.setValue(weather.getAirDensity(locn));
		opticalDepthLabel.setValue(surfaceFeatures.getOpticalDepth(locn));
		zenithAngleLabel.setValue(orbitInfo.getSolarZenithAngle(locn) * RADIANS_TO_DEGREES);
		solarDeclinationLabel.setValue(orbitInfo.getSolarDeclinationAngleInDeg());
		solarIrradianceLabel.setValue(surfaceFeatures.getSolarIrradiance(locn));

		int wd = weather.getWindDirection(locn);
		if (windDirectionCache != wd) {
			windDirectionCache = wd;
			windDirLabel.setText(StyleManager.DECIMAL_DEG.format(windDirectionCache));
		}
    }
}

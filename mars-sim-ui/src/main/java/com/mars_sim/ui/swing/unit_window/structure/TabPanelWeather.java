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
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import com.mars_sim.core.Simulation;
import com.mars_sim.core.Unit;
import com.mars_sim.core.environment.OrbitInfo;
import com.mars_sim.core.environment.SurfaceFeatures;
import com.mars_sim.core.environment.Weather;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.time.MasterClock;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.MainDesktopPane;
import com.mars_sim.ui.swing.MarsPanelBorder;
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
	
//	private static final String DUSTY_SKY = "large/dusty";
//	private static final String FRIGID = "large/frigid";
//	private static final String HAZE = "large/haze";
//	private static final String HOT = "large/hot";
//	private static final String LIGHTNING = "large/lightning";	
//	private static final String SNOW_BLOWING = "large/snow_blowing";
//	private static final String SUN_STORM = "large/sun_storm";
//	private static final String SNOW = "large/now";
//	private static final String SUNNY = "large/sunny";
//	private static final String WIND = "large/windy"; 


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

//	private String iconCache;

	private JLabel latitudeLabel;
	private JLabel longitudeLabel;
	private JLabel weatherLabel;
	private JLabel airDensityLabel;
	private JLabel pressureTF;
	private JLabel solarIrradianceLabel;
	private JLabel windSpeedLabel;
	private JLabel windDirLabel;
	private JLabel opticalDepthLabel;
	private JLabel zenithAngleLabel;
	private JLabel solarDeclinationLabel;

	private JLabel temperatureLabel;
	
	private Coordinates locationCache;
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
    public TabPanelWeather(Unit unit, MainDesktopPane desktop) {
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

        // Initialize location cache
        locationCache = getUnit().getCoordinates();

        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setBorder(new MarsPanelBorder());
        content.add(mainPanel, BorderLayout.NORTH);
		   
		// Create location panel
		AttributePanel locnPanel = new AttributePanel(2);
		latitudeLabel = locnPanel.addRow("Lat", getLatitudeString());
		longitudeLabel = locnPanel.addRow("Lon", getLongitudeString());

      	// Create weatherPanel
        JPanel centerEastPanel = new JPanel(new BorderLayout(5, 5));
        centerEastPanel.setBorder(new EmptyBorder(2, 2, 2, 2));
        centerEastPanel.add(locnPanel, BorderLayout.NORTH);
        
        JPanel eastPanel = new JPanel(new BorderLayout(0, 10));       
        mainPanel.add(eastPanel, BorderLayout.EAST);
        eastPanel.add(centerEastPanel, BorderLayout.CENTER);

        // Create imgPanel
    	JPanel imgPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 5));
        weatherLabel = new JLabel();
    	imgPanel.add(weatherLabel, SwingConstants.CENTER);
    	centerEastPanel.add(imgPanel, BorderLayout.SOUTH);

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

    public double getTemperature() {
		return weather.getTemperature(locationCache);
    }

    public double getAirPressure() {
    	return weather.getAirPressure(locationCache);
    }

    public double getWindSpeed() {
		return weather.getWindSpeed(locationCache);
    }

    public int getWindDirection() {
		return weather.getWindDirection(locationCache);
    }

    public double getOpticalDepth() {
 		return surfaceFeatures.getOpticalDepth(locationCache);
     }

    public double getZenithAngle() {
 		return orbitInfo.getSolarZenithAngle(locationCache);
     }

    public double getSolarDeclination() {
 		return orbitInfo.getSolarDeclinationAngleInDeg();
     }


    /**
     * Gets the air density in g/m3.
     * 
     * @return
     */
    public double getAirDensity() {
		return weather.getAirDensity(locationCache);
    }

    public double getSolarIrradiance() {
  		return surfaceFeatures.getSolarIrradiance(locationCache);
      }

	private String getLatitudeString() {
		return locationCache.getFormattedLatitudeString();
	}

	private String getLongitudeString() {
		return locationCache.getFormattedLongitudeString();
	}

    /**
     * Updates the info on this panel.
     */
	@Override
    public void update() {
    	Coordinates location = getUnit().getCoordinates();
        if (!masterClock.isPaused()) {

	        // If unit's location has changed, update location display.
        	
	    	// Future: if a person goes outside in settlement vicinity for servicing an equipment,
	    	//         does the coordinates (down to how many decimals) change ?
        	
	        if (!locationCache.equals(location)) {
	            locationCache = location;
	            latitudeLabel.setText(getLatitudeString());
	            longitudeLabel.setText(getLongitudeString());
	        }

			double p = getAirPressure();
	        if (airPressureCache != p) {
	        	airPressureCache = p;
	        	pressureTF.setText(StyleManager.DECIMAL_KPA.format(airPressureCache));
	        }

	        double t = getTemperature();
	        if (temperatureCache != t) {
	        	temperatureCache = t;
	        	temperatureLabel.setText(StyleManager.DECIMAL_CELCIUS.format(temperatureCache));
	        }
 
	        int wd = getWindDirection();
	        if (windDirectionCache != wd) {
	        	windDirectionCache = wd;
	        	windDirLabel.setText(StyleManager.DECIMAL_DEG.format(windDirectionCache));
	        }

	        double s = getWindSpeed();
	        if (windSpeedCache != s) {
	        	windSpeedCache = s;
	        	windSpeedLabel.setText(StyleManager.DECIMAL_M_S.format(windSpeedCache));
	        }

	        double ad = getAirDensity();
	        if (airDensityCache != ad) {
	        	airDensityCache = ad;
	        	airDensityLabel.setText(StyleManager.DECIMAL_G_M3.format(airDensityCache));
	        }

	        double od = getOpticalDepth();
	        if (opticalDepthCache != od) {
	        	opticalDepthCache = od;
	        	opticalDepthLabel.setText(StyleManager.DECIMAL_PLACES2.format(opticalDepthCache));
	        }

	        //////////////////////////////////////////////
//	       	String icon = null;
//
//	    	if (temperatureCache <= 0) {
//	    		if (temperatureCache < -40)
//	    			icon = FRIGID;
//	    		else {
//	    			if (windSpeedCache > 6D)
//	    				icon = SNOW_BLOWING;
//	    			else
//	    				icon = SNOW;
//	    		}
//	    	}
//	    	else if (temperatureCache >= 26)
//	    		icon = HOT;
//	    	else { //if (temperatureCache >= 0) {
//	    		if (windSpeedCache > 20D) {
//	    			icon = WIND;
//	    		}
//	    		else if (opticalDepthCache > 1D) {
//			    	if (opticalDepthCache > 3D)
//			    		icon = DUSTY_SKY;
//			    	else
//			    		icon = HAZE;
//		    	}
//	    		else
//	    			icon = SUNNY;
//	    	}
//	
//	    	if (!icon.equals(iconCache)) {
//	    		iconCache = icon;
//	    		weatherLabel.setIcon(ImageLoader.getIconByName(icon));
//	    	}

	        double za = getZenithAngle();
	        if (zenithAngleCache != za) {
	        	zenithAngleCache = za;
	        	zenithAngleLabel.setText(StyleManager.DECIMAL_DEG.format(zenithAngleCache * RADIANS_TO_DEGREES));
	        }

	        double sd = getSolarDeclination();
	        if (solarDeclinationCache != sd) {
	        	solarDeclinationCache = sd;
	        	solarDeclinationLabel.setText(StyleManager.DECIMAL_DEG.format(solarDeclinationCache));
	        }

	        double ir = getSolarIrradiance();
	        if (solarIrradianceCache != ir) {
	        	solarIrradianceCache = ir;
	        	solarIrradianceLabel.setText(StyleManager.DECIMAL_W_M2.format(solarIrradianceCache));
	        }
	    }
    }

	/**
     * Prepares object for garbage collection.
     */
    @Override
    public void destroy() {
    	super.destroy();
    	
    	airDensityLabel = null;
    	pressureTF = null;
    	solarIrradianceLabel = null;
    	windSpeedLabel = null;
    	windDirLabel = null;
    	opticalDepthLabel = null;
    	zenithAngleLabel = null;
    	solarDeclinationLabel = null;
    	temperatureLabel = null;
    	latitudeLabel = null;
    	longitudeLabel = null;
    	weatherLabel = null;
    	locationCache = null;
    }
}

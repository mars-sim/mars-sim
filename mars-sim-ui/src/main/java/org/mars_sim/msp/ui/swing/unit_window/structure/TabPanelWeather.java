/*
 * Mars Simulation Project
 * TabPanelWeather.java
 * @date 2022-07-09
 * @author Manny Kung
 */
package org.mars_sim.msp.ui.swing.unit_window.structure;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.environment.OrbitInfo;
import org.mars_sim.msp.core.environment.SurfaceFeatures;
import org.mars_sim.msp.core.environment.Weather;
import org.mars_sim.msp.core.time.MasterClock;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.StyleManager;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;
import org.mars_sim.msp.ui.swing.utils.AttributePanel;

/**
 * The TabPanelWeather is a tab panel for location information.
 */
@SuppressWarnings("serial")
public class TabPanelWeather
extends TabPanel {

	private static final String WEATHER_ICON = "weather";
	
	private static final String DUSTY_SKY = "large/dusty";
	private static final String SUNNY = "large/sunny";
	private static final String HOT = "large/hot";
	private static final String SNOW_BLOWING = "large/snow_blowing";
	private static final String SNOW = "large/now";
	private static final String WIND = "large/windy"; 
	private static final String FRIGID = "large/frigid";
	private static final String HAZE = "large/haze";

	private static final double RADIANS_TO_DEGREES = 180D/Math.PI;
	
	private JLabel airDensityTF;
	private JLabel pressureTF;
	private JLabel solarIrradianceTF;
	private JLabel windSpeedTF;
	private JLabel windDirTF;
	private JLabel opticalDepthTF;
	private JLabel zenithAngleTF;
	private JLabel solarDeclinationTF;

	private JLabel temperatureValueLabel;

	private double airPressureCache;
	private double temperatureCache;
	private double windSpeedCache;
	private double airDensityCache;
	private double opticalDepthCache;
	private double zenithAngleCache;
	private double solarDeclinationCache;
	private double solarIrradianceCache;

	private int windDirectionCache;
	
	private String iconCache;

	private JLabel latitudeLabel;
	private JLabel longitudeLabel;
	private JLabel weatherLabel;

	private Coordinates locationCache;

	private Weather weather;
	private SurfaceFeatures surfaceFeatures;
	private OrbitInfo orbitInfo;
	private MasterClock masterClock;
		
    /**
     * Constructor.
     * @param unit the unit to display.
     * @param desktop the main desktop.
     */
    public TabPanelWeather(Unit unit, MainDesktopPane desktop) {
        // Use the TabPanel constructor
        super(
    		null,
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
		latitudeLabel = locnPanel.addTextField("Lat", getLatitudeString(), null);
		longitudeLabel = locnPanel.addTextField("Long", getLongitudeString(), null);

      	// Create weatherPanel
        JPanel centerEastPanel = new JPanel(new BorderLayout(0, 0));
        centerEastPanel.setBorder(new EmptyBorder(1, 1, 1, 1));
        centerEastPanel.add(locnPanel, BorderLayout.NORTH);
        
        JPanel eastPanel = new JPanel(new BorderLayout(0, 10));       
        mainPanel.add(eastPanel, BorderLayout.EAST);
        eastPanel.add(centerEastPanel, BorderLayout.CENTER);

        // Create imgPanel
    	JPanel imgPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
        weatherLabel = new JLabel();
    	imgPanel.add(weatherLabel, JLabel.CENTER);
    	centerEastPanel.add(imgPanel, BorderLayout.SOUTH);

    	// Prepare temperature panel
        JPanel temperaturePanel = new JPanel(new FlowLayout());
        centerEastPanel.add(temperaturePanel, BorderLayout.CENTER);

        // Prepare temperature label
        temperatureValueLabel = new JLabel(getTemperatureString(getTemperature()), JLabel.CENTER);
        temperatureValueLabel.setOpaque(false);
        temperaturePanel.add(temperatureValueLabel);

        JPanel metricsPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.add(metricsPanel, BorderLayout.CENTER);

        // Create spring layout panel
        AttributePanel springPanel = new AttributePanel(8);
        metricsPanel.add(springPanel, BorderLayout.NORTH);

        pressureTF =  springPanel.addTextField(Msg.getString("TabPanelWeather.airPressure.label"), "", null);
        airDensityTF = springPanel.addTextField(Msg.getString("TabPanelWeather.airDensity.label"), "", null);
        windSpeedTF = springPanel.addTextField(Msg.getString("TabPanelWeather.windspeed.label"), "", null);
        windDirTF = springPanel.addTextField(Msg.getString("TabPanelWeather.windDirection.label"), "", null);
        solarIrradianceTF = springPanel.addTextField(Msg.getString("TabPanelWeather.solarIrradiance.label"), "", null);
        opticalDepthTF = springPanel.addTextField(Msg.getString("TabPanelWeather.opticalDepth.label"), "", null);
        zenithAngleTF = springPanel.addTextField(Msg.getString("TabPanelWeather.zenithAngle.label"), "", null);
        solarDeclinationTF = springPanel.addTextField(Msg.getString("TabPanelWeather.solarDeclination.label"), "", null);
    }


    public String getTemperatureString(double value) {
    	// Use Msg.getString for the degree sign
    	// Change from " °C" to " �C" for English Locale
    	return StyleManager.DECIMAL_PLACES0.format(value) + " " + Msg.getString("temperature.sign.degreeCelsius"); //$NON-NLS-1$
    }

    public double getTemperature() {
		return weather.getTemperature(locationCache);
    }

    public String getAirPressureString(double value) {
    	return StyleManager.DECIMAL_PLACES2.format(value) + " " + Msg.getString("pressure.unit.kPa"); //$NON-NLS-1$
    }

    public double getAirPressure() {
    	return Math.round(weather.getAirPressure(locationCache) *100.0) / 100.0;
    }

    public String getWindSpeedString(double value) {
    	return StyleManager.DECIMAL_PLACES2.format(value) + " " + Msg.getString("windspeed.unit.meterpersec"); //$NON-NLS-1$
    }

    public double getWindSpeed() {
		return weather.getWindSpeed(locationCache);
    }

    public int getWindDirection() {
		return weather.getWindDirection(locationCache);
    }

    public String getWindDirectionString(double value) {
     	return StyleManager.DECIMAL_PLACES0.format(value) + " " + Msg.getString("windDirection.unit.deg"); //$NON-NLS-1$
    }

    public double getOpticalDepth() {
 		return surfaceFeatures.getOpticalDepth(locationCache);
     }

    public String getOpticalDepthString(double value) {
     	return StyleManager.DECIMAL_PLACES2.format(value);
    }

    public double getZenithAngle() {
 		return orbitInfo.getSolarZenithAngle(locationCache);
     }

    public String getZenithAngleString(double value) {
     	return StyleManager.DECIMAL_PLACES2.format(value * RADIANS_TO_DEGREES) + " " + Msg.getString("direction.degreeSign"); //$NON-NLS-1$
    }

    public double getSolarDeclination() {
 		return orbitInfo.getSolarDeclinationAngleDegree();
     }

    public String getSolarDeclinationString(double value) {
     	return StyleManager.DECIMAL_PLACES2.format(value) + " " + Msg.getString("direction.degreeSign"); //$NON-NLS-1$
    }

    public double getAirDensity() {
		return weather.getAirDensity(locationCache);
    }

    public String getAirDensityString(double value) {
     	return StyleManager.DECIMAL_PLACES2.format(value) + " " + Msg.getString("airDensity.unit.gperm3"); //$NON-NLS-1$
    }

    public double getSolarIrradiance() {
  		return surfaceFeatures.getSolarIrradiance(locationCache);
      }

     public String getSolarIrradianceString(double value) {
      	return StyleManager.DECIMAL_PLACES2.format(value) + " " + Msg.getString("solarIrradiance.unit"); //$NON-NLS-1$
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
	    	// TODO: if a person goes outside the settlement for servicing an equipment
	    	// does the coordinate (down to how many decimal) change?
	        if (!locationCache.equals(location)) {
	            locationCache = location;
	            latitudeLabel.setText(getLatitudeString());
	            longitudeLabel.setText(getLongitudeString());
	        }

			double p =  Math.round(getAirPressure()*100.0)/100.0;
	        if (airPressureCache != p) {
	        	airPressureCache = p;
	        	pressureTF.setText(" " + getAirPressureString(airPressureCache));
	        }

	        double t =  Math.round(getTemperature()*100.0)/100.0;
	        if (temperatureCache != t) {
	        	temperatureCache = t;
	        	temperatureValueLabel.setText(getTemperatureString(temperatureCache));
	        }
 
	        int wd = getWindDirection();
	        if (windDirectionCache != wd) {
	        	windDirectionCache = wd;
	        	windDirTF.setText(" " + getWindDirectionString(windDirectionCache));
	        }

	        double s = Math.round(getWindSpeed()*100.0)/100.0;
	        if (windSpeedCache != s) {
	        	windSpeedCache = s;
	        	windSpeedTF.setText(" " + getWindSpeedString(windSpeedCache));
	        }

	        double ad =  Math.round(getAirDensity()*100.0)/100.0;
	        if (airDensityCache != ad) {
	        	airDensityCache = ad;
	        	airDensityTF.setText(" " + getAirDensityString(airDensityCache));
	        }

	        double od =  Math.round(getOpticalDepth()*100.0)/100.0;
	        if (opticalDepthCache != od) {
	        	opticalDepthCache = od;
	        	opticalDepthTF.setText(" " + getOpticalDepthString(opticalDepthCache));
	        }

	        //////////////////////////////////////////////
	        
	       	String icon = null;

	    	if (temperatureCache <= 0) {
	    		if (temperatureCache < -40)
	    			icon = FRIGID;
	    		else {
	    			if (windSpeedCache > 6D)
	    				icon = SNOW_BLOWING;
	    			else
	    				icon = SNOW;
	    		}
	    	}
	    	else if (temperatureCache >= 26)
	    		icon = HOT;
	    	else { //if (temperatureCache >= 0) {
	    		if (windSpeedCache > 20D) {
	    			icon = WIND;
	    		}
	    		else if (opticalDepthCache > 1D) {
			    	if (opticalDepthCache > 3D)
			    		icon = DUSTY_SKY;
			    	else
			    		icon = HAZE;
		    	}
	    		else
	    			icon = SUNNY;
	    	}

	    	//////////////////////////////////////////////
	    	
	    	if (!icon.equals(iconCache)) {
	    		iconCache = icon;
	    		weatherLabel.setIcon(ImageLoader.getIconByName(icon));
	    	}

	        double za = getZenithAngle();
	        if (zenithAngleCache != za) {
	        	zenithAngleCache = za;
	        	zenithAngleTF.setText(" " + getZenithAngleString(zenithAngleCache));
	        }

	        double sd = getSolarDeclination();
	        if (solarDeclinationCache != sd) {
	        	solarDeclinationCache = sd;
	        	solarDeclinationTF.setText(" " + getSolarDeclinationString(solarDeclinationCache));
	        }

	        double ir = getSolarIrradiance();
	        if (solarIrradianceCache != ir) {
	        	solarIrradianceCache = ir;
	        	solarIrradianceTF.setText(" " + getSolarIrradianceString(solarIrradianceCache));
	        }
	    }
    }

	/**
     * Prepare object for garbage collection.
     */
    @Override
    public void destroy() {
    	super.destroy();
    	
    	airDensityTF = null;
    	pressureTF = null;
    	solarIrradianceTF = null;
    	windSpeedTF = null;
    	windDirTF = null;
    	opticalDepthTF = null;
    	zenithAngleTF = null;
    	solarDeclinationTF = null;

    	temperatureValueLabel = null;

    	latitudeLabel = null;
    	longitudeLabel = null;
    	weatherLabel = null;

    	locationCache = null;
    }
}

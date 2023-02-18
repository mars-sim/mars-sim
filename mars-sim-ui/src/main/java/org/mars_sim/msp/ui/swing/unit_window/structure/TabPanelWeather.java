/*
 * Mars Simulation Project
 * TabPanelWeather.java
 * @date 2022-07-09
 * @author Manny Kung
 */
package org.mars_sim.msp.ui.swing.unit_window.structure;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
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
import org.mars_sim.msp.ui.swing.tool.SpringUtilities;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;

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
	
	private JTextField airDensityTF;
	private JTextField pressureTF;
	private JTextField solarIrradianceTF;
	private JTextField windSpeedTF;
	private JTextField windDirTF;
	private JTextField opticalDepthTF;
	private JTextField zenithAngleTF;
	private JTextField solarDeclinationTF;

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

	private JPanel coordsPanel;
	private JPanel latlonPanel;

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

        coordsPanel = new JPanel(new BorderLayout(0, 0));

        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setBorder(new MarsPanelBorder());
        content.add(mainPanel, BorderLayout.NORTH);
		        
        // Prepare latitude label
        latitudeLabel = new JLabel(getLatitudeString());
        latitudeLabel.setOpaque(false);
        latitudeLabel.setHorizontalAlignment(SwingConstants.LEFT);
        coordsPanel.add(latitudeLabel, BorderLayout.NORTH);

        // Prepare longitude label
        longitudeLabel = new JLabel(getLongitudeString());
        longitudeLabel.setOpaque(false);
        longitudeLabel.setHorizontalAlignment(SwingConstants.LEFT);
        coordsPanel.add(longitudeLabel, BorderLayout.CENTER);

        latlonPanel = new JPanel();
        latlonPanel.setLayout(new BorderLayout(0, 0));
        JLabel latLabel = new JLabel("Lat : ");//, JLabel.RIGHT);
        latLabel.setHorizontalAlignment(SwingConstants.CENTER);
        JLabel longLabel = new JLabel("Lon : ");
        longLabel.setHorizontalAlignment(SwingConstants.CENTER);
        latlonPanel.add(latLabel, BorderLayout.CENTER);
        latlonPanel.add(longLabel, BorderLayout.SOUTH);

        // Create location panel
        JPanel northEastPanel = new JPanel(new GridLayout(1, 2)); 
        northEastPanel.setBorder(new EmptyBorder(1, 1, 1, 1));
        northEastPanel.add(latlonPanel);
        northEastPanel.add(coordsPanel);

      	// Create weatherPanel
        JPanel centerEastPanel = new JPanel(new BorderLayout(0, 0));
        centerEastPanel.setBorder(new EmptyBorder(1, 1, 1, 1));
        centerEastPanel.add(northEastPanel, BorderLayout.NORTH);
        
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
        JPanel springPanel = new JPanel(new SpringLayout());
        metricsPanel.add(springPanel, BorderLayout.NORTH);

        // Prepare air pressure label
        JLabel airPressureLabel = new JLabel(Msg.getString("TabPanelWeather.airPressure.label"), JLabel.RIGHT);
        airPressureLabel.setOpaque(false);
        springPanel.add(airPressureLabel);

		JPanel wrapper1 = new JPanel(new FlowLayout(0, 0, FlowLayout.LEFT));
        pressureTF = new JTextField();
        pressureTF.setEditable(false);
        pressureTF.setColumns(8);
        wrapper1.add(pressureTF);
        springPanel.add(wrapper1);

        // Prepare air density label
        JLabel airDensityLabel = new JLabel(Msg.getString("TabPanelWeather.airDensity.label"), JLabel.RIGHT);
        airDensityLabel.setOpaque(false);
        springPanel.add(airDensityLabel);

		JPanel wrapper2 = new JPanel(new FlowLayout(0, 0, FlowLayout.LEFT));
        airDensityTF = new JTextField();
        airDensityTF.setEditable(false);
        airDensityTF.setColumns(8);
        airDensityTF.setOpaque(false);
        wrapper2.add(airDensityTF);
        springPanel.add(wrapper2);

        JLabel windSpeedLabel = new JLabel(Msg.getString("TabPanelWeather.windspeed.label"), JLabel.RIGHT);
        windSpeedLabel.setOpaque(false);
        springPanel.add(windSpeedLabel);

		JPanel wrapper3 = new JPanel(new FlowLayout(0, 0, FlowLayout.LEFT));
        windSpeedTF = new JTextField(getWindSpeedString(0.0));
        windSpeedTF.setEditable(false);
        windSpeedTF.setColumns(8);
        wrapper3.add(windSpeedTF);
        springPanel.add(wrapper3);

        JLabel windDirLabel = new JLabel(Msg.getString("TabPanelWeather.windDirection.label"), JLabel.RIGHT);
        windDirLabel.setOpaque(false);
        springPanel.add(windDirLabel);

		JPanel wrapper4 = new JPanel(new FlowLayout(0, 0, FlowLayout.LEFT));
        windDirTF = new JTextField();
        windDirTF.setEditable(false);
        windDirTF.setColumns(8);
        wrapper4.add(windDirTF);
        springPanel.add(wrapper4);

        JLabel solarIrradianceLabel = new JLabel(Msg.getString("TabPanelWeather.solarIrradiance.label"), JLabel.RIGHT);
        solarIrradianceLabel.setOpaque(false);
        springPanel.add(solarIrradianceLabel);

		JPanel wrapper5 = new JPanel(new FlowLayout(0, 0, FlowLayout.LEFT));
        solarIrradianceTF = new JTextField(getSolarIrradianceString(0.0));
        solarIrradianceTF.setEditable(false);
        solarIrradianceTF.setColumns(8);
        solarIrradianceTF.setOpaque(false);
        wrapper5.add(solarIrradianceTF);
        springPanel.add(wrapper5);

        JLabel opticalDepthLabel = new JLabel(Msg.getString("TabPanelWeather.opticalDepth.label"), JLabel.RIGHT);
        opticalDepthLabel.setOpaque(false);
        springPanel.add(opticalDepthLabel);

		JPanel wrapper6 = new JPanel(new FlowLayout(0, 0, FlowLayout.LEFT));
        opticalDepthTF = new JTextField();
        opticalDepthTF.setEditable(false);
        opticalDepthTF.setColumns(8);
        opticalDepthTF.setOpaque(false);
        wrapper6.add(opticalDepthTF);
        springPanel.add(wrapper6);

        JLabel zenithAngleLabel = new JLabel(Msg.getString("TabPanelWeather.zenithAngle.label"), JLabel.RIGHT);
        zenithAngleLabel.setOpaque(false);
        springPanel.add(zenithAngleLabel);

		JPanel wrapper7 = new JPanel(new FlowLayout(0, 0, FlowLayout.LEFT));
        zenithAngleTF = new JTextField();
        zenithAngleTF.setEditable(false);
        zenithAngleTF.setColumns(8);
        wrapper7.add(zenithAngleTF);
        springPanel.add(wrapper7);

        JLabel solarDeclinationLabel = new JLabel(Msg.getString("TabPanelWeather.solarDeclination.label"), JLabel.RIGHT);
        solarDeclinationLabel.setOpaque(false);
        springPanel.add(solarDeclinationLabel);

		JPanel wrapper8 = new JPanel(new FlowLayout(0, 0, FlowLayout.LEFT));
		solarDeclinationTF = new JTextField();
        solarDeclinationTF.setEditable(false);
        solarDeclinationTF.setColumns(8);
        wrapper8.add(solarDeclinationTF);
        springPanel.add(wrapper8);

		//Lay out the spring panel.
		SpringUtilities.makeCompactGrid(springPanel,
		                                8, 2, //rows, cols
		                                0, 30,        //initX, initY
		                                5, 5);       //xPad, yPad
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
    	
    	coordsPanel = null;
    	latlonPanel = null;

    	latitudeLabel = null;
    	longitudeLabel = null;
    	weatherLabel = null;

    	locationCache = null;
    }
}

/*
 * Mars Simulation Project
 * TabPanelWeather.java
 * @date 2022-07-09
 * @author Manny Kung
 */
package org.mars_sim.msp.ui.swing.unit_window.structure;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.text.DecimalFormat;

import javax.swing.JPanel;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.environment.OrbitInfo;
import org.mars_sim.msp.core.environment.SurfaceFeatures;
import org.mars_sim.msp.core.environment.Weather;
import org.mars_sim.msp.core.time.MasterClock;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.tool.SpringUtilities;
//import org.mars_sim.msp.ui.swing.tool.MarsViewer;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;

import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;
import com.alee.laf.text.WebTextField;

/**
 * The TabPanelWeather is a tab panel for location information.
 */
@SuppressWarnings("serial")
public class TabPanelWeather
extends TabPanel {

	private static final String WEATHER_ICON = Msg.getString("icon.weather"); //$NON-NLS-1$
	
	private static final String DUSTY_SKY = Msg.getString("img.dust128"); //$NON-NLS-1$
	private static final String SUNNY = Msg.getString("img.sunny128"); //$NON-NLS-1$
	private static final String BALMY = Msg.getString("img.hot128"); //$NON-NLS-1$
//	private static final String LIGHTNING = Msg.getString("img.lightning128"); //$NON-NLS-1$

	private static final String SNOW_BLOWING = Msg.getString("img.snow_blowing"); //$NON-NLS-1$
//	private static final String SUN_STORM = Msg.getString("img.sun_storm"); //$NON-NLS-1$
	private static final String SNOWFLAKE = Msg.getString("img.thermometer_snowflake"); //$NON-NLS-1$
	private static final String WIND_FLAG = Msg.getString("img.wind_flag_storm"); //$NON-NLS-1$
	private static final String FRIGID = Msg.getString("img.frigid"); //$NON-NLS-1$
	private static final String HAZE = Msg.getString("img.haze"); //$NON-NLS-1$

	private static final double RADIANS_TO_DEGREES = 180D/Math.PI;
	
	private WebTextField airDensityTF;
	private WebTextField pressureTF;
	private WebTextField solarIrradianceTF;
	private WebTextField windSpeedTF;
	private WebTextField windDirTF;
	private WebTextField opticalDepthTF;
	private WebTextField zenithAngleTF;
	private WebTextField solarDeclinationTF;

	private WebLabel temperatureValueLabel;

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

	private WebPanel coordsPanel;
	private WebPanel latlonPanel;

	private WebLabel latitudeLabel;
	private WebLabel longitudeLabel;
	private WebLabel weatherLabel;

	private Coordinates locationCache;

	private Weather weather = getMars().getWeather();
	private SurfaceFeatures surfaceFeatures = getMars().getSurfaceFeatures();
	private OrbitInfo orbitInfo = getMars().getOrbitInfo();
	private MasterClock masterClock = getSimulation().getMasterClock();
	
	private static DecimalFormat fmt = new DecimalFormat("##0");
	
    /**
     * Constructor.
     * @param unit the unit to display.
     * @param desktop the main desktop.
     */
    public TabPanelWeather(Unit unit, MainDesktopPane desktop) {
        // Use the TabPanel constructor
        super(
    		null,
    		ImageLoader.getNewIcon(WEATHER_ICON),
    		Msg.getString("TabPanelWeather.title"), //$NON-NLS-1$
    		unit, desktop
    	);
	}
	
	@Override
	protected void buildUI(JPanel content) {

        // Initialize location cache
        locationCache = getUnit().getCoordinates();

        coordsPanel = new WebPanel(new BorderLayout(0, 0));

        WebPanel mainPanel = new WebPanel(new BorderLayout(0, 0));
        mainPanel.setBorder(new MarsPanelBorder());
        content.add(mainPanel, BorderLayout.NORTH);
		
        Font font = new Font("Serif", Font.BOLD, 15);
        
        // Prepare latitude label
        latitudeLabel = new WebLabel(getLatitudeString());
        latitudeLabel.setOpaque(false);
        latitudeLabel.setFont(font);
        latitudeLabel.setHorizontalAlignment(SwingConstants.LEFT);
        coordsPanel.add(latitudeLabel, BorderLayout.NORTH);

        // Prepare longitude label
        longitudeLabel = new WebLabel(getLongitudeString());
        longitudeLabel.setOpaque(false);
        longitudeLabel.setFont(font);
        longitudeLabel.setHorizontalAlignment(SwingConstants.LEFT);
        coordsPanel.add(longitudeLabel, BorderLayout.CENTER);

        latlonPanel = new WebPanel();
        latlonPanel.setLayout(new BorderLayout(0, 0));
        WebLabel latLabel = new WebLabel("Lat : ");//, JLabel.RIGHT);
        latLabel.setFont(font);
        latLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        WebLabel longLabel = new WebLabel("Lon : ");
        longLabel.setFont(font);
        longLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        latlonPanel.add(latLabel, BorderLayout.CENTER);
        latlonPanel.add(longLabel, BorderLayout.SOUTH);

        // Create location panel
        WebPanel northEastPanel = new WebPanel(new GridLayout(1, 2)); 
        northEastPanel.setBorder(new EmptyBorder(1, 1, 1, 1));
        northEastPanel.add(latlonPanel);
        northEastPanel.add(coordsPanel);

      	// Create weatherPanel
        WebPanel centerEastPanel = new WebPanel(new BorderLayout(0, 0));
        centerEastPanel.setBorder(new EmptyBorder(1, 1, 1, 1));
        centerEastPanel.add(northEastPanel, BorderLayout.NORTH);
        
        WebPanel eastPanel = new WebPanel(new BorderLayout(0, 10));       
        mainPanel.add(eastPanel, BorderLayout.EAST);
        eastPanel.add(centerEastPanel, BorderLayout.CENTER);

        // Create imgPanel
    	WebPanel imgPanel = new WebPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
        weatherLabel = new WebLabel();
    	imgPanel.add(weatherLabel, WebLabel.CENTER);
    	centerEastPanel.add(imgPanel, BorderLayout.SOUTH);

    	// Prepare temperature panel
        WebPanel temperaturePanel = new WebPanel(new FlowLayout());
        centerEastPanel.add(temperaturePanel, BorderLayout.CENTER);

        // Prepare temperature label
        temperatureValueLabel = new WebLabel(getTemperatureString(getTemperature()), WebLabel.CENTER);
        temperatureValueLabel.setOpaque(false);
        temperatureValueLabel.setFont(new Font("Serif", Font.BOLD, 28));
//        temperatureValueLabel.setHorizontalAlignment(SwingConstants.CENTER);
        temperaturePanel.add(temperatureValueLabel);

        WebPanel metricsPanel = new WebPanel(new BorderLayout(0, 0));
        mainPanel.add(metricsPanel, BorderLayout.CENTER);

        // Create spring layout panel
        WebPanel springPanel = new WebPanel(new SpringLayout());
        metricsPanel.add(springPanel, BorderLayout.NORTH);

        // Prepare air pressure label
        WebLabel airPressureLabel = new WebLabel(Msg.getString("TabPanelWeather.airPressure.label"), WebLabel.RIGHT);
        airPressureLabel.setOpaque(false);
        airPressureLabel.setFont(new Font("Serif", Font.PLAIN, 12));
        springPanel.add(airPressureLabel);

		WebPanel wrapper1 = new WebPanel(new FlowLayout(0, 0, FlowLayout.LEFT));
        pressureTF = new WebTextField();
        pressureTF.setEditable(false);
        pressureTF.setColumns(8);
        pressureTF.setFont(new Font("Serif", Font.PLAIN, 12));
        wrapper1.add(pressureTF);
        springPanel.add(wrapper1);

        // Prepare air density label
        WebLabel airDensityLabel = new WebLabel(Msg.getString("TabPanelWeather.airDensity.label"), WebLabel.RIGHT);
        airDensityLabel.setOpaque(false);
        airDensityLabel.setFont(new Font("Serif", Font.PLAIN, 12));
        springPanel.add(airDensityLabel);

		WebPanel wrapper2 = new WebPanel(new FlowLayout(0, 0, FlowLayout.LEFT));
        airDensityTF = new WebTextField();
        airDensityTF.setEditable(false);
        airDensityTF.setColumns(8);
        airDensityTF.setOpaque(false);
        airDensityTF.setFont(new Font("Serif", Font.PLAIN, 12));
        wrapper2.add(airDensityTF);
        springPanel.add(wrapper2);

        WebLabel windSpeedLabel = new WebLabel(Msg.getString("TabPanelWeather.windspeed.label"), WebLabel.RIGHT);
        windSpeedLabel.setOpaque(false);
        windSpeedLabel.setFont(new Font("Serif", Font.PLAIN, 12));
        springPanel.add(windSpeedLabel);

		WebPanel wrapper3 = new WebPanel(new FlowLayout(0, 0, FlowLayout.LEFT));
        windSpeedTF = new WebTextField(getWindSpeedString(0.0));
        windSpeedTF.setEditable(false);
        windSpeedTF.setColumns(8);
        windSpeedTF.setFont(new Font("Serif", Font.PLAIN, 12));
        wrapper3.add(windSpeedTF);
        springPanel.add(wrapper3);

        WebLabel windDirLabel = new WebLabel(Msg.getString("TabPanelWeather.windDirection.label"), WebLabel.RIGHT);
        windDirLabel.setOpaque(false);
        windDirLabel.setFont(new Font("Serif", Font.PLAIN, 12));
        springPanel.add(windDirLabel);

		WebPanel wrapper4 = new WebPanel(new FlowLayout(0, 0, FlowLayout.LEFT));
        windDirTF = new WebTextField();
        windDirTF.setEditable(false);
        windDirTF.setColumns(8);
        windDirTF.setFont(new Font("Serif", Font.PLAIN, 12));
        wrapper4.add(windDirTF);
        springPanel.add(wrapper4);

        WebLabel solarIrradianceLabel = new WebLabel(Msg.getString("TabPanelWeather.solarIrradiance.label"), WebLabel.RIGHT);
        solarIrradianceLabel.setOpaque(false);
        solarIrradianceLabel.setFont(new Font("Serif", Font.PLAIN, 12));
        springPanel.add(solarIrradianceLabel);

		WebPanel wrapper5 = new WebPanel(new FlowLayout(0, 0, FlowLayout.LEFT));
        solarIrradianceTF = new WebTextField(getSolarIrradianceString(0.0));
        solarIrradianceTF.setEditable(false);
        solarIrradianceTF.setColumns(8);
        solarIrradianceTF.setOpaque(false);
        solarIrradianceTF.setFont(new Font("Serif", Font.PLAIN, 12));
        wrapper5.add(solarIrradianceTF);
        springPanel.add(wrapper5);

        WebLabel opticalDepthLabel = new WebLabel(Msg.getString("TabPanelWeather.opticalDepth.label"), WebLabel.RIGHT);
        opticalDepthLabel.setOpaque(false);
        opticalDepthLabel.setFont(new Font("Serif", Font.PLAIN, 12));
        springPanel.add(opticalDepthLabel);

		WebPanel wrapper6 = new WebPanel(new FlowLayout(0, 0, FlowLayout.LEFT));
        opticalDepthTF = new WebTextField();
        opticalDepthTF.setEditable(false);
        opticalDepthTF.setColumns(8);
        opticalDepthTF.setOpaque(false);
        opticalDepthTF.setFont(new Font("Serif", Font.PLAIN, 12));
        wrapper6.add(opticalDepthTF);
        springPanel.add(wrapper6);

        WebLabel zenithAngleLabel = new WebLabel(Msg.getString("TabPanelWeather.zenithAngle.label"), WebLabel.RIGHT);
        zenithAngleLabel.setOpaque(false);
        zenithAngleLabel.setFont(new Font("Serif", Font.PLAIN, 12));
        springPanel.add(zenithAngleLabel);

		WebPanel wrapper7 = new WebPanel(new FlowLayout(0, 0, FlowLayout.LEFT));
        zenithAngleTF = new WebTextField();
        zenithAngleTF.setEditable(false);
        zenithAngleTF.setColumns(8);
        zenithAngleTF.setFont(new Font("Serif", Font.PLAIN, 12));
        wrapper7.add(zenithAngleTF);
        springPanel.add(wrapper7);

        WebLabel solarDeclinationLabel = new WebLabel(Msg.getString("TabPanelWeather.solarDeclination.label"), WebLabel.RIGHT);
        solarDeclinationLabel.setOpaque(false);
        solarDeclinationLabel.setFont(new Font("Serif", Font.PLAIN, 12));
        springPanel.add(solarDeclinationLabel);

		WebPanel wrapper8 = new WebPanel(new FlowLayout(0, 0, FlowLayout.LEFT));
		solarDeclinationTF = new WebTextField();
        solarDeclinationTF.setEditable(false);
        solarDeclinationTF.setColumns(8);
        solarDeclinationTF.setFont(new Font("Serif", Font.PLAIN, 12));
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
    	return fmt.format(value) + " " + Msg.getString("temperature.sign.degreeCelsius"); //$NON-NLS-1$
    }

    public double getTemperature() {
		return weather.getTemperature(locationCache);
    }

    public String getAirPressureString(double value) {
    	return DECIMAL_PLACES2.format(value) + " " + Msg.getString("pressure.unit.kPa"); //$NON-NLS-1$
    }

    public double getAirPressure() {
    	return Math.round(weather.getAirPressure(locationCache) *100.0) / 100.0;
    }

    public String getWindSpeedString(double value) {
    	return DECIMAL_PLACES2.format(value) + " " + Msg.getString("windspeed.unit.meterpersec"); //$NON-NLS-1$
    }

    public double getWindSpeed() {
		return weather.getWindSpeed(locationCache);
    }

    public int getWindDirection() {
		return weather.getWindDirection(locationCache);
    }

    public String getWindDirectionString(double value) {
     	return fmt.format(value) + " " + Msg.getString("windDirection.unit.deg"); //$NON-NLS-1$
    }

    public double getOpticalDepth() {
 		return surfaceFeatures.getOpticalDepth(locationCache);
     }

    public String getOpticalDepthString(double value) {
     	return DECIMAL_PLACES2.format(value);
    }

    public double getZenithAngle() {
 		return orbitInfo.getSolarZenithAngle(locationCache);
     }

    public String getZenithAngleString(double value) {
     	return DECIMAL_PLACES2.format(value * RADIANS_TO_DEGREES) + " " + Msg.getString("direction.degreeSign"); //$NON-NLS-1$
    }

    public double getSolarDeclination() {
 		return orbitInfo.getSolarDeclinationAngleDegree();
     }

    public String getSolarDeclinationString(double value) {
     	return DECIMAL_PLACES2.format(value) + " " + Msg.getString("direction.degreeSign"); //$NON-NLS-1$
    }

    public double getAirDensity() {
		return weather.getAirDensity(locationCache);
    }

    public String getAirDensityString(double value) {
     	return DECIMAL_PLACES2.format(value) + " " + Msg.getString("airDensity.unit.gperm3"); //$NON-NLS-1$
    }

    public double getSolarIrradiance() {
  		return surfaceFeatures.getSolarIrradiance(locationCache);
      }

     public String getSolarIrradianceString(double value) {
      	return DECIMAL_PLACES2.format(value) + " " + Msg.getString("solarIrradiance.unit"); //$NON-NLS-1$
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
	    				icon = SNOWFLAKE;
	    		}
	    	}
	    	else if (temperatureCache >= 26)
	    		icon = BALMY;
	    	else { //if (temperatureCache >= 0) {
	    		if (windSpeedCache > 20D) {
	    			icon = WIND_FLAG ;//SUN_STORM;
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
//	    		setImage(icon);
	    		weatherLabel.setIcon(ImageLoader.getNewIcon(icon));
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

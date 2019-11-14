/**
 * Mars Simulation Project
 * TabPanelWeather.java
 * @version 3.1.0 2017-02-14
 * @author Manny Kung
 */

package org.mars_sim.msp.ui.swing.unit_window.structure;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.mars.OrbitInfo;
import org.mars_sim.msp.core.mars.SurfaceFeatures;
import org.mars_sim.msp.core.mars.Weather;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.time.MasterClock;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
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
	
	/** default logger. */
	private static Logger logger = Logger.getLogger(TabPanelWeather.class.getName());

	private static final String DUSTY_SKY = Msg.getString("img.dust128"); //$NON-NLS-1$
	private static final String SUNNY = Msg.getString("img.sunny128"); //$NON-NLS-1$
	private static final String BALMY = Msg.getString("img.hot128"); //$NON-NLS-1$
//	private static final String LIGHTNING = Msg.getString("img.lightning128"); //$NON-NLS-1$

	private static final String SNOW_BLOWING = Msg.getString("img.snow_blowing"); //$NON-NLS-1$
	private static final String SUN_STORM = Msg.getString("img.sun_storm"); //$NON-NLS-1$
	private static final String SNOWFLAKE = Msg.getString("img.thermometer_snowflake"); //$NON-NLS-1$
//	private static final String WIND_FLAG = Msg.getString("img.wind_flag_storm"); //$NON-NLS-1$
	private static final String FRIGID = Msg.getString("img.frigid"); //$NON-NLS-1$
	private static final String HAZE = Msg.getString("img.haze"); //$NON-NLS-1$

	private static final double RADIANS_TO_DEGREES = 180D/Math.PI;
	// TODO: LOCAL_DUST_STORM, GLOBAL_DUST_STORM, DUSTY_SKY, CLEAR_SKY, WARM, COLD, DRY

	 /** default logger.   */
	//private static Logger logger = Logger.getLogger(LocationTabPanel.class.getName());

	/** Is UI constructed. */
	private boolean uiDone = false;
	
	/** The Settlement instance. */
	private Settlement settlement;
	
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

	private WebPanel locationCoordsPanel;
	private WebPanel locationLabelPanel;

	private WebLabel latitudeLabel;
	private WebLabel longitudeLabel;
	private WebLabel weatherLabel;

	private Coordinates locationCache;

//	private StormTrackingWindow stormWin;

	private static Weather weather;
	private static SurfaceFeatures surfaceFeatures;
	private static MasterClock masterClock;
	private static OrbitInfo orbitInfo;

	private static DecimalFormat fmt = new DecimalFormat("##0");
	//private static DecimalFormat fmt1 = new DecimalFormat("#0.0");
	private static DecimalFormat fmt2 = new DecimalFormat("#0.00");
	
    /**
     * Constructor.
     * @param unit the unit to display.
     * @param desktop the main desktop.
     */
    public TabPanelWeather(Unit unit, MainDesktopPane desktop) {
        // Use the TabPanel constructo
        super(Msg.getString("TabPanelWeather.title"), //$NON-NLS-1$
    			null,
    			Msg.getString("TabPanelWeather.tooltip"), //$NON-NLS-1$
    			unit, desktop);

		settlement = (Settlement) unit;

	}
	
	public boolean isUIDone() {
		return uiDone;
	}
	
	public void initializeUI() {
		uiDone = true;
		
		if (masterClock == null)
			masterClock = Simulation.instance().getMasterClock();

        weather = Simulation.instance().getMars().getWeather();
        surfaceFeatures = Simulation.instance().getMars().getSurfaceFeatures();
        orbitInfo = Simulation.instance().getMars().getOrbitInfo();

        // Initialize location cache
        locationCache = new Coordinates(unit.getCoordinates());

        // initialize containerCache
        //containerCache = unit.getContainerUnit();

		// Create label panel.
		WebPanel titlePanel = new WebPanel(new FlowLayout(FlowLayout.CENTER));
		WebLabel titleLabel = new WebLabel(Msg.getString("TabPanelWeather.title"), WebLabel.CENTER); //$NON-NLS-1$);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 16));
        //titleLabel.setForeground(new Color(102, 51, 0)); // dark brown
        titlePanel.add(titleLabel);
        topContentPanel.add(titlePanel);//, BorderLayout.NORTH);

        locationCoordsPanel = new WebPanel();
//        locationCoordsPanel.setBorder(new EmptyBorder(1, 1, 1, 1) );
        locationCoordsPanel.setLayout(new BorderLayout(0, 0));

        // Prepare latitude label
        latitudeLabel = new WebLabel(getLatitudeString());
        latitudeLabel.setOpaque(false);
        latitudeLabel.setFont(new Font("Serif", Font.PLAIN, 15));
        latitudeLabel.setHorizontalAlignment(SwingConstants.LEFT);
        locationCoordsPanel.add(latitudeLabel, BorderLayout.NORTH);

        // Prepare longitude label
        longitudeLabel = new WebLabel(getLongitudeString());
        longitudeLabel.setOpaque(false);
        longitudeLabel.setFont(new Font("Serif", Font.PLAIN, 15));
        longitudeLabel.setHorizontalAlignment(SwingConstants.LEFT);
        locationCoordsPanel.add(longitudeLabel, BorderLayout.CENTER);

        locationLabelPanel = new WebPanel();
//        locationLabelPanel.setBorder(new EmptyBorder(1, 1, 1, 1) );
        locationLabelPanel.setLayout(new BorderLayout(0, 0));
        WebLabel latLabel = new WebLabel("Lat : ");//, JLabel.RIGHT);
        latLabel.setFont(new Font("Serif", Font.PLAIN, 15));
        latLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        WebLabel longLabel = new WebLabel("Lon : ");//, JLabel.RIGHT);
        longLabel.setFont(new Font("Serif", Font.PLAIN, 15));
        longLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        locationLabelPanel.add(latLabel, BorderLayout.NORTH);
        locationLabelPanel.add(longLabel, BorderLayout.CENTER);

        // Create location panel
        WebPanel locationPanel = new WebPanel(new GridLayout(1, 2)); //new BorderLayout(0,0));
        //locationPanel.setBorder(new MarsPanelBorder());
        locationPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        locationPanel.add(locationLabelPanel);
        locationPanel.add(locationCoordsPanel);

        WebPanel leftPanel = new WebPanel(new BorderLayout(0, 0));
        leftPanel.setMaximumSize(new Dimension(180, 350));
        leftPanel.setPreferredSize(new Dimension(180, 350));
		//mainPanel.setBorder(new MarsPanelBorder());
        leftPanel.add(locationPanel, BorderLayout.NORTH);
        
        
//        centerContentPanel.setBorder(new MarsPanelBorder());
        centerContentPanel.add(leftPanel, BorderLayout.WEST);

      	// Create weatherPanel and imgPanel.
        WebPanel weatherPanel = new WebPanel(new BorderLayout(0, 0));//new GridLayout(2, 1));//new FlowLayout(FlowLayout.CENTER));
        locationPanel.setBorder(new EmptyBorder(25, 15, 15, 15));
        leftPanel.add(weatherPanel, BorderLayout.CENTER);

//       	// Create the button panel.
//		WebPanel buttonPane = new WebPanel(new FlowLayout(FlowLayout.RIGHT));
//		weatherPanel.add(buttonPane);//, BorderLayout.NORTH);
//
//		// Create the Storm Tracking button.
//		JButton stormButton = new JButton("Track Dust Storm");
//		stormButton.setToolTipText("Click to Open Storm Tracking Window");
//		stormButton.addActionListener(
//			new ActionListener() {
//				public void actionPerformed(ActionEvent e) {
//					// Open storm tracking window.
//					openStormTracking();
//				}
//			});
//		buttonPane.add(stormButton);

    	WebPanel imgPanel = new WebPanel(new FlowLayout());
        weatherLabel = new WebLabel();
    	imgPanel.add(weatherLabel, WebLabel.CENTER);
    	weatherPanel.add(imgPanel, BorderLayout.NORTH);
    	// TODO: calculate the average, high and low temperature during the day to determine
    	// if it is hot, sunny, dusty, stormy...
    	// Sets up if else clause to choose the proper weather image
    	setImage(SUNNY);
    	
    	// Prepare temperature panel
        WebPanel temperaturePanel = new WebPanel(new FlowLayout());
        weatherPanel.add(temperaturePanel, BorderLayout.CENTER);

        // Prepare temperature label
        temperatureValueLabel = new WebLabel(getTemperatureString(getTemperature()), WebLabel.CENTER);
        temperatureValueLabel.setOpaque(false);
        temperatureValueLabel.setFont(new Font("Serif", Font.BOLD, 28));
        temperatureValueLabel.setHorizontalAlignment(SwingConstants.CENTER);
        temperaturePanel.add(temperatureValueLabel);//, BorderLayout.NORTH);

        WebPanel rightPanel = new WebPanel(new BorderLayout(10, 10));//new FlowLayout());//new GridLayout(3, 1));//new BorderLayout(0, 0));
        centerContentPanel.add(rightPanel, BorderLayout.CENTER);

        // Create spring layout dataPanel
        WebPanel springPanel = new WebPanel(new SpringLayout());//GridLayout(10, 2));
        rightPanel.add(springPanel, BorderLayout.NORTH);

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
        wrapper1.add(pressureTF);//, BorderLayout.CENTER);
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
        //airDensityValueLabel.setHorizontalAlignment(SwingConstants.CENTER);
        wrapper2.add(airDensityTF);//, BorderLayout.CENTER);
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
        //solarIrradianceCache = getSolarIrradiance();
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
        //opticalDepthTF.setPreferredSize(new Dimension(60, 24));
        opticalDepthTF.setOpaque(false);
        opticalDepthTF.setFont(new Font("Serif", Font.PLAIN, 12));
        wrapper6.add(opticalDepthTF);
        springPanel.add(wrapper6);

        WebLabel zenithAngleLabel = new WebLabel(Msg.getString("TabPanelWeather.zenithAngle.label"), WebLabel.RIGHT);
        zenithAngleLabel.setOpaque(false);
        zenithAngleLabel.setFont(new Font("Serif", Font.PLAIN, 12));
        //zenithAngleLabel.setVerticalAlignment(SwingConstants.TOP);
        springPanel.add(zenithAngleLabel);

		WebPanel wrapper7 = new WebPanel(new FlowLayout(0, 0, FlowLayout.LEFT));
        zenithAngleTF = new WebTextField();
        zenithAngleTF.setEditable(false);
        zenithAngleTF.setColumns(8);
        zenithAngleTF.setFont(new Font("Serif", Font.PLAIN, 12));
        //zenithAngleValueLabel.setHorizontalAlignment(SwingConstants.CENTER);
        wrapper7.add(zenithAngleTF);
        springPanel.add(wrapper7);

        WebLabel solarDeclinationLabel = new WebLabel(Msg.getString("TabPanelWeather.solarDeclination.label"), WebLabel.RIGHT);
        solarDeclinationLabel.setOpaque(false);
        solarDeclinationLabel.setFont(new Font("Serif", Font.PLAIN, 12));
        //solarDeclinationLabel.setHorizontalAlignment(SwingConstants.CENTER);
        springPanel.add(solarDeclinationLabel);

		WebPanel wrapper8 = new WebPanel(new FlowLayout(0, 0, FlowLayout.LEFT));
		solarDeclinationTF = new WebTextField();
        solarDeclinationTF.setEditable(false);
        solarDeclinationTF.setColumns(8);
        solarDeclinationTF.setFont(new Font("Serif", Font.PLAIN, 12));
        //solarDeclinationValueLabel.setHorizontalAlignment(SwingConstants.CENTER);
        wrapper8.add(solarDeclinationTF);
        springPanel.add(wrapper8);

		//Lay out the spring panel.
		SpringUtilities.makeCompactGrid(springPanel,
		                                8, 2, //rows, cols
		                                0, 30,        //initX, initY
		                                5, 5);       //xPad, yPad

        // TODO: have a meteorologist or Areologist visit the weather station daily to fine tune the equipment
//        String personName = "ABC";
//        // Prepare temperature label
//        monitorLabel = new JLabel("Station last maintained and monitored by " + personName, JLabel.CENTER);
//        monitorLabel.setOpaque(false);
//        monitorLabel.setFont(new Font("Serif", Font.ITALIC, 11));
//        monitorLabel.setHorizontalAlignment(SwingConstants.CENTER);
//        dataP.add(monitorLabel);//, BorderLayout.NORTH);
    }


//    public void setViewer(StormTrackingWindow w) {
//    	this.stormWin = w;
//    }

//	/**
//	 * Open storm tracking window
//	 */
//	private void openStormTracking() {
//
//		MainWindow mw = desktop.getMainWindow();
//		if (mw != null )  {
//			if (stormWin == null)
//				stormWin = new StormTrackingWindow(desktop, this);
//		}
//
//		MainScene ms = desktop.getMainScene();
//		if (ms != null )  {
//
//			if (stormWin == null) {
//				stormWin = new StormTrackingWindow(desktop, this);
//			}
//		}
//	}

	/**
	 * Sets weather image.
	 */
	public void setImage(String image) {
        URL resource = ImageLoader.class.getResource(image);
		if (resource == null) {
			logger.severe("'" + image + "' cannot be found");
		}
		
        Toolkit kit = Toolkit.getDefaultToolkit();
        Image img = kit.createImage(resource);
        ImageIcon weatherImageIcon = new ImageIcon(img);
    	weatherLabel.setIcon(weatherImageIcon);
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
    	return fmt2.format(value) + " " + Msg.getString("pressure.unit.kPa"); //$NON-NLS-1$
    }

    public double getAirPressure() {
    	return Math.round(weather.getAirPressure(locationCache) *100.0) / 100.0;
    }

    public String getWindSpeedString(double value) {
    	return fmt2.format(value) + " " + Msg.getString("windspeed.unit.meterpersec"); //$NON-NLS-1$
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
     	return fmt2.format(value);
    }

    public double getZenithAngle() {
 		return orbitInfo.getSolarZenithAngle(locationCache);
     }

    public String getZenithAngleString(double value) {
     	return fmt2.format(value * RADIANS_TO_DEGREES) + " " + Msg.getString("direction.degreeSign"); //$NON-NLS-1$
    }

    public double getSolarDeclination() {
 		return orbitInfo.getSolarDeclinationAngleDegree();
     }

    public String getSolarDeclinationString(double value) {
     	return fmt2.format(value) + " " + Msg.getString("direction.degreeSign"); //$NON-NLS-1$
    }

    public double getAirDensity() {
		return weather.getAirDensity(locationCache);
    }

    public String getAirDensityString(double value) {
     	return fmt2.format(value) + " " + Msg.getString("airDensity.unit.gperm3"); //$NON-NLS-1$
    }

    public double getSolarIrradiance() {
  		return surfaceFeatures.getSolarIrradiance(locationCache);
      }

     public String getSolarIrradianceString(double value) {
      	return fmt2.format(value) + " " + Msg.getString("solarIrradiance.unit"); //$NON-NLS-1$
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
    public void update() {
		if (!uiDone)
			initializeUI();
		
    	Coordinates location = unit.getCoordinates();
		//System.out.println("solar declination angle : " + 57.2975 * orbitInfo.getSolarDeclinationAngle());
		//System.out.println("Duration of Mars daylight in millisols : " + orbitInfo.getDaylightinMillisols(location));
        if (!masterClock.isPaused()) {

	        // If unit's location has changed, update location display.
	    	// TODO: if a person goes outside the settlement for servicing an equipment
	    	// does the coordinate (down to how many decimal) change?
	        if (!locationCache.equals(location)) {
	            locationCache.setCoords(location);
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
	    		if (windSpeedCache > 10D) {
	    			icon = SUN_STORM;
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

	    	if (!icon.equals(iconCache)) {
	    		iconCache = icon;
	    		setImage(icon);
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
    public void destroy() {
    	airDensityTF = null;
    	pressureTF = null;
    	solarIrradianceTF = null;
    	windSpeedTF = null;
    	windDirTF = null;
    	opticalDepthTF = null;
    	zenithAngleTF = null;
    	solarDeclinationTF = null;

    	temperatureValueLabel = null;
    	
    	locationCoordsPanel = null;
    	locationLabelPanel = null;

    	latitudeLabel = null;
    	longitudeLabel = null;
    	weatherLabel = null;

    	locationCache = null;
    	weather = null;
    	surfaceFeatures = null;
    	masterClock = null;
    	orbitInfo = null;

//    	stormWin = null;

    	fmt = null;
    	//fmt1 = null;
    	fmt2 = null;

    }
}

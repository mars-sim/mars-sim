/**
 * Mars Simulation Project
 * TabPanelWeather.java
 * @version 3.08 2015-06-15
 * @author Manny Kung
 */

package org.mars_sim.msp.ui.swing.unit_window.structure;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.text.DecimalFormat;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.mars.OrbitInfo;
import org.mars_sim.msp.core.mars.SurfaceFeatures;
import org.mars_sim.msp.core.mars.Weather;
import org.mars_sim.msp.core.time.MasterClock;
import org.mars_sim.msp.ui.javafx.MainScene;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MainWindow;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.tool.MarsViewer;
import org.mars_sim.msp.ui.swing.tool.mission.create.CreateMissionWizard;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;

/**
 * The TabPanelWeather is a tab panel for location information.
 */
public class TabPanelWeather
extends TabPanel {

	/** default serial id. */
	private static final long serialVersionUID = 12L;

	private static final String DUSTY_SKY = Msg.getString("img.dust128"); //$NON-NLS-1$
	private static final String SUNNY = Msg.getString("img.sunny128"); //$NON-NLS-1$
	private static final String BALMY = Msg.getString("img.hot128"); //$NON-NLS-1$
	private static final String LIGHTNING = Msg.getString("img.lightning128"); //$NON-NLS-1$

	private static final String SNOW_BLOWING = Msg.getString("img.snow_blowing"); //$NON-NLS-1$
	private static final String SUN_STORM = Msg.getString("img.sun_storm"); //$NON-NLS-1$
	private static final String SNOWFLAKE = Msg.getString("img.thermometer_snowflake"); //$NON-NLS-1$
	private static final String WIND_FLAG = Msg.getString("img.wind_flag_storm"); //$NON-NLS-1$
	private static final String FRIGID = Msg.getString("img.frigid"); //$NON-NLS-1$
	private static final String HAZE = Msg.getString("img.haze"); //$NON-NLS-1$

	// TODO: LOCAL_DUST_STORM, GLOBAL_DUST_STORM, DUSTY_SKY, CLEAR_SKY, WARM, COLD, DRY


	 /** default logger.   */
	//private static Logger logger = Logger.getLogger(LocationTabPanel.class.getName());

	// 2014-11-11 Added new panels and labels
	private JPanel bottomPanel;
	private JTextField airDensityTF, pressureTF, solarIrradianceTF, windSpeedTF, windDirTF, opticalDepthTF, zenithAngleTF, solarDeclinationTF;
	//private JLabel airDensityLabel, airPressureLabel, solarIrradianceLabel, windSpeedLabel, windDirLabel, opticalDepthLabel, zenithAngleLabel, solarDeclinationLabel;

	private JLabel temperatureValueLabel;
	//private JLabel monitorLabel;

	private double airPressureCache;
	private int temperatureCache;
	private double windSpeedCache;
	private int windDirectionCache;
	private double airDensityCache;
	private double opticalDepthCache;
	private double zenithAngleCache;
	private double solarDeclinationCache;
	private double solarIrradianceCache;

	private String iconCache;

	//private Unit containerCache;

	private JPanel locationCoordsPanel;
	private JPanel locationLabelPanel;

	private JLabel latitudeLabel;
	private JLabel longitudeLabel;
	private JLabel weatherLabel;

	private Coordinates locationCache;
	private Weather weather;
	private SurfaceFeatures surfaceFeatures;
	private MasterClock masterClock;
	private OrbitInfo orbitInfo;

	private StormTrackingWindow stormWin;

	DecimalFormat fmt = new DecimalFormat("##0");
	DecimalFormat fmt1 = new DecimalFormat("#0.0");
	DecimalFormat fmt2 = new DecimalFormat("#0.00");
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
		JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel titleLabel = new JLabel(Msg.getString("TabPanelWeather.title"), JLabel.CENTER); //$NON-NLS-1$);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 16));
        //titleLabel.setForeground(new Color(102, 51, 0)); // dark brown
        titlePanel.add(titleLabel);
        topContentPanel.add(titlePanel);//, BorderLayout.NORTH);

        locationCoordsPanel = new JPanel();
        locationCoordsPanel.setBorder(new EmptyBorder(1, 1, 1, 1) );
        locationCoordsPanel.setLayout(new BorderLayout(0, 0));

        // Prepare latitude label
        latitudeLabel = new JLabel(getLatitudeString());
        latitudeLabel.setOpaque(false);
        latitudeLabel.setFont(new Font("Serif", Font.PLAIN, 15));
        latitudeLabel.setHorizontalAlignment(SwingConstants.LEFT);
        locationCoordsPanel.add(latitudeLabel, BorderLayout.NORTH);

        // Prepare longitude label
        longitudeLabel = new JLabel(getLongitudeString());
        longitudeLabel.setOpaque(false);
        longitudeLabel.setFont(new Font("Serif", Font.PLAIN, 15));
        longitudeLabel.setHorizontalAlignment(SwingConstants.LEFT);
        locationCoordsPanel.add(longitudeLabel, BorderLayout.CENTER);

        locationLabelPanel = new JPanel();
        locationLabelPanel.setBorder(new EmptyBorder(1, 1, 1, 1) );
        locationLabelPanel.setLayout(new BorderLayout(0, 0));
        JLabel latLabel = new JLabel("Latitude : ");//, JLabel.RIGHT);
        latLabel.setFont(new Font("Serif", Font.PLAIN, 15));
        latLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        JLabel longLabel = new JLabel("Longitude : ");//, JLabel.RIGHT);
        longLabel.setFont(new Font("Serif", Font.PLAIN, 15));
        longLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        locationLabelPanel.add(latLabel, BorderLayout.NORTH);
        locationLabelPanel.add(longLabel, BorderLayout.CENTER);

        // Create location panel
        JPanel locationPanel = new JPanel(new GridLayout(1, 2)); //new BorderLayout(0,0));
        locationPanel.setBorder(new MarsPanelBorder());
        locationPanel.setBorder(new EmptyBorder(1, 1, 1, 1));
        locationPanel.add(locationLabelPanel);
        locationPanel.add(locationCoordsPanel);


        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
		mainPanel.setBorder(new MarsPanelBorder());
        mainPanel.add(locationPanel, BorderLayout.NORTH);

        centerContentPanel.add(mainPanel, BorderLayout.NORTH);

      	// Create weatherPanel and imgPanel.
        JPanel weatherPanel = new JPanel(new BorderLayout(0, 0));//new GridLayout(2, 1));//new FlowLayout(FlowLayout.CENTER));

       	// Create the button panel.
		JPanel buttonPane = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		weatherPanel.add(buttonPane);//, BorderLayout.NORTH);

		// Create the Storm Tracking button.
		JButton stormButton = new JButton("Track Dust Storm");
		stormButton.setToolTipText("Click to Open Storm Tracking Window");
		stormButton.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					// Open storm tracking window.
					openStormTracking();
				}
			});
		buttonPane.add(stormButton);

    	JPanel imgPanel = new JPanel(new FlowLayout());
        weatherLabel = new JLabel();
    	imgPanel.add(weatherLabel, JLabel.CENTER);
    	weatherPanel.add(imgPanel, BorderLayout.CENTER);
    	// TODO: calculate the average, high and low temperature during the day to determine
    	// if it is hot, sunny, dusty, stormy...
    	// Sets up if else clause to choose the proper weather image
    	setImage(SUNNY);
        mainPanel.add(weatherPanel, BorderLayout.CENTER);

        bottomPanel = new JPanel(new BorderLayout(0, 0));//new FlowLayout());//new GridLayout(3, 1));//new BorderLayout(0, 0));
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        JPanel tPanel = new JPanel(new FlowLayout());
        bottomPanel.add(tPanel, BorderLayout.NORTH);

        // Prepare temperature label
        temperatureValueLabel = new JLabel(getTemperatureString(getTemperature()), JLabel.CENTER);
        temperatureValueLabel.setOpaque(false);
        temperatureValueLabel.setFont(new Font("Serif", Font.BOLD, 28));
        temperatureValueLabel.setHorizontalAlignment(SwingConstants.CENTER);
        tPanel.add(temperatureValueLabel);//, BorderLayout.NORTH);

        JPanel dataP = new JPanel(new GridLayout(10, 2));
        bottomPanel.add(dataP, BorderLayout.CENTER);

        // Prepare air pressure label
        JLabel airPressureLabel = new JLabel(Msg.getString("TabPanelWeather.airPressure.label"), JLabel.RIGHT);
        airPressureLabel.setOpaque(false);
        airPressureLabel.setFont(new Font("Serif", Font.PLAIN, 12));
        dataP.add(airPressureLabel);

		JPanel wrapper1 = new JPanel(new FlowLayout(0, 0, FlowLayout.LEADING));
        pressureTF = new JTextField();
        pressureTF.setEditable(false);
        pressureTF.setColumns(6);
        pressureTF.setFont(new Font("Serif", Font.PLAIN, 12));
        wrapper1.add(pressureTF);//, BorderLayout.CENTER);
        dataP.add(wrapper1);

        // Prepare air density label
        JLabel airDensityLabel = new JLabel(Msg.getString("TabPanelWeather.airDensity.label"), JLabel.RIGHT);
        airDensityLabel.setOpaque(false);
        airDensityLabel.setFont(new Font("Serif", Font.PLAIN, 12));
        dataP.add(airDensityLabel);

		JPanel wrapper2 = new JPanel(new FlowLayout(0, 0, FlowLayout.LEADING));
        airDensityTF = new JTextField();
        airDensityTF.setEditable(false);
        airDensityTF.setColumns(6);
        airDensityTF.setOpaque(false);
        airDensityTF.setFont(new Font("Serif", Font.PLAIN, 12));
        //airDensityValueLabel.setHorizontalAlignment(SwingConstants.CENTER);
        wrapper2.add(airDensityTF);//, BorderLayout.CENTER);
        dataP.add(wrapper2);

        JLabel windSpeedLabel = new JLabel(Msg.getString("TabPanelWeather.windspeed.label"), JLabel.RIGHT);
        windSpeedLabel.setOpaque(false);
        windSpeedLabel.setFont(new Font("Serif", Font.PLAIN, 12));
        dataP.add(windSpeedLabel);

		JPanel wrapper3 = new JPanel(new FlowLayout(0, 0, FlowLayout.LEADING));
        windSpeedTF = new JTextField();
        windSpeedTF.setEditable(false);
        windSpeedTF.setColumns(5);
        windSpeedTF.setFont(new Font("Serif", Font.PLAIN, 12));
        wrapper3.add(windSpeedTF);
        dataP.add(wrapper3);

        JLabel windDirLabel = new JLabel(Msg.getString("TabPanelWeather.windDirection.label"), JLabel.RIGHT);
        windDirLabel.setOpaque(false);
        windDirLabel.setFont(new Font("Serif", Font.PLAIN, 12));
        dataP.add(windDirLabel);

		JPanel wrapper4 = new JPanel(new FlowLayout(0, 0, FlowLayout.LEADING));
        windDirTF = new JTextField();
        windDirTF.setEditable(false);
        windDirTF.setColumns(4);
        windDirTF.setFont(new Font("Serif", Font.PLAIN, 12));
        wrapper4.add(windDirTF);
        dataP.add(wrapper4);

        JLabel solarIrradianceLabel = new JLabel(Msg.getString("TabPanelWeather.solarIrradiance.label"), JLabel.RIGHT);
        solarIrradianceLabel.setOpaque(false);
        solarIrradianceLabel.setFont(new Font("Serif", Font.PLAIN, 12));
        dataP.add(solarIrradianceLabel);

		JPanel wrapper5 = new JPanel(new FlowLayout(0, 0, FlowLayout.LEADING));
        solarIrradianceCache = getSolarIrradiance();
        solarIrradianceTF = new JTextField(getSolarIrradianceString(solarIrradianceCache));
        solarIrradianceTF.setEditable(false);
        solarIrradianceTF.setColumns(7);
        solarIrradianceTF.setOpaque(false);
        solarIrradianceTF.setFont(new Font("Serif", Font.PLAIN, 12));
        wrapper5.add(solarIrradianceTF);
        dataP.add(wrapper5);

        JLabel opticalDepthLabel = new JLabel(Msg.getString("TabPanelWeather.opticalDepth.label"), JLabel.RIGHT);
        opticalDepthLabel.setOpaque(false);
        opticalDepthLabel.setFont(new Font("Serif", Font.PLAIN, 12));
        dataP.add(opticalDepthLabel);

		JPanel wrapper6 = new JPanel(new FlowLayout(0, 0, FlowLayout.LEADING));
        opticalDepthTF = new JTextField();
        opticalDepthTF.setEditable(false);
        opticalDepthTF.setColumns(4);
        opticalDepthTF.setOpaque(false);
        opticalDepthTF.setFont(new Font("Serif", Font.PLAIN, 12));
        wrapper6.add(opticalDepthTF);
        dataP.add(wrapper6);

        JLabel zenithAngleLabel = new JLabel(Msg.getString("TabPanelWeather.zenithAngle.label"), JLabel.RIGHT);
        zenithAngleLabel.setOpaque(false);
        zenithAngleLabel.setFont(new Font("Serif", Font.PLAIN, 12));
        //zenithAngleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        dataP.add(zenithAngleLabel);

		JPanel wrapper7 = new JPanel(new FlowLayout(0, 0, FlowLayout.LEADING));
        zenithAngleTF = new JTextField();
        zenithAngleTF.setEditable(false);
        zenithAngleTF.setColumns(6);
        zenithAngleTF.setFont(new Font("Serif", Font.PLAIN, 12));
        //zenithAngleValueLabel.setHorizontalAlignment(SwingConstants.CENTER);
        wrapper7.add(zenithAngleTF);
        dataP.add(wrapper7);

        JLabel solarDeclinationLabel = new JLabel(Msg.getString("TabPanelWeather.solarDeclination.label"), JLabel.RIGHT);
        solarDeclinationLabel.setOpaque(false);
        solarDeclinationLabel.setFont(new Font("Serif", Font.PLAIN, 12));
        //solarDeclinationLabel.setHorizontalAlignment(SwingConstants.CENTER);
        dataP.add(solarDeclinationLabel);

		JPanel wrapper8 = new JPanel(new FlowLayout(0, 0, FlowLayout.LEADING));
		solarDeclinationTF = new JTextField();
        solarDeclinationTF.setEditable(false);
        solarDeclinationTF.setColumns(6);
        solarDeclinationTF.setFont(new Font("Serif", Font.PLAIN, 12));
        //solarDeclinationValueLabel.setHorizontalAlignment(SwingConstants.CENTER);
        wrapper8.add(solarDeclinationTF);
        dataP.add(wrapper8);

/*
        // TODO: have a meteorologist or Areologist visit the weather station daily to fine tune the equipment
        String personName = "ABC";
        // Prepare temperature label
        monitorLabel = new JLabel("Station last maintained and monitored by " + personName, JLabel.CENTER);
        monitorLabel.setOpaque(false);
        monitorLabel.setFont(new Font("Serif", Font.ITALIC, 11));
        monitorLabel.setHorizontalAlignment(SwingConstants.CENTER);
        dataP.add(monitorLabel);//, BorderLayout.NORTH);
*/

    }


    public void setViewer(StormTrackingWindow w) {
    	this.stormWin = w;
    }

	/**
	 * Open storm tracking window
	 */
    // 2015-05-21 Added openStormTracking()
	private void openStormTracking() {

		MainWindow mw = desktop.getMainWindow();
		if (mw != null )  {
			// Pause simulation.
			//mw.pauseSimulation();
			// Create Storm Tracking Window.
			if (stormWin == null)
				stormWin = new StormTrackingWindow(desktop, this);
			// Unpause simulation.
			//mw.unpauseSimulation();
		}

		MainScene ms = desktop.getMainScene();
		if (ms != null )  {
			// Pause simulation.
			//ms.pauseSimulation();
			// Create Storm Tracking Window..
			if (stormWin == null) {
				stormWin = new StormTrackingWindow(desktop, this);
			}
			// Unpause simulation.
			//ms.unpauseSimulation();
		}

	}

	/**
	 * Sets weather image.
	 */
	public void setImage(String image) {
        URL resource = ImageLoader.class.getResource(image);
        Toolkit kit = Toolkit.getDefaultToolkit();
        Image img = kit.createImage(resource);
        ImageIcon weatherImageIcon = new ImageIcon(img);
    	weatherLabel.setIcon(weatherImageIcon);
	}

    // 2014-11-11 Modified temperature and pressure panel
    public String getTemperatureString(double value) {
    	// 2015-01-16 Used Msg.getString for the degree sign
    	// 2014-11-20 Changed from " °C" to " �C" for English Locale
    	return fmt.format(value) + " " + Msg.getString("temperature.sign.degreeCelsius"); //$NON-NLS-1$
    }

    public int getTemperature() {
		return (int) weather.getTemperature(locationCache);
    }

    // 2014-11-07 Added temperature and pressure panel
    public String getAirPressureString(double value) {
    	return fmt2.format(value) + " " + Msg.getString("pressure.unit.kPa"); //$NON-NLS-1$
    }

    public double getAirPressure() {
    	return Math.round(weather.getAirPressure(locationCache) *100.0) / 100.0;
    }

    public String getWindSpeedString(double value) {
    	return fmt.format(value) + " " + Msg.getString("windspeed.unit.meterpersec"); //$NON-NLS-1$
    }

    public int getWindSpeed() {
		return (int) weather.getWindSpeed(locationCache);
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
     	return fmt2.format(value/ Math.PI*180D) + " " + Msg.getString("direction.degreeSign"); //$NON-NLS-1$
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
     	return fmt1.format(value) + " " + Msg.getString("airDensity.unit.gperm3"); //$NON-NLS-1$
    }

    public int getSolarIrradiance() {
  		return (int) surfaceFeatures.getSolarIrradiance(locationCache);
      }

     public String getSolarIrradianceString(double value) {
      	return fmt.format(value) + " " + Msg.getString("solarIrradiance.unit"); //$NON-NLS-1$
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
    // 2014-11-11 Overhauled update()
    public void update() {

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


			double p = getAirPressure();
	        if (airPressureCache != p) {
	        	airPressureCache = p;
	        	pressureTF.setText(" " + getAirPressureString(airPressureCache));
	        }

	        int t = getTemperature();
	        if (temperatureCache != t) {
	        	temperatureCache = t;
	        	temperatureValueLabel.setText(getTemperatureString(temperatureCache));
	        }


	        int wd = getWindDirection();
	        if (windDirectionCache != wd) {
	        	windDirectionCache = wd;
	        	windDirTF.setText(" " + getWindDirectionString(windDirectionCache));
	        }

	        int s = getWindSpeed();
	        if (windSpeedCache != s) {
	        	windSpeedCache = s;
	        	windSpeedTF.setText(" " + getWindSpeedString(windSpeedCache));
	        }


	        double ad = getAirDensity();
	        if (airDensityCache != ad) {
	        	airDensityCache = ad;
	        	airDensityTF.setText(" " + getAirDensityString(airDensityCache));
	        }

	        double od = getOpticalDepth();
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

}

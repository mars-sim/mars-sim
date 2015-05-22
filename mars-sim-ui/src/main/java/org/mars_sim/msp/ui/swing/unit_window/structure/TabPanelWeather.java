/**
 * Mars Simulation Project
 * TabPanelWeather.java
 * @version 3.08 2015-05-01
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
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.mars.SurfaceFeatures;
import org.mars_sim.msp.core.mars.Weather;
import org.mars_sim.msp.core.time.MasterClock;
import org.mars_sim.msp.ui.javafx.MainScene;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MainWindow;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.tool.mission.create.CreateMissionWizard;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;

/**
 * The TabPanelWeather is a tab panel for location information.
 */
public class TabPanelWeather
extends TabPanel {

	/** default serial id. */
	private static final long serialVersionUID = 12L;

	private static final String DUSTY_SKY = Msg.getString("img.dust"); //$NON-NLS-1$
	private static final String SUNNY = Msg.getString("img.sunny"); //$NON-NLS-1$
	private static final String BALMY = Msg.getString("img.hot"); //$NON-NLS-1$
	private static final String LIGHTNING = Msg.getString("img.lightning"); //$NON-NLS-1$
	// TODO: LOCAL_DUST_STORM, GLOBAL_DUST_STORM, DUSTY_SKY, CLEAR_SKY, WARM, COLD, FRIGID, DRY


	 /** default logger.   */
	//private static Logger logger = Logger.getLogger(LocationTabPanel.class.getName());

	// 2014-11-11 Added new panels and labels
	private JPanel bottomPanel;
	private JLabel windSpeedLabel, windSpeedValueLabel;
	private JLabel solarIrradianceLabel, solarIrradianceValueLabel;
	private JLabel windDirLabel, windDirValueLabel;
	private JLabel opticalDepthValueLabel, opticalDepthLabel;
	private JLabel temperatureValueLabel, pressureValueLabel, airDensityValueLabel;
	private JLabel monitorLabel;
	//private Color THEME_COLOR = Color.ORANGE;
	private double airPressureCache;
	private int temperatureCache;
	private double windSpeedCache;
	private int windDirectionCache;
	private double airDensityCache;
	private double opticalDepthCache;
	private double solarIrradianceCache;


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

        // Initialize location cache
        locationCache = new Coordinates(unit.getCoordinates());

        // initialize containerCache
        //containerCache = unit.getContainerUnit();


		// Create label panel.
		JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel titleLabel = new JLabel(Msg.getString("TabPanelWeather.title"), JLabel.CENTER); //$NON-NLS-1$);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 16));
        titleLabel.setForeground(new Color(102, 51, 0)); // dark brown
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
		JPanel buttonPane = new JPanel(new FlowLayout());
		weatherPanel.add(buttonPane, BorderLayout.NORTH);

		// Create the Storm Tracking button.
		JButton stormButton = new JButton("Track Dust Storm");
		stormButton.setToolTipText("Click to open dust storm tracking panel");
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

        JPanel dataP = new JPanel(new GridLayout(8, 2));
        bottomPanel.add(dataP, BorderLayout.CENTER);

        // Prepare air pressure label
        JLabel airPressureLabel = new JLabel("Pressure : ", JLabel.RIGHT);
        airPressureLabel.setOpaque(false);
        airPressureLabel.setFont(new Font("Serif", Font.PLAIN, 12));
        //windSpeedLabel.setHorizontalAlignment(SwingConstants.CENTER);
        dataP.add(airPressureLabel);

        pressureValueLabel = new JLabel(getAirPressureString(getAirPressure()), JLabel.LEFT);
        pressureValueLabel.setOpaque(false);
        pressureValueLabel.setFont(new Font("Serif", Font.ITALIC, 12));
        //pressureValueLabel.setHorizontalAlignment(SwingConstants.CENTER);
        dataP.add(pressureValueLabel);//, BorderLayout.CENTER);

        // Prepare air density label
        JLabel airDensityLabel = new JLabel(Msg.getString("TabPanelWeather.airDensity.label"), JLabel.RIGHT);
        airDensityLabel.setOpaque(false);
        airDensityLabel.setFont(new Font("Serif", Font.PLAIN, 12));
        //windSpeedLabel.setHorizontalAlignment(SwingConstants.CENTER);
        dataP.add(airDensityLabel);

        airDensityValueLabel = new JLabel(Msg.getString("airDensity.unit.gperm3"), JLabel.LEFT);
        airDensityValueLabel.setOpaque(false);
        airDensityValueLabel.setFont(new Font("Serif", Font.ITALIC, 12));
        //airDensityValueLabel.setHorizontalAlignment(SwingConstants.CENTER);
        dataP.add(airDensityValueLabel);//, BorderLayout.CENTER);

        windSpeedLabel = new JLabel(Msg.getString("TabPanelWeather.windspeed.label"), JLabel.RIGHT);
        windSpeedLabel.setOpaque(false);
        windSpeedLabel.setFont(new Font("Serif", Font.PLAIN, 12));
        //windSpeedLabel.setHorizontalAlignment(SwingConstants.CENTER);
        dataP.add(windSpeedLabel);

        windSpeedValueLabel = new JLabel(Msg.getString("windspeed.unit.meterpersec"), JLabel.LEFT);
        windSpeedValueLabel.setOpaque(false);
        windSpeedValueLabel.setFont(new Font("Serif", Font.PLAIN, 12));
        //windSpeedValueLabel.setHorizontalAlignment(SwingConstants.CENTER);
        dataP.add(windSpeedValueLabel);

        windDirLabel = new JLabel(Msg.getString("TabPanelWeather.windDirection.label"), JLabel.RIGHT);
        windDirLabel.setOpaque(false);
        windDirLabel.setFont(new Font("Serif", Font.PLAIN, 12));
       // windSpeedLabel.setHorizontalAlignment(SwingConstants.CENTER);
        dataP.add(windDirLabel);

        windDirValueLabel = new JLabel(Msg.getString("windDirection.unit.deg"), JLabel.LEFT);
        windDirValueLabel.setOpaque(false);
        windDirValueLabel.setFont(new Font("Serif", Font.PLAIN, 12));
        //windSpeedValueLabel.setHorizontalAlignment(SwingConstants.CENTER);
        dataP.add(windDirValueLabel);

        solarIrradianceLabel = new JLabel(Msg.getString("TabPanelWeather.solarIrradiance.label"), JLabel.RIGHT);
        solarIrradianceLabel.setOpaque(false);
        solarIrradianceLabel.setFont(new Font("Serif", Font.PLAIN, 12));
        //solarIrradianceLabel.setHorizontalAlignment(SwingConstants.CENTER);
        dataP.add(solarIrradianceLabel);

        solarIrradianceValueLabel = new JLabel(" " + (int) surfaceFeatures.getSolarIrradiance(unit.getCoordinates()), JLabel.LEFT);
        solarIrradianceValueLabel.setOpaque(false);
        solarIrradianceValueLabel.setFont(new Font("Serif", Font.PLAIN, 12));
        //solarIrradianceValueLabel.setHorizontalAlignment(SwingConstants.CENTER);
        dataP.add(solarIrradianceValueLabel);

        opticalDepthLabel = new JLabel(Msg.getString("TabPanelWeather.opticalDepth.label"), JLabel.RIGHT);
        opticalDepthLabel.setOpaque(false);
        opticalDepthLabel.setFont(new Font("Serif", Font.PLAIN, 12));
        //opticalDepthLabel.setHorizontalAlignment(SwingConstants.CENTER);
        dataP.add(opticalDepthLabel);

        opticalDepthValueLabel = new JLabel();
        opticalDepthValueLabel.setOpaque(false);
        opticalDepthValueLabel.setFont(new Font("Serif", Font.PLAIN, 12));
        //opticalDepthValueLabel.setHorizontalAlignment(SwingConstants.CENTER);
        dataP.add(opticalDepthValueLabel);

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


	/**
	 * Open storm tracking window
	 */
    // 2015-05-21 Added openStormTracking()
	private void openStormTracking() {

		MainWindow mw = desktop.getMainWindow();
		if (mw !=null )  {
			// Pause simulation.
			mw.pauseSimulation();
			// Create Storm Tracking Window.
			new StormTrackingWindow(desktop);
			// Unpause simulation.
			mw.unpauseSimulation();
		}

		MainScene ms = desktop.getMainScene();
		if (ms !=null )  {
			// Pause simulation.
			ms.pauseSimulation();
			// Create Storm Tracking Window..
			new StormTrackingWindow(desktop);
			// Unpause simulation.
			ms.unpauseSimulation();
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
		return (int) weather.getTemperature(unit.getCoordinates());
    }

    // 2014-11-07 Added temperature and pressure panel
    public String getAirPressureString(double value) {
    	return fmt2.format(value) + " " + Msg.getString("pressure.unit.kPa"); //$NON-NLS-1$
    }

    public double getAirPressure() {
    	return Math.round(weather.getAirPressure(unit.getCoordinates()) *100.0) / 100.0;
    }

    public String getWindSpeedString(double value) {
    	return fmt.format(value) + " " + Msg.getString("windspeed.unit.meterpersec"); //$NON-NLS-1$
    }

    public int getWindSpeed() {
		return (int) weather.getWindSpeed(unit.getCoordinates());
    }

    public int getWindDirection() {
		return weather.getWindDirection(unit.getCoordinates());
    }

    public String getWindDirectionString(double value) {
     	return fmt.format(value) + " " + Msg.getString("windDirection.unit.deg"); //$NON-NLS-1$
    }

    public double getOpticalDepth() {
 		return surfaceFeatures.getOpticalDepth(unit.getCoordinates());
     }

    public String getOpticalDepthString(double value) {
     	return fmt2.format(value);
    }

    public double getAirDensity() {
		return weather.getAirDensity(unit.getCoordinates());
    }

    public String getAirDensityString(double value) {
     	return fmt1.format(value) + " " + Msg.getString("airDensity.unit.gperm3"); //$NON-NLS-1$
    }

    public int getSolarIrradiance() {
  		return (int) surfaceFeatures.getPreviousSolarIrradiance(unit.getCoordinates());
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

        if (!masterClock.isPaused()) {

	        // If unit's location has changed, update location display.
	    	// TODO: if a person goes outside the settlement for servicing an equipment
	    	// does the coordinate (down to how many decimal) change?
	        if (!locationCache.equals(unit.getCoordinates())) {
	            locationCache.setCoords(unit.getCoordinates());
	            latitudeLabel.setText(getLatitudeString());
	            longitudeLabel.setText(getLongitudeString());
	        }


			double p = getAirPressure();
	        if (airPressureCache != p) {
	        	airPressureCache = p;
	        	pressureValueLabel.setText(" " + getAirPressureString(airPressureCache));
	        }

	        int t = getTemperature();
	        if (temperatureCache != t) {
	        	temperatureCache = t;
	        	temperatureValueLabel.setText(getTemperatureString(temperatureCache));
	        }

	        int wd = getWindDirection();
	        if (windDirectionCache != wd) {
	        	windDirectionCache = wd;
	        	windDirValueLabel.setText(" " + getWindDirectionString(windDirectionCache));
	        }

	        int s = getWindSpeed();
	        if (windSpeedCache != s) {
	        	windSpeedCache = s;
	        	windSpeedValueLabel.setText(" " + getWindSpeedString(windSpeedCache));
	        }


	        double ad = getAirDensity();
	        if (airDensityCache != ad) {
	        	airDensityCache = ad;
	        	airDensityValueLabel.setText(" " + getAirDensityString(airDensityCache));
	        }

	        double od = getOpticalDepth();
	        if (opticalDepthCache != od) {
	        	opticalDepthCache = od;
	        	opticalDepthValueLabel.setText(" " + getOpticalDepthString(opticalDepthCache));
	        }

	        double ir = getSolarIrradiance();
	        if (solarIrradianceCache != ir) {
	        	solarIrradianceCache = ir;
	        	solarIrradianceValueLabel.setText(" " + getSolarIrradianceString(solarIrradianceCache));
	        }

	    }
    }

}

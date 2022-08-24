/*
 * Mars Simulation Project
 * SettlementTransparentPanel.java
 * @date 2022-06-24
 * @author Manny Kung
 */
package org.mars_sim.msp.ui.swing.tool.settlement;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSlider;
import javax.swing.Painter;
import javax.swing.UIDefaults;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.GameManager;
import org.mars_sim.msp.core.GameManager.GameMode;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.UnitEvent;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.UnitListener;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.UnitManagerEvent;
import org.mars_sim.msp.core.UnitManagerListener;
import org.mars_sim.msp.core.UnitType;
import org.mars_sim.msp.core.environment.OrbitInfo;
import org.mars_sim.msp.core.environment.SunData;
import org.mars_sim.msp.core.environment.SurfaceFeatures;
import org.mars_sim.msp.core.environment.Weather;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.time.ClockListener;
import org.mars_sim.msp.core.time.ClockPulse;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.time.MasterClock;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MainWindow;

import com.alee.extended.WebComponent;
import com.alee.extended.svg.SvgIcon;
import com.alee.extended.svg.SvgStroke;
import com.alee.laf.button.WebButton;
import com.alee.laf.combobox.WebComboBox;
import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;
import com.alee.managers.icon.IconManager;
import com.alee.managers.icon.LazyIcon;
import com.alee.managers.style.StyleId;

import eu.hansolo.steelseries.gauges.DisplaySingle;
import eu.hansolo.steelseries.tools.LcdColor;


@SuppressWarnings({ "serial", "rawtypes" })
public class SettlementTransparentPanel extends WebComponent implements ClockListener {

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(SettlementTransparentPanel.class.getName());

	/** Rotation change (radians per rotation button press). */
	private static final double ROTATION_CHANGE = Math.PI / 20D;
	private static final double RADIANS_TO_DEGREES = 180D/Math.PI;

	/** Zoom change. */
	public static final double ZOOM_CHANGE = 0.25;

	public static final String SANDSTORM_SVG = MainWindow.SANDSTORM_SVG;
	public static final String DUST_DEVIL_SVG = MainWindow.DUST_DEVIL_SVG;
	public static final String SAND_SVG = MainWindow.SAND_SVG;
	public static final String HAZY_SVG = MainWindow.HAZY_SVG;

	public static final String COLD_WIND_SVG = MainWindow.COLD_WIND_SVG;
	public static final String FROST_WIND_SVG = MainWindow.FROST_WIND_SVG;

	public static final String SUN_SVG = MainWindow.SUN_SVG;
	public static final String DESERT_SUN_SVG = MainWindow.DESERT_SUN_SVG;
	public static final String CLOUDY_SVG = MainWindow.CLOUDY_SVG;
	public static final String SNOWFLAKE_SVG = MainWindow.SNOWFLAKE_SVG;
	public static final String ICE_SVG = MainWindow.ICE_SVG;

	private static final String TEMPERATURE 	= "   Temperature: ";
	private static final String WINDSPEED 		= "   Windspeed: ";
	private static final String ZENITH_ANGLE 	= "   Zenith Angle: ";
	private static final String OPTICAL_DEPTH 	= "   Optical Depth: ";

	
	private static final String PROJECTED_SUNRISE	= " Projected Sunrise: ";
	private static final String PROJECTED_SUNSET	= "  Projected Sunset: ";
	private static final String SUNRISE				= " Yestersol Sunrise: ";
	private static final String SUNSET				= "  Yestersol Sunset: ";
	private static final String PROJECTED_DAYLIGHT	= "Projected Daylight: ";	
	private static final String DAYLIGHT			= "Yestersol Daylight: ";
	private static final String ZENITH				= "       Zenith Time: ";
	private static final String MAX_LIGHT			= "      Max Sunlight: ";
	private static final String CURRENT_LIGHT		= "  Current Sunlight: ";
	private static final String WM					= " W/m\u00B2 ";
	private static final String MSOL				= " msol ";
	private static final String PENDING				= " ...  ";

	private static final String YESTERSOL_RESOURCE = "Yestersol's Resources (";

	private double temperatureCache;
	private double opticalDepthCache;
	private double windSpeedCache;
	private double zenithAngleCache;

	private String[] iconCache = new String[]{"", "", "", ""};
	private String tString;
	private String wsString;
	private String zaString;
	private String odString;

	private Map<Settlement, String> resourceCache = new HashMap<>();

	private GameMode mode;

	private JLabel emptyLabel;
	private DisplaySingle bannerBar;
	private JSlider zoomSlider;
	private JPanel controlCenterPane, eastPane, labelPane, buttonPane, controlPane;

	private ImageIcon sand = new LazyIcon("sand").getIcon();
	private ImageIcon sandstorm = new LazyIcon("sandstorm").getIcon();
	private ImageIcon dustDevil = new LazyIcon("dustDevil").getIcon();

	private ImageIcon coldWind = new LazyIcon("cold_wind").getIcon();
	private ImageIcon frostWind = new LazyIcon("frost_wind").getIcon();

	private ImageIcon snowflake = new LazyIcon("snowflake").getIcon();
	private ImageIcon ice = new LazyIcon("ice").getIcon();
	private ImageIcon hazy = new LazyIcon("hazy").getIcon();
	private ImageIcon cloudy = new LazyIcon("cloudy").getIcon();
	
	private ImageIcon sun = new LazyIcon("sun").getIcon();
	private ImageIcon desertSun = new LazyIcon("desert_sun").getIcon();
	
	private ImageIcon emptyIcon = new ImageIcon();
    
	/** label for projected sunrise time. */
	private WebLabel projectSunriseLabel;
	/** label for projected sunset time. */
	private WebLabel projectSunsetLabel;
	/** label for projected daylight. */
	private WebLabel projectDaylightLabel;
	/** label for sunrise time. */
	private WebLabel sunriseLabel;
	/** label for sunset time. */
	private WebLabel sunsetLabel;
	/** label for zenith time. */
	private WebLabel zenithLabel;
	/** label for highest solar irradiance. */
	private WebLabel maxSunLabel;
	/** label for the daylight period. */
	private WebLabel daylightLabel;
	/** label for the daylight period. */
	private WebLabel currentSunLabel;

	private WebButton renameBtn;
	private WebButton infoButton;
	private WebButton weatherButton00;
	private WebButton weatherButton01;
	private WebButton weatherButton10;

	private WebButton[] weatherButtons = new WebButton[3];

	private JPopupMenu labelsMenu;
	/** Settlement Combo box */
	private WebComboBox settlementListBox;
	/** Settlement Combo box model. */
	private SettlementComboBoxModel settlementCBModel;

	private JCustomCheckBoxMenuItem buildingLabelMenuItem, personLabelMenuItem, constructionLabelMenuItem, vehicleLabelMenuItem, robotLabelMenuItem ;

	private SettlementMapPanel mapPanel;
	private MainDesktopPane desktop;

	private Weather weather;
	private SurfaceFeatures surfaceFeatures;
	private OrbitInfo orbitInfo;
	private UnitManager unitManager;

	private static DecimalFormat fmt = new DecimalFormat("##0");
	private static DecimalFormat fmt2 = new DecimalFormat("#0.00");

	private Font sunFont = new Font(Font.MONOSPACED, Font.PLAIN, 14);

	private Simulation sim;
	
    public SettlementTransparentPanel(MainDesktopPane desktop, SettlementMapPanel mapPanel) {
        this.mapPanel = mapPanel;
        this.desktop = desktop;

        sim = desktop.getSimulation();
        this.unitManager = sim.getUnitManager();
        
		MasterClock masterClock = sim.getMasterClock();
		masterClock.addClockListener(this, 1000L);
		
        this.weather = sim.getWeather();
        this.surfaceFeatures = sim.getSurfaceFeatures();
        this.orbitInfo = sim.getOrbitInfo();

		if (GameManager.getGameMode() == GameMode.COMMAND) {
			mode = GameMode.COMMAND;
		}
		else
			mode = GameMode.SANDBOX;

		setDoubleBuffered(true);
    }

    public void paintComponent (Graphics g) {
		((Graphics2D) g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,0.0f)); // draw transparent background
		super.paintComponent(g);
		((Graphics2D) g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,1.0f)); // turn on opacity
		g.setColor(Color.RED);
		g.fillRect(20, 20, 500, 300);
	}

    public void createAndShowGUI() {

	    emptyLabel = new JLabel("  ") {
	    	@Override
	    	public Dimension getMinimumSize() {
	    		return new Dimension(50, 100);
	    	};
	    	@Override
	    	public Dimension getPreferredSize() {
	    		return new Dimension(50, 100);
	    	};
	    };

        buildInfoP();
        buildrenameBtn();
        buildLabelPane();
        buildButtonPane();
        buildSettlementNameComboBox();
        buildZoomSlider();
        buildBanner();
        buildWeatherPanel();

	    WebPanel topPane = new WebPanel(new BorderLayout(20, 20));
	    topPane.setBackground(new Color(0,0,0,128));
	    topPane.setOpaque(false);

	    JPanel settlementPanel = new JPanel();
		settlementPanel.setBackground(new Color(0,0,0,128));
		settlementPanel.setOpaque(false);

		settlementPanel.add(settlementListBox, BorderLayout.NORTH);

	    mapPanel.add(topPane, BorderLayout.NORTH);

	    WebPanel weatherPane = new WebPanel(new GridLayout(1, 3, 5, 5));
	    weatherPane.setBackground(new Color(0,0,0,128));
	    weatherPane.setOpaque(false);

	    weatherPane.add(weatherButton00);
	    weatherPane.add(weatherButton01);
	    weatherPane.add(weatherButton10);

	    WebPanel sunPane = createSunPane();

	    WebPanel panel = new WebPanel(new BorderLayout(5, 5));
	    panel.setBackground(new Color(0,0,0,128));
	    panel.setOpaque(false);
	    panel.add(sunPane, BorderLayout.NORTH);

	    WebPanel centerPanel = new WebPanel(new BorderLayout(2, 2));
	    centerPanel.setBackground(new Color(0,0,0,128));
	    centerPanel.setOpaque(false);

	    WebPanel westPanel = new WebPanel(new BorderLayout(5, 5));
	    westPanel.setBackground(new Color(0,0,0,128));
	    westPanel.setOpaque(false);
	    westPanel.add(panel, BorderLayout.CENTER);
	    westPanel.add(weatherPane, BorderLayout.NORTH);

        // Make zoom pane drag-able
		// Register cm cmZoom.registerComponent(zoomPane);

		centerPanel.add(westPanel, BorderLayout.WEST);
		centerPanel.add(settlementPanel, BorderLayout.NORTH);

		topPane.add(centerPanel, BorderLayout.CENTER);
		topPane.add(bannerBar, BorderLayout.NORTH);

	    controlPane = new JPanel(new BorderLayout());//GridLayout(2,1,10,2));
	    controlPane.setBackground(new Color(0,0,0,128));//,0));
		controlPane.setOpaque(false);

	    controlCenterPane = new JPanel(new FlowLayout(FlowLayout.CENTER));
	    controlCenterPane.setBackground(new Color(0,0,0,128));
	    controlCenterPane.setOpaque(false);
	    controlCenterPane.add(zoomSlider);
	    controlCenterPane.setAlignmentY(CENTER_ALIGNMENT);

       	controlPane.add(buttonPane, BorderLayout.NORTH);
	    controlPane.add(labelPane, BorderLayout.SOUTH);
       	controlPane.add(controlCenterPane, BorderLayout.CENTER);

	    eastPane = new JPanel(new BorderLayout());//GridLayout(3,1,10,2));
		eastPane.setBackground(new Color(0,0,0,15));
		eastPane.setBackground(new Color(0,0,0));//,0));
		eastPane.setOpaque(false);
        eastPane.add(emptyLabel, BorderLayout.EAST);
        eastPane.add(emptyLabel, BorderLayout.WEST);
        eastPane.add(emptyLabel, BorderLayout.NORTH);
        eastPane.add(emptyLabel, BorderLayout.SOUTH);
        eastPane.add(controlPane, BorderLayout.CENTER);

        centerPanel.add(eastPane, BorderLayout.EAST);

        mapPanel.setVisible(true);
    }

    /**
     * Creates the sun data panel.
     * 
     * @return
     */
    private WebPanel createSunPane() {
	    WebPanel sunPane = new WebPanel(new BorderLayout(3, 3));
	    sunPane.setBorder(new EmptyBorder(5, 5, 5, 5));
	    sunPane.setBackground(new Color(0, 0, 0, 128));
	    sunPane.setOpaque(false);

	    WebPanel roundPane = new WebPanel(new GridLayout(9, 1, 0, 0)) {
	        @Override
	        protected void paintComponent(Graphics g) {
	           super.paintComponent(g);
	           Dimension arcs = new Dimension(20, 20); //Border corners arcs {width,height}, change this to whatever you want
	           int width = getWidth();
	           int height = getHeight();
	           Graphics2D graphics = (Graphics2D) g;
	           graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

	           //Draws the rounded panel with borders.
	           graphics.setColor(getBackground());
	           graphics.fillRoundRect(0, 0, width-1, height-1, arcs.width, arcs.height);//paint background
	           graphics.setColor(getForeground());
	           graphics.drawRoundRect(0, 0, width-1, height-1, arcs.width, arcs.height);//paint border
	        }
	    };

	    roundPane.setBackground(new Color(0,0,0,128));
	    roundPane.setOpaque(false);
	    roundPane.setPreferredSize(260, 185);
	    sunPane.add(roundPane, BorderLayout.EAST);
  		
	    double time[] = orbitInfo.getSunriseSunsetTime(mapPanel.getSettlement().getCoordinates());

	    projectSunriseLabel = new WebLabel(StyleId.labelShadow, PROJECTED_SUNRISE 
	    		+ Math.round(time[0] *10.0)/10.0 + MSOL);
	    
	    projectSunsetLabel = new WebLabel(StyleId.labelShadow, PROJECTED_SUNSET 
	    		+ Math.round(time[1] *10.0)/10.0 + MSOL);
	    
	    sunriseLabel = new WebLabel(StyleId.labelShadow, SUNRISE + PENDING);
		sunsetLabel = new WebLabel(StyleId.labelShadow, SUNSET + PENDING);
		zenithLabel = new WebLabel(StyleId.labelShadow, ZENITH + PENDING);
		maxSunLabel = new WebLabel(StyleId.labelShadow, MAX_LIGHT + PENDING);
		daylightLabel = new WebLabel(StyleId.labelShadow, DAYLIGHT + PENDING);
		
		projectDaylightLabel  = new WebLabel(StyleId.labelShadow, PROJECTED_DAYLIGHT 
	    		+ Math.round(time[2] *10.0)/10.0 + MSOL);
		
		currentSunLabel = new WebLabel(StyleId.labelShadow, CURRENT_LIGHT + PENDING);

		projectSunriseLabel.setFont(sunFont);
		sunriseLabel.setFont(sunFont);
		projectSunsetLabel.setFont(sunFont);
		sunsetLabel.setFont(sunFont);
		zenithLabel.setFont(sunFont);
		maxSunLabel.setFont(sunFont);
		daylightLabel.setFont(sunFont);
		projectDaylightLabel.setFont(sunFont);
		currentSunLabel.setFont(sunFont);

		Color orange = Color.orange;
		Color yellow = Color.yellow.brighter().brighter();
		Color white = Color.white;
		Color red = Color.red.brighter();
		projectSunriseLabel.setForeground(red);
		sunriseLabel.setForeground(red);
		
		projectSunsetLabel.setForeground(orange);
		sunsetLabel.setForeground(orange);
		
		projectDaylightLabel.setForeground(yellow);
		daylightLabel.setForeground(yellow);
		zenithLabel.setForeground(yellow);
		
		maxSunLabel.setForeground(white);
		currentSunLabel.setForeground(white);

		projectSunriseLabel.setToolTip("The projected time of sunrise");
		sunriseLabel.setToolTip("The time of sunrise");
		projectSunsetLabel.setToolTip("The projected time of sunset");
		sunsetLabel.setToolTip("The time of sunset");
		projectDaylightLabel.setToolTip("The projected duration of time in a sol having sunlight");
		daylightLabel.setToolTip("The duration of time in a sol having sunlight");
		zenithLabel.setToolTip("The time at which the solar irradiance is at max");
		maxSunLabel.setToolTip("The max solar irradiance of yester-sol as recorded");
		currentSunLabel.setToolTip("The current solar irradiance as recorded");

		roundPane.add(projectSunriseLabel);
		roundPane.add(sunriseLabel);
		roundPane.add(projectSunsetLabel);
		roundPane.add(sunsetLabel);
		roundPane.add(projectDaylightLabel);
		roundPane.add(daylightLabel);
		roundPane.add(zenithLabel);
		roundPane.add(maxSunLabel);
		roundPane.add(currentSunLabel);

		return sunPane;
    }

    /**
     * Gets the length of the most lengthy settlement name
     *
     * @return
     */
    private int getNameLength() {
    	Collection<Settlement> list = unitManager.getSettlements();
    	int max = 12;
    	for (Settlement s: list) {
    		int size = s.getNickName().length();
    		if (max < size)
    			max = size;
    	}
    	return max;
    }

    /**
     * Builds the settlement name combo box.
     */
	@SuppressWarnings("unchecked")
	public void buildSettlementNameComboBox() {

		settlementCBModel = new SettlementComboBoxModel();
		settlementListBox = new WebComboBox(StyleId.comboboxHover, settlementCBModel);
		settlementListBox.setWidePopup(true);
		settlementListBox.setPreferredSize(getNameLength() * 12, 30);
		settlementListBox.setBackground(new Color(51,25,0,128)); // dull gold color
		settlementListBox.setOpaque(false);
		settlementListBox.setFont(new Font("Dialog", Font.BOLD, 16));
		settlementListBox.setForeground(Color.BLACK);
		settlementListBox.setToolTipText(Msg.getString("SettlementWindow.tooltip.selectSettlement")); //$NON-NLS-1$
		settlementListBox.setRenderer(new PromptComboBoxRenderer());
		settlementListBox.addItemListener(new ItemListener() {
			@Override
			// unitUpdate will update combobox when a new building is added
			public void itemStateChanged(ItemEvent event) {
				Settlement s = (Settlement) event.getItem();
				if (s != null) {
					// Change to the selected settlement in SettlementMapPanel
					changeSettlement(s);
					// Update the sun data
					displaySunData(s.getCoordinates());
					// Update the display banner
					displayBanner(s);
				}
			}
		});

		int size = settlementListBox.getModel().getSize();

		if (size > 1) {
			// Gets the settlement
			Settlement s = mapPanel.getSettlement();
			// Change to the selected settlement in SettlementMapPanel
			if (s != null)
				changeSettlement(s);
		}

		else if (size == 1) {
			// Selects the first settlement
			settlementListBox.setSelectedIndex(0);
			// Gets the settlement
			Settlement s = (Settlement) settlementListBox.getSelectedItem();
			// Change to the selected settlement in SettlementMapPanel
			if (s != null)
				changeSettlement(s);
		}
	}

	/**
	 * Changes the map display to the selected settlement.
	 *
	 * @param s
	 */
	public void changeSettlement(Settlement s) {
		// Set the selected settlement in SettlementMapPanel
		mapPanel.setSettlement(s);
		// Set the population label in the status bar
		mapPanel.getSettlementWindow().setPop(s.getNumCitizens());
		// Set the box opaque
		settlementListBox.setOpaque(false);
	}

	/**
	 * Builds the text banner bar.
	 */
	public void buildBanner() {
		bannerBar = new DisplaySingle();
		bannerBar.setLcdColor(LcdColor.REDDARKRED_LCD);
		bannerBar.setDigitalFont(true);
		bannerBar.setPreferredSize(new Dimension(150, 30));
		bannerBar.setVisible(true);
		bannerBar.setLcdNumericValues(false);
		bannerBar.setLcdValueFont(new Font("Serif", Font.ITALIC, 8));
		bannerBar.setLcdText("...");
		bannerBar.setLcdTextScrolling(true);
	}

	/**
	 * Updates the weather parameters.
	 *
	 * @param s
	 * @return
	 */
	public boolean updateWeather(Settlement s) {
		boolean result = false;

		Coordinates c = s.getCoordinates();

       	double temperature =  Math.round(getTemperature(c)*100.0)/100.0;
       	if (temperatureCache != temperature) {
       		temperatureCache = temperature;
    		tString = getTemperatureString(temperature);
       		result = true;
       	}

		double windSpeed = Math.round(getWindSpeed(c)*100.0)/100.0;
       	if (windSpeedCache != windSpeed) {
       		windSpeedCache = windSpeed;
       		wsString = getWindSpeedString(windSpeed);
       		result = true;
       	}

		double zenithAngle = getZenithAngle(c);
       	if (zenithAngleCache != zenithAngle) {
       		zenithAngleCache = zenithAngle;
       		zaString = getZenithAngleString(zenithAngle);
       		result = true;
       	}

        double opticalDepth =  Math.round(getOpticalDepth(c)*100.0)/100.0;
       	if (opticalDepthCache != opticalDepth) {
       		opticalDepthCache = opticalDepth;
       		odString =  getOpticalDepthString(opticalDepth);
       		result = true;
       	}

       	return result;
	}

	/**
	 * Puts together the display string for the banner bar.
	 * 
	 * @param s
	 */
	public void displayBanner(Settlement s) {
       	if (updateWeather(s)) {
       		String resources = resourceCache.get(s);
       		if (resources == null)
       			resources = "";
       		bannerBar.setLcdText(resources + TEMPERATURE + tString + WINDSPEED + wsString
       				+ ZENITH_ANGLE + zaString + OPTICAL_DEPTH + odString);
       	}
	}

    public double getTemperature(Coordinates c) {
		return weather.getTemperature(c);
    }

    public String getTemperatureString(double value) {
    	// Note: Use of Msg.getString("temperature.sign.degreeCelsius") for the degree sign 
    	// does not work on the digital banner.
    	// May use of " °C", " �C", or " deg C" 
    	return fmt.format(value) + " deg C";
    }

    public double getWindSpeed(Coordinates c) {
		return weather.getWindSpeed(c);
    }

    public String getWindSpeedString(double value) {
    	return fmt2.format(value) + " " + Msg.getString("windspeed.unit.meterpersec"); //$NON-NLS-1$
    }

    public String getAirPressureString(double value) {
    	return fmt2.format(value) + " " + Msg.getString("pressure.unit.kPa"); //$NON-NLS-1$
    }

    public double getAirPressure(Coordinates c) {
    	return Math.round(weather.getAirPressure(c) *100.0) / 100.0;
    }

    public int getWindDirection(Coordinates c) {
  		return weather.getWindDirection(c);
    }

    public String getWindDirectionString(double value) {
       	return fmt.format(value) + " deg";
    }

    public double getOpticalDepth(Coordinates c) {
 		return surfaceFeatures.getOpticalDepth(c);
    }

    public String getOpticalDepthString(double value) {
     	return fmt2.format(value);
    }

    public double getZenithAngle(Coordinates c) {
 		return orbitInfo.getSolarZenithAngle(c);
     }

    public String getZenithAngleString(double value) {
     	return fmt2.format(value * RADIANS_TO_DEGREES) + " deg";
    }

    public double getSolarDeclination() {
 		return orbitInfo.getSolarDeclinationAngleDegree();
     }

    public String getSolarDeclinationString(double value) {
     	return fmt2.format(value) + " " + Msg.getString("direction.degreeSign"); //$NON-NLS-1$
    }

    public double getAirDensity(Coordinates c) {
		return weather.getAirDensity(c);
    }

    public String getAirDensityString(double value) {
     	return fmt2.format(value) + " " + Msg.getString("airDensity.unit.gperm3"); //$NON-NLS-1$
    }

    public double getSolarIrradiance(Coordinates c) {
  		return surfaceFeatures.getSolarIrradiance(c);
      }

    public String getSolarIrradianceString(double value) {
      	return fmt2.format(value) + " " + Msg.getString("solarIrradiance.unit"); //$NON-NLS-1$
    }

    /**
     * Builds the weather panel
     */
	public void buildWeatherPanel() {
        int size = MainWindow.WEATHER_ICON_SIZE;

    	weatherButton00 = new WebButton(StyleId.buttonUndecorated);
    	weatherButton00.setPreferredSize(new Dimension(size, size));
    	weatherButton01 = new WebButton(StyleId.buttonUndecorated);
    	weatherButton01.setPreferredSize(new Dimension(size, size));
    	weatherButton10 = new WebButton(StyleId.buttonUndecorated);
    	weatherButton10.setPreferredSize(new Dimension(size, size));

	    weatherButtons[0] = weatherButton00;
	    weatherButtons[1] = weatherButton01;
	    weatherButtons[2] = weatherButton10;

        updateIcon();
	}

	/**
	 * Update the weather icon
	 */
	public void updateIcon() {
       	String[] s = determineIcon();

       	for (int i=0; i<3; i++) {
       		String sIcon = s[i];
	    	if (!iconCache[i].equals(sIcon)) {
	    		iconCache[i] = sIcon;

	    		Icon icon = null;

	        	if (sIcon.equals(SANDSTORM_SVG)) {
	        		icon = sandstorm;
	        	}
	        	else if (sIcon.equals(DUST_DEVIL_SVG)) {
	        		icon = dustDevil;
	        	}
	        	else if (sIcon.equals(SAND_SVG)) {
	        		icon = sand;
	        	}
	        	else if (sIcon.equals(HAZY_SVG)) {
	        		icon = hazy;
	        	}
	        	else if (sIcon.equals(COLD_WIND_SVG)) {
	        		icon = coldWind;
	        	}
	        	
	        	if (sIcon.equals(FROST_WIND_SVG)) {
	        		icon = frostWind;
	        	}
	        	else if (sIcon.equals(SUN_SVG)) {
	        		icon = sun;
	        	}
	        	else if (sIcon.equals(DESERT_SUN_SVG)) {
	        		icon = desertSun;
	        	}
	        	else if (sIcon.equals(CLOUDY_SVG)) {
	        		icon = cloudy;
	        	}
            	else if (sIcon.equals(SNOWFLAKE_SVG)) {
	        		icon = snowflake;
	        	}
	        	else if (sIcon.equals(ICE_SVG)) {
	        		icon = ice;
	        	}
	        	else if (sIcon.equals("")) {
	        		icon = emptyIcon;
	        	}

	    		weatherButtons[i].setIcon(icon);
	    	}
       	}
	}

    /**
     * Determines which the weather icon to be shown
     *
     * @return
     */
    public String[] determineIcon() {

       	String icon00 = "";
       	String icon01 = "";
       	String icon10 = "";
//       	String icon11 = "";

    	if (temperatureCache < -40) {
    		icon00 = ICE_SVG;
    	}

    	else if (temperatureCache < 0) {
			icon00 = SNOWFLAKE_SVG;
    	}

    	else if (temperatureCache < 10) {
			icon00 = CLOUDY_SVG;
    	}

    	else if (temperatureCache < 22)
    		icon00 = DESERT_SUN_SVG;

    	else
    		icon00 = SUN_SVG;

		///////////////////////////////////////////////

		if (windSpeedCache > 30D) {

			if (opticalDepthCache > 0.75)
				icon01 = SANDSTORM_SVG;
			else if (temperatureCache < 0)
				icon01 = FROST_WIND_SVG;
			else
				icon01 = COLD_WIND_SVG;
		}

		else if (windSpeedCache > 20D) {

			if (opticalDepthCache > 0.75)
				icon01 = DUST_DEVIL_SVG;
			else if (temperatureCache < 0)
				icon01 = FROST_WIND_SVG;
			else
				icon01 = COLD_WIND_SVG;
		}

		else if (windSpeedCache > 10D) {

		   	if (temperatureCache < 0)
				icon01 = FROST_WIND_SVG;
			else
				icon01 = COLD_WIND_SVG;
		}

		else
			icon01 = "";

		///////////////////////////////////////////////

		if (opticalDepthCache > .5)
			icon10 = SAND_SVG;

    	else if (opticalDepthCache > 0.3) {
    		icon10 = HAZY_SVG;
    	}

		else
			icon10 = "";

    	return new String[] {icon00, icon01, icon10};//, icon11};
    }

	class PromptComboBoxRenderer extends DefaultListCellRenderer {

		private static final long serialVersionUID = 1L;
		private String prompt;

		public PromptComboBoxRenderer(){
		    setHorizontalAlignment(CENTER);
		}

		public PromptComboBoxRenderer(String prompt){
				this.prompt = prompt;
			}

		public Component getListCellRendererComponent(JList<?> list, Object value,
		            int index, boolean isSelected, boolean cellHasFocus) {
				Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

				if (value == null) {
					setText(prompt);
					return this;
				}

				if (isSelected) {
		        	  c.setForeground(Color.black);
		        	  c.setBackground(new Color(255,229,204,50)); // pale orange
		          } else {
						c.setForeground(Color.black);
				        c.setBackground(new Color(184,134,11,50)); // mud orange
		          }

		        return c;
		    }
	}

    public void buildZoomSlider() {

        UIDefaults sliderDefaults = new UIDefaults();

        sliderDefaults.put("Slider.thumbWidth", 15);
        sliderDefaults.put("Slider.thumbHeight", 15);
        sliderDefaults.put("Slider:SliderThumb.backgroundPainter", new Painter<JComponent>() {
            public void paint(Graphics2D g, JComponent c, int w, int h) {
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setStroke(new BasicStroke(2f));
                g.setColor(Color.ORANGE.darker().darker());
                g.fillOval(1, 1, w-1, h-1);
                g.setColor(Color.WHITE);
                g.drawOval(1, 1, w-1, h-1);
            }
        });
        sliderDefaults.put("Slider:SliderTrack.backgroundPainter", new Painter<JComponent>() {
            public void paint(Graphics2D g, JComponent c, int w, int h) {
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setStroke(new BasicStroke(2f));
                //g.setColor(new Color(139,69,19)); // brown
                g.setColor(Color.ORANGE.darker().darker());
                g.fillRoundRect(0, 6, w, 6, 6, 6); // g.fillRoundRect(0, 6, w-1, 6, 6, 6);
                g.setColor(Color.WHITE);
                g.drawRoundRect(0, 6, w, 6, 6, 6);
            }
        });

        zoomSlider = new JSlider(JSlider.VERTICAL, 0, 90, 10);//-20, 30, 0);
        zoomSlider.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 100));
        zoomSlider.setPreferredSize(new Dimension(40, 300));
        zoomSlider.setSize(new Dimension(40, 300));

		zoomSlider.setMajorTickSpacing(30);
		zoomSlider.setMinorTickSpacing(10);
		zoomSlider.setPaintTicks(true);
		zoomSlider.setPaintLabels(true);
		zoomSlider.setForeground(Color.ORANGE.darker().darker());

		zoomSlider.setOpaque(false);
		zoomSlider.setToolTipText(Msg.getString("SettlementTransparentPanel.tooltip.zoom")); //$NON-NLS-1$
		zoomSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				// Change scale of map based on slider position.
				int sliderValue = zoomSlider.getValue();
				if (sliderValue == 0)
					sliderValue = 1;
				mapPanel.setScale(sliderValue);
			}
		});

		// Add mouse wheel listener for zooming.
		mapPanel.addMouseWheelListener(new MouseWheelListener() {
			public void mouseWheelMoved(MouseWheelEvent evt) {
				int numClicks = evt.getWheelRotation();
				if (numClicks > 0) {
					// Move zoom slider down.
					if (zoomSlider.getValue() > zoomSlider.getMinimum())
						zoomSlider.setValue(zoomSlider.getValue() - 1);
				}
				else if (numClicks < 0) {
					// Move zoom slider up.
					if (zoomSlider.getValue() < zoomSlider.getMaximum())
						zoomSlider.setValue(zoomSlider.getValue() + 1);
				}
			}
		});

    }

    public void buildInfoP() {

    	SvgIcon icon = IconManager.getIcon ("info");
    	icon.apply(new SvgStroke(Color.ORANGE));
    	infoButton = new WebButton(StyleId.buttonUndecorated, icon);

		infoButton.setPreferredSize(new Dimension(32, 32));
		infoButton.setOpaque(false);
		infoButton.setBackground(new Color(0,0,0,128));
		infoButton.setContentAreaFilled(false);
		infoButton.setBorderPainted(false);
		infoButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Settlement settlement = mapPanel.getSettlement();
				if (settlement != null) {
					desktop.openUnitWindow(settlement, false);
				}
			};
		});

//		infoP.add(infoButton);
    }

    public void buildrenameBtn() {

    	ImageIcon icon = new LazyIcon("edit").getIcon();
    	renameBtn = new WebButton(StyleId.buttonUndecorated, icon);
    	renameBtn.setPreferredSize(new Dimension(32, 32));
		renameBtn.setOpaque(false);
		renameBtn.setBackground(new Color(0,0,0,128));
		renameBtn.setContentAreaFilled(false);
		renameBtn.setBorderPainted(false);

		renameBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				openRenameDialog();
			};
		});
    }

    public void buildButtonPane() {

        buttonPane = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        buttonPane.setPreferredSize(new Dimension(150, 36));
        buttonPane.setBackground(new Color(0,0,0,128));
        buttonPane.setOpaque(false);

		// Create rotate clockwise button.
        final ImageIcon cwIcon = new LazyIcon("right").getIcon();
        WebButton cwButton = new WebButton(StyleId.buttonUndecorated, cwIcon);
        cwButton.setPreferredSize(new Dimension(32, 32));
        cwButton.setOpaque(false);
		cwButton.setBorderPainted(false);
		cwButton.setContentAreaFilled(false);
		cwButton.setBackground(new Color(0,0,0,128));

		cwButton.setToolTipText(Msg.getString("SettlementTransparentPanel.tooltip.clockwise")); //$NON-NLS-1$
		cwButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				mapPanel.setRotation(mapPanel.getRotation() + ROTATION_CHANGE);
			}
		});

		// Create center button.
        final ImageIcon centerIcon = new LazyIcon("center").getIcon();
		WebButton recenterButton = new WebButton(StyleId.buttonUndecorated, centerIcon);
		recenterButton.setPreferredSize(new Dimension(32, 32));
		recenterButton.setOpaque(false);
		recenterButton.setBorderPainted(false);
		recenterButton.setContentAreaFilled(false);
		recenterButton.setBackground(new Color(0,0,0,128));

		recenterButton.setToolTipText(Msg.getString("SettlementTransparentPanel.tooltip.recenter")); //$NON-NLS-1$
		recenterButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				mapPanel.reCenter();
				zoomSlider.setValue(0);
			}
		});

		// Create rotate counter-clockwise button.
        final ImageIcon ccwIcon = new LazyIcon("left").getIcon();
        WebButton ccwButton = new WebButton(StyleId.buttonUndecorated, ccwIcon);
        ccwButton.setPreferredSize(new Dimension(32, 32));
		ccwButton.setOpaque(false);
		ccwButton.setBorderPainted(false);
		ccwButton.setContentAreaFilled(false);
		ccwButton.setBackground(new Color(0,0,0,128));

		ccwButton.setToolTipText(Msg.getString("SettlementTransparentPanel.tooltip.counterClockwise")); //$NON-NLS-1$
		ccwButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				mapPanel.setRotation(mapPanel.getRotation() - ROTATION_CHANGE);
			}
		});

		buttonPane.add(ccwButton);
		buttonPane.add(recenterButton);
		buttonPane.add(cwButton);

		// Need to clean up these icons
//				img.clockwise          = /icons/map/Clockwise.png
//				img.counterClockwise   = /icons/map/CounterClockwise.png
//				img.centerMap          = /icons/map/CenterMap.png
//				img.cw					= /icons/map/CW.png
//				img.ccw					= /icons/map/CCW.png
//				img.recenter			= /icons/map/recenter.png
//				img.cw_yellow			= /icons/map/CW_yellow.png
//				img.ccw_yellow			= /icons/map/CCW_yellow.png
//				img.recenter_yellow		= /icons/map/recenter_yellow.png

		buttonPane.add(emptyLabel);
    }

    public void buildLabelPane() {
        labelPane = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        labelPane.setPreferredSize(new Dimension(150, 36));
        labelPane.setBackground(new Color(0,0,0,128));
		labelPane.setOpaque(false);

	    final ImageIcon labelsIcon = new LazyIcon("stack").getIcon();
	    WebButton labelsButton = new WebButton(StyleId.buttonUndecorated,
	    		Msg.getString("SettlementTransparentPanel.button.labels"), labelsIcon);  //$NON-NLS-1$

		labelsButton.setFont(new Font("Dialog", Font.BOLD, 13));
		labelsButton.setForeground(Color.ORANGE.darker().darker());
		labelsButton.setPreferredSize(new Dimension(32, 32));
		labelsButton.setVerticalAlignment(JLabel.CENTER);
		labelsButton.setHorizontalAlignment(JLabel.CENTER);

		labelsButton.setOpaque(false);
		labelsButton.setBackground(new Color(0,0,0,128));
		labelsButton.setContentAreaFilled(false); //more artifact when enabled
		labelsButton.setBorderPainted(false);

		labelsButton.setToolTipText(Msg.getString("SettlementTransparentPanel.tooltip.labels")); //$NON-NLS-1$
		labelsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				JButton button = (JButton) evt.getSource();
				if (labelsMenu == null) {
					labelsMenu = createLabelsMenu();
				}
				labelsMenu.show(button, 0, button.getHeight());
				//repaint();
			}
		});

		labelPane.add(renameBtn);
		labelPane.add(infoButton);
		labelPane.add(labelsButton);

		labelPane.add(emptyLabel);
	}

	/**
	 * Create the labels popup menu.
	 * @return popup menu.
	 */
	public JPopupMenu createLabelsMenu() {
		JPopupMenu popMenu = new JPopupMenu(Msg.getString("SettlementWindow.menu.labelOptions")); //$NON-NLS-1$
		popMenu.setOpaque(false);
		popMenu.setBackground(new Color(0,0,0,128));
		popMenu.setBorderPainted(false);

		// Create Day Night Layer menu item.
		JCustomCheckBoxMenuItem dayNightLabelMenuItem = new JCustomCheckBoxMenuItem(
				Msg.getString("SettlementWindow.menu.daylightTracking"), mapPanel.isDaylightTrackingOn()); //$NON-NLS-1$
		dayNightLabelMenuItem.setForeground(new Color(139,69,19));
		dayNightLabelMenuItem.setContentAreaFilled(false);
		dayNightLabelMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				mapPanel.setShowDayNightLayer(!mapPanel.isDaylightTrackingOn());
			}
		});
		dayNightLabelMenuItem.setSelected(mapPanel.isDaylightTrackingOn());
		popMenu.add(dayNightLabelMenuItem);

		// Create building label menu item.
		buildingLabelMenuItem = new JCustomCheckBoxMenuItem(
				Msg.getString("SettlementWindow.menu.buildings"), mapPanel.isShowBuildingLabels()); //$NON-NLS-1$
		// Add setting setForeground setContentAreaFilled setOpaque
		buildingLabelMenuItem.setForeground(new Color(139,69,19));
		buildingLabelMenuItem.setContentAreaFilled(false);
		buildingLabelMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				mapPanel.setShowBuildingLabels(!mapPanel.isShowBuildingLabels());
			}
		});
		popMenu.add(buildingLabelMenuItem);

		// Create construction/salvage label menu item.
		constructionLabelMenuItem = new JCustomCheckBoxMenuItem(
				Msg.getString("SettlementWindow.menu.constructionSites"), mapPanel.isShowConstructionLabels()); //$NON-NLS-1$
		constructionLabelMenuItem.setForeground(new Color(139,69,19));
		constructionLabelMenuItem.setContentAreaFilled(false);
		constructionLabelMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				mapPanel.setShowConstructionLabels(!mapPanel.isShowConstructionLabels());
			}
		});
		popMenu.add(constructionLabelMenuItem);

		// Create vehicle label menu item.
		vehicleLabelMenuItem = new JCustomCheckBoxMenuItem(
				Msg.getString("SettlementWindow.menu.vehicles"), mapPanel.isShowVehicleLabels()); //$NON-NLS-1$
		vehicleLabelMenuItem.setForeground(new Color(139,69,19));
		vehicleLabelMenuItem.setContentAreaFilled(false);
		vehicleLabelMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				mapPanel.setShowVehicleLabels(!mapPanel.isShowVehicleLabels());
			}
		});
		popMenu.add(vehicleLabelMenuItem);

		// Create person label menu item.
		personLabelMenuItem = new JCustomCheckBoxMenuItem(
				Msg.getString("SettlementWindow.menu.people"), mapPanel.isShowPersonLabels()); //$NON-NLS-1$
		personLabelMenuItem.setForeground(new Color(139,69,19));
		personLabelMenuItem.setContentAreaFilled(false);
		personLabelMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				mapPanel.setShowPersonLabels(!mapPanel.isShowPersonLabels());
			}
		});
		popMenu.add(personLabelMenuItem);

		// Create person label menu item.
		robotLabelMenuItem = new JCustomCheckBoxMenuItem(
				Msg.getString("SettlementWindow.menu.robots"), mapPanel.isShowRobotLabels()); //$NON-NLS-1$
		robotLabelMenuItem.setForeground(new Color(139,69,19));
		robotLabelMenuItem.setContentAreaFilled(false);
		robotLabelMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				mapPanel.setShowRobotLabels(!mapPanel.isShowRobotLabels());
			}
		});
		popMenu.add(robotLabelMenuItem);

		popMenu.pack();

		return popMenu;
	}


	public class JCustomCheckBoxMenuItem extends JCheckBoxMenuItem {

		public JCustomCheckBoxMenuItem(String s, boolean b) {
			super(s, b);
		}
	}


	/**
	 * Open dialog box to take in the new settlement name
	 */
	public void openRenameDialog() {

		String oldName = mapPanel.getSettlement().getName();

		JDialog.setDefaultLookAndFeelDecorated(true);
		String newName = askNameDialog();
		if (!oldName.equals(newName)
				&& newName != null
				&& !newName.trim().equals("")
				&& newName.trim().length() != 0) {
			mapPanel.getSettlement().changeName(newName.trim());

			desktop.closeToolWindow(SettlementWindow.NAME);
			desktop.openToolWindow(SettlementWindow.NAME);

		} else {
			return;
		}
	}

	/**
	 * <p>Checks if a String is whitespace, empty ("") or null.</p>
	 *
	 * <pre>
	 * StringUtils.isBlank(null)      = true
	 * StringUtils.isBlank("")        = true
	 * StringUtils.isBlank(" ")       = true
	 * StringUtils.isBlank("bob")     = false
	 * StringUtils.isBlank("  bob  ") = false
	 * </pre>
	 *
	 * @param str  the String to check, may be null
	 * @return <code>true</code> if the String is null, empty or whitespace
	 * @since 2.0
	 * @author commons.apache.org
	 */
	public static boolean isBlank(String str) {
	    int strLen;
	    if (str == null || (strLen = str.length()) == 0) {
	        return true;
	    }
	    for (int i = 0; i < strLen; i++) {
	        if ((Character.isWhitespace(str.charAt(i)) == false)) {
	            return false;
	        }
	    }
	    return true;
	}

	/**
	 * Ask for a new Settlement name
	 * @return pop up jDialog
	 */
	public String askNameDialog() {
		return JOptionPane
			.showInputDialog(desktop,
					Msg.getString("SettlementWindow.JDialog.changeSettlementName.input"), //$NON-NLS-1$
					Msg.getString("SettlementWindow.JDialog.changeSettlementName.title"), //$NON-NLS-1$
			        JOptionPane.QUESTION_MESSAGE);
	}

	/**
	 * Inner class combo box model for settlements.
	 */
	public class SettlementComboBoxModel extends DefaultComboBoxModel<Object>
		implements UnitManagerListener, UnitListener {

		/**
		 * Constructor.
		 */
		public SettlementComboBoxModel() {
			// User DefaultComboBoxModel constructor.
			super();
			// Initialize settlement list.
			updateSettlements();
			// Add this as a unit manager listener.
			unitManager.addUnitManagerListener(this);

			// Add addUnitListener
			Collection<Settlement> settlements = unitManager.getSettlements();
			List<Settlement> settlementList = new ArrayList<>(settlements);
			Collections.sort(settlementList);
			Iterator<Settlement> i = settlementList.iterator();
			while (i.hasNext()) {
				i.next().addUnitListener(this);
			}
		}

		/**
		 * Update the list of settlements.
		 */
		private void updateSettlements() {
			// Clear all elements
			removeAllElements();

			List<Settlement> settlements = new ArrayList<>();

			// Add the command dashboard button
			if (mode == GameMode.COMMAND) {
				settlements = unitManager.getCommanderSettlements();
			}

			else if (mode == GameMode.SANDBOX) {
				settlements.addAll(unitManager.getSettlements());
			}

			Collections.sort(settlements);

			Iterator<Settlement> i = settlements.iterator();
			while (i.hasNext()) {
				addElement(i.next());
			}
		}

		@Override
		public void unitManagerUpdate(UnitManagerEvent event) {
			if (event.getUnit().getUnitType() == UnitType.SETTLEMENT) {
				updateSettlements();
			}
		}

		@Override
		public void unitUpdate(UnitEvent event) {
			// Note: Easily 100+ UnitEvent calls every second
			UnitEventType eventType = event.getType();
			if (eventType == UnitEventType.ADD_BUILDING_EVENT) {
				Object target = event.getTarget();
				Building building = (Building) target; // overwrite the dummy building object made by the constructor
				BuildingManager mgr = building.getBuildingManager();
				Settlement s = mgr.getSettlement();
				mapPanel.setSettlement(s);
				// Updated ComboBox
				settlementListBox.setSelectedItem(s);
			}

			else if (eventType == UnitEventType.REMOVE_ASSOCIATED_PERSON_EVENT) {
				// Update the number of citizens
				Settlement s = (Settlement) settlementListBox.getSelectedItem();
				// Set the selected settlement in SettlementMapPanel
				mapPanel.setSettlement(s);
				// Set the population label in the status bar
				mapPanel.getSettlementWindow().setPop(s.getNumCitizens());
				// Set the box opaque
				settlementListBox.setOpaque(false);
			}
		}

		/**
		 * Prepare class for deletion.
		 */
		public void destroy() {
			unitManager.removeUnitManagerListener(this);
			Collection<Settlement> settlements = unitManager.getSettlements();
			List<Settlement> settlementList = new ArrayList<>(settlements);
			Iterator<Settlement> i = settlementList.iterator();
			while (i.hasNext()) {
				i.next().removeUnitListener(this);
			}
		}
	}


	public WebComboBox getSettlementListBox() {
		return settlementListBox;
	}

	/**
	 * Gets the sunlight data and display it on the top left panel of the settlement map.
	 */
	public void displaySunData(Coordinates location) {
	    double time[] = orbitInfo.getSunriseSunsetTime(mapPanel.getSettlement().getCoordinates());
	    
		projectSunriseLabel.setText (PROJECTED_SUNRISE + Math.round(time[0] *10.0)/10.0 + MSOL);
		projectSunsetLabel.setText (PROJECTED_SUNSET + Math.round(time[1] *10.0)/10.0 + MSOL);
		projectDaylightLabel.setText (PROJECTED_DAYLIGHT + Math.round(time[2] *10.0)/10.0 + MSOL);
		
		// Retrieve the yestersol's sun record
		SunData data = weather.getSunRecord(location);
		
		if (data == null) {
			logger.warning(60_000L, "Sun data at " + location + " is not available.");
			return;
		}

		sunriseLabel.setText(   SUNRISE + data.getSunrise() + MSOL);
		sunsetLabel.setText(    SUNSET + data.getSunset() + MSOL);
		daylightLabel.setText(  DAYLIGHT + data.getDaylight() + MSOL);
		zenithLabel.setText( 	ZENITH + data.getZenith() + MSOL);
		maxSunLabel.setText(    MAX_LIGHT + data.getMaxSun() + WM);
	}

	/**
	 * Prepares the resource data string for the new sol.
	 * 
	 * @param pulse
	 */
	public void prepBannerResourceString(ClockPulse pulse) {

		int sol = pulse.getMarsTime().getMissionSol();
		if (sol > 1) {
			Collection<Settlement> list = unitManager.getSettlements();
			for (Settlement s0: list) {
				prepareResourceStat(s0, sol);
			}
		}
	}

	@Override
	public StyleId getDefaultStyleId() {
		// Auto-generated method stub
		return null;
	}


	@Override
	public String getUIClassID() {
		// Auto-generated method stub
		return null;
	}


	@Override
	public void updateUI() {
		// Auto-generated method stub
	}

	@Override
	public void clockPulse(ClockPulse pulse) {
//		if (!isVisible())
//			return;
//		if (!isShowing()) 
//			return;
			
		if (pulse.isNewSol()) {
			// Redo the resource string once a sol
			prepBannerResourceString(pulse);
			// Update the sun data
			Settlement s = (Settlement)settlementListBox.getSelectedItem();
			displaySunData(s.getCoordinates());
		}
		
		// This can be removed once uiPulse is collapsed into timePulse
		MarsClock marsClock = pulse.getMarsTime();
		if (marsClock.isStable() && bannerBar != null && weatherButtons[0] != null) {
			Settlement s = (Settlement) settlementListBox.getSelectedItem();
			// When loading from a saved sim, s may be initially null
			if (s == null) 
				return;
			displayBanner(s);
			updateIcon();
			updateSunlight(s);
		}
	}

	/**
	 * Updates the sun data.
	 * 
	 * @param pulse
	 * @param s
	 */
	private void updateSunlight(Settlement s) {
		if (currentSunLabel == null)
			return;
	
		currentSunLabel.setText(
			CURRENT_LIGHT
			+ (int)getSolarIrradiance(s.getCoordinates()) 
			+ WM
		);
	}
	
	
	/**
	 * Prepares for the critical resource statistics String
	 *
	 * @param s
	 */
	private void prepareResourceStat(Settlement s, int missionSol) {
		String text = "";
		Map<Integer, Double> yestersolResources = s.gatherResourceStat(missionSol - 1);
		int size = yestersolResources.size();
		int i = 0;
		for (int id: yestersolResources.keySet()) {
			String resource = ResourceUtil.findAmountResourceName(id);
			double amount = yestersolResources.get(id);
			text += amount + " kg " + resource;
			i++;
			if (i == size - 1) {
				text += ")  ";
			}
			else {
				text += ",  ";
			}
		}

		if (text.equalsIgnoreCase(""))
			return;

		resourceCache.remove(s);
	   	resourceCache.put(s, YESTERSOL_RESOURCE + text);
	}


	@Override
	public void pauseChange(boolean isPaused, boolean showPane) {
        bannerBar.setLcdTextScrolling(!isPaused);
	}

	/**
	 * Prepare class for deletion.
	 */
	public void destroy() {
		desktop.getSimulation().getMasterClock().removeClockListener(this);
		
		mapPanel = null;
		settlementCBModel.destroy();
		desktop = null;
		settlementListBox = null;
		settlementCBModel = null;
	}

}

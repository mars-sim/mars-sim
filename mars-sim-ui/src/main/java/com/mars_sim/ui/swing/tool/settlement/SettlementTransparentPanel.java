/*
 * Mars Simulation Project
 * SettlementTransparentPanel.java
 * @date 2025-08-07
 * @author Manny Kung
 */
package com.mars_sim.ui.swing.tool.settlement;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeListener;

import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;

import com.mars_sim.core.GameManager;
import com.mars_sim.core.GameManager.GameMode;
import com.mars_sim.core.Simulation;
import com.mars_sim.core.UnitManager;
import com.mars_sim.core.UnitManagerEvent;
import com.mars_sim.core.UnitManagerListener;
import com.mars_sim.core.UnitType;
import com.mars_sim.core.building.BuildingConfig;
import com.mars_sim.core.building.BuildingManager;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.environment.OrbitInfo;
import com.mars_sim.core.environment.SunData;
import com.mars_sim.core.environment.SurfaceFeatures;
import com.mars_sim.core.environment.Weather;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.MainDesktopPane;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.components.NamedListCellRenderer;
import com.mars_sim.ui.swing.tool.settlement.SettlementMapPanel.DisplayOption;

import eu.hansolo.steelseries.gauges.DisplaySingle;
import eu.hansolo.steelseries.tools.LcdColor;

@SuppressWarnings({ "serial" })
public class SettlementTransparentPanel extends JComponent {

    /** default logger. */
    private static SimLogger logger = SimLogger.getLogger(SettlementTransparentPanel.class.getName());

    /** Rotation change (radians per rotation button press). */
    private static final double ROTATION_CHANGE = Math.PI / 20D;
    private static final double RADIANS_TO_DEGREES = 180D / Math.PI;

    /** Zoom change. */
    public static final double ZOOM_CHANGE = 0.25;

    private static final String TEMPERATURE     = "   Temperature: ";
    private static final String WINDSPEED       = "   Windspeed: ";
    private static final String ZENITH_ANGLE    = "   Zenith Angle: ";
    private static final String OPTICAL_DEPTH   = "   Optical Depth: ";

    private static final String PROJECTED_SUNRISE = "  Projected Sunrise: ";
    private static final String PROJECTED_SUNSET  = "   Projected Sunset: ";
    private static final String PROJECTED_DAYLIGHT= " Projected Daylight: ";
    private static final String SUNRISE           = "  Yestersol Sunrise: ";
    private static final String SUNSET            = "   Yestersol Sunset: ";
    private static final String DAYLIGHT          = " Yestersol Daylight: ";
    private static final String ZENITH            = "        Zenith Time: ";
    private static final String MAX_LIGHT         = "       Max Sunlight: ";
    private static final String CURRENT_LIGHT     = "   Current Sunlight: ";
    private static final String WM                = " W/m\u00B2 ";
    private static final String MSOL              = " msol ";
    private static final String PENDING           = " ...  ";

    private static final String YESTERSOL_RESOURCE = "Yestersol's Resources (";

    private int solCache;

    private double temperatureCache;
    private double opticalDepthCache;
    private double windSpeedCache;
    private double zenithAngleCache;

    private String tString;
    private String wsString;
    private String zaString;
    private String odString;

    private Font sunFont = new Font(Font.MONOSPACED, Font.PLAIN, 12);
    private Font sunBoldFont = new Font(Font.MONOSPACED, Font.BOLD, 12);

    // Thread-safe: simulation thread may update, EDT reads
    private Map<Settlement, String> resourceCache = new ConcurrentHashMap<>();

    private GameMode mode;

    private DisplaySingle bannerBar;
    private JSlider zoomSlider;

    private javax.swing.Timer zoomDebounce;
    private ChangeListener zoomListener;
    private MouseWheelListener mouseWheelListener;

    private JLabel emptyLabel;
    /** label for projected sunrise time. */
    private JLabel projectSunriseLabel;
    /** label for projected sunset time. */
    private JLabel projectSunsetLabel;
    /** label for projected daylight. */
    private JLabel projectDaylightLabel;
    /** label for sunrise time. */
    private JLabel sunriseLabel;
    /** label for sunset time. */
    private JLabel sunsetLabel;
    /** label for zenith time. */
    private JLabel zenithLabel;
    /** label for highest solar irradiance. */
    private JLabel maxSunLabel;
    /** label for the daylight period. */
    private JLabel daylightLabel;
    /** label for the daylight period. */
    private JLabel currentSunLabel;

    private JButton renameBtn;
    private JButton infoButton;
    private JLabel temperatureIcon;
    private JLabel windIcon;
    private JLabel opticalIcon;

    private JPopupMenu labelsMenu;
    /** Settlement Combo box */
    private JComboBox<Settlement> settlementListBox;
    /** Settlement Combo box model. */
    private SettlementComboBoxModel settlementCBModel;

    private SettlementMapPanel mapPanel;
    private MainDesktopPane desktop;

    private Weather weather;
    private SurfaceFeatures surfaceFeatures;
    private OrbitInfo orbitInfo;
    private UnitManager unitManager;

    /**
     * The panel with elements that are on top of the settlement map.
     *
     * @param desktop
     * @param mapPanel
     */
    public SettlementTransparentPanel(MainDesktopPane desktop, SettlementMapPanel mapPanel) {
        this.mapPanel = mapPanel;
        this.desktop = desktop;

        Simulation sim = desktop.getSimulation();
        this.unitManager = sim.getUnitManager();

        this.weather = sim.getWeather();
        this.surfaceFeatures = sim.getSurfaceFeatures();
        this.orbitInfo = sim.getOrbitInfo();

        mode = GameManager.getGameMode();
    }

    public void createAndShowGUI() {

        emptyLabel = new JLabel("  ") {
            @Override
            public Dimension getMinimumSize() {
                return new Dimension(50, 100);
            }
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(50, 100);
            }
        };

        buildInfoP();
        buildRenameBtn();
        var labelPane = buildLabelPane();
        var buttonPane = buildButtonPane();
        buildSettlementNameComboBox();
        buildZoomSlider();
        buildBanner();
        buildWeatherPanel();
        JPanel sunPane = buildSunPane();

        JPanel topPane = new JPanel(new BorderLayout(20, 20));
        topPane.setBackground(new Color(0,0,0,128));
        topPane.setOpaque(false);

        JPanel settlementPanel = new JPanel();
        settlementPanel.setBackground(new Color(0,0,0,128));
        settlementPanel.setOpaque(false);
        settlementPanel.add(settlementListBox, BorderLayout.NORTH);

        mapPanel.add(topPane, BorderLayout.NORTH);

        JPanel weatherPane = new JPanel(new GridLayout(1, 3, 5, 5));
        weatherPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        weatherPane.setBackground(new Color(0,0,0,128));
        weatherPane.setOpaque(false);

        weatherPane.add(temperatureIcon);
        weatherPane.add(windIcon);
        weatherPane.add(opticalIcon);

        JPanel sunlightPanel = new JPanel(new BorderLayout(5, 5));
        sunlightPanel.setBorder(new EmptyBorder(3, 3, 3, 3));
        sunlightPanel.setBackground(new Color(0,0,0,128));
        sunlightPanel.setOpaque(false);
        sunlightPanel.add(sunPane, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout(2, 2));
        centerPanel.setBackground(new Color(0,0,0,128));
        centerPanel.setOpaque(false);

        JPanel westPanel = new JPanel(new BorderLayout());
        westPanel.setBackground(new Color(0,0,0,128));
        westPanel.setOpaque(false);
        westPanel.add(weatherPane, BorderLayout.NORTH);
        westPanel.add(sunlightPanel, BorderLayout.CENTER);

        centerPanel.add(westPanel, BorderLayout.WEST);
        centerPanel.add(settlementPanel, BorderLayout.NORTH);

        topPane.add(centerPanel, BorderLayout.CENTER);
        topPane.add(bannerBar, BorderLayout.NORTH);

        var controlPane = new JPanel(new BorderLayout());
        controlPane.setBackground(new Color(0,0,0,128));
        controlPane.setOpaque(false);

        var controlCenterPane = new JPanel(new FlowLayout(FlowLayout.CENTER));
        controlCenterPane.setBackground(new Color(0,0,0,128));
        controlCenterPane.setOpaque(false);
        controlCenterPane.add(zoomSlider);
        controlCenterPane.setAlignmentY(CENTER_ALIGNMENT);

        controlPane.add(buttonPane, BorderLayout.NORTH);
        controlPane.add(labelPane, BorderLayout.SOUTH);
        controlPane.add(controlCenterPane, BorderLayout.CENTER);

        var eastPane = new JPanel(new BorderLayout());
        eastPane.setBackground(new Color(0,0,0,15));
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
     * @return panel
     */
    private JPanel buildSunPane() {
        JPanel sunPane = new JPanel(new BorderLayout(3, 3));
        sunPane.setBackground(new Color(0, 0, 0, 128));
        sunPane.setBorder(new BevelBorder(BevelBorder.LOWERED, Color.ORANGE, new Color(210, 105, 30)));

        JPanel roundPane = new JPanel(new GridLayout(9, 1, 0, 0));
        roundPane.setBackground(new Color(0, 0, 0, 128));
        roundPane.setOpaque(false);
        roundPane.setPreferredSize(new Dimension(230, 185)); // (260, 185), (293, 185);

        JXTaskPaneContainer taskPaneContainer = new JXTaskPaneContainer();
        taskPaneContainer.setBackground(new Color(0, 0, 0, 128));
        taskPaneContainer.setOpaque(false);
        JXTaskPane actionPane = new JXTaskPane();
        actionPane.setBackground(new Color(0, 0, 0, 128));
        actionPane.setOpaque(false);
        actionPane.getContentPane().setBackground(new Color(0, 0, 0, 128));
        actionPane.setTitle("Solar Data");
        actionPane.add(roundPane, BorderLayout.CENTER);
        taskPaneContainer.add(actionPane);
        sunPane.add(taskPaneContainer, BorderLayout.CENTER);

        double []projectSunTime = {0, 0, 0};
        if (mapPanel.getSettlement() != null) {
            projectSunTime = orbitInfo.getSunTimes(mapPanel.getSettlement().getCoordinates());
        }

        projectSunriseLabel = new JLabel(PROJECTED_SUNRISE
                + StyleManager.DECIMAL_MSOL.format(projectSunTime[0]));

        projectSunsetLabel = new JLabel(PROJECTED_SUNSET
                + StyleManager.DECIMAL_MSOL.format(projectSunTime[1]));

        projectDaylightLabel  = new JLabel(PROJECTED_DAYLIGHT
                + StyleManager.DECIMAL_MSOL.format(projectSunTime[2]));

        sunriseLabel = new JLabel(SUNRISE + PENDING);
        sunsetLabel = new JLabel(SUNSET + PENDING);
        daylightLabel = new JLabel(DAYLIGHT + PENDING);

        zenithLabel = new JLabel(ZENITH + PENDING);
        maxSunLabel = new JLabel(MAX_LIGHT + PENDING);

        currentSunLabel = new JLabel(CURRENT_LIGHT + PENDING);

        projectSunriseLabel.setFont(sunFont);
        sunriseLabel.setFont(sunFont);
        projectSunsetLabel.setFont(sunFont);
        sunsetLabel.setFont(sunFont);
        projectDaylightLabel.setFont(sunFont);
        daylightLabel.setFont(sunFont);

        zenithLabel.setFont(sunFont);
        maxSunLabel.setFont(sunFont);
        currentSunLabel.setFont(sunBoldFont);

        Color orange = Color.orange;
        Color brown = new Color(153, 102, 0).brighter();
        Color yellow = Color.yellow.brighter().brighter();
        Color white = Color.white;
        Color red = Color.pink;
        Color lightBlue = new Color(51, 204, 255);

        projectSunriseLabel.setForeground(red);
        sunriseLabel.setForeground(red);

        projectSunsetLabel.setForeground(brown);
        sunsetLabel.setForeground(brown);

        projectDaylightLabel.setForeground(yellow);
        daylightLabel.setForeground(yellow);

        zenithLabel.setForeground(white);
        maxSunLabel.setForeground(lightBlue);
        currentSunLabel.setForeground(orange);

        projectSunriseLabel.setToolTipText("The projected time of sunrise");
        sunriseLabel.setToolTipText("The time of yestersol sunrise");
        projectSunsetLabel.setToolTipText("The projected time of sunset");
        sunsetLabel.setToolTipText("The time of yestersol sunset");
        projectDaylightLabel.setToolTipText("The projected duration of time in a sol having sunlight");
        daylightLabel.setToolTipText("The duration of time in a sol having sunlight");
        zenithLabel.setToolTipText("The time at which the solar irradiance is at max");
        maxSunLabel.setToolTipText("The max solar irradiance of yestersol as recorded");
        currentSunLabel.setToolTipText("The current solar irradiance as recorded");

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
     * Gets the length of the most lengthy settlement name.
     */
    private int getNameLength() {
        Collection<Settlement> list = unitManager.getSettlements();
        int max = 12;
        for (Settlement s: list) {
            int size = s.getName().length();
            if (max < size)
                max = size;
        }
        return max;
    }

    /**
     * Builds the settlement name combo box.
     */
    private void buildSettlementNameComboBox() {

        settlementCBModel = new SettlementComboBoxModel();
        settlementListBox = new JComboBox<>(settlementCBModel);
        settlementListBox.setPreferredSize(new Dimension(getNameLength() * 12, 30));
        settlementListBox.setToolTipText(Msg.getString("SettlementWindow.tooltip.selectSettlement")); //$NON-NLS-1$
        settlementListBox.setRenderer(new NamedListCellRenderer());
        settlementListBox.addItemListener(event -> {
            if (event.getStateChange() == ItemEvent.SELECTED) {
                Settlement s = (Settlement) event.getItem();
                if (s != null) {
                    // Change to the selected settlement in SettlementMapPanel
                    changeSettlement(s);
                    // Update the sun data (UI writes queued to EDT internally)
                    displaySunData(s.getCoordinates());
                    // Update the display banner (UI write queued to EDT)
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
     */
    private void changeSettlement(Settlement s) {
        // Set the selected settlement in SettlementMapPanel
        mapPanel.setSettlement(s);
        // Set the population label in the status bar
        mapPanel.getSettlementWindow().setPop(s.getNumCitizens());
    }

    /**
     * Builds the text banner bar.
     */
    private void buildBanner() {
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
     * @return true if any cache changed
     */
    private boolean updateWeather(Settlement s) {
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

        double opticalDepth =  getOpticalDepth(c);
        if (opticalDepthCache != opticalDepth) {
            opticalDepthCache = opticalDepth;
            odString =  getOpticalDepthString(opticalDepth);
            result = true;
        }

        return result;
    }

    /**
     * Builds the banner text from current caches (does *not* touch UI).
     */
    private String buildBannerText(Settlement s) {
        String resources = (resourceCache != null) ? resourceCache.get(s) : null;
        if (resources == null) resources = "";
        StringBuilder sb = new StringBuilder();
        var ds = s.getDustStorm();
        if (ds != null) {
            sb.append(ds.getDescription());
        }
        sb.append(resources);
        sb.append(TEMPERATURE).append(tString);
        sb.append(WINDSPEED).append(wsString);
        sb.append(ZENITH_ANGLE).append(zaString);
        sb.append(OPTICAL_DEPTH).append(odString);
        return sb.toString();
    }

    /**
     * Puts together and displays the banner string. Heavy reads happen before EDT push.
     *
     * <p><b>Patch:</b> Always refresh the banner text so changes in resourceCache are reflected
     * even when weather caches are unchanged.</p>
     */
    private void displayBanner(Settlement s) {
        updateWeather(s); // refresh caches if needed
        String text = buildBannerText(s);
        SwingUtilities.invokeLater(() -> {
            if (bannerBar != null) {
                bannerBar.setLcdText(text);
            }
        });
    }

    private double getTemperature(Coordinates c) {
        return weather.getTemperature(c);
    }

    private String getTemperatureString(double value) {
        return StyleManager.DECIMAL_CELCIUS.format(value);
    }

    private double getWindSpeed(Coordinates c) {
        return weather.getWindSpeed(c);
    }

    private String getWindSpeedString(double value) {
        return StyleManager.DECIMAL_M_S.format(value);
    }

    private double getOpticalDepth(Coordinates c) {
        return surfaceFeatures.getOpticalDepth(c);
    }

    private String getOpticalDepthString(double value) {
        return StyleManager.DECIMAL_PLACES2.format(value);
    }

    private double getZenithAngle(Coordinates c) {
        return orbitInfo.getSolarZenithAngle(c);
    }

    private String getZenithAngleString(double value) {
        return StyleManager.DECIMAL_DEG.format(value * RADIANS_TO_DEGREES);
    }

    private double getSolarIrradiance(Coordinates c) {
        return surfaceFeatures.getSolarIrradiance(c);
    }

    /**
     * Builds the weather panel
     */
    private void buildWeatherPanel() {

        temperatureIcon = new JLabel();
        windIcon = new JLabel();
        opticalIcon = new JLabel();

        updateIcon(); // safe at init
    }

    /**
     * Updates the weather icon. (Call on EDT)
     */
    private void updateIcon() {
        if (temperatureIcon == null || windIcon == null || opticalIcon == null) return;

        Icon updatedIcon;
        String tooltip = "";
        if (temperatureCache < -40) {
            updatedIcon = ImageLoader.getIconByName("weather/ice");
            tooltip = "Frigid";
        }
        else if (temperatureCache < 0) {
            updatedIcon = ImageLoader.getIconByName("weather/snowflake");
            tooltip = "Freezing";
        }
        else if (temperatureCache < 10) {
            updatedIcon = ImageLoader.getIconByName("weather/cloudy");
            tooltip = "Cool";
        }
        else if (temperatureCache < 22) {
            updatedIcon = ImageLoader.getIconByName("weather/spinningSun");
            tooltip = "Balmy";
        }
        else {
            updatedIcon = ImageLoader.getIconByName("weather/desert_sun");
            tooltip = "Sunny";
        }

        temperatureIcon.setIcon(updatedIcon);
        temperatureIcon.setToolTipText(tooltip);

        ///////////////////////////////////////////////

        if (windSpeedCache > 120) {
            if (opticalDepthCache > 0.7) {
                updatedIcon = ImageLoader.getIconByName("weather/sandstorm");
                tooltip = "Sandstorm";
            }
            else {
                updatedIcon = ImageLoader.getIconByName("weather/highWind");
                tooltip = "High Wind";
            }

        }
        else if (windSpeedCache > 80) {
            updatedIcon = ImageLoader.getIconByName("weather/dust_devil");
            tooltip = "Low Wind";
        }
        else if (windSpeedCache > 40) {
            if (temperatureCache < 0) {
                updatedIcon = ImageLoader.getIconByName("weather/frost_wind");
                tooltip = "Frosty Wind";
            }
            else {
                updatedIcon = ImageLoader.getIconByName("weather/cold_wind");
                tooltip = "Cool Wind";
            }
        }
        else {
            updatedIcon = ImageLoader.getIconByName("weather/lowWind");
            tooltip = "Low Wind";
        }

        windIcon.setIcon(updatedIcon);
        windIcon.setToolTipText(tooltip);

        ///////////////////////////////////////////////

        if (opticalDepthCache > 1.0) {
            updatedIcon = ImageLoader.getIconByName("weather/sand");
            tooltip = "Sandy";
        }
        else if (opticalDepthCache > 0.6) {
            updatedIcon = ImageLoader.getIconByName("weather/hazy");
            tooltip = "Hazy";
        }
        else if (opticalDepthCache > 0.3) {
            updatedIcon = ImageLoader.getIconByName("weather/dry");
            tooltip = "Dry";
        }
        else {
            updatedIcon = ImageLoader.getIconByName("weather/line_of_sight");
            tooltip = "Clear Line of Sight";
        }

        opticalIcon.setIcon(updatedIcon);
        opticalIcon.setToolTipText(tooltip);
    }

    /**
     * Builds the zoom slider with debounced change handling and safe wheel bounds.
     */
    private void buildZoomSlider() {

        zoomSlider = new JSlider(SwingConstants.VERTICAL, 1, 150, 10);
        zoomSlider.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 75));
        zoomSlider.setPreferredSize(new Dimension(50, 350));
        zoomSlider.setSize(new Dimension(50, 350));

        zoomSlider.setMajorTickSpacing(30);
        zoomSlider.setMinorTickSpacing(10);
        zoomSlider.setPaintTicks(true);
        zoomSlider.setPaintLabels(true);

        Dictionary<Integer, JLabel> labelTable = new Hashtable<>();
        labelTable.put(Integer.valueOf(150), new JLabel("150"));
        labelTable.put(Integer.valueOf(120), new JLabel("120"));
        labelTable.put(Integer.valueOf(90), new JLabel("90"));
        labelTable.put(Integer.valueOf(60), new JLabel("60"));
        labelTable.put(Integer.valueOf(30), new JLabel("30"));
        labelTable.put(Integer.valueOf(20), new JLabel("20"));
        labelTable.put(Integer.valueOf(10), new JLabel("10"));
        labelTable.put(Integer.valueOf(1), new JLabel("1"));
        zoomSlider.setLabelTable(labelTable);

        zoomSlider.setToolTipText(Msg.getString("SettlementTransparentPanel.tooltip.zoom")); //$NON-NLS-1$

        initDebounce(zoomSlider);

        // Add mouse wheel listener for zooming.
        mouseWheelListener = new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent evt) {

                int numClicks = evt.getWheelRotation();
                int value = zoomSlider.getValue();
                int min = zoomSlider.getMinimum();
                int max = zoomSlider.getMaximum();

                if (numClicks > 0) {
                    // wheel down -> zoom out
                    if (value > min) {
                        zoomSlider.setValue(value - 1);
                    }
                }
                else if (numClicks < 0) {
                    // wheel up -> zoom in
                    if (value < max) {
                        zoomSlider.setValue(value + 1);
                    }
                }

                evt.consume();
            }
        };

        mapPanel.addMouseWheelListener(mouseWheelListener);
    }

    /**
     * Sets up the zoom slider with a timer to debounce any redundant re-rendering work.
     */
    private void initDebounce(JSlider slider) {
        zoomListener = e -> {
            int value = ((JSlider) e.getSource()).getValue();
            if (zoomDebounce == null) {
                zoomDebounce = new javax.swing.Timer(75, ae -> convertTo(value));
                zoomDebounce.setRepeats(false);
                zoomDebounce.start();
            }
            else if (zoomDebounce.isRunning()) {
                zoomDebounce.restart();
            }
            else {
                zoomDebounce = null;
                zoomDebounce = new javax.swing.Timer(75, ae -> convertTo(value));
                zoomDebounce.setRepeats(false);
                zoomDebounce.start();
            }
        };
        slider.addChangeListener(zoomListener);
    }

    /**
     * Converts the zoom value found on the slider to a map scale.
     */
    private void convertTo(int value) {
        // Slider min is 1; mapPanel clamps & quantizes internally.
        mapPanel.setScale(value);
    }

    /**
     * Ensure we stop timers and detach listeners when removed from a container.
     * (Fixed: avoid calling super.removeNotify() twice.)
     */
    @Override
    public void removeNotify() {
        try {
            destroy();
        } finally {
            super.removeNotify();
        }
    }

    /**
     * Recursively removes common listeners and clears maps on any Component tree.
     * Safe to call with null; idempotent.
     */
    private static void removeAllListenersRecursively(java.awt.Component c) {
        if (c == null) return;

        // Detach AWT listeners
        for (java.awt.event.MouseListener l : c.getMouseListeners()) c.removeMouseListener(l);
        for (java.awt.event.MouseMotionListener l : c.getMouseMotionListeners()) c.removeMouseMotionListener(l);
        for (java.awt.event.MouseWheelListener l : c.getMouseWheelListeners()) c.removeMouseWheelListener(l);
        for (java.awt.event.KeyListener l : c.getKeyListeners()) c.removeKeyListener(l);
        for (java.awt.event.FocusListener l : c.getFocusListeners()) c.removeFocusListener(l);
        for (java.beans.PropertyChangeListener l : c.getPropertyChangeListeners()) c.removePropertyChangeListener(l);

        // Detach Swing listeners and clear action/input maps per child as well
        if (c instanceof javax.swing.JComponent jc) {
            for (javax.swing.event.AncestorListener l : jc.getAncestorListeners()) jc.removeAncestorListener(l);

            javax.swing.ActionMap am = jc.getActionMap();
            if (am != null) am.clear();
            for (int cond : new int[] {
                    javax.swing.JComponent.WHEN_FOCUSED,
                    javax.swing.JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT,
                    javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW}) {
                javax.swing.InputMap im = jc.getInputMap(cond);
                if (im != null) im.clear();
            }

            jc.setTransferHandler(null);
            jc.setToolTipText(null);
            jc.setCursor(null);

            // Best-effort detach common model listeners when possible
            if (jc instanceof javax.swing.JSlider slider) {
                for (javax.swing.event.ChangeListener l : slider.getChangeListeners()) {
                    slider.removeChangeListener(l);
                }
            }
            if (jc instanceof javax.swing.AbstractButton btn) {
                for (java.awt.event.ActionListener l : btn.getActionListeners()) {
                    btn.removeActionListener(l);
                }
                for (java.awt.event.ItemListener l : btn.getItemListeners()) {
                    btn.removeItemListener(l);
                }
            }
            if (jc instanceof javax.swing.JComboBox<?> combo) {
                for (java.awt.event.ItemListener l : combo.getItemListeners()) {
                    combo.removeItemListener(l);
                }
                for (java.awt.event.ActionListener l : combo.getActionListeners()) {
                    combo.removeActionListener(l);
                }
            }
            if (jc instanceof javax.swing.JTable table) {
                for (java.beans.PropertyChangeListener l : table.getPropertyChangeListeners()) {
                    table.removePropertyChangeListener(l);
                }
            }
        }

        // Recurse
        if (c instanceof java.awt.Container container) {
            for (java.awt.Component child : container.getComponents()) {
                removeAllListenersRecursively(child);
            }
        }
    }

    /**
     * Sets the zoom slider value. Avoids redundant change events.
     */
    public void setZoomValue(int value) {
        if (zoomSlider != null && zoomSlider.getValue() != value) {
            if (zoomDebounce != null && zoomDebounce.isRunning()) {
                zoomDebounce.stop(); // prevent a stale convertTo firing
            }
            zoomSlider.setValue(value);
        }
    }

    private void buildInfoP() {

        Icon icon =  ImageLoader.getIconByName ("settlement_map/info");
        infoButton = new JButton(icon);

        infoButton.setPreferredSize(new Dimension(32, 32));
        infoButton.setOpaque(false);
        infoButton.setBackground(new Color(0,0,0,128));
        infoButton.setContentAreaFilled(false);
        infoButton.setBorderPainted(false);
        infoButton.addActionListener(e -> {
                Settlement settlement = mapPanel.getSettlement();
                if (settlement != null) {
                    desktop.showDetails(settlement);
                }
            });
    }

    private void buildRenameBtn() {

        Icon icon = ImageLoader.getIconByName("settlement_map/edit");
        renameBtn = new JButton(icon);
        renameBtn.setPreferredSize(new Dimension(32, 32));
        renameBtn.setOpaque(false);
        renameBtn.setBackground(new Color(0,0,0,128));
        renameBtn.setContentAreaFilled(false);
        renameBtn.setBorderPainted(false);

        renameBtn.addActionListener(e -> openRenameDialog());
    }

    private JPanel buildButtonPane() {

        var buttonPane = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        buttonPane.setPreferredSize(new Dimension(150, 36));
        buttonPane.setBackground(new Color(0,0,0,128));
        buttonPane.setOpaque(false);

        // Create rotate clockwise button.
        final Icon cwIcon = ImageLoader.getIconByName("settlement_map/right");
        JButton cwButton = new JButton(cwIcon);
        cwButton.setPreferredSize(new Dimension(32, 32));
        cwButton.setOpaque(false);
        cwButton.setBorderPainted(false);
        cwButton.setContentAreaFilled(false);
        cwButton.setBackground(new Color(0,0,0,128));

        cwButton.setToolTipText(Msg.getString("SettlementTransparentPanel.tooltip.clockwise")); //$NON-NLS-1$
        cwButton.addActionListener(e ->
                mapPanel.setRotation(mapPanel.getRotation() + ROTATION_CHANGE));

        // Create center button.
        final Icon centerIcon = ImageLoader.getIconByName("settlement_map/center");
        JButton recenterButton = new JButton(centerIcon);
        recenterButton.setPreferredSize(new Dimension(32, 32));
        recenterButton.setOpaque(false);
        recenterButton.setBorderPainted(false);
        recenterButton.setContentAreaFilled(false);
        recenterButton.setBackground(new Color(0,0,0,128));

        recenterButton.setToolTipText(Msg.getString("SettlementTransparentPanel.tooltip.recenter")); //$NON-NLS-1$
        recenterButton.addActionListener(e -> mapPanel.reCenter());

        // Create rotate counter-clockwise button.
        final Icon ccwIcon = ImageLoader.getIconByName("settlement_map/left");
        JButton ccwButton = new JButton(ccwIcon);
        ccwButton.setPreferredSize(new Dimension(32, 32));
        ccwButton.setOpaque(false);
        ccwButton.setBorderPainted(false);
        ccwButton.setContentAreaFilled(false);
        ccwButton.setBackground(new Color(0,0,0,128));

        ccwButton.setToolTipText(Msg.getString("SettlementTransparentPanel.tooltip.counterClockwise")); //$NON-NLS-1$
        ccwButton.addActionListener(e ->
                mapPanel.setRotation(mapPanel.getRotation() - ROTATION_CHANGE));

        buttonPane.add(ccwButton);
        buttonPane.add(recenterButton);
        buttonPane.add(cwButton);
        buttonPane.add(emptyLabel);

        return buttonPane;
    }

    private JPanel buildLabelPane() {
        var labelPane = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        labelPane.setPreferredSize(new Dimension(150, 36));
        labelPane.setBackground(new Color(0,0,0,128));
        labelPane.setOpaque(false);

        final Icon labelsIcon = ImageLoader.getIconByName("settlement_map/stack");
        JButton labelsButton = new JButton(
                Msg.getString("SettlementTransparentPanel.button.labels"), labelsIcon);  //$NON-NLS-1$
        labelsButton.setFont(new Font("Dialog", Font.BOLD, 13));
        labelsButton.setForeground(Color.ORANGE.darker().darker());
        labelsButton.setPreferredSize(new Dimension(32, 32));
        labelsButton.setVerticalAlignment(SwingConstants.CENTER);
        labelsButton.setHorizontalAlignment(SwingConstants.CENTER);

        labelsButton.setOpaque(false);
        labelsButton.setBackground(new Color(0,0,0,128));
        labelsButton.setContentAreaFilled(false); //more artifact when enabled
        labelsButton.setBorderPainted(false);

        labelsButton.setToolTipText(Msg.getString("SettlementTransparentPanel.tooltip.labels")); //$NON-NLS-1$
        labelsButton.addActionListener(e -> {
            JButton button = (JButton) e.getSource();
            if (labelsMenu == null) {
                labelsMenu = createLabelsMenu();
            }
            labelsMenu.show(button, 0, button.getHeight());
        });

        labelPane.add(renameBtn);
        labelPane.add(infoButton);
        labelPane.add(labelsButton);

        labelPane.add(emptyLabel);

        return labelPane;
    }

    /**
     * Clears the labels menu.
     */
    private void clearLabelsMenu() {
        labelsMenu = null;
    }

    private JCheckBoxMenuItem createDisplayToggle(String label, SettlementMapPanel.DisplayOption op) {
        JCheckBoxMenuItem result = new JCheckBoxMenuItem(label, mapPanel.isOptionDisplayed(op));
        result.setContentAreaFilled(false);
        result.addActionListener(e -> mapPanel.toggleDisplayOption(op));
        result.setSelected(mapPanel.isOptionDisplayed(op));
        return result;
    }

    /**
     * Creates the labels popup menu.
     *
     * @return popup menu.
     */
    private JPopupMenu createLabelsMenu() {
        JPopupMenu popMenu = new JPopupMenu(Msg.getString("SettlementWindow.menu.labelOptions")); //$NON-NLS-1$
        popMenu.setBorderPainted(false);

        // Activity spot menu
        var spotLabelMenuItem = new JMenu("Activity Spots");
        popMenu.add(spotLabelMenuItem);

        BuildingConfig bc = getConfig();
        List<FunctionType> sortedFT = new ArrayList<>(bc.getActivitySpotFunctions());
        Collections.sort(sortedFT);

        // Add an All
        var allItem = new JMenuItem("All"); //$NON-NLS-1$
        allItem.setContentAreaFilled(false);
        allItem.addActionListener(e -> {
                mapPanel.reverseSpotLabels(bc.getActivitySpotFunctions());
                clearLabelsMenu(); // Clear the menu because all the values will change
        });
        spotLabelMenuItem.add(allItem);

        // Add an None
        var noneItem = new JMenuItem("None"); //$NON-NLS-1$
        noneItem.setContentAreaFilled(false);
        noneItem.addActionListener(e -> {
                mapPanel.clearSpotLabels();
                clearLabelsMenu(); // Clear the menu because all the values will change
        });
        spotLabelMenuItem.add(noneItem);

        // Add one per function type
        for (FunctionType ft : sortedFT) {
            var ftItem = new JCheckBoxMenuItem(ft.getName(), mapPanel.isShowSpotLabels(ft)); //$NON-NLS-1$
            ftItem.setContentAreaFilled(false);
            ftItem.addActionListener(e ->
                    mapPanel.setShowSpotLabels(ft, !mapPanel.isShowSpotLabels(ft)));
            spotLabelMenuItem.add(ftItem);
        }

        // Create display option items
        for (DisplayOption op : DisplayOption.values()) {
            popMenu.add(createDisplayToggle(Msg.getString("SettlementWindow.menu." + op.name().toLowerCase()),
                op));
        }

        popMenu.pack();

        return popMenu;
    }

    private BuildingConfig getConfig() {
        return BuildingManager.getBuildingConfig();
    }

    /**
     * Open dialog box to take in the new settlement name
     */
    private void openRenameDialog() {

        String oldName = mapPanel.getSettlement().getName();

        JDialog.setDefaultLookAndFeelDecorated(true);
        String newName = askNameDialog();
        if (!oldName.equals(newName)
                && newName != null
                && !newName.trim().equals("")
                && newName.trim().length() != 0) {
            mapPanel.getSettlement().setName(newName.trim());

            desktop.closeToolWindow(SettlementWindow.NAME);
            desktop.openToolWindow(SettlementWindow.NAME);

        }
    }

    /**
     * Ask for a new Settlement name
     * @return pop up jDialog
     */
    private String askNameDialog() {
        return JOptionPane
            .showInputDialog(desktop,
                    Msg.getString("SettlementWindow.JDialog.changeSettlementName.input"), //$NON-NLS-1$
                    Msg.getString("SettlementWindow.JDialog.changeSettlementName.title"), //$NON-NLS-1$
                    JOptionPane.QUESTION_MESSAGE);
    }

    /**
     * Inner class combo box model for settlements.
     */
    private class SettlementComboBoxModel extends DefaultComboBoxModel<Settlement>
        implements UnitManagerListener {

        /**
         * Constructor.
         */
        public SettlementComboBoxModel() {
            // User DefaultComboBoxModel constructor.
            super();
            // Initialize settlement list.
            updateSettlements();
            // Add this as a unit manager listener.
            unitManager.addUnitManagerListener(UnitType.SETTLEMENT, this);
        }

        /**
         * Updates a list of settlements.
         */
        private void updateSettlements() {

            List<Settlement> settlements;

            // Add the command dashboard button
            if (mode == GameMode.COMMAND) {
                settlements = unitManager.getCommanderSettlements();
            }

            else {
                settlements = new ArrayList<>();
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
                Settlement newSettlement = (Settlement) event.getUnit();

                // Find the best place
                for(int i = 0; i < getSize(); i++) {
                    var existing = getElementAt(i);
                    if (existing.getName().compareTo(newSettlement.getName()) > 0) {
                        insertElementAt(newSettlement, i);
                        return;
                    }
                }

                // Add at the end
                addElement(newSettlement);
            }
        }

        /**
         * Prepare class for deletion.
         */
        public void destroy() {
            unitManager.removeUnitManagerListener(UnitType.SETTLEMENT, this);
        }
    }

    public JComboBox<Settlement> getSettlementListBox() {
        return settlementListBox;
    }

    /**
     * Gets the sunlight data and display it on the top left panel of the settlement map.
     * (UI label updates are marshaled to EDT)
     */
    private void displaySunData(Coordinates location) {
        double [] time = orbitInfo.getSunTimes(mapPanel.getSettlement().getCoordinates());

        // Heavy-ish compute first
        weather.calculateSunRecord(location);
        SunData data = weather.getSunRecord(location);

        // Prepare values
        final String projRise = PROJECTED_SUNRISE + StyleManager.DECIMAL_MSOL.format(time[0]);
        final String projSet  = PROJECTED_SUNSET  + StyleManager.DECIMAL_MSOL.format(time[1]);
        final String projDay  = PROJECTED_DAYLIGHT+ StyleManager.DECIMAL_MSOL.format(time[2]);

        // Push UI writes to EDT
        SwingUtilities.invokeLater(() -> {
            if (projectSunriseLabel != null) projectSunriseLabel.setText(projRise);
            if (projectSunsetLabel  != null) projectSunsetLabel.setText(projSet);
            if (projectDaylightLabel!= null) projectDaylightLabel.setText(projDay);

            if (data == null) {
                logger.warning(0, "Yestersol sunlight data unavailable at " + location + ".");
                return;
            }

            if (sunriseLabel != null) sunriseLabel.setText(SUNRISE + data.getSunrise() + MSOL);
            if (sunsetLabel  != null) sunsetLabel.setText(SUNSET  + data.getSunset()  + MSOL);
            if (daylightLabel!= null) daylightLabel.setText(DAYLIGHT + data.getDaylight() + MSOL);
            if (zenithLabel  != null) zenithLabel.setText(ZENITH + data.getZenith() + MSOL);
            if (maxSunLabel  != null) maxSunLabel.setText(MAX_LIGHT + data.getMaxSun() + WM);
        });
    }

    /**
     * Prepares the resource data string for the new sol.
     */
    private void prepBannerResourceString(ClockPulse pulse) {

        int sol = pulse.getMarsTime().getMissionSol();
        if (sol > 1) {
            Collection<Settlement> list = unitManager.getSettlements();
            for (Settlement s0: list) {
                prepareResourceStat(s0, sol);
            }
        }
    }

    public void update(ClockPulse pulse) {
        int sol = pulse.getMarsTime().getMissionSol();

        if (solCache != sol) {
            solCache = sol;
            // Redo the resource string once a sol (off-EDT; only updates cache)
            prepBannerResourceString(pulse);
            // Update the sun data
            Settlement s0 = (settlementListBox != null) ? (Settlement) settlementListBox.getSelectedItem() : null;
            if (s0 != null)
                displaySunData(s0.getCoordinates()); // EDT marshaled inside
        }

        if (bannerBar != null && settlementListBox != null) {
            Settlement s = (Settlement) settlementListBox.getSelectedItem();
            // When loading from a saved sim, s may be initially null
            if (s == null)
                return;

            // Update weather-derived caches and banner text, then UI on EDT
            displayBanner(s);

            // Update icons on EDT
            SwingUtilities.invokeLater(this::updateIcon);

            // Update current sunlight on EDT
            updateCurrentSunlight(s);
        }
    }

    /**
     * Updates the current sunlight label safely on EDT.
     */
    private void updateCurrentSunlight(Settlement s) {
        if (currentSunLabel == null) return;

        int irr = (int) getSolarIrradiance(s.getCoordinates());
        SwingUtilities.invokeLater(() -> {
            if (currentSunLabel != null) {
                currentSunLabel.setText(CURRENT_LIGHT + irr + WM);
            }
        });
    }

    /**
     * Prepares for the critical resource statistics String
     */
    private void prepareResourceStat(Settlement s, int missionSol) {
        StringBuilder text = new StringBuilder(YESTERSOL_RESOURCE);
        Map<Integer, Double> yestersolResources = s.gatherResourceStat(missionSol - 1);
        int size = yestersolResources.size();
        int i = 0;
        for (Entry<Integer, Double> id: yestersolResources.entrySet()) {
            String resource = ResourceUtil.findAmountResourceName(id.getKey());
            double amount = id.getValue();
            text.append(amount).append(" kg ").append(resource);
            i++;
            if (i == size - 1) {
                text.append(")  ");
            }
            else {
                text.append(",  ");
            }
        }

        if (yestersolResources.isEmpty())
            return;

        resourceCache.put(s, text.toString());
    }

    /**
     * Prepare class for deletion (idempotent, EDT-safe).
     */
    public void destroy() {

        // Ensure Swing teardown happens on the EDT
        if (!javax.swing.SwingUtilities.isEventDispatchThread()) {
            javax.swing.SwingUtilities.invokeLater(this::destroy);
            return;
        }

        // --- Stop timers and detach explicit listeners we track ---
        if (zoomDebounce != null) {
            zoomDebounce.stop();
            zoomDebounce = null;
        }
        if (zoomSlider != null && zoomListener != null) {
            zoomSlider.removeChangeListener(zoomListener);
        }
        zoomListener = null;

        // Defensive unregistration from both this panel and the mapPanel (safe no-ops if not present)
        if (mouseWheelListener != null) {
            removeMouseWheelListener(mouseWheelListener); // in case it was also attached to us
            if (mapPanel != null) {
                mapPanel.removeMouseWheelListener(mouseWheelListener);
            }
        }
        mouseWheelListener = null;

        // --- Best-effort: remove action/item listeners on owned controls ---
        if (renameBtn != null) {
            for (java.awt.event.ActionListener l : renameBtn.getActionListeners()) {
                renameBtn.removeActionListener(l);
            }
        }
        if (infoButton != null) {
            for (java.awt.event.ActionListener l : infoButton.getActionListeners()) {
                infoButton.removeActionListener(l);
            }
        }
        if (settlementListBox != null) {
            for (java.awt.event.ItemListener l : settlementListBox.getItemListeners()) {
                settlementListBox.removeItemListener(l);
            }
            for (java.awt.event.ActionListener l : settlementListBox.getActionListeners()) {
                settlementListBox.removeActionListener(l);
            }
        }

        // --- Clear and hide popup menu if present ---
        if (labelsMenu != null) {
            labelsMenu.setVisible(false);
            labelsMenu.removeAll();
            labelsMenu = null;
        }

        // --- Recursively detach listeners / clear maps for all owned components ---
        removeAllListenersRecursively(this);                // harmless if not in component tree
        removeAllListenersRecursively(zoomSlider);
        removeAllListenersRecursively(bannerBar);
        removeAllListenersRecursively(temperatureIcon);
        removeAllListenersRecursively(windIcon);
        removeAllListenersRecursively(opticalIcon);
        removeAllListenersRecursively(renameBtn);
        removeAllListenersRecursively(infoButton);
        removeAllListenersRecursively(settlementListBox);
        removeAllListenersRecursively(projectSunriseLabel);
        removeAllListenersRecursively(projectSunsetLabel);
        removeAllListenersRecursively(projectDaylightLabel);
        removeAllListenersRecursively(sunriseLabel);
        removeAllListenersRecursively(sunsetLabel);
        removeAllListenersRecursively(zenithLabel);
        removeAllListenersRecursively(maxSunLabel);
        removeAllListenersRecursively(daylightLabel);
        removeAllListenersRecursively(currentSunLabel);

        // Remove any children this component might have (defensive)
        try {
            removeAll();
            revalidate();
            repaint();
        } catch (Throwable ignore) {
            // best-effort cleanup; ignore if already detached
        }

        if (settlementCBModel != null) settlementCBModel.destroy();
        settlementListBox = null;
        settlementCBModel = null;

        if (resourceCache != null) {
            resourceCache.clear();
            resourceCache = null;
        }

        mode = null;

        emptyLabel = null;
        bannerBar = null;
        zoomSlider = null;

        projectSunriseLabel = null;
        projectSunsetLabel = null;
        projectDaylightLabel = null;
        sunriseLabel = null;
        sunsetLabel = null;
        zenithLabel = null;
        maxSunLabel = null;
        daylightLabel = null;
        currentSunLabel = null;

        renameBtn = null;
        infoButton = null;
        temperatureIcon = null;
        windIcon = null;
        opticalIcon = null;

        labelsMenu = null;

        desktop = null;

        weather = null;
        surfaceFeatures = null;
        orbitInfo = null;
        unitManager = null;

        mapPanel = null;
    }
}

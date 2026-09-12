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
import java.awt.event.ItemEvent;
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

import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;

import com.mars_sim.core.Entity;
import com.mars_sim.core.EntityManagerListener;
import com.mars_sim.core.GameManager;
import com.mars_sim.core.GameManager.GameMode;
import com.mars_sim.core.Simulation;
import com.mars_sim.core.UnitManager;
import com.mars_sim.core.UnitType;
import com.mars_sim.core.environment.OrbitInfo;
import com.mars_sim.core.environment.SurfaceFeatures;
import com.mars_sim.core.environment.Weather;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.time.MasterClock;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.utils.NamedListCellRenderer;

import eu.hansolo.steelseries.gauges.DisplaySingle;
import eu.hansolo.steelseries.tools.LcdColor;

@SuppressWarnings({ "serial" })

public class SettlementTransparentPanel extends JComponent {

    /** Rotation change (radians per rotation button press). */
    private static final double ROTATION_CHANGE = Math.PI / 20D;
    private static final double RADIANS_TO_DEGREES = 180D / Math.PI;

    /** Zoom change. */
    public static final double ZOOM_CHANGE = 0.25;

    private static final String TEMPERATURE     = "   Temperature: ";
    private static final String WINDSPEED       = "   Windspeed: ";
    private static final String ZENITH_ANGLE    = "   Zenith Angle: ";
    private static final String OPTICAL_DEPTH   = "   Optical Depth: ";

    private static final String YESTERSOL_RESOURCE = "Yestersol's Resources (";

    private int solCache = 0;

    private double temperatureCache;
    private double opticalDepthCache;
    private double windSpeedCache;
    private double zenithAngleCache;

    private String tString;
    private String wsString;
    private String zaString;
    private String odString;

    private String resourceCache = "";

    private GameMode mode;

    private DisplaySingle bannerBar;
    private JSlider zoomSlider;
    
    private JLabel emptyLabel;

    private JButton infoButton;

    /** Settlement Combo box */
    private JComboBox<Settlement> settlementListBox;

    private SettlementMapPanel mapPanel;
    private WeatherPanel weatherPanel;
    private UIContext context;

    private Weather weather;
    private SurfaceFeatures surfaceFeatures;
    private OrbitInfo orbitInfo;
    private UnitManager unitManager;
    private MasterClock masterClock;
    private boolean weatherVisible;
    private boolean bannerVisible;

    /**
     * The panel with elements that are on top of the settlement map.
     *
     * @param desktop
     * @param mapPanel
     * @param weatherVisible whether the weather panel should be visible.
     * @param bannerVisible whether the banner bar should be visible.
     */
    public SettlementTransparentPanel(UIContext context, SettlementMapPanel mapPanel, 
        boolean bannerVisible, boolean weatherVisible) {
        this.mapPanel = mapPanel;
        this.context = context;

        Simulation sim = context.getSimulation();
        this.unitManager = sim.getUnitManager();
        this.masterClock = sim.getMasterClock();

        this.weather = sim.getWeather();
        this.surfaceFeatures = sim.getSurfaceFeatures();
        this.orbitInfo = sim.getOrbitInfo();
        this.bannerVisible = bannerVisible;
        this.weatherVisible = weatherVisible;

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
        var labelPane = buildLabelPane();
        var buttonPane = buildButtonPane();
        buildSettlementNameComboBox();
        buildZoomSlider();
        buildBanner();
        bannerBar.setVisible(bannerVisible);
        weatherPanel = new WeatherPanel(mapPanel, weather, surfaceFeatures, orbitInfo, masterClock);
        weatherPanel.setVisible(weatherVisible);
        
        JPanel topPane = new JPanel(new BorderLayout(20, 20));
        topPane.setBackground(new Color(0,0,0,128));
        topPane.setOpaque(false);

        JPanel settlementPanel = new JPanel();
        settlementPanel.setBackground(new Color(0,0,0,128));
        settlementPanel.setOpaque(false);
        settlementPanel.add(settlementListBox, BorderLayout.NORTH);

        mapPanel.add(topPane, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout(2, 2));
        centerPanel.setBackground(new Color(0,0,0,128));
        centerPanel.setOpaque(false);

        centerPanel.add(weatherPanel, BorderLayout.WEST);
        centerPanel.add(settlementPanel, BorderLayout.NORTH);

        topPane.add(centerPanel, BorderLayout.CENTER);
        topPane.add(bannerBar, BorderLayout.NORTH);

        var controlPane = new JPanel(new BorderLayout());
        controlPane.setBackground(new Color(0,0,0,128));
        controlPane.setOpaque(false);

        var zoomPane = new JPanel(new FlowLayout(FlowLayout.CENTER));
        zoomPane.setBackground(new Color(0,0,0,128));
        zoomPane.setOpaque(false);
        zoomPane.add(zoomSlider);
        zoomPane.setAlignmentY(CENTER_ALIGNMENT);

        controlPane.add(buttonPane, BorderLayout.NORTH);
        controlPane.add(labelPane, BorderLayout.SOUTH);
        controlPane.add(zoomPane, BorderLayout.CENTER);

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
     * Gets the length of the most lengthy settlement name.
     */
    private int getNameLength() {
        Collection<Settlement> list = unitManager.getSettlements();
        int min = 10;
        for (Settlement s: list) {
            int size = s.getName().length();
            if (min < size) {
                min = size;
            }
        }
        return min;
    }

    /**
     * Builds the settlement name combo box.
     */
    private void buildSettlementNameComboBox() {

        var settlementCBModel = new SettlementComboBoxModel();
        settlementListBox = new JComboBox<>(settlementCBModel);
        settlementListBox.setPreferredSize(new Dimension(getNameLength() * 9, 30));
        settlementListBox.setToolTipText(Msg.getString("SettlementWindow.tooltip.selectSettlement")); //$NON-NLS-1$
        settlementListBox.setRenderer(new NamedListCellRenderer());
        settlementListBox.addItemListener(event -> {
            if (event.getStateChange() == ItemEvent.SELECTED) {
                Settlement s = (Settlement) event.getItem();
                if (s != null) {
                    // Change to the selected settlement in SettlementMapPanel
                    changeSettlement(s);
                    // Update weather/sunlight pane
                    if (weatherPanel != null) {
                        weatherPanel.update(s);
                    }
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
        resourceCache = "";

        // Set the selected settlement in SettlementMapPanel
        mapPanel.setSettlement(s);
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
        StringBuilder sb = new StringBuilder();
        var ds = s.getDustStorm();
        if (ds != null) {
            sb.append(ds.getDescription());
        }
        sb.append(TEMPERATURE).append(tString);
        sb.append(WINDSPEED).append(wsString);
        sb.append(ZENITH_ANGLE).append(zaString);
        sb.append(OPTICAL_DEPTH).append(odString);
        sb.append(resourceCache);
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
        return StyleManager.DECIMAL_PLACES1.format(value);
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
        return StyleManager.DECIMAL_PLACES0.format(value * RADIANS_TO_DEGREES);
    }

    /**
     * Builds the zoom slider with debounced change handling and safe wheel bounds.
     */
    private void buildZoomSlider() {

        zoomSlider = new JSlider(SwingConstants.VERTICAL, 1, 150, 10);
        zoomSlider.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 75));
        zoomSlider.setPreferredSize(new Dimension(50, 400));
        zoomSlider.setSize(new Dimension(50, 400));

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

        // Prepare mouse wheel listener for zooming (install idempotently elsewhere).
        MouseWheelListener mouseWheelListener = evt -> {

            int numClicks = evt.getWheelRotation();
            int value = zoomSlider.getValue();
            int min = zoomSlider.getMinimum();
            int max = zoomSlider.getMaximum();

            if (numClicks > 0) {
                // wheel down -> zoom out
                if (value - 1 >= min) {
                    zoomSlider.setValue(value - 1);
                }
            }
            else if (numClicks < 0) {
                // wheel up -> zoom in
                if (value + 1 <= max) {
                    zoomSlider.setValue(value + 1);
                }
            }

            evt.consume();
        };

        mapPanel.addMouseWheelListener(mouseWheelListener);
    }

    boolean isBannerBarVisible() {
        return bannerBar.isVisible();
    }

    boolean isWeatherPanelVisible() {
        return weatherPanel.isVisible();
    }

    /**
     * Sets up the zoom slider with a timer to debounce any redundant re-rendering work.
     */
    private void initDebounce(JSlider slider) {
        ChangeListener zoomListener = e -> {
            int value = ((JSlider) e.getSource()).getValue();
            mapPanel.setScale(value);

        };
        slider.addChangeListener(zoomListener);
    }

    /**
     * Sets the zoom slider value. Avoids redundant change events.
     */
    public void setZoomValue(int value) {
        zoomSlider.setValue(value);
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
                    context.showDetails(settlement);
                }
            });
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
            var labelsMenu = createLabelsMenu();
            labelsMenu.show(button, 0, button.getHeight());
        });

        labelPane.add(infoButton);
        labelPane.add(labelsButton);

        labelPane.add(emptyLabel);

        return labelPane;
    }


    /**
     * Creates the labels popup menu.
     *
     * @return popup menu.
     */
    private JPopupMenu createLabelsMenu() {
        JPopupMenu popMenu = new JPopupMenu(Msg.getString("SettlementWindow.menu.labelOptions")); //$NON-NLS-1$
        popMenu.setBorderPainted(false);
        
        var bannerItem = new JCheckBoxMenuItem("Display Banner", bannerBar.isVisible());
        bannerItem.setContentAreaFilled(false);
        bannerItem.addActionListener(e -> {
            bannerVisible = bannerItem.isSelected();
            bannerBar.setVisible(bannerVisible);
        });
        popMenu.add(bannerItem);

        var weatherItem = new JCheckBoxMenuItem("Display Weather", weatherPanel.isVisible());
        weatherItem.setContentAreaFilled(false);
        weatherItem.addActionListener(e -> {
            weatherVisible = weatherItem.isSelected();
            weatherPanel.setVisible(weatherVisible);
        });
        popMenu.add(weatherItem);

        popMenu.addSeparator();

        // Create display option items
        for (var layer : mapPanel.getMapLayers()) {
            var items = layer.getFilterControls();
            items.forEach(popMenu::add);
        }

        popMenu.pack();

        return popMenu;
    }

    /**
     * Inner class combo box model for settlements.
     */
    private class SettlementComboBoxModel extends DefaultComboBoxModel<Settlement>
        implements EntityManagerListener {

        /**
         * Constructor.
         */
        public SettlementComboBoxModel() {
            // User DefaultComboBoxModel constructor.
            super();
            // Initialize settlement list.
            updateSettlements();
            // Add this as an entity manager listener.
            unitManager.addEntityManagerListener(UnitType.SETTLEMENT, this);
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
        public void entityAdded(Entity newEntity) {
            if (newEntity instanceof Settlement newSettlement) {

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

        @Override
        public void entityRemoved(Entity removedEntity) {
            if (removedEntity instanceof Settlement removedSettlement) {
                removeElement(removedSettlement);
            }
        }

        /**
         * Prepare class for deletion.
         */
        public void destroy() {
            unitManager.removeEntityManagerListener(UnitType.SETTLEMENT, this);
        }
    }

    public JComboBox<Settlement> getSettlementListBox() {
        return settlementListBox;
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

    /**
     * Updates with a new clock pulse.
     * 
     * @param pulse
     */
    void update(ClockPulse pulse) {
        int sol = pulse.getMarsTime().getMissionSol();

        if (pulse.isNewHalfSol()
        	|| solCache != sol) {
            solCache = sol;
            // Redo the resource string once a sol (off-EDT; only updates cache)
            prepBannerResourceString(pulse);
        }
        
        Settlement s = mapPanel.getSettlement(); 
        // When loading from a saved sim, s may be initially null
        if (s == null)
            return;
        
        if (bannerBar != null && settlementListBox != null) {
 
            // Update weather-derived caches and banner text, then UI on EDT
            displayBanner(s);
        }
        
        if (weatherPanel.isVisible()) {
            // Update weather/sunlight pane
            weatherPanel.update(s);
        }
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

        resourceCache = text.toString();
    }
}

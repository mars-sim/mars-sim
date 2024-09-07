/*
 * Mars Simulation Project
 * NavigatorWindow.java
 * @date 2023-07-03
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.tool.navigator;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.MemoryImageSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.Painter;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIDefaults;
import javax.swing.UIManager;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.extras.components.FlatToggleButton;
import com.mars_sim.core.GameManager;
import com.mars_sim.core.GameManager.GameMode;
import com.mars_sim.core.Simulation;
import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.Unit;
import com.mars_sim.core.UnitManager;
import com.mars_sim.core.UnitManagerEventType;
import com.mars_sim.core.UnitManagerListener;
import com.mars_sim.core.UnitType;
import com.mars_sim.core.environment.Landmark;
import com.mars_sim.core.environment.TerrainElevation;
import com.mars_sim.core.map.IntegerMapData;
import com.mars_sim.core.map.Map;
import com.mars_sim.core.map.MapDataFactory;
import com.mars_sim.core.map.MapDataUtil;
import com.mars_sim.core.map.MapLayer;
import com.mars_sim.core.map.MapMetaData;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.ui.swing.ConfigurableWindow;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.JComboBoxMW;
import com.mars_sim.ui.swing.MainDesktopPane;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.tool.JStatusBar;
import com.mars_sim.ui.swing.tool.map.ExploredSiteMapLayer;
import com.mars_sim.ui.swing.tool.map.LandmarkMapLayer;
import com.mars_sim.ui.swing.tool.map.MapPanel;
import com.mars_sim.ui.swing.tool.map.MineralMapLayer;
import com.mars_sim.ui.swing.tool.map.NavpointMapLayer;
import com.mars_sim.ui.swing.tool.map.ShadingMapLayer;
import com.mars_sim.ui.swing.tool.map.UnitIconMapLayer;
import com.mars_sim.ui.swing.tool.map.UnitLabelMapLayer;
import com.mars_sim.ui.swing.tool.map.VehicleTrailMapLayer;
import com.mars_sim.ui.swing.tool_window.ToolWindow;
import com.mars_sim.ui.swing.unit_display_info.UnitDisplayInfo;
import com.mars_sim.ui.swing.unit_display_info.UnitDisplayInfoFactory;

/**
 * The NavigatorWindow is a tool window that displays a map and a globe showing
 * Mars, and various other elements. It is the primary interface component that
 * presents the simulation to the user.
 */
@SuppressWarnings("serial")
public class NavigatorWindow extends ToolWindow implements ActionListener, ConfigurableWindow, MouseWheelListener {

	private static class MapOrder {
		int order;
		MapLayer layer;

		public MapOrder(int order, MapLayer layer) {
			this.order = order;
			this.layer = layer;
		}
		
		public int getOrder() {
			return order;
		}
	}
	
	private static final Logger logger = Logger.getLogger(NavigatorWindow.class.getName());

	public static final int MAP_BOX_WIDTH = Map.MAP_BOX_WIDTH; // Refers to Map's MAP_BOX_WIDTH in mars-sim-mapdata maven submodule
	public static final int MAP_BOX_HEIGHT = Map.MAP_BOX_HEIGHT;
	private static final int HEIGHT_STATUS_BAR = 16;
	private static final int SCALE_CONVERSION = 3;
	private static final int CONTROL_PANE_HEIGHT = 85;
	
	private static final String LEVEL = "Level ";
	private static final String DASH = "- ";
	private static final String CHOOSE_SETTLEMENT = "List";
	private static final String MAPTYPE_ACTION = "mapType";
	private static final String RESOLUTION_ACTION = "resolution";
	private static final String MAPTYPE_RELOAD_ACTION = "notloaded";
	private static final String LAYER_ACTION = "layer";
	private static final String GO_THERE_ACTION = "goThere";
	private static final String MINERAL_ACTION = "mineralLayer";
	private static final String MINERAL = "mineral";

	private static final String MINERAL_LAYER = "minerals";
	private static final String DAYLIGHT_LAYER = "daylightTracking";
	private static final String EXPLORED_LAYER = "exploredSites";

	private static final String LON_PROP = "longitude";
	private static final String LAT_PROP = "latitude";

	/** Tool name. */
	public static final String NAME = "navigator";
	public static final String ICON = "mars";
	public static final String TITLE = Msg.getString("NavigatorWindow.title");
	public static final String PIN_ICON = "pin";
	public static final String MAP_ICON = "settlement_map";

	private static final String WHITESPACE = " ";
	private static final String SCALE = "Scale: ";
	private static final String RHO = "\u03C1: ";
	private static final String THETA = "\u03B8: ";
	private static final String PHI = "\u03C6: ";

	private static final String KM_PIXEL = " pixel: ";
	private static final String ELEVATION = " height: ";
	private static final String KM = " km";
	private static final String OPEN_PARA = " (";
	private static final String CLOSE_PARA = ")";
	
	private static final String RESOLUTION = "0";


	
	// Data member
	/** The map rho. */
	private double rho;
	/** The scale of the zoom slider. */
	private double scale;
	
	private String mapTypeCache;
	
	/** The latitude combox.  */
	private JComboBoxMW<?> latCB;
	/** The longitude combox. */
	private JComboBoxMW<?> lonCB;
	/** Settlement Combo box. */
	private JComboBox<Settlement> settlementComboBox;
	/** Latitude direction choice. */
	private JComboBoxMW<?> latCBDir;
	/** Longitude direction choice. */
	private JComboBoxMW<?> lonCBDir;

	/** Toggle button for mineral types. */
	private JButton mineralsButton;
	/** Toggle button for GPU acceleration. */
	private FlatToggleButton gpuButton;
	/** Go button. */
	private JButton goButton;
	
	private JRadioButton r0;
	private JRadioButton r1;
	
	private JPanel settlementPane;
	private JPanel goPane;
	private JPanel bottomPane;
	
	/** The info label on the status bar. */
	private JLabel scaleLabel;
	private JLabel kmPerPixelLabel;
	private JLabel heightLabel;
	private JLabel coordLabel;
	private JLabel phiLabel;
	private JLabel thetaLabel;
	private JLabel rhoLabel;

	private JSlider zoomSlider;

	private transient java.util.Map<String, MapOrder> mapLayers = new HashMap<>();

	private transient MapDataUtil mapDataUtil = MapDataUtil.instance();
	
	private transient Icon mapIcon = ImageLoader.getIconByName(MAP_ICON);
	
	private transient MineralMapLayer mineralLayer;
	
	private transient UnitManagerListener umListener;
	
	private List<Landmark> landmarks;
	
	/** The map panel class for holding all the map layers. */
	private MapPanel mapPanel;

	private JPanel detailPane;
	
	private UnitManager unitManager;
	
	private Settlement selectedSettlement;
	

	/**
	 * Constructor.
	 * 
	 * @param desktop {@link MainDesktopPane} the desktop pane
	 */
	public NavigatorWindow(MainDesktopPane desktop) {
		// use ToolWindow constructor
		super(NAME, TITLE, desktop);

		Simulation sim = desktop.getSimulation();
		this.landmarks = SimulationConfig.instance().getLandmarkConfiguration().getLandmarkList();
		this.unitManager = sim.getUnitManager();
	
		// Prepare content pane		
		JPanel contentPane = new JPanel(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		// Prepare whole 
		JPanel wholePane = new JPanel(new BorderLayout(0, 0));
		wholePane.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(),
								BorderFactory.createEmptyBorder(1, 1, 1, 1)));
		contentPane.add(wholePane, BorderLayout.CENTER);

		JPanel mapPane = new JPanel();
		wholePane.add(mapPane, BorderLayout.CENTER);

		mapPanel = new MapPanel(desktop, this);
		mapPanel.setPreferredSize(new Dimension(MAP_BOX_WIDTH, MAP_BOX_WIDTH));
		
		mapPanel.setMouseDragger(true);
		mapPanel.addMouseListener(new MouseListener());
		mapPanel.addMouseMotionListener(new MouseMotionListener());
		
		// Create map layers.
		createMapLayer(DAYLIGHT_LAYER, 0, new ShadingMapLayer(mapPanel));
		mineralLayer = new MineralMapLayer(mapPanel);
		createMapLayer(MINERAL_LAYER, 1, mineralLayer);
		createMapLayer("unitIcon", 2, new UnitIconMapLayer(mapPanel));
		createMapLayer("unitLabels", 3, new UnitLabelMapLayer());
		createMapLayer("navPoints", 4, new NavpointMapLayer(mapPanel));
		createMapLayer("vehicleTrails", 5, new VehicleTrailMapLayer());
		createMapLayer("landmarks", 6, new LandmarkMapLayer());
		createMapLayer(EXPLORED_LAYER, 7, new ExploredSiteMapLayer(mapPanel));

		mapPanel.showMap(new Coordinates((Math.PI / 2D), 0D));
		
		mapPane.add(mapPanel);
			
		buildZoomSlider();
		
		JPanel zoomPane = new JPanel(new BorderLayout());
		zoomPane.setBackground(new Color(0, 0, 0, 128));
		zoomPane.setOpaque(false);
		zoomPane.add(zoomSlider);

		mapPane.add(zoomPane);

		mapPanel.addMouseWheelListener(this);
		
		///////////////////////////////
		
		JPanel wholeBottomPane = new JPanel(new BorderLayout(0, 0));
		wholeBottomPane.setPreferredSize(new Dimension(MAP_BOX_WIDTH, CONTROL_PANE_HEIGHT));
		wholeBottomPane.setMinimumSize(new Dimension(MAP_BOX_WIDTH, CONTROL_PANE_HEIGHT));
		wholeBottomPane.setMaximumSize(new Dimension(MAP_BOX_WIDTH, CONTROL_PANE_HEIGHT));
		wholePane.add(wholeBottomPane, BorderLayout.SOUTH);
		
		JPanel coordControlPane = new JPanel(new BorderLayout());
		wholeBottomPane.add(coordControlPane, BorderLayout.CENTER);
		
		JPanel centerPane = new JPanel(new BorderLayout(0, 0));
		coordControlPane.add(centerPane, BorderLayout.CENTER);
		
		JPanel westPane = new JPanel(new BorderLayout());
		coordControlPane.add(westPane, BorderLayout.WEST);
		
		// Create a button panel
		JPanel buttonPanel = new JPanel(new GridLayout(2, 1));
		westPane.add(buttonPanel, BorderLayout.EAST);

		buttonPanel.setBorder(BorderFactory.createTitledBorder("POI"));
		buttonPanel.setToolTipText("Select your point of Interest");

		ButtonGroup group = new ButtonGroup();

		r0 = new JRadioButton("Coordinate", true);
		r1 = new JRadioButton("Settlement");

		r0.setSelected(true);	
			
		group.add(r0);
		group.add(r1);

		buttonPanel.add(r0);
		buttonPanel.add(r1);
		
		PolicyRadioActionListener actionListener = new PolicyRadioActionListener();
	
		r0.addActionListener(actionListener);
		r1.addActionListener(actionListener);
		
		////////////////////////////////////////////
			
		JPanel twoLevelPane = new JPanel(new BorderLayout(0, 0));
		centerPane.add(twoLevelPane, BorderLayout.CENTER);
		
		JPanel topPane = new JPanel(new FlowLayout(FlowLayout.CENTER));
		topPane.setAlignmentY(BOTTOM_ALIGNMENT);
		twoLevelPane.add(topPane, BorderLayout.CENTER);

		bottomPane = new JPanel(new FlowLayout(FlowLayout.CENTER));
		bottomPane.setAlignmentY(TOP_ALIGNMENT);
		twoLevelPane.add(bottomPane, BorderLayout.SOUTH);
        
		// Prepare the go button and button pane
		createGoPane(true);
			
		// Create the settlement combo box
        buildSettlementComboBox(setupSettlements());
        
        // Set up the label and the settlement pane
        createSettlementPane(false);

		////////////////////////////////////////////
			
		// Prepare latitude entry components
		JLabel latLabel = new JLabel("   Lat :", SwingConstants.RIGHT);
		topPane.add(latLabel);

		Integer[] lonDegrees = new Integer[361];
		Integer[] latDegrees = new Integer[91];
		
		// Switch to using ComboBoxMW for latitude
		int lonSize = lonDegrees.length;
		for (int i = 0; i < lonSize; i++) {
			lonDegrees[i] = i;
		}

		int latSize = latDegrees.length;
		for (int i = 0; i < latSize; i++) {
			latDegrees[i] = i;
		}

		latCB = new JComboBoxMW<>(latDegrees);
		latCB.setPreferredSize(new Dimension(60, 25));
		latCB.setSelectedItem(0);
		topPane.add(latCB);

		String[] latStrings = { Msg.getString("direction.degreeSign") + Msg.getString("direction.northShort"), //$NON-NLS-1$ //$NON-NLS-2$
				Msg.getString("direction.degreeSign") + Msg.getString("direction.southShort") //$NON-NLS-1$ //$NON-NLS-2$
		};
		latCBDir = new JComboBoxMW<>(latStrings);
		latCBDir.setPreferredSize(new Dimension(60, 25));
		latCBDir.setEditable(false);
		topPane.add(latCBDir);

		// Prepare longitude entry components
		JLabel longLabel = new JLabel("Lon :", SwingConstants.RIGHT);
		topPane.add(longLabel);

		// Switch to using ComboBoxMW for longitude
		lonCB = new JComboBoxMW<>(lonDegrees);
		lonCB.setPreferredSize(new Dimension(60, 25));
		lonCB.setSelectedItem(0);
		topPane.add(lonCB);

		String[] longStrings = { Msg.getString("direction.degreeSign") + Msg.getString("direction.eastShort"), //$NON-NLS-1$ //$NON-NLS-2$
				Msg.getString("direction.degreeSign") + Msg.getString("direction.westShort") //$NON-NLS-1$ //$NON-NLS-2$
		};
		lonCBDir = new JComboBoxMW<>(longStrings);
		lonCBDir.setPreferredSize(new Dimension(60, 25));
		lonCBDir.setEditable(false);
		topPane.add(lonCBDir);

		///////////////////////////////////////////////////////////////////////////
		
		// Prepare options panel on the right pane
		JPanel gridPane = new JPanel(new GridLayout(3, 1));
		gridPane.setAlignmentY(Component.CENTER_ALIGNMENT);
		coordControlPane.add(gridPane, BorderLayout.EAST);

		JPanel topOptionPane = new JPanel(new FlowLayout(FlowLayout.CENTER));
		topOptionPane.setPreferredSize(new Dimension(120, 25));
		topOptionPane.setMinimumSize(new Dimension(120, 25));
		topOptionPane.setMaximumSize(new Dimension(120, 25));
		gridPane.add(topOptionPane);
        
		// Prepare options button.
		JButton optionsButton = new JButton(Msg.getString("NavigatorWindow.button.mapOptions")); //$NON-NLS-1$
		optionsButton.putClientProperty("JButton.buttonType", "help");
//		May add back 
		optionsButton.setIcon(UIManager.getIcon("Tree.closedIcon"));
		optionsButton.setToolTipText(Msg.getString("NavigatorWindow.tooltip.mapOptions")); //$NON-NLS-1$
		optionsButton.addActionListener(e ->
				SwingUtilities.invokeLater(() ->
					createMapOptionsMenu().show(optionsButton, 0, optionsButton.getHeight())
				)
		);
		
		topOptionPane.add(optionsButton);

		JPanel mineralBtnPane = new JPanel(new FlowLayout(FlowLayout.CENTER));
		mineralBtnPane.setPreferredSize(new Dimension(100, 25));
		mineralBtnPane.setMinimumSize(new Dimension(100, 25));
		mineralBtnPane.setMaximumSize(new Dimension(100, 25));
		gridPane.add(mineralBtnPane);
		
		// Prepare minerals button
		mineralsButton = new JButton(Msg.getString("NavigatorWindow.button.mineralOptions")); //$NON-NLS-1$
		mineralsButton.setPreferredSize(new Dimension(100, 25));
		mineralsButton.putClientProperty("JButton.buttonType", "roundRect");
		mineralsButton.setToolTipText(Msg.getString("NavigatorWindow.tooltip.mineralOptions")); //$NON-NLS-1$
		mineralsButton.setEnabled(false);
		mineralsButton.addActionListener(e -> {
				SwingUtilities.invokeLater(() -> {
					createMineralsMenu().show(mineralsButton, 0, mineralsButton.getHeight());
				});
		});
		
		mineralBtnPane.add(mineralsButton);
		
		JPanel gpuBtnPane = new JPanel(new FlowLayout(FlowLayout.CENTER));
		gpuBtnPane.setPreferredSize(new Dimension(80, 25));
		gpuBtnPane.setMinimumSize(new Dimension(80, 25));
		gpuBtnPane.setMaximumSize(new Dimension(80, 25));
		gridPane.add(gpuBtnPane);
	
		// Prepare gpu button
		gpuButton = new FlatToggleButton(); 
		gpuButton.setPreferredSize(new Dimension(80, 25));
		gpuButton.putClientProperty("JButton.buttonType", "roundRect");
		gpuButton.setToolTipText(Msg.getString("NavigatorWindow.tooltip.gpu")); //$NON-NLS-1$
		boolean gpuState = IntegerMapData.hardwareAccel;
		gpuButton.setEnabled(gpuState);
		updateGPUButton();
		gpuButton.addActionListener(e -> {
				SwingUtilities.invokeLater(() -> updateGPUButton());
		});
		
		gpuBtnPane.add(gpuButton);

		// Create the status bar
		JStatusBar statusBar = new JStatusBar(3, 3, HEIGHT_STATUS_BAR);
		contentPane.add(statusBar, BorderLayout.SOUTH);
		
		Font font = StyleManager.getSmallFont();
		
		phiLabel = new JLabel();
		phiLabel.setFont(font);
		phiLabel.setPreferredSize(new Dimension(50, HEIGHT_STATUS_BAR));

		thetaLabel = new JLabel();
		thetaLabel.setFont(font);
		thetaLabel.setPreferredSize(new Dimension(50, HEIGHT_STATUS_BAR));

		scaleLabel = new JLabel();
		scaleLabel.setFont(font);
		scaleLabel.setPreferredSize(new Dimension(55, HEIGHT_STATUS_BAR));
		
		kmPerPixelLabel = new JLabel();
		kmPerPixelLabel.setFont(font);
		kmPerPixelLabel.setPreferredSize(new Dimension(80, HEIGHT_STATUS_BAR));
		
		rhoLabel = new JLabel();
		rhoLabel.setFont(font);
		rhoLabel.setPreferredSize(new Dimension(60, HEIGHT_STATUS_BAR));
		
		heightLabel = new JLabel();
		heightLabel.setFont(font);
		heightLabel.setPreferredSize(new Dimension(85, HEIGHT_STATUS_BAR));
	    
		coordLabel = new JLabel();
		coordLabel.setFont(font);
		coordLabel.setPreferredSize(new Dimension(110, HEIGHT_STATUS_BAR));
		
		statusBar.addLeftComponent(coordLabel, false);
		statusBar.addLeftComponent(phiLabel, false);
		statusBar.addLeftComponent(thetaLabel, false);
		
		statusBar.addCenterComponent(rhoLabel, false);
		statusBar.addCenterComponent(heightLabel, false);
		
		statusBar.addRightComponent(scaleLabel, false);
		statusBar.addRightComponent(kmPerPixelLabel, false);

		// Apply user choice from xml config file
		checkSettings();
		
		setClosable(true);
		setResizable(false);
		setMaximizable(false);

		setVisible(true);
		// Pack window
		pack();
	}

	/**
	 * Updates the state of the GPU button.
	 */
	private void updateGPUButton() {
		boolean isCapable = IntegerMapData.isGPUEnabled();
		
		if (!isCapable) {
			logger.config("Disabled GPU button since GPU acceleration cannot be enabled.");
			gpuButton.setText(Msg.getString("NavigatorWindow.button.gpu") + " off");
			gpuButton.setEnabled(false);
			mapPanel.setRho(mapPanel.getRho());
			return;
		}
		
		boolean isSelected = gpuButton.isSelected();
		String gpuStateStr1 = " off";
		if (isSelected) {
			gpuStateStr1 = " on";
			logger.config("Set GPU button to be ON.");
		}
		else {
			logger.config("Set GPU button to be OFF.");
		}
		IntegerMapData.setGPU(isSelected);
		gpuButton.setText(Msg.getString("NavigatorWindow.button.gpu") + gpuStateStr1);
		
		mapPanel.setRho(mapPanel.getRho());
	}
	
	
	/**
	 * Checks for config settings.
	 */
	private void checkSettings() {
		// Apply user choice from xml config file
		Properties userSettings = desktop.getMainWindow().getConfig().getInternalWindowProps(NAME);
		if (userSettings != null) {
			
			String resolutionString = userSettings.getProperty(RESOLUTION_ACTION, RESOLUTION);
			
			int resolution = mapPanel.getMapResolution();
			
			if (resolutionString != null) {
				resolution = Integer.parseInt(resolutionString);
			}
			
			// Set the map type
			// NOTE: this method causes Error JOptionPane.showMessageDialog at start up 
			changeMap(userSettings.getProperty(MAPTYPE_ACTION, MapDataFactory.DEFAULT_MAP_TYPE), resolution, true);

			mapTypeCache = MapDataFactory.DEFAULT_MAP_TYPE;
			
			// Check for layer action and mineral action
			checkLayerAction(userSettings);

			String latString = userSettings.getProperty(LAT_PROP);
			String lonString = userSettings.getProperty(LON_PROP);
			if ((latString != null) && (lonString != null)) {
				Coordinates userCenter = new Coordinates(latString, lonString);
				updateCoordsMaps(userCenter);
			}
		}
		else {
			// Add default map layers
			for (String layerName : mapLayers.keySet()) {
				if (!layerName.equals(DAYLIGHT_LAYER) && !layerName.equals(MINERAL_LAYER)
					&& !layerName.equals(EXPLORED_LAYER)) {
					setMapLayer(true, layerName);
				}
			}
		}
	}
	
	/**
	 * Checks for layer actions.
	 * 
	 * @param userSettings
	 */
	private void checkLayerAction(Properties userSettings) {
		for (Object key : userSettings.keySet()) {
			String prop = (String) key;
			String propValue = userSettings.getProperty(prop);

			if (prop.startsWith(LAYER_ACTION)) {
				String layer = prop.substring(LAYER_ACTION.length());
				// Check if a layer is selected
				boolean selected = Boolean.parseBoolean(propValue);
				setMapLayer(selected, layer);

				if (MINERAL_LAYER.equals(layer)) {
					selectMineralLayer(selected);
				}

			}
			else if (prop.startsWith(MINERAL_ACTION)) {
				String mineral = prop.substring(MINERAL_ACTION.length());
				mineralLayer.setMineralDisplayed(mineral, Boolean.parseBoolean(propValue));
			}
		}
	}
	
	/**
	 * Creates the go button and pane.
	 * 
	 * @param isCreatingButton
	 */
	public void createGoPane(boolean isCreatingButton) {
		
		if (isCreatingButton) {
			goButton = new JButton(Msg.getString("NavigatorWindow.button.resetGo")); //$NON-NLS-1$
			goButton.setToolTipText("Go to the location with your specified coordinate");
			goButton.setActionCommand(GO_THERE_ACTION);
			goButton.addActionListener(this);
		}
		
		goPane = new JPanel(new FlowLayout(FlowLayout.CENTER));
		goPane.add(goButton);
		
		bottomPane.add(goPane);
	}
	
	public void setZoomPanel(JPanel zoomPanel) {
		detailPane.add(zoomPanel);
	}
	
	/**
	 * Creates the settlement pane.
	 * 
	 * @param isAdding
	 */
	public void createSettlementPane(boolean isAdding) {   
	    settlementPane = new JPanel(new FlowLayout(FlowLayout.CENTER));
	    JLabel label = new JLabel("Select a settlement: ");
	    settlementPane.add(label);
	    
    	settlementPane.add(settlementComboBox);
    	
	    if (isAdding) {
			bottomPane.add(settlementPane);
	    }
	}
	
	/**
	 * Sets up a list of settlements.
	 *
	 * @return List<Settlement>
	 */
	private List<Settlement> setupSettlements() {
		List<Settlement> settlements = new ArrayList<>();

		if (GameManager.getGameMode() == GameMode.COMMAND) {
			settlements = unitManager.getCommanderSettlements();
		}

		else {
			settlements.addAll(unitManager.getSettlements());
		}

		Collections.sort(settlements);
		
		return settlements;
	}
	
	/**
	 * Creates the map layer.
	 * 
	 * @param name
	 * @param order
	 * @param layer
	 */
	private void createMapLayer(String name, int order, MapLayer layer) {
		mapLayers.put(name, new MapOrder(order, layer));
	}

	/**
	 * Updates the labels on the status bar.
	 * 
	 * @param scale
	 * @param phi
	 * @param theta
	 * @param rho
	 * @param height
	 * @param coord
	 */
	private void updateStatusBar(double scale, double phi, double theta, double rho, double height, String coord) {
		coordLabel.setText(WHITESPACE + coord);

		phiLabel.setText(PHI + StyleManager.DECIMAL_PLACES3.format(phi));
		thetaLabel.setText(THETA + StyleManager.DECIMAL_PLACES3.format(theta));
		
		rhoLabel.setText(RHO + StyleManager.DECIMAL_PLACES2.format(rho));
		heightLabel.setText(ELEVATION + StyleManager.DECIMAL3_KM.format(height));
		
		scaleLabel.setText(SCALE + StyleManager.DECIMAL_PLACES2.format(scale));
		kmPerPixelLabel.setText(KM_PIXEL + StyleManager.DECIMAL_PLACES2.format(Coordinates.MARS_RADIUS_KM / rho) + KM);
	}
	
	/**
	 * Builds the settlement combo box.
	 * 
	 * @param startingSettlements
	 */
	private void buildSettlementComboBox(List<Settlement> startingSettlements) {

		DefaultComboBoxModel<Settlement> model = new DefaultComboBoxModel<>();
		model.addAll(startingSettlements);
		model.setSelectedItem(selectedSettlement);
		settlementComboBox = new JComboBox<>(model);
		settlementComboBox.setOpaque(false);
		settlementComboBox.setToolTipText(Msg.getString("SettlementWindow.tooltip.selectSettlement")); //$NON-NLS-1$
		settlementComboBox.setRenderer(new PromptComboBoxRenderer(CHOOSE_SETTLEMENT));

		// Set the item listener only after the setup is done
		settlementComboBox.addItemListener(new ItemListener() {
	        @Override
	        public void itemStateChanged(ItemEvent e) {
				if (settlementComboBox.getSelectedIndex() == -1)
					return;
				
				
				Settlement newSettlement = (Settlement) e.getItem();
				
				if (newSettlement != null) {
					// Change to the selected settlement in SettlementMapPanel
					if (selectedSettlement != newSettlement) {
						// Set the selected settlement
						selectedSettlement = newSettlement;
					}
					
					// Need to update the coordinates
					updateCoordsMaps(newSettlement.getCoordinates());
					
					// Reset it back to the prompt text
					settlementComboBox.setSelectedIndex(-1);
				}
	        }
		});
		
		// Listen for new Settlements
		umListener = event -> {
			if (event.getEventType() == UnitManagerEventType.ADD_UNIT) {
				settlementComboBox.addItem((Settlement) event.getUnit());
			}
		};
		
		unitManager.addUnitManagerListener(UnitType.SETTLEMENT, umListener);
	}

	/**
	 * Updates coordinates in map, buttons, and globe Redraw map and globe if
	 * necessary.
	 * 
	 * @param newCoords the new center location
	 */
	public void updateCoordsBox(Coordinates newCoords) {
		
		String lat = newCoords.getFormattedLatitudeString();
		String lon = newCoords.getFormattedLongitudeString();
		
		String latNumS = lat.substring(0, lat.indexOf(' '));
		String latDirS = Msg.getString("direction.degreeSign") + lat.substring(lat.indexOf(' ') + 1);
		
		String lonNumS = lon.substring(0, lon.indexOf(' '));
		String lonDirS = Msg.getString("direction.degreeSign") + lon.substring(lon.indexOf(' ') + 1);
		
		int latNum = (int) Math.round(Double.parseDouble(latNumS));
		int lonNum = (int) Math.round(Double.parseDouble(lonNumS));
		
		latCB.setSelectedItem(latNum);
		lonCB.setSelectedItem(lonNum);
		
		latCBDir.setSelectedItem(latDirS);
		lonCBDir.setSelectedItem(lonDirS);
	}
	
	/**
	 * Updates coordinates and map and globe.
	 * 
	 * @param newCoords the new center location
	 */
	public void updateCoordsMaps(Coordinates newCoords) {
		updateCoordsBox(newCoords);
		mapPanel.showMap(newCoords);
	}
	
	/**
	 * Updates map and globe.
	 * 
	 * @param newCoords the new center location
	 */
	public void updateMaps(Coordinates newCoords) {
		mapPanel.showMap(newCoords);
	}
	
	private void goToLocation() {

		double latitude = (int) latCB.getSelectedItem();
		double longitude = (int) lonCB.getSelectedItem();
		
		String latDirStr = ((String) latCBDir.getSelectedItem()).substring(1);
		String longDirStr = ((String) lonCBDir.getSelectedItem()).substring(1);

		if ((latitude >= 0D) && (latitude <= 90D) && (longitude >= 0D) && (longitude <= 360)) {

			String westString = Msg.getString("direction.westShort"); // $NON-NLS-1$
			if (longDirStr.equals(westString)) {
				// If it's toward west
				longitude = 360D - longitude; 
			}

			updateMaps(new Coordinates(latitude + " " + latDirStr, longitude + " " + longDirStr)); // $NON-NLS-1$
			// FUTURE: May switch to having a prompt statement at the top of the combobox
			settlementComboBox.setSelectedIndex(-1);
		}
	}
		
	/**
	 * Processes the mineral command.
	 * 
	 * @param source
	 */
	private void goToMineral(Object source) {
		JCheckBoxMenuItem mineralItem = (JCheckBoxMenuItem) source;
		boolean previous = ((MineralMapLayer) mineralLayer).isMineralDisplayed(mineralItem.getText());
		boolean now = !previous;
		mineralItem.setSelected(now);
		((MineralMapLayer) mineralLayer).setMineralDisplayed(mineralItem.getText(), now);
		logger.config("Just set the state of " + mineralItem.getText() + " to " + now + " in mineral layer.");
	}
	
	/**
	 * Processes other commands.
	 * 
	 * @param command
	 * @param source
	 */
	private void goToOtherCommands(String command, Object source) {
		if (command.startsWith(MAPTYPE_ACTION)) {
			String newMapType = command.substring(MAPTYPE_ACTION.length());
			if (((JCheckBoxMenuItem) source).isSelected()) {
				changeMap(newMapType, mapPanel.getMapResolution(), false);
				mapTypeCache = newMapType;
			}
		}
		else if (command.startsWith(MAPTYPE_RELOAD_ACTION)) {			
			goToMapTypeReload(command, source);
		}
		else if (command.startsWith(LAYER_ACTION)) {
			String selectedLayer = command.substring(LAYER_ACTION.length());
			// Check if a layer is selected
			boolean selected = ((JCheckBoxMenuItem) source).isSelected();
			setMapLayer(selected, selectedLayer);
			
			if (MINERAL_LAYER.equals(selectedLayer)) {
				selectMineralLayer(selected);
			}
		}
	}
	
	/**
	 * Processes the map reload command.
	 * 
	 * @param command
	 * @param source
	 */
	private void goToMapTypeReload(String command, Object source) {
		if (((JCheckBoxMenuItem) source).isSelected()) {
			String newMapType = command.substring(MAPTYPE_RELOAD_ACTION.length());
			int reply = loadDialog(newMapType);

			// Warning: do not allow reply to be -1
			if (reply < 0)
				reply = 0;
			
			// Note: may explore the use of mapDataUtil.loadMapData(newMapType).getMetaData().getResolution()
			int lastMapRes = mapPanel.getMapMetaData().getResolution(); 
			if (lastMapRes < 0)
				lastMapRes = 0;
			
			// Consider if reply < mapPanel.getMapMetaData().getNumLevel()				
			// Note: Level 0 is the lowest res
			
			if (!newMapType.equalsIgnoreCase(mapTypeCache)) {
				// If loading a completely different map type
				
				// Set to the new map resolution
				mapPanel.setMapResolution(reply);
				// Change the map
				changeMap(newMapType, reply, false);
				// Set the the map type cache
				mapTypeCache = newMapType;
			}
			
			else if (reply != lastMapRes) {
				// if it's the same map type but of a different resolution
				
				// Set to the new map resolution
				mapPanel.setMapResolution(reply);
				// Change the map
				changeMap(newMapType, reply, false);
			}
		}
	}
	
	/** 
	 * ActionListener method overridden. 
	 * 
	 * @param event
	 */
	public void actionPerformed(ActionEvent event) {

		Object source = event.getSource();
		String command = event.getActionCommand();
		switch (command) {
			case GO_THERE_ACTION: {
				// Read longitude and latitude from user input, translate to radians,
				// and recenter globe and surface map on that location.
				try {
					goToLocation();
				} catch (NumberFormatException e) {
					// show exception;
				}
			} break;

			case MINERAL: {				
				goToMineral(source);			
			} break;
		
		default: // Grouped command
			goToOtherCommands(command, source);
		}
	}

	/**
	 * Loads a dialog and prompt user for res level.
	 * 
	 * @param newMapType
	 */
	private int loadDialog(String newMapType) {

		int oldRes = mapPanel.getMapMetaData().getResolution(); 
		if (oldRes < 0) {
			oldRes = 0;
		}
		
		List<String> list = new ArrayList<>();
		
		MapMetaData mapMetaData = mapPanel.getNewMapMetaData(newMapType);
		
		int numLevel = mapMetaData.getNumLevel();
		
		for (int i = 0; i < numLevel; i++) {
			list.add(LEVEL + i);
		}

		
		String[] options = list.toArray(String[]::new);
		
		String intialValue = options[0];
		// Set the initial value to the previous resolution choice
		if (oldRes != 0)
			intialValue = LEVEL + oldRes;
		
		return JOptionPane.showOptionDialog(getFocusOwner(),
			"Choose res level for '" + newMapType 
			+ "' map type ? (Will download if not available locally)", 
			"Surface Map Level",
			JOptionPane.YES_NO_CANCEL_OPTION,
			JOptionPane.QUESTION_MESSAGE,
			mapIcon,
			options,
			intialValue);
	}

	/**
	 * Changes the map.
	 * 
	 * @param newMapType New map Type
	 * @param res
	 * @param startup
	 */
	private void changeMap(String newMapType, int res, boolean startup) {
				
		// Load a new map 
		if (startup || mapPanel.loadMap(newMapType, res)) {
			// Update dependent panels
			MapMetaData metaType = mapPanel.getMapMetaData();
			
			if (metaType.isColourful()) {
				// turn off day night layer
				setMapLayer(false, DAYLIGHT_LAYER);
				// turn off mineral layer
				setMapLayer(false, MINERAL_LAYER);
				
				mineralsButton.setEnabled(false);
			}
		}
		
		else {
			// Inform user
			JOptionPane.showMessageDialog(getFocusOwner(), "There was a problem loading the map data",
													"Problem loading Map",
													JOptionPane.ERROR_MESSAGE);
		}

		boolean selected = mineralsButton.isEnabled();
		
		updateMineralButtonText(selected);
	}

	/**
	 * Selects the mineral button state and text.
	 * 
	 * @param selected
	 */
	private void selectMineralLayer(boolean selected) {
		mineralsButton.setEnabled(selected);
		
		updateMineralButtonText(selected);
	}
	
	/**
	 * Updates the mineral button text.
	 * 
	 * @param selected
	 */
	private void updateMineralButtonText(boolean selected) {
		mineralsButton.setEnabled(selected);
		if (selected) {
			mineralsButton.setText(Msg.getString("NavigatorWindow.button.mineralOptions") + " on ");
			logger.config("Set Mineral button to be ON.");
		}
		else {
			mineralsButton.setText(Msg.getString("NavigatorWindow.button.mineralOptions") + " off ");
			logger.config("Set Mineral button to be OFF.");
		}
	}
	
	/**
	 * Sets a map layer on or off.
	 * 
	 * @param setMap   true if map is on and false if off.
	 * @param layerName Name of the map layer to change
	 */
	private void setMapLayer(boolean setMap, String layerName) {
		MapOrder selected = mapLayers.get(layerName);
		if (setMap) {
			mapPanel.addMapLayer(selected.layer, selected.order);
		} else {
			mapPanel.removeMapLayer(selected.layer);
		}
	}

	/**
	 * Returns the map options menu.
	 */
	private JPopupMenu createMapOptionsMenu() {
		// Create map options menu.
		JPopupMenu optionsMenu = new JPopupMenu();
		optionsMenu.setToolTipText(Msg.getString("NavigatorWindow.menu.mapOptions")); //$NON-NLS-1$
		
		// Create map type menu item.
		ButtonGroup group = new ButtonGroup();
		
		for (MapMetaData metaData: mapDataUtil.getMapTypes()) {
			
			// In case mars-sim has just been loaded: metaData.checkMapLocalAvailability();
			
			int currentRes = metaData.getResolution();
			
			String resolutionString = LEVEL + currentRes;
			
			JCheckBoxMenuItem mapItem = new JCheckBoxMenuItem(metaData.getMapType() + OPEN_PARA +  resolutionString + CLOSE_PARA
															, metaData.equals(mapPanel.getMapMetaData()));
			
			mapItem.setActionCommand(MAPTYPE_RELOAD_ACTION + metaData.getMapString());
			
			mapItem.addActionListener(this);
			optionsMenu.add(mapItem);
			group.add(mapItem);
		}
		
		optionsMenu.addSeparator();

		int size = mapLayers.size();
		for (int i = 0; i < size; i++) {
			for (Entry<String, MapOrder> e : mapLayers.entrySet()) {
				MapOrder mo = e.getValue();
				if (mo.getOrder() == i) {
					optionsMenu.add(createSelectableMapOptions(LAYER_ACTION, e.getKey(),
								mapPanel.hasMapLayer(e.getValue().layer)));
				}
			}
		}

		optionsMenu.pack();
		return optionsMenu;
	}

	/**
	 * Activates action listeners for map options menu.
	 * 
	 * @param actionPrefix
	 * @param action
	 * @param selected
	 * @return
	 */
	private JCheckBoxMenuItem createSelectableMapOptions(String actionPrefix, String action, boolean selected) {
		JCheckBoxMenuItem item = new JCheckBoxMenuItem(Msg.getString("NavigatorWindow.menu.map." + action), //$NON-NLS-1$
														selected);
		item.setActionCommand(actionPrefix + action);
		item.addActionListener(this);
		return item;
	}

	/**
	 * Returns the minerals menu.
	 */
	private JPopupMenu createMineralsMenu() {
		// Create the mineral options menu.
		JPopupMenu minMenu = new JPopupMenu();

		// Create each mineral check box item.
		java.util.Map<String, Color> mineralColors = mineralLayer.getMineralColors();
		Iterator<String> i = mineralColors.keySet().iterator();
		while (i.hasNext()) {
			String mineralName = i.next();
			Color mineralColor = mineralColors.get(mineralName);
			boolean isMineralDisplayed = mineralLayer.isMineralDisplayed(mineralName);
			JCheckBoxMenuItem mineralItem = new JCheckBoxMenuItem(mineralName, isMineralDisplayed);
			
			Icon icon = createColorLegendIcon(mineralColor, mineralItem);
			mineralItem.setIcon(icon);
			mineralItem.addActionListener(this);
			mineralItem.setActionCommand(MINERAL);
			minMenu.add(mineralItem);
		}

		minMenu.pack();
		return minMenu;
	}
	
	/**
	 * Creates an icon representing a color.
	 * 
	 * @param color            the color for the icon.
	 * @param displayComponent the component to display the icon on.
	 * @return the color icon.
	 */
	private Icon createColorLegendIcon(Color color, Component displayComponent) {
		int[] imageArray = new int[10 * 10];
		Arrays.fill(imageArray, color.getRGB());
		Image image = displayComponent.createImage(new MemoryImageSource(10, 10, imageArray, 0, 10));
		return new ImageIcon(image);
	}

	private class MouseListener extends MouseAdapter {
		@Override
		public void mouseEntered(MouseEvent event) {
			// May add back: checkHover(event);
		}
		@Override
		public void mouseExited(MouseEvent event) {
		}
		@Override
		public void mouseClicked(MouseEvent event) {
			if (SwingUtilities.isRightMouseButton(event) && event.getClickCount() == 1) {
				checkClick(event);
            }
		}
	}

	private class MouseMotionListener extends MouseMotionAdapter {
		@Override
		public void mouseMoved(MouseEvent event) {
			checkHover(event);
		}
		@Override
		public void mouseDragged(MouseEvent event) {
		}
	}

	/**
	 * Checks the click location.
	 * 
	 * @param event
	 */
	public void checkClick(MouseEvent event) {

		Coordinates mapCenter = mapPanel.getCenterLocation();
		if (mapCenter == null) {
			return;
		}
		
		Coordinates clickedPosition = mapPanel.getMouseCoordinates(event.getX(), event.getY());

		Iterator<Unit> i = unitManager.getDisplayUnits().iterator();

		// Open window if unit is clicked on the map
		while (i.hasNext()) {
			Unit unit = i.next();
			
			if (unit.getUnitType() == UnitType.VEHICLE) {
				if (((Vehicle)unit).isOutsideOnMarsMission()) {
					// Proceed to below to set cursor;
					setCursorOpenWindow(unit, clickedPosition);
				}
				else 
					continue;
			}
		}
	}

	/**
	 * Sets the cursor and open detail window of a unit.
	 * 
	 * @param unit
	 * @param clickedPosition
	 */
	private void setCursorOpenWindow(Unit unit, Coordinates clickedPosition) {
		UnitDisplayInfo displayInfo = UnitDisplayInfoFactory.getUnitDisplayInfo(unit);
		if (displayInfo != null && displayInfo.isMapDisplayed(unit)) {
			Coordinates unitCoords = unit.getCoordinates();
			double clickRange = unitCoords.getDistance(clickedPosition);
			double unitClickRange = displayInfo.getMapClickRange();
			if (clickRange < unitClickRange) {
				mapPanel.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
				desktop.showDetails(unit);
			} else
				mapPanel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}
	}
	
	/**
	 * Checks if the mouse is hovering over a map.
	 * 
	 * @param event
	 */
	public void checkHover(MouseEvent event) {

		Coordinates mapCenter = mapPanel.getCenterLocation();
		if (mapCenter == null) {
			return;
		}
		
		Coordinates pos = mapPanel.getMouseCoordinates(event.getX(), event.getY());

		double phi = pos.getPhi();
		double theta = pos.getTheta();			
		double h0 = TerrainElevation.getMOLAElevation(phi, theta);
//		double h1 = TerrainElevation.getColorElevation(phi, theta);	
		double scale = mapPanel.getScale();
		
		updateStatusBar(scale, phi, theta, rho, h0, pos.getFormattedString());

		checkOnTarget(pos);
	}

	/**
	 * Finds a target.
	 * 
	 * @param pos
	 */
	private void checkOnTarget(Coordinates pos) {
		boolean onTarget = false;

		Iterator<Unit> i = unitManager.getDisplayUnits().iterator();

		// Change mouse cursor if hovering over an unit on the map
		while (i.hasNext()) {
			Unit unit = i.next();
			if (unit.getUnitType() == UnitType.VEHICLE) {
				if (((Vehicle)unit).isOutsideOnMarsMission()) {
					// Proceed to below to set cursor;
					mapPanel.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
				}
				else 
					continue;
			}
			
			UnitDisplayInfo displayInfo = UnitDisplayInfoFactory.getUnitDisplayInfo(unit);
			if (displayInfo != null && displayInfo.isMapDisplayed(unit)) {
				double clickRange = Coordinates.computeDistance(unit.getCoordinates(), pos);
				double unitClickRange = displayInfo.getMapClickRange();
				if (clickRange < unitClickRange) {
					mapPanel.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
					onTarget = true;
				}
			}
		}

		// FUTURE: how to avoid overlapping labels ?
		
		// Change mouse cursor if hovering over a landmark on the map
		Iterator<Landmark> j = landmarks.iterator();
		while (j.hasNext()) {
			Landmark landmark = j.next();
			double clickRange = Coordinates.computeDistance(landmark.getLandmarkCoord(), pos);
			double unitClickRange = 20;
			if (clickRange < unitClickRange) {
				mapPanel.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
				onTarget = true;
			}
		}

		if (!onTarget) {
			mapPanel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}
	}
	
	/**
	 * Updates the layers with time pulse.
	 * 
	 * @param pulse The Change to the clock
	 */
	@Override
	public void update(ClockPulse pulse) {
		if (mapPanel != null) {
			mapPanel.update(pulse);
		}
	}
	 	
	/** 
	 * Gets the map panel class.
	 */
	public MapPanel getMapPanel() {
		return mapPanel;
	}
	
	@Override
	public Properties getUIProps() {
		Properties results = new Properties();

		// Record the map type
		results.setProperty(MAPTYPE_ACTION, mapPanel.getMapMetaData().getMapString());
		// Record the resolution
		results.setProperty(RESOLUTION_ACTION, "" + mapPanel.getMapMetaData().getResolution());
		Coordinates center = mapPanel.getCenterLocation();
		// Record the longitude
		results.setProperty(LON_PROP, center.getFormattedLongitudeString());
		// Record the latitude
		results.setProperty(LAT_PROP, center.getFormattedLatitudeString());

		// Additional layers
		for (Entry<String, MapOrder> e : mapLayers.entrySet()) {
			// Record the choice of layers
			results.setProperty(LAYER_ACTION + e.getKey(),
							Boolean.toString(mapPanel.hasMapLayer(e.getValue().layer)));
		}

		// Record the mineral layers
		for (String mineralName : mineralLayer.getMineralColors().keySet()) {
			results.setProperty(MINERAL_ACTION + mineralName, Boolean.toString(mineralLayer.isMineralDisplayed(mineralName)));
		}
		
		return results;
	}
	
	private void buildZoomSlider() {

		UIDefaults sliderDefaults = new UIDefaults();

        sliderDefaults.put("Slider.thumbWidth", 15);
        sliderDefaults.put("Slider.thumbHeight", 15);
        sliderDefaults.put("Slider:SliderThumb.backgroundPainter", new Painter<JComponent>() {
            public void paint(Graphics2D g, JComponent c, int w, int h) {
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setStroke(new BasicStroke(2f));
                g.setColor(Color.BLACK);
                g.fillOval(1, 1, w-1, h-1);
                g.setColor(Color.WHITE);
                g.drawOval(1, 1, w-1, h-1);
            }
        });
        sliderDefaults.put("Slider:SliderTrack.backgroundPainter", new Painter<JComponent>() {
            public void paint(Graphics2D g, JComponent c, int w, int h) {
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setStroke(new BasicStroke(2f));
                g.setColor(Color.BLACK);
                g.fillRoundRect(0, 6, w, 6, 6, 6);
                g.setColor(Color.WHITE);
                g.drawRoundRect(0, 6, w, 6, 6, 6);
            }
        });

        zoomSlider = new JSlider(SwingConstants.VERTICAL, 0, 
        		(int)computeSliderValue(IntegerMapData.maxRhoMultiplier), 10);
        zoomSlider.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 100));
        zoomSlider.setPreferredSize(new Dimension(60, 400));
        zoomSlider.setSize(new Dimension(60, 400));
		zoomSlider.setPaintTicks(true);
		zoomSlider.setPaintLabels(true);
		zoomSlider.setForeground(Color.ORANGE.darker().darker());
		zoomSlider.setBackground(new Color(0, 0, 0, 128));
		zoomSlider.setOpaque(false);
		
		zoomSlider.setVisible(true);
		
		zoomSlider.setToolTipText(Msg.getString("SettlementTransparentPanel.tooltip.zoom")); //$NON-NLS-1$
		zoomSlider.addChangeListener(e -> {
				// Change scale of map based on slider position.
				int newSliderValue = zoomSlider.getValue();
				// Note: scale = rho / RHO_DEFAULT;
				double oldScale = getScale();	
				
				double oldRho = getRho();
				
				double scale = computeScale(newSliderValue);
				
				double rho = MapPanel.RHO_DEFAULT * scale;
				
				if (rho > MapPanel.MAX_RHO) {
					rho = MapPanel.MAX_RHO;
					scale = rho / MapPanel.RHO_DEFAULT;
				}
				else if (rho < MapPanel.MIN_RHO) {
					rho = MapPanel.MIN_RHO;
					scale = rho / MapPanel.RHO_DEFAULT;
				}
	
				if (scale != oldScale) {				
					setScale(scale);
				}
				
				if (rho != oldRho) {	
					// Note: Call setRho() will redraw the map
					setRho(rho);
				}

				
//				logger.info("res: " + mapPanel.getMapResolution()
//						+ "  newSliderValue: " + Math.round(newSliderValue * 10.0)/10.0 
//						+ "  Scale: " + Math.round(oldScale* 100.0)/100.0
//						+ " -> " + Math.round(scale* 1000.0)/1000.0
//						+ "  RHO_DEFAULT: " +  Math.round(MapPanel.RHO_DEFAULT * 10.0)/10.0 
//						+ "  rho: " + Math.round(oldRho* 10.0)/10.0
//						+ " -> " + Math.round(rho* 10.0)/10.0);

		});
		
		Hashtable<Integer, JLabel> labelTable = new Hashtable<>();	
		for (int i = 1; i < IntegerMapData.maxRhoMultiplier + 1; i++) {
			labelTable.put((int)computeSliderValue(i), new JLabel(i + ""));
		}
//		labelTable.put(0, new JLabel("1/4"));
		zoomSlider.setLabelTable(labelTable);
    }
	
	/**
	 * Explicitly changes the scale and sets the zoom slider value.
	 * 
	 * @param rho
	 */
	public void updateScaleZoomSlider(double rho) {
		
		double newScale = rho / MapPanel.RHO_DEFAULT;
		
		if (scale != newScale && newScale < (int)computeSliderValue(IntegerMapData.maxRhoMultiplier)) {
//			logger.info("scale: " + Math.round(scale * 100.0)/100.0 + "  rho: " + Math.round(rho * 10.0)/10.0);
			scale = newScale;

			double newSliderValue = computeSliderValue(scale);
			
			zoomSlider.setValue((int)(Math.round(newSliderValue * 10.0)/10.0));
		}
	}
	
	/**
	 * Computes the new slider value.
	 * 
	 * @param scale
	 * @return
	 */
	private double computeSliderValue(double scale) {
		return (scale * IntegerMapData.minRhoFraction - SCALE_CONVERSION) * IntegerMapData.maxRhoMultiplier;
	}
	
	/**
	 * Computes the new scale.
	 * 
	 * @param sliderValue
	 * @return
	 */
	private double computeScale(double sliderValue) {
		return (1.0 * sliderValue / IntegerMapData.maxRhoMultiplier + SCALE_CONVERSION) / IntegerMapData.minRhoFraction;
	}
	
	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		double movement = e.getPreciseWheelRotation();
		// Note: limiting the mouse movement to incrementing or decrementing 1 only
		// to lower the need of having to render a new map excessively		
		if (movement > 0) {
			// Move mouse wheel rotated down, thus moving down zoom slider.
			zoomSlider.setValue(zoomSlider.getValue() - 1);
		}
		else if (movement < 0) {
			// Move mouse wheel rotated up, thus moving up zoom slider.
			zoomSlider.setValue(zoomSlider.getValue() + 1);
		}
	}

	/**
	 * Gets the map scale.
	 *
	 * @param scale
	 */
	public double getScale() {
		return scale;
	}
	
	/**
	 * Sets the map scale.
	 *
	 * @param scale
	 */
	public void setScale(double scale) {
		this.scale = scale;
	}
	
	/**
	 * Gets the map rho.
	 *
	 * @param rho
	 */
	public double getRho() {
		return rho;
	}
	
	/**
	 * Sets the map rho.
	 *
	 * @param rho
	 */
	public void setRho(double rho) {
		this.rho = rho;
		mapPanel.setRho(rho);
	}

	/**
	 * Prepares tool window for deletion.
	 */	
	@Override
	public void destroy() {
		if (mapPanel != null)
			mapPanel.destroy();

		latCB = null;
		lonCB = null;
		mapPanel = null;
		latCBDir = null;
		lonCBDir = null;
		
		settlementComboBox = null;
		mineralsButton = null;
		gpuButton = null;
		goButton = null;
		
		r0 = null;	
		r1 = null;
		settlementPane = null;
		goPane = null;
		
		bottomPane = null;
		heightLabel = null;
		coordLabel = null;
		phiLabel = null;
		thetaLabel = null;

		mapLayers  = null;
		landmarks = null;
		mapPanel = null;
		mineralLayer = null;

		unitManager.removeUnitManagerListener(UnitType.SETTLEMENT, umListener);

		unitManager = null;
		umListener = null;
		selectedSettlement = null;
		
	}
	
	class DoubleJSlider extends JSlider {

	    final int scale;

	    public DoubleJSlider(int min, int max, int value, int scale) {
	        super(min, max, value);
	        this.scale = scale;
	    }

	    public double getScaledValue() {
	        return ((double)super.getValue()) / this.scale;
	    }
	}
	
	/**
	 * This class allows appending a message to each element of the combo box.
	 */
	class PromptComboBoxRenderer extends DefaultListCellRenderer {

		private String prompt;
		public PromptComboBoxRenderer() {

			setHorizontalAlignment(CENTER);
			setVerticalAlignment(CENTER);
		}

		public PromptComboBoxRenderer(String prompt) {
			this.prompt = prompt;
		}

		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
				boolean cellHasFocus) {
			Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			
			if (index == -1 && value == null) {
				setText(prompt);
			}
            else {
    			setText(DASH + value);
            }
			
            return c;
		}
	}

	
	class PolicyRadioActionListener implements ActionListener {
	    @Override
	    public void actionPerformed(ActionEvent event) {
	        JRadioButton button = (JRadioButton) event.getSource();

			// Note: due to the behavior of FlatLAF
			// If player changes to a different FlatLAF theme while the settlementPane is
			// not visible, it won't get updated with the new theme color.
			// Therefore, it's best rebuilding settlementPane and goPane when r0/r1 radio button is clicked
			// in order to match the current color theme
	        
	        if (button == r0) {
			
				// Enable all lat lon controls
				latCB.setEnabled(true);
				latCBDir.setEnabled(true);
				lonCB.setEnabled(true);
				lonCBDir.setEnabled(true);
			
				bottomPane.remove(settlementPane);
				bottomPane.add(goPane);
							
				// Call to update the all components if a new theme is chosen
				FlatLaf.updateUI();
				
				repaint();
				
	        } else if (button == r1) {
				// Disable all lat lon controls
				latCB.setEnabled(false);
				latCBDir.setEnabled(false);
				lonCB.setEnabled(false);
				lonCBDir.setEnabled(false);
				
				bottomPane.remove(goPane);
				bottomPane.add(settlementPane);
							
				// Call to update the all components if a new theme is chosen
				FlatLaf.updateUI();
				
				repaint();
	        }
	    }
	}
}
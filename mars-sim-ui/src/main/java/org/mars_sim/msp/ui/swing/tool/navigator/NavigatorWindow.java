/*
 * Mars Simulation Project
 * NavigatorWindow.java
 * @date 2023-07-03
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.navigator;

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
import javax.swing.SwingUtilities;
import javax.swing.UIDefaults;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.mars.sim.mapdata.MapDataUtil;
import org.mars.sim.mapdata.MapMetaData;
import org.mars.sim.mapdata.location.Coordinates;
import org.mars.sim.mapdata.map.Map;
import org.mars.sim.mapdata.map.MapLayer;
import org.mars.sim.tools.Msg;
import org.mars_sim.msp.core.GameManager;
import org.mars_sim.msp.core.GameManager.GameMode;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.UnitManagerEventType;
import org.mars_sim.msp.core.UnitManagerListener;
import org.mars_sim.msp.core.UnitType;
import org.mars_sim.msp.core.environment.Landmark;
import org.mars_sim.msp.core.environment.TerrainElevation;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.time.ClockPulse;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.ui.swing.ConfigurableWindow;
import org.mars_sim.msp.ui.swing.JComboBoxMW;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.StyleManager;
import org.mars_sim.msp.ui.swing.tool.JStatusBar;
import org.mars_sim.msp.ui.swing.tool.map.ExploredSiteMapLayer;
import org.mars_sim.msp.ui.swing.tool.map.LandmarkMapLayer;
import org.mars_sim.msp.ui.swing.tool.map.MapPanel;
import org.mars_sim.msp.ui.swing.tool.map.MineralMapLayer;
import org.mars_sim.msp.ui.swing.tool.map.NavpointMapLayer;
import org.mars_sim.msp.ui.swing.tool.map.ShadingMapLayer;
import org.mars_sim.msp.ui.swing.tool.map.UnitIconMapLayer;
import org.mars_sim.msp.ui.swing.tool.map.UnitLabelMapLayer;
import org.mars_sim.msp.ui.swing.tool.map.VehicleTrailMapLayer;
import org.mars_sim.msp.ui.swing.toolwindow.ToolWindow;
import org.mars_sim.msp.ui.swing.unit_display_info.UnitDisplayInfo;
import org.mars_sim.msp.ui.swing.unit_display_info.UnitDisplayInfoFactory;

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
	}
	
	private static final Logger logger = Logger.getLogger(NavigatorWindow.class.getName());

	private static final String DASH = "- ";
	private static final String CHOOSE_SETTLEMENT = "List";
	private static final String MAPTYPE_ACTION = "mapType";
	private static final String MAPTYPE_UNLOAD_ACTION = "notloaded";
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
	public static final String NAME = Msg.getString("NavigatorWindow.title"); //$NON-NLS-1$
	public static final String ICON = "mars";

	public static final int MAP_BOX_WIDTH = Map.MAP_BOX_WIDTH; // Refers to Map's MAP_BOX_WIDTH in mars-sim-mapdata maven submodule
	public static final int MAP_BOX_HEIGHT = Map.MAP_BOX_HEIGHT;
	
	private static final int HEIGHT_STATUS_BAR = 16;

	private static final String WHITESPACE = " ";
	private static final String MAG = "mag: ";
	private static final String THETA = "\u03B8: "; //"Theta: ";
	private static final String PHI = "\u03C6: "; //"Phi: ";

	private static final String ELEVATION = " h: ";
	private static final String KM = " km";

	private static final String SURFACE_MAP = "surface";

	// Data member
	/** The map rho. */
	private double rho;
	
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
	/** Minerals button. */
	private JButton mineralsButton;
	/** Go button. */
	private JButton goButton;
	
	private JRadioButton r0;
	private JRadioButton r1;
	
	private JPanel settlementPane;
	private JPanel goPane;
	private JPanel bottomPane;
	
	/** The info label on the status bar. */
	private JLabel magLabel;
	private JLabel heightLabel;
	private JLabel coordLabel;
	private JLabel phiLabel;
	private JLabel thetaLabel;

	private JSlider zoomSlider;
//	private DoubleJSlider doubleSlider;
	
	private java.util.Map<String, MapOrder> mapLayers = new HashMap<>();

	private List<Landmark> landmarks;
	
	/** The map panel class for holding all the map layers. */
	private MapPanel mapLayerPanel;

	private JPanel detailPane;
	
	private MineralMapLayer mineralLayer;
	
	private UnitManager unitManager;
	
	private UnitManagerListener umListener;
	
	private Settlement selectedSettlement;

	/**
	 * Constructor.
	 * 
	 * @param desktop {@link MainDesktopPane} the desktop pane
	 */
	public NavigatorWindow(MainDesktopPane desktop) {
		// use ToolWindow constructor
		super(NAME, desktop);

		Simulation sim = desktop.getSimulation();
		this.landmarks = SimulationConfig.instance().getLandmarkConfiguration().getLandmarkList();
		this.unitManager = sim.getUnitManager();
	
		// Prepare content pane		
		JPanel contentPane = new JPanel(new BorderLayout(0, 0));
		contentPane.setBackground(new Color(0, 0, 0, 128));
		contentPane.setOpaque(false);
		setContentPane(contentPane);
		
		// Prepare whole 
		JPanel wholePane = new JPanel(new BorderLayout(0, 0));
		wholePane.setBackground(new Color(0, 0, 0, 128));
		wholePane.setOpaque(false);
		wholePane.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(),
								BorderFactory.createEmptyBorder(1, 1, 1, 1)));
		contentPane.add(wholePane, BorderLayout.CENTER);

		JPanel mapPane = new JPanel();//new FlowLayout(FlowLayout.CENTER, 0, 0));//new GridLayout(1, 2));
		mapPane.setBackground(new Color(0, 0, 0, 128));
		mapPane.setOpaque(false);
		wholePane.add(mapPane, BorderLayout.CENTER);
	
		// Build the Map panel first as the globe is a slave
//		detailPane = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
//		detailPane.setBackground(new Color(0, 0, 0, 128));
//		detailPane.setOpaque(false);
//		detailPane.setMaximumSize(new Dimension(MAP_BOX_WIDTH, MAP_BOX_WIDTH));
//		detailPane.setAlignmentX(Component.CENTER_ALIGNMENT);
//		detailPane.setAlignmentY(Component.TOP_ALIGNMENT);

//		mapPane.add(detailPane);
		
		mapLayerPanel = new MapPanel(desktop, this);
		mapLayerPanel.setBackground(new Color(0, 0, 0, 128));
		mapLayerPanel.setOpaque(false);
		mapLayerPanel.setPreferredSize(new Dimension(MAP_BOX_WIDTH, MAP_BOX_WIDTH));
		
		mapLayerPanel.setMouseDragger(true);
		mapLayerPanel.addMouseListener(new MouseListener());
		mapLayerPanel.addMouseMotionListener(new MouseMotionListener());
		
		// Create map layers.
		createMapLayer(DAYLIGHT_LAYER, 0, new ShadingMapLayer(mapLayerPanel));
		mineralLayer = new MineralMapLayer(mapLayerPanel);
		createMapLayer(MINERAL_LAYER, 1, mineralLayer);
		createMapLayer("unitIcon", 2, new UnitIconMapLayer(mapLayerPanel));
		createMapLayer("unitLabels", 3, new UnitLabelMapLayer());
		createMapLayer("navPoints", 4, new NavpointMapLayer(mapLayerPanel));
		createMapLayer("vehicleTrails", 5, new VehicleTrailMapLayer());
		createMapLayer("landmarks", 6, new LandmarkMapLayer());
		createMapLayer(EXPLORED_LAYER, 7, new ExploredSiteMapLayer(mapLayerPanel));

		mapLayerPanel.showMap(new Coordinates((Math.PI / 2D), 0D));
		
		mapPane.add(mapLayerPanel);
//		detailPane.add(mapLayerPanel);
				
		buildZoomSlider();
		
		JPanel zoomPane = new JPanel(new BorderLayout());
		zoomPane.setBackground(new Color(0, 0, 0, 128));
		zoomPane.setOpaque(false);
		zoomPane.add(zoomSlider);
		
//		JLayeredPane layerPane = new JLayeredPane();
//		layerPane.setLayer(zoomPane, JLayeredPane.PALETTE_LAYER);	
//		layerPane.add(zoomPane);
//		// Detect the mouse scroll
//		layerPane.addMouseWheelListener(this);

		mapPane.add(zoomPane);
//		mapPane.add(layerPane);
		mapLayerPanel.addMouseWheelListener(this);
		
		///////////////////////////////
		
		JPanel wholeBottomPane = new JPanel(new BorderLayout(0, 0));
		wholeBottomPane.setPreferredSize(new Dimension(MAP_BOX_WIDTH, 80));
		wholeBottomPane.setMinimumSize(new Dimension(MAP_BOX_WIDTH, 80));
		wholeBottomPane.setMaximumSize(new Dimension(MAP_BOX_WIDTH, 80));
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
        buildSettlementNameComboBox(setupSettlements());
        
        // Set up the label and the settlement pane
        createSettlementPane(false);

		
		////////////////////////////////////////////
			
		// Prepare latitude entry components
		JLabel latLabel = new JLabel("   Lat :", JLabel.RIGHT);
		topPane.add(latLabel);

		Integer[] lon_degrees = new Integer[361];
		Integer[] lat_degrees = new Integer[91];
		
		// Switch to using ComboBoxMW for latitude
		int size = lon_degrees.length;
		for (int i = 0; i < size; i++) {
			lon_degrees[i] = i;
		}

		int lat_size = lat_degrees.length;
		for (int i = 0; i < lat_size; i++) {
			lat_degrees[i] = i;
		}

		latCB = new JComboBoxMW<Integer>(lat_degrees);
		latCB.setPreferredSize(new Dimension(50, 25));
		latCB.setSelectedItem(0);
		topPane.add(latCB);

		String[] latStrings = { Msg.getString("direction.degreeSign") + Msg.getString("direction.northShort"), //$NON-NLS-1$ //$NON-NLS-2$
				Msg.getString("direction.degreeSign") + Msg.getString("direction.southShort") //$NON-NLS-1$ //$NON-NLS-2$
		};
		latCBDir = new JComboBoxMW<Object>(latStrings);
		latCBDir.setPreferredSize(new Dimension(50, 25));
		latCBDir.setEditable(false);
		topPane.add(latCBDir);

		// Prepare longitude entry components
		JLabel longLabel = new JLabel("Lon :", JLabel.RIGHT);
		topPane.add(longLabel);

		// Switch to using ComboBoxMW for longitude
		lonCB = new JComboBoxMW<Integer>(lon_degrees);
		lonCB.setPreferredSize(new Dimension(50, 25));
		lonCB.setSelectedItem(0);
		topPane.add(lonCB);

		String[] longStrings = { Msg.getString("direction.degreeSign") + Msg.getString("direction.eastShort"), //$NON-NLS-1$ //$NON-NLS-2$
				Msg.getString("direction.degreeSign") + Msg.getString("direction.westShort") //$NON-NLS-1$ //$NON-NLS-2$
		};
		lonCBDir = new JComboBoxMW<Object>(longStrings);
		lonCBDir.setPreferredSize(new Dimension(50, 25));
		lonCBDir.setEditable(false);
		topPane.add(lonCBDir);

		///////////////////////////////////////////////////////////////////////////
		
		// Prepare options panel on the right pane
		JPanel optionsPane = new JPanel(new BorderLayout(0, 0));
		optionsPane.setAlignmentY(Component.CENTER_ALIGNMENT);
		coordControlPane.add(optionsPane, BorderLayout.EAST);

		JPanel topOptionPane = new JPanel(new FlowLayout(FlowLayout.CENTER));
		topOptionPane.setPreferredSize(new Dimension(120, 30));
		topOptionPane.setMinimumSize(new Dimension(120, 30));
		topOptionPane.setMaximumSize(new Dimension(120, 30));
		optionsPane.add(topOptionPane, BorderLayout.NORTH);
        
		// Prepare options button.
		JButton optionsButton = new JButton(Msg.getString("NavigatorWindow.button.mapOptions")); //$NON-NLS-1$
		optionsButton.setToolTipText(Msg.getString("NavigatorWindow.tooltip.mapOptions")); //$NON-NLS-1$
		optionsButton.addActionListener(e -> {
				SwingUtilities.invokeLater(() -> {
					createMapOptionsMenu().show(optionsButton, 0, optionsButton.getHeight());
				});
		});
		
		topOptionPane.add(optionsButton);

		JPanel bottomOptionPane = new JPanel(new FlowLayout(FlowLayout.CENTER));
		bottomOptionPane.setPreferredSize(new Dimension(120, 30));
		bottomOptionPane.setMinimumSize(new Dimension(120, 30));
		bottomOptionPane.setMaximumSize(new Dimension(120, 30));
		optionsPane.add(bottomOptionPane, BorderLayout.CENTER);
		
		// Prepare minerals button.0
		mineralsButton = new JButton(Msg.getString("NavigatorWindow.button.mineralOptions")); //$NON-NLS-1$
		mineralsButton.setToolTipText(Msg.getString("NavigatorWindow.tooltip.mineralOptions")); //$NON-NLS-1$
		mineralsButton.setEnabled(false);
		mineralsButton.addActionListener(e -> {
				SwingUtilities.invokeLater(() -> {
					createMineralsMenu().show(mineralsButton, 0, mineralsButton.getHeight());
				});
		});
		
		bottomOptionPane.add(mineralsButton);

		// Create the status bar
		JStatusBar statusBar = new JStatusBar(3, 3, HEIGHT_STATUS_BAR);
		contentPane.add(statusBar, BorderLayout.SOUTH);
		
		Font font = StyleManager.getSmallFont();
		
		phiLabel = new JLabel();
		phiLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
		phiLabel.setFont(font);
		phiLabel.setPreferredSize(new Dimension(60, HEIGHT_STATUS_BAR));

		thetaLabel = new JLabel();
		thetaLabel.setFont(font);
		thetaLabel.setPreferredSize(new Dimension(60, HEIGHT_STATUS_BAR));

		magLabel = new JLabel();
		magLabel.setFont(font);
		magLabel.setPreferredSize(new Dimension(60, HEIGHT_STATUS_BAR));
		
		heightLabel = new JLabel();
		heightLabel.setFont(font);
		heightLabel.setPreferredSize(new Dimension(90, HEIGHT_STATUS_BAR));
	    
		coordLabel = new JLabel();
		coordLabel.setFont(font);
		coordLabel.setPreferredSize(new Dimension(120, HEIGHT_STATUS_BAR));
		
		statusBar.addLeftComponent(phiLabel, false);
		statusBar.addLeftComponent(thetaLabel, false);
		statusBar.addCenterComponent(magLabel, false);
		statusBar.addCenterComponent(heightLabel, false);
		statusBar.addRightComponent(coordLabel, false);
		
		// Apply user choice from xml config file
		Properties userSettings = desktop.getMainWindow().getConfig().getInternalWindowProps(NAME);
		if (userSettings != null) {
			// Type of Map
			setMapType(userSettings.getProperty(MAPTYPE_ACTION, SURFACE_MAP));

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

			String latString = userSettings.getProperty(LAT_PROP);
			String lonString = userSettings.getProperty(LON_PROP);
			if ((latString != null) && (lonString != null)) {
				Coordinates userCenter = new Coordinates(latString, lonString);
				updateCoordsMaps(userCenter);
			}
		}
		else {
			// Add default map layers.
			for(String layerName : mapLayers.keySet()) {
				if (!layerName.equals(DAYLIGHT_LAYER) && !layerName.equals(MINERAL_LAYER)
					&& !layerName.equals(EXPLORED_LAYER)) {
					setMapLayer(true, layerName);
				}
			}
		}
		
		setClosable(true);
		setResizable(false);
		setMaximizable(false);

		setVisible(true);
		// Pack window
		pack();
	}

	/**
	 * Creates the go button.
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
    	
	    if (isAdding)
			bottomPane.add(settlementPane);
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

		else if (GameManager.getGameMode() == GameMode.SANDBOX) {
			settlements.addAll(unitManager.getSettlements());
		}

		Collections.sort(settlements);
		
		return settlements;
	}
	
	private void createMapLayer(String name, int order, MapLayer layer) {
		mapLayers.put(name, new MapOrder(order, layer));
	}

	/**
	 * Updates the labels on the status bar.
	 * 
	 * @param height
	 * @param coord
	 * @param phi
	 * @param theta
	 */
	private void updateStatusBar(double mag, String height, String coord, double phi, double theta) {
		magLabel.setText(MAG + mag);
		heightLabel.setText(height);
		coordLabel.setText(WHITESPACE + coord);
		phiLabel.setText(PHI + phi);
		thetaLabel.setText(THETA + theta);
	}
	
	/**
	 * Builds the settlement combo box/
	 */
	private void buildSettlementNameComboBox(List<Settlement> startingSettlements) {

		DefaultComboBoxModel<Settlement> model = new DefaultComboBoxModel<>();
		model.addAll(startingSettlements);
		model.setSelectedItem(selectedSettlement);
		settlementComboBox = new JComboBox<>(model);
		settlementComboBox.setOpaque(false);
		settlementComboBox.setToolTipText(Msg.getString("SettlementWindow.tooltip.selectSettlement")); //$NON-NLS-1$
		settlementComboBox.setRenderer(new PromptComboBoxRenderer(CHOOSE_SETTLEMENT));
		
		// Set the item listener only after the setup is done
		settlementComboBox.addItemListener(event -> {
			if (settlementComboBox.getSelectedIndex() == -1)
				return;
			
			Settlement newSettlement = (Settlement) event.getItem();
			// Change to the selected settlement in SettlementMapPanel
			if (newSettlement != selectedSettlement) {
				setSettlement(newSettlement);
			}
			
			if (selectedSettlement != null)
				// Need to update the coordinates
				updateCoordsMaps(selectedSettlement.getCoordinates());
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
	 * Changes the map display to the selected settlement.
	 *
	 * @param s
	 */
	private void setSettlement(Settlement s) {
		// Set the selected settlement
		selectedSettlement = s;
		// Set the box opaque
		settlementComboBox.setOpaque(false);
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
		mapLayerPanel.showMap(newCoords);
	}
	
	/**
	 * Update map and globe.
	 * 
	 * @param newCoords the new center location
	 */
	public void updateMaps(Coordinates newCoords) {
		mapLayerPanel.showMap(newCoords);
	}
	
	/** ActionListener method overridden */
	public void actionPerformed(ActionEvent event) {

		Object source = event.getSource();
		String command = event.getActionCommand();
		switch (command) {
			case GO_THERE_ACTION: {
				// Read longitude and latitude from user input, translate to radians,
				// and recenter globe and surface map on that location.
				try {

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
				} catch (NumberFormatException e) {
				}
			} break;

			case MINERAL: {
				JCheckBoxMenuItem mineralItem = (JCheckBoxMenuItem) source;
				boolean previous = ((MineralMapLayer) mineralLayer).isMineralDisplayed(mineralItem.getText());
				boolean now = !previous;
				mineralItem.setSelected(now);
				((MineralMapLayer) mineralLayer).setMineralDisplayed(mineralItem.getText(), now);
				logger.config("Just set the state of " + mineralItem.getText() + " to " + now + " in mineral layer.");
			}
			
			break;
		
		default: // Grouped command
			if (command.startsWith(MAPTYPE_ACTION)) {
				String newMapType = command.substring(MAPTYPE_ACTION.length());
				if (((JCheckBoxMenuItem) source).isSelected()) {
					setMapType(newMapType);
				}
			}
			else if (command.startsWith(MAPTYPE_UNLOAD_ACTION)) {
				if (((JCheckBoxMenuItem) source).isSelected()) {
					String newMapType = command.substring(MAPTYPE_UNLOAD_ACTION.length());
					selectUnloadedMap(newMapType);
				}
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
	}

	/**
	 * Selects an unloaded map as the new choice but prompt user first.
	 * 
	 * @param newMapType
	 */
	private void selectUnloadedMap(String newMapType) {
		int reply = JOptionPane.showConfirmDialog(null,
								"Download the map data?", "Download map",
								JOptionPane.YES_NO_OPTION);
		if (reply == JOptionPane.YES_OPTION) {
			// This method will take a few seconds
			setMapType(newMapType);
		}
	}

	/**
	 * Changes the MapType.
	 * 
	 * @param newMapType New map Type
	 */
	private void setMapType(String newMapType) {
		if (mapLayerPanel.setMapType(newMapType)) {
			// Update dependent panels
			MapMetaData metaType = mapLayerPanel.getMapType();
			
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
		
		printMineralText(selected);
	}

	/**
	 * Selects the mineral button state and text.
	 * 
	 * @param selected
	 */
	private void selectMineralLayer(boolean selected) {
		mineralsButton.setEnabled(selected);
		
		printMineralText(selected);
	}
	
	private void printMineralText(boolean selected) {
		mineralsButton.setEnabled(selected);
		if (selected) {
			mineralsButton.setText(Msg.getString("NavigatorWindow.button.mineralOptions") + " on ");
		}
		else {
			mineralsButton.setText(Msg.getString("NavigatorWindow.button.mineralOptions") + " off ");
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
			mapLayerPanel.addMapLayer(selected.layer, selected.order);
		} else {
			mapLayerPanel.removeMapLayer(selected.layer);
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
		for (MapMetaData mapType : MapDataUtil.instance().getMapTypes()) {
			boolean loaded = mapType.isLocallyAvailable();
			JCheckBoxMenuItem mapItem = new JCheckBoxMenuItem(mapType.getName()
															+ (!loaded ? " (not loaded)" : ""),
															mapType.equals(mapLayerPanel.getMapType()));
			// Different action for unloaded maps
			mapItem.setActionCommand((loaded ? MAPTYPE_ACTION : MAPTYPE_UNLOAD_ACTION)
										+ mapType.getId());
			mapItem.addActionListener(this);
			optionsMenu.add(mapItem);
			group.add(mapItem);
		}
		optionsMenu.addSeparator();

		for (Entry<String, MapOrder> e : mapLayers.entrySet()) {
			optionsMenu.add(createSelectableMapOptions(LAYER_ACTION, e.getKey(),
							mapLayerPanel.hasMapLayer(e.getValue().layer)));

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
		MineralMapLayer mineralMapLayer = (MineralMapLayer) mineralLayer;
		java.util.Map<String, Color> mineralColors = mineralMapLayer.getMineralColors();
		Iterator<String> i = mineralColors.keySet().iterator();
		while (i.hasNext()) {
			String mineralName = i.next();
			Color mineralColor = mineralColors.get(mineralName);
			boolean isMineralDisplayed = mineralMapLayer.isMineralDisplayed(mineralName);
//			logger.config(mineralName + " in check menu's start state: " + isMineralDisplayed);
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
		public void mouseEntered(MouseEvent event) {
			// checkHover(event);
		}
		public void mouseExited(MouseEvent event) {
		}

		public void mouseClicked(MouseEvent event) {
			if (SwingUtilities.isRightMouseButton(event) && event.getClickCount() == 1) {
				checkClick(event);
            }
		}
	}

	private class MouseMotionListener extends MouseMotionAdapter {
		public void mouseMoved(MouseEvent event) {
			checkHover(event);
		}
		public void mouseDragged(MouseEvent event) {
		}
	}

	public void checkClick(MouseEvent event) {

		if (mapLayerPanel.getCenterLocation() != null) {
			Coordinates clickedPosition = mapLayerPanel.getMouseCoordinates(event.getX(), event.getY());

			Iterator<Unit> i = unitManager.getDisplayUnits().iterator();

			// Open window if unit is clicked on the map
			while (i.hasNext()) {
				Unit unit = i.next();
				
				if (unit.getUnitType() == UnitType.VEHICLE) {
					if (((Vehicle)unit).isOutsideOnMarsMission()) {
						// Proceed to below to set cursor;
					}
					else 
						continue;
				}
				
				UnitDisplayInfo displayInfo = UnitDisplayInfoFactory.getUnitDisplayInfo(unit);
				if (displayInfo != null && displayInfo.isMapDisplayed(unit)) {
					Coordinates unitCoords = unit.getCoordinates();
					double clickRange = unitCoords.getDistance(clickedPosition);
					double unitClickRange = displayInfo.getMapClickRange();
					if (clickRange < unitClickRange) {
						mapLayerPanel.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
						desktop.showDetails(unit);
					} else
						mapLayerPanel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
				}
			}
		}
	}

	/**
	 * Checks if the mouse is hovering over a map.
	 * 
	 * @param event
	 */
	public void checkHover(MouseEvent event) {

		Coordinates mapCenter = mapLayerPanel.getCenterLocation();
		if (mapCenter != null) {
			Coordinates pos = mapLayerPanel.getMouseCoordinates(event.getX(), event.getY());

			StringBuilder coordSB = new StringBuilder();			
			StringBuilder elevSB = new StringBuilder();

			double phi = pos.getPhi();
			double theta = pos.getTheta();			
			double h0 = TerrainElevation.getMOLAElevation(phi, theta);
			
			double mag = mapLayerPanel.getMagnification();
			
			phi = Math.round(phi*1000.0)/1000.0;
			theta = Math.round(theta*1000.0)/1000.0;
			mag = Math.round(mag*10.0)/10.0;
				
			elevSB.append(ELEVATION)
				.append(Math.round(h0*1000.0)/1000.0)
				.append(KM);
			
			coordSB.append(pos.getFormattedString());
			
			updateStatusBar(mag, elevSB.toString(), 
					coordSB.toString(), phi, theta);

			boolean onTarget = false;

			Iterator<Unit> i = unitManager.getDisplayUnits().iterator();

			// Change mouse cursor if hovering over an unit on the map
			while (i.hasNext()) {
				Unit unit = i.next();
				if (unit.getUnitType() == UnitType.VEHICLE) {
					if (((Vehicle)unit).isOutsideOnMarsMission()) {
						// Proceed to below to set cursor;
//						logger.info("The mouse cursor is hovering over " + unit);
					}
					else 
						continue;
				}
				
				UnitDisplayInfo displayInfo = UnitDisplayInfoFactory.getUnitDisplayInfo(unit);
				if (displayInfo != null && displayInfo.isMapDisplayed(unit)) {
					double clickRange = Coordinates.computeDistance(unit.getCoordinates(), pos);
					double unitClickRange = displayInfo.getMapClickRange();
					if (clickRange < unitClickRange) {
//						logger.info("The mouse cursor is hovering over " + unit);
						mapLayerPanel.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
						onTarget = true;
					}
				}
			}

			// FUTURE: how to avoid overlapping labels ?
			
			// Change mouse cursor if hovering over a landmark on the map
			Iterator<Landmark> j = landmarks.iterator();
			while (j.hasNext()) {
				Landmark landmark = (Landmark) j.next();
//				logger.info("The mouse cursor is hovering over " + landmark);
				double clickRange = Coordinates.computeDistance(landmark.getLandmarkCoord(), pos);
				double unitClickRange = 40D;
				if (clickRange < unitClickRange) {
					onTarget = true;
				}
			}

			if (!onTarget) {
				mapLayerPanel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}
		}
	}

	/**
	 * Updates the layers with time pulse.
	 * 
	 * @param pulse The Change to the clock
	 */
	@Override
	public void update(ClockPulse pulse) {
		if (mapLayerPanel != null) {
			mapLayerPanel.update(pulse);
		}
	}
	 	
	/** 
	 * Gets the map panel class.
	 */
	public MapPanel getMapPanel() {
		return mapLayerPanel;
	}
	
	@Override
	public Properties getUIProps() {
		Properties results = new Properties();

		// Type of Map
		results.setProperty(MAPTYPE_ACTION, mapLayerPanel.getMapType().getId());
		Coordinates center = mapLayerPanel.getCenterLocation();
		results.setProperty(LON_PROP, center.getFormattedLongitudeString());
		results.setProperty(LAT_PROP, center.getFormattedLatitudeString());

		// Additional layers
		for (Entry<String, MapOrder> e : mapLayers.entrySet()) {
			results.setProperty(LAYER_ACTION + e.getKey(),
							Boolean.toString(mapLayerPanel.hasMapLayer(e.getValue().layer)));
		}

		// Mineral Layers
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
                g.setColor(Color.BLACK);//ORANGE.darker().darker());
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
                g.setColor(Color.BLACK);//ORANGE.darker().darker());
                g.fillRoundRect(0, 6, w, 6, 6, 6); // g.fillRoundRect(0, 6, w-1, 6, 6, 6);
                g.setColor(Color.WHITE);
                g.drawRoundRect(0, 6, w, 6, 6, 6);
            }
        });

        zoomSlider = new JSlider(JSlider.VERTICAL, 0, 98, 14);
        zoomSlider.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 100));
        zoomSlider.setPreferredSize(new Dimension(60, 400));
        zoomSlider.setSize(new Dimension(60, 400));
//		zoomSlider.setMajorTickSpacing(30);
//		zoomSlider.setMinorTickSpacing(6);
		zoomSlider.setPaintTicks(true);
		zoomSlider.setPaintLabels(true);
		zoomSlider.setForeground(Color.ORANGE.darker().darker());
		zoomSlider.setBackground(new Color(0, 0, 0, 128));
		zoomSlider.setOpaque(false);
		
		zoomSlider.setVisible(true);
		
		zoomSlider.setToolTipText(Msg.getString("SettlementTransparentPanel.tooltip.zoom")); //$NON-NLS-1$
		zoomSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				// Change scale of map based on slider position.
				int newSliderValue = zoomSlider.getValue();

//				double newMag = mapLayerPanel.getMagnification();
			
				double newMag = (5.0/14 * newSliderValue + 1)/6;
									
				double newRho = MapPanel.RHO_DEFAULT * newMag;
				
				setRho(newRho);
				
//				logger.info("newSliderValue: " + newSliderValue
//						+ "  newMag: " + Math.round(newMag * 1000.0)/1000.0
//						+ "  newRho: " + Math.round(newRho * 1000.0)/1000.0
//						);
			}
		});
		
		Hashtable<Integer, JLabel> labelTable = new Hashtable<>();
		labelTable.put( Integer.valueOf(98), new JLabel("6") );
		labelTable.put( Integer.valueOf(81), new JLabel("5") );
		labelTable.put( Integer.valueOf(65), new JLabel("4") );
		labelTable.put( Integer.valueOf(48), new JLabel("3") );
		labelTable.put( Integer.valueOf(31), new JLabel("2") );
		labelTable.put( Integer.valueOf(14), new JLabel("1") );
		labelTable.put( Integer.valueOf(0), new JLabel("0.167") );		
		zoomSlider.setLabelTable(labelTable);
		
//		doubleSlider = new DoubleJSlider(0, 95, 14, 1000);
    }
	

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		int numClicks = e.getWheelRotation();
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

	/**
	 * Sets the map rho.
	 *
	 * @param rho
	 */
	public void setRho(double rho) {
		this.rho = rho;
		mapLayerPanel.setRho(rho);
	}
	
	
	/**
	 * Prepares tool window for deletion.
	 */	
	@Override
	public void destroy() {
		if (mapLayerPanel != null)
			mapLayerPanel.destroy();

		latCB = null;
		lonCB = null;
		mapLayerPanel = null;
		latCBDir = null;
		lonCBDir = null;
		
		settlementComboBox = null;
		mineralsButton = null;
		
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
		mapLayerPanel = null;
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
//            	setText(value.toString());
            }
			
            return c;
			
//			if (value == null) {
//				setText(prompt);
//				// this.setForeground(Color.orange);
//				// this.setBackground(new Color(184,134,11));
//				return this;
//			}
//
//			setText(CHOOSE_SETTLEMENT + value);
//			
//			// result.setOpaque(false);
//			return c;
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
				
				createGoPane(false);
	
				repaint();
				
	        } else if (button == r1) {
				// Disable all lat lon controls
				latCB.setEnabled(false);
				latCBDir.setEnabled(false);
				lonCB.setEnabled(false);
				lonCBDir.setEnabled(false);
				
				bottomPane.remove(goPane);
				
				createSettlementPane(true);
				
				repaint();
	        }
	    }
	}
}

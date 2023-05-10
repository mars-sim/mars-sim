/*
 * Mars Simulation Project
 * NavigatorWindow.java
 * @date 2023-04-28
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.navigator;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.MemoryImageSource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import org.mars_sim.mapdata.MapDataUtil;
import org.mars_sim.mapdata.MapMetaData;
import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.UnitType;
import org.mars_sim.msp.core.environment.Landmark;
import org.mars_sim.msp.core.environment.TerrainElevation;
import org.mars_sim.msp.core.time.ClockPulse;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.ui.swing.ConfigurableWindow;
import org.mars_sim.msp.ui.swing.JComboBoxMW;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.StyleManager;
import org.mars_sim.msp.ui.swing.tool.JStatusBar;
import org.mars_sim.msp.ui.swing.tool.map.ExploredSiteMapLayer;
import org.mars_sim.msp.ui.swing.tool.map.LandmarkMapLayer;
import org.mars_sim.msp.ui.swing.tool.map.MapLayer;
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
public class NavigatorWindow extends ToolWindow implements ActionListener, ConfigurableWindow {

	private static class MapOrder {
		int order;
		MapLayer layer;

		public MapOrder(int order, MapLayer layer) {
			this.order = order;
			this.layer = layer;
		}
	}
	
	private static final Logger logger = Logger.getLogger(NavigatorWindow.class.getName());

	private static final String MAPTYPE_ACTION = "mapType";
	private static final String MAPTYPE_UNLOAD_ACTION = "notloaded";
	private static final String LAYER_ACTION = "layer";
	private static final String GO_THERE_ACTION = "goThere";
	private static final String MINERAL_ACTION = "mineralLayer";

	private static final String MINERAL_LAYER = "minerals";
	private static final String DAYLIGHT_LAYER = "daylightTracking";
	private static final String EXPLORED_LAYER = "exploredSites";

	private static final String LON_PROP = "longitude";
	private static final String LAT_PROP = "latitude";

	/** Tool name. */
	public static final String NAME = Msg.getString("NavigatorWindow.title"); //$NON-NLS-1$
	public static final String ICON = "mars";

	private static final int GLOBAL_MAP_WIDTH = MapPanel.MAP_BOX_WIDTH;

	private static final int HEIGHT_STATUS_BAR = 20;

	private static final double RAD_PER_DEGREE = Math.PI / 180D;

	private static final String WHITESPACE = " ";
	private static final String COMMA = ", ";
	private static final String THETA = "\u03B8: "; //"Theta: ";
	private static final String PHI = "\u03C6: "; //"Phi: ";
	private static final String CLOSE_P = ")";

	private static final String RGB = "RGB (";

	private static final String ELEVATION = " h: ";
	private static final String KM = " km";

	private static final String SURFACE_MAP = "surface";


	// Data member
	/** The latitude combox  */
	private JComboBoxMW<?> latCB;
	/** The longitude combox. */
	private JComboBoxMW<?> longCB;
	/** The map panel class for holding all the map layers. */
	private MapPanel mapLayerPanel;
	/** Globe navigation. */
	private GlobeDisplay globeNav;

	/** Latitude direction choice. */
	private JComboBoxMW<?> latDir;
	/** Longitude direction choice. */
	private JComboBoxMW<?> longDir;
	/** Minerals button. */
	private JButton mineralsButton;
	/** The info label on the status bar. */
	private JLabel heightLabel;
	private JLabel coordLabel;
	private JLabel phiLabel;
	private JLabel thetaLabel;
	private JLabel rgbLabel;
	
	private MineralMapLayer mineralLayer;
	private Map<String, MapOrder> mapLayers = new HashMap<>();

	private List<Landmark> landmarks;
	private UnitManager unitManager;

	/**
	 * Constructor.
	 * 
	 * @param desktop {@link MainDesktopPane} the desktop pane
	 */
	public NavigatorWindow(MainDesktopPane desktop) {
		// use ToolWindow constructor
		super(NAME, desktop);

		Simulation sim = desktop.getSimulation();
		this.landmarks = sim.getSurfaceFeatures().getLandmarks();
		this.unitManager = sim.getUnitManager();
	
		// Prepare content pane		
		JPanel contentPane = new JPanel(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		// Prepare whole 
		JPanel wholePane = new JPanel(new BorderLayout(0, 0));
		wholePane.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(),
								BorderFactory.createEmptyBorder(1, 1, 1, 1)));
		contentPane.add(wholePane, BorderLayout.CENTER);

		JPanel mapPane = new JPanel(new GridLayout(1, 2));
		wholePane.add(mapPane, BorderLayout.CENTER);
	
		// Build teh Map panel first as the globe is a slave
		JPanel detailPane = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		detailPane.setMaximumSize(new Dimension(GLOBAL_MAP_WIDTH, GLOBAL_MAP_WIDTH));
		detailPane.setAlignmentX(Component.CENTER_ALIGNMENT);
		detailPane.setAlignmentY(Component.TOP_ALIGNMENT);

		mapLayerPanel = new MapPanel(desktop, 500L);
		mapLayerPanel.setPreferredSize(new Dimension(GLOBAL_MAP_WIDTH, GLOBAL_MAP_WIDTH));
		mapLayerPanel.setNavWin(this);
		
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
		detailPane.add(mapLayerPanel);
		
		// Prepare globe display
		globeNav = new GlobeDisplay(this);
		globeNav.setMapType(mapLayerPanel.getMapType());
		JPanel globePane = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		globePane.setOpaque(true);
		globePane.add(globeNav);
		globePane.setMaximumSize(new Dimension(GLOBAL_MAP_WIDTH, GLOBAL_MAP_WIDTH));
		globePane.setAlignmentX(Component.CENTER_ALIGNMENT);
		globePane.setAlignmentY(Component.TOP_ALIGNMENT);

		mapPane.add(globePane);
		mapPane.add(detailPane);
		///////////////////////////////////////////////////////////////////////////
		JPanel controlPane = new JPanel();
		wholePane.add(controlPane, BorderLayout.SOUTH);

		// Prepare position coordination entry panel on the left pane
		JPanel coordPane = new JPanel();
		coordPane.setAlignmentY(Component.TOP_ALIGNMENT);

		// Prepare latitude entry components
		JLabel latLabel = new JLabel("Lat :", JLabel.RIGHT);
		coordPane.add(latLabel);

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
		latCB.setSelectedItem(0);
		coordPane.add(latCB);

		String[] latStrings = { Msg.getString("direction.degreeSign") + Msg.getString("direction.northShort"), //$NON-NLS-1$ //$NON-NLS-2$
				Msg.getString("direction.degreeSign") + Msg.getString("direction.southShort") //$NON-NLS-1$ //$NON-NLS-2$
		};
		latDir = new JComboBoxMW<Object>(latStrings);
		latDir.setEditable(false);
		coordPane.add(latDir);

		// Prepare longitude entry components
		JLabel longLabel = new JLabel("Lon :", JLabel.RIGHT);
		coordPane.add(longLabel);

		// Switch to using ComboBoxMW for longitude
		longCB = new JComboBoxMW<Integer>(lon_degrees);
		longCB.setSelectedItem(0);
		coordPane.add(longCB);

		String[] longStrings = { Msg.getString("direction.degreeSign") + Msg.getString("direction.eastShort"), //$NON-NLS-1$ //$NON-NLS-2$
				Msg.getString("direction.degreeSign") + Msg.getString("direction.westShort") //$NON-NLS-1$ //$NON-NLS-2$
		};
		longDir = new JComboBoxMW<Object>(longStrings);
		longDir.setEditable(false);
		coordPane.add(longDir);

		controlPane.add(coordPane);
		///////////////////////////////////////////////////////////////////////////
		
		// Prepare options panel on the right pane
		JPanel optionsPane = new JPanel();

		controlPane.add(optionsPane);
		optionsPane.setAlignmentY(Component.BOTTOM_ALIGNMENT);
		
		// Prepare location entry submit button
		JButton goThere = new JButton(Msg.getString("NavigatorWindow.button.resetGo")); //$NON-NLS-1$
		goThere.setToolTipText("Go to the location with your specified coordinates");
		goThere.setActionCommand(GO_THERE_ACTION);
		goThere.addActionListener(this);

		optionsPane.add(goThere);

		// Prepare options button.
		JButton optionsButton = new JButton(Msg.getString("NavigatorWindow.button.mapOptions")); //$NON-NLS-1$
		optionsButton.setToolTipText(Msg.getString("NavigatorWindow.tooltip.mapOptions")); //$NON-NLS-1$
		optionsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				SwingUtilities.invokeLater(() -> {
					JPopupMenu optionsMenu = createOptionsMenu();
					optionsMenu.show(optionsButton, 0, optionsButton.getHeight());
				});
			}
		});
		
		optionsPane.add(optionsButton);

		// Prepare minerals button.0
		mineralsButton = new JButton(Msg.getString("NavigatorWindow.button.mineralOptions")); //$NON-NLS-1$
		mineralsButton.setToolTipText(Msg.getString("NavigatorWindow.tooltip.mineralOptions")); //$NON-NLS-1$
		mineralsButton.setEnabled(false);
		mineralsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				SwingUtilities.invokeLater(() -> {
					JPopupMenu mineralsMenu = createMineralsMenu();
					mineralsMenu.show(mineralsButton, 0, mineralsButton.getHeight());
				});
			}
		});
		optionsPane.add(mineralsButton);

		// Create the status bar
		JStatusBar statusBar = new JStatusBar(3, 3, HEIGHT_STATUS_BAR+2);
		contentPane.add(statusBar, BorderLayout.SOUTH);
		
		Font font = StyleManager.getSmallFont();
		
		coordLabel = new JLabel();
		coordLabel.setFont(font);
		coordLabel.setPreferredSize(new Dimension(135, HEIGHT_STATUS_BAR));

		phiLabel = new JLabel();
		phiLabel.setFont(font);
		phiLabel.setPreferredSize(new Dimension(45, HEIGHT_STATUS_BAR));

		thetaLabel = new JLabel();
		thetaLabel.setFont(font);
		thetaLabel.setPreferredSize(new Dimension(45, HEIGHT_STATUS_BAR));

		heightLabel = new JLabel();
		heightLabel.setFont(font);
		heightLabel.setPreferredSize(new Dimension(130, HEIGHT_STATUS_BAR));

		rgbLabel = new JLabel();
		rgbLabel.setFont(font);
		rgbLabel.setPreferredSize(new Dimension(110, HEIGHT_STATUS_BAR));
	    
		statusBar.addLeftComponent(coordLabel, false);
		statusBar.addLeftComponent(phiLabel, false);
		statusBar.addLeftComponent(thetaLabel, false);
		
		statusBar.addCenterComponent(heightLabel, false);

		statusBar.addRightComponent(rgbLabel, false);
		
		// Apply user choice
		Properties userSettings = desktop.getMainWindow().getConfig().getInternalWindowProps(NAME);
		if (userSettings != null) {
			// Type of Map
			setMapType(userSettings.getProperty(MAPTYPE_ACTION, SURFACE_MAP));

			for (Object key : userSettings.keySet()) {
				String prop = (String) key;
				String propValue = userSettings.getProperty(prop);

				if (prop.startsWith(LAYER_ACTION)) {
					String layer = prop.substring(LAYER_ACTION.length());
					setMapLayer(Boolean.parseBoolean(propValue), layer);
					if (MINERAL_LAYER.equals(layer)) {
						mineralsButton.setEnabled(true);
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
				updateCoords(userCenter);
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
	 * @param rgb
	 */
	private void updateStatusBarLabels(String height, String coord, double phi, double theta, String rgb) {
		heightLabel.setText(height);
		coordLabel.setText(WHITESPACE + coord);
		phiLabel.setText(PHI + phi);
		thetaLabel.setText(THETA + theta);
		rgbLabel.setText(rgb);
	}
	
	/**
	 * Updates coordinates in map, buttons, and globe Redraw map and globe if
	 * necessary.
	 * 
	 * @param newCoords the new center location
	 */
	public void updateCoords(Coordinates newCoords) {
		mapLayerPanel.showMap(newCoords);
		globeNav.showGlobe(newCoords);
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

					double latitude = 0;
					double longitude = 0;

					latitude = (int) latCB.getSelectedItem();
					longitude = (int) longCB.getSelectedItem();
					
					String latDirStr = (String) latDir.getSelectedItem();
					String longDirStr = (String) longDir.getSelectedItem();

					if ((latitude >= 0D) && (latitude <= 90D) && (longitude >= 0D) && (longitude <= 360)) {
						String northString = Msg.getString("direction.degreeSign") + Msg.getString("direction.northShort");
						if (latDirStr.equals(northString)) {
							latitude = 90D - latitude; // $NON-NLS-1$
						} else {
							latitude += 90D;
						}

						String westString = Msg.getString("direction.degreeSign") + Msg.getString("direction.westShort");
						if (longitude > 0D) {
							if (longDirStr.equals(westString)) {
								longitude = 360D - longitude; // $NON-NLS-1$
							}
						}

						double phi = RAD_PER_DEGREE * latitude;
						double theta = RAD_PER_DEGREE * longitude;
						updateCoords(new Coordinates(phi, theta));
					}
				} catch (NumberFormatException e) {
				}
			} break;

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
					boolean selected = ((JCheckBoxMenuItem) source).isSelected();
					setMapLayer(selected, selectedLayer);
					if (MINERAL_LAYER.equals(selectedLayer)) {
						mineralsButton.setEnabled(selected);
					}
				}
		}
	}

	/**
	 * Select an unloaded map as the new choice but prompt user first
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
		mapLayerPanel.setMapType(newMapType);

		MapMetaData metaType = mapLayerPanel.getMapType();
		globeNav.setMapType(metaType);

		if (metaType.isColourful()) {
			// turn off day night layer
			setMapLayer(false, DAYLIGHT_LAYER);
			// turn off mineral layer
			setMapLayer(false, MINERAL_LAYER);
			mineralsButton.setEnabled(false);
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
	 * Create the map options menu.
	 */
	private JPopupMenu createOptionsMenu() {
		// Create options menu.
		JPopupMenu optionsMenu = new JPopupMenu();
		optionsMenu.setToolTipText(Msg.getString("NavigatorWindow.menu.mapOptions")); //$NON-NLS-1$

		// Create map type menu item.
		ButtonGroup group = new ButtonGroup();
		for (MapMetaData mapType : MapDataUtil.instance().getMapTypes()) {
			boolean loaded = mapType.isLocallyAvailable();
			JCheckBoxMenuItem mapItem = new JCheckBoxMenuItem(mapType.getName()
															+ (!loaded ? " (not loaded)" : ""),
															mapType.equals(mapLayerPanel.getMapType()));
			// Different actino for unloaded maps
			mapItem.setActionCommand((loaded ? MAPTYPE_ACTION : MAPTYPE_UNLOAD_ACTION)
										+ mapType.getId());
			mapItem.addActionListener(this);
			optionsMenu.add(mapItem);
			group.add(mapItem);
		}
		optionsMenu.addSeparator();

		for (Entry<String, MapOrder> e : mapLayers.entrySet()) {
			optionsMenu.add(createSelectable(LAYER_ACTION, e.getKey(),
							mapLayerPanel.hasMapLayer(e.getValue().layer)));

		}

		optionsMenu.pack();

		return optionsMenu;
	}

	private JCheckBoxMenuItem createSelectable(String actionPrefix, String action, boolean selected) {
		JCheckBoxMenuItem item = new JCheckBoxMenuItem(Msg.getString("NavigatorWindow.menu.map." + action), //$NON-NLS-1$
														selected);
		item.setActionCommand(actionPrefix + action);
		item.addActionListener(this);
		return item;
	}

	/**
	 * Creates the minerals menu.
	 */
	private JPopupMenu createMineralsMenu() {
		// Create the mineral options menu.
		JPopupMenu mineralsMenu = new JPopupMenu();

		// Create each mineral check box item.
		MineralMapLayer mineralMapLayer = (MineralMapLayer) mineralLayer;
		java.util.Map<String, Color> mineralColors = mineralMapLayer.getMineralColors();
		Iterator<String> i = mineralColors.keySet().iterator();
		while (i.hasNext()) {
			String mineralName = i.next();
			Color mineralColor = mineralColors.get(mineralName);
			boolean isMineralDisplayed = mineralMapLayer.isMineralDisplayed(mineralName);
			JCheckBoxMenuItem mineralItem = new JCheckBoxMenuItem(mineralName, isMineralDisplayed);
			mineralItem.setIcon(createColorLegendIcon(mineralColor, mineralItem));

			// TODO Re-use existing Action listener with a prefix pattern
			mineralItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					SwingUtilities.invokeLater(() -> {
						JCheckBoxMenuItem checkboxItem = (JCheckBoxMenuItem) event.getSource();
						((MineralMapLayer) mineralLayer).setMineralDisplayed(checkboxItem.getText(),
								checkboxItem.isSelected());
					});
				}
			});
			mineralsMenu.add(mineralItem);
		}

		mineralsMenu.pack();
		return mineralsMenu;
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
						desktop.openUnitWindow(unit, false);
					} else
						mapLayerPanel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
				}
			}
		}
	}

	/**
	 * Checks if the mouse is hovering over a map
	 * 
	 * @param event
	 */
	public void checkHover(MouseEvent event) {

		Coordinates mapCenter = mapLayerPanel.getCenterLocation();
		if (mapCenter != null) {
			Coordinates pos = mapLayerPanel.getMouseCoordinates(event.getX(), event.getY());

			StringBuilder coordSB = new StringBuilder();			
			StringBuilder rgbSB = new StringBuilder();
			StringBuilder elevSB = new StringBuilder();
			
			double phi = pos.getPhi();
			double theta = pos.getTheta();			
			double h0 = TerrainElevation.getMOLAElevation(phi, theta);
			//double h1 = TerrainElevation.getPatchedElevation(pos);
			
			phi = Math.round(phi*1000.0)/1000.0;
			theta = Math.round(theta*1000.0)/1000.0;

			elevSB.append(ELEVATION)
				.append(Math.round(h0*1000.0)/1000.0)
				//.append(" / " + Math.round(h1*1000.0)/1000.0)
				.append(KM);
			
			coordSB.append(pos.getCoordinateString());
			
			updateStatusBarLabels(elevSB.toString(), coordSB.toString(), phi, theta, rgbSB.toString());

			boolean onTarget = false;

			Iterator<Unit> i = unitManager.getDisplayUnits().iterator();

			// Change mouse cursor if hovering over an unit on the map
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
					double clickRange = Coordinates.computeDistance(unitCoords, pos);
					double unitClickRange = displayInfo.getMapClickRange();
					if (clickRange < unitClickRange) {
						// System.out.println("you're on a settlement or vehicle");
						mapLayerPanel.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
						onTarget = true;
					}
				}
			}

			// TODO: how to avoid overlapping labels ?
			
			// Change mouse cursor if hovering over a landmark on the map
			Iterator<Landmark> j = landmarks.iterator();
			while (j.hasNext()) {
				Landmark landmark = (Landmark) j.next();

				Coordinates unitCoords = landmark.getLandmarkCoord();
				double clickRange = Coordinates.computeDistance(unitCoords, pos);
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
	public void destroy() {
		if (mapLayerPanel != null)
			mapLayerPanel.destroy();
		if (globeNav != null)
			globeNav.destroy();

		latCB = null;
		longCB = null;
		mapLayerPanel = null;
		globeNav = null;

		latDir = null;
		longDir = null;
	}

	@Override
	public Properties getUIProps() {
		Properties results = new Properties();

		// Type of Map
		results.setProperty(MAPTYPE_ACTION, mapLayerPanel.getMapType().getId());
		Coordinates center = globeNav.getCoordinates();
		results.setProperty(LON_PROP, center.getFormattedLongitudeString());
		results.setProperty(LAT_PROP, center.getFormattedLatitudeString());

		// Additional layers
		for( Entry<String, MapOrder> e : mapLayers.entrySet()) {
			results.setProperty(LAYER_ACTION + e.getKey(),
							Boolean.toString(mapLayerPanel.hasMapLayer(e.getValue().layer)));
		}

		// Mineral Layers
		for(String mineralName : mineralLayer.getMineralColors().keySet()) {
			results.setProperty(MINERAL_ACTION + mineralName, Boolean.toString(mineralLayer.isMineralDisplayed(mineralName)));
		}
		return results;
	}
}

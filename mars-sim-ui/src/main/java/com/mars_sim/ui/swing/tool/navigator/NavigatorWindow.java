/*
 * Mars Simulation Project
 * NavigatorWindow.java
 * @date 2023-07-03
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.tool.navigator;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

import com.formdev.flatlaf.extras.components.FlatToggleButton;
import com.mars_sim.core.GameManager;
import com.mars_sim.core.GameManager.GameMode;
import com.mars_sim.core.Simulation;
import com.mars_sim.core.UnitManager;
import com.mars_sim.core.UnitManagerEventType;
import com.mars_sim.core.UnitManagerListener;
import com.mars_sim.core.UnitType;
import com.mars_sim.core.environment.TerrainElevation;
import com.mars_sim.core.map.IntegerMapData;
import com.mars_sim.core.map.MapDataFactory;
import com.mars_sim.core.map.MapMetaData;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ConfigurableWindow;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.MainDesktopPane;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.tool.JStatusBar;
import com.mars_sim.ui.swing.tool.map.ExploredSiteMapLayer;
import com.mars_sim.ui.swing.tool.map.FilteredMapLayer;
import com.mars_sim.ui.swing.tool.map.FilteredMapLayer.MapFilter;
import com.mars_sim.ui.swing.tool.map.LandmarkMapLayer;
import com.mars_sim.ui.swing.tool.map.MapDisplay;
import com.mars_sim.ui.swing.tool.map.MapLayer;
import com.mars_sim.ui.swing.tool.map.MapMouseListener;
import com.mars_sim.ui.swing.tool.map.MapPanel;
import com.mars_sim.ui.swing.tool.map.MineralMapLayer;
import com.mars_sim.ui.swing.tool.map.NavpointMapLayer;
import com.mars_sim.ui.swing.tool.map.ShadingMapLayer;
import com.mars_sim.ui.swing.tool.map.UnitMapLayer;
import com.mars_sim.ui.swing.tool.map.VehicleTrailMapLayer;
import com.mars_sim.ui.swing.tool_window.ToolWindow;
import com.mars_sim.ui.swing.utils.EntityListCellRenderer;
import com.mars_sim.ui.swing.utils.JCoordinateEditor;
import com.mars_sim.ui.swing.utils.TreeCheckFactory;
import com.mars_sim.ui.swing.utils.TreeCheckFactory.SelectableNode;

/**
 * The NavigatorWindow is a tool window that displays a map and a globe showing
 * Mars, and various other elements. It is the primary interface component that
 * presents the simulation to the user.
 */
@SuppressWarnings("serial")
public class NavigatorWindow extends ToolWindow implements ActionListener, ConfigurableWindow {

	// Binding from name to layer
	private static record NamedLayer(String name, MapLayer layer) {}

	/**
	 * This is used to show a MapFilter instance in a CheckTree
	 */
	private static class FilterNode implements SelectableNode {
		private MapFilter filter;
		private FilteredMapLayer layer;

		public FilterNode(FilteredMapLayer l, MapFilter m) {
			filter = m;
			layer = l;
		}

		@Override
		public String getName() {
			return filter.label();
		}

		@Override
		public Icon getIcon() {
			return filter.symbol();
		}

		@Override
		public boolean isSelected() {
			return layer.isFilterActive(filter.name());
		}

		@Override
		public void toggleSelection() {
			var selected = !isSelected();
			layer.displayFilter(filter.name(), selected);
		}
	}
	
	/**
	 * This is used to show a MapLayer instance in a CheckTree
	 */
	private class LayerNode implements SelectableNode {
		private String layerName;
		private String label;
		private MapLayer layer;

		public LayerNode(String layerName, MapLayer layer) {
			this.layerName = layerName;
			this.label = Msg.getString("NavigatorWindow.menu.layer." + layerName);
			this.layer = layer;
		}

		@Override
		public String getName() {
			return label;
		}

		@Override
		public Icon getIcon() {
			return null;
		}

		@Override
		public boolean isSelected() {
			return mapPanel.hasMapLayer(layer);
		}

		@Override
		public void toggleSelection() {
			setMapLayer(!isSelected(), layerName);
		}
	}

	private static final Logger logger = Logger.getLogger(NavigatorWindow.class.getName());

	public static final int MAP_BOX_WIDTH = MapDisplay.MAP_BOX_WIDTH; // Refers to Map's MAP_BOX_WIDTH in mars-sim-mapdata maven submodule
	public static final int MAP_BOX_HEIGHT = MapDisplay.MAP_BOX_HEIGHT;
	private static final int HEIGHT_STATUS_BAR = 18;
	private static final int CONTROL_PANE_WIDTH = 250;


	private static final String MAP_SEPERATOR = "~";

	private static final String LEVEL = "Level ";
	private static final String CHOOSE_SETTLEMENT = "Settlement";
	private static final String MAPTYPE_RELOAD_ACTION = "notloaded";

	private static final String MINERAL_LAYER = "minerals";
	private static final String DAYLIGHT_LAYER = "daylightTracking";
	private static final String EXPLORED_LAYER = "exploredSites";

	private static final String MAPTYPE_PROP = "mapType";
	private static final String RESOLUTION_PROP = "resolution";
	private static final String LAYER_PROP = "lyr" + MAP_SEPERATOR;
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
	
	private static final String RESOLUTION = "0";

	private static final Object LAYER_VISIBLE = "displayed";

	/** Toggle button for GPU acceleration. */
	private FlatToggleButton gpuButton;

	/** The info label on the status bar. */
	private JLabel scaleLabel;
	private JLabel kmPerPixelLabel;
	private JLabel heightLabel;
	private JLabel coordLabel;
	private JLabel phiLabel;
	private JLabel thetaLabel;
	private JLabel rhoLabel;

	private transient List<NamedLayer> mapLayers = new ArrayList<>();
		
	private transient UnitManagerListener umListener;
	
	/** The map panel class for holding all the map layers. */
	private MapPanel mapPanel;
	
	private UnitManager unitManager;

	private JCoordinateEditor coordEditor;
	private JComboBox<Settlement> settlementComboBox;

	/**
	 * Constructor.
	 * 
	 * @param desktop {@link MainDesktopPane} the desktop pane
	 */
	public NavigatorWindow(MainDesktopPane desktop) {
		// use ToolWindow constructor
		super(NAME, TITLE, desktop);

		Simulation sim = desktop.getSimulation();
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

		mapPanel = new MapPanel(desktop);
		mapPanel.setPreferredSize(new Dimension(MAP_BOX_WIDTH, MAP_BOX_WIDTH));
		
		mapPanel.setMouseDragger(true);

		// Create a mouse listener to show hotspots and update status bar
		var mapListner = new MapMouseListener(mapPanel) {
		    @Override
    		public void mouseMoved(MouseEvent event) {
				var coord = mapPanel.getMouseCoordinates(event.getX(), event.getY());
				updateStatusBar(coord);
				super.mouseMoved(event);
			}
		};
		mapPanel.addMouseListener(mapListner);
		mapPanel.addMouseMotionListener(mapListner);
		
		// Create map layers.
		mapLayers.add(new NamedLayer(DAYLIGHT_LAYER, new ShadingMapLayer(mapPanel)));
		mapLayers.add(new NamedLayer(MINERAL_LAYER, new MineralMapLayer(mapPanel)));
		mapLayers.add(new NamedLayer("unit", new UnitMapLayer(mapPanel)));
		mapLayers.add(new NamedLayer("navPoints", new NavpointMapLayer(mapPanel)));
		mapLayers.add(new NamedLayer("vehicleTrails", new VehicleTrailMapLayer(mapPanel)));
		mapLayers.add(new NamedLayer("landmarks", new LandmarkMapLayer(mapPanel)));
		mapLayers.add(new NamedLayer(EXPLORED_LAYER, new ExploredSiteMapLayer(mapPanel)));

		mapPanel.showMap(new Coordinates((Math.PI / 2D), 0D));
		
		mapPane.add(mapPanel);

		wholePane.add(createControlPanel(), BorderLayout.EAST);

		// Create the status bar
		contentPane.add(createStatusBar(), BorderLayout.SOUTH);

		// Apply user choice from xml config file
		checkSettings();
		
		setClosable(true);
		setResizable(false);
		setMaximizable(false);

		setVisible(true);
		// Pack window
		pack();
	}

	private JPanel createControlPanel() {
		JPanel controlPane = new JPanel(new BorderLayout(0, 0));

		controlPane.setPreferredSize(new Dimension(CONTROL_PANE_WIDTH, MAP_BOX_HEIGHT));
		controlPane.setMinimumSize(new Dimension(CONTROL_PANE_WIDTH, MAP_BOX_HEIGHT));
		controlPane.setMaximumSize(new Dimension(CONTROL_PANE_WIDTH, MAP_BOX_HEIGHT));
		
		JPanel searchPane = new JPanel();
		searchPane.setLayout(new BoxLayout(searchPane, BoxLayout.Y_AXIS));
		searchPane.setBorder(StyleManager.createLabelBorder("Search"));

		controlPane.add(searchPane, BorderLayout.NORTH);
        
        searchPane.add(createSettlementPane());
		searchPane.add(createCoordPane());

		var layerRoot = createLayerModel();
		JTree layers = TreeCheckFactory.createCheckTree(layerRoot);
		JScrollPane treeView = new JScrollPane(layers);
		controlPane.add(treeView, BorderLayout.CENTER);
		treeView.setBorder(StyleManager.createLabelBorder("Layers"));

		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.X_AXIS));
		buttonPane.setBorder(StyleManager.createLabelBorder("Graphics"));

		controlPane.add(buttonPane, BorderLayout.SOUTH);

		// Prepare options button.
		JButton mapButton = new JButton(Msg.getString("NavigatorWindow.button.mapOptions")); //-NLS-1$
		mapButton.setToolTipText(Msg.getString("NavigatorWindow.tooltip.mapOptions")); //-NLS-1$
		mapButton.addActionListener(e -> {
					var s = (Component)e.getSource();
					createMapOptionsMenu().show(s, s.getWidth(), s.getHeight());
			}
		);		
		buttonPane.add(mapButton);
	
		// Prepare gpu button
		gpuButton = new FlatToggleButton(); 
		gpuButton.setPreferredSize(new Dimension(80, 25));
		gpuButton.putClientProperty("JButton.buttonType", "roundRect");
		gpuButton.setToolTipText(Msg.getString("NavigatorWindow.tooltip.gpu")); //-NLS-1$
		gpuButton.setSelected(IntegerMapData.isHardwareAccel());

		updateGPUButton();
		gpuButton.addActionListener(e -> updateGPUButton());
		buttonPane.add(gpuButton);
		return controlPane;
	}

	private JStatusBar createStatusBar() {
		JStatusBar statusBar = new JStatusBar(3, 3, HEIGHT_STATUS_BAR);
		
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
		
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(coordLabel, BorderLayout.WEST);
		panel.add(phiLabel, BorderLayout.CENTER);
		panel.add(thetaLabel, BorderLayout.EAST);
		
		statusBar.addLeftComponent(panel, false);
		
		statusBar.addCenterComponent(rhoLabel, false);
		statusBar.addCenterComponent(heightLabel, false);
		
		statusBar.addRightComponent(scaleLabel, false);
		statusBar.addRightComponent(kmPerPixelLabel, false);

		return statusBar;
	}

	/**
	 * Updates the state of the GPU button.
	 */
	private void updateGPUButton() {	
		if (IntegerMapData.isGPUAvailable()) {
			boolean isSelected = gpuButton.isSelected();
			String gpuStateStr1 = " off";
			if (isSelected) {
				gpuStateStr1 = " on";
				logger.config("Set GPU button to be ON.");
			}
			else {
				logger.config("Set GPU button to be OFF.");
			}
			IntegerMapData.setHardwareAccel(isSelected);
			gpuButton.setText(Msg.getString("NavigatorWindow.button.gpu") + gpuStateStr1);
			
			mapPanel.setRho(mapPanel.getRho());
		}
		else {
			gpuButton.setEnabled(false);
			gpuButton.setText("No GPU");
		}
	}
	
	
	/**
	 * Checks for config settings.
	 */
	private void checkSettings() {
		boolean layerDefined = false;
		// Apply user choice from xml config file
		Properties userSettings = desktop.getMainWindow().getConfig().getInternalWindowProps(NAME);
		if (userSettings != null) {
			
			String resolutionString = userSettings.getProperty(RESOLUTION_PROP, RESOLUTION);
			
			int resolution = mapPanel.getMapResolution();
			
			if (resolutionString != null) {
				resolution = Integer.parseInt(resolutionString);
			}
			
			// Set the map type
			changeMap(userSettings.getProperty(MAPTYPE_PROP, MapDataFactory.DEFAULT_MAP_TYPE), resolution);
			
			// Check for layer action and mineral action
			layerDefined = checkLayerAction(userSettings);

			String latString = userSettings.getProperty(LAT_PROP);
			String lonString = userSettings.getProperty(LON_PROP);
			if ((latString != null) && (lonString != null)) {
				Coordinates userCenter = new Coordinates(latString, lonString);
				updateCoordsMaps(userCenter);
			}
		}
		
		if (!layerDefined) {
			// Add default map layers
			for (var l : mapLayers) {
				String layerName = l.name();
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
	private boolean checkLayerAction(Properties userSettings) {
		boolean layerDefined = false;
		for (Object key : userSettings.keySet()) {
			String prop = (String) key;
			String propValue = userSettings.getProperty(prop);

			if (prop.startsWith(LAYER_PROP)) {
				layerDefined = true;
				String []parts = prop.split(MAP_SEPERATOR);
				if (parts.length != 3) {
					logger.warning("Cannot use user prop" + prop);
				}
				else {
					boolean selected = Boolean.parseBoolean(propValue);

					String layerName = parts[1];
					String command = parts[2];
					if (command.equals(LAYER_VISIBLE)) {
						setMapLayer(selected, layerName);
					}
					// Must be a filter
					else if (nameToLayer(layerName) instanceof FilteredMapLayer fl) {
						fl.displayFilter(command, selected);
					}
				}
			}
		}

		return layerDefined;
	}

	private JPanel createCoordPane() {
		var p = new JPanel();
		coordEditor = new JCoordinateEditor(true);

		var goTo = new JButton(ImageLoader.getIconByName("action/find"));
		goTo.addActionListener(e -> {							
			// Reset it back to the prompt text
			settlementComboBox.setSelectedIndex(-1);

			var locn = coordEditor.getCoordinates();
			mapPanel.showMap(locn);
		});

		p.add(coordEditor);
		p.add(goTo);

		return p;
	}

	/**
	 * Creates the settlement pane.
	 */
	private JPanel createSettlementPane() {   
	    var settlementPane = new JPanel(new FlowLayout(FlowLayout.CENTER));

	    JLabel label = new JLabel("Settlement: ");
	    settlementPane.add(label);
	    	
		DefaultComboBoxModel<Settlement> model = new DefaultComboBoxModel<>();
		model.addAll(setupSettlements());
		settlementComboBox = new JComboBox<>(model);
		settlementComboBox.setOpaque(false);
		settlementComboBox.setToolTipText(Msg.getString("SettlementWindow.tooltip.selectSettlement")); //-NLS-1$
		settlementComboBox.setRenderer(new EntityListCellRenderer(CHOOSE_SETTLEMENT));

		// Set the item listener only after the setup is done
		settlementComboBox.addItemListener(e -> {
			Settlement newSettlement = (Settlement) e.getItem();
			
			if (newSettlement != null) {				
				// Need to update the coordinates
				updateCoordsMaps(newSettlement.getCoordinates());
			}
		});
		
		// Listen for new Settlements
		umListener = event -> {
			if (event.getEventType() == UnitManagerEventType.ADD_UNIT) {
				settlementComboBox.addItem((Settlement) event.getUnit());
			}
		};
		
		unitManager.addUnitManagerListener(UnitType.SETTLEMENT, umListener);

    	settlementPane.add(settlementComboBox);

		return settlementPane;
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
	 * Updates the labels on the status bar.
	 * 
	 * @param pos New positional Coordinate
	 */
	private void updateStatusBar(Coordinates pos) {
		double phi = pos.getPhi();
		double theta = pos.getTheta();			
		double height = TerrainElevation.getMOLAElevation(phi, theta);
		double scale = mapPanel.getScale();
		var coord = pos.getFormattedString();
		double rho = mapPanel.getRho();

		coordLabel.setText(WHITESPACE + coord);

		phiLabel.setText(PHI + StyleManager.DECIMAL_PLACES3.format(phi));
		thetaLabel.setText(THETA + StyleManager.DECIMAL_PLACES3.format(theta));
		
		rhoLabel.setText(RHO + StyleManager.DECIMAL_PLACES2.format(rho));
		heightLabel.setText(ELEVATION + StyleManager.DECIMAL3_KM.format(height));
		
		scaleLabel.setText(SCALE + StyleManager.DECIMAL_PLACES2.format(scale));
		kmPerPixelLabel.setText(KM_PIXEL + StyleManager.DECIMAL_PLACES2.format(Coordinates.MARS_RADIUS_KM / rho) + KM);
	}
	
	/**
	 * Updates coordinates and map and globe.
	 * 
	 * @param newCoords the new center location
	 */
	public void updateCoordsMaps(Coordinates newCoords) {
		coordEditor.setCoordinates(newCoords);
		mapPanel.showMap(newCoords);
	}

	/**
	 * Processes the map reload command.
	 * 
	 * @param command
	 * @param source
	 */
	private void goToMapTypeReload(String command, Object source) {
		if (((JCheckBoxMenuItem) source).isSelected()) {
			String [] parts = command.split(MAP_SEPERATOR);
			String newMapType = parts[2];
			int reply = Integer.parseInt(parts[1]);

			// if it's the same map type but of a different resolution
			// Change the map
			changeMap(newMapType, reply);
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
		if (command.startsWith(MAPTYPE_RELOAD_ACTION)) {			
			goToMapTypeReload(command, source);
		}
	}

	/**
	 * Changes the map.
	 * 
	 * @param newMapType New map Type
	 * @param res Resolution layer
	 */
	private void changeMap(String newMapType, int res) {
				
		// Load a new map 
		if (mapPanel.loadMap(newMapType, res)) {
			updateMapControls();
		}
	}

	/**
	 * Update layers according to Map type
	 */
	private void updateMapControls() {
		// Update dependent panels
		MapMetaData metaType = mapPanel.getMapMetaData();
		
		var isColourful = metaType.isColourful();
		if (isColourful) {
			// turn off day night layer
			setMapLayer(false, DAYLIGHT_LAYER);
		}
	}

	private MapLayer nameToLayer(String name) {
		return mapLayers.stream()
					.filter(n -> n.name().equals(name))
					.map(l -> l.layer())
					.findFirst()
					.orElse(null);
	}

	/**
	 * Sets a map layer on or off.
	 * 
	 * @param setMap   true if map is on and false if off.
	 * @param layerName Name of the map layer to change
	 */
	private void setMapLayer(boolean setMap, String layerName) {
		int idx = 0;
		NamedLayer selected = null;
		for(var nl : mapLayers) {
			if (nl.name().equals(layerName)) {
				selected = nl;
				break;
			}
		}
		if (selected == null) {
			logger.warning("No layer called " + layerName);
		}
		else if (setMap) {
			mapPanel.addMapLayer(selected.layer, idx);
		}
		else {
			mapPanel.removeMapLayer(selected.layer);
		}
	}

	private class JActiveMenu extends JMenu {
		private String baseName;

		public JActiveMenu(String name, boolean initialActive) {
			super();
			this.baseName = name;

			setActive(initialActive);
		}

		public void setActive(boolean active) {
			setText((active ? "> " : "") + baseName);
		}
	}

	/**
	 * Returns the map options menu.
	 */
	private JPopupMenu createMapOptionsMenu() {
		// Create map options menu.
		JPopupMenu optionsMenu = new JPopupMenu();
				
		var currentMap = mapPanel.getMapMetaData();
		var currentRes = mapPanel.getMapResolution();
		for (MapMetaData metaData: MapDataFactory.getLoadedTypes()) {
	
			JMenu mapMenu = new JActiveMenu(metaData.getDescription(), metaData.equals(currentMap));

			for(int lvl = 0; lvl < metaData.getNumLevel(); lvl++) {
				boolean displayed = (metaData.equals(currentMap)
										&& (lvl == currentRes));
				JCheckBoxMenuItem mapItem = new JCheckBoxMenuItem(LEVEL + lvl
															+ (metaData.isLocal(lvl) ? " *" : "")
															, displayed);
				mapItem.setEnabled(!displayed);
				mapItem.setActionCommand(MAPTYPE_RELOAD_ACTION + MAP_SEPERATOR + lvl + MAP_SEPERATOR + metaData.getId());
				mapItem.addActionListener(this);
				mapMenu.add(mapItem);
			}
			optionsMenu.add(mapMenu);
		}
		return optionsMenu;
	}
	
	private DefaultMutableTreeNode createLayerModel() {
		var root = new DefaultMutableTreeNode("Layers");

		for(var nl : mapLayers) {
			var layerNode = new DefaultMutableTreeNode(
						new LayerNode(nl.name(), nl.layer()));
			root.add(layerNode);

			if (nl.layer() instanceof FilteredMapLayer fl) {
				for(var m : fl.getFilterDetails()) {
					var filterNode = new DefaultMutableTreeNode(new FilterNode(fl, m));
					layerNode.add(filterNode);
				}
			}
		}

		return root;
	}

	/**
	 * Updates the map with time pulse.
	 * 
	 * @param pulse The clock pulse
	 */
	@Override
	public void update(ClockPulse pulse) {
		if ((mapPanel != null) && mapPanel.updateDisplay()) {
			updateMapControls();
		}
	}
	
	@Override
	public Properties getUIProps() {
		Properties results = new Properties();

		// Record the map type
		results.setProperty(MAPTYPE_PROP, mapPanel.getMapMetaData().getId());
		// Record the resolution
		results.setProperty(RESOLUTION_PROP, "" + mapPanel.getMapResolution());
		Coordinates center = mapPanel.getCenterLocation();
		// Record the longitude
		results.setProperty(LON_PROP, center.getFormattedLongitudeString());
		// Record the latitude
		results.setProperty(LAT_PROP, center.getFormattedLatitudeString());

		// Additional layers
		for (var e : mapLayers) {
			// Record the choice of layers
			results.setProperty(LAYER_PROP + e.name() + MAP_SEPERATOR + LAYER_VISIBLE,
							Boolean.toString(mapPanel.hasMapLayer(e.layer())));
			if (e.layer() instanceof FilteredMapLayer fl) {
				for(var f : fl.getFilterDetails()) {
					results.setProperty(LAYER_PROP + e.name() + MAP_SEPERATOR + f.name(),
								Boolean.toString(fl.isFilterActive(f.name())));
				}
			}
		}

		return results;
	}

	/**
	 * Prepares tool window for deletion.
	 */	
	@Override
	public void destroy() {
		super.destroy();
		if (mapPanel != null)
			mapPanel.destroy();

		
		unitManager.removeUnitManagerListener(UnitType.SETTLEMENT, umListener);
		
	}
}
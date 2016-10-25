/**
 * Mars Simulation Project
 * NavigatorWindow.java
 * @version 3.08 2015-07-01
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.navigator;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.MemoryImageSource;
import java.util.Arrays;
import java.util.Iterator;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.mars.Landmark;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.JComboBoxMW;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.tool.map.CannedMarsMap;
import org.mars_sim.msp.ui.swing.tool.map.ExploredSiteMapLayer;
import org.mars_sim.msp.ui.swing.tool.map.LandmarkMapLayer;
import org.mars_sim.msp.ui.swing.tool.map.Map;
import org.mars_sim.msp.ui.swing.tool.map.MapLayer;
import org.mars_sim.msp.ui.swing.tool.map.MapPanel;
import org.mars_sim.msp.ui.swing.tool.map.MineralMapLayer;
import org.mars_sim.msp.ui.swing.tool.map.NavpointMapLayer;
import org.mars_sim.msp.ui.swing.tool.map.ShadingMapLayer;
import org.mars_sim.msp.ui.swing.tool.map.SurfMarsMap;
import org.mars_sim.msp.ui.swing.tool.map.TopoMarsMap;
import org.mars_sim.msp.ui.swing.tool.map.UnitIconMapLayer;
import org.mars_sim.msp.ui.swing.tool.map.UnitLabelMapLayer;
import org.mars_sim.msp.ui.swing.tool.map.VehicleTrailMapLayer;
import org.mars_sim.msp.ui.swing.toolWindow.ToolWindow;
import org.mars_sim.msp.ui.swing.unit_display_info.UnitDisplayInfo;
import org.mars_sim.msp.ui.swing.unit_display_info.UnitDisplayInfoFactory;

/**
 * The NavigatorWindow is a tool window that displays a map and a
 * globe showing Mars, and various other elements. It is the primary
 * interface component that presents the simulation to the user.
 */
public class NavigatorWindow
extends ToolWindow
implements ActionListener {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** Tool name. */
	public static final String NAME = Msg.getString("NavigatorWindow.title"); //$NON-NLS-1$

	public static final int HORIZONTAL = 635;
	public static final int VERTICAL = 435;
	// Data members
	/** map navigation. */
	private MapPanel map;
	/** Globe navigation. */
	private GlobeDisplay globeNav;
	/** Compass navigation buttons. */
	//private NavButtonDisplay navButtons;
	/** Topographical and distance legend. */
	private LegendDisplay legend;
	/** Latitude entry. */
	private JTextField latText;
	/** Longitude entry. */
	private JTextField longText;
	/** Latitude direction choice. */
	private JComboBoxMW<?> latDir;
	/** Longitude direction choice. */
	private JComboBoxMW<?> longDir;
	/** Location entry submit button. */
	private JButton goThere;
	/** Options for map display. */
	private JButton optionsButton;
	/** Map options menu. */
	private JPopupMenu optionsMenu;
	/** Minerals button. */
	private JButton mineralsButton;
	/** Topographical map menu item. */
	private JCheckBoxMenuItem topoItem;
	/** Show unit labels menu item. */
	private JCheckBoxMenuItem unitLabelItem;
	/** Day/night tracking menu item. */
	private JCheckBoxMenuItem dayNightItem;
	/** Show vehicle trails menu item. */
	private JCheckBoxMenuItem trailItem;
	/** Show landmarks menu item. */
	private JCheckBoxMenuItem landmarkItem;
	/** Show navpoints menu item. */
	private JCheckBoxMenuItem navpointItem;
	/** Show explored sites menu item. */
	private JCheckBoxMenuItem exploredSiteItem;
	/** Show minerals menu item. */
	private JCheckBoxMenuItem mineralItem;

	private JPanel mapPaneInner;

	private MapLayer unitIconLayer;
	private MapLayer unitLabelLayer;
	private MapLayer shadingLayer;
	private MapLayer mineralLayer;
	private MapLayer trailLayer;
	private MapLayer navpointLayer;
	private MapLayer landmarkLayer;
	private MapLayer exploredSiteLayer;

	/**
	 * Constructor.
	 * @param desktop {@link MainDesktopPane} the desktop pane
	 */
	public NavigatorWindow(MainDesktopPane desktop) {

		// use ToolWindow constructor
		super(NAME, desktop);

		// Prepare content pane
		JPanel mainPane = new JPanel();
		mainPane.setLayout(new BoxLayout(mainPane, BoxLayout.Y_AXIS));
		mainPane.setBorder(new MarsPanelBorder());
		setContentPane(mainPane);
		

		// Prepare top layout panes
		JPanel topMainPane = new JPanel();
		topMainPane.setLayout(new BoxLayout(topMainPane, BoxLayout.X_AXIS));
		mainPane.add(topMainPane);

		JPanel leftTopPane = new JPanel();
		leftTopPane.setLayout(new BoxLayout(leftTopPane, BoxLayout.Y_AXIS));
		topMainPane.add(leftTopPane);

		// Prepare globe display
		//globeNav = new GlobeDisplay(this, 150, 150);
		globeNav = new GlobeDisplay(this);//, GlobeDisplay.GLOBE_MAP_WIDTH, GlobeDisplay.GLOBE_MAP_HEIGHT);
		JPanel globePane = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		globePane.setBackground(Color.black);
		globePane.setOpaque(true);
		globePane.setBorder( new CompoundBorder(new BevelBorder(BevelBorder.LOWERED),
				new LineBorder(Color.gray)));
		globePane.add(globeNav);
		leftTopPane.add(globePane);
/*
		// Prepare navigation buttons display
		navButtons = new NavButtonDisplay(this);
		JPanel navPane = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		navPane.setBorder( new CompoundBorder(new BevelBorder(BevelBorder.LOWERED),
				new LineBorder(Color.gray)));
		navPane.add(navButtons);
		leftTopPane.add(navPane);
*/
		// Put strut spacer in
		topMainPane.add(Box.createHorizontalStrut(5));

		JPanel rightTopPane = new JPanel();
		rightTopPane.setLayout(new BoxLayout(rightTopPane, BoxLayout.Y_AXIS));
		topMainPane.add(rightTopPane);

		// Prepare surface map display
		JPanel mapPane = new JPanel(new BorderLayout(0, 0));
		mapPane.setBorder( new CompoundBorder(new BevelBorder(BevelBorder.LOWERED),
				new LineBorder(Color.gray)));
		rightTopPane.add(mapPane);
		mapPaneInner = new JPanel(new BorderLayout(0, 0));
		mapPaneInner.setBackground(Color.black);
		mapPaneInner.setOpaque(true);
//		mapPaneInner.setCursor(new Cursor(java.awt.Cursor.DEFAULT_CURSOR));

		map = new MapPanel(500L);
		map.setNavWin(this);
		map.addMouseListener(new mapListener());
		map.addMouseMotionListener(new mouseMotionListener());
		map.setCursor(new Cursor(java.awt.Cursor.DEFAULT_CURSOR));

		// Create map layers.
		unitIconLayer = new UnitIconMapLayer(map);
		unitLabelLayer = new UnitLabelMapLayer();
		mineralLayer = new MineralMapLayer(map);
		shadingLayer = new ShadingMapLayer(map);
		navpointLayer = new NavpointMapLayer(map);
		trailLayer = new VehicleTrailMapLayer();
		landmarkLayer = new LandmarkMapLayer();
		exploredSiteLayer = new ExploredSiteMapLayer(map);

		// Add default map layers.
		map.addMapLayer(shadingLayer, 0);
		map.addMapLayer(unitIconLayer, 2);
		map.addMapLayer(unitLabelLayer, 3);
		map.addMapLayer(navpointLayer, 4);
		map.addMapLayer(trailLayer, 5);
		map.addMapLayer(landmarkLayer, 6);

		map.showMap(new Coordinates((Math.PI / 2D), 0D));
		mapPaneInner.add(map, BorderLayout.CENTER);
		mapPane.add(mapPaneInner, BorderLayout.CENTER);

		// Create map layers.
		unitIconLayer = new UnitIconMapLayer(map);

		// Put some glue in to fill in extra space
		rightTopPane.add(Box.createVerticalStrut(5));

		// Prepare topographical panel
		JPanel topoPane = new JPanel(new BorderLayout());
		topoPane.setBorder(new EmptyBorder(0, 3, 0, 0));
		mainPane.add(topoPane);

		// Prepare options panel
		JPanel optionsPane = new JPanel(new GridLayout(2, 1));
		topoPane.add(optionsPane, BorderLayout.CENTER);

		// Prepare options button.
		optionsButton = new JButton(Msg.getString("NavigatorWindow.button.mapOptions")); //$NON-NLS-1$
		optionsButton.setToolTipText(Msg.getString("NavigatorWindow.tooltip.mapOptions")); //$NON-NLS-1$
		optionsButton.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					if (optionsMenu == null) createOptionsMenu();
					optionsMenu.show(optionsButton, 0, optionsButton.getHeight());
				}
			}
		);
		optionsPane.add(optionsButton);

		// Prepare minerals button.0
		mineralsButton = new JButton(Msg.getString("NavigatorWindow.button.mineralOptions")); //$NON-NLS-1$
		mineralsButton.setToolTipText(Msg.getString("NavigatorWindow.tooltip.mineralOptions")); //$NON-NLS-1$
		mineralsButton.setEnabled(false);
		mineralsButton.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					JPopupMenu mineralsMenu = createMineralsMenu();
					mineralsMenu.show(mineralsButton, 0, mineralsButton.getHeight());
				}
			}
		);
		optionsPane.add(mineralsButton);

		// Prepare legend icon
		legend = new LegendDisplay();
		legend.setBorder( new CompoundBorder(new BevelBorder(BevelBorder.LOWERED),
				new LineBorder(Color.gray)));
		JPanel legendPanel = new JPanel(new BorderLayout(0, 0));
		legendPanel.add(legend, BorderLayout.NORTH);
		topoPane.add(legendPanel, BorderLayout.EAST);

		// Prepare position entry panel
		JPanel positionPane = new JPanel();
		positionPane.setLayout(new BoxLayout(positionPane, BoxLayout.X_AXIS));
		positionPane.setBorder(new EmptyBorder(6, 6, 3, 3));
		mainPane.add(positionPane);

		// Prepare latitude entry components
		JLabel latLabel = new JLabel(Msg.getString("NavigatorWindow.latitude")); //$NON-NLS-1$
		latLabel.setAlignmentY(.5F);
		positionPane.add(latLabel);

		latText = new JTextField(5);
		positionPane.add(latText);

		String[] latStrings = {
			Msg.getString("direction.degreeSign") + Msg.getString("direction.northShort"), //$NON-NLS-1$ //$NON-NLS-2$
			Msg.getString("direction.degreeSign") + Msg.getString("direction.southShort") //$NON-NLS-1$ //$NON-NLS-2$
		};
		latDir = new JComboBoxMW<Object>(latStrings);
		latDir.setEditable(false);
		latDir.setPreferredSize(new Dimension(50, -1));
		positionPane.add(latDir);

		// Put glue and strut spacers in
		positionPane.add(Box.createHorizontalGlue());
		positionPane.add(Box.createHorizontalStrut(5));

		// Prepare longitude entry components
		JLabel longLabel = new JLabel(Msg.getString("NavigatorWindow.longitude")); //$NON-NLS-1$
		longLabel.setAlignmentY(.5F);
		positionPane.add(longLabel);

		longText = new JTextField(5);
		positionPane.add(longText);

		String[] longStrings = {
			Msg.getString("direction.degreeSign") + Msg.getString("direction.eastShort"), //$NON-NLS-1$ //$NON-NLS-2$
			Msg.getString("direction.degreeSign") + Msg.getString("direction.westShort") //$NON-NLS-1$ //$NON-NLS-2$
		};
		longDir = new JComboBoxMW<Object>(longStrings);
		longDir.setEditable(false);
		longDir.setPreferredSize(new Dimension(50, -1));
		positionPane.add(longDir);

		// Put glue and strut spacers in
		positionPane.add(Box.createHorizontalGlue());
		positionPane.add(Box.createHorizontalStrut(5));

		// Prepare location entry submit button
		goThere = new JButton(Msg.getString("NavigatorWindow.button.goThere")); //$NON-NLS-1$
		goThere.addActionListener(this);
		goThere.setAlignmentY(.5F);
		positionPane.add(goThere);
		
		setSize(new Dimension(HORIZONTAL, VERTICAL));
		setPreferredSize(new Dimension(HORIZONTAL, VERTICAL));

		if (desktop.getMainScene() != null) {
			setClosable(false);
			setResizable(false);
			setMaximizable(false);
		}
		else {
			setClosable(true);
			setResizable(false);
			setMaximizable(false);
		}
		
		// Pack window
		//pack();
	}

	/** Update coordinates in map, buttons, and globe
	 *  Redraw map and globe if necessary
	 *  @param newCoords the new center location
	 */
	public void updateCoords(Coordinates newCoords) {
		//navButtons.updateCoords(newCoords);
		map.showMap(newCoords);
		globeNav.showGlobe(newCoords);
	}

	/** Update coordinates on globe only. Redraw globe if necessary
	 *  @param newCoords the new center location
	 */
	public void updateGlobeOnly(Coordinates newCoords) {
		globeNav.showGlobe(newCoords);
	}

	/** ActionListener method overridden */
	public void actionPerformed(ActionEvent event) {

		Object source = event.getSource();

		if (source == goThere) {
			// Read longitude and latitude from user input, translate to radians,
			// and recenter globe and surface map on that location.
			try {
				double latitude = ((Float) new Float(latText.getText())).doubleValue();
				double longitude = ((Float) new Float(longText.getText())).doubleValue();
				String latDirStr = (String) latDir.getSelectedItem();
				String longDirStr = (String) longDir.getSelectedItem();

				if ((latitude >= 0D) && (latitude <= 90D)) {
					if ((longitude >= 0D) && (longitude <= 180)) {
					    String northString = Msg.getString("direction.degreeSign") + Msg.getString("direction.northShort");
						if (latDirStr.equals(northString)) {
						    latitude = 90D - latitude; //$NON-NLS-1$
						}
						else {
						    latitude += 90D;
						}

						String westString = Msg.getString("direction.degreeSign") + Msg.getString("direction.westShort");
						if (longitude > 0D) {
							if (longDirStr.equals(westString)) {
							    longitude = 360D - longitude; //$NON-NLS-1$
							}
						}

						double phi = Math.PI * (latitude / 180D);
						double theta = (2 * Math.PI) * (longitude / 360D);
						updateCoords(new Coordinates(phi, theta));
					}
				}
			} catch (NumberFormatException e) {}
		}
		else if (source == topoItem) {
			if (topoItem.isSelected()) {
				map.setMapType(TopoMarsMap.TYPE);
				globeNav.showTopo();
				legend.showColor();
			}
			else {
				map.setMapType(SurfMarsMap.TYPE);
				globeNav.showSurf();
				legend.showMap();
			}
		}
		else if (source == dayNightItem) {
			setMapLayer(dayNightItem.isSelected(), 0, shadingLayer);
			globeNav.setDayNightTracking(dayNightItem.isSelected());
		}
		else if (source == unitLabelItem) setMapLayer(unitLabelItem.isSelected(), 3, unitLabelLayer);
		else if (source == trailItem) setMapLayer(trailItem.isSelected(), 5, trailLayer);
		else if (source == landmarkItem) setMapLayer(landmarkItem.isSelected(), 6, landmarkLayer);
		else if (source == navpointItem) setMapLayer(navpointItem.isSelected(), 4, navpointLayer);
		else if (source == exploredSiteItem) setMapLayer(exploredSiteItem.isSelected(), 7, exploredSiteLayer);
		else if (source == mineralItem) {
			setMapLayer(mineralItem.isSelected(), 1, mineralLayer);
			mineralsButton.setEnabled(mineralItem.isSelected());
		}
	}

	/**
	 * Sets a map layer on or off.
	 * @param setMap true if map is on and false if off.
	 * @param index the index order of the map layer.
	 * @param mapLayer the map layer.
	 */
	private void setMapLayer(boolean setMap, int index, MapLayer mapLayer) {
		if (setMap) {
		    map.addMapLayer(mapLayer, index);
		}
		else {
		    map.removeMapLayer(mapLayer);
		}
	}

	/**
	 * Create the map options menu.
	 */
	private void createOptionsMenu() {
		// Create options menu.
		optionsMenu = new JPopupMenu(Msg.getString("NavigatorWindow.menu.mapOptions")); //$NON-NLS-1$

		// Create topographical map menu item.
		topoItem = new JCheckBoxMenuItem(Msg.getString("NavigatorWindow.menu.map.topo"), TopoMarsMap.TYPE.equals(map.getMapType())); //$NON-NLS-1$
		topoItem.addActionListener(this);
		optionsMenu.add(topoItem);

		// Create unit label menu item.
		unitLabelItem = new JCheckBoxMenuItem(Msg.getString("NavigatorWindow.menu.map.showLabels"), map.hasMapLayer(unitLabelLayer)); //$NON-NLS-1$
		unitLabelItem.addActionListener(this);
		optionsMenu.add(unitLabelItem);

		// Create day/night tracking menu item.
		dayNightItem = new JCheckBoxMenuItem(Msg.getString("NavigatorWindow.menu.map.daylightTracking"), map.hasMapLayer(shadingLayer)); //$NON-NLS-1$
		dayNightItem.addActionListener(this);		
		optionsMenu.add(dayNightItem);
		//2016-06-08 Unchecked dayNightItem at the start of sim
		//globeNav.setDayNightTracking(false);
		//dayNightItem.setSelected(false);
		
		// Create vehicle trails menu item.
		trailItem = new JCheckBoxMenuItem(Msg.getString("NavigatorWindow.menu.map.showVehicleTrails"), map.hasMapLayer(trailLayer)); //$NON-NLS-1$
		trailItem.addActionListener(this);
		optionsMenu.add(trailItem);

		// Create landmarks menu item.
		landmarkItem = new JCheckBoxMenuItem(Msg.getString("NavigatorWindow.menu.map.showLandmarks"), map.hasMapLayer(landmarkLayer)); //$NON-NLS-1$
		landmarkItem.addActionListener(this);
		optionsMenu.add(landmarkItem);

		// Create navpoints menu item.
		navpointItem = new JCheckBoxMenuItem(Msg.getString("NavigatorWindow.menu.map.showNavPoints"), map.hasMapLayer(navpointLayer)); //$NON-NLS-1$
		navpointItem.addActionListener(this);
		optionsMenu.add(navpointItem);

		// Create explored site menu item.
		exploredSiteItem = new JCheckBoxMenuItem(Msg.getString("NavigatorWindow.menu.map.showExploredSites"), map.hasMapLayer(exploredSiteLayer)); //$NON-NLS-1$
		exploredSiteItem.addActionListener(this);
		optionsMenu.add(exploredSiteItem);

		// Create minerals menu item.
		mineralItem = new JCheckBoxMenuItem(Msg.getString("NavigatorWindow.menu.map.showMinerals"), map.hasMapLayer(mineralLayer)); //$NON-NLS-1$
		mineralItem.addActionListener(this);
		optionsMenu.add(mineralItem);

		optionsMenu.pack();
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
			mineralItem.addActionListener(
					new ActionListener() {
						public void actionPerformed(ActionEvent event) {
							JCheckBoxMenuItem checkboxItem = (JCheckBoxMenuItem) event.getSource();
							((MineralMapLayer) mineralLayer).setMineralDisplayed(checkboxItem.getText(),
									checkboxItem.isSelected());
						}
					});
			mineralsMenu.add(mineralItem);
		}

		mineralsMenu.pack();
		return mineralsMenu;
	}

	/**
	 * Creates an icon representing a color.
	 * @param color the color for the icon.
	 * @param displayComponent the component to display the icon on.
	 * @return the color icon.
	 */
	private Icon createColorLegendIcon(Color color, Component displayComponent) {
		int[] imageArray = new int[10 * 10];
		Arrays.fill(imageArray, color.getRGB());
		Image image = displayComponent.createImage(
				new MemoryImageSource(10, 10, imageArray, 0, 10));
		return new ImageIcon(image);
	}

	/**
	 * Opens a unit window on the desktop.
	 *
	 * @param unit the unit the window is for.
	 */
	public void openUnitWindow(Unit unit) {
		desktop.openUnitWindow(unit, false);
	}

	private class mapListener extends MouseAdapter {

		public void mouseEntered(MouseEvent event) {
			checkHover(event);
		}

		public void mouseExited(MouseEvent event) {
		}

		public void mouseClicked(MouseEvent event) {
			checkClick(event);
		}

	}

	// 2015-06-26 Added mouseMotionListener
	private class mouseMotionListener extends MouseMotionAdapter {

		public void mouseMoved(MouseEvent event) {
			checkHover(event);
		}

		public void mouseDragged(MouseEvent event) {
		}

	}

	// 2015-06-26 Added checkClick()
	public void checkClick(MouseEvent event) {

		if (map.getCenterLocation() != null) {
			double rho = CannedMarsMap.PIXEL_RHO;

			Coordinates clickedPosition = map.getCenterLocation().convertRectToSpherical(
					(double)(event.getX() - (Map.DISPLAY_HEIGHT / 2) - 1),
					(double)(event.getY() - (Map.DISPLAY_HEIGHT / 2) - 1), rho);

			Iterator<Unit> i = Simulation.instance().getUnitManager().getUnits().iterator();

			// Open window if unit is clicked on the map
			while (i.hasNext()) {
				Unit unit = i.next();
				UnitDisplayInfo displayInfo = UnitDisplayInfoFactory.getUnitDisplayInfo(unit);
				if (displayInfo.isMapDisplayed(unit)) {
					Coordinates unitCoords = unit.getCoordinates();
					double clickRange = unitCoords.getDistance(clickedPosition);
					double unitClickRange = displayInfo.getMapClickRange();
					if (clickRange < unitClickRange) {
						openUnitWindow(unit);
						map.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
					}
				}
			}
		}
	}


	// 2015-06-26 Added checkHover()
	public void checkHover(MouseEvent event) {

		Coordinates mapCenter = map.getCenterLocation();
		if (mapCenter != null) {
			double rho = CannedMarsMap.PIXEL_RHO;

			Coordinates mousePos = map.getCenterLocation().convertRectToSpherical(
					(double)(event.getX() - (Map.DISPLAY_HEIGHT / 2) - 1),
					(double)(event.getY() - (Map.DISPLAY_HEIGHT / 2) - 1), rho);
			boolean onTarget = false;

			Iterator<Unit> i = Simulation.instance().getUnitManager().getUnits().iterator();

			// Change mouse cursor if hovering over an unit on the map
			while (i.hasNext()) {
				Unit unit = i.next();
				UnitDisplayInfo displayInfo = UnitDisplayInfoFactory.getUnitDisplayInfo(unit);
				if (displayInfo.isMapDisplayed(unit)) {
					Coordinates unitCoords = unit.getCoordinates();
					double clickRange = unitCoords.getDistance(mousePos);
					double unitClickRange = displayInfo.getMapClickRange();
					if (clickRange < unitClickRange) {
						onTarget = true;
						map.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
					}
				}
			}

			// Change mouse cursor if hovering over a landmark on the map
			Iterator<Landmark> j = Simulation.instance().getMars().getSurfaceFeatures().getLandmarks().iterator();
			while (j.hasNext()) {
				Landmark landmark = (Landmark) j.next();

				Coordinates unitCoords = landmark.getLandmarkLocation();
				double clickRange = unitCoords.getDistance(mousePos);
				double unitClickRange = 40D;

				if (clickRange < unitClickRange) {
					onTarget = true;
					map.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
					//System.out.println("right on landmark");
				}
			}

			if (!onTarget) {
			    map.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}
		}
	}

	public void destroy() {
		map.destroy();
		globeNav.destroy();
		//navButtons = null;
		legend = null;
		unitIconLayer = null;
		unitLabelLayer = null;
		shadingLayer = null;
		mineralLayer = null;
		trailLayer = null;
		navpointLayer = null;
		landmarkLayer = null;
		exploredSiteLayer = null;
	}
}
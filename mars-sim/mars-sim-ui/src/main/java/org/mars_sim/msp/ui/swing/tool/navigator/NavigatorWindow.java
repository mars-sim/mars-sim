/**
 * Mars Simulation Project
 * NavigatorWindow.java
 * @version 3.00 2010-08-10
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.tool.navigator;  
  
import java.awt.*;
import java.awt.event.*;
import java.awt.image.MemoryImageSource;
import java.util.Arrays;
import java.util.Iterator;

import javax.swing.*;
import javax.swing.border.*;

import org.mars_sim.msp.core.*;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.tool.ToolWindow;
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
import org.mars_sim.msp.ui.swing.tool.map.USGSMarsMap;
import org.mars_sim.msp.ui.swing.tool.map.UnitIconMapLayer;
import org.mars_sim.msp.ui.swing.tool.map.UnitLabelMapLayer;
import org.mars_sim.msp.ui.swing.tool.map.VehicleTrailMapLayer;
import org.mars_sim.msp.ui.swing.unit_display_info.UnitDisplayInfo;
import org.mars_sim.msp.ui.swing.unit_display_info.UnitDisplayInfoFactory;

/** 
 * The NavigatorWindow is a tool window that displays a map and a
 * globe showing Mars, and various other elements. It is the primary
 * interface component that presents the simulation to the user.
 */
public class NavigatorWindow extends ToolWindow implements ActionListener {
	
	// Tool name
	public static final String NAME = "Mars Navigator";

    // Data members
    private MapPanel map; // map navigation
    private GlobeDisplay globeNav; // Globe navigation
    private NavButtonDisplay navButtons; // Compass navigation buttons
    private LegendDisplay legend; // Topographical and distance legend
    private JTextField latText; // Latitude entry
    private JTextField longText; // Longitude entry
    private JComboBox latDir; // Latitude direction choice
    private JComboBox longDir; // Longitude direction choice
    private JButton goThere; // Location entry submit button
    private JButton optionsButton; // Options for map display
    private JPopupMenu optionsMenu; // Map options menu
    private JButton mineralsButton; // Minerals button
    private JCheckBoxMenuItem topoItem; //Topographical map menu item.
    private JCheckBoxMenuItem unitLabelItem; // Show unit labels menu item.
    private JCheckBoxMenuItem dayNightItem; // Day/night tracking menu item.
    private JCheckBoxMenuItem usgsItem; // Show USGS map mode menu item.
    private JCheckBoxMenuItem trailItem; // Show vehicle trails menu item.
    private JCheckBoxMenuItem landmarkItem; // Show landmarks menu item. 
    private JCheckBoxMenuItem navpointItem; // Show navpoints menu item.
    private JCheckBoxMenuItem exploredSiteItem;  // Show explored sites menu item.
    private JCheckBoxMenuItem mineralItem; // Show minerals menu item.
    private MapLayer unitIconLayer;
    private MapLayer unitLabelLayer;
    private MapLayer shadingLayer;
    private MapLayer mineralLayer;
    private MapLayer trailLayer;
    private MapLayer navpointLayer;
    private MapLayer landmarkLayer;
    private MapLayer exploredSiteLayer;
    

    /** Constructs a NavigatorWindow object 
     *  @param desktop the desktop pane
     */
    public NavigatorWindow(MainDesktopPane desktop) {

        // use ToolWindow constructor
        super(NAME, desktop);
        
        // Set window resizable to false.
        setResizable(false);

        // Prepare content pane
        JPanel mainPane = new JPanel();
        mainPane.setLayout(new BoxLayout(mainPane, BoxLayout.Y_AXIS));
        mainPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(mainPane);

        // Prepare top layout panes
        JPanel topMainPane = new JPanel();
        topMainPane.setLayout(new BoxLayout(topMainPane, BoxLayout.X_AXIS));
        mainPane.add(topMainPane);

        JPanel leftTopPane = new JPanel();
        leftTopPane.setLayout(new BoxLayout(leftTopPane, BoxLayout.Y_AXIS));
        topMainPane.add(leftTopPane);

        // Prepare globe display
        globeNav = new GlobeDisplay(150, 150);
        JPanel globePane = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        globePane.setBorder( new CompoundBorder(new BevelBorder(BevelBorder.LOWERED),
                new LineBorder(Color.green)));
        globePane.add(globeNav);
        leftTopPane.add(globePane);

        // Prepare navigation buttons display
        navButtons = new NavButtonDisplay(this);
        JPanel navPane = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        navPane.setBorder( new CompoundBorder(new BevelBorder(BevelBorder.LOWERED),
                new LineBorder(Color.green)));
        navPane.add(navButtons);
        leftTopPane.add(navPane);

        // Put strut spacer in
        topMainPane.add(Box.createHorizontalStrut(5));

        JPanel rightTopPane = new JPanel();
        rightTopPane.setLayout(new BoxLayout(rightTopPane, BoxLayout.Y_AXIS));
        topMainPane.add(rightTopPane);

        // Prepare surface map display
        JPanel mapPane = new JPanel(new BorderLayout(0, 0));
        mapPane.setBorder( new CompoundBorder(new BevelBorder(BevelBorder.LOWERED),
                new LineBorder(Color.green)));
        rightTopPane.add(mapPane);
        JPanel mapPaneInner = new JPanel(new BorderLayout(0, 0));
        mapPaneInner.setBackground(Color.black);
        
        map = new MapPanel();
        map.addMouseListener(new mapClickListener());
        
        // Create map layers.
        unitIconLayer = new UnitIconMapLayer(map);
        unitLabelLayer = new UnitLabelMapLayer();
        map.addMapLayer(unitLabelLayer);
        mineralLayer = new MineralMapLayer(map);
        shadingLayer = new ShadingMapLayer(map);
        navpointLayer = new NavpointMapLayer(map);
        trailLayer = new VehicleTrailMapLayer();
        landmarkLayer = new LandmarkMapLayer();
        exploredSiteLayer = new ExploredSiteMapLayer(map);
        
        // Add default map layers.
        map.addMapLayer(unitIconLayer);
        map.addMapLayer(unitLabelLayer);
        map.addMapLayer(navpointLayer);
        map.addMapLayer(trailLayer);
        map.addMapLayer(landmarkLayer);
        
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
		optionsButton = new JButton("Map Options");
		optionsButton.setToolTipText("Options for displaying map.");
		optionsButton.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					if (optionsMenu == null) createOptionsMenu();
			       	optionsMenu.show(optionsButton, 0, optionsButton.getHeight());
				}
			});
		optionsPane.add(optionsButton);
		
		// Prepare minerals button.
		mineralsButton = new JButton("Minerals");
		mineralsButton.setToolTipText("Options for displaying minerals on map.");
		mineralsButton.setEnabled(false);
		mineralsButton.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					JPopupMenu mineralsMenu = createMineralsMenu();
					mineralsMenu.show(mineralsButton, 0, mineralsButton.getHeight());
				}
			});
		optionsPane.add(mineralsButton);
	
        // Prepare legend icon
        legend = new LegendDisplay();
        legend.setBorder( new CompoundBorder(new BevelBorder(BevelBorder.LOWERED),
                new LineBorder(Color.green)));
        JPanel legendPanel = new JPanel(new BorderLayout(0, 0));
        legendPanel.add(legend, BorderLayout.NORTH);
        topoPane.add(legendPanel, BorderLayout.EAST);

        // Prepare position entry panel
        JPanel positionPane = new JPanel();
        positionPane.setLayout(new BoxLayout(positionPane, BoxLayout.X_AXIS));
        positionPane.setBorder(new EmptyBorder(6, 6, 3, 3));
        mainPane.add(positionPane);

        // Prepare latitude entry components
        JLabel latLabel = new JLabel("Latitude: ");
        latLabel.setAlignmentY(.5F);
        positionPane.add(latLabel);

        latText = new JTextField(5);
        positionPane.add(latText);

        String[] latStrings = { "N", "S" };
        latDir = new JComboBox(latStrings);
        latDir.setEditable(false);
        latDir.setPreferredSize(new Dimension(45, -1));
        positionPane.add(latDir);

        // Put glue and strut spacers in
        positionPane.add(Box.createHorizontalGlue());
        positionPane.add(Box.createHorizontalStrut(5));

        // Prepare longitude entry components
        JLabel longLabel = new JLabel("Longitude: ");
        longLabel.setAlignmentY(.5F);
        positionPane.add(longLabel);

        longText = new JTextField(5);
        positionPane.add(longText);

        String[] longStrings = { "E", "W" };
        longDir = new JComboBox(longStrings);
        longDir.setEditable(false);
        longDir.setPreferredSize(new Dimension(45, -1));
        positionPane.add(longDir);

        // Put glue and strut spacers in
        positionPane.add(Box.createHorizontalGlue());
        positionPane.add(Box.createHorizontalStrut(5));

        // Prepare location entry submit button
        goThere = new JButton("Go There");
        goThere.addActionListener(this);
        goThere.setAlignmentY(.5F);
        positionPane.add(goThere);

        // Pack window
        pack();
    }

    /** Update coordinates in map, buttons, and globe
      *  Redraw map and globe if necessary 
      *  @param newCoords the new center location
      */
    public void updateCoords(Coordinates newCoords) {
        navButtons.updateCoords(newCoords);
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
                    	if (latDirStr.equals("N")) latitude = 90D - latitude;
	                    else latitude += 90D;
        	            if (longitude > 0D) {
            	            if (longDirStr.equals("W")) longitude = 360D - longitude;
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
        		//usgsItem.setEnabled(false);
        	}	 
        	else {
        		map.setMapType(SurfMarsMap.TYPE);
        		globeNav.showSurf();
        		legend.showMap();
        		//usgsItem.setEnabled(true);
        	} 
        }
        else if (source == usgsItem) {
        	if (usgsItem.isSelected()) map.setMapType(USGSMarsMap.TYPE);
        	else map.setMapType(SurfMarsMap.TYPE);
        	globeNav.setUSGSMap(usgsItem.isSelected());
        	legend.setUSGSMode(usgsItem.isSelected());
        	legend.showMap();
        	topoItem.setEnabled(!usgsItem.isSelected());
        } 
        else if (source == dayNightItem) {
        	setMapLayer(dayNightItem.isSelected(), shadingLayer);
        	globeNav.setDayNightTracking(dayNightItem.isSelected());
        }
        else if (source == unitLabelItem) setMapLayer(unitLabelItem.isSelected(), unitLabelLayer);
        else if (source == trailItem) setMapLayer(trailItem.isSelected(), trailLayer);
        else if (source == landmarkItem) setMapLayer(landmarkItem.isSelected(), landmarkLayer);
        else if (source == navpointItem) setMapLayer(navpointItem.isSelected(), navpointLayer);
        else if (source == exploredSiteItem) setMapLayer(exploredSiteItem.isSelected(), exploredSiteLayer);
        else if (source == mineralItem) {
        	setMapLayer(mineralItem.isSelected(), mineralLayer);
        	mineralsButton.setEnabled(mineralItem.isSelected());
        }
    }
    
    /**
     * Sets a map layer on or off.
     * @param setMap true if map is on and false if off.
     * @param mapLayer the map layer.
     */
    private void setMapLayer(boolean setMap, MapLayer mapLayer) {
    	if (setMap) map.addMapLayer(mapLayer);
    	else map.removeMapLayer(mapLayer);
    }
    
    /**
     * Create the map options menu.
     */
    private void createOptionsMenu() {
    	// Create options menu.
		optionsMenu = new JPopupMenu("Map Options");
		
		// Create topographical map menu item.
		topoItem = new JCheckBoxMenuItem("Topographical Mode", TopoMarsMap.TYPE.equals(map.getMapType()));
		topoItem.addActionListener(this);
		optionsMenu.add(topoItem);
		
		// Create unit label menu item.
		unitLabelItem = new JCheckBoxMenuItem("Show Unit Labels", map.hasMapLayer(unitLabelLayer));
		unitLabelItem.addActionListener(this);
		optionsMenu.add(unitLabelItem);
		
		// Create day/night tracking menu item.
		dayNightItem = new JCheckBoxMenuItem("Day/Night Tracking", map.hasMapLayer(shadingLayer));
		dayNightItem.addActionListener(this);
		optionsMenu.add(dayNightItem);
		
		// Create USGS menu item.
		usgsItem = new JCheckBoxMenuItem("8x Surface Map Zoom", USGSMarsMap.TYPE.equals(map.getMapType()));
		usgsItem.addActionListener(this);
        // Disabling this option due to problems with USGS Map-a-planet website.
        usgsItem.setEnabled(false);
		optionsMenu.add(usgsItem);
		
		// Create vehicle trails menu item.
		trailItem = new JCheckBoxMenuItem("Show Vehicle Trails", map.hasMapLayer(trailLayer));
		trailItem.addActionListener(this);
		optionsMenu.add(trailItem);
		
		// Create landmarks menu item.
		landmarkItem = new JCheckBoxMenuItem("Show Landmarks", map.hasMapLayer(landmarkLayer));
		landmarkItem.addActionListener(this);
		optionsMenu.add(landmarkItem);
		
		// Create navpoints menu item.
		navpointItem = new JCheckBoxMenuItem("Show Mission Navpoints", map.hasMapLayer(navpointLayer));
		navpointItem.addActionListener(this);
		optionsMenu.add(navpointItem);
		
		// Create explored site menu item.
		exploredSiteItem = new JCheckBoxMenuItem("Show Explored Sites", map.hasMapLayer(exploredSiteLayer));
		exploredSiteItem.addActionListener(this);
		optionsMenu.add(exploredSiteItem);
		
		// Create minerals menu item.
		mineralItem = new JCheckBoxMenuItem("Show Minerals", map.hasMapLayer(mineralLayer));
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
    
    private class mapClickListener extends MouseAdapter {
    	public void mouseClicked(MouseEvent event) {

    		if (map.getCenterLocation() != null) {
    			double rho;
    			if (USGSMarsMap.TYPE.equals(map.getMapType())) rho = USGSMarsMap.PIXEL_RHO;
    			else rho = CannedMarsMap.PIXEL_RHO;

    			Coordinates clickedPosition = map.getCenterLocation().convertRectToSpherical(
    					(double)(event.getX() - (Map.DISPLAY_HEIGHT / 2) - 1),
    					(double)(event.getY() - (Map.DISPLAY_HEIGHT / 2) - 1), rho);
    			boolean unitsClicked = false;

    			Iterator<Unit> i = Simulation.instance().getUnitManager().getUnits().iterator();

    			// Open window if unit is clicked on the map
    			while (i.hasNext()) {
    				Unit unit = i.next();
    				UnitDisplayInfo displayInfo = UnitDisplayInfoFactory.getUnitDisplayInfo(unit);
    				if (displayInfo.isMapDisplayed(unit)) {
    					Coordinates unitCoords = unit.getCoordinates();
    					double clickRange = unitCoords.getDistance(clickedPosition);
    					double unitClickRange = displayInfo.getMapClickRange();
    					if (USGSMarsMap.TYPE.equals(map.getMapType())) unitClickRange *= .1257D;
    					if (clickRange < unitClickRange) {
    						openUnitWindow(unit);
    						unitsClicked = true;
    					}
                    }
                }
    			
    			if (!unitsClicked) updateCoords(clickedPosition);
    		}
        }
    }
    
    public void destroy() {
    	map.destroy();
    	globeNav.destroy();
    }
}
/**
 * Mars Simulation Project
 * NavigatorWindow.java
 * @version 2.76 2004-05-23
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard.tool.navigator;  
  
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.ui.standard.MainDesktopPane;
import org.mars_sim.msp.ui.standard.tool.ToolWindow;

/** 
 * The NavigatorWindow is a tool window that displays a map and a
 * globe showing Mars, and various other elements. It is the primary
 * interface component that presents the simulation to the user.
 */
public class NavigatorWindow extends ToolWindow implements ActionListener {

    // Data members
    private MapDisplay map; // map navigation
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
    private JCheckBoxMenuItem topoItem; //Topographical map menu item.
    private JCheckBoxMenuItem unitLabelItem; // Show unit labels menu item.
    private JCheckBoxMenuItem dayNightItem; // Day/night tracking menu item.
    private JCheckBoxMenuItem usgsItem; // Show USGS map mode menu item.
    private JCheckBoxMenuItem trailItem; // Show vehicle trails menu item.
    private JCheckBoxMenuItem landmarkItem; // Show landmarks menu item. 

    /** Constructs a NavigatorWindow object 
     *  @param desktop the desktop pane
     */
    public NavigatorWindow(MainDesktopPane desktop) {

        // use ToolWindow constructor
        super("Mars Navigator", desktop);

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
        Mars mars = desktop.getMainWindow().getMars();
        globeNav = new GlobeDisplay(150, 150, mars);
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
        map = new MapDisplay(this, 300, 300, mars);
        JPanel mapPane = new JPanel(new BorderLayout(0, 0));
        mapPane.setBorder( new CompoundBorder(new BevelBorder(BevelBorder.LOWERED),
                new LineBorder(Color.green)));
        rightTopPane.add(mapPane);
        JPanel mapPaneInner = new JPanel(new BorderLayout(0, 0));
        mapPaneInner.setBackground(Color.black);
        mapPaneInner.add(map, BorderLayout.CENTER);
        mapPane.add(mapPaneInner, BorderLayout.CENTER);

        // Put some glue in to fill in extra space
        rightTopPane.add(Box.createVerticalStrut(5));

        // Prepare topographical panel
        JPanel topoPane = new JPanel(new BorderLayout());
        topoPane.setBorder(new EmptyBorder(0, 3, 0, 0));
        mainPane.add(topoPane);

        // Prepare options panel
        JPanel optionsPane = new JPanel(new BorderLayout());
        topoPane.add(optionsPane, BorderLayout.CENTER);

		// Prepare options button.
		optionsButton = new JButton("Map Options");
		optionsButton.addActionListener(this);
		optionsPane.add(optionsButton, BorderLayout.NORTH);
	
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
        else if (source == optionsButton) {
        	if (optionsMenu == null) {
        		// Create options menu.
        		createOptionsMenu();
        		optionsMenu.show(optionsButton, 0, optionsButton.getHeight());
        	}
        	else optionsMenu.setVisible(true);
        }
        else if (source == topoItem) {
        	if (topoItem.isSelected()) {
        		map.showTopo();
        		globeNav.showTopo();
				legend.showColor();
        		dayNightItem.setEnabled(false);
        		usgsItem.setEnabled(false);
        	}	 
        	else {
        		map.showSurf();
        		globeNav.showSurf();
        		legend.showMap();
        		dayNightItem.setEnabled(true);
        		usgsItem.setEnabled(true);
        	} 
        }
        else if (source == unitLabelItem) map.setUnitLabels(unitLabelItem.isSelected());
        else if (source == dayNightItem) {
        	map.setDayNightTracking(dayNightItem.isSelected());
        	globeNav.setDayNightTracking(dayNightItem.isSelected());
        } 
        else if (source == usgsItem) {
        	map.setUSGSMap(usgsItem.isSelected());
        	globeNav.setUSGSMap(usgsItem.isSelected());
			legend.setUSGSMode(usgsItem.isSelected());
        	topoItem.setEnabled(!usgsItem.isSelected());
        } 
        else if (source == trailItem) map.setVehicleTrails(trailItem.isSelected());
        else if (source == landmarkItem) map.setLandmarks(landmarkItem.isSelected());
    }
    
    /**
     * Create the map options menu.
     */
    private void createOptionsMenu() {
    	// Create options menu.
		optionsMenu = new JPopupMenu("Map Options");
		
		// Create topographical map menu item.
		topoItem = new JCheckBoxMenuItem("Topographical Mode", map.isTopo());
		topoItem.addActionListener(this);
		optionsMenu.add(topoItem);
		
		// Create unit label menu item.
		unitLabelItem = new JCheckBoxMenuItem("Show Unit Labels", map.useUnitLabels());
		unitLabelItem.addActionListener(this);
		optionsMenu.add(unitLabelItem);
		
		// Create day/night tracking menu item.
		dayNightItem = new JCheckBoxMenuItem("Day/Night Tracking", map.isDayNightTracking());
		dayNightItem.addActionListener(this);
		optionsMenu.add(dayNightItem);
		
		// Create USGS menu item.
		usgsItem = new JCheckBoxMenuItem("8x Surface Map Zoom", map.isUsgs());
		usgsItem.addActionListener(this);
		optionsMenu.add(usgsItem);
		
		// Create vehicle trails menu item.
		trailItem = new JCheckBoxMenuItem("Show Vehicle Trails", map.isVehicleTrails());
		trailItem.addActionListener(this);
		optionsMenu.add(trailItem);
		
		// Create landmarks menu item.
		landmarkItem = new JCheckBoxMenuItem("Show Landmarks", map.isLandmarks());
		landmarkItem.addActionListener(this);
		optionsMenu.add(landmarkItem);
		
		optionsMenu.pack();
    }

    /** 
     * Opens a unit window on the desktop.
     *
     * @param unit the unit the window is for.
     */
    public void openUnitWindow(Unit unit) {
        desktop.openUnitWindow(unit);
    }

    /** accessor for the MapDisplay */
    public MapDisplay getMapDisplay() {
        return map;
    }
}
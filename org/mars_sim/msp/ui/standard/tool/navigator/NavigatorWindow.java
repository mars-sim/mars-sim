/**
 * Mars Simulation Project
 * NavigatorWindow.java
 * @version 2.75 2003-09-17
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
public class NavigatorWindow extends ToolWindow implements ActionListener,
        ItemListener {

    // Data members
    private MapDisplay map; // map navigation
    private GlobeDisplay globeNav; // Globe navigation
    private NavButtonDisplay navButtons; // Compass navigation buttons
    private LegendDisplay legend; // Topographical and distance legend
    private JCheckBox topoCheck; // Topographical view checkbox
    private JTextField latText; // Latitude entry
    private JTextField longText; // Longitude entry
    private JComboBox latDir; // Latitude direction choice
    private JComboBox longDir; // Longitude direction choice
    private JButton goThere; // Location entry submit button
    private JCheckBox unitLabelCheckbox; // Show unit labels checkbox
    private JCheckBox dayNightCheckbox; // Day/night tracking checkbox
    private JCheckBox usgsCheckbox; // Show USGS map mode
    private JCheckBox trailCheckbox; // Show vehicle trails

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
        mapPaneInner.add(map, "Center");
        mapPane.add(mapPaneInner, "Center");

        // Put some glue in to fill in extra space
        rightTopPane.add(Box.createVerticalStrut(5));

        // Prepare topographical panel
        JPanel topoPane = new JPanel(new BorderLayout());
        topoPane.setBorder(new EmptyBorder(0, 3, 0, 0));
        mainPane.add(topoPane);

        // Prepare checkbox panel
        JPanel checkBoxPane = new JPanel(new GridLayout(5, 1));
        topoPane.add(checkBoxPane, "West");

        // Prepare show topographical map checkbox
        topoCheck = new JCheckBox("Topographical Mode");
        topoCheck.addItemListener(this);
        checkBoxPane.add(topoCheck);

        // Prepare unit label mode checkbox
        unitLabelCheckbox = new JCheckBox("Show Unit Labels");
        unitLabelCheckbox.setSelected(true);
        unitLabelCheckbox.addItemListener(this);
        checkBoxPane.add(unitLabelCheckbox);

        // Prepare day/night checkbox
        dayNightCheckbox = new JCheckBox("Day/Night Tracking");
        dayNightCheckbox.setSelected(false);
        dayNightCheckbox.addItemListener(this);
        checkBoxPane.add(dayNightCheckbox);

        // Prepare USGS checkbox
        usgsCheckbox = new JCheckBox("8x Surface Map Zoom");
        usgsCheckbox.setSelected(false);
        usgsCheckbox.addItemListener(this);
        checkBoxPane.add(usgsCheckbox);

        // Prepare vehicle trails checkbox
        trailCheckbox = new JCheckBox("Show Vehicle Trails");
        trailCheckbox.setSelected(true);
        trailCheckbox.addItemListener(this);
        checkBoxPane.add(trailCheckbox);
	
        // Prepare legend icon
        legend = new LegendDisplay();
        legend.setBorder( new CompoundBorder(new BevelBorder(BevelBorder.LOWERED),
                new LineBorder(Color.green)));
        JPanel legendPanel = new JPanel(new BorderLayout(0, 0));
        legendPanel.add(legend, "North");
        topoPane.add(legendPanel, "East");

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

    /** Change topographical mode
     *  Redraw legend
     *  Redraw map and globe if necessary
     *  @param topoMode true if in topographical mode
     */
    public void updateTopo(boolean topoMode) {
        if (topoMode) {
            legend.showColor();
            globeNav.showTopo();
            map.showTopo();
            dayNightCheckbox.setEnabled(false);
            usgsCheckbox.setEnabled(false);
        } else {
            legend.showMap();
            globeNav.showSurf();
            map.showSurf();
            dayNightCheckbox.setEnabled(true);
            usgsCheckbox.setEnabled(true);
        }
    }
    
    /** Set USGS as surface map
     *  @param useUSGSMap true if using USGS map.
     */
    public void setUSGSMap(boolean useUSGSMap) {
    	globeNav.setUSGSMap(useUSGSMap);
    	map.setUSGSMap(useUSGSMap);
    	legend.setUSGSMode(useUSGSMap);
    	if (!topoCheck.isSelected()) legend.showMap();
    }

    /** ActionListener method overridden */
    public void actionPerformed(ActionEvent event) {

        // Read longitude and latitude from user input, translate to radians,
        // and recenter globe and surface map on that location.
        try {
            double latitude = ((Float) new Float(latText.getText())).doubleValue();
            double longitude = ((Float) new Float(longText.getText())).doubleValue();
            String latDirStr = (String) latDir.getSelectedItem();
            String longDirStr = (String) longDir.getSelectedItem();

            if ((latitude >= 0D) && (latitude <= 90D)) {
                if ((longitude >= 0D) && (longitude <= 180)) {
                    if (latDirStr.equals("N"))
                        latitude = 90D - latitude;
                    else
                        latitude += 90D;
                    if (longitude > 0D)
                        if (longDirStr.equals("W"))
                            longitude = 360D - longitude;
                    double phi = Math.PI * (latitude / 180D);
                    double theta = (2 * Math.PI) * (longitude / 360D);
                    updateCoords(new Coordinates(phi, theta));
                }
            }
        } catch (NumberFormatException e) {}
    }

    /** ItemListener method overridden */
    public void itemStateChanged(ItemEvent event) {
        Object object = event.getSource();

        if (object == topoCheck) {
            updateTopo(event.getStateChange() == ItemEvent.SELECTED);
        } 
        else if (object == unitLabelCheckbox) {
            map.setUnitLabels(unitLabelCheckbox.isSelected()); // Change map's label settings
        }
        else if (object == dayNightCheckbox) {
            globeNav.setDayNightTracking(dayNightCheckbox.isSelected());
            map.setDayNightTracking(dayNightCheckbox.isSelected());
        }
        else if (object == usgsCheckbox) {
            setUSGSMap(usgsCheckbox.isSelected());
        }
        else if (object == trailCheckbox) {
            map.setVehicleTrails(trailCheckbox.isSelected());
        }
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

/*
 * Mars Simulation Project
 * RoverDialog.java
 * @version 2.74 2002-03-17
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard;

import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.simulation.vehicle.*;
import org.mars_sim.msp.ui.standard.monitor.PersonTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;

/**
 * The RoverDialog class is an abstract detail window for a rover.
 * It displays information about the rover as well as its current status.
 * It is abstract and an appropriate detail window needs to be derived for
 * a particular type of rover.
 */
public abstract class RoverDialog extends GroundVehicleDialog implements MouseListener {

    // Data members
    protected Rover rover; // Vehicle detail window is about
    protected JList crewList; // List of passengers

    // Cached data members
    protected ArrayList crewInfo; // Cached list of crewmembers.

    /** Constructs a VehicleDialog object
     *  @param parentDesktop the desktop pane
     *  @param GroundVehicleUIProxy the vehicle's UI proxy
     */
    public RoverDialog(MainDesktopPane parentDesktop, GroundVehicleUIProxy groundVehicleUIProxy) {

        // Use GroundVehicleDialog constructor
        super(parentDesktop, groundVehicleUIProxy);
    }

    /** Initialize cached data members */
    protected void initCachedData() {
	super.initCachedData();
        crewInfo = new ArrayList();
    }

    /** Complete update (overridden) */
    protected void generalUpdate() {
	super.generalUpdate();
        updateCrew();
    }

    /** Implement MouseListener Methods */
    public void mouseClicked(MouseEvent event) {
        Object object = event.getSource();
        if (object == crewList) {
            if (event.getClickCount() >= 2) {
                if (crewList.locationToIndex(event.getPoint()) > -1) {
                    if ((crewList.getSelectedValue() != null) &&
                            !((String) crewList.getSelectedValue()).equals(" ")) {
                        UnitUIProxy personProxy = (UnitUIProxy) crewInfo.get(
                                crewList.getSelectedIndex());
                        try { parentDesktop.openUnitWindow(personProxy); }
                        catch (NullPointerException e) {}
                    }
                }
            }
        }
    }
    public void mousePressed(MouseEvent event) {}
    public void mouseReleased(MouseEvent event) {}
    public void mouseEntered(MouseEvent event) {}
    public void mouseExited(MouseEvent event) {}

    /** Prepare and add components to window */
    protected void setupComponents() {

        super.setupComponents();

        // Initialize rover
        rover = (Rover) parentUnit;

	// Replace driver tab with new crew tab.
        tabPane.remove(tabPane.indexOfTab("Driver"));
	tabPane.addTab("Crew", setupCrewPane());
    }

    /** Set up crew pane
     *  @return crew pane
     */
    protected JPanel setupCrewPane() {

        // Prepare crew pane
        JPanel crewPane = new JPanel();
        crewPane.setBorder(
                new CompoundBorder(new EtchedBorder(), new EmptyBorder(5, 5, 5, 5)));
        crewPane.setLayout(new BoxLayout(crewPane, BoxLayout.Y_AXIS));

        // Prepare maximum crew capacity pane
        JPanel maxCrewPane = new JPanel(new BorderLayout());
        maxCrewPane.setBorder(
                new CompoundBorder(new EtchedBorder(), new EmptyBorder(5, 5, 5, 5)));
        crewPane.add(maxCrewPane);

        // Prepare maximum crew capacity label
        int maxCrew = ((Crewable) vehicle).getCrewCapacity();
        JLabel maxCrewLabel = new JLabel("Maximum Crew Capacity: " + maxCrew, JLabel.CENTER);
        maxCrewPane.add(maxCrewLabel, "Center");

        // Prepare driver pane
        JPanel driverPane = new JPanel(new BorderLayout());
        driverPane.setBorder(
                new CompoundBorder(new EtchedBorder(), new EmptyBorder(5, 5, 5, 5)));
        crewPane.add(driverPane);

        // Prepare driver label
        JLabel driverLabel = new JLabel("Driver", JLabel.CENTER);
        driverPane.add(driverLabel, "North");

        // Prepare driver button pane
        driverButtonPane = new JPanel();
        driverPane.add(driverButtonPane, "Center");

        // Prepare driver button
        driverButton = new JButton();
        driverButton.setMargin(new Insets(1, 1, 1, 1));
        driverButton.addActionListener(this);

        if (vehicle.getSpeed() != 0D) {
            driverButton.setText(vehicle.getDriver().getName());
            driverButtonPane.add(driverButton);
        }

        // Prepare crew list pane
        JPanel crewListPane = new JPanel(new BorderLayout());
        crewListPane.setBorder(
                new CompoundBorder(new EtchedBorder(), new EmptyBorder(5, 5, 5, 5)));
        crewPane.add(crewListPane);

        // Prepare crew label
        JLabel peopleLabel = new JLabel("Crew", JLabel.CENTER);
        crewListPane.add(peopleLabel, "North");

        // Add monitor button
        JButton monitorButton = new JButton(ImageLoader.getIcon("Monitor"));
        monitorButton.setMargin(new Insets(1, 1, 1, 1));
        monitorButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        parentDesktop.addModel(new PersonTableModel(rover));
                    }
                });
        JPanel monitorPanel = new JPanel();
        monitorPanel.add(monitorButton);
        crewListPane.add(monitorPanel, "East");

        // Prepare crew list
        DefaultListModel crewListModel = new DefaultListModel();

        PersonIterator i = ((Crewable) vehicle).getCrew().iterator();
	while (i.hasNext()) {
	    Person person = i.next();
            PersonUIProxy tempCrew = (PersonUIProxy) proxyManager.getUnitUIProxy(person);
            crewInfo.add(tempCrew);
            crewListModel.addElement(person.getName());
        }

        // This prevents the list from sizing strange due to having no contents
        if (crewInfo.size() == 0) crewListModel.addElement(" ");

        crewList = new JList(crewListModel);
        crewList.setVisibleRowCount(7);
        crewList.addMouseListener(this);
        crewList.setPreferredSize(
                new Dimension(175, (int) crewList.getPreferredSize().getHeight()));
        JScrollPane crewScroll = new JScrollPane(crewList);
        JPanel crewScrollPane = new JPanel();
        crewScrollPane.add(crewScroll);
        crewListPane.add(crewScrollPane, "Center");

        // Return crew pane
        return crewPane;
    }

    /** Update crew info */
    protected void updateCrew() {
        boolean vehicleMoving = (rover.getStatus() == Vehicle.MOVING);

        // Update crew list
        DefaultListModel model = (DefaultListModel) crewList.getModel();
        boolean match = false;

        // Check if model matches crew
        PersonCollection crew = rover.getCrew();

        if (model.getSize() == crew.size()) {
            match = true;
	    PersonIterator i = crew.iterator();
	    int count = 0;
	    while (i.hasNext()) {
                String tempName = (String) model.getElementAt(count);
                if (!tempName.equals(i.next().getName())) match = false;
            }
        }

        // If no match, update crew list
        if (!match) {
            model.removeAllElements();
            crewInfo.clear();
	    PersonIterator i = crew.iterator();
	    while (i.hasNext()) {
                Person person = i.next();
                crewInfo.add(proxyManager.getUnitUIProxy(person));
                model.addElement(person.getName());
            }

            // This prevents the list from sizing strange due to having no contents
            if (crewInfo.size() == 0) model.addElement(" ");

            validate();
	}
    }
}

/**
 * Mars Simulation Project
 * CrewTabPanel.java
 * @version 2.75 2003-08-03
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard.unit_window.vehicle;

import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.simulation.vehicle.*;
import org.mars_sim.msp.ui.standard.*;
import org.mars_sim.msp.ui.standard.tool.monitor.PersonTableModel;
import org.mars_sim.msp.ui.standard.unit_window.TabPanel;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/** 
 * The CrewTabPanel is a tab panel for a vehicle's crew information.
 */
public class CrewTabPanel extends TabPanel implements MouseListener, ActionListener {
    
    private JLabel crewNumLabel;
    private JLabel crewCapLabel;
    private DefaultListModel crewListModel;
    private JList crewList;
    private PersonCollection crewCache;
    private int crewNumCache;
    private int crewCapacityCache;
    
    /**
     * Constructor
     *
     * @param vehicle the vehicle.
     * @param desktop the main desktop.
     */
    public CrewTabPanel(Vehicle vehicle, MainDesktopPane desktop) { 
        // Use the TabPanel constructor
        super("Crew", null, "Vehicle's Crew", vehicle, desktop);
        
        Crewable crewable = (Crewable) vehicle;
        
        // Create crew count panel
        JPanel crewCountPanel = new JPanel(new GridLayout(2, 1, 0, 0));
        crewCountPanel.setBorder(new MarsPanelBorder());
        topContentPanel.add(crewCountPanel);
        
        // Create crew num label
        crewNumCache = crewable.getCrewNum();
        crewNumLabel = new JLabel("Crew: " + crewNumCache, JLabel.CENTER);
        crewCountPanel.add(crewNumLabel);
        
        // Create crew capacity label
        crewCapacityCache = crewable.getCrewCapacity();
        crewCapLabel = new JLabel("Crew Capacity: " + crewCapacityCache, JLabel.CENTER);
        crewCountPanel.add(crewCapLabel);
        
        // Create crew display panel
        JPanel crewDisplayPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        crewDisplayPanel.setBorder(new MarsPanelBorder());
        topContentPanel.add(crewDisplayPanel);
        
        // Create scroll panel for crew list.
        JScrollPane crewScrollPanel = new JScrollPane();
        crewScrollPanel.setPreferredSize(new Dimension(175, 100));
        crewDisplayPanel.add(crewScrollPanel);
        
        // Create crew list model
        crewListModel = new DefaultListModel();
        crewCache = new PersonCollection(crewable.getCrew());
        PersonIterator i = crewCache.iterator();
        while (i.hasNext()) crewListModel.addElement(i.next());
        
        // Create crew list
        crewList = new JList(crewListModel);
        crewList.addMouseListener(this);
        crewScrollPanel.setViewportView(crewList);
        
        // Create crew monitor button
        JButton monitorButton = new JButton(ImageLoader.getIcon("Monitor"));
        monitorButton.setMargin(new Insets(1, 1, 1, 1));
        monitorButton.addActionListener(this);
        monitorButton.setToolTipText("Open tab in monitor tool");
        crewDisplayPanel.add(monitorButton);
    }
    
    /**
     * Updates the info on this panel.
     */
    public void update() {
        Vehicle vehicle = (Vehicle) unit;
        Crewable crewable = (Crewable) vehicle;
        
        // Update crew num
        if (crewNumCache != crewable.getCrewNum()) {
            crewNumCache = crewable.getCrewNum();
            crewNumLabel.setText("Crew: " + crewNumCache);
        }
        
        // Update crew capacity
        if (crewCapacityCache != crewable.getCrewCapacity()) {
            crewCapacityCache = crewable.getCrewCapacity();
            crewCapLabel.setText("Crew Capacity: " + crewCapacityCache);
        }
        
        // Update crew list
        if (!crewCache.equals(crewable.getCrew())) {
            crewCache = new PersonCollection(crewable.getCrew());
            crewListModel.clear();
            PersonIterator i = crewCache.iterator();
            while (i.hasNext()) crewListModel.addElement(i.next());
        }
        
    }
                
    /** 
     * Action event occurs.
     *
     * @param event the action event
     */
    public void actionPerformed(ActionEvent event) {
        // If the crew monitor button was pressed, create tab in monitor tool.
        Vehicle vehicle = (Vehicle) unit;
        Crewable crewable = (Crewable) vehicle;
        desktop.addModel(new PersonTableModel(crewable));
    }
    
    /** 
     * Mouse clicked event occurs.
     *
     * @param event the mouse event
     */
    public void mouseClicked(MouseEvent event) {

        // If double-click, open person window.
        if (event.getClickCount() >= 2) {
        	Person person = (Person) crewList.getSelectedValue();
        	if (person != null) desktop.openUnitWindow(person);
        }
    }

    public void mousePressed(MouseEvent event) {}
    public void mouseReleased(MouseEvent event) {}
    public void mouseEntered(MouseEvent event) {}
    public void mouseExited(MouseEvent event) {}
}

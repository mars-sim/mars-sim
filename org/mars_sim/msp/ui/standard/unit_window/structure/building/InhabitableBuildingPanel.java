/**
 * Mars Simulation Project
 * InhabitableBuildingPanel.java
 * @version 2.75 2003-04-05
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.standard.unit_window.structure.building;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.simulation.structure.building.function.LifeSupport;
import org.mars_sim.msp.ui.standard.MainDesktopPane;

/**
 * The InhabitableBuildingPanel class is a building function panel representing 
 * the inhabitants of a settlement building.
 */
public class InhabitableBuildingPanel extends BuildingFunctionPanel implements MouseListener {
    
    private LifeSupport inhabitable; // The inhabitable building.
    private DefaultListModel inhabitantListModel;
    private JList inhabitantList;
    private PersonCollection inhabitantCache;
    
    /**
     * Constructor
     *
     * @param inhabitable The inhabitable building this panel is for.
     * @param desktop The main desktop.
     */
    public InhabitableBuildingPanel(LifeSupport inhabitable, MainDesktopPane desktop) {
        
        // Use BuildingFunctionPanel constructor
        super(inhabitable.getBuilding() , desktop);
        
        // Initialize data members.
        this.inhabitable = inhabitable;
        
        // Set panel layout
        setLayout(new BorderLayout());
        
        // Create label panel
        JPanel labelPanel = new JPanel(new GridLayout(2, 1, 0, 0));
        add(labelPanel, BorderLayout.NORTH);
        
        // Create inhabitant label
        JLabel inhabitantLabel = new JLabel("Inhabitants", JLabel.CENTER);
        labelPanel.add(inhabitantLabel);
        
        // Create capacity label
        JLabel capacityLabel = new JLabel("Capacity: " + 
            inhabitable.getOccupantCapacity(), JLabel.CENTER);
        labelPanel.add(capacityLabel);
        
        // Create inhabitant list panel
        JPanel inhabitantListPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        add(inhabitantListPanel, BorderLayout.CENTER);
        
        // Create scroll panel for inhabitant list
        JScrollPane inhabitantScrollPanel = new JScrollPane();
        inhabitantScrollPanel.setPreferredSize(new Dimension(160, 60));
        inhabitantListPanel.add(inhabitantScrollPanel);
        
        // Create inhabitant list model
        inhabitantListModel = new DefaultListModel();
        inhabitantCache = inhabitable.getOccupants();
        PersonIterator i = inhabitantCache.iterator();
        while (i.hasNext()) inhabitantListModel.addElement(i.next());
        
        // Create inhabitant list
        inhabitantList = new JList(inhabitantListModel);
        inhabitantList.addMouseListener(this);
        inhabitantScrollPanel.setViewportView(inhabitantList);
    }
    
    /**
     * Update this panel
     */
    public void update() {
        
        // Update population list
        if (!inhabitantCache.equals(inhabitable.getOccupants())) {
            inhabitantCache = new PersonCollection(inhabitable.getOccupants());
            inhabitantListModel.clear();
            PersonIterator i = inhabitantCache.iterator();
            while (i.hasNext()) inhabitantListModel.addElement(i.next());
        }
    }
    
    /** 
     * Mouse clicked event occurs.
     *
     * @param event the mouse event
     */
    public void mouseClicked(MouseEvent event) {

        // If double-click, open person window.
        if (event.getClickCount() >= 2) 
            desktop.openUnitWindow((Person) inhabitantList.getSelectedValue());
    }

    public void mousePressed(MouseEvent event) {}
    public void mouseReleased(MouseEvent event) {}
    public void mouseEntered(MouseEvent event) {}
    public void mouseExited(MouseEvent event) {}
}

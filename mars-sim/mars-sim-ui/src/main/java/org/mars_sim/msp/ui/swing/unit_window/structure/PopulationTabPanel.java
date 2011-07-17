/**
 * Mars Simulation Project
 * PopulationTabPanel.java
 * @version 3.00 2010-08-10
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.unit_window.structure;

import org.apache.commons.collections.CollectionUtils;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.tool.monitor.PersonTableModel;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Collection;
import java.util.Iterator;

/** 
 * The PopulationTabPanel is a tab panel for population information.
 */
public class PopulationTabPanel extends TabPanel implements MouseListener, ActionListener {
    
    private JLabel populationNumLabel;
    private JLabel populationCapLabel;
    private DefaultListModel populationListModel;
    private JList populationList;
    private Collection<Person> populationCache;
    private int populationNumCache;
    private int populationCapacityCache;
    
    /**
     * Constructor
     *
     * @param unit the unit to display.
     * @param desktop the main desktop.
     */
    public PopulationTabPanel(Unit unit, MainDesktopPane desktop) { 
        // Use the TabPanel constructor
        super("Population", null, "Population of the settlement", unit, desktop);
        
        Settlement settlement = (Settlement) unit;
        
        // Create population count panel
        JPanel populationCountPanel = new JPanel(new GridLayout(2, 1, 0, 0));
        populationCountPanel.setBorder(new MarsPanelBorder());
        topContentPanel.add(populationCountPanel);
        
        // Create population num label
        populationNumCache = settlement.getCurrentPopulationNum();
        populationNumLabel = new JLabel("Population: " + populationNumCache, JLabel.CENTER);
        populationCountPanel.add(populationNumLabel);
        
        // Create population capacity label
        populationCapacityCache = settlement.getPopulationCapacity();
        populationCapLabel = new JLabel("Population Capacity: " + populationCapacityCache, JLabel.CENTER);
        populationCountPanel.add(populationCapLabel);
        
        // Create population display panel
        JPanel populationDisplayPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        populationDisplayPanel.setBorder(new MarsPanelBorder());
        topContentPanel.add(populationDisplayPanel);
        
        // Create scroll panel for population list.
        JScrollPane populationScrollPanel = new JScrollPane();
        populationScrollPanel.setPreferredSize(new Dimension(175, 250));
        populationDisplayPanel.add(populationScrollPanel);
        
        // Create population list model
        populationListModel = new DefaultListModel();
        populationCache = settlement.getInhabitants();
        Iterator<Person> i = populationCache.iterator();
        while (i.hasNext()) populationListModel.addElement(i.next());
        
        // Create population list
        populationList = new JList(populationListModel);
        populationList.addMouseListener(this);
        populationScrollPanel.setViewportView(populationList);
        
        // Create population monitor button
        JButton monitorButton = new JButton(ImageLoader.getIcon("Monitor"));
        monitorButton.setMargin(new Insets(1, 1, 1, 1));
        monitorButton.addActionListener(this);
        monitorButton.setToolTipText("Open tab in monitor tool");
        populationDisplayPanel.add(monitorButton);
    }
    
    /**
     * Updates the info on this panel.
     */
    public void update() {
        Settlement settlement = (Settlement) unit;
        
        // Update population num
        if (populationNumCache != settlement.getCurrentPopulationNum()) {
            populationNumCache = settlement.getCurrentPopulationNum();
            populationNumLabel.setText("Population: " + populationNumCache);
        }
        
        // Update population capacity
        if (populationCapacityCache != settlement.getPopulationCapacity()) {
            populationCapacityCache = settlement.getPopulationCapacity();
            populationCapLabel.setText("Population Capacity: " + populationCapacityCache);
        }
        
        // Update population list
        if (!CollectionUtils.isEqualCollection(populationCache, settlement.getInhabitants())) {
            populationCache = settlement.getInhabitants();
            populationListModel.clear();
            Iterator<Person> i = populationCache.iterator();
            while (i.hasNext()) populationListModel.addElement(i.next());
        }
        
    }
                
    /** 
     * Action event occurs.
     *
     * @param event the action event
     */
    public void actionPerformed(ActionEvent event) {
        // If the population monitor button was pressed, create tab in monitor tool.
        desktop.addModel(new PersonTableModel((Settlement) unit, false));
    }
    
    /** 
     * Mouse clicked event occurs.
     *
     * @param event the mouse event
     */
    public void mouseClicked(MouseEvent event) {

        // If double-click, open person window.
        if (event.getClickCount() >= 2) {
            Person person = (Person) populationList.getSelectedValue();
            if (person != null) {
                desktop.openUnitWindow(person, false);
            }
        }
    }

    public void mousePressed(MouseEvent event) {}
    public void mouseReleased(MouseEvent event) {}
    public void mouseEntered(MouseEvent event) {}
    public void mouseExited(MouseEvent event) {}
}

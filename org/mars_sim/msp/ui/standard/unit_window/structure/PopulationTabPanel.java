/**
 * Mars Simulation Project
 * PopulationTabPanel.java
 * @version 2.75 2003-05-11
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard.unit_window.structure;

import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.ui.standard.*;
import org.mars_sim.msp.ui.standard.monitor.PersonTableModel;
import org.mars_sim.msp.ui.standard.unit_window.TabPanel;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/** 
 * The PopulationTabPanel is a tab panel for population information.
 */
public class PopulationTabPanel extends TabPanel implements MouseListener, ActionListener {
    
    private JLabel populationNumLabel;
    private JLabel populationCapLabel;
    private DefaultListModel populationListModel;
    private JList populationList;
    private PersonCollection populationCache;
    private int populationNumCache;
    private int populationCapacityCache;
    
    /**
     * Constructor
     *
     * @param proxy the UI proxy for the unit.
     * @param desktop the main desktop.
     */
    public PopulationTabPanel(UnitUIProxy proxy, MainDesktopPane desktop) { 
        // Use the TabPanel constructor
        super("Population", null, "Population of the settlement", proxy, desktop);
        
        Settlement settlement = (Settlement) proxy.getUnit();
        
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
        populationScrollPanel.setPreferredSize(new Dimension(175, 100));
        populationDisplayPanel.add(populationScrollPanel);
        
        // Create population list model
        populationListModel = new DefaultListModel();
        populationCache = new PersonCollection(settlement.getInhabitants());
        PersonIterator i = populationCache.iterator();
        while (i.hasNext()) populationListModel.addElement(i.next());
        
        // Create population list
        populationList = new JList(populationListModel);
        populationList.addMouseListener(this);
        populationScrollPanel.setViewportView(populationList);
        
        // Create population monitor button
        JButton monitorButton = new JButton(ImageLoader.getIcon("Monitor"));
        monitorButton.setMargin(new Insets(1, 1, 1, 1));
        monitorButton.addActionListener(this);
        populationDisplayPanel.add(monitorButton);
    }
    
    /**
     * Updates the info on this panel.
     */
    public void update() {
        Settlement settlement = (Settlement) proxy.getUnit();
        
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
        if (!populationCache.equals(settlement.getInhabitants())) {
            populationCache = new PersonCollection(settlement.getInhabitants());
            populationListModel.clear();
            PersonIterator i = populationCache.iterator();
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
        Settlement settlement = (Settlement) proxy.getUnit();
        desktop.addModel(new PersonTableModel(settlement));
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
            UnitUIProxy proxy = desktop.getProxyManager().getUnitUIProxy(person);
            desktop.openUnitWindow(proxy);
        }
    }

    public void mousePressed(MouseEvent event) {}
    public void mouseReleased(MouseEvent event) {}
    public void mouseEntered(MouseEvent event) {}
    public void mouseExited(MouseEvent event) {}
}

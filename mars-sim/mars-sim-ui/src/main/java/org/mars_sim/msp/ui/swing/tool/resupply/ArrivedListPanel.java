/**
 * Mars Simulation Project
 * ArrivedListPanel.java
 * @version 3.02 2012-04-05
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.tool.resupply;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.events.HistoricalEvent;
import org.mars_sim.msp.core.events.HistoricalEventListener;
import org.mars_sim.msp.core.events.HistoricalEventManager;
import org.mars_sim.msp.core.interplanetary.transport.resupply.Resupply;
import org.mars_sim.msp.core.interplanetary.transport.resupply.ResupplyEvent;
import org.mars_sim.msp.core.interplanetary.transport.resupply.ResupplyManager;

/**
 * A panel showing a list of all arrived resupply missions.
 */
public class ArrivedListPanel extends JPanel implements ListSelectionListener {

    // Data members
    private JList arrivedList;
    private ArrivedListModel listModel;
    
    /**
     * Constructor
     */
    public ArrivedListPanel() {
        
        // Use JPanel constructor
        super();
        
        setLayout(new BorderLayout());
        setBorder(new TitledBorder("Arrived Resupplies"));
        setPreferredSize(new Dimension(200, 200));
        
        // Create arrived list.
        listModel = new ArrivedListModel();
        arrivedList = new JList(listModel);
        arrivedList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(arrivedList);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    /**
     * Gets the arrived resupply list.
     * @return the arrived resupply list.
     */
    JList getArrivedList() {
        return arrivedList;
    }
    
    @Override
    public void valueChanged(ListSelectionEvent evt) {
        if (evt.getValueIsAdjusting()) {
            JList incomingList = (JList) evt.getSource();
            if (incomingList.getSelectedValue() != null) {
                arrivedList.clearSelection();
            }
        }
    }
    
    /**
     * Prepare panel for deletion.
     */
    public void destroy() {
        listModel.destroy();
    }
    
    /**
     * Inner class for the arrived resupply list model.
     */
    private class ArrivedListModel extends AbstractListModel implements 
            HistoricalEventListener {

        // Data members.
        private List<Resupply> resupplyList;
        
        private ArrivedListModel() {
            
            ResupplyManager manager = Simulation.instance().getResupplyManager();
            resupplyList = manager.getDeliveredResupplies();
            Collections.sort(resupplyList);
            
            // Register as historical event listener.
            Simulation.instance().getEventManager().addListener(this);
        }
        
        @Override
        public Object getElementAt(int index) {
            Object result = null;
            if ((index > -1) && (index < resupplyList.size())) {
                result = resupplyList.get(index);
            }
            
            return result;
        }

        @Override
        public int getSize() {
            return resupplyList.size();
        }
        
        @Override
        public void eventAdded(int index, HistoricalEvent event) {
            if (event.getCategory().equals(HistoricalEventManager.SUPPLY)) {
                Resupply resupply = (Resupply) event.getSource();
                
                if (ResupplyEvent.RESUPPLY_ARRIVED.equals(event.getType())) {
                    resupplyList.add(resupply);
                    Collections.sort(resupplyList);
                    int resupplyIndex = resupplyList.indexOf(resupply);
                    fireIntervalAdded(this, resupplyIndex, resupplyIndex);
                }
            }
        }

        @Override
        public void eventsRemoved(int startIndex, int endIndex) {
            // Do Nothing
        }
        
        /**
         * Prepares the list for deletion.
         */
        public void destroy() {
            resupplyList.clear();
            resupplyList = null;
            Simulation.instance().getEventManager().removeListener(this);
        }
    }
}
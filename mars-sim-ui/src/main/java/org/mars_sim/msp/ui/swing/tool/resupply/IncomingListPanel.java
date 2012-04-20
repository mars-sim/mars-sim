/**
 * Mars Simulation Project
 * IncomingListPanel.java
 * @version 3.02 2012-04-15
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
 * A panel showing a list of all incoming resupply missions.
 */
public class IncomingListPanel extends JPanel implements ListSelectionListener {

    // Data members
    private JList incomingList;
    private IncomingListModel listModel;
    
    /**
     * Constructor
     */
    public IncomingListPanel() {
        
        // Use JPanel constructor
        super();
        
        setLayout(new BorderLayout());
        setBorder(new TitledBorder("Incoming Resupplies"));
        setPreferredSize(new Dimension(225, 200));
        
        // Create incoming list.
        listModel = new IncomingListModel();
        incomingList = new JList(listModel);
        incomingList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(incomingList);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    /**
     * Gets the incoming resupply list.
     * @return the incoming resupply list.
     */
    JList getIncomingList() {
        return incomingList;
    }
    
    @Override
    public void valueChanged(ListSelectionEvent evt) {
        if (evt.getValueIsAdjusting()) {
            JList arrivedList = (JList) evt.getSource();
            if (arrivedList.getSelectedValue() != null) {
                incomingList.clearSelection();
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
     * Inner class for the incoming resupply list model.
     */
    private class IncomingListModel extends AbstractListModel implements 
            HistoricalEventListener {

        // Data members.
        private List<Resupply> resupplyList;
        
        private IncomingListModel() {
            
            ResupplyManager manager = Simulation.instance().getResupplyManager();
            resupplyList = manager.getIncomingResupplies();
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
                
                if (ResupplyEvent.RESUPPLY_CREATED.equals(event.getType())) {
                    if (Resupply.PLANNED.equals(resupply.getState()) || 
                            Resupply.IN_TRANSIT.equals(resupply.getState())) {
                        resupplyList.add(resupply);
                        Collections.sort(resupplyList);
                        int resupplyIndex = resupplyList.indexOf(resupply);
                        fireIntervalAdded(this, resupplyIndex, resupplyIndex);
                    }
                }
                else if (ResupplyEvent.RESUPPLY_ARRIVED.equals(event.getType()) || 
                        ResupplyEvent.RESUPPLY_CANCELLED.equals(event.getType())) {
                    int resupplyIndex = resupplyList.indexOf(resupply);
                    resupplyList.remove(resupply);
                    fireIntervalRemoved(this, resupplyIndex, resupplyIndex);
                }
                else if (ResupplyEvent.RESUPPLY_MODIFIED.equals(event.getType())) {
                    if (resupplyList.contains(resupply)) {
                        Collections.sort(resupplyList);
                        fireContentsChanged(this, 0, resupplyList.size() - 1);
                    }
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
/**
 * Mars Simulation Project
 * IncomingListPanel.java
 * @version 3.04 2013-04-14
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
import org.mars_sim.msp.core.interplanetary.transport.TransportEvent;
import org.mars_sim.msp.core.interplanetary.transport.TransportManager;
import org.mars_sim.msp.core.interplanetary.transport.Transportable;

/**
 * A panel showing a list of all incoming transport items.
 */
public class IncomingListPanel extends JPanel implements ListSelectionListener {

    // Data members
    private JList<Transportable> incomingList;
    private IncomingListModel listModel;

    /**
     * Constructor
     */
    public IncomingListPanel() {

        // Use JPanel constructor
        super();

        setLayout(new BorderLayout());
        setBorder(new TitledBorder("Incoming Transport Items"));
        setPreferredSize(new Dimension(225, 200));

        // Create incoming list.
        listModel = new IncomingListModel();
        incomingList = new JList<Transportable>(listModel);
        incomingList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(incomingList);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * Gets the incoming resupply list.
     * @return the incoming resupply list.
     */
    JList<Transportable> getIncomingList() {
        return incomingList;
    }

    @Override
    public void valueChanged(ListSelectionEvent evt) {
        if (evt.getValueIsAdjusting()) {
            JList<Transportable> arrivedList = (JList<Transportable>) evt.getSource();
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
    private class IncomingListModel extends AbstractListModel<Transportable> implements 
            HistoricalEventListener {

        // Data members.
        private List<Transportable> transportList;

        private IncomingListModel() {

            TransportManager manager = Simulation.instance().getTransportManager();
            transportList = manager.getIncomingTransportItems();
            Collections.sort(transportList);

            // Register as historical event listener.
            Simulation.instance().getEventManager().addListener(this);
        }

        @Override
        public Transportable getElementAt(int index) {
            Transportable result = null;
            if ((index > -1) && (index < transportList.size())) {
                result = transportList.get(index);
            }

            return result;
        }

        @Override
        public int getSize() {
            return transportList.size();
        }

        @Override
        public void eventAdded(int index, HistoricalEvent event) {
            if (event.getCategory().equals(HistoricalEventManager.TRANSPORT)) {
                Transportable transportItem = (Transportable) event.getSource();

                if (TransportEvent.TRANSPORT_ITEM_CREATED.equals(event.getType())) {
                    if (Transportable.PLANNED.equals(transportItem.getTransitState()) || 
                            Transportable.IN_TRANSIT.equals(transportItem.getTransitState())) {
                        transportList.add(transportItem);
                        Collections.sort(transportList);
                        int transportIndex = transportList.indexOf(transportItem);
                        fireIntervalAdded(this, transportIndex, transportIndex);
                    }
                }
                else if (TransportEvent.TRANSPORT_ITEM_ARRIVED.equals(event.getType()) || 
                        TransportEvent.TRANSPORT_ITEM_CANCELLED.equals(event.getType())) {
                    int transportIndex = transportList.indexOf(transportItem);
                    transportList.remove(transportItem);
                    fireIntervalRemoved(this, transportIndex, transportIndex);
                }
                else if (TransportEvent.TRANSPORT_ITEM_MODIFIED.equals(event.getType())) {
                    if (transportList.contains(transportItem)) {
                        Collections.sort(transportList);
                        fireContentsChanged(this, 0, transportList.size() - 1);
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
            transportList.clear();
            transportList = null;
            Simulation.instance().getEventManager().removeListener(this);
        }
    }
}
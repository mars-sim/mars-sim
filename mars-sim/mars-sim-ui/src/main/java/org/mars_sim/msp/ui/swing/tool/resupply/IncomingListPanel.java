/**
 * Mars Simulation Project
 * IncomingListPanel.java
 * @version 3.02 2012-04-15
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.tool.resupply;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;

import org.mars_sim.msp.core.interplanetary.transport.resupply.Resupply;

/**
 * A panel showing a list of all incoming resupply missions.
 */
public class IncomingListPanel extends JPanel {

    // Data members
    private JList incomingList;
    
    /**
     * Constructor
     */
    public IncomingListPanel() {
        
        // Use JPanel constructor
        super();
        
        setLayout(new BorderLayout());
        setBorder(new TitledBorder("Incoming Resupplies"));
        setPreferredSize(new Dimension(200, 200));
        
        // Create incoming list.
        incomingList = new JList(new IncomingListModel());
        incomingList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(incomingList), BorderLayout.CENTER);
    }
    
    private class IncomingListModel extends AbstractListModel {

        // Data members.
        private List<Resupply> resupplyList;
        
        private IncomingListModel() {
            resupplyList = new ArrayList<Resupply>();
            
            
        }
        
        @Override
        public Object getElementAt(int index) {
            Object result = null;
            if ((index > -1) && (index < resupplyList.size())) {
                StringBuffer buff = new StringBuffer();
                Resupply resupply = resupplyList.get(index);
                buff.append(resupply.getSettlement().getName());
                buff.append(": ");
                buff.append(resupply.getArrivalDate().getDateString());
                result = buff.toString();
            }
            
            return result;
        }

        @Override
        public int getSize() {
            return resupplyList.size();
        }
    }
}
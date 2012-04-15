/**
 * Mars Simulation Project
 * ArrivedListPanel.java
 * @version 3.02 2012-04-05
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.tool.resupply;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.AbstractListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;

/**
 * A panel showing a list of all arrived resupply missions.
 */
public class ArrivedListPanel extends JPanel {

    // Data members
    private JList arrivedList;
    
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
        arrivedList = new JList(new ArrivedListModel());
        arrivedList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(arrivedList), BorderLayout.CENTER);
    }
    
    private class ArrivedListModel extends AbstractListModel {

        @Override
        public Object getElementAt(int arg0) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public int getSize() {
            // TODO Auto-generated method stub
            return 0;
        }
    }
}
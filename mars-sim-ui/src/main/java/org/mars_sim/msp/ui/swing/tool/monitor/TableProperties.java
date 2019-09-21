/**
 * Mars Simulation Project
 * TableProperties.java
 * @version 3.1.0 2017-02-03
 * @author Barry Evans
 */

package org.mars_sim.msp.ui.swing.tool.monitor;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.swing.JCheckBox;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.WindowConstants;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;

/**
 * The TableProperties class display the columns of a specific table.
 * The columns can be either display or hidden in the target table.
 */
@SuppressWarnings("serial")
class TableProperties extends JInternalFrame {

    // Data members
	/** True if window is open. */
	protected boolean opened;

	/** Table to change. */
    private TableColumnModel model;
    /** Checkboxes. */
    private ArrayList<JCheckBox> columnButtons = new ArrayList<JCheckBox>();

    /**
     * Constructs a MonitorPropsDialog class.
     * @param title The name of the specified model
     * @param table the table to configure
     * @param desktop the main desktop.
     */
    public TableProperties(String title, JTable table,
                              MainDesktopPane desktop) {

        // Use JInternalFrame constructor
        super(title + " Properties", false, true);

        // Initialize data members
        this.model = table.getColumnModel();

        // Prepare content pane
        JPanel mainPane = new JPanel();
        mainPane.setLayout(new BorderLayout());
        mainPane.setBorder(MainDesktopPane.newEmptyBorder());
        setContentPane(mainPane);

        // Create column pane
        JPanel columnPane = new JPanel(new GridLayout(0, 1));
        columnPane.setBorder(new MarsPanelBorder());

        // Create a checkbox for each column in the model
        TableModel dataModel = table.getModel();
        for(int i = 0; i < dataModel.getColumnCount(); i++) {
            String name = dataModel.getColumnName(i);
            JCheckBox column = new JCheckBox(name);
            column.setSelected(false);

            column.addActionListener(
            	new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        columnSelected(event);
                    }
                }
            );
            columnButtons.add(column);
            columnPane.add(column);
        }

        // Selected if column is visible
        Enumeration<?> en = model.getColumns();
        while(en.hasMoreElements()) {
            int selected = ((TableColumn)en.nextElement()).getModelIndex();
            JCheckBox columnButton = columnButtons.get(selected);
            columnButton.setSelected(true);
        }

        mainPane.add(columnPane, BorderLayout.CENTER);
        
        setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        
        pack();
        
        desktop.add(this);
        
//        // Add to its own tab pane
//        if (desktop.getMainScene() != null)
//        	desktop.add(this);
//        	//desktop.getMainScene().getDesktops().get(0).add(this);
//        else 
//        	desktop.add(this);
    }


	/**
	 * Checks if the tool window has previously been opened.
	 * @return true if tool window has previously been opened.
	 */
	public boolean wasOpened() {
		return opened;
	}

	/**
	 * Sets if the window has previously been opened.
	 * @param opened true if previously opened.
	 */
	public void setWasOpened(boolean opened) {
		this.opened = opened;
	}

    /**
     * The user has selected a column in the dialog.
     *
     * @param event Event driving the action.
     */
    private void columnSelected(ActionEvent event) {
        JCheckBox box = (JCheckBox)event.getSource();
        int index = columnButtons.indexOf(box);

        // Either add or remove column
        if (box.isSelected()) {
            TableColumn col = new TableColumn(index);
            col.setHeaderValue(box.getText());
            model.addColumn(col);
        }
        else {
            TableColumn col = null;
            Enumeration<?> en = model.getColumns();
            while((col == null) && en.hasMoreElements()) {
                TableColumn next = (TableColumn)en.nextElement();
                if (next.getModelIndex() == index) {
                    col = next;
                }
            }
            model.removeColumn(col);
        }
    }
}
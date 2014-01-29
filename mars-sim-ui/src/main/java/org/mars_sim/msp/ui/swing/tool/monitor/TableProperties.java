/**
 * Mars Simulation Project
 * MonitorPropsDialog.java
 * @version 3.06 2014-01-29
 * @author Barry Evans
 */

package org.mars_sim.msp.ui.swing.tool.monitor;

import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Enumeration;

/**
 * The MonitorPropsDialog class display the columns of a specific table.
 * The columns can be either display or hidden in the target table.
 */
class TableProperties extends JInternalFrame {

    // Data members
    private TableColumnModel model; // Table to change
    private ArrayList<JCheckBox> columnButtons = new ArrayList<JCheckBox>(); // Check boxes

    /** Constructs a MonitorPropsDialog class
     *  @param title The name of the specified model
     *  @param table the table to configure
     *  @param desktop the main desktop.
     */
    public TableProperties(String title, JTable table,
                              MainDesktopPane main) {

        // Use JInternalFrame constructor
        super(title + " Properties", false, true);

        // Initialize data members
        this.model = table.getColumnModel();

        // Prepare content pane
        JPanel mainPane = new JPanel();
        mainPane.setLayout(new BorderLayout());
        mainPane.setBorder(new EmptyBorder(5, 5, 5, 5));
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

            column.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        columnSelected(event);
                    }
                });
            columnButtons.add(column);
            columnPane.add(column);
        }

        // Selected if column is visible
        Enumeration en = model.getColumns();
        while(en.hasMoreElements()) {
            int selected = ((TableColumn)en.nextElement()).getModelIndex();
            JCheckBox columnButton = columnButtons.get(selected);
            columnButton.setSelected(true);
        }

        mainPane.add(columnPane, "Center");
        pack();
        main.add(this);
    }

    /**
     * The user has selected a column in the dialog.
     *
     * @param event Event driving the action.
     */
    private void columnSelected(ActionEvent event) {
        JCheckBox button = (JCheckBox)event.getSource();
        int index = columnButtons.indexOf(button);

        // Either add or remove column
        if (button.isSelected()) {
            TableColumn col = new TableColumn(index);
            col.setHeaderValue(button.getText());
            model.addColumn(col);
        }
        else {
            TableColumn col = null;
            Enumeration en = model.getColumns();
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
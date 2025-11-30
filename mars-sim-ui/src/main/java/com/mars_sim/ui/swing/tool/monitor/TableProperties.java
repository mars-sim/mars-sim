/**
 * Mars Simulation Project
 * TableProperties.java
 * @version 3.2.0 2021-06-20
 * @author Barry Evans
 */

package com.mars_sim.ui.swing.tool.monitor;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.WindowConstants;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import com.mars_sim.ui.swing.MarsPanelBorder;
import com.mars_sim.ui.swing.StyleManager;

/**
 * The TableProperties class display the columns of a specific table.
 * The columns can be either display or hidden in the target table.
 */
@SuppressWarnings("serial")
class TableProperties extends JDialog {

    private TableColumnModel model;
    private List<JCheckBox> columnButtons = new ArrayList<>();

    /**
     * Constructs a MonitorPropsDialog class.
     * @param parent The parent frame
     * @param title The name of the specified model
     * @param table the table to configure
     */
    public TableProperties(Frame parent, String title, JTable table) {

        // Use JDialog constructor
        super(parent, title + " Properties", true); // true for modal

        // Initialize data members
        this.model = table.getColumnModel();

        // Prepare content pane
        JPanel mainPane = new JPanel();
        mainPane.setLayout(new BorderLayout());
        mainPane.setBorder(StyleManager.newEmptyBorder());
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

            column.addActionListener(this::columnSelected);
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
        pack();   
        
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(parent);        
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

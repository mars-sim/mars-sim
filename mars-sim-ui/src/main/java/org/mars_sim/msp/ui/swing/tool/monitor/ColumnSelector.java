/**
 * Mars Simulation Project
 * ColumnSelector.java
 * @version 2.81 2007-08-27
 * @author Barry Evans
 */

package org.mars_sim.msp.ui.swing.tool.monitor;

import org.mars_sim.msp.core.*;

import java.util.Vector;
import javax.swing.*;
import java.awt.Frame;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * This window displays a list of columns from the specified model.
 * The columns displayed depends upon the final chart being rendered.
 */
public class ColumnSelector extends JDialog {

    private final static String PIE_MESSAGE =
                        "Select a single column to display in the Pie chart";
    private final static String BAR_MESSAGE =
                        "Select a multiple columns to display in the Bar chart";

    // Data members
    private JList columnList = null; // Check boxes
    private int columnMappings[] = null;
    private boolean okPressed = false;

    /**
     * Constructs the dialog with a title and columns extracted from the
     * specified model.
     *
     * @param owner the owner of the dialog.
     * @param model Model driving the columns.
     * @param bar Display selection for a Bar chart.
     */
    public ColumnSelector(Frame owner, MonitorModel model, boolean bar) {
        super(owner, model.getName(), true);

        // Add all valid columns into the list
        Vector<String> items = new Vector<String>();
        columnMappings = new int[model.getColumnCount()-1];
        for(int i = 1; i < model.getColumnCount(); i++) {
            // If a valid column then add to model.
            Class columnType = model.getColumnClass(i);

            // If a bar, look for Number classes
            if (bar) {
                if (Number.class.isAssignableFrom(columnType)) {
                    columnMappings[items.size()] = i;
                    items.add(model.getColumnName(i));
                }
            }
            else if (isCategory(columnType)) {
                columnMappings[items.size()] = i;
                items.add(model.getColumnName(i));
            }
        }

        // Center pane
        JPanel centerPane = new JPanel(new BorderLayout());
        centerPane.setBorder(BorderFactory.createRaisedBevelBorder());
        columnList = new JList(items);
        if (bar) {
            columnList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            centerPane.add(new JLabel(BAR_MESSAGE), "North");
        }
        else {
            centerPane.add(new JLabel(PIE_MESSAGE), "North");
        }
        centerPane.add(new JScrollPane(columnList), "Center");

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent event) {
                            okPressed = true;
                            setVisible(false);
                        }
                    });
        buttonPanel.add(okButton);
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent event) {
                            setVisible(false);
                        }
                    });
        buttonPanel.add(cancelButton);

        // Package dialog
        JPanel pane = new JPanel(new BorderLayout());
        pane.add(centerPane, "Center");
        pane.add(buttonPanel, "South");
        getContentPane().add(pane);

        pack();
    }

    /**
     * Create a column selector popup for use with a Bar chart.
     *
     * @param window Parent frame.
     * @param model Model containing columns.
     * @return Array of column indexes to display.
     */
    public static int[] createBarSelector(Frame window,
                                          MonitorModel model) {
        ColumnSelector select = new ColumnSelector(window, model, true);
        select.setVisible(true);
        return select.getSelectedColumns();
    }

    /**
     * Create a column selector popup for a Pie chart.
     *
     * @param window Parent frame.
     * @param model Model containign columns.
     * @return Column index to use as category.
     */
    public static int createPieSelector(Frame window,
                                        MonitorModel model) {
        ColumnSelector select = new ColumnSelector(window, model, false);
        select.setVisible(true);
        int [] columns = select.getSelectedColumns();
        if (columns.length > 0) {
            return columns[0];
        }
        else {
            return -1;
        }
    }

    /**
     * Return the list of selected columns. The return is the index value
     * as described by the assoicated table model. The return array maybe
     * zero or more depending on selection and mode.
     *
     * @return Array of TableColumn indexes of selection.
     */
    public int[] getSelectedColumns() {
        int index[] = null;
        if (okPressed) {
            int selection[] = columnList.getSelectedIndices();
            index = new int[selection.length];

            for(int i = 0; i < selection.length; i++) {
                index[i] = columnMappings[selection[i]];
            }
        }
        else {
            index = new int[0];
        }

        return index;
    }

    /**
     * Is the class specified suitable to be displayed as a category dataset.
     * This is basically any class that is an Class and not a numerical
     * @param columnClass The Class of the data in the column.
     *
     * @return Is the class a category
     */
    private boolean isCategory(Class columnClass) {
        return (!Number.class.isAssignableFrom(columnClass) &&
                !Coordinates.class.equals(columnClass));
    }
}

/**
 * Mars Simulation Project
 * ColumnSelector.java
 * @version 2.72 2002-01-30
 * @author Barry Evans
 */

package org.mars_sim.msp.ui.standard.monitor;

import org.mars_sim.msp.simulation.*;
import java.util.Vector;
import javax.swing.*;
import javax.swing.border.*;
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

    // Data members
    private JList columnList = null; // Check boxes
    private int columnMappings[] = null;
    private boolean okPressed = false;

    /**
     * Constructs the dialog with a title and columns extracted from the
     * specified model.
     *
     * @param frame Owner of the dialog.
     * @param model Model driving the columns.
     */
    public ColumnSelector(Frame owner, UnitTableModel model) {
        super(owner, model.getName(), true);

        // Add all valid columns into the list
        Vector items = new Vector();
        columnMappings = new int[model.getColumnCount()-1];
        for(int i = 1; i < model.getColumnCount(); i++) {
            // If a valid column then add to model.
            if (isCategory(model.getColumnClass(i))) {
                columnMappings[items.size()] = i;
                items.add(model.getColumnName(i));
            }
        }

        // Center pane
        JPanel centerPane = new JPanel(new BorderLayout());
        centerPane.setBorder(BorderFactory.createRaisedBevelBorder());
        columnList = new JList(items);
        centerPane.add(new JLabel(PIE_MESSAGE), "North");
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

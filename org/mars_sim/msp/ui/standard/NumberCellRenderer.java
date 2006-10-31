/**
 * Mars Simulation Project
 * NumberCellRenderer.java
 * @version 2.75 2003-05-19
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * The NumberCellRenderer class renders table cells containing Number objects.
 */
public class NumberCellRenderer extends DefaultTableCellRenderer {
    
    private DecimalFormat decFormatter = new DecimalFormat("0.0");
    
    /**
     * Constructor
     */
    public NumberCellRenderer() {
        // Use DefaultTableCellRenderer constructor
        super();
        
        // Set the horizontal alignment to right.
        setHorizontalAlignment(SwingConstants.RIGHT);
    }

    /**
     * Sets the String object for the cell being rendered to value.
     *
     * @param value the string value for this cell; if value is 
     * null it sets the text value to an empty string.
     */
    public void setValue(Object value) {
        if (value != null) {
            if (value instanceof Double) 
                value = decFormatter.format(((Double) value).doubleValue());
            else if (value instanceof Float)
                value = decFormatter.format(((Float) value).floatValue());
            else if (value instanceof BigDecimal)
                value = decFormatter.format(((BigDecimal) value).doubleValue());
        } 
        super.setValue(value);
    } 
}

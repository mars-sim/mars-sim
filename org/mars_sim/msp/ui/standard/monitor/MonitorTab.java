/**
 * Mars Simulation Project
 * MonitorTab.java
 * @version 2.72 2002-01-18
 * @author Barry Evans
 */

package org.mars_sim.msp.ui.standard.monitor;

import org.mars_sim.msp.simulation.Unit;
import org.mars_sim.msp.ui.standard.MainDesktopPane;
import org.mars_sim.msp.ui.standard.UnitUIProxy;
import org.mars_sim.msp.ui.standard.UIProxyManager;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;

/**
 * This class represents an absraction of a view displayed in the Monitor Window.
 * The view is displayed inside a tab panel within the window and depends on
 * a UnitTableModel
 */
abstract class MonitorTab extends JPanel
{

    private UnitTableModel model;    // Mode providing the data
    private String name;
    private Icon icon;
    private boolean mandatory;

    /**
     * Create a view within a tab displaying the specified model.
     * @param model The model of Units to display.
     * @param mandatory This view is a mandatory view can can not be removed.
     * @param icon Iconic representation.
     */
    public MonitorTab(UnitTableModel model, boolean mandatory, Icon icon)
    {
        this.model = model;
        this.icon = icon;
        this.mandatory = mandatory;

        // Create a panel
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(5, 5, 5, 5));
    }

    /**
     * Remove this view.
     */
    public void removeTab() {
        model = null;
    }

    /**
     * Display details for selected rows
     */
    public void displayDetails(MainDesktopPane desktop)
    {
        UIProxyManager proxyManager = desktop.getProxyManager();
        ArrayList units = model.getUnits(getSelection());
        Iterator it = units.iterator();
        while(it.hasNext())
        {
            UnitUIProxy proxy = proxyManager.getUnitUIProxy((Unit)it.next());
            desktop.openUnitWindow(proxy);
        }
    }

    /**
     * Center the map on the first selected row.
     * @param desktop Main window of application.
     */
    public void centerMap(MainDesktopPane desktop)
    {
        ArrayList units = model.getUnits(getSelection());
        Iterator it = units.iterator();
        if (it.hasNext())
        {
            Unit unit = (Unit) it.next();
            desktop.centerMapGlobe(unit.getCoordinates());
        }
    }

    /**
     * Display property window controlling this view.
     */
    abstract public void displayProps(MainDesktopPane desktop);

    /**
     * This return the selected rows in the model that are current
     * selected in this view.
     *
     * @return array of row indexes.
     */
    abstract protected int[] getSelection();

    /**
     * Update the selected table
     */
    public void update()
    {
        model.update();
    }

    /**
     * Get the icon associated with this view.
     * @return Icon for this view
     */
    public Icon getIcon()
    {
        return icon;
    }

    /**
     * Get the associated model.
     * @return UnitTableModel associated to the tab.
     */
    public UnitTableModel getModel() {
        return model;
    }

    /**
     * Get the mandatory state of this view
     * @return Mandatory view.
     */
    public boolean getMandatory() {
        return mandatory;
    }
}

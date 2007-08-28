/**
 * Mars Simulation Project
 * UnitToolbar.java
 * @version 2.81 2007-08-27
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard;  
 
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JToolBar;
import javax.swing.border.BevelBorder;

import org.mars_sim.msp.simulation.Unit;

/** The UnitToolBar class is a UI toolbar for holding unit buttons.
 *  There should only be one instance and it is contained in the
 *  MainWindow instance.
 */
public class UnitToolBar extends JToolBar implements ActionListener {

    // Data members
    private List<UnitButton> unitButtons; // List of unit buttons
    private MainWindow parentMainWindow; // Main window that contains this toolbar.

    /** Constructs a UnitToolBar object 
     *  @param parentMainWindow the main window pane
     */
    public UnitToolBar(MainWindow parentMainWindow) {

        // Use JToolBar constructor
        super();

        // Initialize data members
        unitButtons = new ArrayList<UnitButton>();
        this.parentMainWindow = parentMainWindow;

        // Set name
        setName("Unit Toolbar");

        // Fix tool bar
        setFloatable(false);

        // Set preferred height to 57 pixels.
        setPreferredSize(new Dimension(0, 57));

        // Set border around toolbar
        setBorder(new BevelBorder(BevelBorder.RAISED));
    }

    /** 
     * Create a new unit button in the toolbar.
     *
     * @param unit the unit to make a button for.
     */
    public void createUnitButton(Unit unit) {

        // Check if unit button already exists
        boolean alreadyExists = false;
        Iterator<UnitButton> i = unitButtons.iterator();
        while (i.hasNext()) {
            UnitButton unitButton = i.next();
            if (unitButton.getUnit() == unit) alreadyExists = true;
        }

        if (!alreadyExists) {
            UnitButton tempButton = new UnitButton(unit);
            tempButton.addActionListener(this);
            add(tempButton);
            validate();
            repaint();
            unitButtons.add(tempButton);
        }
    }

    /** 
     * Disposes a unit button in toolbar.
     *
     * @param unit the unit whose button is to be removed.
     */
    public void disposeUnitButton(Unit unit) {
        Iterator<UnitButton> i = unitButtons.iterator();
        while (i.hasNext()) {
            UnitButton unitButton = i.next();
            if (unitButton.getUnit() == unit) {
                remove(unitButton);
                validate();
                repaint();
                i.remove();
            }
        }
    }

    /** ActionListener method overriden */
    public void actionPerformed(ActionEvent event) {
        // show unit window on desktop
        Unit unit = ((UnitButton) event.getSource()).getUnit();
        parentMainWindow.getDesktop().openUnitWindow(unit);
    }
}

/**
 * Mars Simulation Project
 * UnitToolbar.java
 * @version 2.70 2000-08-31
 * @author Scott Davis
 */

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;

/** The UnitToolBar class is a UI toolbar for holding unit buttons.
 *  There should only be one instance and it is contained in the
 *  MainWindow instance.
 */
public class UnitToolBar extends JToolBar implements ActionListener {

    private Vector unitButtons;          // List of unit buttons
    private MainWindow parentMainWindow; // Main window that contains this toolbar.
	
    public UnitToolBar(MainWindow parentMainWindow) {
	
	// Use JToolBar constructor
	super();
	
	// Initialize data members
	unitButtons = new Vector();
	this.parentMainWindow = parentMainWindow;
	
	// Set name
	setName("Unit Toolbar");
		
	// Fix tool bar
	setFloatable(false);
		
	// Set preferred height to 53 pixels.
	setPreferredSize(new Dimension(0, 57));
		
	// Set border around toolbar
	setBorder(new BevelBorder(BevelBorder.RAISED));
    }
	
    /** Create a new unit button in toolbar */
    public void createUnitButton(int unitID, String unitName, ImageIcon unitIcon) {
		
	// Check if unit button already exists
	boolean alreadyExists = false;
	for (int x=0; x < unitButtons.size(); x++) {
	    if (((UnitButton) unitButtons.elementAt(x)).getUnitID() == unitID) {
		alreadyExists = true;
	    }
	}
		
	if (!alreadyExists) {
	    UnitButton tempButton = new UnitButton(unitID, unitName, unitIcon);	
	    tempButton.addActionListener(this);
	    add(tempButton);
	    validate();
	    repaint();	
	    unitButtons.addElement(tempButton);
	}
    }
	
    /** Disposes a unit button in toolbar */
    public void disposeUnitButton(int unitID) {
	for (int x=0; x < unitButtons.size(); x++) {
	    UnitButton tempButton = (UnitButton) unitButtons.elementAt(x);
	    if (tempButton.getUnitID() == unitID) {
		unitButtons.removeElement(tempButton);
		remove(tempButton);
		validate();
		repaint();
	    }
	}
    }
		
    /** ActionListener method overriden */
    public void actionPerformed(ActionEvent event) {
	// show unit window on desktop
	parentMainWindow.openUnitWindow(((UnitButton) event.getSource()).getUnitID());
    }
}	

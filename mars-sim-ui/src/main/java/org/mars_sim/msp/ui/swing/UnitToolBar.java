/**
 * Mars Simulation Project
 * UnitToolbar.java
 * @version 3.1.0 2017-10-11
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JToolBar;

import org.mars_sim.msp.core.Unit;

/**
 * The UnitToolBar class is a UI toolbar for holding unit buttons. There should
 * only be one instance and it is contained in the MainWindow instance.
 */
@SuppressWarnings("serial")
public class UnitToolBar extends JToolBar implements ActionListener {

	public static final int HEIGHT = 57;
	
	private static final Color antiqueBronze = new Color(102,93,30,128);
	private static final Color almond = new Color(239,222,205,0);
	
	private static final Color cafeNoir = new Color(75,54,33,128);
	// maroon : 128,21,0; 
	// almond : 239,222,205;
	// Antique Bronze : 102,93,30
	// cafeNoir : 75,54,33
	
	// Data members
	private List<UnitButton> unitButtons; // List of unit buttons

	private MainWindow parentMainWindow; // Main window that contains this toolbar.

	/**
	 * Constructs a UnitToolBar object
	 * 
	 * @param parentMainWindow
	 *            the main window pane
	 */
	public UnitToolBar(MainWindow parentMainWindow) {

		// Use JToolBar constructor
		super();

		setBackground(cafeNoir);
		setOpaque(false);
		
		// Initialize data members
		unitButtons = new ArrayList<UnitButton>();
		this.parentMainWindow = parentMainWindow;

		// Set name
		setName("Unit Toolbar");

		// Fix tool bar
		setFloatable(false);

		// Set preferred height to 57 pixels.
		setPreferredSize(new Dimension(0, HEIGHT));

		// Set border around toolbar
//		setBorder(new BevelBorder(BevelBorder.RAISED));
//		setBorder(new LineBorder(Color.BLACK, 2));

	}


	/**
	 * Create a new unit button in the toolbar.
	 * 
	 * @param unit
	 *            the unit to make a button for.
	 */
	public void createUnitButton(Unit unit) {

		// Check if unit button already exists
		boolean alreadyExists = false;

		for (UnitButton unitButton : unitButtons) {
			if (unitButton.getUnit() == unit)
				alreadyExists = true;
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
	 * @param unit
	 *            the unit whose button is to be removed.
	 */
	public void disposeUnitButton(Unit unit) {
		Iterator<UnitButton> i = unitButtons.iterator();
		while (i.hasNext()) {
			UnitButton unitButton = i.next();

			unitButton.setBorderPainted(false);
			unitButton.setContentAreaFilled(false);

			if (unitButton.getUnit() == unit) {
				remove(unitButton);
				validate();
				repaint();
				i.remove();
			}
		}
	}

	/** ActionListener method overridden */
	public void actionPerformed(ActionEvent event) {
		// show unit window on desktop
		Unit unit = ((UnitButton) event.getSource()).getUnit();
		parentMainWindow.getDesktop().openUnitWindow(unit, false);
	}

	/**
	 * Gets all the units in the toolbar.
	 * 
	 * @return array of units.
	 */
	public Unit[] getUnitsInToolBar() {
		Unit[] result = new Unit[unitButtons.size()];
		for (int x = 0; x < unitButtons.size(); x++)
			result[x] = unitButtons.get(x).getUnit();
		return result;
	}

	@Override
	protected void addImpl(Component comp, Object constraints, int index) {
		super.addImpl(comp, constraints, index);
		if (comp instanceof JButton) {
			((JButton) comp).setContentAreaFilled(false);
		}
	}

	@Override
	protected JButton createActionComponent(Action a) {
		JButton jb = super.createActionComponent(a);
		jb.setOpaque(false);
		return jb;
	}

	@Override
	protected void paintComponent(Graphics g){
		super.paintComponent(g);
	    // Create the 2D copy
	    Graphics2D g2 = (Graphics2D)g.create();
	    
//	    Graphics2D bgr = bimage.createGraphics();
//	    bgr.setComposite(AlphaComposite.SRC);
	    
	    // Apply vertical gradient
	    g2.setPaint(new GradientPaint(0, 0, almond, 0, getHeight(), antiqueBronze, true));
	    g2.fillRect(0, 0, getWidth(), getHeight());

	    // Dipose of copy
	    g2.dispose();
	}
	
}
/**
 * Mars Simulation Project
 * EntityToolBar.java
 * @date 2025-12-02
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.entitywindow;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;

import com.mars_sim.core.Entity;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.unit_display_info.UnitDisplayInfoFactory;

/**
 * The UnitToolBar class is a UI toolbar for holding shortcut buttons for Entities. There should
 * only be one instance and it is contained in the MainWindow instance.
 */
@SuppressWarnings("serial")
public class EntityToolBar extends JToolBar {

	private static final int HEIGHT = 57;
	
	private static final Color ANTIQUE_BRONZE = new Color(102,93,30,128);
	private static final Color ALMOND = new Color(239,222,205,0);	
	private static final Color CAFE_NOIR = new Color(75,54,33,128);

	
	// Data members
	private UIContext context;
	private Map<Entity, JButton> entityButtonMap = new HashMap<>();

	/**
	 * Constructs a UnitToolBar object
	 * 
	 * @param context the UI context
	 */
	public EntityToolBar(UIContext context) {
		super();

		this.context = context;

		setBackground(CAFE_NOIR);
		setOpaque(false);
		setName("Unit Toolbar");
		setFloatable(false);

		// Set preferred height to 57 pixels.
		setPreferredSize(new Dimension(0, HEIGHT));
	}

	/**
	 * Create a new button in the toolbar.
	 * 
	 * @param entity The Entity whose button is to be created.
	 */
	public void createButton(Entity entity) {

		if (entityButtonMap.containsKey(entity)) {
			return;
		}

		var info = UnitDisplayInfoFactory.getUnitDisplayInfo(entity);
		var tempButton = new JButton(entity.getName(), info.getButtonIcon(entity));
		tempButton.setToolTipText(info.getSingularLabel());
		
		// Prepare default unit button values
		tempButton.setVerticalTextPosition(SwingConstants.BOTTOM);
		tempButton.setHorizontalTextPosition(SwingConstants.CENTER);
		tempButton.setAlignmentX(.5F);
		tempButton.setAlignmentY(.5F);
		tempButton.setContentAreaFilled(false);
		tempButton.setOpaque(false);
		tempButton.addActionListener(e -> context.showDetails(entity));

		entityButtonMap.put(entity,  tempButton);
		add(tempButton);
	}

	/**
	 * Disposes a button in toolbar.
	 * 
	 * @param entity the entity whose button is to be removed.
	 */
	public void disposeButton(Entity entity) {
		var oldButton = entityButtonMap.get(entity);

		if (oldButton != null) {
			entityButtonMap.remove(entity);
			remove(oldButton);
			repaint();
		}
	}

	@Override
	protected void paintComponent(Graphics g){
		super.paintComponent(g);
	    // Create the 2D copy
	    Graphics2D g2 = (Graphics2D)g.create();
	    
	    // Apply vertical gradient
	    g2.setPaint(new GradientPaint(0, 0, ALMOND, 0, getHeight(), ANTIQUE_BRONZE, true));
	    g2.fillRect(0, 0, getWidth(), getHeight());

	    // Dispose of copy
	    g2.dispose();
	}
}

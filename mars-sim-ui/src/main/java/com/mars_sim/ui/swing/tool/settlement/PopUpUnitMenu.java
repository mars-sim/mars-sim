/*
 * Mars Simulation Project
 * PopUpUnitMenu.java
 * @date 2021-11-28
 * @author Manny Kung
 */
package com.mars_sim.ui.swing.tool.settlement;

import java.awt.Color;
import java.awt.Font;
import java.util.function.Consumer;

import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import com.mars_sim.core.Entity;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.displayinfo.EntityDisplayInfoFactory;
import com.mars_sim.ui.swing.tool.settlement.UnitInfoPanel.UnitSummary;
import com.mars_sim.ui.swing.utils.SwingHelper;


class PopUpUnitMenu extends JPopupMenu {

	private static final long serialVersionUID = 1L;
	
	public static final int WIDTH_1 = 350;
	public static final int HEIGHT_1 = 300;

    public PopUpUnitMenu(final MapHotspot<?> selected, UIContext context){
		var entity = selected.target;
    	String unitType = EntityDisplayInfoFactory.getDisplayInfo(entity).getSingularLabel();

		add(unitType + ": " + entity.getName());
		addSeparator();

		// Add summary details for Entity
		UnitSummary summary = selected.getSummary();
		if (summary != null) {
			add(buildInfoItem(entity, summary));
		}    	
		
		// Standard Entity launcher action
		add(createItem("details", entity, context::showDetails));

		// Custom actions for specific entity types
		for(var a : selected.getActions()) {
			var actionItem = new JMenuItem(Msg.getString("PopUpUnitMenu." + a));
			actionItem.addActionListener(e -> {
				selected.applyAction(a);
				repaint();
			});
			add(actionItem);
		}
    }

    /**
     * Builds info dialog action based on a summary of the target
     *
     * @param e the target entity
     * @param summary the summary of the target entity
     */
    private JMenuItem buildInfoItem(final Entity e, UnitSummary summary) {
        
		return createItem("info", e, t -> {
			UnitInfoPanel b = new UnitInfoPanel(e.getName(), summary);
			b.setOpaque(false);
			b.setBackground(new Color(0,0,0,128));
			
			JDialog d = SwingHelper.createPopupWindow(b, WIDTH_1, HEIGHT_1, 0, 0);

			d.setForeground(Color.WHITE); // orange font
			d.setFont(new Font("Arial", Font.BOLD, 14));

			d.setOpacity(0.75f);
			d.setBackground(new Color(0,0,0,128));
			d.setVisible(true);
		});
    }

	/**
     * Creates a menu item.
     * 
     * @param <T>
     * @param name
     * @param target
     * @param action
     * @return
     */
	private <T> JMenuItem createItem(String name, T target, Consumer<T> action) {
		JMenuItem relocateItem = new JMenuItem(Msg.getString("PopUpUnitMenu." + name));
		
        relocateItem.addActionListener(e -> {
			action.accept(target);
			repaint();
        });

		return relocateItem;
	}
}

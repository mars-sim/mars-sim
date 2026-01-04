/*
 * Mars Simulation Project
 * UnitListPanel.java
 * @date 2021-12-20
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.unit_window;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.mars_sim.core.Entity;
import com.mars_sim.core.UnitManager;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.utils.EntityListLauncher;

/**
 * A class that presents a selectable visual list of Units. Double clicking
 * will open the associated UnitWindow dialog.
 * The implementors provide a getData method which returns the raw Units to display.
 * 
 * @param <T> The unit Subclass to display.
 */
@SuppressWarnings("serial")
public abstract class UnitListPanel<T extends Entity> extends JPanel {
	private DefaultListModel<T> model;
	private List<T> cachedData;
	private UIContext desktop;

	protected UnitListPanel(UIContext desktop) {
		this(desktop, null);
	}
	
	/**
	 * Creates a list with a specific preferred size.
	 * 
	 * @param desktop
	 * @param dim Preferred size
	 */
	protected UnitListPanel(UIContext desktop, Dimension dim) {
		super(new FlowLayout(FlowLayout.CENTER));

		this.desktop = desktop;
		this.model = new DefaultListModel<T>();
		
		// Create unit list
		JList<T> list = new JList<>(this.model);
		list.addMouseListener(new EntityListLauncher(desktop));
		list.setToolTipText(EntityListLauncher.TOOLTIP);
		
		// Create scroll panel
		JScrollPane scrollPanel = new JScrollPane();

		if (dim != null) {
			scrollPanel.setPreferredSize(dim);
		}
		scrollPanel.setViewportView(list);
		add(scrollPanel);
		
		// Populate by triggering a refresh
		update();
	}
	
	/**
	 * Converts a collection of Unit Identifiers into a
	 * collection of T.
	 * 
	 * @param ids Unit ids
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected Collection<T> getUnitsFromIds(Collection<Integer> ids) {
		List<T> result = new ArrayList<>();
		UnitManager um = desktop.getSimulation().getUnitManager();
		
		for(Integer id : ids) {
			result.add( (T) um.getUnitByID(id));
		}
		return result;
	}
	
	/**
	 * Returns the collection of Units to display.
	 * 
	 * @return
	 */
	protected abstract Collection<T> getData();
	
	/**
	 * How many units are displayed in the list.
	 * 
	 * @return
	 */
	public int getUnitCount() {
		return cachedData.size();
	}
	
	/**
	 * Updates the list.
	 * 
	 * @return Was the list changed?
	 */
	public boolean update() {
		List<T> newData = new ArrayList<>(getData());
		boolean changed = false;
		
		// Update population list and number label
		if ((cachedData == null) || !cachedData.equals(newData)) {
			cachedData = newData;
			model.clear();
			model.addAll(cachedData);
			changed = true;
		}
		
		return changed;
	}
}
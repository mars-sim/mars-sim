/**
 * Mars Simulation Project
 * BuildingPanelInhabitable.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.structure.building;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.commons.collections.CollectionUtils;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.structure.building.function.LifeSupport;
import org.mars_sim.msp.ui.swing.MainDesktopPane;

import com.alee.laf.panel.WebPanel;
import com.alee.laf.scroll.WebScrollPane;

/**
 * The InhabitableBuildingPanel class is a building function panel representing 
 * the inhabitants of a settlement building.
 */
@SuppressWarnings("serial")
public class BuildingPanelInhabitable
extends BuildingFunctionPanel
implements MouseListener {

	/** The inhabitable building. */
	private LifeSupport inhabitable;
	private DefaultListModel<Person> inhabitantListModel;
	private JList<Person> inhabitantList;
	private Collection<Person> inhabitantCache;
	private JTextField numberLabel;

	/**
	 * Constructor.
	 * @param inhabitable The inhabitable building this panel is for.
	 * @param desktop The main desktop.
	 */
	public BuildingPanelInhabitable(LifeSupport inhabitable, MainDesktopPane desktop) {

		// Use BuildingFunctionPanel constructor
		super(Msg.getString("BuildingPanelInhabitable.title"), inhabitable.getBuilding(), desktop);

		// Initialize data members.
		this.inhabitable = inhabitable;
	}
	
	/**
	 * Build the UI
	 */
	@Override
	protected void buildUI(JPanel center) {
		// Create label panel
		WebPanel labelPanel = new WebPanel(new GridLayout(2, 2, 3, 1));
		center.add(labelPanel, BorderLayout.NORTH);
		labelPanel.setOpaque(false);
		labelPanel.setBackground(new Color(0,0,0,128));

		// Create number label
		numberLabel = addTextField(labelPanel, Msg.getString("BuildingPanelInhabitable.number"),
								   inhabitable.getOccupantNumber(), null); //$NON-NLS-1$

		// Create capacity label
		addTextField(labelPanel, Msg.getString("BuildingPanelInhabitable.capacity"),
					 inhabitable.getOccupantCapacity(), null);


		// Create inhabitant list panel
		WebPanel inhabitantListPanel = new WebPanel(new FlowLayout(FlowLayout.CENTER));
		addBorder(inhabitantListPanel, "Inhabitants");
		center.add(inhabitantListPanel, BorderLayout.CENTER);

		// Create inhabitant list model
		inhabitantListModel = new DefaultListModel<>();
		inhabitantCache = new ArrayList<>(inhabitable.getOccupants());
		Iterator<Person> i = inhabitantCache.iterator();
		while (i.hasNext()) inhabitantListModel.addElement(i.next());

		// Create inhabitant list
		inhabitantList = new JList<>(inhabitantListModel);
		inhabitantList.addMouseListener(this);
		
		// Create scroll panel for occupant list.
		WebScrollPane scrollPanel1 = new WebScrollPane();
		scrollPanel1.setPreferredSize(new Dimension(150, 100));
		scrollPanel1.setViewportView(inhabitantList);

		inhabitantListPanel.add(scrollPanel1);
	}

	/**
	 * Update this panel.
	 */
	@Override
	public void update() {

		// Update population list and number label
		if (!CollectionUtils.isEqualCollection(inhabitantCache, inhabitable.getOccupants())) {
			inhabitantCache = new ArrayList<>(inhabitable.getOccupants());
			inhabitantListModel.clear();
			Iterator<Person> i = inhabitantCache.iterator();
			while (i.hasNext()) inhabitantListModel.addElement(i.next());

			numberLabel.setText(Integer.toString(inhabitantCache.size()));
		}
	}

	/** 
	 * Mouse clicked event occurs.
	 * @param event the mouse event
	 */
	@Override
	public void mouseClicked(MouseEvent event) {

		// If double-click, open person window.
		if (event.getClickCount() >= 2) 
			desktop.openUnitWindow((Person) inhabitantList.getSelectedValue(), false);
	}

	@Override
	public void mousePressed(MouseEvent event) {}
	
	@Override
	public void mouseReleased(MouseEvent event) {}
	
	@Override
	public void mouseEntered(MouseEvent event) {}
	
	@Override
	public void mouseExited(MouseEvent event) {}
}

/**
 * Mars Simulation Project
 * BuildingPanelInhabitable.java
 * @version 3.1.0 2017-09-15
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.structure.building;

import org.apache.commons.collections.CollectionUtils;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.structure.building.function.LifeSupport;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.unit_window.UnitWindow;

import com.alee.laf.scroll.WebScrollPane;

import javax.swing.*;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * The InhabitableBuildingPanel class is a building function panel representing 
 * the inhabitants of a settlement building.
 */
public class BuildingPanelInhabitable
extends BuildingFunctionPanel
implements MouseListener {

	/** The inhabitable building. */
	private LifeSupport inhabitable;
	private DefaultListModel<Person> inhabitantListModel;
	private JList<Person> inhabitantList;
	private Collection<Person> inhabitantCache;
	private JLabel numberLabel;

	/**
	 * Constructor.
	 * @param inhabitable The inhabitable building this panel is for.
	 * @param desktop The main desktop.
	 */
	public BuildingPanelInhabitable(LifeSupport inhabitable, MainDesktopPane desktop) {

		// Use BuildingFunctionPanel constructor
		super(inhabitable.getBuilding(), desktop);

		// Initialize data members.
		this.inhabitable = inhabitable;

		// Set panel layout
		setLayout(new BorderLayout());

		// Create label panel
		JPanel labelPanel = new JPanel(new GridLayout(3, 1, 0, 0));
		add(labelPanel, BorderLayout.NORTH);
		labelPanel.setOpaque(false);
		labelPanel.setBackground(new Color(0,0,0,128));

		// Create inhabitant label
		// 2014-11-21 Changed font type, size and color of the title text
		JLabel inhabitantLabel = new JLabel(Msg.getString("BuildingPanelInhabitable.title"), JLabel.CENTER); //$NON-NLS-1$
		inhabitantLabel.setFont(new Font("Serif", Font.BOLD, 16));
		//inhabitantLabel.setForeground(new Color(102, 51, 0)); // dark brown
		labelPanel.add(inhabitantLabel);
		inhabitantLabel.setOpaque(false);
		inhabitantLabel.setBackground(new Color(0,0,0,128));

		// Create number label
		numberLabel = new JLabel(Msg.getString("BuildingPanelInhabitable.number", inhabitable.getOccupantNumber()), JLabel.CENTER); //$NON-NLS-1$
		labelPanel.add(numberLabel);
		numberLabel.setOpaque(false);
		numberLabel.setBackground(new Color(0,0,0,128));

		// Create capacity label
		JLabel capacityLabel = new JLabel(
			Msg.getString(
				"BuildingPanelInhabitable.capacity", //$NON-NLS-1$
				inhabitable.getOccupantCapacity()
			),JLabel.CENTER
		);
		labelPanel.add(capacityLabel);
		capacityLabel.setOpaque(false);
		capacityLabel.setBackground(new Color(0,0,0,128));

		// Create inhabitant list panel
		JPanel inhabitantListPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		add(inhabitantListPanel, BorderLayout.CENTER);
		//inhabitantListPanel.setOpaque(false);
		//inhabitantListPanel.setBackground(new Color(0,0,0,128));

		
		// Create scroll panel for inhabitant list
//		WebScrollPane scrollPanel = new WebScrollPane();
//		scrollPanel.setPreferredSize(new Dimension(UnitWindow.WIDTH-80, 60));
//		inhabitantListPanel.add(scrollPanel);
//		scrollPanel.setViewportView(inhabitantList);
//		//scrollPanel.getViewport().setOpaque(false);
//		//scrollPanel.getViewport().setBackground(new Color(0,0,0,128));
//		//scrollPanel.setOpaque(false);
//		//scrollPanel.setBackground(new Color(0,0,0,128));
//	    scrollPanel.setBorder( BorderFactory.createLineBorder(Color.LIGHT_GRAY) );

		
		// Create inhabitant list model
		inhabitantListModel = new DefaultListModel<Person>();
		inhabitantCache = new ArrayList<Person>(inhabitable.getOccupants());
		Iterator<Person> i = inhabitantCache.iterator();
		while (i.hasNext()) inhabitantListModel.addElement(i.next());

		// Create inhabitant list
		inhabitantList = new JList<Person>(inhabitantListModel);
		inhabitantList.addMouseListener(this);

		//inhabitantList.setOpaque(false);
		//inhabitantList.setBackground(new Color(0,0,0,128));

	}

	/**
	 * Update this panel.
	 */
	public void update() {

		// Update population list and number label
		if (!CollectionUtils.isEqualCollection(inhabitantCache, inhabitable.getOccupants())) {
			inhabitantCache = new ArrayList<Person>(inhabitable.getOccupants());
			inhabitantListModel.clear();
			Iterator<Person> i = inhabitantCache.iterator();
			while (i.hasNext()) inhabitantListModel.addElement(i.next());

			numberLabel.setText(Msg.getString("BuildingPanelInhabitable.number", inhabitantCache.size())); //$NON-NLS-1$
		}
	}

	/** 
	 * Mouse clicked event occurs.
	 * @param event the mouse event
	 */
	public void mouseClicked(MouseEvent event) {

		// If double-click, open person window.
		if (event.getClickCount() >= 2) 
			desktop.openUnitWindow((Person) inhabitantList.getSelectedValue(), false);
	}

	public void mousePressed(MouseEvent event) {}
	public void mouseReleased(MouseEvent event) {}
	public void mouseEntered(MouseEvent event) {}
	public void mouseExited(MouseEvent event) {}
}

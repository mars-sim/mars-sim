/**
 * Mars Simulation Project
 * AssociatedPeopleTabPanel.java
 * @version 3.06 2014-01-29
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.structure;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.apache.commons.collections.CollectionUtils;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.tool.monitor.PersonTableModel;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;

/** 
 * The AssociatedPeopleTabPanel is a tab panel for information on all people 
 * associated with a settlement.
 */
public class TabPanelAssociatedPeople
extends TabPanel
implements MouseListener, ActionListener {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Data members
	private DefaultListModel<Person> populationListModel;
	private JList<Person> populationList;
	private Collection<Person> populationCache;

	/**
	 * Constructor.
	 * @param unit the unit to display.
	 * @param desktop the main desktop.
	 */
	public TabPanelAssociatedPeople(Unit unit, MainDesktopPane desktop) { 
		// Use the TabPanel constructor
		super(
			Msg.getString("TabPanelAssociatedPeople.title"), //$NON-NLS-1$
			null,
			Msg.getString("TabPanelAssociatedPeople.tooltip"), //$NON-NLS-1$
			unit, desktop
		);

		Settlement settlement = (Settlement) unit;

		// Create label
		JPanel associatedLabelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		topContentPanel.add(associatedLabelPanel);

		// Create associated people label
		JLabel associatedLabel = new JLabel(Msg.getString("TabPanelAssociatedPeople.label"), JLabel.CENTER); //$NON-NLS-1$
		associatedLabelPanel.add(associatedLabel);

		// Create population display panel
		JPanel populationDisplayPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		populationDisplayPanel.setBorder(new MarsPanelBorder());
		topContentPanel.add(populationDisplayPanel);

		// Create scroll panel for population list.
		JScrollPane populationScrollPanel = new JScrollPane();
		populationScrollPanel.setPreferredSize(new Dimension(175, 250));
		populationDisplayPanel.add(populationScrollPanel);

		// Create population list model
		populationListModel = new DefaultListModel<Person>();
		populationCache = settlement.getAllAssociatedPeople();
		Iterator<Person> i = populationCache.iterator();
		while (i.hasNext()) populationListModel.addElement(i.next());

		// Create population list
		populationList = new JList<Person>(populationListModel);
		populationList.addMouseListener(this);
		populationScrollPanel.setViewportView(populationList);

		// Create population monitor button
		JButton monitorButton = new JButton(ImageLoader.getIcon(Msg.getString("img.monitor"))); //$NON-NLS-1$
		monitorButton.setMargin(new Insets(1, 1, 1, 1));
		monitorButton.addActionListener(this);
		monitorButton.setToolTipText(Msg.getString("TabPanelAssociatedPeople.tooltip.monitor")); //$NON-NLS-1$
		populationDisplayPanel.add(monitorButton);		
	}

	/**
	 * Updates the info on this panel.
	 */
	public void update() {
		Settlement settlement = (Settlement) unit;

		// Update population list
		if (!CollectionUtils.isEqualCollection(populationCache, settlement.getAllAssociatedPeople())) {
			populationCache = settlement.getAllAssociatedPeople();
			populationListModel.clear();
			Iterator<Person> i = populationCache.iterator();
			while (i.hasNext()) populationListModel.addElement(i.next());
		}
	}

	/** 
	 * Mouse clicked event occurs.
	 * @param event the mouse event
	 */
	public void mouseClicked(MouseEvent event) {
		// If double-click, open person window.
		if (event.getClickCount() >= 2) {
			Person person = (Person) populationList.getSelectedValue();
			if (person != null) {
				desktop.openUnitWindow(person, false);
			}
		}
	}

	public void mousePressed(MouseEvent arg0) {}
	public void mouseReleased(MouseEvent arg0) {}
	public void mouseEntered(MouseEvent arg0) {}
	public void mouseExited(MouseEvent arg0) {}

	/** 
	 * Action event occurs.
	 * @param event the action event
	 */
	public void actionPerformed(ActionEvent event) {
		// If the population monitor button was pressed, create tab in monitor tool.
		desktop.addModel(new PersonTableModel((Settlement) unit, true));
	}
}
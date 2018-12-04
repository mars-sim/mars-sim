/**
 * Mars Simulation Project
 * TabPanelAssociatedPeople.java
 * @version 3.1.0 2017-10-18
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.structure;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.JList;
import javax.swing.SpringLayout;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.tool.SpringUtilities;
import org.mars_sim.msp.ui.swing.tool.monitor.PersonTableModel;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;

import com.alee.laf.button.WebButton;
import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;
import com.alee.laf.scroll.WebScrollPane;

/**
 * The AssociatedPeopleTabPanel is a tab panel for information on all people
 * associated with a settlement.
 */
public class TabPanelAssociatedPeople extends TabPanel implements MouseListener, ActionListener {

	// Data members
	private AssociatedPopulationListModel populationListModel;
	private JList<Person> populationList;
	private WebScrollPane populationScrollPanel;
	private WebLabel populationNumLabel;
	private WebLabel populationCapacityLabel;
	private WebLabel populationIndoorLabel;
	private int populationNumCache;
	private int populationCapacityCache;
	private int populationIndoorCache;
	
	/**
	 * Constructor.
	 * 
	 * @param unit    the unit to display.
	 * @param desktop the main desktop.
	 */
	public TabPanelAssociatedPeople(Unit unit, MainDesktopPane desktop) {
		// Use the TabPanel constructor
		super(Msg.getString("TabPanelAssociatedPeople.title"), //$NON-NLS-1$
				null, Msg.getString("TabPanelAssociatedPeople.tooltip"), //$NON-NLS-1$
				unit, desktop);

		Settlement settlement = (Settlement) unit;

		WebPanel titlePane = new WebPanel(new FlowLayout(FlowLayout.CENTER));
		topContentPanel.add(titlePane);

		// Create associated people label
		WebLabel heading = new WebLabel(Msg.getString("TabPanelAssociatedPeople.heading"), WebLabel.CENTER); //$NON-NLS-1$
		heading.setFont(new Font("Serif", Font.BOLD, 16));
		// heading.setForeground(new Color(102, 51, 0)); // dark brown
		titlePane.add(heading);

		// Prepare info panel.
		WebPanel countPanel = new WebPanel(new GridLayout(3, 1, 0, 0));
		countPanel.setBorder(new MarsPanelBorder());
		topContentPanel.add(countPanel);

		// Create associate label
		populationNumCache = settlement.getNumCitizens();
		populationNumLabel = new WebLabel(Msg.getString("TabPanelAssociatedPeople.associated", populationNumCache),
				WebLabel.CENTER); // $NON-NLS-1$
		countPanel.add(populationNumLabel);

		// Create population indoor label
		populationIndoorCache = settlement.getIndoorPeopleCount();
		populationIndoorLabel = new WebLabel(Msg.getString("TabPanelAssociatedPeople.indoor", populationIndoorCache),
				WebLabel.CENTER); // $NON-NLS-1$
		countPanel.add(populationIndoorLabel);
		
		// Create population capacity label
		populationCapacityCache = settlement.getPopulationCapacity();
		populationCapacityLabel = new WebLabel(Msg.getString("TabPanelAssociatedPeople.capacity", populationCapacityCache),
				WebLabel.CENTER); // $NON-NLS-1$
		countPanel.add(populationCapacityLabel);
		
		// Create spring layout population display panel
		WebPanel populationDisplayPanel = new WebPanel(new SpringLayout());// FlowLayout(FlowLayout.LEFT));
		populationDisplayPanel.setBorder(new MarsPanelBorder());
		topContentPanel.add(populationDisplayPanel);

		// Create scroll panel for population list.
		populationScrollPanel = new WebScrollPane();
		populationScrollPanel.setPreferredSize(new Dimension(120, 250));
		populationDisplayPanel.add(populationScrollPanel);

		// Create population list model
		populationListModel = new AssociatedPopulationListModel(settlement);

		// Create population list
		populationList = new JList<Person>(populationListModel);
		populationList.addMouseListener(this);
		populationScrollPanel.setViewportView(populationList);

		// Create population monitor button
		WebButton monitorButton = new WebButton(ImageLoader.getIcon(Msg.getString("img.monitor"))); //$NON-NLS-1$
		monitorButton.setMargin(new Insets(1, 1, 1, 1));
		monitorButton.addActionListener(this);
		monitorButton.setToolTipText(Msg.getString("TabPanelAssociatedPeople.tooltip.monitor")); //$NON-NLS-1$
		
		WebPanel buttonPane = new WebPanel(new FlowLayout(FlowLayout.CENTER));
//		buttonPane.setPreferredSize(new Dimension(25, 25));
		buttonPane.add(monitorButton);
		
		populationDisplayPanel.add(buttonPane);

		// Lay out the spring panel.
		SpringUtilities.makeCompactGrid(populationDisplayPanel, 2, 1, // rows, cols
				30, 10, // initX, initY
				10, 10); // xPad, yPad
	}

	/**
	 * Updates the info on this panel.
	 */
	public void update() {

		Settlement settlement = (Settlement) unit;

		int num = settlement.getNumCitizens();
		// Update population num
		if (populationNumCache != num) {
			populationNumCache = num;
			populationNumLabel.setText(Msg.getString("TabPanelAssociatedPeople.associated", populationNumCache)); // $NON-NLS-1$
		}

		// Update population list
		populationListModel.update();
		populationScrollPanel.validate();
	}

	/**
	 * List model for settlement population.
	 */
	private class AssociatedPopulationListModel extends AbstractListModel<Person> {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

		private Settlement settlement;
		private List<Person> populationList;

		private AssociatedPopulationListModel(Settlement settlement) {
			this.settlement = settlement;

			populationList = new ArrayList<Person>(settlement.getAllAssociatedPeople());
			Collections.sort(populationList);
		}

		@Override
		public Person getElementAt(int index) {

			Person result = null;

			if ((index >= 0) && (index < settlement.getNumCitizens())) {
				result = populationList.get(index);
			}

			return result;
		}

		@Override
		public int getSize() {
			return settlement.getNumCitizens();//populationList.size();
		}

		/**
		 * Update the population list model.
		 */
		public void update() {

			if (!populationList.containsAll(settlement.getAllAssociatedPeople())
					|| !settlement.getAllAssociatedPeople().containsAll(populationList)) {

				List<Person> oldPopulationList = populationList;

				List<Person> tempPopulationList = new ArrayList<Person>(settlement.getAllAssociatedPeople());
				Collections.sort(tempPopulationList);

				populationList = tempPopulationList;
				fireContentsChanged(this, 0, getSize());

				oldPopulationList.clear();
			}
		}
	}

	/**
	 * Mouse clicked event occurs.
	 * 
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

	public void mousePressed(MouseEvent arg0) {
	}

	public void mouseReleased(MouseEvent arg0) {
	}

	public void mouseEntered(MouseEvent arg0) {
	}

	public void mouseExited(MouseEvent arg0) {
	}

	/**
	 * Action event occurs.
	 * 
	 * @param event the action event
	 */
	public void actionPerformed(ActionEvent event) {
		// If the population monitor button was pressed, create tab in monitor tool.
		desktop.addModel(new PersonTableModel((Settlement) unit, true));
	}

	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
		populationListModel = null;
		populationList = null;
		populationScrollPanel = null;
		populationNumLabel = null;
	}
}
/**
 * Mars Simulation Project
 * TabPanelAssociatedPeople.java
 * @version 3.1.0 2017-10-18
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.structure;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.SpringLayout;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
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
@SuppressWarnings("serial")
public class TabPanelAssociatedPeople extends TabPanel implements MouseListener, ActionListener {

	// Data members
	/** Is UI constructed. */
	private boolean uiDone = false;

	private int populationCitizensCache;
	private int populationCapacityCache;
	private int populationIndoorCache;
	
	private Settlement settlement;
	
	private AssociatedPopulationListModel populationListModel;
	private JList<Person> populationList;
	private WebScrollPane populationScrollPanel;
	
	private WebLabel populationCitizensLabel;
	private WebLabel populationCapacityLabel;
	private WebLabel populationIndoorLabel;

	
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

		settlement = (Settlement) unit;

	}
	
	public boolean isUIDone() {
		return uiDone;
	}
	
	public void initializeUI() {
		uiDone = true;
		
		WebPanel titlePane = new WebPanel(new FlowLayout(FlowLayout.CENTER));
		topContentPanel.add(titlePane);

		// Create associated people label
		WebLabel heading = new WebLabel(Msg.getString("TabPanelAssociatedPeople.title"), WebLabel.CENTER); //$NON-NLS-1$
		heading.setFont(new Font("Serif", Font.BOLD, 16));
		// heading.setForeground(new Color(102, 51, 0)); // dark brown
		titlePane.add(heading);

		// Prepare count spring layout panel.
		WebPanel countPanel = new WebPanel(new SpringLayout());//GridLayout(3, 1, 0, 0));
//		countPanel.setBorder(new MarsPanelBorder());
		topContentPanel.add(countPanel);

		// Create associate label
		WebLabel populationNumHeader = new WebLabel(Msg.getString("TabPanelAssociatedPeople.associated"),
				WebLabel.RIGHT); // $NON-NLS-1$
		countPanel.add(populationNumHeader);
		
		populationCitizensCache = settlement.getNumCitizens();
		populationCitizensLabel = new WebLabel(populationCitizensCache + "", WebLabel.LEFT);
		countPanel.add(populationCitizensLabel);

		// Create population indoor label
		WebLabel populationIndoorHeader = new WebLabel(Msg.getString("TabPanelAssociatedPeople.indoor"),
				WebLabel.RIGHT); // $NON-NLS-1$
		countPanel.add(populationIndoorHeader);
		
		populationIndoorCache = settlement.getIndoorPeopleCount();
		populationIndoorLabel = new WebLabel(populationIndoorCache + "", WebLabel.LEFT);
		countPanel.add(populationIndoorLabel);
		
		// Create population capacity label
		WebLabel populationCapacityHeader = new WebLabel(Msg.getString("TabPanelAssociatedPeople.capacity"),
				WebLabel.RIGHT); // $NON-NLS-1$
		countPanel.add(populationCapacityHeader);
		
		populationCapacityCache = settlement.getPopulationCapacity();
		populationCapacityLabel = new WebLabel(populationCapacityCache + "", WebLabel.RIGHT);
		countPanel.add(populationCapacityLabel);
		
		// Lay out the spring panel.
		SpringUtilities.makeCompactGrid(countPanel, 3, 2, // rows, cols
				25, 10, // initX, initY
				10, 10); // xPad, yPad
		
        UIManager.getDefaults().put("TitledBorder.titleColor", Color.darkGray);
        Border lowerEtched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
        TitledBorder title = BorderFactory.createTitledBorder(lowerEtched, " " + Msg.getString("TabPanelAssociatedPeople.title") + " ");
//      title.setTitleJustification(TitledBorder.RIGHT);
        Font titleFont = UIManager.getFont("TitledBorder.font");
        title.setTitleFont( titleFont.deriveFont(Font.ITALIC + Font.BOLD));
        
		// Create spring layout population display panel
		WebPanel populationDisplayPanel = new WebPanel(new FlowLayout(FlowLayout.CENTER));
		populationDisplayPanel.setBorder(title);
//		populationDisplayPanel.setBorder(new MarsPanelBorder());
		topContentPanel.add(populationDisplayPanel);

		// Create scroll panel for population list.
		populationScrollPanel = new WebScrollPane();
		populationScrollPanel.setPreferredSize(new Dimension(200, 250));
		populationDisplayPanel.add(populationScrollPanel);

		// Create population list model
		populationListModel = new AssociatedPopulationListModel(settlement);

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
		
//		WebPanel buttonPane = new WebPanel(new FlowLayout(FlowLayout.RIGHT));
////		buttonPane.setPreferredSize(new Dimension(25, 25));
//		buttonPane.add(monitorButton);
//		
//		populationDisplayPanel.add(buttonPane);

	}

	/**
	 * Updates the info on this panel.
	 */
	public void update() {
		if (!uiDone)
			this.initializeUI();

		int num0 = settlement.getNumCitizens();
		// Update citizen num
		if (populationCitizensCache != num0) {
			populationCitizensCache = num0;
			populationCitizensLabel.setText(populationCitizensCache + "");
		}

		int num = settlement.getIndoorPeopleCount();
		// Update indoor num
		if (populationIndoorCache != num) {
			populationIndoorCache = num;
			populationIndoorLabel.setText(populationIndoorCache + "");
		}
		
		int cap = settlement.getPopulationCapacity();
		// Update capacity
		if (populationCapacityCache != cap) {
			populationCapacityCache = cap;
			populationCapacityLabel.setText(populationCapacityCache + "");
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
		populationCitizensLabel = null;
	}
}
/**
 * Mars Simulation Project
 * PopulationTabPanel.java
 * @version 3.1.0 2017-02-14
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.structure;


import java.awt.Color;
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
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
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

import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;

/**
 * This is a tab panel for population information.
 */
@SuppressWarnings("serial")
public class TabPanelPopulation
extends TabPanel
implements MouseListener, ActionListener {

	/** Is UI constructed. */
	private boolean uiDone = false;
	
	/** The Settlement instance. */
	private Settlement settlement;
	
	private JLabel populationIndoorLabel;
	private JLabel populationCapacityLabel;
	
	private PopulationListModel populationListModel;
	private JList<Person> populationList;
	private JScrollPane populationScrollPanel;
	
	private int populationIndoorCache;
	private int populationCapacityCache;

	/**
	 * Constructor.
	 * @param unit the unit to display.
	 * @param desktop the main desktop.
	 */
	public TabPanelPopulation(Unit unit, MainDesktopPane desktop) {
		// Use the TabPanel constructor
		super(
			Msg.getString("TabPanelPopulation.title"), //$NON-NLS-1$
			null,
			Msg.getString("TabPanelPopulation.tooltip"), //$NON-NLS-1$
			unit, desktop
		);

		settlement = (Settlement) unit;

	}
	
	public boolean isUIDone() {
		return uiDone;
	}
	
	public void initializeUI() {
		uiDone = true;
		
		JPanel titlePane = new JPanel(new FlowLayout(FlowLayout.CENTER));
		topContentPanel.add(titlePane);

		JLabel heading = new JLabel(Msg.getString("TabPanelPopulation.title"), JLabel.CENTER); //$NON-NLS-1$
		heading.setFont(new Font("Serif", Font.BOLD, 16));
		//heading.setForeground(new Color(102, 51, 0)); // dark brown
		titlePane.add(heading);

		// Prepare count spring layout panel.
		WebPanel countPanel = new WebPanel(new SpringLayout());//GridLayout(3, 1, 0, 0));
//		countPanel.setBorder(new MarsPanelBorder());
		topContentPanel.add(countPanel);
		
		// Create population indoor label
		WebLabel populationIndoorHeader = new WebLabel(Msg.getString("TabPanelPopulation.indoor"),
				WebLabel.RIGHT); // $NON-NLS-1$
		countPanel.add(populationIndoorHeader);
		
		populationIndoorCache = settlement.getIndoorPeopleCount();
		populationIndoorLabel = new WebLabel(populationIndoorCache + "", WebLabel.LEFT);
		countPanel.add(populationIndoorLabel);
		
		// Create population capacity label
		WebLabel populationCapacityHeader = new WebLabel(Msg.getString("TabPanelPopulation.capacity"),
				WebLabel.RIGHT); // $NON-NLS-1$
		countPanel.add(populationCapacityHeader);
		
		populationCapacityCache = settlement.getPopulationCapacity();
		populationCapacityLabel = new WebLabel(populationCapacityCache + "", WebLabel.RIGHT);
		countPanel.add(populationCapacityLabel);
		
		// Lay out the spring panel.
		SpringUtilities.makeCompactGrid(countPanel, 2, 2, // rows, cols
				25, 10, // initX, initY
				10, 10); // xPad, yPad
		
        UIManager.getDefaults().put("TitledBorder.titleColor", Color.darkGray);
        Border lowerEtched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
        TitledBorder title = BorderFactory.createTitledBorder(lowerEtched, " " + Msg.getString("TabPanelPopulation.title") + " ");
//      title.setTitleJustification(TitledBorder.RIGHT);
        Font titleFont = UIManager.getFont("TitledBorder.font");
        title.setTitleFont( titleFont.deriveFont(Font.ITALIC + Font.BOLD));
        
		// Create spring layout population display panel
		JPanel populationDisplayPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		populationDisplayPanel.setBorder(title);
		topContentPanel.add(populationDisplayPanel);

		// Create scroll panel for population list.
		populationScrollPanel = new JScrollPane();
		populationScrollPanel.setPreferredSize(new Dimension(200, 250));
		populationDisplayPanel.add(populationScrollPanel);

		// Create population list model
		populationListModel = new PopulationListModel(settlement);

		// Create population list
		populationList = new JList<Person>(populationListModel);
		populationList.addMouseListener(this);
		populationScrollPanel.setViewportView(populationList);

		// Create population monitor button
		JButton monitorButton = new JButton(ImageLoader.getIcon(Msg.getString("img.monitor"))); //$NON-NLS-1$
		monitorButton.setMargin(new Insets(1, 1, 1, 1));
		monitorButton.addActionListener(this);
		monitorButton.setToolTipText(Msg.getString("TabPanelPopulation.tooltip.monitor")); //$NON-NLS-1$
		populationDisplayPanel.add(monitorButton);

	}

	/**
	 * Updates the info on this panel.
	 */
	public void update() {
		if (!uiDone)
			initializeUI();
		
		Settlement settlement = (Settlement) unit;

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
	private class PopulationListModel extends AbstractListModel<Person> {

	    /** default serial id. */
	    private static final long serialVersionUID = 1L;

	    private Settlement settlement;
	    private List<Person> populationList;

	    private PopulationListModel(Settlement settlement) {
	        this.settlement = settlement;

	        populationList = new ArrayList<Person>(settlement.getIndoorPeople());
	        Collections.sort(populationList);
	    }

        @Override
        public Person getElementAt(int index) {

            Person result = null;

            if ((index >= 0) && (index < populationList.size())) {
                result = populationList.get(index);
            }

            return result;
        }

        @Override
        public int getSize() {
            return populationList.size();
        }

        /**
         * Update the population list model.
         */
        public void update() {

            if (!populationList.containsAll(settlement.getIndoorPeople()) ||
                    !settlement.getIndoorPeople().containsAll(populationList)) {

                List<Person> oldPopulationList = populationList;

                List<Person> tempPopulationList = new ArrayList<Person>(settlement.getIndoorPeople());
                Collections.sort(tempPopulationList);

                populationList = tempPopulationList;
                fireContentsChanged(this, 0, getSize());

                oldPopulationList.clear();
            }
        }
	}

	/**
	 * Action event occurs.
	 * @param event the action event
	 */
	public void actionPerformed(ActionEvent event) {
		// If the population monitor button was pressed, create tab in monitor tool.
		desktop.addModel(new PersonTableModel((Settlement) unit, false));
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

	public void mousePressed(MouseEvent event) {}
	public void mouseReleased(MouseEvent event) {}
	public void mouseEntered(MouseEvent event) {}
	public void mouseExited(MouseEvent event) {}
	
	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
		populationIndoorLabel = null;
		populationCapacityLabel = null;
		populationListModel = null;
		populationList = null;
		populationScrollPanel = null;
	}
	
}

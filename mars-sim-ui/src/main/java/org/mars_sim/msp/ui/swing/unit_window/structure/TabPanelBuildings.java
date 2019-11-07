/**
 * Mars Simulation Project
 * TabPanelBuildings.java
 * @version 3.1.0 2017-09-07
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.unit_window.structure;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComponent;
import javax.swing.JList;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.ui.swing.JComboBoxMW;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;
import org.mars_sim.msp.ui.swing.unit_window.structure.building.BuildingPanel;

import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;

/**
 * The BuildingsTabPanel is a tab panel containing building panels.
 */
@SuppressWarnings("serial")
public class TabPanelBuildings extends TabPanel implements ActionListener {

	/** Is UI constructed. */
	private boolean uiDone = false;
	
	private int count;

	private Settlement settlement;
	private Building building;

	private DefaultComboBoxModel<Building> comboBoxModel;
	private JComboBoxMW<Building> comboBox;
	private WebPanel buildingDisplayPanel;
	private CardLayout buildingLayout;
	
	private List<Building> buildingsCache;
	private List<BuildingPanel> buildingPanels;
	private List<WebPanel> panelList = new ArrayList<WebPanel>();

	/**
	 * Constructor
	 * 
	 * @param unit    the unit to display.
	 * @param desktop the main desktop.
	 */
	public TabPanelBuildings(Unit unit, MainDesktopPane desktop) {
		// Use the TabPanel constructor
		super(Msg.getString("TabPanelBuildings.title"), //$NON-NLS-1$
				null, Msg.getString("TabPanelBuildings.tooltip"), //$NON-NLS-1$
				unit, desktop);
		this.setOpaque(false);
		this.setBackground(new Color(0, 0, 0, 128));

		settlement = (Settlement) unit;
	}
	
	public boolean isUIDone() {
		return uiDone;
	}
	
	public void initializeUI() {
		uiDone = true;
						
		List<Building> buildings = settlement.getBuildingManager().getSortedBuildings();

		// Set building to the first element on the list
		// Add if-clause for opening the building panel via the right click popup menu
		if (building == null) {
			building = buildings.get(0);
			setCurrentBuilding(building);
		}

		// Create building select panel.
		// Add buildingInfoPanel & buildingTitleLabel
		WebPanel buildingInfoPanel = new WebPanel(new GridLayout(2, 1, 0, 0));
		// buildingInfoPanel.setBorder(new MarsPanelBorder());
		topContentPanel.add(buildingInfoPanel);
		panelList.add(buildingInfoPanel);

		WebLabel titleLabel = new WebLabel("Buildings", WebLabel.CENTER);
		titleLabel.setFont(new Font("Serif", Font.BOLD, 16));
		// titleLabel.setForeground(new Color(102, 51, 0)); // dark brown
		buildingInfoPanel.add(titleLabel);
		titleLabel.setOpaque(false);
		titleLabel.setBackground(new Color(0, 0, 0, 128));

		WebPanel buildingSelectPanel = new WebPanel(new FlowLayout(FlowLayout.CENTER));
		buildingInfoPanel.add(buildingSelectPanel);
		panelList.add(buildingSelectPanel);

		// Create building combo box model.
		comboBoxModel = new DefaultComboBoxModel<Building>();

		buildingsCache = new ArrayList<Building>(buildings);
		for (Building b : buildingsCache) {
			comboBoxModel.addElement(b);
		}
		// Create building list.
		comboBox = new JComboBoxMW<Building>(comboBoxModel);
//		comboBox.setRenderer(new PromptComboBoxRenderer("Select a Building"));
		// comboBox.setOpaque(false);
		// comboBox.setBackground(new Color(0,0,0,128));
		// comboBox.setBackground(new Color(255,229,204));
		// comboBox.setForeground(Color.orange);
		comboBox.addActionListener(this);
		comboBox.setMaximumRowCount(10);
		comboBox.setBorder(null);

		buildingSelectPanel.add(comboBox);

		// Create building display panel.
		buildingDisplayPanel = new WebPanel();
		buildingLayout = new CardLayout();
		buildingDisplayPanel.setLayout(buildingLayout);
//		buildingDisplayPanel.setBorder(new MarsPanelBorder());
		centerContentPanel.add(buildingDisplayPanel);
		panelList.add(buildingDisplayPanel);

		// Create building panels
		buildingPanels = new ArrayList<BuildingPanel>();
		count = 0;
		Iterator<Building> iter = buildings.iterator();
		while (iter.hasNext()) {
			BuildingPanel panel = new BuildingPanel(String.valueOf(count), iter.next(), desktop);
			buildingPanels.add(panel);
			buildingDisplayPanel.add(panel, panel.getPanelName());
			count++;
		}

		setPanelTranslucent();
	}

	class PromptComboBoxRenderer extends DefaultListCellRenderer {

		private String prompt;

		// public boolean isOptimizedDrawingEnabled();
		// private DefaultListCellRenderer defaultRenderer = new
		// DefaultListCellRenderer();
		public PromptComboBoxRenderer() {
			// defaultRenderer.setHorizontalAlignment(DefaultListCellRenderer.CENTER);
			// settlementListBox.setRenderer(defaultRenderer);
			// setOpaque(false);
//		    setHorizontalAlignment(CENTER);
//		    setVerticalAlignment(CENTER);
		}

		public PromptComboBoxRenderer(String prompt) {
			this.prompt = prompt;
		}

		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
				boolean cellHasFocus) {
//				Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			JComponent result = (JComponent) super.getListCellRendererComponent(list, value, index, isSelected,
					cellHasFocus);
			// Component component = super.getListCellRendererComponent(list, value, index,
			// isSelected, cellHasFocus);
			// component.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
			if (value == null) {
				setText(prompt);
				// this.setForeground(Color.orange);
				// this.setBackground(new Color(184,134,11));
				return this;
			}
//				int theme = MainScene.getTheme();		
//				if (theme == 0 || theme == 6) {
//					
//				}
//				else if (theme == 7) { 
//					if (isSelected) {
//						result.setForeground(new Color(184,134,11));
//				        result.setBackground(Color.orange); 
//			
//			          // unselected, and not the DnD drop location
//			        } else {
//			        	  result.setForeground(new Color(184,134,11));
//			        	  result.setBackground(new Color(255,229,204)); //pale yellow (255,229,204)
//					      //Color(184,134,11)) brown
//			        }
//				}
//			 result.setOpaque(false);
//				c = super.getListCellRendererComponent(
//	                    list, value, index, isSelected, cellHasFocus);

			return result;
		}
	}

	public void setPanelStyle(WebPanel p) {
		// if (isTranslucent) {
		p.setOpaque(false);
		p.setBackground(new Color(0, 0, 0, 128));
		// }
	}

	public void setPanelTranslucent() {
		// if (isTranslucent) {
		Iterator<WebPanel> i = panelList.iterator();
		while (i.hasNext()) {
			WebPanel pp = i.next();
			setPanelStyle(pp);
		}
		// }
	}

	/**
	 * Set the new name of a Building
	 * 
	 * @return none
	 */
	public void setCurrentBuilding(Building building) {
		this.building = building;
	}

	/**
	 * Updates the info on this panel.
	 */
	@Override
	public void update() {
		if (!uiDone)
			this.initializeUI();
		
		List<Building> buildings = settlement.getBuildingManager().getSortedBuildings();

		// Update buildings if necessary.
		if (!buildingsCache.equals(buildings)) {

			// Add building panels for new buildings.
			Iterator<Building> iter1 = buildings.iterator();
			while (iter1.hasNext()) {
				Building building = iter1.next();
				if (!buildingsCache.contains(building)) {
					BuildingPanel panel = new BuildingPanel(String.valueOf(count), building, desktop);
					buildingPanels.add(panel);
					panel.setOpaque(false);// setBackground(new Color(139,69,19));
					panel.setForeground(Color.green);
					panel.setBackground(new Color(0, 0, 0, 15));
					buildingDisplayPanel.add(panel, panel.getPanelName());
					// TODO: Modify to load building's nickName instead of buildingType
					comboBoxModel.addElement(building);
					count++;
				}
			}

			// Remove building panels for destroyed buildings.
			Iterator<Building> iter2 = buildingsCache.iterator();
			while (iter2.hasNext()) {
				Building building = iter2.next();
				if (!buildings.contains(building)) {
					BuildingPanel panel = getBuildingPanel(building);
					if (panel != null) {
						buildingPanels.remove(panel);
						buildingDisplayPanel.remove(panel);
						comboBoxModel.removeElement(building);
					}
				}
			}

			// Update buildings cache.
			buildingsCache = buildings;
		}

		// Have each building panel update.
		for (BuildingPanel p : buildingPanels)
			p.update();
	}

	/**
	 * Action event occurs.
	 * 
	 * @param event the action event
	 */
	public void actionPerformed(ActionEvent event) {
		Building building = (Building) comboBox.getSelectedItem();
		BuildingPanel panel = getBuildingPanel(building);

		if (panel != null)
			buildingLayout.show(buildingDisplayPanel, panel.getPanelName());
		else
			System.err.println(Msg.getString("TabPanelBuildings.err.cantFindPanelForBuilding", building.getNickName())); //$NON-NLS-1$
	}

	/**
	 * Gets the building panel for a given building.
	 * 
	 * @param building the given building
	 * @return the building panel or null if none.
	 */
	private BuildingPanel getBuildingPanel(Building building) {
		BuildingPanel result = null;
		Iterator<BuildingPanel> i = buildingPanels.iterator();
		while (i.hasNext()) {
			BuildingPanel panel = i.next();
			if (panel.getBuilding() == building) {
				// Set as current building object
				// setCurrentBuilding(building);
				result = panel;
			}
		}

		return result;
	}

	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
		// take care to avoid null exceptions
		if (panelList != null) {
			panelList.clear();
			panelList = null;
		}
		if (buildingsCache != null) {
			buildingsCache.clear();
			buildingsCache = null;
		}

		if (buildingPanels != null) {
			buildingPanels.clear();
			buildingPanels = null;
		}

		comboBoxModel = null;
		comboBox = null;
		buildingDisplayPanel = null;
		buildingLayout = null;
		building = null;
	}
}
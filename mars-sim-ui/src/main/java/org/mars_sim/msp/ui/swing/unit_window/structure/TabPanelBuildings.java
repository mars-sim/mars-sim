/**
 * Mars Simulation Project
 * TabPanelBuildings.java
 * @version 3.07 2014-11-07
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.unit_window.structure;

import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.ui.swing.JComboBoxMW;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;
import org.mars_sim.msp.ui.swing.unit_window.structure.building.BuildingPanel;

/**
 * The BuildingsTabPanel is a tab panel containing building panels.
 */
public class TabPanelBuildings
extends TabPanel
implements ActionListener {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private static final String RENAME = Msg.getString("TabPanelBuildings.renameBuilding.renameButton"); //$NON-NLS-1$
	
	private DefaultComboBoxModel<Building> buildingComboBoxModel;
	private JComboBoxMW<Building> buildingComboBox;
	private List<Building> buildingsCache;
	private JPanel buildingDisplayPanel;
	private CardLayout buildingLayout;
	private List<BuildingPanel> buildingPanels;
	private int count;

	//2014-10-29 Added renameBtn
	private JButton renameBtn;
	private Building building;

	/**
	 * Constructor.
	 * @param unit the unit to display.
	 * @param desktop the main desktop.
	 */
	public TabPanelBuildings(Unit unit, MainDesktopPane desktop) {
		// Use the TabPanel constructor
		super(
			Msg.getString("TabPanelBuildings.title"), //$NON-NLS-1$
			null,
			Msg.getString("TabPanelBuildings.tooltip"), //$NON-NLS-1$
			unit, desktop
		);

		Settlement settlement = (Settlement) unit;
		List<Building> buildings = settlement.getBuildingManager().getBuildings();
		Collections.sort(buildings);
		
		// 2014-11-07 Set the first building object on the list 
		// as the most current building object
		building = buildings.get(0);
		setCurrentBuilding(building);
		
		// Create building select panel.
		JPanel buildingSelectPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		buildingSelectPanel.setBorder(new MarsPanelBorder());
		topContentPanel.add(buildingSelectPanel);

		// Create building combo box model.
		buildingComboBoxModel = new DefaultComboBoxModel<Building>();
		buildingsCache = new ArrayList<Building>(buildings);
			//System.out.println("TabPanelBuildings.java : constructor : buildingsCache is "+ buildingsCache);
		Iterator<Building> i = buildingsCache.iterator();
		
		
		while (i.hasNext()) {
			Building b = i.next();
			// 2014-10-29: <<NOT USED>> Modified to load nickName instead of buildingType
			// b.setType(b.getNickName());
	    	buildingComboBoxModel.addElement(b);
		}
		// Create building list.
		buildingComboBox = new JComboBoxMW<Building>(buildingComboBoxModel);
		buildingComboBox.addActionListener(this);
		buildingComboBox.setMaximumRowCount(10);
		buildingSelectPanel.add(buildingComboBox);		

		//2014-10-29  Added renameBtn for renaming a building
		renameBtn = new JButton(RENAME);
		renameBtn.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
				renameBuilding();
			}
		});
		buildingSelectPanel.add(renameBtn);
		
		// Create building display panel.
		buildingDisplayPanel = new JPanel();
		buildingLayout = new CardLayout();
		buildingDisplayPanel.setLayout(buildingLayout);
		buildingDisplayPanel.setBorder(new MarsPanelBorder());
		centerContentPanel.add(buildingDisplayPanel);

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
	}

	/**
	 * Ask for a new building name
	 * @return pop up jDialog
	 */
	// 2014-10-29  Added askNameDialog()
	public String askNameDialog() {
		return JOptionPane
			.showInputDialog(desktop, 
					Msg.getString("TabPanelBuildings.JDialog.renameBuilding.input"),
					Msg.getString("TabPanelBuildings.JDialog.renameBuilding.title"),
			        JOptionPane.QUESTION_MESSAGE);
	}
	/**
	 * Change and validate the new name of a Building
	 * @return call Dialog popup
	 */
	// 2014-10-29  Added renameBuilding()
	private void renameBuilding() {
		JDialog.setDefaultLookAndFeelDecorated(true);
		String oldName = building.getNickName();
			System.out.println("TabPanelBuildings.java : renameBuilding() : old name is " + oldName);
		String newName = askNameDialog();
				
		if (newName.trim().equals(null) || (newName.trim().length() == 0))
			newName = askNameDialog();
		else {
			building.setNickName(newName);
			System.out.println("TabPanelBuildings.java : renameBuilding() : new name is " + newName);
		}
		//Settlement settlement = buildingDisplayPanel.getSettlement();
		//if (settlement != null) getDesktop().openUnitWindow(settlement, false);
        //desktop.disposeUnitWindow(unit.getContainerUnit());
		//desktop.openUnitWindow(unit.getContainerUnit(), false);
	}
	
	/** Set the new name of a Building
	 * @return none
	 */
	// 2014-10-29 Added setCurrentBuilding()
	public void setCurrentBuilding(Building building) {
		this.building = building;
	}
		
	/**
	 * Updates the info on this panel.
	 */
	@Override
	public void update() {
		Settlement settlement = (Settlement) unit;
		List<Building> buildings = settlement.getBuildingManager().getBuildings();

		// Update buildings if necessary.
		if (!buildingsCache.equals(buildings)) {

			// Add building panels for new buildings.
			Iterator<Building> iter1 = buildings.iterator();
			while (iter1.hasNext()) {
				Building building = iter1.next();
				if (!buildingsCache.contains(building)) {
					BuildingPanel panel = new BuildingPanel(String.valueOf(count), building, desktop);
					buildingPanels.add(panel);
					buildingDisplayPanel.add(panel, panel.getPanelName());
					// TODO: Modify to load building's nickName instead of buildingType
					buildingComboBoxModel.addElement(building);
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
						buildingComboBoxModel.removeElement(building);
					}
				}
			}

			// Update buildings cache.
			buildingsCache = buildings;
		}

		// Have each building panel update.
		Iterator<BuildingPanel> i = buildingPanels.iterator();
		while (i.hasNext()) i.next().update();
	}

	/** 
	 * Action event occurs.
	 * @param event the action event
	 */
	public void actionPerformed(ActionEvent event) {
		Building building = (Building) buildingComboBox.getSelectedItem();
		BuildingPanel panel = getBuildingPanel(building);
		
		if (panel != null) buildingLayout.show(buildingDisplayPanel, panel.getPanelName());
		else System.err.println(Msg.getString("TabPanelBuildings.err.cantFindPanelForBuilding", building.getNickName())); //$NON-NLS-1$
	}

	/**
	 * Gets the building panel for a given building.
	 * @param building the given building
	 * @return the building panel or null if none.
	 */
	private BuildingPanel getBuildingPanel(Building building) {
		BuildingPanel result = null;
		Iterator<BuildingPanel> i = buildingPanels.iterator();
		while (i.hasNext()) {
			BuildingPanel panel = i.next();
			if (panel.getBuilding() == building) {
				
				// 2014-10-29 Set as current building object
				setCurrentBuilding(building);
	
				result = panel;
			}
		}

		return result;
	}
}
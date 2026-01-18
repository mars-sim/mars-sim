/*
 * Mars Simulation Project
 * TabPanelCitizen.java
 * @date 2025-07-02
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.unit_window.structure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import com.mars_sim.core.Entity;
import com.mars_sim.core.location.LocationStateType;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.structure.PopulationStats;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.TemporalComponent;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.components.JDoubleLabel;
import com.mars_sim.ui.swing.entitywindow.EntityTableTabPanel;
import com.mars_sim.ui.swing.utils.AttributePanel;
import com.mars_sim.ui.swing.utils.EntityModel;

/**
 * The TabPanelCitizen is a tab panel for information on all people
 * associated with a settlement.
 */
@SuppressWarnings("serial")
class TabPanelCitizen extends EntityTableTabPanel<Settlement> implements TemporalComponent {

	private static final String CITIZEN_ICON = "people";
	
	private int populationCitizensCache = -1;
	private int populationCapacityCache = -1;
	private int populationIndoorCache = -1;

	private String genderRatioCache = "";
	
	private JDoubleLabel populationAgeLabel;
	private JLabel populationCitizensLabel;
	private JLabel populationCapacityLabel;
	private JLabel populationIndoorLabel;
	private JLabel genderRatioLabel;
	
	private PersonModel citizenModel;

	/**
	 * Constructor.
	 *
	 * @param unit    the unit to display.
	 * @param context the main desktop.
	 */
	public TabPanelCitizen(Settlement unit, UIContext context) {
		super(
			Msg.getString("settlement.population"), //$NON-NLS-1$
			ImageLoader.getIconByName(CITIZEN_ICON), null,
			unit, context
		);

		setTableTitle(Msg.getString("settlement.population"));
	}

	@Override
	protected JPanel createInfoPanel() {
		// Prepare count spring layout panel.
		AttributePanel countPanel = new AttributePanel();

		// Create associate label
		populationCitizensLabel = countPanel.addTextField(Msg.getString("TabPanelCitizen.citizen"),
											   		"", null);

		// Create population indoor label
		populationIndoorLabel = countPanel.addTextField(Msg.getString("TabPanelCitizen.indoor"),
											 		"", null);

		// Create population capacity label
		populationCapacityLabel = countPanel.addTextField(Msg.getString("TabPanelCitizen.capacity"),
											   		"", null);

		genderRatioLabel = countPanel.addTextField(Msg.getString("TabPanelCitizen.gender"),
		   								"", null);

		var settlement = getEntity();
		var pop = settlement.getAllAssociatedPeople();
		populationAgeLabel = new JDoubleLabel(StyleManager.DECIMAL_PLACES1, PopulationStats.getAverageAge(pop));
		countPanel.addLabelledItem(Msg.getString("TabPanelCitizen.age"), populationAgeLabel, null);
		clockUpdate(null);

		return countPanel;
	}

	@Override
	protected TableModel createModel() {
		citizenModel = new PersonModel(getEntity());

		return citizenModel;
	}

	@Override
	public void clockUpdate(ClockPulse pulse) {
		var settlement = getEntity();

		int num0 = settlement.getNumCitizens();
		// Update citizen num
		if (populationCitizensCache != num0) {
			populationCitizensCache = num0;
			populationCitizensLabel.setText(Integer.toString(populationCitizensCache));
		}

		int num = settlement.getIndoorPeopleCount();
		// Update indoor num
		if (populationIndoorCache != num) {
			populationIndoorCache = num;
			populationIndoorLabel.setText(Integer.toString(populationIndoorCache));
		}

		int cap = settlement.getPopulationCapacity();
		// Update capacity
		if (populationCapacityCache != cap) {
			populationCapacityCache = cap;
			populationCapacityLabel.setText(Integer.toString(populationCapacityCache));
		}
		
		var pop = settlement.getAllAssociatedPeople();
		var ratio = PopulationStats.getGenderRatioAsString(pop);
		// Update gender ratio
		if (!genderRatioCache.equals(ratio)) {
			genderRatioCache = ratio;
			genderRatioLabel.setText(genderRatioCache);
		}

		// Update average age
		populationAgeLabel.setValue(PopulationStats.getAverageAge(pop));
		
		// Update population table
		if (citizenModel != null) {
			citizenModel.update();
		}
	}

	/**
	 * Table model showing all Persons in a Settlement
	 */
	private static class PersonModel extends AbstractTableModel implements EntityModel {

		private Settlement settlement;
		private List<Person> citizens = Collections.emptyList();


		private PersonModel(Settlement settlement) {
			this.settlement = settlement;
			update();
		}

		private void update() {
			var newCitizens = settlement.getAllAssociatedPeople();
			if (!newCitizens.equals(citizens)) {
				citizens = new ArrayList<>(newCitizens);

				// reload the whole table
				fireTableDataChanged();
			}
			else if (!citizens.isEmpty()) {
				fireTableRowsUpdated(0, citizens.size()-1);
			}
		}

		@Override
		public int getRowCount() {
			return citizens.size();
		}

		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			if (columnIndex == 1) {
				return Boolean.class;
			}
			return String.class;
		}

		@Override
		public String getColumnName(int columnIndex) {
			return switch(columnIndex) {
				case 0 -> Msg.getString("Entity.name");
				case 1 -> "Inside";
				default -> "";
			};
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			var c = citizens.get(rowIndex);
			return switch(columnIndex) {
				case 0 -> c.getName();
				case 1 -> c.getLocationStateType() == LocationStateType.INSIDE_SETTLEMENT;
				default -> "";
			};
		}

		@Override
		public Entity getAssociatedEntity(int row) {
			return citizens.get(row);
		}
	}
}
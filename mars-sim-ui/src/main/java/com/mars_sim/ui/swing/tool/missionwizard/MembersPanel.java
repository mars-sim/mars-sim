/*
 * Mars Simulation Project
 * MembersPanel.java
 * @date 2021-08-27
 * @author Scott Davis
 */

package com.mars_sim.ui.swing.tool.missionwizard;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.PhysicalConditionFormat;
import com.mars_sim.core.person.ai.mission.Delivery;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.components.ColumnSpec;
import com.mars_sim.ui.swing.tool.mission.create.MissionDataBean;
import com.mars_sim.ui.swing.utils.wizard.WizardItemModel;
import com.mars_sim.ui.swing.utils.wizard.WizardItemStep;
/**
 * A wizard panel for selecting settlers.
 */
@SuppressWarnings("serial")
class MembersPanel extends WizardItemStep<MissionDataBean, Person>
{

	/** The wizard panel name. */
	public static final String ID = "Members";
	
	/**
	 * Constructor.
	 * 
	 * @param wizard the create mission wizard
	 */
	public MembersPanel(MissionCreate wizard, MissionDataBean state) {
		// Use WizardPanel constructor
		super(ID, wizard, new PeopleTableModel(state), 2,
				getCapacity(state));

	}

	/**
	 * Update 
	 */
	@Override
	protected void updateState(MissionDataBean state, List<Person> selection) {
		List<Worker> selectedWorkers = new ArrayList<>();
		selection.forEach(selectedWorkers::add);
				
		state.setMixedMembers(selectedWorkers);
	}

	/**
	 * Clears information on the wizard panel.
	 */
	@Override
	public void clearState(MissionDataBean state) {
		state.setMixedMembers(null);
		super.clearState(state);
	}

	/**
	 * Gets the remaining vehicle capacity.
	 * 
	 * @return vehicle capacity.
	 */
	private static int getCapacity(MissionDataBean state) {
		return switch (state.getMissionType()) {
			case CONSTRUCTION -> Integer.MAX_VALUE;
			case DELIVERY -> Delivery.MAX_MEMBERS;
			default -> state.getRover().getCrewCapacity();
		};
	}

	/**
	 * Table model for people.
	 */
	private static class PeopleTableModel extends WizardItemModel<Person> {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

		private static final List<ColumnSpec> COLUMNS = List.of(
				new ColumnSpec(Msg.getString("entity.name"), String.class),
				new ColumnSpec(Msg.getString("person.job"), String.class),
				new ColumnSpec(Msg.getString("person.shift"), String.class),
				new ColumnSpec(Msg.getString("mission.singular"), String.class),
				new ColumnSpec(Msg.getString("person.performance"), Double.class, ColumnSpec.STYLE_PERCENTAGE),
				new ColumnSpec(Msg.getString("person.health"), String.class)
		);

		/** Constructor. */
		private PeopleTableModel(MissionDataBean state) {
			super(COLUMNS);

			List<Person> people = state.getStartingSettlement().getIndoorPeople().stream()
					.sorted(Comparator.comparing(Person::getName))
					.toList();
			setItems(people);
		}

		/**
		 * Failure is if the Person is already assigned to a mission.
		 */
		@Override
		protected boolean isFailureCell(Person item, int column) {
			return (column == 3 && item.getMind().getMission() != null);
		}

		/**
		 * Gets the value for the cell at columnIndex and rowIndex.
		 * 
		 * @param item the item.
		 * @param column the column index.
		 * @return Rendered values
		 */
		@Override
		protected Object getItemValue(Person item, int column) {
			return switch (column) {
				case 0 -> item.getName();
				case 1 -> item.getMind().getJobType().getName();
				case 2 -> item.getShiftSlot().getStatusDescription();
				case 3 -> {
					Mission mission = item.getMind().getMission();
					if (mission != null) yield mission.getName();
					else yield null;
				}
				case 4 -> item.getPerformanceRating() * 100D;
				case 5 -> PhysicalConditionFormat.getHealthSituation(item.getPhysicalCondition());
				default -> null;
			}; 
		}
	}
}

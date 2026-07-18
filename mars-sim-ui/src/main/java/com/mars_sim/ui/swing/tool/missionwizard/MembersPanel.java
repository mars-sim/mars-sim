/*
 * Mars Simulation Project
 * MembersPanel.java
 * @date 2021-08-27
 * @author Scott Davis
 */

package com.mars_sim.ui.swing.tool.missionwizard;

import java.util.Comparator;
import java.util.List;

import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.mission.Delivery;
import com.mars_sim.ui.swing.components.ColumnSpec;
import com.mars_sim.ui.swing.utils.model.BasePersonModel;
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
		super(ID, wizard, new MembersTableModel(state), 2,
				getCapacity(state));

	}

	/**
	 * Update 
	 */
	@Override
	protected void updateState(MissionDataBean state, List<Person> selection) {				
		state.setPersonMembers(selection);
	}

	/**
	 * Clears information on the wizard panel.
	 */
	@Override
	public void clearState(MissionDataBean state) {
		state.setPersonMembers(null);
		super.clearState(state);
	}

	/**
	 * Gets the remaining vehicle capacity.
	 * 
	 * @return vehicle capacity.
	 */
	private static int getCapacity(MissionDataBean state) {
		var capacity = state.getMetaMission().getDefaultCapacity();
		if (state.getRover() != null) {
			capacity = Math.min(capacity, state.getRover().getCrewCapacity());
		}
		return capacity;
	}

	private static class MembersTableModel extends BasePersonModel implements WizardItemModel<Person> {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

		/** Constructor. */
		private MembersTableModel(MissionDataBean state) {
			super(NAME, JOB, SHIFT, MISSION, PERFORMANCE, HEALTH);

			List<Person> people = state.getStartingSettlement().getIndoorPeople().stream()
					.sorted(Comparator.comparing(Person::getName))
					.toList();
			setEntities(people);

			enableListeners(true);
		}

		@Override
		public Person getItem(int row) {
			return (Person)getAssociatedEntity(row);
		}

		@Override
		public String isFailureCell(int row, int column) {
			ColumnSpec columnSpec = getColumnSpec(column);
			if (columnSpec.equals(MISSION.column())) {
				Person person = getItem(row);
				if (person.getMind().getMission() != null) {
					return MissionCreate.ALREADY_ON_MISSION;
				}
			}
			return null;
		}	
	}
}

/*
 * Mars Simulation Project
 * PersonTableModel.java
 * @date 2024-07-21
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.tool.monitor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.mars_sim.core.EntityEvent;
import com.mars_sim.core.EntityEventType;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.utils.model.BasePersonModel;

/**
 * The PersonTableModel that maintains a list of Person objects. By defaults the
 * source of the list is the Unit Manager. It maps key attributes of the Person
 * into Columns.
 */
@SuppressWarnings("serial")
public class PersonTableModel extends BasePersonModel
		implements FilteredTableModel, MonitorModel {

	private static final String PEOPLE = Msg.getString("person.plural");
	private static final String LIVE = "Show Alive";
	private static final String DECEASED = "Show Deceased";
	
	private boolean isLiveCB = true;
	private boolean isDeceasedCB = false;
	private Set<Settlement> settlements = new HashSet<>();

	/**
	 * Constructs a PersonTableModel that displays residents are all associated
	 * people with a specified settlement.
	 *
	 */
	public PersonTableModel()  {
		super (NAME, TASK, SETTLEMENT, MISSION, HEALTH, ENERGY, WATER, FATIGUE, STRESS,
				PERFORMANCE, EMOTION, LOCATION, LOCALE, ROLE, JOB, SHIFT);
	}

	@Override
	public String getName() {
		return PEOPLE;
	}

	@Override
	public int getSettlementColumn() {
		return 2;
	}

	/**
	 * Sets the settlement filter for the model. This will select the Persons associated with the selected settlements.
	 * @param filter New settlements
	 * @return
	 */
	@Override
	public boolean setSettlementFilter(Set<Settlement> filter) {	

		settlements.forEach(s -> s.removeEntityListener(this));

		settlements = filter;

		reapplyFilter();

		// Listen to the settlements for new People
		settlements.forEach(s -> s.addEntityListener(this));

		return true;
	}

	/**
	 * Find the relevant Person by apply filters.
	 */
	private void reapplyFilter() {
		Collection<Person> entities = settlements.stream()
					.map(Settlement::getAllAssociatedPeople)
					.flatMap(Collection::stream)
					.filter(this::isPersonDisplayable)
					.toList();
		
		setEntities(entities);
	}

	/**
	 * Is the Person displayable based on the alive/deceased filters.
	 * @param p
	 * @return
	 */
	private boolean isPersonDisplayable(Person p) {
		if (!isLiveCB && !isDeceasedCB) {
			return false;
		}
		if (p.isDeclaredDead()) {
			return isDeceasedCB;
		}
		return isLiveCB;
	}

	/**
	 * Get a list of the supported filters and their active state based on the alive state of Persons
	 * @return
	 */	
	@Override
	public List<FilteredTableModel.Filter> getActiveFilters() {
		var filters = new ArrayList<FilteredTableModel.Filter>();
		filters.add(new Filter(LIVE, LIVE, isLiveCB));
		filters.add(new Filter(DECEASED, DECEASED, isDeceasedCB));

		return filters;
	}

	/**
	 * Enable/disable display of a filter.
	 * @param name Name of the filter.
	 * @param selected true to display, false to block
	 */
	@Override
	public void setFilter(String name, boolean isDisplayed) {
		switch (name) {
			case LIVE -> isLiveCB = isDisplayed;
			case DECEASED -> isDeceasedCB = isDisplayed;
			default -> {
				// Do nothing as only LIVE or DECEASED filters supported
			}
		}

		// Reload
		reapplyFilter();
	}

	/**
	 * Catches changes to the Settlement
	 *
	 * @param event the unit event.
	 */
	@Override		
	public void entityUpdate(EntityEvent event) {
		if (event.getSource() instanceof Settlement s) {
			// Catch all Settlement events that add/remove associated people.
			if (event.getTarget() instanceof Person p) {
				String eventType = event.getType();
				if (EntityEventType.ADD_ASSOCIATED_PERSON_EVENT.equals(eventType)) {
					addEntity(p);
				}
				else if (EntityEventType.REMOVE_ASSOCIATED_PERSON_EVENT.equals(eventType)) {
					removeEntity(p);
				}
			}
		}
		else {
			super.entityUpdate(event);
		}
	}

	/**
	 * Prepares the model for deletion.
	 */
	@Override
	public void release() {
		settlements.forEach(s -> s.removeEntityListener(this));
		super.release();
	}
}
/**
 * Mars Simulation Project
 * SettlementListModel.java
 * @version 3.1.0 2019-12-03
 * @author Manny Kung
 */

package org.mars_sim.msp.ui.swing.tool.mission;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.AbstractListModel;
import javax.swing.SwingUtilities;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.UnitEvent;
import org.mars_sim.msp.core.UnitListener;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.UnitManagerEvent;
import org.mars_sim.msp.core.UnitManagerEventType;
import org.mars_sim.msp.core.UnitManagerListener;
import org.mars_sim.msp.core.structure.Settlement;

/**
 * List model for the settlement list.
 */
@SuppressWarnings("serial")
public class SettlementListModel extends AbstractListModel<Settlement> implements UnitManagerListener, UnitListener {

	// Private members.
	private List<Settlement> settlements;

	private static UnitManager unitManager;

	/**
	 * Constructor.
	 */
	public SettlementListModel() {
		settlements = new CopyOnWriteArrayList<>();

		unitManager = Simulation.instance().getUnitManager();

		Iterator<Settlement> i = unitManager.getSettlements().iterator();
		while (i.hasNext())
			addSettlement(i.next());

		// Add list as unit manager listener.
		unitManager.addUnitManagerListener(this);
	}

	/**
	 * Adds a settlement to this list.
	 * 
	 * @param settlement {@link Settlement} the settlement to add.
	 */
	public void addSettlement(Settlement settlement) {
		if (!settlements.contains(settlement)) {
			settlements.add(settlement);
			settlement.addUnitListener(this);
			SwingUtilities.invokeLater(new SettlementListUpdater(SettlementListUpdater.ADD, this, settlements.size() - 1));
		}
	}

	/**
	 * Removes a settlement from this list.
	 * 
	 * @param settlement {@link Settlement} settlement to remove.
	 */
	public void removeSettlement(Settlement settlement) {
		if (settlements.contains(settlement)) {
			int index = settlements.indexOf(settlement);
			settlements.remove(settlement);
			settlement.removeUnitListener(this);
			SwingUtilities.invokeLater(new SettlementListUpdater(SettlementListUpdater.REMOVE, this, index));
		}
	}


	@Override
	public void unitManagerUpdate(UnitManagerEvent event) {

		if (event.getUnit() instanceof Settlement) {

			Settlement settlement = (Settlement) event.getUnit();

			if (UnitManagerEventType.ADD_UNIT == event.getEventType()) {
				// If settlement is new, add to settlement columns.
				addSettlement(settlement);
			} else if (UnitManagerEventType.REMOVE_UNIT == event.getEventType()) {
				// If settlement is gone, remove from settlement columns.
				removeSettlement(settlement);
			}
		}
	}
	
	/**
	 * Gets the list size.
	 * 
	 * @return size.
	 */
	@Override
	public int getSize() {
		return settlements.size();
	}

	/**
	 * Gets the list element at a given index.
	 * 
	 * @param index the index.
	 * @return the object at the index or null if one.
	 */
	@Override
	public Settlement getElementAt(int index) {
		try {
			return settlements.get(index);
		} catch (IndexOutOfBoundsException e) {
			return null;
		}
	}

	/**
	 * Checks if the list contains a given settlement.
	 * 
	 * @param settlement the settlement to check for.
	 * @return true if list contains the settlement.
	 */
	public boolean containsSettlement(Settlement settlement) {
		return (settlements != null) && settlements.contains(settlement);
	}

	/**
	 * Gets the index a given settlement is at.
	 * 
	 * @param settlement the settlement to check for.
	 * @return the index for the settlement or -1 if not in list.
	 */
	public int getSettlementIndex(Settlement settlement) {
		if (containsSettlement(settlement))
			return settlements.indexOf(settlement);
		else
			return -1;
	}

	/**
	 * Prepares the list for deletion.
	 */
	public void destroy() {
		settlements.clear();
		settlements = null;
		unitManager.removeUnitManagerListener(this);
		unitManager = null;
	}

	/**
	 * Inner class for updating the settlement list.
	 */
	private class SettlementListUpdater implements Runnable {

		private static final int ADD = 0;
		private static final int REMOVE = 1;
		private static final int CHANGE = 2;

		private int mode;
		private SettlementListModel model;
		private int row;

		private SettlementListUpdater(int mode, SettlementListModel model, int row) {
			this.mode = mode;
			this.model = model;
			this.row = row;
		}

		public void run() {
			switch (mode) {
			case ADD: {
				fireIntervalAdded(model, row, row);
			}
				break;
			case REMOVE: {
				fireIntervalRemoved(model, row, row);
			}
				break;
			case CHANGE: {
				fireContentsChanged(model, row, row);
			}
				break;
			}
		}
	}

	@Override
	public void unitUpdate(UnitEvent event) {
		// TODO Auto-generated method stub
		
	}
}

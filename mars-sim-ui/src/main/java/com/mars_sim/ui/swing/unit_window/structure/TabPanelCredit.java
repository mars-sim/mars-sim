/*
 * Mars Simulation Project
 * TabPanelCredit.java
 * @date 2022-07-09
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.unit_window.structure;

import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import com.mars_sim.core.Entity;
import com.mars_sim.core.EntityManagerListener;
import com.mars_sim.core.Simulation;
import com.mars_sim.core.Unit;
import com.mars_sim.core.UnitManager;
import com.mars_sim.core.UnitType;
import com.mars_sim.core.goods.CreditEvent;
import com.mars_sim.core.goods.CreditListener;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.components.NumberCellRenderer;
import com.mars_sim.ui.swing.entitywindow.EntityTableTabPanel;
import com.mars_sim.ui.swing.utils.EntityModel;

@SuppressWarnings("serial")
class TabPanelCredit extends EntityTableTabPanel<Settlement> {
	
	private static final String CREDIT_ICON = "credit";
	private CreditTableModel creditTableModel;

	/**
	 * Constructor.
	 * @param unit {@link Unit} the unit to display.
	 * @param context {@link UIContext} the UI context.
	 */
	public TabPanelCredit(Settlement unit, UIContext context) {
		// Use TabPanel constructor.
		super(
			Msg.getString("TabPanelCredit.title"),
			ImageLoader.getIconByName(CREDIT_ICON), null,
			unit, context
		);

	}

	@Override
	protected TableModel createModel() {
		// Prepare credit table model.
		creditTableModel = new CreditTableModel(getEntity());
		return creditTableModel;
	}

	@Override
	protected void setColumnDetails(TableColumnModel columnModel) {

		columnModel.getColumn(0).setPreferredWidth(100);
		columnModel.getColumn(1).setPreferredWidth(120);
		columnModel.getColumn(2).setPreferredWidth(50);


		// Align the preference score to the center of the cell
		DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
		renderer.setHorizontalAlignment(SwingConstants.RIGHT);
		columnModel.getColumn(0).setCellRenderer(renderer);
		columnModel.getColumn(2).setCellRenderer(renderer);
		columnModel.getColumn(1).setCellRenderer(new NumberCellRenderer(3));
	}

	/**
	 * Internal class used as model for the credit table.
	 */
	private static class CreditTableModel extends AbstractTableModel implements CreditListener,
						EntityManagerListener, EntityModel {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

		// Data members
		private List<Settlement> settlements;
		private Settlement thisSettlement;
		private UnitManager unitManager = Simulation.instance().getUnitManager();

		/**
		 * hidden constructor.
		 * @param thisSettlement {@link Settlement}
		 */
		private CreditTableModel(Settlement thisSettlement) {
			this.thisSettlement = thisSettlement;

			// Get collection of all other settlements.
			settlements = new ArrayList<>();
			for(Settlement settlement : unitManager.getSettlements()) {
				if (!settlement.equals(thisSettlement)) {
					addSettlement(settlement);
				}
			}

			unitManager.addEntityManagerListener(UnitType.SETTLEMENT, this);
		}

		@Override
		public int getRowCount() {
			return settlements.size();
		}

		@Override
		public int getColumnCount() {
			return 3;
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			Class<?> dataType = super.getColumnClass(columnIndex);
			if (columnIndex == 0) dataType = String.class;
			else if (columnIndex == 1) dataType = Double.class;
			else if (columnIndex == 2) dataType = String.class;
			return dataType;
		}

		@Override
		public String getColumnName(int columnIndex) {
			if (columnIndex == 0) return Msg.getString("Settlement.singular"); //$NON-NLS-1$
			else if (columnIndex == 1) return Msg.getString("TabPanelCredit.column.credit"); //$NON-NLS-1$
			else if (columnIndex == 2) return Msg.getString("TabPanelCredit.column.type"); //$NON-NLS-1$
			else return null;
		}

		@Override
		public Object getValueAt(int row, int column) {
			if (row < getRowCount()) {
				Settlement settlement = settlements.get(row);
				if (column == 0) return settlement.getName();
				else {
					int id = settlement.getIdentifier();
					double credit = thisSettlement.getCreditManager().getCreditMap().getOrDefault(id, 0D);

					if (column == 1) return Math.round(credit*100.0)/100.0;
					else if (column == 2) {
						if (credit > 0D) return Msg.getString("TabPanelCredit.credit"); //$NON-NLS-1$
						else if (credit < 0D) return Msg.getString("TabPanelCredit.debt"); //$NON-NLS-1$
						else return null;
					}
					else return null;
				}
			}
			else return null;
		}

		/**
		 * Catch credit update event.
		 * 
		 * @param event the credit event.
		 */
		@Override
		public void creditUpdate(CreditEvent event) {
			if (
				thisSettlement.equals(event.getSettlement1()) ||
				thisSettlement.equals(event.getSettlement2())
			) {
				SwingUtilities.invokeLater(
					new Runnable() {
						@Override
						public void run() {
							fireTableDataChanged();
							// FUTURE : update only the affected row
						}
					}
				);
			}
		}

		@Override
		public void entityAdded(Entity newEntity) {
			if (newEntity instanceof Settlement newSettlement
					&& !settlements.contains(newSettlement)) {
				addSettlement(newSettlement);

				SwingUtilities.invokeLater(
					new Runnable() {
						@Override
						public void run() {
							fireTableDataChanged();
							// FUTURE : update only the affected row
						}
					}
				);
			}
		}

		/**
		 * Add this settlement to the list and register as listener.
		 * @param newSettlement Settlement to add
		 */
		private void addSettlement(Settlement newSettlement) {
			settlements.add(newSettlement);
			newSettlement.getCreditManager().addListener(this);
		}

		@Override
		public void entityRemoved(Entity removedEntity) {
			// Handle the same way as entityAdded for this use case
			if (removedEntity instanceof Settlement s
					&& settlements.contains(s)
			) {
				settlements.remove(s);
				s.getCreditManager().removeListener(this);

				SwingUtilities.invokeLater(
					new Runnable() {
						@Override
						public void run() {
							fireTableDataChanged();
							// FUTURE : update only the affected row
						}
					}
				);
			}
		}

		public void destroy() {
			unitManager.removeEntityManagerListener(UnitType.SETTLEMENT, this);
			settlements.forEach(s -> s.getCreditManager().removeListener(this));
		}

		@Override
		public Entity getAssociatedEntity(int row) {
			return settlements.get(row);
		}
	}

	/**
	 * Prepare object for garbage collection.
	 */
	@Override
	public void destroy() {
		if (creditTableModel != null) {
			creditTableModel.destroy();
		}

		super.destroy();
	}
}
/*
 * Mars Simulation Project
 * TabPanelCredit.java
 * @date 2022-07-09
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.structure;

import java.awt.Dimension;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import org.mars_sim.msp.core.CollectionUtils;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.UnitManagerEvent;
import org.mars_sim.msp.core.UnitManagerListener;
import org.mars_sim.msp.core.UnitType;
import org.mars_sim.msp.core.goods.CreditEvent;
import org.mars_sim.msp.core.goods.CreditListener;
import org.mars_sim.msp.core.goods.CreditManager;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.NumberCellRenderer;
import org.mars_sim.msp.ui.swing.tool.TableStyle;
import org.mars_sim.msp.ui.swing.tool.ZebraJTable;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;

import com.alee.laf.scroll.WebScrollPane;

@SuppressWarnings("serial")
public class TabPanelCredit
extends TabPanel {
	
	private static final String CREDIT_ICON = Msg.getString("icon.credit"); //$NON-NLS-1$

	/** The Settlement instance. */
	private Settlement settlement;

	private JTable creditTable;

	/**
	 * Constructor.
	 * @param unit {@link Unit} the unit to display.
	 * @param desktop {@link MainDesktopPane} the main desktop.
	 */
	public TabPanelCredit(Unit unit, MainDesktopPane desktop) {
		// Use TabPanel constructor.
		super(
			null,
			ImageLoader.getNewIcon(CREDIT_ICON),
			Msg.getString("TabPanelCredit.title"), //$NON-NLS-1$
			unit, desktop
		);

		settlement = (Settlement) unit;

	}

	@Override
	protected void buildUI(JPanel content) {

		// Create scroll panel for the outer table panel.
		WebScrollPane creditScrollPanel = new WebScrollPane();
		creditScrollPanel.setPreferredSize(new Dimension(280, 280));
		content.add(creditScrollPanel);

		// Prepare credit table model.
		CreditTableModel creditTableModel = new CreditTableModel(settlement);

		// Prepare credit table.
		creditTable = new ZebraJTable(creditTableModel);
		creditScrollPanel.setViewportView(creditTable);
		creditTable.setRowSelectionAllowed(true);

		creditTable.setDefaultRenderer(Double.class, new NumberCellRenderer(2, true));

		creditTable.getColumnModel().getColumn(0).setPreferredWidth(100);
		creditTable.getColumnModel().getColumn(1).setPreferredWidth(120);
		creditTable.getColumnModel().getColumn(2).setPreferredWidth(50);

		// Resizable automatically when its Panel resizes
		creditTable.setPreferredScrollableViewportSize(new Dimension(225, -1));
//		creditTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

		// Add sorting
		creditTable.setAutoCreateRowSorter(true);

		// Align the preference score to the center of the cell
		DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
		renderer.setHorizontalAlignment(SwingConstants.RIGHT);
		creditTable.getColumnModel().getColumn(0).setCellRenderer(renderer);
		creditTable.getColumnModel().getColumn(2).setCellRenderer(renderer);

		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
		creditTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);

		TableStyle.setTableStyle(creditTable);

	}

	/**
	 * Updates the info on this panel.
	 */
	@Override
	public void update() {
		TableStyle.setTableStyle(creditTable);
	}

	/**
	 * Internal class used as model for the credit table.
	 */
	private static class CreditTableModel extends AbstractTableModel implements CreditListener,
	UnitManagerListener {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

		// Data members
		private Collection<Settlement> settlements;
		private Settlement thisSettlement;
		private UnitManager unitManager = Simulation.instance().getUnitManager();

		/**
		 * hidden constructor.
		 * @param thisSettlement {@link Settlement}
		 */
		private CreditTableModel(Settlement thisSettlement) {
			this.thisSettlement = thisSettlement;

			// Get collection of all other settlements.
			settlements = new ConcurrentLinkedQueue<Settlement>();
			Iterator<Settlement> i = CollectionUtils.sortByName(unitManager.getSettlements()).iterator();
			while (i.hasNext()) {
				Settlement settlement = i.next();
				if (settlement != thisSettlement) {
					settlements.add(settlement);
					settlement.getCreditManager().addListener(this);
				}
			}

			unitManager.addUnitManagerListener(this);
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
			if (columnIndex == 0) return Msg.getString("TabPanelCredit.column.settlement"); //$NON-NLS-1$
			else if (columnIndex == 1) return Msg.getString("TabPanelCredit.column.credit"); //$NON-NLS-1$
			else if (columnIndex == 2) return Msg.getString("TabPanelCredit.column.type"); //$NON-NLS-1$
			else return null;
		}

		@Override
		public Object getValueAt(int row, int column) {
			if (row < getRowCount()) {
				Settlement settlement = (Settlement) settlements.toArray()[row];
				if (column == 0) return settlement.getName();
				else {
					double credit = 0D;
					try {
						credit = CreditManager.getCredit(thisSettlement, settlement);
					}
					catch (Exception e) {
					}

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
				(thisSettlement == event.getSettlement1()) ||
				(thisSettlement == event.getSettlement2())
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
		public void unitManagerUpdate(UnitManagerEvent event) {

			if (event.getUnit().getUnitType() == UnitType.SETTLEMENT) {
				settlements.clear();
				Iterator<Settlement> i = CollectionUtils.sortByName(unitManager.
						getSettlements()).iterator();
				while (i.hasNext()) {
					Settlement settlement = i.next();
					if (settlement != thisSettlement) {
						settlements.add(settlement);
					}
				}

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
	}

	/**
	 * Prepare object for garbage collection.
	 */
	@Override
	public void destroy() {
		creditTable = null;
	}
}

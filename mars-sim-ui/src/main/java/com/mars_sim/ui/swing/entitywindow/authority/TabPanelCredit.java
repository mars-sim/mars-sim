/*
 * Mars Simulation Project
 * TabPanelCredit.java
 * @date 2026-11-22
 */
package com.mars_sim.ui.swing.entitywindow.authority;

import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.authority.Authority;
import com.mars_sim.core.goods.CreditEvent;
import com.mars_sim.core.goods.CreditListener;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.components.NumberCellRenderer;
import com.mars_sim.ui.swing.entitywindow.EntityTableTabPanel;
import com.mars_sim.ui.swing.utils.EntityModel;
import com.mars_sim.ui.swing.utils.SwingHelper;

@SuppressWarnings("serial")
class TabPanelCredit extends EntityTableTabPanel<Authority> {

	private static final String CREDIT_ICON = "credit";
	private CreditTableModel creditTableModel;

	public TabPanelCredit(Authority authority, UIContext context) {
		super(
			Msg.getString("TabPanelCredit.title"),
			ImageLoader.getIconByName(CREDIT_ICON), null,
			authority, context
		);
	}

	@Override
	protected TableModel createModel() {
		creditTableModel = new CreditTableModel(getEntity());
		return creditTableModel;
	}

	@Override
	protected void setColumnDetails(TableColumnModel columnModel) {
		columnModel.getColumn(0).setPreferredWidth(100);
		columnModel.getColumn(1).setPreferredWidth(120);
		columnModel.getColumn(2).setPreferredWidth(50);

		DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
		renderer.setHorizontalAlignment(SwingConstants.RIGHT);
		columnModel.getColumn(0).setCellRenderer(renderer);
		columnModel.getColumn(2).setCellRenderer(renderer);
		columnModel.getColumn(1).setCellRenderer(new NumberCellRenderer(3));
	}

	private static class CreditTableModel extends AbstractTableModel
						implements CreditListener, EntityModel {

		private final List<Authority> authorities = new ArrayList<>();
		private final Authority thisAuthority;

		private CreditTableModel(Authority thisAuthority) {
			this.thisAuthority = thisAuthority;

			for (Authority authority : SimulationConfig.instance()
					.getReportingAuthorityFactory().getKnownItems()) {
				if (!authority.equals(thisAuthority)) {
					authorities.add(authority);
				}
			}

			thisAuthority.getCreditManager().addListener(this);
		}

		@Override
		public int getRowCount() {
			return authorities.size();
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
			if (columnIndex == 0) return Msg.getString("authority.singular"); //$NON-NLS-1$
			else if (columnIndex == 1) return Msg.getString("TabPanelCredit.column.credit"); //$NON-NLS-1$
			else if (columnIndex == 2) return Msg.getString("TabPanelCredit.column.type"); //$NON-NLS-1$
			else return null;
		}

		@Override
		public Object getValueAt(int row, int column) {
			if (row < getRowCount()) {
				Authority authority = authorities.get(row);
				if (column == 0) return authority.getName();
				else {
					double credit = thisAuthority.getCreditManager().getCredit(authority);

					if (column == 1) return Math.round(credit * 100.0) / 100.0;
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

		@Override
		public void creditUpdate(CreditEvent event) {
			String sourceAuthority = thisAuthority.getName();
			if (sourceAuthority.equals(event.getAuthority1())
					|| sourceAuthority.equals(event.getAuthority2())) {
				SwingHelper.runInEDT(
					CreditTableModel.this::fireTableDataChanged
				);
			}
		}

		public void destroy() {
			thisAuthority.getCreditManager().removeListener(this);
		}

		@Override
		public Authority getAssociatedEntity(int row) {
			return authorities.get(row);
		}
	}

	@Override
	public void destroy() {
		if (creditTableModel != null) {
			creditTableModel.destroy();
		}

		super.destroy();
	}
}

/*
 * Mars Simulation Project
 * TabPanelCredit.java
 * @date 2026-11-22
 */
package com.mars_sim.ui.swing.entitywindow.authority;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.table.TableModel;

import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.authority.Authority;
import com.mars_sim.core.goods.CreditEvent;
import com.mars_sim.core.goods.CreditListener;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.components.AbstractEnhancedTableModel;
import com.mars_sim.ui.swing.components.ColumnSpec;
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

	private static class CreditTableModel extends AbstractEnhancedTableModel
						implements CreditListener, EntityModel {

		private static final ColumnSpec AUTHORITY_COL = new ColumnSpec(Msg.getString("authority.singular"), String.class);
		private static final ColumnSpec CREDIT_COL = new ColumnSpec(Msg.getString("TabPanelCredit.column.credit"), Double.class, ColumnSpec.STYLE_DIGIT2);

		private final List<Authority> authorities = new ArrayList<>();
		private final Authority thisAuthority;

		private CreditTableModel(Authority thisAuthority) {
			super(AUTHORITY_COL, CREDIT_COL);
			this.thisAuthority = thisAuthority;

			refreshAuthorities();

			thisAuthority.getCreditManager().addListener(this);
		}

		private void refreshAuthorities() {
			authorities.clear();
			var authorityFactory = SimulationConfig.instance().getReportingAuthorityFactory();
			var authorityNames = new ArrayList<>(thisAuthority.getCreditManager().getCreditMap().keySet());
			Collections.sort(authorityNames);
			for (String authorityName : authorityNames) {
				Authority authority = authorityFactory.getItem(authorityName);
				if (authority != null && !authority.equals(thisAuthority)) {
					authorities.add(authority);
				}
			}
		}

		@Override
		public int getRowCount() {
			return authorities.size();
		}

		@Override
		public Object getValueAt(int row, int column) {
			Authority authority = authorities.get(row);
			return switch (column) {
				case 0 -> authority.getName();
				case 1 -> thisAuthority.getCreditManager().getCredit(authority);
				default -> null;
			};
		}

		@Override
		public void creditUpdate(CreditEvent event) {
			SwingHelper.runInEDT(() -> {
				refreshAuthorities();
				fireTableDataChanged();
			});
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

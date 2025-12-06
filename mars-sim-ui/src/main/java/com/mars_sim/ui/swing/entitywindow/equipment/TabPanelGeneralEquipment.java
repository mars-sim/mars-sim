/*
 * Mars Simulation Project
 * TabPanelGeneralEquipment.java
 * @date 2024-08-14
 * @author Manny Kung
 */
package com.mars_sim.ui.swing.entitywindow.equipment;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JPanel;

import com.mars_sim.core.data.History;
import com.mars_sim.core.equipment.EVASuit;
import com.mars_sim.core.equipment.Equipment;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.unit.UnitHolder;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.TemporalComponent;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.components.EntityLabel;
import com.mars_sim.ui.swing.entitywindow.EntityTabPanel;
import com.mars_sim.ui.swing.utils.AttributePanel;
import com.mars_sim.ui.swing.utils.ColumnSpec;
import com.mars_sim.ui.swing.utils.JHistoryPanel;

/**
 * This tab displays general information about an equipment.
 */
@SuppressWarnings("serial")
class TabPanelGeneralEquipment extends EntityTabPanel<Equipment> 
	implements TemporalComponent {

	private static final String ID_ICON = "info"; //$NON-NLS-1$

	private HistoryPanel historyPanel;

	private EntityLabel ownerLabel;
		
	/**
	 * Constructor.
	 * 
	 * @param eqm the equipment to display.
	 * @param context the UI context.
	 */
	public TabPanelGeneralEquipment(Equipment eqm, UIContext context) {
		// Use the TabPanel constructor
		super(
			Msg.getString("EntityGeneral.title"), //-NLS-1$
			ImageLoader.getIconByName(ID_ICON),		
			Msg.getString("EntityGeneral.tooltip"),
			context, eqm
		);
	}
	
	@Override
	protected void buildUI(JPanel content) {

		// Prepare spring layout info panel.
		AttributePanel infoPanel = new AttributePanel();
		
		content.add(infoPanel, BorderLayout.NORTH);
		
		var eqm = getEntity();
		infoPanel.addTextField("Type", eqm.getEquipmentType().getName(), null);
		infoPanel.addTextField("Mass", StyleManager.DECIMAL_KG2.format(eqm.getBaseMass()), null);

		ownerLabel = new EntityLabel(eqm.getRegisteredOwner(), getContext());
		infoPanel.addLabelledItem("Registered Owner", ownerLabel, null);
		
		if (eqm instanceof EVASuit suit) {	
			historyPanel = new HistoryPanel(suit.getHistory());
			historyPanel.setPreferredSize(new Dimension(225, 200));
	
			content.add(historyPanel, BorderLayout.CENTER);
		}
	}
	
	/**
     * Updates content panel with clock pulse information.
     * @param pulse Pulse information,
     */
	@Override
    public void clockUpdate(ClockPulse pulse) {
		ownerLabel.setEntity(getEntity().getRegisteredOwner());
		if (historyPanel != null) {
			historyPanel.refresh();
		}
	}

	/**
	 * Internal class used as model for the attribute table.
	 */
	private static class HistoryPanel extends JHistoryPanel<UnitHolder> {
		private static final ColumnSpec[] COLUMNS = {new ColumnSpec("Location", String.class)};

		HistoryPanel(History<UnitHolder> source) {
			super(source, COLUMNS);
		}

		@Override
		protected Object getValueFrom(UnitHolder value, int columnIndex) {
			return value.getName();
		}
	}
}

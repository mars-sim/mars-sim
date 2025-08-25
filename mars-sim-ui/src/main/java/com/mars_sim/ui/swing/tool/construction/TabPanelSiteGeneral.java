/**
 * Mars Simulation Project
 * TabPanelSiteGeneral.java
 * @date 2023-06-07
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.tool.construction;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.AbstractTableModel;

import com.mars_sim.core.building.construction.ConstructionSite;
import com.mars_sim.core.building.construction.ConstructionSite.ConstructionPhase;
import com.mars_sim.core.building.construction.ConstructionStage;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.MainDesktopPane;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.components.EntityLabel;
import com.mars_sim.ui.swing.unit_window.TabPanel;
import com.mars_sim.ui.swing.utils.AttributePanel;
import com.mars_sim.ui.swing.utils.ConstructionStageFormat;
import com.mars_sim.ui.swing.utils.ToolTipTableModel;

/**
 * The TabPanelSiteGeneral is a tab panel for general information about a construction site.
 */
@SuppressWarnings("serial")
public class TabPanelSiteGeneral extends TabPanel {

	private static final String ID_ICON = "info"; //-NLS-1$
	
	/** The ConstructionSite instance. */
	private ConstructionSite constructionSite;

	private EntityLabel missionLabel;

	private PhaseTableModel phaseModel;
	private ConstructionStage currentStage;

	private double lastWorkLeft = 0D;
	private JLabel stageName;
	private JLabel stageType;
	private JLabel workLeft;
	
	/**
	 * Constructor.
	 * 
	 * @param unit the unit to display.
	 * @param desktop the main desktop.
	 */
	public TabPanelSiteGeneral(ConstructionSite unit, MainDesktopPane desktop) {
		// Use the TabPanel constructor
		super(
			Msg.getString("TabPanelGeneral.title"), //-NLS-1$
			ImageLoader.getIconByName(ID_ICON),		
			Msg.getString("TabPanelGeneral.title"), //-NLS-1$
			desktop
		);

		constructionSite = unit;
	}
	
	@Override
	protected void buildUI(JPanel content) {

		// Prepare spring layout info panel.
		AttributePanel infoPanel = new AttributePanel();
		
		content.add(infoPanel, BorderLayout.NORTH);

		String name = constructionSite.getName();

		infoPanel.addTextField("Site Name", name, null);
		infoPanel.addTextField("Building Type", constructionSite.getBuildingName(), null);
		stageName = infoPanel.addTextField("Current Stage", "", null);
		stageType = infoPanel.addTextField("Current Stage Type", "", null);

		missionLabel = new EntityLabel(constructionSite.getWorkOnSite(), getDesktop());
		infoPanel.addLabelledItem("Work Mission", missionLabel);
        workLeft = infoPanel.addTextField("Stage Work", "", null);

		phaseModel = new PhaseTableModel();
		var phaseTable = new JTable(phaseModel) {
			@Override
            public String getToolTipText(MouseEvent e) {
                return ToolTipTableModel.extractToolTip(e, this);
            }
		};
		phaseTable.setPreferredScrollableViewportSize(new Dimension(225, -1));

		var scrollPane = StyleManager.createScrollBorder("Remaining Phases", phaseTable);
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		content.add(scrollPane, BorderLayout.CENTER);

		update();
	}

	@Override
	public void update() {
		super.update();
		
		missionLabel.setEntity(constructionSite.getWorkOnSite());
		phaseModel.update(constructionSite.getRemainingPhases());

		ConstructionStage stage = constructionSite.getCurrentConstructionStage();
        if (stage != null) {
			double workRemaining = stage.getRequiredWorkTime() - stage.getCompletedWorkTime();
			if (workRemaining != lastWorkLeft) {
				lastWorkLeft = workRemaining;
				workLeft.setText(StyleManager.DECIMAL_MSOL.format(lastWorkLeft));
			}
        }

		var activeStage = constructionSite.getCurrentConstructionStage();
		if (!activeStage.equals(currentStage)) {
			currentStage = activeStage;

			var info = currentStage.getInfo();
			stageName.setText(info.getName());
			stageType.setText(info.getType().name().toLowerCase());
		}
	}

	private class PhaseTableModel extends AbstractTableModel 
						implements ToolTipTableModel {

		private List<ConstructionPhase> phases = Collections.emptyList();
		
		@Override
		public int getRowCount() {
			return phases.size();
		}

		public void update(List<ConstructionPhase> remainingPhases) {
			if (!phases.equals(remainingPhases)) {
				phases = remainingPhases;
				fireTableDataChanged();
			}
		}

		@Override
		public int getColumnCount() {
			return 4;
		}

		@Override
		public String getColumnName(int column) {	
			return switch (column) {
				case 0 -> "Stage";
				case 1 -> "Type";
				case 2 -> "Work";
				case 3 -> "Work Time";
				default -> null;
			};
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			var p = phases.get(rowIndex);
			return switch (columnIndex) {
				case 0 -> p.stage().getName();
				case 1 -> p.stage().getType().name().toLowerCase();
				case 2 -> p.construct() ? "Construct" : "Salvage";
				case 3 -> p.stage().getWorkTime();
				default -> null;
			};
		}
		
		@Override
		public String getToolTipAt(int row, int col) {
			var selected = phases.get(row);

			return ConstructionStageFormat.getTooltip(selected.stage());
		}
	}
}

/*
 * Mars Simulation Project
 * BuildingPanelManufacture.java
 * @date 2022-07-10
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.entitywindow.building;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.function.Manufacture;
import com.mars_sim.core.building.function.Manufacture.ToolCapacity;
import com.mars_sim.core.manufacture.Tooling;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.TemporalComponent;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.entitywindow.EntityTabPanel;
import com.mars_sim.ui.swing.utils.AttributePanel;
import com.mars_sim.ui.swing.utils.ProcessListPanel;
import com.mars_sim.ui.swing.utils.SwingHelper;
import com.mars_sim.ui.swing.utils.ToolTipTableModel;

/**
 * A building panel displaying the manufacture building function.
 */
@SuppressWarnings("serial")
class BuildingPanelManufacture extends EntityTabPanel<Building>
	implements TemporalComponent {

	private static final String MANU_ICON = "manufacture";

	/** The manufacture building. */
	private Manufacture workshop;
	/** Panel for displaying process panels. */
	private ProcessListPanel processListPane;

	private ToolModel tools;

	/**
	 * Constructor.
	 * 
	 * @param workshop the manufacturing building function.
	 * @param context the UI context
	 */
	public BuildingPanelManufacture(Manufacture workshop, UIContext context) {
		// Use BuildingFunctionPanel constructor.
		super(
			Msg.getString("BuildingPanelManufacture.title"),
			ImageLoader.getIconByName(MANU_ICON), null,
			context, workshop.getBuilding() 
		);

		// Initialize data model.
		this.workshop = workshop;
	}
	
	/**
	 * Builds the UI.
	 */
	@Override
	protected void buildUI(JPanel center) {
		var topPanel = new JPanel(new BorderLayout(0, 0));
		center.add(topPanel, BorderLayout.NORTH);

		// Prepare label panel
		AttributePanel labelPanel = new AttributePanel(1, 2);
		topPanel.add(labelPanel, BorderLayout.NORTH);
		labelPanel.addTextField("Tech Level", Integer.toString(workshop.getTechLevel()), null);
		labelPanel.addTextField("Process Capacity", Integer.toString(workshop.getMaxProcesses()), null);

		JScrollPane tscrollPanel = new JScrollPane();
		tscrollPanel.setPreferredSize(new Dimension(160, 130));

		topPanel.add(tscrollPanel, BorderLayout.CENTER);
		tscrollPanel.setBorder(SwingHelper.createLabelBorder("Tools"));

		tools = new ToolModel(workshop.getToolDetails());

		var table = new JTable(tools) {
            @Override          
            public String getToolTipText(MouseEvent e) {
                return ToolTipTableModel.extractToolTip(e, this);
            }
		};
		table.setCellSelectionEnabled(false);
		table.setRowSelectionAllowed(false);

		tscrollPanel.setViewportView(table);

		// Create scroll pane for manufacturing processes
		var scrollPanel = new JScrollPane();
		scrollPanel.setPreferredSize(new Dimension(170, 90));
		center.add(scrollPanel, BorderLayout.CENTER);

		// Create process list main panel
		JPanel processListMainPane = new JPanel(new BorderLayout(0, 0));
		scrollPanel.setViewportView(processListMainPane);

		// Create process list panel
		processListPane = new ProcessListPanel(false, getContext());
		processListMainPane.add(processListPane, BorderLayout.NORTH);
		processListPane.update(workshop.getProcesses());
	}


	@Override
	public void clockUpdate(ClockPulse pulse) {
		var processes = workshop.getProcesses();
		processListPane.update(processes);

		tools.update(workshop.getToolDetails());

	}

	private class ToolModel extends AbstractTableModel implements ToolTipTableModel {
		private static final long serialVersionUID = 1L;

		private List<Tooling> tool = new ArrayList<>();
		private List<Integer> inuse = new ArrayList<>();
		private List<Integer> capacity = new ArrayList<>();

		private ToolModel(Map<Tooling, ToolCapacity> toolDetails) {

			toolDetails.forEach((name, details) -> {
				tool.add(name);
				inuse.add(details.getInUse());
				capacity.add(details.getCapacity());
			});
		}

		private void update(Map<Tooling, ToolCapacity> toolDetails) {

			toolDetails.forEach((name, details) -> {
				int idx = tool.indexOf(name);
				var newInUse = details.getInUse();
				if (idx >= 0 && inuse.get(idx) != newInUse) {
					inuse.set(idx, newInUse);
					fireTableCellUpdated(idx, 1);
				}
			});
		}
		
		@Override
		public int getRowCount() {
			return tool.size();
		}

		@Override
		public int getColumnCount() {
			return 3;
		}

		@Override
		public String getColumnName(int columnIndex) {
			switch(columnIndex) {
				case 0:
					return "Tool";
				case 1:
					return "In Use";
				case 2:
					return "Capacity";
				default:
					return null;
			}
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			return switch (columnIndex) {
				case 0 -> tool.get(rowIndex).name();
				case 1 -> inuse.get(rowIndex);
				case 2 -> capacity.get(rowIndex);

				default -> null;
			};
		}

		@Override
		public String getToolTipAt(int row, int col) {
			if (col == 0) {
				return tool.get(row).description();
			}
			return null;
		}
	}
}

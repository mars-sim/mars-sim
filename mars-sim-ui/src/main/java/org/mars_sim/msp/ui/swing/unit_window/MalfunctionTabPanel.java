/*
 * Mars Simulation Project
 * MalfunctionTabPanel.java
 * @date 2022-08-02
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.malfunction.Malfunction;
import org.mars_sim.msp.core.malfunction.MalfunctionRepairWork;
import org.mars_sim.msp.core.malfunction.Malfunctionable;
import org.mars_sim.msp.core.resource.ItemResourceUtil;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.utils.PercentageCellRenderer;
import org.mars_sim.msp.ui.swing.utils.UnitModel;
import org.mars_sim.msp.ui.swing.utils.UnitTableLauncher;


/**
 * The MalfunctionTabPanel class is a building function panel
 * representing the malfunctions of a settlement building.
 */
@SuppressWarnings("serial")
public class MalfunctionTabPanel extends TabPanel {

	private static final String WARN_ICON = "warn";
	private static final int NAME = 0;
	private static final int SOURCE = 1;
	private static final int EVA_WORK = 2;
	private static final int INSIDE_WORK = 3;
	private static final int COMPLETED = 4;

	private static final String NONE = "None.";
	private static final String ONE_SPACE = " ";
	private static final String COMMA = ", ";
	private static final String DOT = ".";
	private static final String BR = "<br>";
	private static final String HTML_START = "<html>";
	private static final String REPAIR_TIME = " Repair Time: ";
	private static final String SLASH = " / ";
	private static final String MILLISOLS = " millisols";
	private static final String HTML_END = "</html>";
	private static final String REPAIR_PARTS_NEEDED = "Parts Needed:";


	 /**
     * Private table model to manage the Resource Processes. It can act in 2 modes
     * - Single building mode
     * - Multiple building mode
     */
	private static class MalfunctionTableModel extends AbstractTableModel
                implements UnitModel {

        private List<Malfunction> malfunctions;

		private boolean showSource;

		public MalfunctionTableModel(List<Malfunction> source, boolean showSource) {
			malfunctions = new ArrayList<>(source);
			this.showSource = showSource;
		}

        @Override
		public int getRowCount() {
			return malfunctions.size();
		}

        @Override
		public int getColumnCount() {
            if (showSource)
			    return 5;
            else
                return 4;
		}

        @Override
		public Class<?> getColumnClass(int columnIndex) {
            int realColumn = getPropFromColumn(columnIndex);
            switch(realColumn) {
                case NAME: 
                case SOURCE: 
                case EVA_WORK: 
				case INSIDE_WORK: return String.class;
				case COMPLETED: return Integer.class;
                default:
                    throw new IllegalArgumentException("Column unknown " + columnIndex);
            }
		}

        @Override
		public String getColumnName(int columnIndex) {
            int realColumn = getPropFromColumn(columnIndex);
            switch(realColumn) {
				case NAME:  return "Name";
                case SOURCE: return "Source";
                case EVA_WORK: return "EVA" ;
				case INSIDE_WORK: return "Inside";
				case COMPLETED: return "Fix";
                default:
                    throw new IllegalArgumentException("Column unknown " + columnIndex);
            }
		}

        @Override
		public Object getValueAt(int row, int column) {
            Malfunction p = malfunctions.get(row);
            int realColumn = getPropFromColumn(column);
            switch(realColumn) {
                case NAME: return p.getName();
                case COMPLETED: return (int)p.getPercentageFixed();
                case SOURCE: return p.getSource().getName();
				case EVA_WORK: return createRepairers(p, MalfunctionRepairWork.EVA);
				case INSIDE_WORK: return createRepairers(p, MalfunctionRepairWork.INSIDE);
                default:
                    throw new IllegalArgumentException("Column unknown " + column);
            }
		}

        private static String createRepairers(Malfunction p, MalfunctionRepairWork e) {
			int needed = p.getDesiredRepairers(e);
			if (needed > 0) {
				StringBuilder b = new StringBuilder();
				b.append(needed - p.numRepairerSlotsEmpty(e)).append("/").append(needed);
				return b.toString();
			}
			return "None";
		}

		/**
         * This maps the column index into the logical property
         * @param column
         * @return
         */
        private int getPropFromColumn(int column) {
			if (column == 0) {
				return NAME;
			}
			else if (!showSource) {
				return column + 1;
			}
			return column;
        }

        @Override
        public Unit getAssociatedUnit(int row) {
            return (Unit)malfunctions.get(row).getSource();
        }

		public void update(List<Malfunction> newMalfunctions) {
			boolean changed = false;

			if (newMalfunctions.isEmpty() && !malfunctions.isEmpty()) {
				// No new malfunction but old in table
				malfunctions.clear();
				changed = true;
			}
			else if (malfunctions.isEmpty()) {
				// New malfunction but none in model
				malfunctions.addAll(newMalfunctions);
				changed = true;
			}
			else {
				// Malfunctions on both sides; need to do a Union
				List<Malfunction> rowsToDelete = new ArrayList<>(malfunctions);
				rowsToDelete.removeAll(newMalfunctions);

				List<Malfunction> rowsToAdd = new ArrayList<>(newMalfunctions);
				rowsToAdd.removeAll(malfunctions);

				malfunctions.removeAll(rowsToDelete);
				malfunctions.addAll(rowsToAdd);
				changed = true;
			}

			if (changed || !malfunctions.isEmpty()) {
				fireTableDataChanged();
			}
		}

		public Malfunction getMalfunction(int rowIndex) {
			return malfunctions.get(rowIndex);
		}
	}
	
	/** The malfunctionable building. */
	private Malfunctionable malfunctionable;
	
	private MalfunctionTableModel model;
	private boolean showSource;
	private Settlement settlement;

	/**
	 * Builds the panel that show sMalfunctions of a entity that can break.
	 * 
	 * @param malfunctionable the malfunctionable the panel is for.
	 * @param desktop         The main desktop.
	 */
	public MalfunctionTabPanel(Malfunctionable malfunctionable, MainDesktopPane desktop) {

		super(
			Msg.getString("BuildingPanelMalfunctionable.title"), 
			ImageLoader.getIconByName(WARN_ICON), 
			Msg.getString("BuildingPanelMalfunctionable.title"), 
			desktop
		);

		this.malfunctionable = malfunctionable;
		this.showSource = false;
		this.model = new MalfunctionTableModel(malfunctionable.getMalfunctionManager().getMalfunctions(), showSource);
	}
	
	/**
	 * Shows a panel for a Settlement that controls number entity that can fail. Create an aggregated report.
	 * 
	 * @param settlement Being displayed
	 * @param desktop Owner
	 */
	public MalfunctionTabPanel(Settlement settlement, MainDesktopPane desktop) {
		super(
			Msg.getString("BuildingPanelMalfunctionable.title"), 
			ImageLoader.getIconByName(WARN_ICON), 
			Msg.getString("BuildingPanelMalfunctionable.title"), 
			desktop
		);
		this.settlement = settlement;

		this.showSource = true;
		List<Malfunction> malfunctions = createSettlementMalfunction(settlement);
		this.model = new MalfunctionTableModel(malfunctions, showSource);
	}

	private static List<Malfunction> createSettlementMalfunction(Settlement s) {
		List<Malfunction> active = new ArrayList<>();
		for(Building b : s.getBuildingManager().getBuildingSet()) {
			active.addAll(b.getMalfunctionManager().getMalfunctions());
		}

		return active;
	}

	/**
	 * Builds the UI.
	 */
	@Override
	protected void buildUI(JPanel center) {

		// Create scroll panel for table
		JScrollPane scrollPanel = new JScrollPane();
		scrollPanel.getViewport().setOpaque(false);
		scrollPanel.setPreferredSize(new Dimension(200, -1));

		JTable mTable = new JTable(model) {
			//Implement table cell tool tips.           
			public String getToolTipText(MouseEvent e) {
				Point p = e.getPoint();
				
				int rowIndex = rowAtPoint(p);
				if (rowIndex < 0) {
					return null;
				}
				rowIndex = getRowSorter().convertRowIndexToModel(rowIndex);

				int dataColumn = model.getPropFromColumn(columnAtPoint(p));
				switch(dataColumn) {
					case SOURCE:
					case NAME: return generateToolTip(model.getMalfunction(rowIndex));
					case EVA_WORK: return "Number of repairers active on EVA";
					case INSIDE_WORK: return "Number of repairers active inside";
					case COMPLETED: return "%age repaired";
					default: return "";
				}
			}
		};

		mTable.setCellSelectionEnabled(false);
		mTable.setAutoCreateRowSorter(true);
		scrollPanel.setViewportView(mTable);

        TableColumnModel columnModel = mTable.getColumnModel();

		int offset = 0;
		if (showSource) {
			mTable.addMouseListener(new UnitTableLauncher(getDesktop()));
			columnModel.getColumn(SOURCE).setPreferredWidth(80);
		}
		else {
			offset = 1;
		}
		columnModel.getColumn(NAME).setPreferredWidth(100);
        columnModel.getColumn(EVA_WORK - offset).setMaxWidth(50);
        columnModel.getColumn(INSIDE_WORK- offset).setMaxWidth(50);
        columnModel.getColumn(COMPLETED- offset).setMaxWidth(PercentageCellRenderer.DEFAULT_WIDTH);
		columnModel.getColumn(COMPLETED- offset).setCellRenderer(new PercentageCellRenderer(true));


        center.add(scrollPanel, BorderLayout.CENTER);
	}

	
	/**
	 * Creates multi-line tool tip text.
	 */
	private static String generateToolTip(Malfunction malfunction) {
		StringBuilder result = new StringBuilder(HTML_START);
		result.append(malfunction.getName()).append(BR);
		for (MalfunctionRepairWork workType : MalfunctionRepairWork.values()) {
			if (malfunction.getWorkTime(workType) > 0) {
				result.append(workType.getName()).append(REPAIR_TIME)
					  .append((int) malfunction.getCompletedWorkTime(workType))
					  .append(SLASH)
					  .append((int) malfunction.getWorkTime(workType))
					  .append(MILLISOLS + BR);
			}
		}

		result.append(getPartsString(REPAIR_PARTS_NEEDED, malfunction.getRepairParts(), false));
		result.append(HTML_END);

		return result.toString();
	}

	/**
	 * Gets the parts string.
	 * 
	 * @return string.
	 */
	public static String getPartsString(String title, Map<Integer, Integer> parts, boolean useHTML) {

		StringBuilder buf = new StringBuilder(title);
		if (!parts.isEmpty()) {
			boolean first = true;
			for(Entry<Integer, Integer> entry : parts.entrySet()) {
				if (!first) {
					buf.append(useHTML ? BR : COMMA);
				}
				first = false;
				Integer part = entry.getKey();
				int number = entry.getValue();
				buf.append(number).append(ONE_SPACE)
						.append(ItemResourceUtil.findItemResource(part).getName());
			}
			buf.append(DOT);
		} else
			buf.append(NONE);

		return buf.toString();
	}

	@Override
	public void update() {

		List<Malfunction> newMalfunctions;
		if (malfunctionable != null) {
			newMalfunctions = malfunctionable.getMalfunctionManager().getMalfunctions();
		}
		else if (settlement != null) {
			newMalfunctions = createSettlementMalfunction(settlement);
		}
		else {
			// Shouldn't be here
			throw new IllegalStateException("No source for the malfunctions");
		}
		model.update(newMalfunctions);
	}
}

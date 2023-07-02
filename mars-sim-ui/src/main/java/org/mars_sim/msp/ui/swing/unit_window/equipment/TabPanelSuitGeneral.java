/*
 * Mars Simulation Project
 * TabPanelSuitGeneral.java
 * @date 2023-07-01
 * @author Manny Kung
 */
package org.mars_sim.msp.ui.swing.unit_window.equipment;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.data.History.HistoryItem;
import org.mars_sim.msp.core.equipment.EVASuit;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.StyleManager;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;
import org.mars_sim.msp.ui.swing.utils.AttributePanel;

/**
 * This tab displays general information about an EVA Suit.
 */
@SuppressWarnings("serial")
public class TabPanelSuitGeneral extends TabPanel {

	private static final String ID_ICON = "info"; //$NON-NLS-1$
	
	/** The suit instance. */
	private EVASuit suit;
	
	private JLabel registeredOwnerLabel;
	
	private String pOwnerCache;
	
	private HistoryTableModel historyTableModel;
		
	/**
	 * Constructor.
	 * 
	 * @param unit the unit to display.
	 * @param desktop the main desktop.
	 */
	public TabPanelSuitGeneral(EVASuit suit, MainDesktopPane desktop) {
		// Use the TabPanel constructor
		super(
			Msg.getString("TabPanelSuitGeneral.title"), //$NON-NLS-1$
			ImageLoader.getIconByName(ID_ICON),		
			Msg.getString("TabPanelSuitGeneral.title"), //$NON-NLS-1$
			desktop
		);

		this.suit = suit;
	}
	
	@Override
	protected void buildUI(JPanel content) {

		// Prepare spring layout info panel.
		AttributePanel infoPanel = new AttributePanel(1);
		
		content.add(infoPanel, BorderLayout.NORTH);

		// Prepare registered owner label
		pOwnerCache = "";
		Person p = suit.getRegisteredOwner();	
		if (p != null) {
			pOwnerCache = suit.getRegisteredOwner().getName();
		}		
		registeredOwnerLabel = infoPanel.addTextField(Msg.getString("TabPanelSuitGeneral.regOwner"), //$NON-NLS-1$
				pOwnerCache, null);

		
		JScrollPane scrollPanel = new JScrollPane();
		scrollPanel.setBorder(StyleManager.createLabelBorder("History"));
		historyTableModel = new HistoryTableModel(suit.getHistory());
		JTable table = new JTable(historyTableModel);
		table.setPreferredScrollableViewportSize(new Dimension(225, 100));
		scrollPanel.setViewportView(table);

		content.add(scrollPanel, BorderLayout.CENTER);

	}
	
	/**
	 * Updates the info on this panel.
	 */
	@Override
	public void update() {
		
		String pOwner = "";
		Person p = suit.getRegisteredOwner();	
		if (p != null) {
			pOwner = suit.getRegisteredOwner().getName();
		}
		if (!pOwnerCache.equalsIgnoreCase(pOwner)) {
			pOwnerCache = pOwner;
			registeredOwnerLabel.setText(pOwner); 
		}

		historyTableModel.update();
	}

	/**
	 * Internal class used as model for the attribute table.
	 */
	private static class HistoryTableModel extends AbstractTableModel {

		private static final long serialVersionUID = 1L;

		private List<HistoryItem<Unit>> history;
		private int origSize;

		private Object firstTime;

		/**
		 * hidden constructor.
		 *
		 * @param unit {@link Unit}
		 */
		HistoryTableModel(List<HistoryItem<Unit>> history) {
			this.history = history;
			origSize = history.size();
			firstTime = (history.isEmpty() ? null : history.get(0).getWhen());
		}

		@Override
		public int getRowCount() {
			if (history != null)
				return history.size();
			else
				return 0;
		}

		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return String.class;
		}

		@Override
		public String getColumnName(int columnIndex) {
			return switch(columnIndex) {
				case 0 -> "Date";
				case 1 -> "Location";
				default -> null;
			};
		}

		public Object getValueAt(int row, int column) {
			int r = history.size() - row - 1;
			HistoryItem<Unit> item = history.get(r);
			return switch(column) {
				case 0 -> item.getWhen().getTruncatedDateTimeStamp();
				case 1 -> item.getWhat().getName();
				default -> null;
			};
		}

		/**
		 * Prepares the job history of the person.
		 */
		void update() {
			// Has the size changed or the time of the first item (handles fixed size history)
			if ((origSize != history.size()) 
				|| !history.get(0).getWhen().equals(firstTime)) {
				origSize = history.size();
				firstTime = history.get(0).getWhen();
				fireTableDataChanged();
			}
		}
	}
}

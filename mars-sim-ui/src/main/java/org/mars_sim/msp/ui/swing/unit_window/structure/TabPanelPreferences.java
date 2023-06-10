/*
 * Mars Simulation Project
 * TabPanelWeights.java
 * @date 2023-06-08
 * @author Barry Evans
 */
package org.mars_sim.msp.ui.swing.unit_window.structure;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;

import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.person.ai.mission.MissionType;
import org.mars_sim.msp.core.person.ai.task.util.MetaTaskUtil;
import org.mars_sim.msp.core.reportingAuthority.PreferenceKey;
import org.mars_sim.msp.core.reportingAuthority.PreferenceKey.Type;
import org.mars_sim.msp.core.science.ScienceType;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.NumberCellRenderer;
import org.mars_sim.msp.ui.swing.StyleManager;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;

@SuppressWarnings("serial")
public class TabPanelPreferences extends TabPanel {

	/**
	 * Rrepresents a renderable version fo a PreferenceKey with a displayable label.
	 */
	private static final record RenderableKey(PreferenceKey key, String label) {
		@Override
		public String toString() {
			return label;
		}		
	}

	private static final String ICON = "favourite";

	// Maintains a cache of preferences already having a renderable equivalent
	private static Map<PreferenceKey,RenderableKey> keys = new HashMap<>();
	private PreferenceTableModel tableModel;
	private JComboBox<PreferenceKey.Type> typeCombo;
	private JComboBox<RenderableKey> nameCombo;

	/**
	 * Constructor.
	 * @param unit {@link Unit} the unit to display.
	 * @param desktop {@link MainDesktopPane} the main desktop.
	 */
	public TabPanelPreferences(Unit unit, MainDesktopPane desktop) {
		// Use TabPanel constructor.
		super(
			null,
			ImageLoader.getIconByName(ICON),
			"Preferences", //$NON-NLS-1$
			unit, desktop
		);
	}
	
	@Override
	protected void buildUI(JPanel content) {

		JPanel topPanel = new JPanel(new BorderLayout());
		content.add(topPanel);

 		// Create scroll panel for the outer table panel.
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setPreferredSize(new Dimension(250, 250));
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		topPanel.add(scrollPane, BorderLayout.CENTER);

		// Prepare goods table model.
		tableModel = new PreferenceTableModel((Settlement) getUnit());

		// Prepare goods table.
		JTable table = new JTable(tableModel);
		scrollPane.setViewportView(table);
		
		// Override default cell renderer for formatting double values.
		table.setDefaultRenderer(Double.class, new NumberCellRenderer(1, true));
		TableColumnModel cModel = table.getColumnModel();
		cModel.getColumn(0).setPreferredWidth(30);
		cModel.getColumn(2).setPreferredWidth(20);

		// Added the two methods below to make all heatTable columns
		// Resizable automatically when its Panel resizes
		table.setPreferredScrollableViewportSize(new Dimension(225, -1));

		// Added sorting
		table.setAutoCreateRowSorter(true);

		// Create editor control
		JPanel newPanel = new JPanel();
		newPanel.setBorder(StyleManager.createLabelBorder("Add new Preference"));
		topPanel.add(newPanel, BorderLayout.NORTH);

		typeCombo = new JComboBox<>(PreferenceKey.Type.values());
		typeCombo.addItemListener(i -> {populateNameCombo(); });
		newPanel.add(typeCombo);

		nameCombo = new JComboBox<>();
		newPanel.add(nameCombo);

		JButton add = new JButton(ImageLoader.getIconByName("action/add"));
		add.addActionListener(i -> {addEntry();});
		newPanel.add(add);

		// Add an expaliantion
		JLabel message = new JLabel("Edit the modifier column to change");
		message.setFont(StyleManager.getSmallFont());
		topPanel.add(message, BorderLayout.SOUTH);

		// Load up combo
		populateNameCombo();
	}

	/**
	 * Add a new entry to the settlement
	 */
	private void addEntry() {
		tableModel.addEntry((RenderableKey) nameCombo.getSelectedItem(), 1D);
	}

	/**
	 * Populate the name combo based on the type of preference
	 */
	private void populateNameCombo() {
		PreferenceKey.Type selectedType = (Type) typeCombo.getSelectedItem();
		nameCombo.removeAllItems();

		List<RenderableKey> newItems = null;
		switch(selectedType) {
			case TASK: {
				newItems = MetaTaskUtil.getAllMetaTasks().stream()
									.map(mt -> getRendered(new PreferenceKey(Type.TASK, mt.getID())))
									.sorted((v1, v2) -> v1.label().compareTo(v2.label()))
									.toList();
				}
			break;

			case MISSION:
				newItems = new ArrayList<>();
				for(MissionType mt : MissionType.values()) {
					newItems.add(getRendered(new PreferenceKey(Type.MISSION, mt.name())));
				}
				break;

			case SCIENCE:
				newItems = ScienceType.valuesList().stream()
									.map(mt -> getRendered(new PreferenceKey(Type.SCIENCE, mt.name())))
									.toList();
				break;
		}

		if (newItems != null) {
			newItems.forEach(r -> nameCombo.addItem(r));
		}
	}

	/**
	 * Get a renderable version of a PreferenceKey
	 * @param key
	 * @return
	 */
	private static RenderableKey getRendered(PreferenceKey key) {
		if (keys.containsKey(key)) {
			return keys.get(key);
		}

		// Create a better readable number of the user
		String label = "?";
		try {
			label = switch (key.getType()) {
				case TASK -> MetaTaskUtil.getMetaTask(key.getName()).getName();
				case SCIENCE -> ScienceType.valueOf(key.getName()).getName();
				case MISSION -> MissionType.valueOf(key.getName()).getName();
			};
		}
		catch (RuntimeException e) {
			// Problem with the specifc user value not matching any known item
		}
		RenderableKey result = new RenderableKey(key, label);
		keys.put(key, result);
		return result;
	}

	/**
	 * Internal class used as model for preference weights
	 */
	private static class PreferenceTableModel
				extends AbstractTableModel {

		private Settlement manager;
		private List<RenderableKey> items;
		private List<Double> target;

		private PreferenceTableModel(Settlement manager) {
			this.manager = manager;
			items = new ArrayList<>(manager.getKnownPreferences().stream()
										.map(v -> getRendered(v)).toList());
			target = new ArrayList<>();
			for(RenderableKey i : items) {
				target.add(manager.getPreferenceModifier(i.key()));
			}
		}

		/**
		 * Add an entry to the table and the Settlement
		 */
		public void addEntry(RenderableKey preference, double modifier) {
			if (!items.contains(preference)) {
				items.add(preference);
				target.add(modifier);

				manager.setPreferenceModifier(preference.key(), modifier);
				int newRow = items.size()-1;
				fireTableRowsInserted(newRow, newRow);
			}
		}

		@Override
		public int getRowCount() {
			return items.size();
		}

		@Override
		public int getColumnCount() {
			return 3;
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return switch(columnIndex) {
				case 0, 1 -> String.class;
				case 2 -> Double.class;
				default -> throw new IllegalArgumentException("Unexpected value: " + columnIndex);
			};
		}

		@Override
		public String getColumnName(int columnIndex) {
			return switch(columnIndex) {
				case 0 -> "Type";
				case 1 -> "Name";
				case 2 -> "Modifier";
				default -> throw new IllegalArgumentException("Unexpected value: " + columnIndex);
			};
		}

		@Override
		public Object getValueAt(int row, int column) {
			if (row < getRowCount()) {
				RenderableKey entry = items.get(row);
				return switch(column) {
					case 0 -> entry.key().getType();
					case 1 -> entry.label();
					case 2 -> target.get(row);
					default -> throw new IllegalArgumentException("Unexpected value: " + column);
				};
			}
			return null;
		}

		@Override
		public boolean isCellEditable(int row, int col) {
			return (col == 2);
		}

		@Override
		public void setValueAt(Object value, int row, int col) {
			if (col == 2) {
				Double newValue = (Double) value;
				target.set(row, newValue);
				manager.setPreferenceModifier(items.get(row).key(), newValue);
			}
		}
	}
}

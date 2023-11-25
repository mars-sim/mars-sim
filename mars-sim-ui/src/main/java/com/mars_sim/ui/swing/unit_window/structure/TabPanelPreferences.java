/*
 * Mars Simulation Project
 * TabPanelWeights.java
 * @date 2023-06-08
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.unit_window.structure;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;

import com.mars_sim.core.Unit;
import com.mars_sim.core.authority.PreferenceCategory;
import com.mars_sim.core.authority.PreferenceKey;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.ai.mission.MissionType;
import com.mars_sim.core.person.ai.task.util.MetaTaskUtil;
import com.mars_sim.core.science.ScienceType;
import com.mars_sim.core.structure.OverrideType;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.MainDesktopPane;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.unit_window.TabPanel;

public class TabPanelPreferences extends TabPanel {

	/** Default logger. */
	private static SimLogger logger = SimLogger.getLogger(TabPanelPreferences.class.getName());


	/**
	 * Represents a renderable version for a Preference Key with a displayable label.
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
	private JComboBox<PreferenceCategory> typeCombo;
	private JComboBox<RenderableKey> nameCombo;

	/**
	 * Constructor.
	 * 
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
		TableColumnModel cModel = table.getColumnModel();
		cModel.getColumn(0).setPreferredWidth(30);
		cModel.getColumn(2).setPreferredWidth(20);

		// Add the two methods below to make all columns
		// Resizable automatically when its Panel resizes
		table.setPreferredScrollableViewportSize(new Dimension(225, -1));

		// Add sorting
		table.setAutoCreateRowSorter(true);

		// Create editor control
		JPanel newPanel = new JPanel();
		newPanel.setBorder(StyleManager.createLabelBorder("Add new Preference"));
		topPanel.add(newPanel, BorderLayout.NORTH);

		typeCombo = new JComboBox<>(PreferenceCategory.values());
		typeCombo.addItemListener(i -> populateNameCombo());
		typeCombo.setRenderer(new PreferenceCategoryRenderer());
		newPanel.add(typeCombo);

		nameCombo = new JComboBox<>();
		newPanel.add(nameCombo);

		JButton add = new JButton(ImageLoader.getIconByName("action/add"));
		add.addActionListener(i -> addEntry());
		newPanel.add(add);

		// Add an explanation
		JLabel message = new JLabel("Note: Click on modifier column to change");
		message.setFont(StyleManager.getSmallFont());
		topPanel.add(message, BorderLayout.SOUTH);

		// Load up combo
		populateNameCombo();
	}

	/**
	 * Adds a new entry to the settlement.
	 */
	private void addEntry() {
		RenderableKey key = (RenderableKey) nameCombo.getSelectedItem();
		Object newValue = switch(key.key().getCategory().getValueType()) {
			case BOOLEAN -> Boolean.TRUE;
			case DOUBLE -> Double.valueOf(1D);
		};
		tableModel.addEntry(key, newValue);
	}

	/**
	 * Populates the name combo based on the type of preference.
	 */
	private void populateNameCombo() {
		PreferenceCategory selectedType = (PreferenceCategory) typeCombo.getSelectedItem();
		nameCombo.removeAllItems();

		List<RenderableKey> newItems = null;
		switch(selectedType) {
			case TASK_WEIGHT: {
				newItems = MetaTaskUtil.getAllMetaTasks().stream()
									.map(mt -> getRendered(new PreferenceKey(PreferenceCategory.TASK_WEIGHT, mt.getID())))
									.sorted((v1, v2) -> v1.label().compareTo(v2.label()))
									.toList();
				}
			break;

			case MISSION_WEIGHT:
				newItems = new ArrayList<>();
				for(MissionType mt : MissionType.values()) {
					newItems.add(getRendered(new PreferenceKey(PreferenceCategory.MISSION_WEIGHT, mt.name())));
				}
				break;

			case SCIENCE:
				newItems = Arrays.stream(ScienceType.values())
									.map(mt -> getRendered(new PreferenceKey(PreferenceCategory.SCIENCE, mt.name())))
									.toList();
				break;

			case PROCESS_OVERRIDE:
				newItems = new ArrayList<>();
				for(OverrideType mt : OverrideType.values()) {
					newItems.add(getRendered(new PreferenceKey(PreferenceCategory.PROCESS_OVERRIDE, mt.name())));
				}
				break;
			default:
				newItems = null;
				break;
		}

		if (newItems != null) {
			newItems.forEach(r -> nameCombo.addItem(r));
		}
	}

	/**
	 * Gets a renderable version of a PreferenceKey.
	 * 
	 * @param key
	 * @return
	 */
	private static RenderableKey getRendered(PreferenceKey key) {
		if (keys.containsKey(key)) {
			return keys.get(key);
		}

		// Create a better readable version for the user
		String label = "?";
		try {
			label = switch (key.getCategory()) {
				case TASK_WEIGHT -> MetaTaskUtil.getMetaTask(key.getName()).getName();
				case SCIENCE -> ScienceType.valueOf(key.getName()).getName();
				case MISSION_WEIGHT -> MissionType.valueOf(key.getName()).getName();
				case PROCESS_OVERRIDE -> OverrideType.valueOf(key.getName()).getName();

				default -> key.getName();
			};
		}
		catch (RuntimeException e) {
			// Problem with the specfic user value not matching any known item
		}
		RenderableKey result = new RenderableKey(key, label);
		keys.put(key, result);
		return result;
	}

	/**
	 * Internal class used as model for preference weights.
	 */
	private static class PreferenceTableModel
				extends AbstractTableModel {

		private Settlement manager;
		private transient List<RenderableKey> items;
		private transient Map<PreferenceKey,Object> target;

		private PreferenceTableModel(Settlement manager) {
			this.manager = manager;
			this.target = manager.getPreferences();
			items = new ArrayList<>(target.keySet().stream()
										.map(v -> getRendered(v)).toList());
		}

		/**
		 * Adds an entry to the table and the Settlement.
		 */
		public void addEntry(RenderableKey preference, Object value) {
			if (!items.contains(preference)) {
				items.add(preference);
				manager.setPreference(preference.key(), value);
				int newRow = items.size()-1;
				fireTableRowsInserted(newRow, newRow);
				
				logger.info(manager, preference.label() + " @ " 
						+ value.toString()
						+ " manually added by Player in Settlement's Preference tab.");
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
				case 2 -> Object.class;
				default -> throw new IllegalArgumentException("Unexpected column class index: " + columnIndex);
			};
		}

		@Override
		public String getColumnName(int columnIndex) {
			return switch(columnIndex) {
				case 0 -> "Category";
				case 1 -> "Name";
				case 2 -> "Modifier";
				default -> throw new IllegalArgumentException("Unexpected column name index: " + columnIndex);
			};
		}

		@Override
		public Object getValueAt(int row, int column) {
			if (row < getRowCount()) {
				RenderableKey entry = items.get(row);
				return switch(column) {
					case 0 -> entry.key().getCategory().getName();
					case 1 -> entry.label();
					case 2 -> target.get(entry.key());
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
				PreferenceKey key = items.get(row).key();
				Object newValue = switch(key.getCategory().getValueType()) {
					case BOOLEAN -> Boolean.parseBoolean((String)value);
					case DOUBLE -> Double.parseDouble((String)value);
				};
				manager.setPreference(key, newValue);
			}
		}
	}

	/**
	 * Renderer for preference category items
	 */
	private static class PreferenceCategoryRenderer extends BasicComboBoxRenderer {
		@Override
		public Component getListCellRendererComponent(JList list, Object value,
					int index, boolean isSelected, boolean cellHasFocus) {
			super.getListCellRendererComponent(list, value, index, isSelected,
				cellHasFocus);

			PreferenceCategory item = (PreferenceCategory) value;
			setText(item.getName());
			return this;
		}
	}
}

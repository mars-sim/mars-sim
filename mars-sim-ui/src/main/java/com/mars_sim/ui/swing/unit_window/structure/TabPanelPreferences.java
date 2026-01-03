/*
 * Mars Simulation Project
 * TabPanelPreferences.java
 * @date 2023-06-08
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.unit_window.structure;

import java.awt.BorderLayout;
import java.awt.Component;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import com.mars_sim.core.Unit;
import com.mars_sim.core.parameter.ParameterCategories;
import com.mars_sim.core.parameter.ParameterCategory;
import com.mars_sim.core.parameter.ParameterCategory.ParameterSpec;
import com.mars_sim.core.parameter.ParameterKey;
import com.mars_sim.core.parameter.ParameterManager;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.entitywindow.EntityTableTabPanel;
import com.mars_sim.ui.swing.utils.SwingHelper;

@SuppressWarnings("serial")
class TabPanelPreferences extends EntityTableTabPanel<Settlement> {

	/**
	 * Represents a renderable version for a Parameter Key with a displayable label.
	 */
	private static final record RenderableKey(ParameterKey key, ParameterSpec spec) {
		@Override
		public String toString() {
			return spec.displayName();
		}	
		
		public ParameterKey key() {
			return key;
		}
	}

	private static final String ICON = "favourite";

	// Maintains a cache of preferences already having a renderable equivalent
	private PreferenceTableModel tableModel;
	private JComboBox<ParameterCategory> typeCombo;
	private JComboBox<RenderableKey> nameCombo;

	private ParameterManager mgr;

	private transient ParameterCategory[] categories;

	/**
	 * Constructor.
	 * 
	 * @param unit {@link Unit} the unit to display.
	 * @param context {@link UIContext} the main desktop.
	 */
	public TabPanelPreferences(Settlement unit, UIContext context) {
		super(
			Msg.getString("TabPanelPreferences.title"), //-NLS-1$
			ImageLoader.getIconByName(ICON), null,
			unit, context
		);
		mgr = unit.getPreferences();
		categories = ParameterCategories.getSettlementCategories();
	}
	
	/**
	 * Info panel contains the controls to add/modify preferences
	 */
	@Override
	protected JPanel createInfoPanel() {
		JPanel topPanel = new JPanel(new BorderLayout());

		// Create editor control
		JPanel newPanel = new JPanel();
		newPanel.setBorder(SwingHelper.createLabelBorder("Add new Preference"));
		topPanel.add(newPanel, BorderLayout.NORTH);

		typeCombo = new JComboBox<>(categories);
		typeCombo.addItemListener(i -> populateNameCombo());
		typeCombo.setRenderer(new ParameterCategoryRenderer());
		newPanel.add(typeCombo);

		nameCombo = new JComboBox<>();
		newPanel.add(nameCombo);

		JButton add = new JButton(ImageLoader.getIconByName("action/plus"));
		add.addActionListener(i -> addEntry());
		newPanel.add(add);

		JButton remove = new JButton(ImageLoader.getIconByName("action/clear"));
		remove.addActionListener(i -> deleteEntry());
		newPanel.add(remove);

		// Add an explanation
		JLabel message = new JLabel("Note: Click on modifier column to change");
		message.setFont(StyleManager.getSmallFont());
		topPanel.add(message, BorderLayout.SOUTH);

		// Load up combo
		populateNameCombo();

		return topPanel;
	}

	@Override
	protected TableModel createModel() {
		// Prepare goods table model.
		tableModel = new PreferenceTableModel(mgr);
		return tableModel;
	}
	
	/**
	 * Set the width of the preference columns
	 */
	@Override
	protected void setColumnDetails(TableColumnModel cModel) {
		// Override default cell renderer for formatting double values.
		cModel.getColumn(0).setPreferredWidth(30);
		cModel.getColumn(2).setPreferredWidth(20);
	}

	/**
	 * Adds a new entry to the settlement.
	 */
	private void addEntry() {
		RenderableKey key = (RenderableKey) nameCombo.getSelectedItem();
		Serializable newValue = switch(key.spec().type()) {
			case BOOLEAN -> Boolean.TRUE;
			case DOUBLE -> Double.valueOf(1D);
			case INTEGER -> Integer.valueOf(1);
		};
		tableModel.addEntry(key, newValue);
		nameCombo.removeItem(key);
	}

	private void deleteEntry() {
		var t = getMainTable();
		int idx = t.getSelectedRow();
		if (idx >= 0) {
			idx = t.getRowSorter().convertRowIndexToModel(idx);
 			RenderableKey selection = tableModel.getValue(idx);
			int input = JOptionPane.showConfirmDialog(this, 
                "Delete Preference " + selection.key.getCategory().getName() + ":" + selection.spec.displayName(), "Delete Preference", 
                JOptionPane.OK_CANCEL_OPTION);
			if (input == 0) {
				tableModel.removeEntry(selection);

				// Reload combo
				populateNameCombo();
			}
		}
	}

	/**
	 * Populates the name combo based on the type of preference.
	 */
	private void populateNameCombo() {
		ParameterCategory selectedType = (ParameterCategory) typeCombo.getSelectedItem();
		nameCombo.removeAllItems();

		var possibles = selectedType.getRange();
		var existing = mgr.getValues().keySet();
		List<RenderableKey> newItems = possibles.entrySet().stream()
									.filter(e -> !existing.contains(e.getKey()))
									.map(e -> new RenderableKey(e.getKey(), e.getValue()))
									.sorted((v1, v2) -> v1.spec().displayName().compareTo(v2.spec().displayName()))
									.toList();
		

		newItems.forEach(r -> nameCombo.addItem(r));
	}

	/**
	 * Internal class used as model for preference weights.
	 */
	private static class PreferenceTableModel
				extends AbstractTableModel {

		private static final long serialVersionUID = 1L;
		private transient List<RenderableKey> items;
		private transient ParameterManager target;

		private PreferenceTableModel(ParameterManager source) {
			this.target = source;

			var missing = target.getValues().keySet().stream()
				.filter(k -> k.getCategory().getSpec(k) == null)
				.toList();
			if (!missing.isEmpty()) {
				System.out.println("Preference keys with missing specs: " + missing);
			}

			items = new ArrayList<>(target.getValues().keySet().stream()
						.map(k -> new RenderableKey(k, k.getCategory().getSpec(k)))
						.toList());
		}

		public RenderableKey getValue(int idx) {
			return items.get(idx);
		}

		/**
		 * Adds an entry to the table and the underlying manager.
		 */
		public void addEntry(RenderableKey newKey, Serializable value) {
			if (!items.contains(newKey)) {
				items.add(newKey);
				target.putValue(newKey.key(), value);
				int newRow = items.size()-1;
				fireTableRowsInserted(newRow, newRow);
			}
		}

		/**
		 * Removes an entry to the table and the underlying manager.
		 */
		public void removeEntry(RenderableKey newKey) {
			int idx = items.indexOf(newKey);
			if (idx >= 0) {
				items.remove(newKey);
				target.removeValue(newKey.key());
				fireTableRowsDeleted(idx, idx);
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
				case 2 -> "Value";
				default -> throw new IllegalArgumentException("Unexpected column name index: " + columnIndex);
			};
		}

		@Override
		public Object getValueAt(int row, int column) {
			if (row < items.size()) {
				var entry = items.get(row);
				return switch(column) {
					case 0 -> entry.key().getCategory().getName();
					case 1 -> entry.spec().displayName();
					case 2 -> target.getValues().get(entry.key());
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
				var selectedRow = items.get(row);
				Serializable newValue;
				switch(selectedRow.spec().type()) {
				case BOOLEAN:
					newValue = Boolean.parseBoolean((String)value);
					break;
				case DOUBLE:
					newValue = Double.parseDouble((String)value);
					break;
				case INTEGER:
					newValue = Integer.parseInt((String)value);
					break;
				default:
					throw new IllegalArgumentException("Cannot handle preference value of type "
										+ selectedRow.spec().type());
				}
				target.putValue(selectedRow.key(), newValue);
			}
		}
	}

	/**
	 * Renderer for preference category items
	 */
	private static class ParameterCategoryRenderer extends BasicComboBoxRenderer {
		private static final long serialVersionUID = 5196005571089330950L;

		@SuppressWarnings("rawtypes")
		@Override
		public Component getListCellRendererComponent(JList list, Object value,
					int index, boolean isSelected, boolean cellHasFocus) {
			super.getListCellRendererComponent(list, value, index, isSelected,
				cellHasFocus);

			ParameterCategory item = (ParameterCategory) value;
			setText(item.getName());
			return this;
		}
	}
}

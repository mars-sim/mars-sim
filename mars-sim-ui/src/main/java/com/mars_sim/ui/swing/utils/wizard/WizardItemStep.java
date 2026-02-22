/*
 * Mars Simulation Project
 * WizardItemStep.java
 * @date 2026-02-01
 * @author Barry Evans
 */

package com.mars_sim.ui.swing.utils.wizard;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import com.mars_sim.ui.swing.components.ColumnSpec;
import com.mars_sim.ui.swing.components.ColumnSpecHelper;
import com.mars_sim.ui.swing.utils.ToolTipTableModel;


/**
 * A wizard step for selecting an item from a table.
 * The potential items are provided by a {@link AbstractWizardItemModel}.
 * This panel will automatically create a table to display the items and handle selection and validation of the selection.
 * Optionally it can also show an info panel.
 * 
 * @param <S> The wizard state type.
 * @param <I> The type of item to select.
 */
@SuppressWarnings("serial")
public abstract class WizardItemStep<S,I> extends WizardStep<S> {

	// Data members.
	private WizardItemModel<I> model;
	private JLabel selectionLabel;
	private int minSelection;
	private int maxSelection;
	private List<Integer> orderedSelection = new ArrayList<>();

	/**
	 * Constructor with a single selection
	 * @param id the step identifier.
	 * @param wizard the create mission wizard.
	 * @param model the item model.
	 */
	protected WizardItemStep(String id, WizardPane<S> parent, WizardItemModel<I> model) {
		this(id, parent, model, 1, 1);
	}

	/**
	 * Constructor.
	 * @param id the step identifier.
	 * @param wizard the create mission wizard.
	 * @param model the item model.
	 * @param minSelection the minimum number of selections required.
	 * @param maxSelection the maximum number of selections allowed.
	 */
	protected WizardItemStep(String id, WizardPane<S> parent,
			WizardItemModel<I> model, int minSelection, int maxSelection) {
		super(id, parent);
		this.model = model;
		this.minSelection = minSelection;
		this.maxSelection = maxSelection;

		// Set the layout.
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		// Create the settlement panel.
		JPanel settlementPane = new JPanel(new BorderLayout(0, 0));
		settlementPane.setMaximumSize(new Dimension(Short.MAX_VALUE, 100));
		settlementPane.setAlignmentX(Component.CENTER_ALIGNMENT);
		add(settlementPane);

		// Create scroll panel for settlement list.
		JScrollPane tableScrollPane = new JScrollPane();
		settlementPane.add(tableScrollPane, BorderLayout.CENTER);

		// Create the item table and support tooltips.
		boolean single = minSelection == 1;
		if (!single) {
			// For multiple selection we add a selection column to show the order of selection.
			model = new SelectionColumnModel<>(model);
		}
		var minTable = new JTable(model) {
			@Override
			public String getToolTipText(MouseEvent e) {
				return ToolTipTableModel.extractToolTip(e, this);
			}
		};

		// Configure row selection
		minTable.setRowSelectionAllowed(true);
		minTable.setSelectionMode((single ? ListSelectionModel.SINGLE_SELECTION
								: ListSelectionModel.MULTIPLE_INTERVAL_SELECTION));
		minTable.getSelectionModel().addListSelectionListener(
				e -> selectionUpdated(e, minTable)
			);		
		
		// Single selection also add double click
		if (single) {
			minTable.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					if (e.getClickCount() == 2) {
						var selectedCount = getValidedSelection().size();
						if (selectedCount == 1) {
							advanceStep();
						}
					}
				}
			});
		}	
		minTable.setPreferredScrollableViewportSize(minTable.getPreferredSize());
		tableScrollPane.setViewportView(minTable);

		// Add the failure cell renderer. This uses the ColumnSpecHelper to gget the base renderer
		// and then add a proxy renderer to annotate failed cells.
		var colModel = 	minTable.getColumnModel();
	    for(int colId = 0; colId < colModel.getColumnCount(); colId++) {
            var col = colModel.getColumn(colId);
            var renderer = ColumnSpecHelper.createBestRenderer(model, col);    
            if (renderer == null) {
				renderer = new DefaultTableCellRenderer();
			}

			// Add failure decorator
            col.setCellRenderer(new FailureCellDecorator<>(model, renderer));
        }

		// Footer
		selectionLabel = new JLabel();
		selectionLabel.setHorizontalAlignment(SwingConstants.CENTER);
		setSelectionLabel(0);
		add(selectionLabel);

		var info = buildInfoPanel();
		if (info != null) {
			add(Box.createVerticalStrut(5));
			add(info);
		}
		// Add a vertical glue.
		add(Box.createVerticalGlue());
	}
	
	/**
	 * Table has changed the selection. This updates the ordered selection and then calls selection changed with the valid selection.
	 * @param e The list selection event.
	 * @param sourceTable The table that triggered the event.
	 */
	private void selectionUpdated(ListSelectionEvent e, JTable sourceTable) {
		if (!e.getValueIsAdjusting()) {

			// Get new selection in terms of model frame
			List<Integer> tableSelection = new ArrayList<>();
			for (var i : sourceTable.getSelectedRows()) {
				tableSelection.add(sourceTable.convertRowIndexToModel(i));
			}

			// Check existing selectinos are still selected
			List<Integer> newSelection = new ArrayList<>();
			for (var sel : orderedSelection) {
				if (tableSelection.contains(sel)) {
					newSelection.add(sel);
					tableSelection.remove(sel);
				}
			}

			// Append remaining new selections
			newSelection.addAll(tableSelection);
			orderedSelection = newSelection;
			selectionChanged(getValidedSelection());

			// Force a repaint; don't fire a data changed a it breaks the selection model
			if (maxSelection > 1) {
				sourceTable.repaint();
			}
		}
	}

	/**
	 * Build an optional info panel to display below the selection.
	 * This can be used to provide additional information about the selection or instructions.
	 * @return Default return null
	 */
	protected JComponent buildInfoPanel() {
		return null;
	}

	/**
	 * Called when the selection is changed. This updates the mandatory done status and the selection label.
	 * I can be overridden by subclasses to provide additional behaviour on selection change.
	 * @param selectedItems Current selected items in the table.
	 */
	protected void selectionChanged(List<I> selectedItems) {
		var selectedCount = selectedItems.size();
		setMandatoryDone(selectedCount >= minSelection && selectedCount <= maxSelection);
		setSelectionLabel(selectedCount);
	}

	private void setSelectionLabel(int selected) {
		selectionLabel.setText("Selected " + selected + ", minimum " + minSelection
				+ (maxSelection != Integer.MAX_VALUE ? ", maximum " + maxSelection : ""));
	}

	/**
	 * Get the valid selection from the table.
	 * @return List of items
	 */
	private List<I> getValidedSelection() {
		var selectedUnits = new ArrayList<I>();
		for (var rowModel : orderedSelection) {
			if (!model.isFailureItem(rowModel)) {
				selectedUnits.add(model.getItem(rowModel));
			}
		}
		return selectedUnits;
	}

	/**
	 * The wizards request this step to update the main state.
	 * @param state the wizard state.
	 */
	@Override
	public void updateState(S state) {
		if (orderedSelection.isEmpty())
			return;

		var selectedItems = getValidedSelection();
		if (!selectedItems.isEmpty()) {
			updateState(state, selectedItems);
		}
	}

	/**
	 * This step is released by the wizard as no longer being required. This releases the model resources.
	 */
	@Override
	void release() {
		super.release();
		model.release();
	}

	/**
	 * Update the state with the selected valids items from the table.
	 * @param state Wizard state
	 * @param selectedItems Selected items in the table
	 */
	protected abstract void updateState(S state, List<I> selectedItems);

	/**
	 * This decorates the base renderer if it is in error.
	 */
	private static class FailureCellDecorator<I> implements TableCellRenderer {

		private static final Border RED_BORDER = BorderFactory.createLineBorder(Color.RED, 2);

		// Private data members.
		private WizardItemModel<I> model;
		private TableCellRenderer baseRenderer;
		
		/**
		 * Constructor
		 * @param model the unit table model.
		 */
		FailureCellDecorator(WizardItemModel<I> model, TableCellRenderer baseRenderer) {
			this.model = model;
			this.baseRenderer = baseRenderer;
		}
	
		/**
		 * Returns the default table cell renderer.
		 * @param table the table the cell is in.
		 * @param value the value in the cell.
		 * @return the rendering component.
		 */
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, 
				boolean isSelected, boolean hasFocus, int row, int column) {

			JLabel l = (JLabel) baseRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

			// If failure cell, mark background red.
			int rowModel = table.convertRowIndexToModel(row);
			if (model.isFailureCell(rowModel, column) != null)
				l.setBorder(RED_BORDER);
			else
				l.setBorder(null);

			return l;
		}
	}

	/**
	 * This is a proxy model that adds a selection column.
	 */
	private class SelectionColumnModel<T> extends AbstractTableModel implements WizardItemModel<T> {
		private static final ColumnSpec SELECTION_COL = new ColumnSpec("#", Integer.class);
		private WizardItemModel<T> model;

		private SelectionColumnModel(WizardItemModel<T> model) {
			this.model = model;
		}

		@Override
		public int getRowCount() {
			return model.getRowCount();
		}

		@Override
		public int getColumnCount() {
			return model.getColumnCount() + 1;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			if (columnIndex == 0) {
				var selectedIdx = orderedSelection.indexOf(rowIndex);
				return selectedIdx >= 0 ? selectedIdx + 1 : null;
			} else {
				return model.getValueAt(rowIndex, columnIndex - 1);
			}
		}

		@Override
		public T getItem(int rowIndex) {
			return model.getItem(rowIndex);
		}

		@Override
		public boolean isFailureItem(int rowIndex) {
			return model.isFailureItem(rowIndex);
		}

		@Override
		public String isFailureCell(int rowIndex, int columnIndex) {
			if (columnIndex == 0) {
				return null;
			} else {
				return model.isFailureCell(rowIndex, columnIndex - 1);
			}
		}

		@Override
		public void release() {
			model.release();
		}

		@Override
		public ColumnSpec getColumnSpec(int modelIndex) {
			if (modelIndex == 0) {
				return SELECTION_COL;
			} else {
				return model.getColumnSpec(modelIndex - 1);
			}
		}

		@Override
		public String getToolTipAt(int row, int col) {
			if (col > 0) {
				return model.getToolTipAt(row, col - 1);
			}
			return null;
		}

		@Override
		public String getColumnName(int columnIndex) {
			if (columnIndex == 0) {
				return SELECTION_COL.name();
			} else {
				return model.getColumnName(columnIndex - 1);
			}
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			if (columnIndex == 0) {
				return SELECTION_COL.type();
			} else {
				return model.getColumnClass(columnIndex - 1);
			}
		}
	}
}

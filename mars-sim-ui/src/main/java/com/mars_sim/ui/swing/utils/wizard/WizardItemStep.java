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
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import com.mars_sim.ui.swing.components.ColumnSpecHelper;


/**
 * A wizard step for selecting an item from a table.
 * The potential items are provided by a {@link WizardItemModel}.
 * @param <S> The wizard state type.
 * @param <I> The type of item to select.
 */
@SuppressWarnings("serial")
public
abstract class WizardItemStep<S,I> extends WizardStep<S> {

	// Data members.
	private WizardItemModel<I> model;
	private JLabel errorMessageLabel;
	private JTable itemTable;
	private JLabel selectionLabel;
	private int minSelection;

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
	 * @param minSelection the minimum number of selections required.
	 * @param maxSelection the maximum number of selections allowed.
	 */
	protected WizardItemStep(String id, WizardPane<S> parent,
			WizardItemModel<I> model, int minSelection, int maxSelection) {
		super(id, parent);
		this.model = model;
		this.minSelection = minSelection;

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

		// Create the item table.
		itemTable = new JTable(model);
		itemTable.setRowSelectionAllowed(true);
		itemTable.setSelectionMode((minSelection == 1 ? ListSelectionModel.SINGLE_SELECTION
								: ListSelectionModel.MULTIPLE_INTERVAL_SELECTION));
		itemTable.getSelectionModel().addListSelectionListener(
				e -> {
					var selectedCount = getValidedSelection().size();
					setMandatoryDone(selectedCount >= minSelection && selectedCount <= maxSelection);
					setSelectionLabel(selectedCount);
				}
			);		
		itemTable.setPreferredScrollableViewportSize(itemTable.getPreferredSize());
		tableScrollPane.setViewportView(itemTable);

		// Add the failure cell renderer. This uses the ColumnSpecHelper to gget the base renderer
		// and then add a proxy renderer to annotate failed cells.
		var colModel = 	itemTable.getColumnModel();
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
		var footer = new JPanel();
		selectionLabel = new JLabel();
		selectionLabel.setHorizontalAlignment(SwingConstants.LEFT);
		setSelectionLabel(0);
		footer.add(selectionLabel);
		errorMessageLabel = new JLabel(" ");
		errorMessageLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		footer.add(errorMessageLabel);
		add(footer);

		// Add a vertical glue.
		add(Box.createVerticalGlue());
	}

	private void setSelectionLabel(int selected) {
		selectionLabel.setText("Selected " + selected + " out of " + minSelection);
	}

	/**
	 * Get the valid selection from the table.
	 * @return List of items
	 */
	private List<I> getValidedSelection() {
		var selectedUnits = new ArrayList<I>();
		var idx = itemTable.getSelectedRows();
		for (var i : idx) {
			var rowModel = itemTable.convertRowIndexToModel(i);
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
		var idx = itemTable.getSelectedRows();
		if (idx.length == 0)
			return;

		var selectedItems = getValidedSelection();
		if (!selectedItems.isEmpty()) {
			updateState(state, selectedItems);
		}
	}

	@Override
	public void clearState(S state) {
		itemTable.clearSelection();
		errorMessageLabel.setText(" ");
		super.clearState(state);
	}

	/**
	 * Update the state with the selected valids items from the table.
	 * @param state Wizard state
	 * @param selectedItems Selected items in the table
	 */
	protected abstract void updateState(S state, List<I> selectedItems);

	/**
	 * This decorates a base renderer with a Reb backgound if it is in error.
	 */
	private static class FailureCellDecorator<I> implements TableCellRenderer {

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
			I item = model.getItem(rowModel);
			if (model.isFailureCell(item, column))
				l.setBackground(Color.RED);
			else {
				l.setBackground(null); 
			}

			return l;
		}
	}
}

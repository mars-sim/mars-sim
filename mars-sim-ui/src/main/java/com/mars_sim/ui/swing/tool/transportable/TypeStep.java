/*
 * Mars Simulation Project
 * TypeStep.java
 * @date 2026-03-03
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.tool.transportable;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.Comparator;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.mars_sim.core.Named;
import com.mars_sim.ui.swing.components.JMarsTimeEditor;
import com.mars_sim.ui.swing.components.NamedListCellRenderer;
import com.mars_sim.ui.swing.utils.wizard.WizardPane;
import com.mars_sim.ui.swing.utils.wizard.WizardStep;

/**
 * Step for selecting the transportable type.
 */
class TypeStep extends WizardStep<TransportState> {

	static final String ID = "Type";

	private JComboBox<TransportableType> typeSelect;

    private JTextField descriptionTF;

	private JMarsTimeEditor dateEditor;

	TypeStep(WizardPane<TransportState> parent, TransportState state) {
		super(ID, parent);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		JPanel typePane = new JPanel(new FlowLayout(FlowLayout.LEFT));
		typePane.setAlignmentX(Component.LEFT_ALIGNMENT);
		add(typePane);

		typePane.add(new JLabel("Type: "));

		typeSelect = new JComboBox<>();
		typeSelect.setRenderer(new NamedListCellRenderer());
		List.of(TransportableType.values()).stream()
				.sorted(Comparator.comparing(Named::getName))
				.forEach(typeSelect::addItem);
		typeSelect.addItemListener(e -> checkMandatory());
		typeSelect.setMaximumRowCount(typeSelect.getItemCount());
		typePane.add(typeSelect);
		typePane.setMaximumSize(new Dimension(Short.MAX_VALUE, typeSelect.getPreferredSize().height));

        // Create the description panel.
		JPanel descriptionPane = new JPanel(new FlowLayout(FlowLayout.LEFT));
		descriptionPane.setAlignmentX(Component.LEFT_ALIGNMENT);
		add(descriptionPane);
		
		// Create the description label.
		descriptionPane.add(new JLabel("Name: "));
		descriptionTF = new JTextField(20);
		descriptionTF.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				checkMandatory();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				checkMandatory();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				checkMandatory();
			}
		});
		descriptionPane.add(descriptionTF);
		descriptionPane.setMaximumSize(new Dimension(Short.MAX_VALUE, descriptionTF.getPreferredSize().height));

		JPanel datePane = new JPanel(new FlowLayout(FlowLayout.LEFT));
		datePane.setAlignmentX(Component.LEFT_ALIGNMENT);
		datePane.add(new JLabel("Arrival Date: "));
		dateEditor = new JMarsTimeEditor();
		dateEditor.addListener(e -> checkMandatory());
		datePane.add(dateEditor);
		add(datePane);

		add(Box.createVerticalGlue());
	}

	private void checkMandatory() {
		setMandatoryDone((typeSelect.getSelectedItem() != null)
						&& dateEditor.getMarsTime() != null
						&& !descriptionTF.getText().isBlank());
	}

	@Override
	public void updateState(TransportState state) {
		var type = (TransportableType) typeSelect.getSelectedItem();
		state.setType(type);
        state.setArrivalDate(dateEditor.getMarsTime());
        state.setName(descriptionTF.getText());

		getWizard().setStepSequence(TransportableWizard.getSteps(type));
	}
}

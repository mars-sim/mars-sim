/*
 * Mars Simulation Project
 * ResupplyManifestStep.java
 * @date 2026-03-03
 * @author Barry Evans
 */

package com.mars_sim.ui.swing.tool.transportable;

import java.awt.BorderLayout;

import javax.swing.JComboBox;

import com.mars_sim.core.interplanetary.transport.resupply.ResupplyManifest;
import com.mars_sim.ui.swing.components.NamedListCellRenderer;
import com.mars_sim.ui.swing.utils.AttributePanel;
import com.mars_sim.ui.swing.utils.wizard.WizardPane;
import com.mars_sim.ui.swing.utils.wizard.WizardStep;

/**
 * Step for selecting the resupply manifest for a resupply transportable.
 * The manifest determines the supplies that will be delivered to the destination settlement.
 */
class ResupplyManifestStep extends WizardStep<TransportState> {

	static final String ID = "Resupply";

	private JComboBox<ResupplyManifest> manifestCB;

	private SettlementSuppliesPanel supplies;

	ResupplyManifestStep(WizardPane<TransportState> parent, TransportState state) {
		super(ID, parent);
		setLayout(new BorderLayout());

		var attrPanel = new AttributePanel();
		add(attrPanel, BorderLayout.NORTH);

		manifestCB = new JComboBox<>();
		manifestCB.setRenderer(new NamedListCellRenderer());
		parent.getContext().getSimulation().getConfig().getSettlementTemplateConfiguration().getSupplyManifests()
					.forEach(manifestCB::addItem);
		manifestCB.addItemListener(e -> manifestSelection());

		attrPanel.addLabelledItem("Manifest", manifestCB);

		supplies = new SettlementSuppliesPanel();
		add(supplies.getComponent(), BorderLayout.CENTER);

		// Do the initial selection to populate the supplies panel
		manifestSelection();
	}

	private void manifestSelection() {
		ResupplyManifest manifest = (ResupplyManifest) manifestCB.getSelectedItem();
		supplies.show(manifest.getSupplies());

		setMandatoryDone(true);
	}

	@Override
	public void updateState(TransportState state) {
		state.setManifest((ResupplyManifest) manifestCB.getSelectedItem());
	}
}

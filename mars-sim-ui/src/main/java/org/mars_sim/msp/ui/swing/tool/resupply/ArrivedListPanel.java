/*
 * Mars Simulation Project
 * ArrivedListPanel.java
 * @date 2021-09-20
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.resupply;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.events.HistoricalEvent;
import org.mars_sim.msp.core.events.HistoricalEventCategory;
import org.mars_sim.msp.core.events.HistoricalEventListener;
import org.mars_sim.msp.core.events.SimpleEvent;
import org.mars_sim.msp.core.interplanetary.transport.TransportManager;
import org.mars_sim.msp.core.interplanetary.transport.Transportable;
import org.mars_sim.msp.core.person.EventType;

/**
 * A panel showing a list of all arrived resupply missions.
 */
@SuppressWarnings("serial")
public class ArrivedListPanel extends JPanel
implements ListSelectionListener {

	// Data members
	private JList<?> arrivedList;
	private ArrivedListModel listModel;

	/**
	 * Constructor.
	 */
	public ArrivedListPanel() {

		// Use JPanel constructor
		super();

		setLayout(new BorderLayout());
		setBorder(new TitledBorder(Msg.getString("ArrivedListPanel.arrivedTransportItems"))); //$NON-NLS-1$
		setPreferredSize(new Dimension(200, 200));

		// Create arrived list.
		listModel = new ArrivedListModel();
		arrivedList = new JList<Object>(listModel);
		arrivedList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane scrollPane = new JScrollPane(arrivedList);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		add(scrollPane, BorderLayout.CENTER);
	}

	/**
	 * Gets the arrived resupply list.
	 * @return the arrived resupply list.
	 */
	JList<?> getArrivedList() {
		return arrivedList;
	}

	@Override
	public void valueChanged(ListSelectionEvent evt) {
		if (evt.getValueIsAdjusting()) {
			JList<?> incomingList = (JList<?>) evt.getSource();
			if (incomingList.getSelectedValue() != null) {
				arrivedList.clearSelection();
			}
		}
	}

	/**
	 * Prepare panel for deletion.
	 */
	public void destroy() {
		listModel.destroy();
	}
	
	/**
	 * Inner class for the arrived resupply list model.
	 */
	private class ArrivedListModel
	extends AbstractListModel<Object>
	implements HistoricalEventListener {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

		// Data members.
		private List<Transportable> resupplyList;

		private ArrivedListModel() {

			TransportManager manager = Simulation.instance().getTransportManager();
			resupplyList = manager.getArrivedTransportItems();
			Collections.sort(resupplyList);

			// Register as historical event listener.
			Simulation.instance().getEventManager().addListener(this);
		}

		@Override
		public Transportable getElementAt(int index) {
			Transportable result = null;
			if ((index > -1) && (index < resupplyList.size())) {
				result = resupplyList.get(index);
			}

			return result;
		}

		@Override
		public int getSize() {
			return resupplyList.size();
		}

		@Override
		public void eventAdded(int index, SimpleEvent se, HistoricalEvent he) {
			if (he.getCategory() == HistoricalEventCategory.TRANSPORT) {
				Transportable transportItem = (Transportable) he.getSource();

				if (EventType.TRANSPORT_ITEM_ARRIVED.equals(he.getType())) {
					resupplyList.add(transportItem);
					Collections.sort(resupplyList);
					int transportItemIndex = resupplyList.indexOf(transportItem);
					fireIntervalAdded(this, transportItemIndex, transportItemIndex);
				}
			}
		}

		@Override
		public void eventsRemoved(int startIndex, int endIndex) {
			// Do Nothing
		}

		/**
		 * Prepares the list for deletion.
		 */
		public void destroy() {
			resupplyList.clear();
			resupplyList = null;
			Simulation.instance().getEventManager().removeListener(this);
		}
	}
}

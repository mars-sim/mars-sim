/**
 * Mars Simulation Project
 * IncomingListPanel.java
 * @version 3.1.0 2017-11-21
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.resupply;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.JList;

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
import org.mars_sim.msp.core.interplanetary.transport.TransitState;
import org.mars_sim.msp.core.interplanetary.transport.TransportManager;
import org.mars_sim.msp.core.interplanetary.transport.Transportable;
import org.mars_sim.msp.core.person.EventType;

import com.alee.laf.panel.WebPanel;
import com.alee.laf.scroll.WebScrollPane;

/**
 * A panel showing a list of all incoming transport items.
 */
public class IncomingListPanel
extends WebPanel
implements ListSelectionListener {

	// Data members
	private JList<?> incomingList;
	private IncomingListModel listModel;

	/**
	 * Constructor.
	 */
	public IncomingListPanel() {

		// Use WebPanel constructor
		super();

		setLayout(new BorderLayout());
		setBorder(new TitledBorder(Msg.getString("IncomingListPanel.title"))); //$NON-NLS-1$
		setPreferredSize(new Dimension(225, 200));

		// Create incoming list.
		listModel = new IncomingListModel();
		incomingList = new JList<Object>(listModel);
		incomingList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		WebScrollPane scrollPane = new WebScrollPane(incomingList);
		scrollPane.setHorizontalScrollBarPolicy(WebScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		add(scrollPane, BorderLayout.CENTER);
	}

	/**
	 * Gets the incoming resupply list.
	 * @return the incoming resupply list.
	 */
	JList<?> getIncomingList() {
		return incomingList;
	}

	@Override
	public void valueChanged(ListSelectionEvent evt) {
		if (evt.getValueIsAdjusting()) {
			JList<?> arrivedList = (JList<?>) evt.getSource();
			if (arrivedList.getSelectedValue() != null) {
				incomingList.clearSelection();
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
	  * Inner class for the incoming resupply list model.
	  */
	 private class IncomingListModel
	 extends AbstractListModel<Object>
	 implements HistoricalEventListener {

		 /** default serial id. */
		 private static final long serialVersionUID = 1L;

		 // Data members.
		 private List<Transportable> transportList;

		 private IncomingListModel() {

			 TransportManager manager = Simulation.instance().getTransportManager();
			 transportList = manager.getIncomingTransportItems();
			 Collections.sort(transportList);

			 // Register as historical event listener.
			 Simulation.instance().getEventManager().addListener(this);
		 }

		 @Override
		 public Transportable getElementAt(int index) {
			 Transportable result = null;
			 if ((index > -1) && (index < transportList.size())) {
				 result = (Transportable) transportList.get(index);
			 }

			 return result;
		 }

		 @Override
		 public int getSize() {
			 return transportList.size();
		 }

		 @Override
			public void eventAdded(int index, SimpleEvent se, HistoricalEvent he) {
			 if (he.getCategory().equals(HistoricalEventCategory.TRANSPORT)) {
				 Transportable transportItem = (Transportable) he.getSource();

				 if (EventType.TRANSPORT_ITEM_CREATED.equals(he.getType())) {
					 if (TransitState.PLANNED == transportItem.getTransitState() ||
							 TransitState.IN_TRANSIT.equals(transportItem.getTransitState())) {
						 transportList.add(transportItem);
						 Collections.sort(transportList);
						 int transportIndex = transportList.indexOf(transportItem);
						 fireIntervalAdded(this, transportIndex, transportIndex);
					 }
				 }
				 else if (EventType.TRANSPORT_ITEM_ARRIVED.equals(he.getType()) ||
						 EventType.TRANSPORT_ITEM_CANCELLED.equals(he.getType())) {
					 int transportIndex = transportList.indexOf(transportItem);
					 transportList.remove(transportItem);
					 fireIntervalRemoved(this, transportIndex, transportIndex);
				 }
				 else if (EventType.TRANSPORT_ITEM_MODIFIED.equals(he.getType())) {
					 if (transportList.contains(transportItem)) {
						 Collections.sort(transportList);
						 fireContentsChanged(this, 0, transportList.size() - 1);
					 }
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
			  transportList.clear();
			  transportList = null;
			  Simulation.instance().getEventManager().removeListener(this);
		  }

	 }
}
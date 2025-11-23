/*
 * Mars Simulation Project
 * ResupplyWindow.java
 * @date 2021-09-20
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.tool.resupply;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import com.mars_sim.core.events.HistoricalEvent;
import com.mars_sim.core.events.HistoricalEventCategory;
import com.mars_sim.core.events.HistoricalEventListener;
import com.mars_sim.core.events.HistoricalEventType;
import com.mars_sim.core.interplanetary.transport.TransportManager;
import com.mars_sim.core.interplanetary.transport.Transportable;
import com.mars_sim.core.interplanetary.transport.resupply.Resupply;
import com.mars_sim.core.interplanetary.transport.settlement.ArrivingSettlement;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.ui.swing.ContentPanel;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.UIContext;


/**
 * Window for the resupply tool.
 * Future: externalize strings
 */
@SuppressWarnings("serial")
public class ResupplyWindow extends ContentPanel
			implements HistoricalEventListener  {

	/** Default logger. */
	private static final SimLogger logger = SimLogger.getLogger(ResupplyWindow.class.getName());

	/** Tool name. */
	public static final String NAME = "resupply";
	public static final String ICON = "resupply";
    public static final String TITLE = "Resupply Tool";

	// Data members

	private TransportDetailPanel detailPane;
	private DefaultTreeModel treeModel;
	private JTree delveryTree;
	private DefaultMutableTreeNode deliveryRoot;
	private Map<String,DefaultMutableTreeNode> settlementNodes = new HashMap<>();
	private Map<Transportable,DefaultMutableTreeNode> deliveryNodes  = new HashMap<>();

	private UIContext context;

	/**
	 * Constructor.
	 * 
	 * @param context the main desktop panel.
	 */
	public ResupplyWindow(UIContext context)  {
		// Use the ToolWindow constructor.
		super(NAME, TITLE);
		this.context = context;

		// Create main panel.
		JPanel mainPane = new JPanel(new BorderLayout());
		mainPane.setBorder(StyleManager.newEmptyBorder());
		add(mainPane, BorderLayout.CENTER);

		// Create the left panel.
		JPanel treePanel = new JPanel(new BorderLayout());
		treePanel.setBorder(StyleManager.createLabelBorder("Transports"));
	
		deliveryRoot = new DefaultMutableTreeNode("Deliveries");
		treeModel = new DefaultTreeModel(deliveryRoot);
		delveryTree = new JTree(treeModel);
		delveryTree.setExpandsSelectedPaths(true);    
		delveryTree.setCellRenderer(new TransportableTreeRenderer());          


		JScrollPane scroller = new JScrollPane(delveryTree);
		scroller.setMinimumSize(new Dimension(200, HEIGHT - 10));
		treePanel.add(scroller, BorderLayout.CENTER);

		// Create detail panel.
		detailPane = new TransportDetailPanel(context);

		JSplitPane spliter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treePanel, detailPane);
		mainPane.add(spliter, BorderLayout.CENTER);

		// Create button panel.
		JPanel buttonPane = new JPanel(new FlowLayout(FlowLayout.CENTER));
		mainPane.add(buttonPane, BorderLayout.SOUTH);

		// Create new button.
		// Change button text from "New"  to "New Mission"
		JButton newButton = new JButton("New Mission");
		newButton.addActionListener(e ->
			new NewTransportItemDialog(context.getTopFrame(), context.getSimulation()).setVisible(true)
		);
		buttonPane.add(newButton);

		// Create modify button.
		// Change button text from "Modify"  to "Modify Mission"
		var modifyButton = new JButton("Modify Mission");
		modifyButton.setEnabled(false);
		modifyButton.addActionListener(e ->
				modifyTransport()
		);
		buttonPane.add(modifyButton);

		// Create cancel button.
		// Change button text from "Discard"  to "Discard Mission"
		var cancelButton = new JButton("Discard Mission");
		cancelButton.setEnabled(false);
		cancelButton.addActionListener(e ->
			cancelTransportItem()
		);
		buttonPane.add(cancelButton);

		delveryTree.addTreeSelectionListener(e -> {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) e
						.getPath().getLastPathComponent();
				Object selection = node.getUserObject();

				boolean selected = false;
				if (selection instanceof Transportable t) {
					detailPane.setTransportable(t);
					selected = true;
				}

				modifyButton.setEnabled(selected);
				cancelButton.setEnabled(selected);
		});

		var dim = new Dimension(600, 600);
		setPreferredSize(dim);
		setSize(dim);

		// Load it
		var sim = context.getSimulation();
		TransportManager manager = sim.getTransportManager();
		for(Transportable in : manager.getTransportItems()) {
			addTreeNode(in);
		}
		sim.getEventManager().addListener(this);
	}

	private DefaultMutableTreeNode addTreeNode(Transportable at) {
		DefaultMutableTreeNode dNode = deliveryNodes.get(at);
		if (dNode == null) {
			String receiver = at.getSettlementName();
			receiver = (receiver == null ? "New Settlement" : receiver);
			DefaultMutableTreeNode sNode = settlementNodes.get(receiver);
			if (sNode == null) {
				sNode = new DefaultMutableTreeNode(receiver, true);
				treeModel.insertNodeInto(sNode, deliveryRoot, deliveryRoot.getChildCount());
				settlementNodes.put(receiver, sNode);
			}	
			dNode = new DefaultMutableTreeNode(at, false);

			// Find the order according to arrival date
			int newIdx = 0;
			Enumeration<TreeNode> existing = sNode.children();
			while (existing.hasMoreElements()) {
				Transportable n = (Transportable) ((DefaultMutableTreeNode)existing.nextElement()).getUserObject();
				if (at.getArrivalDate().getTimeDiff(n.getArrivalDate()) < 0D) {
					// Found the Transportable arriving later than the target
					break;
				} 
				
				newIdx++;
			}
			treeModel.insertNodeInto(dNode, sNode, newIdx);
			deliveryNodes.put(at, dNode);
		}

		// Open Tree
		TreePath path = new TreePath(dNode.getPath());
		delveryTree.makeVisible(path);
		delveryTree.scrollPathToVisible(path);

		return dNode;
	}

	/**
	 * External tools has asked to open a Transportable.
	 * 
	 * @param transport Mission to display
	 */
	public void openTransportable(Transportable transport) {
		DefaultMutableTreeNode found = deliveryNodes.get(transport);
		if (found == null) {
			// Should never happen
			found = addTreeNode(transport);
		}

		TreePath path = new TreePath(found.getPath());
		delveryTree.makeVisible(path);
		delveryTree.setSelectionPath(path);
	}

	/**
	 * Potentially a new Transport item has been loaded or adjusted.
	 * 
	 * @param he Historical view of the event 
	 */
	@Override
	public void eventAdded(HistoricalEvent he) {
		if (HistoricalEventCategory.TRANSPORT == he.getCategory()) {
			if (HistoricalEventType.TRANSPORT_ITEM_MODIFIED == he.getType()) {
				Transportable selected = getSelectedNode();
				if ((selected != null) && he.getSource().equals(selected)) {
					detailPane.setTransportable(selected);
				}
			}
			else if ((HistoricalEventType.TRANSPORT_ITEM_CREATED == he.getType())
					&& (he.getSource() instanceof Transportable t)) {
				addTreeNode(t);
			}
		}
	}

	@Override
	public void eventsRemoved(int startIndex, int endIndex) {
		// Do nothing.
	}

	/**
	 * Updates the time.
	 * 
	 * @param pulse Clock change
	 */
	@Override
	public void update(ClockPulse pulse) {
		detailPane.update(pulse);
	}

	private Transportable getSelectedNode() {
		// Get currently selected incoming transport item.
		DefaultMutableTreeNode treeNode = 
				(DefaultMutableTreeNode) delveryTree.getSelectionPath().getLastPathComponent();
		if (treeNode != null) {
			Object transportItem = treeNode.getUserObject();
			if (transportItem instanceof Transportable t) {
				return t;
			}
		}

		return null;
	}

	/**
	 * Loads modify dialog for the currently selected transport item.
	 */
	private void modifyTransport() {
		// Get currently selected incoming transport item.
		Transportable transportItem = getSelectedNode();
		if (transportItem == null) {
			logger.info("transportItem is null");
			return;
		}
		String title = (transportItem instanceof Resupply) ?
				"Modify Resupply Mission " + transportItem.getName() :
				"Modify Arriving Settlement " + transportItem.getName();
		var dialog = new ModifyTransportItemDialog(context.getTopFrame(), this, title, transportItem, context.getSimulation());
		dialog.setVisible(true);
	}

	
	/**
	 * Refreshes the display because the contents has changed.
	 */
	void refreshMission() {
		detailPane.setTransportable(getSelectedNode());
	}
	
	/**
	 * Cancels the currently selected transport item.
	 */
	private void cancelTransportItem() {
		Transportable transportItem = getSelectedNode();
		if (transportItem == null) {
			logger.info("transportItem is null");
			return;
		}
		
		String msg = "Cancel the delivery Mission " + transportItem.getName() + " ?";

		// Add a dialog box asking the user to confirm "discarding" the mission
		JDialog.setDefaultLookAndFeelDecorated(true);
		final int response = JOptionPane.showConfirmDialog(null, msg, "Confirm",
				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		if (response == JOptionPane.NO_OPTION) {
			// "No" button click, do nothing
		} else if (response == JOptionPane.YES_OPTION) {
			// "Yes" button clicked and go ahead with discarding this mission
			transportItem.cancel();
			detailPane.setTransportable(transportItem);
		} else if (response == JOptionPane.CLOSED_OPTION) {
			// Close the dialogbox, do nothing
		}
	}

	/**
	 * Prepares this window for deletion.
	 */
	@Override
	public void destroy() {
		context.getSimulation().getEventManager().removeListener(this);
	}

	private static class TransportableTreeRenderer extends DefaultTreeCellRenderer {
		private static final Icon ARRIVED = ImageLoader.getIconByName("mission/completed");
		private static final Icon ABORTED = ImageLoader.getIconByName("mission/aborted");
		private static final Icon IN_TRANSIT = ImageLoader.getIconByName("mission/inprogress");
		private static final Icon PENDING = ImageLoader.getIconByName("mission/review");
	  
	  
		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
													  boolean leaf, int row, boolean hasFocus) {
			super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
			Object userObject = node.getUserObject();
			if (userObject instanceof Transportable t) {
				t.getTransitState();
				Icon mIcon = switch(t.getTransitState()) {
					case PLANNED -> PENDING;
					case ARRIVED -> ARRIVED;
					case CANCELED -> ABORTED;
					case IN_TRANSIT -> IN_TRANSIT;
				};
				this.setIcon(mIcon);

				// Build a name
				StringBuilder name = new StringBuilder();
				if (t instanceof Resupply r) {
					name.append(r.getName());
				}
				else if (t instanceof ArrivingSettlement a) {
					name.append(a.getTemplate());
				}				
				name.append(" @ ").append(t.getArrivalDate().getTruncatedDateTimeStamp());
				this.setText(name.toString());
			}
			return this;
		}
	  }
}

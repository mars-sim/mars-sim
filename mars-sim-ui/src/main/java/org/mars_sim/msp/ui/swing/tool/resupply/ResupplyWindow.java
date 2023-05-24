/*
 * Mars Simulation Project
 * ResupplyWindow.java
 * @date 2021-09-20
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.resupply;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.events.HistoricalEvent;
import org.mars_sim.msp.core.events.HistoricalEventCategory;
import org.mars_sim.msp.core.events.HistoricalEventListener;
import org.mars_sim.msp.core.interplanetary.transport.TransportManager;
import org.mars_sim.msp.core.interplanetary.transport.Transportable;
import org.mars_sim.msp.core.interplanetary.transport.resupply.Resupply;
import org.mars_sim.msp.core.interplanetary.transport.settlement.ArrivingSettlement;
import org.mars_sim.msp.core.person.EventType;
import org.mars_sim.msp.core.time.ClockPulse;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.StyleManager;
import org.mars_sim.msp.ui.swing.toolwindow.ToolWindow;


/**
 * Window for the resupply tool.
 * TODO externalize strings
 */

@SuppressWarnings("serial")
public class ResupplyWindow extends ToolWindow
			implements HistoricalEventListener  {

	/** Tool name. */
	public static final String NAME = "Resupply Tool";
	public static final String ICON = "resupply";

	// Data members

	private TransportDetailPanel detailPane;
	private JButton modifyButton;
	private JButton cancelButton;
	private DefaultTreeModel treeModel;
	private JTree delveryTree;
	private DefaultMutableTreeNode deliveryRoot;
	private Map<String,DefaultMutableTreeNode> settlementNodes = new HashMap<>();
	private Map<Transportable,DefaultMutableTreeNode> deliveryNodes  = new HashMap<>();

	/**
	 * Constructor.
	 * @param desktop the main desktop panel.
	 */
	public ResupplyWindow(MainDesktopPane desktop)  {
		// Use the ToolWindow constructor.
		super(NAME, desktop);

		// Create main panel.
		JPanel mainPane = new JPanel(new BorderLayout());
		mainPane.setBorder(MainDesktopPane.newEmptyBorder());
		setContentPane(mainPane);

		// Create the left panel.
		JPanel treePanel = new JPanel(new BorderLayout());
		treePanel.setBorder(StyleManager.createLabelBorder("Transports"));
	
		deliveryRoot = new DefaultMutableTreeNode("Deliveries");
		treeModel = new DefaultTreeModel(deliveryRoot);
		delveryTree = new JTree(treeModel);
		delveryTree.setExpandsSelectedPaths(true);    
		delveryTree.setCellRenderer(new TransportableTreeRenderer());          
		delveryTree.addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent e) {
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
			}
		});


		JScrollPane scroller = new JScrollPane(delveryTree);
		scroller.setMinimumSize(new Dimension(200, HEIGHT - 10));
		treePanel.add(scroller, BorderLayout.CENTER);

		// Create detail panel.
		detailPane = new TransportDetailPanel(desktop);

		JSplitPane spliter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treePanel, detailPane);
		mainPane.add(spliter, BorderLayout.CENTER);

		// Create button panel.
		JPanel buttonPane = new JPanel(new FlowLayout(FlowLayout.CENTER));
		mainPane.add(buttonPane, BorderLayout.SOUTH);

		// Create new button.
		// Change button text from "New"  to "New Mission"
		JButton newButton = new JButton("New Mission");
		newButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				createNewTransportItem();
			}
		});
		buttonPane.add(newButton);

		// Create modify button.
		// Change button text from "Modify"  to "Modify Mission"
		modifyButton = new JButton("Modify Mission");
		modifyButton.setEnabled(false);
		modifyButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				modifyTransport();
			}
		});
		buttonPane.add(modifyButton);

		// Create cancel button.
		// Change button text from "Discard"  to "Discard Mission"
		cancelButton = new JButton("Discard Mission");
		cancelButton.setEnabled(false);
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				cancelTransportItem();
			}
		});
		buttonPane.add(cancelButton);

		setResizable(true);
		setMaximizable(true);

		setSize(new Dimension(600, 600));
		//pack();

		// Load it
		Simulation sim = desktop.getSimulation();
		TransportManager manager = sim.getTransportManager();
		for(Transportable in : manager.getTransportItems()) {
			addTreeNode(in);
		}
		sim.getEventManager().addListener(this);
	}

	private void addTreeNode(Transportable at) {
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
			while(existing.hasMoreElements()) {
				Transportable n = (Transportable) ((DefaultMutableTreeNode)existing.nextElement()).getUserObject();
				if (MarsClock.getTimeDiff(at.getArrivalDate(), n.getArrivalDate()) < 0D) {
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
	}

	
	/**
	 * Potentially a new Transportiem has been loaded or adjusted
	 * @param he Historical view of the event 
	 */
	@Override
	public void eventAdded(HistoricalEvent he) {
		if (HistoricalEventCategory.TRANSPORT == he.getCategory()) {
			if (EventType.TRANSPORT_ITEM_MODIFIED == he.getType()) {
				Transportable selected = getSelectedNode();
				if ((selected != null) && he.getSource().equals(selected)) {
					detailPane.setTransportable(selected);
				}
			}
			else if ((EventType.TRANSPORT_ITEM_CREATED == he.getType())
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
	 * Time has changed
	 * @param pulse Clock change
	 */
	@Override
	public void update(ClockPulse pulse) {
		detailPane.update(pulse);
	}

	/**
	 * Opens a create dialog.
	 */
	private void createNewTransportItem() {
		new NewTransportItemDialog(desktop, this);
	}


	private Transportable getSelectedNode() {
		// Get currently selected incoming transport item.
		DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) delveryTree.getSelectionPath().getLastPathComponent();
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
		if (transportItem instanceof Resupply resupply) {
			// Create modify resupply mission dialog.
			String title = "Modify Resupply Mission " + transportItem.getName();
			new ModifyTransportItemDialog(desktop, this, title, resupply);

		}
		else if (transportItem instanceof ArrivingSettlement settlement) {
			// Create modify arriving settlement dialog.
			String title = "Modify Arriving Settlement " + transportItem.getName();
			new ModifyTransportItemDialog(desktop, this, title, settlement);
		}
	}

	
	/**
	 * Refresh the display because the contents has changed
	 */
	void refreshMission() {
		detailPane.setTransportable(getSelectedNode());
	}
	
	/**
	 * Cancels the currently selected transport item.
	 */
	private void cancelTransportItem() {
		String msg = "Cancel the delivery Mission " + getSelectedNode().getName() + " ?";

		// Add a dialog box asking the user to confirm "discarding" the mission
		JDialog.setDefaultLookAndFeelDecorated(true);
		final int response = JOptionPane.showConfirmDialog(null, msg, "Confirm",
				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		if (response == JOptionPane.NO_OPTION) {
			// "No" button click, do nothing
		} else if (response == JOptionPane.YES_OPTION) {
			// "Yes" button clicked and go ahead with discarding this mission
			Transportable transportItem = getSelectedNode();
			if (transportItem != null) {
				transportItem.cancel();
				detailPane.setTransportable(transportItem);
			}
		} else if (response == JOptionPane.CLOSED_OPTION) {
			// Close the dialogbox, do nothing
		}
	}

	public void setModifyButton(boolean value) {
		modifyButton.setEnabled(value);
	}

	/**
	 * Prepare this window for deletion.
	 */
	@Override
	public void destroy() {
		desktop.getSimulation().getEventManager().addListener(this);
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
				name.append(" @ ").append(t.getArrivalDate().getDateString());
				this.setText(name.toString());
			}
			return this;
		}
	  }
}

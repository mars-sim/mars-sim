/**
 * Mars Simulation Project
 * BuildingPanelMaintenance.java
 * @version 3.1.2 2020-09-02
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.structure.building;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoundedRangeModel;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.malfunction.MalfunctionManager;
import org.mars_sim.msp.core.malfunction.Malfunctionable;
import org.mars_sim.msp.core.resource.ItemResourceUtil;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.resource.Part.MaintenanceEntity;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.NumberCellRenderer;
import org.mars_sim.msp.ui.swing.tool.Conversion;
import org.mars_sim.msp.ui.swing.tool.TableStyle;
import org.mars_sim.msp.ui.swing.tool.ZebraJTable;

import com.alee.laf.panel.WebPanel;
import com.alee.laf.scroll.WebScrollPane;

/**
 * The BuildingPanelMaintenance class is a building function panel representing
 * the maintenance state of a settlement building.
 */
@SuppressWarnings("serial")
public class BuildingPanelMaintenance extends BuildingFunctionPanel {

	/** Cached value for the wear condition. */
	private int wearConditionCache;
	/** The time since last completed maintenance. */
	private int lastCompletedTime;

	/** The malfunctionable building. */
	private Malfunctionable malfunctionable;
	/** The Inventory instance. */
	private Inventory inv;
	/** The malfunction manager instance. */
	private MalfunctionManager manager;

	/** The wear condition label. */
	private JLabel wearConditionLabel;
	/** The last completed label. */
	private JLabel lastCompletedLabel;
	/** Label for parts. */
	private JLabel partsLabel;

	/** The progress bar model. */
	private BoundedRangeModel progressBarModel;
	/** The parts table model. */
	private PartTableModel tableModel;
	/** The parts table. */
	private JTable table;

	/** The parts to be maintained this entity. */
	private LinkedHashMap<Part, List<String>> standardMaintParts;

	/**
	 * Constructor.
	 * 
	 * @param malfunctionable the malfunctionable building the panel is for.
	 * @param desktop         The main desktop.
	 */
	public BuildingPanelMaintenance(Malfunctionable malfunctionable, MainDesktopPane desktop) {

		// Use BuildingFunctionPanel constructor
		super((Building) malfunctionable, desktop);

		// Initialize data members.
		inv = ((Building) malfunctionable).getInventory();
		this.malfunctionable = malfunctionable;
		manager = malfunctionable.getMalfunctionManager();
		standardMaintParts = manager.getStandardMaintParts();
	
		// Set the layout
		setLayout(new BorderLayout(1, 1));
		
		WebPanel labelPanel = new WebPanel(new GridLayout(6, 1, 2, 2));
		add(labelPanel, BorderLayout.NORTH);
		
		// Create maintenance label.
		JLabel maintenanceLabel = new JLabel(Msg.getString("BuildingPanelMaintenance.title"), JLabel.CENTER);
		maintenanceLabel.setFont(new Font("Serif", Font.BOLD, 16));
		// maintenanceLabel.setForeground(new Color(102, 51, 0)); // dark brown
		labelPanel.add(maintenanceLabel);

		labelPanel.add(new JLabel(" "));
		
		// Create wear condition label.
		int wearConditionCache = (int) Math.round(manager.getWearCondition());
		wearConditionLabel = new JLabel(Msg.getString("BuildingPanelMaintenance.wearCondition", wearConditionCache),
				JLabel.CENTER);
		wearConditionLabel.setToolTipText(Msg.getString("BuildingPanelMaintenance.wear.toolTip"));
		labelPanel.add(wearConditionLabel);

		// Create lastCompletedLabel.
		lastCompletedTime = (int) (manager.getTimeSinceLastMaintenance() / 1000D);
		lastCompletedLabel = new JLabel(Msg.getString("BuildingPanelMaintenance.lastCompleted", lastCompletedTime),
				JLabel.CENTER);
		labelPanel.add(lastCompletedLabel);

		// Create maintenance progress bar panel.
		JPanel progressPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		labelPanel.add(progressPanel);
		progressPanel.setOpaque(false);
		progressPanel.setBackground(new Color(0, 0, 0, 128));

		// Prepare progress bar.
		JProgressBar progressBar = new JProgressBar();
		progressBarModel = progressBar.getModel();
		progressBar.setStringPainted(true);
		progressPanel.add(progressBar);

		// Set initial value for progress bar.
		double completed = manager.getMaintenanceWorkTimeCompleted();
		double total = manager.getMaintenanceWorkTime();
		int percentDone = (int) (100D * (completed / total));
		progressBarModel.setValue(percentDone);

		// Prepare maintenance parts label.
		partsLabel = new JLabel(getPartsString(false), JLabel.CENTER);
		partsLabel.setPreferredSize(new Dimension(-1, -1));
		labelPanel.add(partsLabel);
		
		// Create the parts panel
		WebScrollPane partsPane = new WebScrollPane();

		WebPanel tablePanel = new WebPanel();
		tablePanel.add(partsPane);
		
		add(tablePanel, BorderLayout.CENTER);

		
		UIManager.getDefaults().put("TitledBorder.titleColor", Color.darkGray);
		Border lowerEtched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
		TitledBorder title = BorderFactory.createTitledBorder(
	        		lowerEtched, " " + Msg.getString("BuildingPanelMaintenance.tableBorder") + " ");
//	      title.setTitleJustification(TitledBorder.RIGHT);
		Font titleFont = UIManager.getFont("TitledBorder.font");
		title.setTitleFont(titleFont.deriveFont(Font.ITALIC + Font.BOLD));
		
		tablePanel.setBorder(title);
		
		// Create the parts table model
		tableModel = new PartTableModel(inv);

		// Create the parts table
		table = new ZebraJTable(tableModel);
		table.setPreferredScrollableViewportSize(new Dimension(220, 125));
		table.setRowSelectionAllowed(true);// .setCellSelectionEnabled(true);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//        table.getSelectionModel().addListSelectionListener(this);
		partsPane.setViewportView(table);

		table.setDefaultRenderer(Double.class, new NumberCellRenderer(2, true));

		table.getColumnModel().getColumn(0).setPreferredWidth(110);
		table.getColumnModel().getColumn(1).setPreferredWidth(90);
		table.getColumnModel().getColumn(2).setPreferredWidth(30);
		table.getColumnModel().getColumn(3).setPreferredWidth(40);

		DefaultTableCellRenderer renderer1 = new DefaultTableCellRenderer();
		renderer1.setHorizontalAlignment(SwingConstants.CENTER);
		table.getColumnModel().getColumn(2).setCellRenderer(renderer1);
		table.getColumnModel().getColumn(3).setCellRenderer(renderer1);
		
		// Align the preference score to the center of the cell
		DefaultTableCellRenderer renderer2 = new DefaultTableCellRenderer();
		renderer2.setHorizontalAlignment(SwingConstants.LEFT);
		table.getColumnModel().getColumn(0).setCellRenderer(renderer2);
		table.getColumnModel().getColumn(1).setCellRenderer(renderer2);


		// Added sorting
		table.setAutoCreateRowSorter(true);

		// Add a mouse listener to hear for double-clicking a part (rather than single
		// click using valueChanged()
//        table.addMouseListener(new MouseAdapter() {
//		    public void mousePressed(MouseEvent me) {
//		    	JTable table =(JTable) me.getSource();
//		        Point p = me.getPoint();
//		        int row = table.rowAtPoint(p);
//		        int col = table.columnAtPoint(p);
//		        if (me.getClickCount() == 2) {
//		            if (row > 0 && col > 0) {
//		    		    String name = ((Equipment)table.getValueAt(row, 1)).getName();
////    		    		System.out.println("name : " + name + "   row : " + row);
//		    		    for (Part p : partList) {
////	    		    		System.out.println("nickname : " + e.getName());
//		    		    	if (p.getName().equalsIgnoreCase(name)) {
////		    		    		System.out.println("name : " + name + "   nickname : " + e.getName());
//				    		    desktop.openUnitWindow(p, false);
//		    		    	}
//		    		    } 	    			
//		    	    }
//		        }
//		    }
//		});

		// Added setTableStyle()
		TableStyle.setTableStyle(table);
	}

	/**
	 * Update this panel
	 */
	public void update() {

		// Update the wear condition label.
		int wearCondition = (int) Math.round(manager.getWearCondition());
		if (wearCondition != wearConditionCache) {
			wearConditionCache = wearCondition;
			wearConditionLabel.setText(Msg.getString("BuildingPanelMaintenance.wearCondition", wearConditionCache));
		}

		// Update last completed label.
		int lastComplete = (int) (manager.getTimeSinceLastMaintenance() / 1000D);
		if (lastComplete != lastCompletedTime) {
			lastCompletedTime = lastComplete;
			lastCompletedLabel.setText(Msg.getString("BuildingPanelMaintenance.lastCompleted", lastCompletedTime));
		}

		// Update tool tip.
		lastCompletedLabel.setToolTipText(getToolTipString());

		// Update progress bar.
		double completed = manager.getMaintenanceWorkTimeCompleted();
		double total = manager.getMaintenanceWorkTime();
		int percentDone = (int) (100D * (completed / total));
		progressBarModel.setValue(percentDone);

		// Update parts label.
		partsLabel.setText(getPartsString(false));
		// Update tool tip.
		partsLabel.setToolTipText("<html>" + getPartsString(true) + "</html>");

	}

	/**
	 * Gets the parts string.
	 * 
	 * @return string.
	 */
	private String getPartsString(boolean useHtml) {
		StringBuilder buf = new StringBuilder("Needed Parts: ");

		Map<Integer, Integer> parts = manager.getMaintenanceParts();
		if (parts.size() > 0) {
			Iterator<Integer> i = parts.keySet().iterator();
			while (i.hasNext()) {
				Integer part = i.next();
				int number = parts.get(part);
				if (useHtml)
					buf.append("<br>");
				buf.append(number).append(" ")
						.append(Conversion.capitalize(ItemResourceUtil.findItemResource(part).getName()));
				if (i.hasNext())
					buf.append(", ");
				else {
					buf.append(".");
					if (useHtml)
						buf.append("<br>");
				}
			}
		} else
			buf.append("None.");

		return buf.toString();
	}

	/**
	 * Creates multi-line tool tip text.
	 */
	private String getToolTipString() {
		StringBuilder result = new StringBuilder("<html>");
		result.append("The Very Last Maintenance Was Completed ").append(lastCompletedTime).append(" Sols Ago<br>");
		result.append("</html>");
		return result.toString();
	}

	/**
	 * Internal class used as model for the equipment table.
	 */
	private class PartTableModel extends AbstractTableModel {

		private static final String WHITESPACE = " ";

		private int size;
		
//		private Inventory inventory;

		private List<Part> parts = new ArrayList<>();
		private List<String> functions = new ArrayList<>();
		private List<Integer> max = new ArrayList<>();
		private List<Integer> probability = new ArrayList<>();

		
		/**
		 * hidden constructor.
		 * 
		 * @param inventory {@link Inventory}
		 */
		private PartTableModel(Inventory inventory) {
//			this.inventory = inventory;
			
			size = standardMaintParts.size();
			
			for (Part p: standardMaintParts.keySet()) {

				List<String> fList = standardMaintParts.get(p);
				for (String f: fList) {
					parts.add(p);
					functions.add(f);
					for (MaintenanceEntity me: p.getMaintenanceEntities()) {
						if (me.getName().equalsIgnoreCase(f)) {
							max.add(me.getMaxNumber());
							probability.add(me.getProbability());
						}
					}
				}
			}		
		}

		public int getRowCount() {
			return size;
		}

		public int getColumnCount() {
			return 4;
		}

		public Class<?> getColumnClass(int columnIndex) {
			Class<?> dataType = super.getColumnClass(columnIndex);
			if (columnIndex == 0)
				dataType = String.class;
			else if (columnIndex == 1)
				dataType = String.class;
			else if (columnIndex == 2)
				dataType = Integer.class;
			else if (columnIndex == 3)
				dataType = Double.class;
			return dataType;
		}

		public String getColumnName(int columnIndex) {
			if (columnIndex == 0)
				return Msg.getString("BuildingPanelMaintenance.header.part"); //$NON-NLS-1$
			else if (columnIndex == 1)
				return Msg.getString("BuildingPanelMaintenance.header.function"); //$NON-NLS-1$
			else if (columnIndex == 2)
				return Msg.getString("BuildingPanelMaintenance.header.max"); //$NON-NLS-1$
			else if (columnIndex == 3)
				return Msg.getString("BuildingPanelMaintenance.header.probability"); //$NON-NLS-1$
			else
				return "unknown";
		}

		public Object getValueAt(int row, int column) {
			if (parts != null && row >= 0 && row < parts.size()) {
				if (column == 0)
					return WHITESPACE + Conversion.capitalize(parts.get(row).getName()) + WHITESPACE;
				else if (column == 1)
					return functions.get(row);
				else if (column == 2)
					return max.get(row);
				else if (column == 3)
					return probability.get(row);
			}
			return "unknown";
		}

//		public void update() {
//			List<Equipment> newNames = new ArrayList<>();
//			Map<String, String> newTypes = new HashMap<>();
//			Map<String, String> newEquipment = new HashMap<>();
//			Map<String, Double> newMass = new HashMap<>();
//			for (Equipment e : inventory.findAllEquipment()) {
//				newTypes.put(e.getName(), e.getType());
//				newEquipment.put(e.getName(), showOwner(e));
//				newMass.put(e.getName(), e.getMass());
//				newNames.add(e);
//			}
//			Collections.sort(newNames);// , new NameComparator());
//
//			if (equipmentList.size() != newNames.size() || !equipmentList.equals(newNames)) {
//				equipmentList = newNames;
//				equipment = newEquipment;
//				types = newTypes;
//				fireTableDataChanged();
//			}
//		}
	}
}

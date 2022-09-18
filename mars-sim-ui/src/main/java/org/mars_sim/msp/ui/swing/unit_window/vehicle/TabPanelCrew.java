/*
 * Mars Simulation Project
 * TabPanelCrew.java
 * @date 2022-07-09
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.vehicle;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.UnitEvent;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.UnitListener;
import org.mars_sim.msp.core.UnitType;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.core.person.ai.task.util.Worker;
import org.mars_sim.msp.core.vehicle.Crewable;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.tool.SpringUtilities;
import org.mars_sim.msp.ui.swing.tool.TableStyle;
import org.mars_sim.msp.ui.swing.tool.ZebraJTable;
import org.mars_sim.msp.ui.swing.tool.monitor.PersonTableModel;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;

import com.alee.laf.button.WebButton;
import com.alee.laf.panel.WebPanel;
import com.alee.laf.scroll.WebScrollPane;

/**
 * The TabPanelCrew is a tab panel for a vehicle's crew information.
 */
@SuppressWarnings("serial")
public class TabPanelCrew extends TabPanel implements ActionListener {

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(TabPanelCrew.class.getName());

	private static final String SAILOR_ICON = Msg.getString("icon.sailor"); //$NON-NLS-1$

	
	private MemberTableModel memberTableModel;
	private JTable memberTable;

	private JTextField crewNumTF;

	private int crewNumCache;
	private int crewCapacityCache;

	/** The mission instance. */
	private Mission mission;
	/** The Crewable instance. */
	private Crewable crewable;

	/**
	 * Constructor.
	 * 
	 * @param vehicle the vehicle.
	 * @param desktop the main desktop.
	 */
	public TabPanelCrew(Vehicle vehicle, MainDesktopPane desktop) {
		// Use the TabPanel constructor
		super(
			null,
			ImageLoader.getNewIcon(SAILOR_ICON),
			Msg.getString("TabPanelCrew.tooltip"), //$NON-NLS-1$
			vehicle, desktop
		);

		crewable = (Crewable) vehicle;
		mission = vehicle.getMission();
	}

	@Override
	protected void buildUI(JPanel content) {
        JPanel northPanel = new JPanel();
        northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));
        
		// Create crew count panel
		WebPanel crewCountPanel = new WebPanel(new SpringLayout());
		northPanel.add(crewCountPanel, BorderLayout.CENTER);

		// Create crew num header label
		crewNumCache = crewable.getCrewNum();
		crewNumTF = addTextField(crewCountPanel, Msg.getString("TabPanelCrew.crewNum"), crewNumCache,
								 Msg.getString("TabPanelCrew.crew.tooltip"));

		// Create crew cap header label
		crewCapacityCache = crewable.getCrewCapacity();
		addTextField(crewCountPanel, Msg.getString("TabPanelCrew.crewCapacity"), crewCapacityCache,
					 Msg.getString("TabPanelCrew.crewCapacity.tooltip"));

		// Prepare SpringLayout.
		SpringUtilities.makeCompactGrid(crewCountPanel,
				2, 2, // rows, cols
				120, 2, // initX, initY
				XPAD_DEFAULT, YPAD_DEFAULT); // xPad, yPad

		// Create crew monitor button
		WebButton monitorButton = new WebButton(ImageLoader.getIcon(Msg.getString("img.monitor"))); //$NON-NLS-1$
		monitorButton.setMargin(new Insets(1, 1, 1, 1));
		monitorButton.addActionListener(this);
		monitorButton.setToolTipText(Msg.getString("TabPanelCrew.tooltip.monitor")); //$NON-NLS-1$

		WebPanel crewButtonPanel = new WebPanel(new FlowLayout(FlowLayout.CENTER));
		crewButtonPanel.add(monitorButton);
		northPanel.add(crewButtonPanel, BorderLayout.SOUTH);
       	content.add(northPanel, BorderLayout.NORTH);

		// Create scroll panel for member list.
		WebScrollPane memberScrollPane = new WebScrollPane();
		memberScrollPane.setPreferredSize(new Dimension(300, 300));
		content.add(memberScrollPane, BorderLayout.CENTER);

		// Create member table model.
		memberTableModel = new MemberTableModel();
		if (mission != null)
			memberTableModel.setMission(mission);

		// Create member table.
		memberTable = new ZebraJTable(memberTableModel);
		TableStyle.setTableStyle(memberTable);
		memberTable.getColumnModel().getColumn(0).setPreferredWidth(110);
		memberTable.getColumnModel().getColumn(1).setPreferredWidth(140);
		memberTable.setRowSelectionAllowed(true);
		memberTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
		renderer.setHorizontalAlignment(SwingConstants.CENTER);
		memberTable.getColumnModel().getColumn(2).setCellRenderer(renderer);
		memberScrollPane.setViewportView(memberTable);

		// Call it a click to display details button when user double clicks the table
		memberTable.addMouseListener(new MouseListener() {
			public void mouseReleased(MouseEvent e) {
			}

			public void mousePressed(MouseEvent e) {
			}

			public void mouseExited(MouseEvent e) {
			}

			public void mouseEntered(MouseEvent e) {
			}

			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2 && !e.isConsumed()) {
			        int index = memberTable.getSelectedRow();
	    			Person selectedPerson = (Person) memberTableModel.getMemberAtIndex(index);
	    			if (selectedPerson != null)
	    				getDesktop().openUnitWindow(selectedPerson, false);
		        }
			}
		});

		update();
	}

	/**
	 * Updates the info on this panel.
	 */
	@Override
	public void update() {
		Vehicle vehicle = (Vehicle) getUnit();
		Crewable crewable = (Crewable) vehicle;
		Mission newMission = vehicle.getMission();
		if (mission != newMission) {
			mission = newMission;
			memberTableModel.setMission(newMission);
		}

		// Update crew num
		if (crewNumCache != crewable.getCrewNum() ) {
			crewNumCache = crewable.getCrewNum() ;
			crewNumTF.setText(crewNumCache + "");
		}

		// Update crew table
		memberTableModel.updateMembers();
	}

	/**
	 * Action event occurs.
	 * 
	 * @param event the action event
	 */
	public void actionPerformed(ActionEvent event) {
		// If the crew monitor button was pressed, create tab in monitor tool.
		Vehicle vehicle = (Vehicle) getUnit();
		Crewable crewable = (Crewable) vehicle;
		try {
			getDesktop().addModel(new PersonTableModel(crewable));
		} catch (Exception e) {
			logger.severe("PersonTableModel cannot be added.");
		}
	}

	@Override
	public void destroy() {
		super.destroy();
		
		crewNumTF = null;
		memberTable = null;
		memberTableModel = null;
	}

	/**
	 * Table model for mission members.
	 */
	private class MemberTableModel extends AbstractTableModel implements UnitListener {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

		// Private members.
		private Mission mission;
		private List<Worker> members;

		/**
		 * Constructor.
		 */
		private MemberTableModel() {
			mission = null;
			members = new ArrayList<>();
		}

		/**
		 * Gets the row count.
		 *
		 * @return row count.
		 */
		public int getRowCount() {
			return members.size();
		}

		/**
		 * Gets the column count.
		 *
		 * @return column count.
		 */
		public int getColumnCount() {
			return 3;
		}

		/**
		 * Gets the column name at a given index.
		 *
		 * @param columnIndex the column's index.
		 * @return the column name.
		 */
		public String getColumnName(int columnIndex) {
			if (columnIndex == 0)
				return Msg.getString("MainDetailPanel.column.name"); //$NON-NLS-1$
			else if (columnIndex == 1)
				return Msg.getString("MainDetailPanel.column.task"); //$NON-NLS-1$
			else
				return "Boarded";
		}

		/**
		 * Gets the value at a given row and column.
		 *
		 * @param row    the table row.
		 * @param column the table column.
		 * @return the value.
		 */
		public Object getValueAt(int row, int column) {
			if (row < members.size()) {
				Object array[] = members.toArray();
				Worker member = (Worker) array[row];
				if (column == 0)
					return member.getName();
				else if (column == 1)
					return member.getTaskDescription();
				else {
					if (boarded(member))
						return "Y";
					else
						return "N";
				}
			} else
				return Msg.getString("unknown"); //$NON-NLS-1$
		}

		/**
		 * Has this member boarded the vehicle ?
		 *
		 * @param member
		 * @return
		 */
		boolean boarded(Worker member) {
			if (mission instanceof VehicleMission) {			
				if (member.getUnitType() == UnitType.PERSON) {
					Rover r = (Rover)(((VehicleMission)mission).getVehicle());
					if (r != null && r.isCrewmember((Person)member))
						return true;
				}
			}
			return false;
		}


		/**
		 * Sets the mission for this table model.
		 *
		 * @param newMission the new mission.
		 */
		void setMission(Mission newMission) {
			this.mission = newMission;
			updateMembers();
		}

		/**
		 * Catch unit update event.
		 *
		 * @param event the unit event.
		 */
		public void unitUpdate(UnitEvent event) {
			UnitEventType type = event.getType();
			Worker member = (Worker) event.getSource();
			int index = getIndex(members, member);
			if (type == UnitEventType.NAME_EVENT) {
				SwingUtilities.invokeLater(new MemberTableUpdater(index, 0));
			} else if ((type == UnitEventType.TASK_DESCRIPTION_EVENT) || (type == UnitEventType.TASK_EVENT)
					|| (type == UnitEventType.TASK_ENDED_EVENT) || (type == UnitEventType.TASK_SUBTASK_EVENT)
					|| (type == UnitEventType.TASK_NAME_EVENT)) {
				SwingUtilities.invokeLater(new MemberTableUpdater(index, 1));
			}
		}

		private int getIndex(Collection<?> col, Object obj) {
			int result = -1;
			Object array[] = col.toArray();
			int size = array.length;

			for (int i = 0; i < size; i++) {
				if (array[i].equals(obj)) {
					result = i;
					break;
				}
			}

			return result;
		}

		/**
		 * Update mission members.
		 */
		void updateMembers() {
			if (mission != null) {
//				clearMembers();
				List<Worker> newList = new ArrayList<Worker>(mission.getMembers());
				Collections.sort(newList, new Comparator<Worker>() {
					@Override
					public int compare(Worker o1, Worker o2) {
						return o1.getName().compareToIgnoreCase(o2.getName());
					}
				});

				if (!members.equals(newList)) {
					List<Integer> rows = new ArrayList<Integer>();

					for (Worker mm: members) {
						if (!newList.contains(mm)) {
							mm.removeUnitListener(this);
						}
					}

					for (Worker mm: newList) {
						if (!members.contains(mm)) {
							mm.addUnitListener(this);
							int index = newList.indexOf(mm);
							rows.add(index);
						}
					}

					// Replace the old member list with new one.
					members = newList;

					for (int i : rows) {
						// Update this row
						SwingUtilities.invokeLater(new MemberTableUpdater(i));
					}
				}
			} else {
				if (members.size() > 0) {
					clearMembers();
					SwingUtilities.invokeLater(new MemberTableUpdater());
				}
			}
		}

		/**
		 * Clear all members from the table.
		 */
		private void clearMembers() {
			if (members != null) {
				Iterator<Worker> i = members.iterator();
				while (i.hasNext()) {
					Worker member = i.next();
					member.removeUnitListener(this);
				}
				members.clear();
			}
		}

		/**
		 * Gets the mission member at a given index.
		 *
		 * @param index the index.
		 * @return the mission member.
		 */
		Worker getMemberAtIndex(int index) {
			if ((index >= 0) && (index < members.size())) {
				return (Worker) members.toArray()[index];
			} else {
				return null;
			}
		}

		/**
		 * Inner class for updating member table.
		 */
		private class MemberTableUpdater implements Runnable {

			private int row;
			private int column;
			private boolean entireData;

			private MemberTableUpdater(int row, int column) {
				this.row = row;
				this.column = column;
				entireData = false;
			}

			private MemberTableUpdater(int row) {
				this.row = row;
				this.column = -1;
				entireData = false;
			}

			private MemberTableUpdater() {
				entireData = true;
			}

			public void run() {
				if (entireData) {
					fireTableDataChanged();
				} else if (column == -1) {
					fireTableRowsUpdated(row, row);
				} else {
					fireTableCellUpdated(row, column);
				}
			}
		}
	}
}

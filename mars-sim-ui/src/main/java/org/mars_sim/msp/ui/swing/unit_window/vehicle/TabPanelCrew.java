/**
 * Mars Simulation Project
 * TabPanelCrew.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.vehicle;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
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

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.UnitEvent;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.UnitListener;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionMember;
import org.mars_sim.msp.core.vehicle.Crewable;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.tool.SpringUtilities;
import org.mars_sim.msp.ui.swing.tool.TableStyle;
import org.mars_sim.msp.ui.swing.tool.ZebraJTable;
import org.mars_sim.msp.ui.swing.tool.monitor.PersonTableModel;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;

import com.alee.laf.button.WebButton;
import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;
import com.alee.laf.scroll.WebScrollPane;
import com.alee.laf.text.WebTextField;

/** 
 * The TabPanelCrew is a tab panel for a vehicle's crew information.
 */
public class TabPanelCrew extends TabPanel implements ActionListener {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private WebLabel crewNumLabel;
	
	private MemberTableModel memberTableModel;
	private JTable memberTable;
	
	private WebTextField crewNumTF;

	private int crewNumCache;
	private int crewCapacityCache;

	/** Is UI constructed. */
	private boolean uiDone = false;
	
	/** The mission instance. */
	private Mission mission;
	/** The Crewable instance. */
	private Crewable crewable;
	
	/**
	 * Constructor.
	 * @param vehicle the vehicle.
	 * @param desktop the main desktop.
	 */
	public TabPanelCrew(Vehicle vehicle, MainDesktopPane desktop) { 
		// Use the TabPanel constructor
		super(
			Msg.getString("TabPanelCrew.title"), //$NON-NLS-1$
			null,
			Msg.getString("TabPanelCrew.tooltip"), //$NON-NLS-1$
			vehicle, desktop
		);

		crewable = (Crewable) vehicle;
		mission = vehicle.getMission();
	}

	public boolean isUIDone() {
		return uiDone;
	}
	
	public void initializeUI() {
		uiDone = true;
		
		// Prepare title label.
		WebPanel titlePanel = new WebPanel(new FlowLayout(FlowLayout.CENTER));
		WebLabel titleLabel = new WebLabel(Msg.getString("TabPanelCrew.title"), WebLabel.CENTER); //$NON-NLS-1$
		titleLabel.setFont(new Font("Serif", Font.BOLD, 16));
		titlePanel.add(titleLabel);
		topContentPanel.add(titlePanel);

		// Create crew count panel
		WebPanel crewCountPanel = new WebPanel(new SpringLayout()); //GridLayout(2, 1, 0, 0));
//		crewCountPanel.setBorder(new MarsPanelBorder());
//		crewCountPanel.setPreferredSize(new Dimension(-1, HEIGHT_0));
		topContentPanel.add(crewCountPanel);
		
		// Create crew num header label
		WebLabel crewNumHeader = new WebLabel(Msg.getString("TabPanelCrew.crew"), WebLabel.RIGHT); //$NON-NLS-1$
		crewNumHeader.setToolTipText(Msg.getString("TabPanelCrew.crew.tooltip")); //$NON-NLS-1$
		crewCountPanel.add(crewNumHeader);
		
		crewNumCache = crewable.getCrewNum();
		
		crewNumTF = new WebTextField();
		crewNumTF.setEditable(true);
		crewNumTF.setColumns(4);
//		crewNumTF.setAlignmentX(Component.CENTER_ALIGNMENT);
		crewNumTF.setText(crewNumCache + "");
		
		WebPanel wrapper0 = new WebPanel(new FlowLayout(0, 0, FlowLayout.LEADING));
		wrapper0.add(crewNumTF);
		crewCountPanel.add(wrapper0);

		// Create crew cap header label
		WebLabel crewCapHeaderLabel = new WebLabel(Msg.getString("TabPanelCrew.crewCapacity"), WebLabel.RIGHT); //$NON-NLS-1$
		crewCapHeaderLabel.setToolTipText(Msg.getString("TabPanelCrew.crewCapacity.tooltip")); //$NON-NLS-1$
		crewCountPanel.add(crewCapHeaderLabel);
		
		crewCapacityCache = crewable.getCrewCapacity();
		
		// Create crew capacity label
		WebLabel crewCapLabel = new WebLabel(" " + crewCapacityCache, WebLabel.LEFT); //$NON-NLS-1$
		crewCountPanel.add(crewCapLabel);
		
		// Prepare SpringLayout.
		SpringUtilities.makeCompactGrid(crewCountPanel, 
				2, 2, // rows, cols
				120, 2, // initX, initY
				15, 2); // xPad, yPad
		
		// Create crew display panel
		WebPanel crewDisplayPanel = new WebPanel(new BorderLayout(0, 0)); //FlowLayout(FlowLayout.LEFT));
//		crewDisplayPanel.setBorder(new MarsPanelBorder());
		topContentPanel.add(crewDisplayPanel);

		// Prepare member list panel
		WebPanel memberListPane = new WebPanel(new BorderLayout(0, 0));
		memberListPane.setPreferredSize(new Dimension(100, 150));
		crewDisplayPanel.add(memberListPane, BorderLayout.CENTER);

		// Create scroll panel for member list.
		WebScrollPane memberScrollPane = new WebScrollPane();
		// memberScrollPane.setPreferredSize(new Dimension(300, 250));
		memberListPane.add(memberScrollPane, BorderLayout.CENTER);

		// Create member table model.
		memberTableModel = new MemberTableModel();
		if (mission != null)
			memberTableModel.setMission(mission);
		
		// Create member table.
		memberTable = new ZebraJTable(memberTableModel);
		TableStyle.setTableStyle(memberTable);
		// memberTable.setPreferredSize(new Dimension(300, 250));
		memberTable.getColumnModel().getColumn(0).setPreferredWidth(100);
		memberTable.getColumnModel().getColumn(1).setPreferredWidth(100);
		memberTable.setRowSelectionAllowed(true);
		memberTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		memberScrollPane.setViewportView(memberTable);
		
		// call it a click to display details button when user double clicks the table
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
	    				desktop.openUnitWindow(selectedPerson, false);
		        }
			}
		});
		
		// Create crew monitor button
		WebButton monitorButton = new WebButton(ImageLoader.getIcon(Msg.getString("img.monitor"))); //$NON-NLS-1$
		monitorButton.setMargin(new Insets(1, 1, 1, 1));
		monitorButton.addActionListener(this);
		monitorButton.setToolTipText(Msg.getString("TabPanelCrew.tooltip.monitor")); //$NON-NLS-1$
		
		WebPanel crewButtonPanel = new WebPanel(new FlowLayout(FlowLayout.CENTER));
		crewButtonPanel.add(monitorButton);
		crewDisplayPanel.add(crewButtonPanel, BorderLayout.NORTH);
		
		update();
	}

	/**
	 * Updates the info on this panel.
	 */
	public void update() {
		if (!uiDone)
			initializeUI();
		
		Vehicle vehicle = (Vehicle) unit;
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
	 * @param event the action event
	 */
	public void actionPerformed(ActionEvent event) {
		// If the crew monitor button was pressed, create tab in monitor tool.
		Vehicle vehicle = (Vehicle) unit;
		Crewable crewable = (Crewable) vehicle;
		desktop.addModel(new PersonTableModel(crewable));
	}

	public void destroy() {
		crewNumLabel = null; 
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
		private List<MissionMember> members;

		/**
		 * Constructor.
		 */
		private MemberTableModel() {
			mission = null;
			members = new ArrayList<MissionMember>();
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
			return 2;
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
				return Msg.getString("unknown"); //$NON-NLS-1$
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
				MissionMember member = (MissionMember) array[row];
				if (column == 0)
					return member.getName();
				else
					return member.getTaskDescription();
			} else
				return Msg.getString("unknown"); //$NON-NLS-1$
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
			MissionMember member = (MissionMember) event.getSource();
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
				clearMembers();
				members = new ArrayList<MissionMember>(mission.getMembers());
				Collections.sort(members, new Comparator<MissionMember>() {

					@Override
					public int compare(MissionMember o1, MissionMember o2) {
						return o1.getName().compareToIgnoreCase(o2.getName());
					}
				});
				Iterator<MissionMember> i = members.iterator();
				while (i.hasNext()) {
					MissionMember member = i.next();
					member.addUnitListener(this);
				}
				SwingUtilities.invokeLater(new MemberTableUpdater());
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
				Iterator<MissionMember> i = members.iterator();
				while (i.hasNext()) {
					MissionMember member = i.next();
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
		MissionMember getMemberAtIndex(int index) {
			if ((index >= 0) && (index < members.size())) {
				return (MissionMember) members.toArray()[index];
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

			private MemberTableUpdater() {
				entireData = true;
			}

			public void run() {
				if (entireData) {
					fireTableDataChanged();
				} else {
					fireTableCellUpdated(row, column);
				}
			}
		}
	}
}

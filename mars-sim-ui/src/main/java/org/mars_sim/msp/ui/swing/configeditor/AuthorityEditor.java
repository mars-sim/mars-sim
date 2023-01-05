/*
 * Mars Simulation Project
 * AuthorityEditor.java
 * @date 2021-09-04
 * @author Barry Evans
 */
package org.mars_sim.msp.ui.swing.configeditor;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.reportingAuthority.MissionAgenda;
import org.mars_sim.msp.core.reportingAuthority.MissionSubAgenda;
import org.mars_sim.msp.core.reportingAuthority.ReportingAuthority;
import org.mars_sim.msp.core.reportingAuthority.ReportingAuthorityFactory;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MainWindow;

/**
 * UI Editor that allows ReportingAuthorities to be edited
 */
public class AuthorityEditor  {

	private static final String OBJECTIVE = "Objective: ";


	class TextList {
		private JPanel content;
		private DefaultListModel<String> model;
		
		TextList(String title) {
			content = new JPanel();
			content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
			content.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
			//content.setBorder(BorderFactory.createTitledBorder(title));

			model = new DefaultListModel<String>();
			JList<String> list = new JList<>(model);
			content.add(new JScrollPane(list));
			
			Box newItemPanel = Box.createHorizontalBox();
			newItemPanel.add(new JLabel("New " + title + " :"));
			JTextField newText = new JTextField();
			newText.setColumns(20);
			newText.setMaximumSize(newText.getPreferredSize());
			newItemPanel.add(newText);
			content.add(newItemPanel);
			
			JPanel controlPanel = new JPanel();
			JButton addButton = new JButton(Msg.getString("AuthorityEditor.button.add"));
			addButton.setToolTipText(Msg.getString("AuthorityEditor.tooltop.add", title));
			addButton.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					model.addElement(newText.getText());	
					newText.setText("");
				}
			});
			controlPanel.add(addButton);

			JButton removeButton = new JButton(Msg.getString("AuthorityEditor.button.remove"));
			removeButton.setToolTipText(Msg.getString("AuthorityEditor.tooltop.remove"));
			removeButton.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					List<String> selected = list.getSelectedValuesList();
					for (String string : selected) {
						model.removeElement(string);
					}
				}
			});
			controlPanel.add(removeButton);
			
			content.add(controlPanel);
		}
		
		public void loadItems(List<String> newItems) {
			model.removeAllElements();
			model.addAll(newItems);
		}
		
		public List<String> getItems() {
			List<String> items = new ArrayList<>();
			for(int idx = 0; idx < model.getSize(); idx++) {
				items.add(model.get(idx));
			}
			return items;
		}
		
		public JComponent getContent() {
			return content;
		}
	}

	private static final String TITLE = "Authority Editor";

	private ReportingAuthorityFactory raFactory;
	
	private JDialog f;

	private TextList settlementNames;
	private TextList countries;
	private TextList vehicleNames;

	private JComboBox<String> agendaCB;

	private JTextArea ta;

	private JLabel agendaObjective;

	
	/**
	 * Constructor.
	 * 
	 * @param config
	 *            SimulationConfig
	 * @param simulationConfigEditor
	 *            SimulationConfigEditor
	 */
	public AuthorityEditor(SimulationConfigEditor simulationConfigEditor,
					  ReportingAuthorityFactory raFactory) {
		this.raFactory = raFactory;
		
		f = new JDialog(simulationConfigEditor.getFrame(), TITLE, true);
		f.setIconImage(MainWindow.getIconImage());

		// Create main panel.
		JPanel mainPane = new JPanel(new BorderLayout());
		mainPane.setBorder(MainDesktopPane.newEmptyBorder());
		f.getContentPane().add(mainPane);
		
		// Create main panel.
		JPanel contentPane = new JPanel();
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.X_AXIS));
		contentPane.setBorder(MainDesktopPane.newEmptyBorder());
		mainPane.add(contentPane, BorderLayout.CENTER);
				
		// Add Agenda panel
		JPanel agendaPanel = new JPanel();
		agendaPanel.setLayout(new BoxLayout(agendaPanel, BoxLayout.Y_AXIS));
		agendaPanel.setBorder(BorderFactory.createTitledBorder("Agenda"));
		agendaCB = new JComboBox<>();
		agendaCB.setAlignmentX(Component.LEFT_ALIGNMENT);
		agendaCB.setToolTipText(Msg.getString("AuthorityEditor.button.agenda"));
		for (String agenda : raFactory.getAgendaNames()) {
			agendaCB.addItem(agenda);			
		}
		agendaCB.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				String selected = (String) agendaCB.getSelectedItem();
				MissionAgenda selectedAgenda = raFactory.getAgenda(selected);
				agendaObjective.setText(OBJECTIVE + selectedAgenda.getObjectiveName());
				ta.setText(selectedAgenda.getAgendas().stream()
						.map(MissionSubAgenda::getDescription)
						.collect(Collectors.joining(".\n- ", "- ", ".")));
			}
		});
		agendaPanel.add(agendaCB);
		agendaObjective = new JLabel("");
		agendaObjective.setAlignmentX(Component.LEFT_ALIGNMENT);
		agendaPanel.add(agendaObjective);
		
		JLabel goals = new JLabel("Goals:");
		goals.setAlignmentX(Component.LEFT_ALIGNMENT);
		agendaPanel.add(goals);
		
		ta = new JTextArea();
		ta.setAlignmentX(Component.LEFT_ALIGNMENT);
		ta.setEditable(false);
		//ta.setFont(new Font(Font.DIALOG, Font.PLAIN, 10));
		ta.setColumns(20);
		ta.setLineWrap(true);
		ta.setWrapStyleWord(true);
		agendaPanel.add(ta);
		
		contentPane.add(agendaPanel);
		
		// Add List panels
		JTabbedPane tabs = new JTabbedPane();
		countries = new TextList(Msg.getString("AuthorityEditor.country"));
		tabs.addTab(Msg.getString("AuthorityEditor.country"), countries.getContent());
		settlementNames = new TextList(Msg.getString("AuthorityEditor.settlement"));
		tabs.addTab(Msg.getString("AuthorityEditor.settlement"), settlementNames.getContent());
		vehicleNames = new TextList(Msg.getString("AuthorityEditor.vehicle"));
		tabs.addTab(Msg.getString("AuthorityEditor.vehicle"), vehicleNames.getContent());
		contentPane.add(tabs);
		
		// Create button panel.
		UserConfigurableControl<ReportingAuthority> control = new UserConfigurableControl<ReportingAuthority>(f, "Authority",
																		raFactory) {
			@Override
			protected void displayItem(ReportingAuthority newDisplay) {
				loadAuthority(newDisplay);
			}

			@Override
			protected ReportingAuthority createItem(String newName, String newDescription) {
				return commitChanges(newName, newDescription);
			}
		};
		mainPane.add(control.getPane(), BorderLayout.SOUTH);

		
		// Set up the frame to be visible
		f.pack();
		f.setAlwaysOnTop(true);
		f.setLocationRelativeTo(null);
		f.setVisible(true);
	}
	
	public JDialog getJFrame() {
		return f;
	}


	private void loadAuthority(ReportingAuthority newDisplay) {
		f.setTitle(TITLE + " - " + newDisplay.getName());
				
		agendaCB.setSelectedItem(newDisplay.getMissionAgenda().getName());
		countries.loadItems(newDisplay.getCountries());
		settlementNames.loadItems(newDisplay.getSettlementNames());
		vehicleNames.loadItems(newDisplay.getVehicleNames());
	}


	/**
	 * Commits the changes to the crew profiles
	 */
	private ReportingAuthority commitChanges(String name, String description) {
		String agendaName = (String) agendaCB.getSelectedItem();
		ReportingAuthority newRA = new ReportingAuthority(name, description, false,
														  raFactory.getAgenda(agendaName),
														  countries.getItems(),
														  settlementNames.getItems(),
														  vehicleNames.getItems());		
		return newRA;
	}
}

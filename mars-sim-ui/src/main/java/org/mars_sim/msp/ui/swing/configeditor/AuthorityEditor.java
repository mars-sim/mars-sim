/**
 * Mars Simulation Project
 * AuthorityEditor.java
 * @version 3.2.2 2021-08-27
 * @author Barry Evans
 */
package org.mars_sim.msp.ui.swing.configeditor;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import org.mars_sim.msp.core.reportingAuthority.MissionAgenda;
import org.mars_sim.msp.core.reportingAuthority.MissionSubAgenda;
import org.mars_sim.msp.core.reportingAuthority.ReportingAuthority;
import org.mars_sim.msp.core.reportingAuthority.ReportingAuthorityFactory;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MainWindow;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;

import com.alee.laf.text.WebTextArea;
import com.alee.laf.window.WebDialog;

/**
 * UI Editor that allows ReportingAuthorities to be edited
 */
public class AuthorityEditor  {

	class TextList {
		private JPanel content;
		private DefaultListModel<String> model;
		
		TextList(String title) {
			content = new JPanel();
			content.setBorder(BorderFactory.createTitledBorder(title));
			content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

			
			model = new DefaultListModel<String>();
			JList<String> list = new JList<>(model);
			content.add(new JScrollPane(list));
			
			JTextField newText = new JTextField();
			content.add(newText);
			
			JPanel controlPanel = new JPanel();
			JButton addButton = new JButton("Add");
			addButton.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					model.addElement(newText.getText());		
				}
			});
			controlPanel.add(addButton);

			JButton removeButton = new JButton("Remove");
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
		
		public JPanel getContent() {
			return content;
		}
	}

	private static final String TITLE = "Authority Editor";

	private ReportingAuthorityFactory raFactory;
	
	private WebDialog<?> f;

	private TextList settlementNames;
	private TextList countries;
	private TextList vehicleNames;

	private JComboBox<String> agendaCB;

	private WebTextArea ta;

	
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
		
		f = new WebDialog(simulationConfigEditor.getFrame(), TITLE, true);
		f.setIconImage(MainWindow.getIconImage());
		f.setResizable(false);

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
		agendaCB.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				String selected = (String) agendaCB.getSelectedItem();
				MissionAgenda selectedAgenda = raFactory.getAgenda(selected);
				ta.clear();
				ta.append(selectedAgenda.getAgendas().stream()
						.map(MissionSubAgenda::getDescription)
						.collect(Collectors.joining("\n")));
			}
		});
		for (String agenda : raFactory.getAgendaNames()) {
			agendaCB.addItem(agenda);			
		}
		agendaPanel.add(agendaCB);
		agendaPanel.add(new JLabel("Sub Agendas"));
		ta = new WebTextArea();
		ta.setEditable(false);
		ta.setFont(new Font(Font.DIALOG, Font.PLAIN, 10));
		ta.setColumns(7);
		ta.setLineWrap(true);
		agendaPanel.add(ta);

		contentPane.add(agendaPanel);
		
		// Add List panels
		countries = new TextList("Countries");
		contentPane.add(countries.getContent());
		settlementNames = new TextList("Settlements");
		contentPane.add(settlementNames.getContent());
		vehicleNames = new TextList("Vehicle Names");
		contentPane.add(vehicleNames.getContent());
		
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
	
	public WebDialog getJFrame() {
		return f;
	}


	private void loadAuthority(ReportingAuthority newDisplay) {
		f.setTitle(TITLE + " - " + newDisplay.getName());
				
		agendaCB.setSelectedItem(newDisplay.getMissionAgenda().getObjectiveName());
		countries.loadItems(newDisplay.getCountries());
		settlementNames.loadItems(newDisplay.getSettlementNames());
		vehicleNames.loadItems(newDisplay.getVehicleNames());
	}


	/**
	 * Commits the changes to the crew profiles
	 */
	private ReportingAuthority commitChanges(String name, String description) {
		String agendaName = (String) agendaCB.getSelectedItem();
		ReportingAuthority newRA = new ReportingAuthority(name, description,
														  raFactory.getAgenda(agendaName),
														  countries.getItems(),
														  settlementNames.getItems(),
														  vehicleNames.getItems());		
		return newRA;
	}
}

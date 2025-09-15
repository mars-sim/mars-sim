/*
 * Mars Simulation Project
 * AuthorityEditor.java
 * @date 2023-05-31
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.configeditor;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.util.ArrayList;
import java.util.Hashtable;
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
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.mars_sim.core.authority.Authority;
import com.mars_sim.core.authority.AuthorityFactory;
import com.mars_sim.core.authority.MissionAgenda;
import com.mars_sim.core.authority.MissionCapability;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.MainWindow;
import com.mars_sim.ui.swing.StyleManager;

/**
 * UI Editor that allows a reporting authority to be edited.
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
			addButton.addActionListener(e -> {
					model.addElement(newText.getText());	
					newText.setText("");
			});
			controlPanel.add(addButton);

			JButton removeButton = new JButton(Msg.getString("AuthorityEditor.button.remove"));
			removeButton.setToolTipText(Msg.getString("AuthorityEditor.tooltop.remove"));
			removeButton.addActionListener(e ->  {
					List<String> selected = list.getSelectedValuesList();
					for (String string : selected) {
						model.removeElement(string);
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

	private boolean isCorporation = false;

	private AuthorityFactory raFactory;
	
	private JDialog f;

	private TextList settlementNames;
	private TextList countries;
	private TextList vehicleNames;

	private JComboBox<String> agendaCB;

	private JTextArea ta;

	private JLabel agendaObjective;

	private JSlider genderRatio;

	
	/**
	 * Constructor.
	 * 
	 * @param simulationConfigEditor
	 * @param raFactory
	 */
	public AuthorityEditor(SimulationConfigEditor simulationConfigEditor,
					  AuthorityFactory raFactory) {
		
		this.raFactory = raFactory;
		
		f = new JDialog(simulationConfigEditor.getFrame(), TITLE, true);
		f.setIconImage(MainWindow.getIconImage());

		// Create main panel.
		JPanel mainPane = new JPanel(new BorderLayout());
		mainPane.setBorder(StyleManager.newEmptyBorder());
		f.getContentPane().add(mainPane);
		
		// Create main panel.
		JPanel contentPane = new JPanel();
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.X_AXIS));
		contentPane.setBorder(StyleManager.newEmptyBorder());
		mainPane.add(contentPane, BorderLayout.CENTER);
				
		JPanel leftPanel = new JPanel(new BorderLayout());

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
		agendaCB.addActionListener(e ->  {
				String selected = (String) agendaCB.getSelectedItem();
				MissionAgenda selectedAgenda = raFactory.getAgenda(selected);
				agendaObjective.setText(OBJECTIVE + selectedAgenda.getObjectiveName());
				ta.setText(selectedAgenda.getCapabilities().stream()
						.map(MissionCapability::getDescription)
						.collect(Collectors.joining(".\n- ", "- ", ".")));
		});
		agendaPanel.add(agendaCB);
		agendaObjective = new JLabel("");
		agendaObjective.setAlignmentX(Component.LEFT_ALIGNMENT);
		agendaPanel.add(agendaObjective);
		
		JLabel cap = new JLabel("Mission Capabilities:");
		cap.setAlignmentX(Component.LEFT_ALIGNMENT);
		agendaPanel.add(cap);
		
		ta = new JTextArea();
		ta.setAlignmentX(Component.LEFT_ALIGNMENT);
		ta.setEditable(false);
		ta.setColumns(20);
		ta.setLineWrap(true);
		ta.setWrapStyleWord(true);
		agendaPanel.add(ta);
		
		leftPanel.add(agendaPanel, BorderLayout.CENTER);

		// Add gender panel
		Box genderPanel = Box.createHorizontalBox();
		genderPanel.setBorder(BorderFactory.createTitledBorder("Details"));
		genderRatio = new JSlider(JSlider.HORIZONTAL,0, 100, 50);

		//Turn on labels at major tick marks.
		genderRatio.setMajorTickSpacing(50);
		genderRatio.setMinorTickSpacing(5);
		genderRatio.setPaintTicks(true);
		genderRatio.setPaintLabels(true);
		Hashtable<Integer,JComponent> labelTable = new Hashtable<>();
		labelTable.put(0, new JLabel("All Female") );
		labelTable.put(50, new JLabel("Even") );
		labelTable.put(100, new JLabel("All Male") );
		genderRatio.setLabelTable( labelTable );
		genderPanel.add(new JLabel("Male to Female Ratio"));
		genderPanel.add(genderRatio);

		leftPanel.add(genderPanel, BorderLayout.SOUTH);
		contentPane.add(leftPanel);

		
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
		UserConfigurableControl<Authority> control = new UserConfigurableControl<>(f, "Authority",
																		raFactory) {
			@Override
			protected void displayItem(Authority newDisplay) {
				loadAuthority(newDisplay);
			}

			@Override
			protected Authority createItem(String newName, String newDescription) {
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


	private void loadAuthority(Authority newDisplay) {
		f.setTitle(TITLE + " - " + newDisplay.getName());

		genderRatio.setValue((int)(newDisplay.getGenderRatio() * 100));
		agendaCB.setSelectedItem(newDisplay.getMissionAgenda().getName());
		countries.loadItems(newDisplay.getCountries());
		settlementNames.loadItems(newDisplay.getSettlementNames());
		vehicleNames.loadItems(newDisplay.getVehicleNames());
		
		// Load the corporation value
		// Note: this comes as the default and cannot be changed
		isCorporation = newDisplay.isCorporation();
	}


	/**
	 * Commits the changes to the crew profiles.
	 *
	 * @param shortName
	 * @param description
	 * @return
	 */
	private Authority commitChanges(String shortName, String description) {
		String agendaName = (String) agendaCB.getSelectedItem();
		
		return new Authority(shortName, description, isCorporation, false,
				// Note: the following entries may be subject to change
				genderRatio.getValue()/100D,
				raFactory.getAgenda(agendaName),
				countries.getItems(),
				settlementNames.getItems(),
				vehicleNames.getItems());
	}
}

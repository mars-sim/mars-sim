/**
 * Mars Simulation Project
 * CrewEditor.java
 * @version 3.1.0 2016-10-27
 * @author Manny Kung
 */
package org.mars_sim.msp.ui.swing.configeditor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.person.CrewConfig;
import org.mars_sim.msp.core.person.GenderType;
import org.mars_sim.msp.core.person.ai.job.JobType;
import org.mars_sim.msp.core.reportingAuthority.ReportingAuthorityType;
import org.mars_sim.msp.core.tool.Conversion;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.ui.swing.JComboBoxMW;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MainWindow;

/**
 * CrewEditor allows users to design the crew manifest for an initial settlement
 */
public class CrewEditor implements ActionListener {

	public static final String TITLE = "Alpha Crew Editor";


	public static final int ALPHA_CREW = 0;

	private static final String POLITICIAN = "Politician";
	
	// Data members

	private List<JTextField> nameTF = new ArrayList<JTextField>();

	private int crewNum = 0;

	private boolean[][] personalityArray;

//	private DefaultComboBoxModel<String> jobsComboBoxModel;
	private List<JComboBoxMW<String>> jobsComboBoxList = new ArrayList<JComboBoxMW<String>>(JobType.JOB_TYPES.length);

//	private DefaultComboBoxModel<String> countriesComboBoxModel;
	private List<JComboBoxMW<String>> countriesComboBoxList = new ArrayList<JComboBoxMW<String>>(SimulationConfig.instance().getPersonConfig().createAllCountryList().size());

//	private DefaultComboBoxModel<String> sponsorsComboBoxModel;
	private List<JComboBoxMW<String>> sponsorsComboBoxList = new ArrayList<JComboBoxMW<String>>(2);

//	private DefaultComboBoxModel<String> genderComboBoxModel;
	private List<JComboBoxMW<String>> genderComboBoxList = new ArrayList<JComboBoxMW<String>>(2);
	
	private List<MyItemListener> actionListeners = new ArrayList<>(4);
	
	private CrewConfig cc;
	
	private SimulationConfigEditor simulationConfigEditor;

	private JFrame f;

	private JPanel mainPane;
	private JPanel listPane;
	private JPanel radioPane;
	private JPanel ppane;
	private JPanel qpane;

	/**
	 * Constructor.
	 * 
	 * @param config
	 *            SimulationConfig
	 * @param simulationConfigEditor
	 *            SimulationConfigEditor
	 */
	public CrewEditor(SimulationConfigEditor simulationConfigEditor) {

		cc = SimulationConfig.instance().getCrewConfig();
		
		this.simulationConfigEditor = simulationConfigEditor;

		crewNum = cc.getNumberOfConfiguredPeople();
		personalityArray = new boolean[4][crewNum];

		createGUI();
	}


	/**
	 * Creates the GUI
	 */
	public void createGUI() {

		simulationConfigEditor.setCrewEditorOpen(true);

		f = new JFrame(TITLE);
		
		ImageIcon icon = new ImageIcon(CrewEditor.class.getResource(MainWindow.ICON_IMAGE));
		f.setIconImage(MainWindow.iconToImage(icon));
		
		// f.setSize(600, 300);
		f.setSize(new Dimension(600, 500));

		// Create main panel.
		mainPane = new JPanel(new BorderLayout());
		mainPane.setBorder(MainDesktopPane.newEmptyBorder());
		f.setContentPane(mainPane);

		// Create list panel.
		listPane = new JPanel(new GridLayout(6, crewNum + 1));
		mainPane.add(listPane, BorderLayout.NORTH);

		// Create radio panel.
		radioPane = new JPanel(new GridLayout(1, crewNum + 1));
		mainPane.add(radioPane, BorderLayout.CENTER);

		listPane.add(new JLabel(""));
		for (int i = 0; i < crewNum; i++) {
			String num = i + 1 + "";
			listPane.add(new JLabel("Slot " + num));
		}

		listPane.add(new JLabel("Name :   ", JLabel.RIGHT));
		setUpCrewName();

		listPane.add(new JLabel("Gender :   ", JLabel.RIGHT));
		setUpCrewGender();

		radioPane.add(new JLabel("M.B.T.I. :   ", JLabel.RIGHT));
		setUpCrewPersonality();
		
		listPane.add(new JLabel("Job :   ", JLabel.RIGHT));
		setUpCrewJob();

		listPane.add(new JLabel("Country :   ", JLabel.RIGHT));
		setUpCrewCountry();

		listPane.add(new JLabel("Sponsor :   ", JLabel.RIGHT));
		setUpCrewSponsor();
		

		// Create button panel.
		JPanel buttonPane = new JPanel(new FlowLayout(FlowLayout.CENTER));
		mainPane.add(buttonPane, BorderLayout.SOUTH);

		// Create commit button.
		JButton commitButton = new JButton("Commit Changes");

		commitButton.addActionListener(this);

		buttonPane.add(commitButton);

		f.pack();
		f.setLocationRelativeTo(null);
		f.setVisible(true);
		f.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		f.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent ev) {
				simulationConfigEditor.setCrewEditorOpen(false);
				f.dispose();
			}
		});
		
		
		// Manually trigger the country selection again so as to correctly 
		// set up the sponsor combobox at the start of the crew editor
		for (int i = 0; i < crewNum; i++) {
			final JComboBoxMW<String> g = countriesComboBoxList.get(i);
//			g.addActionListener(new ActionListener() {
//				public void actionPerformed(ActionEvent e1) {
					String s = (String) g.getSelectedItem();
					
					int max = g.getItemCount();
					int index = g.getSelectedIndex();
					
					if (max > 1) {
						int num = getRandom(max, index);
//						System.out.println("num : " + num);
						String c = (String)g.getItemAt(num);
						// Fictitiously select a num (other than the index)
						if (c != null && !c.isBlank())
							g.setSelectedIndex(num);
						// Then choose the one already chosen
						// Note: This should force the sponsor to be chosen correction
						g.setSelectedItem(s);
					}
					
					else
						g.setSelectedItem(s);
//				}
//			});
		}
	}

	private int getRandom(int max, int index) {
		int num = RandomUtil.getRandomInt(max);
		if (num != index)
			return num;
		else
			return getRandom(max, index);
	}
	
	public JFrame getJFrame() {
		return f;
	}


	public void actionPerformed(ActionEvent evt) {

		String cmd = (String) evt.getActionCommand();
		// System.out.println("cmd is " + cmd);
		if (!evt.getActionCommand().equals("Commit Changes")) {

			for (int i = 0; i < 4; i++) {
				for (int j = 0; j < crewNum; j++) {
					if (cmd.equals("a" + i + j)) {
						personalityArray[i][j] = true;

					} else if (cmd.equals("b" + i + j)) {
						personalityArray[i][j] = false;

					}
				}
			}
		}

		else {

			boolean goodToGo = true;
			
			for (int i = 0; i < crewNum; i++) {
	
				if (checkNameFields(i, goodToGo)) {
					String genderStr = (String) genderComboBoxList.get(i).getSelectedItem();
					if (genderStr.equals("M"))
						genderStr = "MALE";
					else if (genderStr.equals("F"))
						genderStr = "FEMALE";
					cc.setPersonGender(i, genderStr, ALPHA_CREW);
					System.out.print(genderStr + ", ");
					
					String personalityStr = getPersonality(i);
					cc.setPersonPersonality(i, personalityStr, ALPHA_CREW);
					System.out.print(personalityStr + ", ");
					
					String jobStr = (String) jobsComboBoxList.get(i).getSelectedItem();
					cc.setPersonJob(i, jobStr, ALPHA_CREW);
					System.out.print(jobStr + ", ");

					String countryStr = (String) countriesComboBoxList.get(i).getSelectedItem();
					cc.setPersonCountry(i, countryStr, ALPHA_CREW);
					System.out.print(countryStr + ", ");
					
					String sponsorStr = (String) sponsorsComboBoxList.get(i).getSelectedItem();
					cc.setPersonSponsor(i, sponsorStr, ALPHA_CREW);
					System.out.print(sponsorStr + ", ");
					
					String maindish = cc.getFavoriteMainDish(i, ALPHA_CREW);
					cc.setMainDish(i, maindish, ALPHA_CREW);
					System.out.print(maindish + ", ");
					
					String sidedish = cc.getFavoriteMainDish(i, ALPHA_CREW);
					cc.setSideDish(i, sidedish, ALPHA_CREW);
					System.out.print(sidedish + ", ");
					
					String dessert = cc.getFavoriteDessert(i, ALPHA_CREW);
					cc.setDessert(i, dessert, ALPHA_CREW);
					System.out.print(dessert + ", ");
					
					String activity = cc.getFavoriteActivity(i, ALPHA_CREW);
					cc.setActivity(i, activity, ALPHA_CREW);
					System.out.print(activity + ", ");
					
					String destinationStr = cc.getConfiguredPersonDestination(i, ALPHA_CREW);
					cc.setPersonDestination(i, destinationStr, ALPHA_CREW);
					System.out.println(destinationStr + ". ");
				}
				
				else {
					goodToGo = false;
					break;
				}

			}

			if (goodToGo) {
				simulationConfigEditor.setCrewEditorOpen(false);
				f.setVisible(false);
				f.dispose();
			}
		}
	}

	
	public boolean checkNameFields(int i, boolean goodToGo) {
		
//		String destinationStr = (String) destinationCB.getValue();
//		destinationName = destinationStr;
			
			// Name
			String nameStr = nameTF.get(i).getText().trim();
			// Added isBlank() and checking against invalid names
			if (!Conversion.isBlank(nameStr)
					&& nameStr.contains(" ")) {
				System.out.print(nameStr + ", ");
				cc.setPersonName(i, nameStr, ALPHA_CREW);
				return true;
				
			} else {
				JDialog.setDefaultLookAndFeelDecorated(true);
				JOptionPane.showMessageDialog(f, 
								"Settler's name must include first and last name, separated by a whitespace",
								"Invalid Name Field",
								JOptionPane.ERROR_MESSAGE);
				
				// Disable Start;
				// event.consume();
				nameTF.get(i).requestFocus();
				return false;
			}
	}
	
	public void setUpCrewName() {
		for (int i = 0; i < crewNum; i++) {
			int crew_id = cc.getCrew(i);
			String n = cc.getConfiguredPersonName(i, ALPHA_CREW);

			JTextField tf = new JTextField();
			nameTF.add(tf);
			listPane.add(tf);
			tf.setText(n);
		}
	}

	public DefaultComboBoxModel<String> setUpGenderCBModel() {

		List<String> genderList = new ArrayList<String>(2);
		genderList.add("M");
		genderList.add("F");
		DefaultComboBoxModel<String> m = new DefaultComboBoxModel<String>();

		Iterator<String> i = genderList.iterator();
		while (i.hasNext()) {
			String s = i.next();
			m.addElement(s);
		}

		return m;
	}

	public JComboBoxMW<String> setUpCB(int choice, String s) {
		DefaultComboBoxModel<String> m = null;
		if (choice == 0)
			m = setUpGenderCBModel();

		else if (choice == 2)
			m = setUpJobCBModel();
		
		else if (choice == 3) 
			m = setUpCountryCBModel();
		
		else if (choice == 4) 
			m = setUpSponsorCBModel(s);

		final JComboBoxMW<String> g = new JComboBoxMW<String>(m);
		g.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e1) {
				String s = (String) g.getSelectedItem();

				g.setSelectedItem(s);
			}
		});

		return g;
	}

	public void setUpCrewGender() {

		String s[] = new String[crewNum];
		for (int j = 0; j < crewNum; j++) {
			GenderType n = cc.getConfiguredPersonGender(j, ALPHA_CREW);

			s[j] = n.toString();
			if (s[j].equals("MALE"))
				s[j] = "M";
			else
				s[j] = "F";

			JComboBoxMW<String> g = setUpCB(0, ""); // 0 = Gender
			g.setMaximumRowCount(2);
			listPane.add(g);
			genderComboBoxList.add(g);
			g.setSelectedItem(s[j]);

		}
	}


	/**
	 * Set up personality radio buttons
	 * 
	 * @param col
	 */
	public void setUpCrewPersonality() {
		for (int col = 0; col < crewNum; col++) {
			ppane = new JPanel(new GridLayout(4, 1));

			String quadrant1A = "Extravert", quadrant1B = "Introvert";
			String quadrant2A = "Intuition", quadrant2B = "Sensing";
			String quadrant3A = "Feeling", quadrant3B = "Thinking";
			String quadrant4A = "Judging", quadrant4B = "Perceiving";
			String cat1 = "World", cat2 = "Information", cat3 = "Decision", cat4 = "Structure";
			String a = null, b = null, c = null;

			for (int row = 0; row < 4; row++) {
				qpane = new JPanel(new FlowLayout());
				if (row == 0) {
					a = quadrant1A;
					b = quadrant1B;
					c = cat1;
				} else if (row == 1) {
					a = quadrant2A;
					b = quadrant2B;
					c = cat2;
				} else if (row == 2) {
					a = quadrant3A;
					b = quadrant3B;
					c = cat3;
				} else if (row == 3) {
					a = quadrant4A;
					b = quadrant4B;
					c = cat4;
				}

				JRadioButton ra = new JRadioButton(a);
				ra.addActionListener(this);
				ra.setActionCommand("a" + row + col);
				JRadioButton rb = new JRadioButton(b);
				rb.setActionCommand("b" + row + col);
				rb.addActionListener(this);
				
				if (retrieveCrewMBTI(row, col))
					ra.setSelected(true);
				else
					rb.setSelected(true);
				
				ButtonGroup bg1 = new ButtonGroup();
				bg1.add(ra);
				bg1.add(rb);
				qpane.setBorder(BorderFactory.createTitledBorder(c));
				qpane.add(ra);
				qpane.add(rb);
				ppane.add(qpane);
			}
			radioPane.add(ppane);

		}
	}

	public boolean retrieveCrewMBTI(int row, int col) {
				
		if (row == 0)
			return cc.isExtrovert(col);
		else if (row == 1)
			return cc.isIntuitive(col);
		else if (row == 2)
			return cc.isFeeler(col);
		else if (row == 3)
			return cc.isJudger(col);
		else
			return false;
//		return personalityArray[row][col];
		
	}

	public boolean getRandomBoolean() {
	    Random random = new Random();
	    return random.nextBoolean();
	}
	
	public String getPersonality(int col) {
		String type = null;
		boolean value = getRandomBoolean();
		
		for (int row = 0; row < 4; row++) {
			value = personalityArray[row][col];

			switch (row) {
			case 0:
				if (value)
					type = "E";
				else
					type = "I";
				break;
			case 1:
				if (value)
					type += "N";
				else
					type += "S";
				break;
			case 2:
				if (value)
					type += "F";
				else
					type += "T";
				break;
			case 3:
				if (value)
					type += "J";
				else
					type += "P";
				break;
			}
		}

		return type;
	}

	public DefaultComboBoxModel<String> setUpJobCBModel() {

		List<String> jobs = JobType.getList();
				
		jobs.remove(POLITICIAN);

		Collections.sort(jobs);

		DefaultComboBoxModel<String> m = new DefaultComboBoxModel<String>();
		Iterator<String> j = jobs.iterator();

		while (j.hasNext()) {
			String s = j.next();
			m.addElement(s);
		}
		return m;
	}

	public DefaultComboBoxModel<String> setUpCountryCBModel() {

		List<String> countries = UnitManager.getAllCountryList();
			
		//Collections.sort(countries);

		DefaultComboBoxModel<String> m = new DefaultComboBoxModel<String>();
		Iterator<String> j = countries.iterator();

		while (j.hasNext()) {
			String s = j.next();
			m.addElement(s);
		}
		return m;
	}
	
	public DefaultComboBoxModel<String> setUpSponsorCBModel(String country) {

		//List<String> sponsors = UnitManager.getSponsorByCountryID(id);
		List<String> sponsors = new ArrayList<>();

		sponsors.add(ReportingAuthorityType.MARS_SOCIETY_L.getName());
//		// Retrieve the sponsor from the selected country 		
		if (!country.isBlank())
			sponsors.add(UnitManager.mapCountry2Sponsor(country));		
				
		DefaultComboBoxModel<String> m = new DefaultComboBoxModel<String>();
		Iterator<String> j = sponsors.iterator();

		while (j.hasNext()) {
			String s = j.next();
			m.addElement(s);
		}
		return m;
	}
	
	public void setUpCrewJob() {

		for (int i = 0; i < crewNum; i++) {
			String n[] = new String[15];
			n[i] = cc.getConfiguredPersonJob(i, ALPHA_CREW);
			JComboBoxMW<String> g = setUpCB(2, n[i]);// 2 = Job
			g.setMaximumRowCount(8);
			listPane.add(g);
			g.getModel().setSelectedItem(n[i]);
			jobsComboBoxList.add(g);
		}
	}

	public void setUpCrewCountry() {

		for (int i = 0; i < crewNum; i++) {
			String n[] = new String[28];
			n[i] = cc.getConfiguredPersonCountry(i, ALPHA_CREW);
			JComboBoxMW<String> g = setUpCB(3, n[i]); // 3 = Country
			
			g.setMaximumRowCount(8);
			listPane.add(g);
			g.getModel().setSelectedItem(n[i]);
			countriesComboBoxList.add(g);
			
			// Set up and add a item listener to the country combobox
			MyItemListener a = new MyItemListener();
			actionListeners.add(a);
		    g.addItemListener(a);
		}
	}

	public void setUpCrewSponsor() {

		for (int i = 0; i < crewNum; i++) {
			String n[] = new String[10];
			n[i] = cc.getConfiguredPersonSponsor(i, ALPHA_CREW);
			JComboBoxMW<String> g = setUpCB(4, n[i]); // 4 = Sponsor
			g.setMaximumRowCount(8);
			listPane.add(g);
			g.getModel().setSelectedItem(n[i]);
			sponsorsComboBoxList.add(g);
		}
	}

	class MyItemListener implements ItemListener {
		// This method is called only if a new item has been selected.
		public void itemStateChanged(ItemEvent evt) {
			JComboBoxMW<String> cb = (JComboBoxMW<String>) evt.getSource();

			int index = actionListeners.indexOf(this);
			
			Object item = evt.getItem();

			if (evt.getStateChange() == ItemEvent.SELECTED && sponsorsComboBoxList.size() > 0) {
				// Item was just selected

		        JComboBoxMW<String> m = sponsorsComboBoxList.get(index);
		        
				// Get combo box model
		        DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>) m.getModel();
		        
		        // removing old data
		        model.removeAllElements();

	            model.addElement(ReportingAuthorityType.MARS_SOCIETY_L.getName());
	            model.addElement(ReportingAuthorityType.SPACEX_L.getName());
	            
	            
				String countryStr = (String) item;
				
	            if (!countryStr.isBlank()) {
					String sponsorStr = UnitManager.mapCountry2Sponsor(countryStr);            
					model.addElement(sponsorStr);
	            }
		        
			} else if (evt.getStateChange() == ItemEvent.DESELECTED && sponsorsComboBoxList.size() > 0) {
				// Item is no longer selected
				
		        JComboBoxMW<String> m = sponsorsComboBoxList.get(index);
		        
				// Get combo box model
		        DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>) m.getModel();
		        
		        // removing old data
		        model.removeAllElements();
		        
				model.addElement("To be determined");

			}
		}
	}
		  
	/**
	 * Prepare this window for deletion.
	 */
	public void destroy() {
		cc = null;
		simulationConfigEditor.setCrewEditorOpen(false);
		simulationConfigEditor = null;
		f = null;
		mainPane = null;
		listPane = null;
		radioPane = null;
		ppane = null;
		qpane = null;
		nameTF = null;
		sponsorsComboBoxList = null;
		countriesComboBoxList = null;
		jobsComboBoxList = null;
		genderComboBoxList = null;
	}

}
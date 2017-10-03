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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.person.PersonConfig;
import org.mars_sim.msp.core.person.GenderType;
import org.mars_sim.msp.ui.swing.JComboBoxMW;
import org.mars_sim.msp.ui.swing.MainDesktopPane;


/**
 * CrewEditor allows users to design the crew manifest for an initial settlement
 */
public class CrewEditor implements ActionListener {

	public static final String TITLE = "Alpha Crew Editor";

	public static final int SIZE_OF_CREW = PersonConfig.SIZE_OF_CREW;
	
	public static final int ALPHA_CREW = 0;


	// Data members
	private PersonConfig pc;// = SimulationConfig.instance().getPersonConfiguration();
	private SimulationConfigEditor simulationConfigEditor;

	private JFrame f;
	private JPanel mainPane, listPane, radioPane, ppane, qpane ;
	//private SimulationConfig config; // needed in the constructor

	private List<JTextField> nameTF  = new ArrayList<JTextField>();

	//private DefaultComboBoxModel<String> personalityComboBoxModel;
	//private List<JComboBoxMW<String>> personalityComboBoxList = new ArrayList<JComboBoxMW<String>>(16);

	//private List<String> personalityList = new ArrayList<>(SIZE_OF_CREW);

	private boolean[][] personalityArray;// = new boolean [4][SIZE_OF_CREW];

	private DefaultComboBoxModel<String> jobsComboBoxModel;
	private List<JComboBoxMW<String>> jobsComboBoxList = new ArrayList<JComboBoxMW<String>>(15);

	private DefaultComboBoxModel<String> genderComboBoxModel;
	private List<JComboBoxMW<String>> genderComboBoxList = new ArrayList<JComboBoxMW<String>>(2);

	/**
	 * Constructor.
	 * @param config SimulationConfig
	 * @param simulationConfigEditor SimulationConfigEditor
	 */
	public CrewEditor(SimulationConfig config, SimulationConfigEditor simulationConfigEditor) {

		//this.config = config;
		this.pc = config.getPersonConfiguration();
		this.simulationConfigEditor = simulationConfigEditor;

		personalityArray = new boolean [4][SIZE_OF_CREW];

		createGUI();
	}

	// 2015-10-07 Added and revised createGUI()
	public void createGUI() {

		simulationConfigEditor.setCrewEditorOpen(true);

		f = new JFrame(TITLE);
	    //f.setSize(600, 300);
        f.setSize(new Dimension(600, 500));

		// Create main panel.
		mainPane = new JPanel(new BorderLayout());
		mainPane.setBorder(MainDesktopPane.newEmptyBorder());
		f.setContentPane(mainPane);

		// Create list panel.
		listPane = new JPanel(new GridLayout(4, 5));
		mainPane.add(listPane, BorderLayout.NORTH);

		// Create radio panel.
		radioPane = new JPanel(new GridLayout(1, 5));
		mainPane.add(radioPane, BorderLayout.CENTER);

		//JLabel titleLabel = new JLabel("Alpha Crew Manifest", JLabel.CENTER);
		//mainPane.add(titleLabel, BorderLayout.NORTH);

		listPane.add(new JLabel(""));
		for (int i = 0 ; i < SIZE_OF_CREW; i++) {
			String num = i + 1 + "";
			listPane.add(new JLabel("Slot " + num));
		}

		listPane.add(new JLabel("Name : ", JLabel.CENTER));
		setUpCrewName();

		listPane.add(new JLabel("Gender : ", JLabel.CENTER));
		setUpCrewGender();

		listPane.add(new JLabel("Job : ", JLabel.CENTER));
		setUpCrewJob();

		radioPane.add(new JLabel("MBTI : ", JLabel.CENTER));
		for (int col = 0 ; col < SIZE_OF_CREW; col++) {
			setUpCrewPersonality(col);
		}

		// Create button panel.
		JPanel buttonPane = new JPanel(new FlowLayout(FlowLayout.CENTER));
		mainPane.add(buttonPane, BorderLayout.SOUTH);

		// Create commit button.
		JButton commitButton = new JButton("Commit Changes");

		commitButton.addActionListener(this);

		buttonPane.add(commitButton);

		f.pack();

		f.setLocationRelativeTo(null);

		// Set the location of the dialog at the center of the screen.
		//Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		//f.setLocation((screenSize.width - f.getWidth()) / 2, (screenSize.height - f.getHeight()) / 2);

        f.setVisible(true);

        f.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        f.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent ev) {
				simulationConfigEditor.setCrewEditorOpen(false);
            	f.dispose();
            }
        });
	}

	public JFrame getJFrame() {
		return f;
	}

	// 2015-10-07 Revised actionPerformed()
	public void actionPerformed(ActionEvent evt) {

		String cmd = (String) evt.getActionCommand();
		//System.out.println("cmd is " + cmd);
		if (!evt.getActionCommand().equals("Commit Changes")) {

			for (int i = 0 ; i < 4; i++) {
				for (int j = 0 ; j < SIZE_OF_CREW; j++) {
					if (cmd.equals("a"+i+j)) {
						personalityArray [i][j] = true;
						//System.out.println(" value " + i + "," + j + "," + " is " + true);
					}
					else if (cmd.equals("b"+i+j)) {
						personalityArray [i][j] = false;
						//System.out.println(" value " + i + "," + j + "," + " is " + false);
					}
				}
			}
		}

		else {

			for (int i = 0; i< SIZE_OF_CREW; i++) {

				String nameStr = nameTF.get(i).getText();
				//System.out.println(" name is " + nameStr);
				pc.setPersonName(i, nameStr, ALPHA_CREW);

				String genderStr = (String) genderComboBoxList.get(i).getSelectedItem();
				if ( genderStr.equals("M")  )
					genderStr = "MALE";
				else if ( genderStr.equals("F") )
					genderStr = "FEMALE";
				//System.out.println(" gender is " + genderStr);
				pc.setPersonGender(i, genderStr, ALPHA_CREW);

				//String personalityStr = (String) personalityComboBoxList.get(i).getSelectedItem();
				String personalityStr = getPersonality(i);
				//System.out.println(" personality is " + personalityStr);
				pc.setPersonPersonality(i, personalityStr, ALPHA_CREW);

				//String jobStr = jobTF.get(i).getText();
				String jobStr = (String) jobsComboBoxList.get(i).getSelectedItem();
				//System.out.println(" job is " + jobStr);
				pc.setPersonJob(i, jobStr, ALPHA_CREW);

			}

			simulationConfigEditor.setCrewEditorOpen(false);
			f.setVisible(false);
			f.dispose();
		}
	}

	public void setUpCrewName() {
		for (int i = 0 ; i < SIZE_OF_CREW; i++) {
			int crew_id = pc.getCrew(i);
			String n = pc.getConfiguredPersonName(i, ALPHA_CREW);
			//System.out.println(" name is "+ n);
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
		genderComboBoxModel = new DefaultComboBoxModel<String>();

		Iterator<String> i = genderList.iterator();
		while (i.hasNext()) {
			String s = i.next();
			genderComboBoxModel.addElement(s);
		}

		return genderComboBoxModel;
	}


	public JComboBoxMW<String> setUpCB(int choice) {
		DefaultComboBoxModel<String> m = null;
		if (choice == 0)
			 m = setUpGenderCBModel() ;
		//else if (choice == 1)
		//	 m = setUpPersonalityCBModel();
		else if (choice == 2)
			 m = setUpJobCBModel();

		final JComboBoxMW<String> g = new JComboBoxMW<String>(m);
		g.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e1) {
				String s = (String) g.getSelectedItem();
				//System.out.println(" selectedItem is " + s);
	           	g.setSelectedItem(s);
	        }});

		return g;
	}

	public void setUpCrewGender() {

		String s[] = new String[SIZE_OF_CREW];
		for (int j = 0 ; j < SIZE_OF_CREW; j++) {
			GenderType n = pc.getConfiguredPersonGender(j, ALPHA_CREW);
			// convert MALE to M, FEMAL to F
			s[j] = n.toString();
			if (s[j].equals("MALE")) s[j] = "M";
			else s[j] = "F";

			JComboBoxMW<String> g = setUpCB(0); // 0 = Gender
			g.setMaximumRowCount(2);
			listPane.add(g);
			genderComboBoxList.add(g);
			g.setSelectedItem(s[j]);

		}
	}

	// 2015-10-07 Revised setUpCrewPersonality() to use radio buttons instead of combobox
	public void setUpCrewPersonality(int col) {

		ppane = new JPanel(new GridLayout(4,1));

		String quadrant1A = "Extravert", quadrant1B = "Introvert";
		String quadrant2A = "Intuition", quadrant2B = "Sensing";
		String quadrant3A = "Feeling", quadrant3B = "Thinking";
		String quadrant4A = "Judging", quadrant4B = "Perceiving";
		String cat1 = "World", cat2 = "Information", cat3 = "Decision", cat4 = "Structure";
		String a = null, b = null, c = null;

		for (int row = 0 ; row < 4; row++) {
			qpane = new JPanel(new FlowLayout());
			if (row == 0) {
				a = quadrant1A;
				b = quadrant1B;
				c = cat1;
			}
			else if (row == 1) {
				a = quadrant2A;
				b = quadrant2B;
				c = cat2;
			}
			else if (row == 2) {
				a = quadrant3A;
				b = quadrant3B;
				c = cat3;
			}
			else if (row == 3) {
				a = quadrant4A;
				b = quadrant4B;
				c = cat4;
			}

			JRadioButton ra = new JRadioButton(a);
			ra.addActionListener(this);
			ra.setActionCommand("a"+row+col);
			JRadioButton rb = new JRadioButton(b);
			rb.setActionCommand("b"+row+col);
			rb.addActionListener(this);
			if (retrievePersonality(row, col))
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

	// 2015-10-07 Added  retrievePersonality()
	public boolean retrievePersonality(int row, int col) {
		return personalityArray[row][col];
	}

	// 2015-10-07 Added getPersonality()
	public String getPersonality(int col) {
		String type = null;
		boolean value = true;

		for (int row = 0 ; row < 4; row++) {
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
		//System.out.println("For " + col + " type is " + type);
		return type;
	}
/*
	public DefaultComboBoxModel<String> setUpPersonalityCBModel() {
		//String[] items = { "item1", "item2" };
		List<String> personalityTypes = new ArrayList<String>(16);
		personalityTypes.add("ISTP");
		personalityTypes.add("ISTJ");
		personalityTypes.add("ISFP");
		personalityTypes.add("ISFJ");
		personalityTypes.add("INTP");
		personalityTypes.add("INTJ");
		personalityTypes.add("INFP");
		personalityTypes.add("INFJ");
		personalityTypes.add("ESTP");
		personalityTypes.add("ESTJ");
		personalityTypes.add("ESFP");
		personalityTypes.add("ESFJ");
		personalityTypes.add("ENTP");
		personalityTypes.add("ENTJ");
		personalityTypes.add("ENFP");
		personalityTypes.add("ENFJ");
		Collections.sort(personalityTypes);
		personalityComboBoxModel = new DefaultComboBoxModel<String>();
		Iterator<String> i = personalityTypes.iterator();

		while (i.hasNext()) {
			String s = i.next();
	    	personalityComboBoxModel.addElement(s);

		}

		return personalityComboBoxModel;

	}

	public void setUpCrewPersonality() {

		for (int j = 0 ; j < SIZE_OF_CREW; j++) {
			String n[] = new String[16];
			n[j] = pc.getConfiguredPersonPersonalityType(j);

			JComboBoxMW<String> g = setUpCB(1);		 // 1 = Personality
		    g.setMaximumRowCount(8);
			listPane.add(g);
			g.getModel().setSelectedItem(n[j]);
			//g.setSelectedItem(n[j]);
			personalityComboBoxList.add(g);
		}

	}
*/
	public DefaultComboBoxModel<String> setUpJobCBModel() {

		List<String> jobs = new ArrayList<String>(15);
		jobs.add("Botanist");
		jobs.add("Areologist");
		jobs.add("Doctor");
		jobs.add("Engineer");
		jobs.add("Driver");
		jobs.add("Chef");
		jobs.add("Trader");
		jobs.add("Technician");
		jobs.add("Architect");
		jobs.add("Biologist");
		jobs.add("Astronomer");
		jobs.add("Chemist");
		jobs.add("Physicist");
		jobs.add("Mathematician");
		jobs.add("Meteorologist");
		Collections.sort(jobs);
		jobsComboBoxModel = new DefaultComboBoxModel<String>();
		Iterator<String> j = jobs.iterator();

		while (j.hasNext()) {
			String s = j.next();
			jobsComboBoxModel.addElement(s);
		}
		return jobsComboBoxModel;
	}

	public void setUpCrewJob() {

		for (int i = 0 ; i < SIZE_OF_CREW; i++) {
			String n[] = new String[15];
			n[i] = pc.getConfiguredPersonJob(i, ALPHA_CREW);
			JComboBoxMW<String> g = setUpCB(2);		// 2 = Job
		    g.setMaximumRowCount(8);
			listPane.add(g);
			g.getModel().setSelectedItem(n[i]);
			//g.setSelectedItem(n[j]);
			jobsComboBoxList.add(g);
		}
	}


	/**
	 * Prepare this window for deletion.
	 */
	public void destroy() {
		pc = null;
		simulationConfigEditor.setCrewEditorOpen(false);
		simulationConfigEditor = null;
	}


}
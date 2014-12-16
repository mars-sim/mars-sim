/**
 * Mars Simulation Project
 * CrewEditor.java
 * @version 3.07 2014-12-16
 * @author Manny Kung
 */
package org.mars_sim.msp.ui.swing.configeditor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.person.PersonConfig;
import org.mars_sim.msp.core.person.PersonGender;
import org.mars_sim.msp.ui.swing.JComboBoxMW;
import org.mars_sim.msp.ui.swing.MainDesktopPane;


/**
 * Window for the resupply tool.
 * TODO externalize strings
 */
public class CrewEditor
extends JDialog {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** Tool name. */
	public static final String NAME = "Resupply Tool";

	public static final int SIZE_OF_CREW = 4;
	
	// Data members
	private PersonConfig pc;// = SimulationConfig.instance().getPersonConfiguration();
	
	private JPanel mainPane;
	private JPanel listPane ;
	private SimulationConfig config; // needed in the constructor
	
	private List<JTextField> nameTF  = new ArrayList<JTextField>();

	private DefaultComboBoxModel<String> personalityComboBoxModel;
	private List<JComboBoxMW<String>> personalityComboBoxList = new ArrayList<JComboBoxMW<String>>(16);

	private DefaultComboBoxModel<String> jobsComboBoxModel;
	private List<JComboBoxMW<String>> jobsComboBoxList = new ArrayList<JComboBoxMW<String>>(15);

	private DefaultComboBoxModel<String> genderComboBoxModel;
	private List<JComboBoxMW<String>> genderComboBoxList = new ArrayList<JComboBoxMW<String>>(2);

	/**
	 * Constructor.
	 * @param owner Window
	 * @param config SimulationConfig
	 */
	public CrewEditor(Window owner, SimulationConfig config) {
		super(owner, Msg.getString("CrewEditor.title"), ModalityType.APPLICATION_MODAL); //$NON-NLS-1$
		this.config = config;
		pc = config.getPersonConfiguration();		
		
		// Set the location of the dialog at the center of the screen.
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((screenSize.width - getWidth()) / 2, (screenSize.height - getHeight()) / 2);
		
		// Create main panel.
		mainPane = new JPanel(new BorderLayout());
		mainPane.setBorder(MainDesktopPane.newEmptyBorder());
		setContentPane(mainPane);
		
		// Create list panel.
		listPane = new JPanel(new GridLayout(6, 5));
		mainPane.add(listPane, BorderLayout.CENTER);

		JLabel titleLabel = new JLabel("Alpha Crew Manifest", JLabel.CENTER);
		mainPane.add(titleLabel, BorderLayout.NORTH);
		
		JLabel empty = new JLabel("");
		listPane.add(empty);
		
		JLabel slotOne = new JLabel("Slot 1");
		listPane.add(slotOne);

		JLabel slotTwo = new JLabel("Slot 2");
		listPane.add(slotTwo);

		JLabel slotThree = new JLabel("Slot 3");
		listPane.add(slotThree);

		JLabel slotFour = new JLabel("Slot 4");
		listPane.add(slotFour);

		JLabel name = new JLabel("Name :");
		listPane.add(name);

		setUpCrewName();

		
		JLabel gender = new JLabel("Gender :");
		listPane.add(gender);

		setUpCrewGender();
		
		JLabel personality = new JLabel("Personality :");
		listPane.add(personality);

		setUpCrewPersonality();	
		
		JLabel job = new JLabel("Job :");
		listPane.add(job);

		setUpCrewJob();
		
		// Create button panel.
		JPanel buttonPane = new JPanel(new FlowLayout(FlowLayout.CENTER));
		mainPane.add(buttonPane, BorderLayout.SOUTH);

		
		// Create commit button.
		JButton commitButton = new JButton("Commit Changes");
		commitButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {

				for (int i = 0; i< SIZE_OF_CREW; i++) {
					String nameStr = nameTF.get(i).getText();
					//System.out.println(" name is " + nameStr);
					pc.setPersonName(i, nameStr);
					
					String genderStr = (String) genderComboBoxList.get(i).getSelectedItem();
					if ( genderStr.equals("M")  )
						genderStr = "MALE";
					else if ( genderStr.equals("F") )
						genderStr = "FEMALE";
					//System.out.println(" gender is " + genderStr);
					pc.setPersonGender(i, genderStr);
					
					String personalityStr = (String) personalityComboBoxList.get(i).getSelectedItem();
					//System.out.println(" personality is " + personalityStr);
					pc.setPersonPersonality(i, personalityStr);
					
					//String jobStr = jobTF.get(i).getText();
					String jobStr = (String) jobsComboBoxList.get(i).getSelectedItem();
					//System.out.println(" job is " + jobStr);
					pc.setPersonJob(i, jobStr);
				}
					
				dispose();
				setVisible(false);
			}
		});
		buttonPane.add(commitButton);

		pack();
	}


	
	public void setUpCrewName() {
		for (int i = 0 ; i < SIZE_OF_CREW; i++) {
			String n = pc.getConfiguredPersonName(i);
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
		else if (choice == 1)
			 m = setUpPersonalityCBModel();
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
			PersonGender n = pc.getConfiguredPersonGender(j);
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

/*
		DefaultComboBoxModel<String> m2 = setGenderCBModel() ;

		final JComboBoxMW<String> g2 = new JComboBoxMW<String>(m2);
		g2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e2) {
	         	String s2 = (String) g2.getSelectedItem();
	           	g2.setSelectedItem(s2);
	        }});  
		g2.setMaximumRowCount(2);
		g2.getModel().setSelectedItem(s[1]);		
		listPane.add(g2);
		genderComboBoxList.add(g2);			

		DefaultComboBoxModel<String> m3 = setGenderCBModel() ;

		final JComboBoxMW<String> g3 = new JComboBoxMW<String>(m3);
		g3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e3) {
	         	String s3 = (String) g3.getSelectedItem();
	           	g3.setSelectedItem(s3);
	        }});  
		g3.setMaximumRowCount(2);
		g3.getModel().setSelectedItem(s[2]);		
		listPane.add(g3);
		genderComboBoxList.add(g3);	
		
		DefaultComboBoxModel<String> m4 = setGenderCBModel() ;

			
		final JComboBoxMW<String> g4 = new JComboBoxMW<String>(m4);
		g4.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e4) {
	         	String s4 = (String) g4.getSelectedItem();
	           	g4.setSelectedItem(s4);
	        }});  
		g4.setMaximumRowCount(2);
		g4.getModel().setSelectedItem(s[3]);		
		listPane.add(g4);
		genderComboBoxList.add(g4);	
				
	}

	
	public JComboBoxMW<String> setUpPersonalityCB() {
		
		DefaultComboBoxModel<String> m = setUpPersonalityCBModel() ;

		final JComboBoxMW<String> g = new JComboBoxMW<String>(m);
		g.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e1) {
				//JComboBoxMW<String> c1 = (JComboBoxMW<String>)e1.getSource();
				String s = (String) g.getSelectedItem();
				//System.out.println(" s is " + s);
				//String s1 = (String) g1.getSelectedItem();
	           	g.setSelectedItem(s);
	        }});  	
		return g;
		
	}
	*/
	
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
/*
	class MyActionListener implements ActionListener {
		  //Object oldItem;
		  String oldItem;
		  public void actionPerformed(ActionEvent evt) {
				JComboBoxMW<String> pCB  = (JComboBoxMW<String>) evt.getSource();
		    String newItem = (String) pCB.getSelectedItem();
			//System.out.println(" Personality is "+ newItem);

		    pCB.setSelectedItem(newItem);
		    
		    boolean same = newItem.equals(oldItem);
		    oldItem = newItem;

		  }
	}
		  
	public void makeComboBox(int j) {
		String n = pc.getConfiguredPersonPersonalityType(j);
		System.out.println(" Personality is "+ n);
		//JTextField tf = new JTextField();
		//personalityTF.add(tf);
		final JComboBoxMW<String> pCB = new JComboBoxMW<String>(personalityComboBoxModel);

		pCB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	String s = (String) pCB.getSelectedItem();
            	//sList.add(s);
            	pCB.setSelectedItem(s);
            }
            });  
		pCB.setMaximumRowCount(8);
		listPane.add(pCB);
		pCB.getModel().setSelectedItem(n);
		personalityComboBoxList.add(pCB);
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
			
			n[i] = pc.getConfiguredPersonJob(i);
			
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
		config = null;
	}


}
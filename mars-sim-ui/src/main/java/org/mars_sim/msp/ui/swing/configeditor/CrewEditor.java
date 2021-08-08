/**
 * Mars Simulation Project
 * CrewEditor.java
 * @version 3.2.0 2021-06-20
 * @author Manny Kung
 */
package org.mars_sim.msp.ui.swing.configeditor;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;

import org.mars_sim.msp.core.person.Crew;
import org.mars_sim.msp.core.person.CrewConfig;
import org.mars_sim.msp.core.person.GenderType;
import org.mars_sim.msp.core.person.Member;
import org.mars_sim.msp.core.person.ai.job.JobType;
import org.mars_sim.msp.core.reportingAuthority.ReportingAuthority;
import org.mars_sim.msp.core.reportingAuthority.ReportingAuthorityFactory;
import org.mars_sim.msp.core.tool.Conversion;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MainWindow;

import com.alee.api.annotations.NotNull;
import com.alee.api.annotations.Nullable;
import com.alee.api.data.BoxOrientation;
import com.alee.extended.button.WebSwitch;
import com.alee.extended.overlay.AlignedOverlay;
import com.alee.extended.overlay.WebOverlay;
import com.alee.extended.svg.SvgIcon;
import com.alee.laf.combobox.WebComboBox;
import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;
import com.alee.laf.radiobutton.WebRadioButton;
import com.alee.laf.text.WebTextField;
import com.alee.laf.window.WebDialog;
import com.alee.managers.icon.IconManager;
import com.alee.managers.style.StyleId;
import com.alee.managers.tooltip.TooltipManager;
import com.alee.managers.tooltip.TooltipWay;
import com.alee.utils.CoreSwingUtils;
import com.alee.utils.swing.extensions.DocumentEventRunnable;

/**
 * CrewEditor allows users to design the crew manifest for an initial settlement
 */
public class CrewEditor implements ActionListener {
		
	/** default logger. */
	private static final Logger logger = Logger.getLogger(CrewEditor.class.getName());

	public static final String TITLE = "Crew Editor";
	private static final String SELECT_SPONSOR = "Select Sponsor";


	private static final String SAVE_BETA = "Save as Beta Crew";
	private static final String COMMIT = "Commit Changes";
	private static final String LOAD_ALPHA = "Load Alpha Crew";
	private static final String LOAD_BETA = "Load Beta Crew";
				
	public static final int ALPHA_CREW_ID = 0;
	public static final int BETA_CREW_ID = 1;
	public static final int PANEL_WIDTH = 180;
	public static final int WIDTH = (int)(PANEL_WIDTH * 3.5);
	public static final int HEIGHT = 512;
	
	// Data members
	private int crewNum = 0;

	private List<JTextField> nameTFs = new ArrayList<JTextField>();

	private List<JTextField> ageTFs = new ArrayList<JTextField>();
	
	private List<List<JRadioButton>> allRadioButtons = new ArrayList<>();

	private List<WebComboBox> jobsComboBoxList = new ArrayList<WebComboBox>();

	private List<WebComboBox> countriesComboBoxList = new ArrayList<WebComboBox>();

	private List<WebComboBox> sponsorsComboBoxList = new ArrayList<WebComboBox>();
	
	private List<WebSwitch> webSwitches = new ArrayList<>();
	
	private List<SponsorListener> actionListeners = new ArrayList<>();
	
	private WebDialog<?> f;
	
	private JPanel mainPane;
	private JPanel scrollPane;
	
	private List<Box> crewPanels = new ArrayList<>();
	
	private CrewConfig crewConfig;

	private SimulationConfigEditor simulationConfigEditor;

	private Crew crew;


	/**
	 * Constructor.
	 * 
	 * @param config
	 *            SimulationConfig
	 * @param simulationConfigEditor
	 *            SimulationConfigEditor
	 */
	public CrewEditor(SimulationConfigEditor simulationConfigEditor, CrewConfig config) {
		
		this.simulationConfigEditor = simulationConfigEditor;
		this.crewConfig = config;
		
		// Start with Alpha
		this.crew = config.loadCrew(CrewConfig.ALPHA_NAME);
		crewNum = crew.getNumberOfConfiguredPeople();
		
		createGUI();
	}


	/**
	 * Create a crew panel to house the attributes of a crewman
	 * 
	 * @return
	 */
	private Box createCrewPanel() {
		// Create attribute panel.
//		JPanel crewPanel = new JPanel();
		Box crewPanel = Box.createVerticalBox();
//		crewPanel.setLayout(new BoxLayout(crewPanel, BoxLayout.Y_AXIS));
//		crewPanel.setPreferredSize(new Dimension(CREW_WIDTH, HEIGHT));
		crewPanels.add(crewPanel);
		return crewPanel;
	}
	
	/**
	 * Creates the GUI
	 */
	private void createGUI() {
	
		f = new WebDialog(simulationConfigEditor.getFrame(), TITLE + " - Alpha Crew On-board", true); //new JFrame(TITLE + " - Alpha Crew On-board");
		f.setIconImage(MainWindow.getIconImage());
//		f.setPreferredSize(new Dimension(WIDTH, HEIGHT));
		f.setResizable(false);
		f.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		f.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent ev) {
				simulationConfigEditor.setCrewEditorOpen(false);
				f.dispose();
				destroy();
			}
		});

		// Create main panel.
		mainPane = new JPanel(new BorderLayout());
		mainPane.setPreferredSize(new Dimension(WIDTH, HEIGHT));
		mainPane.setBorder(MainDesktopPane.newEmptyBorder());
//		f.setContentPane(mainPane);
		f.getContentPane().add(mainPane);
		
		// Create main panel.
		scrollPane = new JPanel();
		scrollPane.setLayout(new BoxLayout(scrollPane, BoxLayout.X_AXIS));
//		scrollPane.setPreferredSize(new Dimension(WIDTH, HEIGHT));
//		scrollPane.setAlignmentX(LEFT_ALIGNMENT);
		scrollPane.setBorder(MainDesktopPane.newEmptyBorder());

		// Prepare scroll panel.
		JScrollPane scrollPanel = new JScrollPane(scrollPane);
//		scrollPanel.setPreferredSize(new Dimension(WIDTH, HEIGHT));
//		scrollPanel.getVerticalScrollBar().setUnitIncrement(CREW_WIDTH);
//		scrollPanel.getHorizontalScrollBar().setUnitIncrement(CREW_WIDTH * 2);
		mainPane.add(scrollPanel, BorderLayout.CENTER);
		
		for (int i=0; i<crewNum; i++) {
			Box crewPanel = createCrewPanel();
			// Add the Crewman title border
			String num = i + 1 + "";
			crewPanel.setBorder(BorderFactory.createTitledBorder("Crewman " + num));
			scrollPane.add(crewPanel);
		}
		
		// Create attribute header panel.	
//		attributeHeader = new JPanel(new GridLayout(8, 1));
//		mainPane.add(attributeHeader, BorderLayout.WEST);
	
//		attributeHeader.add(new JLabel("Name :   ", JLabel.RIGHT));
		// Add the name textfields
		setUpCrewName();

//		attributeHeader.add(new JLabel("Gender :   ", JLabel.RIGHT));
		// Add the gender combobox options
		setUpCrewGender();

		// Add the age textfields
		setUpCrewAge();
		
		// Add the personality traits checkboxes 
		setUpCrewPersonality();
		
		// Add the job combobox options
		setUpCrewJob();
		
		// Add the sponsor combobox options
		setUpCrewSponsor();
		
		// Add the country combobox options
		setUpCrewCountry();
	
		// Create button panel.
		JPanel buttonPane = new JPanel(new FlowLayout(FlowLayout.CENTER));
		mainPane.add(buttonPane, BorderLayout.SOUTH);

		// Create load alpha crew button.
		JButton loadAlphaButton = new JButton(LOAD_ALPHA);
		loadAlphaButton.addActionListener(this);
		buttonPane.add(loadAlphaButton);
		
		// Create load beta crew button.
		JButton loadBetaButton = new JButton(LOAD_BETA);
		loadBetaButton.addActionListener(this);
		buttonPane.add(loadBetaButton);		
		
		// Create commit button.
		JButton commitButton = new JButton(COMMIT);
		commitButton.addActionListener(this);
		buttonPane.add(commitButton);

		// Create commit button.
		JButton saveButton = new JButton(SAVE_BETA);
		saveButton.addActionListener(this);
		buttonPane.add(saveButton);
//		saveButton.setEnabled(false);
		
		// Manually trigger the country selection again so as to correctly 
		// set up the sponsor combobox at the start of the crew editor
		for (int i = 0; i < crewNum; i++) {
			final WebComboBox g = countriesComboBoxList.get(i);

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

		// Set up the frame to be visible
		f.pack();
		f.setAlwaysOnTop(true);
		f.setLocationRelativeTo(null);
		f.setVisible(true);
	}

	private int getRandom(int max, int index) {
		int num = RandomUtil.getRandomInt(max);
		if (num != index)
			return num;
		else
			return getRandom(max, index);
	}
	
	public WebDialog getJFrame() {
		return f;
	}


	/**
	 * Takes the actions from the button being clicked on
	 */
	public void actionPerformed(ActionEvent evt) {

		String cmd = (String) evt.getActionCommand();
		
		if (cmd.equals(LOAD_ALPHA)) {

			JDialog.setDefaultLookAndFeelDecorated(true);
			int result = JOptionPane.showConfirmDialog(f, 
							"Are you sure you want to reload the default Alpha Crew? " + System.lineSeparator()
							+ "All the changes made will be lost.",
							"Confirm Reloading Alpha Crew",
							JOptionPane.YES_NO_CANCEL_OPTION);
			
			if (result == JOptionPane.YES_OPTION) {
				designateCrew(CrewConfig.ALPHA_NAME);
			}
		}

		else if (cmd.equals(LOAD_BETA)) {

			JDialog.setDefaultLookAndFeelDecorated(true);
			int result = JOptionPane.showConfirmDialog(f, 
							"Are you sure you want to load the Beta Crew? " + System.lineSeparator()
							+ "All the changes made will be lost.",
							"Confirm Loading Beta Crew",
							JOptionPane.YES_NO_CANCEL_OPTION);
			
			if (result == JOptionPane.YES_OPTION) {
				
				if (!designateCrew(CrewConfig.BETA_NAME)) {
					JDialog.setDefaultLookAndFeelDecorated(true);
					JOptionPane.showMessageDialog(f, 
									"beta_crew.xml does not exist !",
									"File Not Found",
									JOptionPane.ERROR_MESSAGE);
				}
			}
		}
		
		else if (cmd.equals(COMMIT)) {

			if (commitChanges()) {
//				simulationConfigEditor.setCrewEditorOpen(false);
				f.setVisible(false);
				f.dispose();
			}
		}
		
		else if (cmd.equals(SAVE_BETA)) {

			JDialog.setDefaultLookAndFeelDecorated(true);
			int result = JOptionPane.showConfirmDialog(f,
					"Are you sure you want to save the changes to beta_crew.xml ? " 
					+ System.lineSeparator() + System.lineSeparator()
					+ "Note : If you only want the changes to apply to " + System.lineSeparator()
					+ "the simulation you are setting up, choose " + System.lineSeparator()
					+ "'Commit Change' instead." + System.lineSeparator(),
					"Confirm Saving as Beta Crew",
					JOptionPane.YES_NO_CANCEL_OPTION);
			
			if (result == JOptionPane.YES_OPTION) {
				// Save to beta_crew.xml ...
				crew.setName(CrewConfig.BETA_NAME);
				commitChanges();
				crewConfig.save(crew);
				f.setTitle(TITLE + " - " + crew.getName() + " Crew On-board");	
			}
		}
	}

	/**
	 * Commits the changes to the crew profiles
	 */
	private boolean commitChanges() {
		boolean goodToGo = true;
		
		List<Member> members = crew.getTeam();
		for (int i = 0; i < crewNum; i++) {
			// Find member
			Member m = null;
			if (i == members.size()) {
				m = new Member();
				members.add(m);
			}
			else {
				m = members.get(i);
			}
			
			if (!checkNameFields(i, m, goodToGo)) {
				goodToGo = false;
				break;
			}
				
			if (!checkAgeFields(i, m, goodToGo)) {
				goodToGo = false;
				break;
			}
				
			GenderType gender;
			boolean isSelected = webSwitches.get(i).isSelected();
			
			if (isSelected)
				gender = GenderType.MALE;
			else 
				gender = GenderType.FEMALE;

			m.setGender(gender);
			
			m.setMBTI(getSelectedPersonality(i));
	
			String sponsor = (String) sponsorsComboBoxList.get(i).getSelectedItem();
			if (sponsor != null) {
				m.setSponsorCode(sponsor);
			}
			
			m.setCountry((String) countriesComboBoxList.get(i).getSelectedItem());
			m.setJob((String) jobsComboBoxList.get(i).getSelectedItem());
		}
		return goodToGo;
	}
	
	/**
	 * Checks if the name textfields are valid
	 * 
	 * @param i
	 * @param goodToGo
	 * @return
	 */
	private boolean checkNameFields(int i, Member m, boolean goodToGo) {

		String nameStr = nameTFs.get(i).getText().trim();
		// Use isBlank() to check against invalid names
		if (!Conversion.isBlank(nameStr)
				&& nameStr.contains(" ")) {
			m.setName(nameStr);
			return true;
			
		} else {
			JDialog.setDefaultLookAndFeelDecorated(true);
			JOptionPane.showMessageDialog(f, 
							"Each Settler's name must include first and last name, separated by a whitespace",
							"Invalid Name Format",
							JOptionPane.ERROR_MESSAGE);
			
			// Disable Start;
			// event.consume();
			nameTFs.get(i).requestFocus();
			return false;
		}
	}
	
	/**
	 * Checks if the age textfields are valid
	 * 
	 * @param i
	 * @param goodToGo
	 * @return
	 */
	private boolean checkAgeFields(int i, Member m, boolean goodToGo) {
		
		String s = ageTFs.get(i).getText().trim();
		// Use isBlank() to check against invalid names
		if (!Conversion.isBlank(s)
				&& isNumeric(s)) {
			
			int age = Integer.parseInt(s);
			
			if (age < 0 || age > 100) {
				JDialog.setDefaultLookAndFeelDecorated(true);
				JOptionPane.showMessageDialog(f, 
								"A settler's age must be between 0 and 100",
								"Invalid Age Range",
								JOptionPane.ERROR_MESSAGE);
				
				ageTFs.get(i).requestFocus();
				return false;
			}
			
			else {
				m.setAge(s);
				return true;
			}
		} 
		
		else {
			JDialog.setDefaultLookAndFeelDecorated(true);
			JOptionPane.showMessageDialog(f, 
							"A settler's age must be numeric and between 5 and 100",
							"Invalid Age Format",
							JOptionPane.ERROR_MESSAGE);
			
			ageTFs.get(i).requestFocus();
			return false;
		}
	}
	
	/**
	 * Checks if a string is purely numeric
	 * 
	 * @param str
	 * @return
	 */
	private static boolean isNumeric(String str) { 
		try {  
			Integer.parseInt(str);
			return true;
		} catch(NumberFormatException e){  
			return false;  
		}  
	}
	
	/**
	 * Sets up the name textfields
	 */
	private void setUpCrewName() {
    	SvgIcon icon = IconManager.getIcon ("info_red");//new LazyIcon("info").getIcon();
//    	icon.apply(new SvgStroke(Color.ORANGE));
    	
    	int i = 0;
		for(Member m : crew.getTeam()) {

			String n = m.getName();
			WebTextField tf = new WebTextField(15);
			tf.setMargin(3, 0, 3, 0);
			final WebOverlay overlay = new WebOverlay(StyleId.overlay);
			overlay.setContent(tf);
	        final WebLabel overlayLabel = new WebLabel(icon);
			onChange(tf, overlayLabel, overlay);
			WebPanel panel = new WebPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
			panel.add(new WebLabel("   Name : "));
			panel.add(tf);
			nameTFs.add(tf);
			crewPanels.get(i++).add(panel);
			tf.setText(n);
		}
	}

	private void onChange(WebTextField tf, WebLabel overlayLabel, WebOverlay overlay) {
		tf.onChange(new DocumentEventRunnable<WebTextField> () {
            @Override
            public void run(@NotNull final WebTextField component, @Nullable final DocumentEvent event ) {
                CoreSwingUtils.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        final String text = tf.getText();
                        if (text.length() == 0) {
                        	overlayLabel.setToolTip("This name textfield cannot be blank !", TooltipWay.right);
                            if (overlay.getOverlayCount() == 0){
                                overlay.addOverlay(
                                        new AlignedOverlay(
                                                overlayLabel,
                                                BoxOrientation.right,
                                                BoxOrientation.top,
                                                new Insets(0, 0, 0, 3)
                                        )
                                );
                            }
                        }
                        else {
//                        	System.out.println("The name textfield is not blank.");
                            if (overlay.getOverlayCount() > 0) {
                                overlay.removeOverlay(overlayLabel);
                            }
                        }
                    }
                } );
            }
        } );
	}
	
	/**
	 * Loads the names of the crew into the name textfields
	 */
	private void loadCrewNames() {
		int i = 0;
		for(Member m : crew.getTeam()) {
			String n = m.getName();

			JTextField tf = nameTFs.get(i++);
			tf.setText(n);
		}
	}

	/**
	 * Sets up the age textfields
	 */
	private void setUpCrewAge() {
		int i = 0;
		for(Member m : crew.getTeam()) {
			
			int age = 0;
			String ageStr = m.getAge();
			if (ageStr == null)
				age = RandomUtil.getRandomInt(21, 65);
			else
				age = Integer.parseInt(ageStr);		
			
			JTextField tf = new JTextField(5);
			JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
			panel.add(new WebLabel("      Age : "));
			panel.add(tf);

			ageTFs.add(tf);
			crewPanels.get(i++).add(panel);
			tf.setText(age + "");
		}
	}

	
	/**
	 * Get the random age for each crew member into the age textfields
	 */
	private void loadCrewAges() {
		int i = 0;
		for(Member m : crew.getTeam()) {
			int age = 0;
			String ageStr = m.getAge();
			if (ageStr == null)
				age = RandomUtil.getRandomInt(21, 65);
			else
				age = Integer.parseInt(ageStr);	
			JTextField tf = ageTFs.get(i++);
			tf.setText(age + "");
		}
	}
	

	/**
	 * Sets up the combo box for a few attributes
	 * 
	 * @param choice
	 * @param s
	 * @return
	 */
	private WebComboBox setUpCB(int choice, String s) {
		DefaultComboBoxModel<String> m = null;
//		if (choice == 0)
//			m = setUpGenderCBModel();
//		else 
		if (choice == 2)
			m = setUpJobCBModel();
		
		else if (choice == 3) 
			m = setUpCountryCBModel();

		else if (choice == 5) 
			m = setUpDestinationCBModel(s);
		
		final WebComboBox g = new WebComboBox(StyleId.comboboxHover, m);
		g.setWidePopup(true);
//		g.setPreferredSize(new Dimension(CREW_WIDTH, 25));
//		g.setSize(CREW_WIDTH, 25);
		g.setPreferredWidth(PANEL_WIDTH);
//		g.setMinimumWidth(CREW_WIDTH);
		g.setMaximumWidth(PANEL_WIDTH);
		
		g.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e1) {
				String s = (String) g.getSelectedItem();

				g.setSelectedItem(s);
			}
		});

		return g;
	}

	/**
	 * Sets up the crew gender
	 */
	private void setUpCrewGender() {

//		String s[] = new String[crewNum];
		String s = "";
		int j = 0;
		for(Member m : crew.getTeam()) {
			GenderType n = m.getGender();

			s = n.toString();
			
			WebSwitch webSwitch = new WebSwitch(true);

            //				s[j] = "M";
            //				s[j] = "F";
            webSwitch.setSelected(s.equalsIgnoreCase(GenderType.MALE.getName()));
			
			webSwitches.add(webSwitch);
			webSwitch.setSwitchComponents(
					"M", 
					"F");
			TooltipManager.setTooltip(webSwitch, "Choose male or female", TooltipWay.down);
			
			WebPanel panel = new WebPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
//			panel.setPreferredSize(new Dimension(55, 35));
//			panel.setSize(55, 35);
//			panel.setMaximumSize(55, 35);
			panel.add(new WebLabel(" Gender : "));
			panel.add(webSwitch);
			
			crewPanels.get(j++).add(panel);
			
//			webSwitch.addActionListener(new ActionListener() {
//				public void actionPerformed(ActionEvent e) {
////					masterClock.setPaused(!masterClock.isPaused(), false);
//					if (webSwitch.isSelected())
//						;
//					else
//						;
//				};
//			});
				
//			WebComboBox g = setUpCB(0, ""); // 0 = Gender
//			g.setMaximumRowCount(2);
//			crewPanels.get(j).add(g);
//			genderComboBoxList.add(g);
//			g.setSelectedItem(s[j]);
		}
	}

	/**
	 * Loads the crew gender
	 */
	private void loadCrewGender() {

		int j = 0;
		for (Member m : crew.getTeam()) {
			GenderType n = m.getGender();
			
			WebSwitch webSwitch = webSwitches.get(j++);

            webSwitch.setSelected(n == GenderType.MALE);
		}
	}
	
	

	
	/**
	 * Set up personality radio buttons
	 * 
	 * @param col
	 */
	private void setUpCrewPersonality() {
		String quadrant1A = "{E:u}xtravert";
		String quadrant1B = "{I:u}ntrovert"; 
		String quadrant2A = "I{N:u}tuition";
		String quadrant2B = "{S:u}ensing";
		String quadrant3A = "{F:u}eeling"; 
		String quadrant3B = "{T:u}hinking";
		String quadrant4A = "{J:u}udging";
		String quadrant4B = "{P:u}erceiving";
		String cat1 = "World", cat2 = "Information", cat3 = "Decision", cat4 = "Structure";
		String a = null, b = null, c = null;
		
		int col = 0;
		for (Member m : crew.getTeam()) {
			JPanel fullPane = new JPanel(new FlowLayout());
			fullPane.setLayout(new BoxLayout(fullPane, BoxLayout.Y_AXIS));
			fullPane.setPreferredSize(new Dimension(PANEL_WIDTH, 200));
			fullPane.setSize(PANEL_WIDTH, 200);
			fullPane.setMaximumSize(new Dimension(PANEL_WIDTH, 200));
//			ppane.setAlignmentX(0);
			
			List<JRadioButton> radioButtons = new ArrayList<>();	
					
			for (int row = 0; row < 4; row++) {
				
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

				WebRadioButton ra = new WebRadioButton(StyleId.radiobuttonStyled, a);
				ra.addActionListener(this);
//				ra.setActionCommand("a" + row + col);
				
				WebRadioButton rb = new WebRadioButton(StyleId.radiobuttonStyled, b);
				rb.addActionListener(this);
//				rb.setActionCommand("b" + row + col);
				
				if (retrieveCrewMBTI(m, row))
					ra.setSelected(true);
				else
					rb.setSelected(true);
				
				radioButtons.add(ra);
				radioButtons.add(rb);
				
				// Use ButtonGroup to limit to choosing either ra or rb
				ButtonGroup bg1 = new ButtonGroup();
				bg1.add(ra);
				bg1.add(rb);
				
				JPanel quadPane = new JPanel(new GridLayout(1, 2));
//				quadPane.setLayout(new BoxLayout(quadPane, BoxLayout.Y_AXIS));
//				quadPane.setAlignmentX(Component.LEFT_ALIGNMENT);
				quadPane.setPreferredSize(new Dimension(PANEL_WIDTH/2, 60));
//				quadPane.setMinimumSize(new Dimension(CREW_WIDTH/2, 60));
//				quadPane.setSize(CREW_WIDTH/2, 60);
				quadPane.setBorder(BorderFactory.createTitledBorder(c));
				quadPane.add(ra);
				quadPane.add(rb);
				quadPane.setAlignmentX(Component.CENTER_ALIGNMENT);
				fullPane.add(quadPane);
			}
			
			crewPanels.get(col++).add(fullPane);
			
			allRadioButtons.add(radioButtons);
		}
	}

	/**
	 * Loads crew personality
	 * 
	 * @param col
	 */
	private void loadCrewPersonality() {
		int col = 0;
		for (Member m : crew.getTeam()) {

			List<JRadioButton> radioButtons = allRadioButtons.get(col++);
			
			for (int row = 0; row < 4; row++) {

				JRadioButton ra = radioButtons.get(2 * row);
				JRadioButton rb = radioButtons.get(2 * row + 1);
						
				if (retrieveCrewMBTI(m, row))
					ra.setSelected(true);
				else
					rb.setSelected(true);
			}
		}
	}

	/**
	 * Retrieves the crew's specific MBTI
	 * 
	 * @param row
	 * @param col
	 * @param loadFromXML
	 * @return
	 */
	private boolean retrieveCrewMBTI(Member m , int row) {
				
		if (row == 0)
			return m.isExtrovert();
		else if (row == 1)
			return m.isIntuitive();
		else if (row == 2)
			return m.isFeeler();
		else if (row == 3)
			return m.isJudger();
		else
			return false;		
	}

	/**
	 * Get a person's personality type (MTBI) as shown in the radio buttons selected
	 * 
	 * @param col
	 * @return the MTBI string
	 */
	private String getSelectedPersonality(int col) {
		String type = null;		
		List<JRadioButton> radioButtons = allRadioButtons.get(col);//new ArrayList<>();
		
		for (int num = 0; num < 8; num++) {
			JRadioButton b = radioButtons.get(num);
			String oneType = null;
			if (b.isSelected()) {
				oneType = convert2Type(num);
				if (num == 0 || num == 1)
					type = oneType;
				else
					type += oneType;
			}
		}
		
//		System.out.println(type);
		return type;
	}
	
	/**
	 * Converts the type of personality from int to String
	 * 
	 * @param num
	 * @return
	 */
	private static String convert2Type(int num) {
		
		switch (num) {
		case 0:
			return "E";
		case 1:
			return "I";
		case 2:
			return "N";
		case 3:
			return "S";
		case 4:
			return "F";
		case 5:	
			return "T";
		case 6:
			return "J";
		case 7:	
			return "P";
		}
		return null;
	}

	/**
	 * Set up the job comboxbox model
	 * 
	 * @return DefaultComboBoxModel<String>
	 */
	private DefaultComboBoxModel<String> setUpJobCBModel() {

		DefaultComboBoxModel<String> m = new DefaultComboBoxModel<String>();
		for (JobType jt : JobType.values()) {
			if (jt != JobType.POLITICIAN) {
				m.addElement(jt.getName());
			}
		}
		return m;
	}

	/**
	 * Set up the country comboxbox model
	 * 
	 * @return DefaultComboBoxModel<String>
	 */
	private DefaultComboBoxModel<String> setUpCountryCBModel() {
		DefaultComboBoxModel<String> m = new DefaultComboBoxModel<String>();

		m.addElement(SELECT_SPONSOR);
		return m;
	}
	
	/**
	 * Set up the sponsor comboxbox model
	 * 
	 * @param country
	 * @return DefaultComboBoxModel<String>
	 */
	private DefaultComboBoxModel<String> setUpSponsorCBModel(String country) {
					
		DefaultComboBoxModel<String> m = new DefaultComboBoxModel<>();
		m.addAll(ReportingAuthorityFactory.getSupportedCodes());
		return m;
	}
	
	/**
	 * Set up the destination comboxbox model
	 * 
	 * @param destination
	 * @return DefaultComboBoxModel<String>
	 */
	private DefaultComboBoxModel<String> setUpDestinationCBModel(String destination) {

		List<String> destinations = simulationConfigEditor.loadDestinations();
		
		DefaultComboBoxModel<String> m = new DefaultComboBoxModel<String>();
		Iterator<String> j = destinations.iterator();

		while (j.hasNext()) {
			String s = j.next();
			m.addElement(s);
		}
		
		if (m.getIndexOf(destination) != -1 && destinations.contains(destination))
			m.setSelectedItem(destination);
		
		return m;
	}
	
	/**
	 * Set up the job comboxbox
	 * 
	 */
	private void setUpCrewJob() {
		int i = 0;
		for (Member m : crew.getTeam()) {
			String n = m.getJob();
			WebComboBox g = setUpCB(2, n);// 2 = Job
			TooltipManager.setTooltip(g, "Choose the job of this person", TooltipWay.down);
			g.setMaximumRowCount(8);
			crewPanels.get(i++).add(g);
			g.getModel().setSelectedItem(n);
			jobsComboBoxList.add(g);
		}
	}

	/**
	 * Loads the crew job
	 * 
	 */
	private void loadCrewJob() {
		int i = 0;
		for (Member m : crew.getTeam()) {
			String n = m.getJob();
			WebComboBox g = jobsComboBoxList.get(i++); //setUpCB(2, n[i]);// 2 = Job

			g.getModel().setSelectedItem(n);
		}
	}

	/**
	 * Set up the country comboxbox
	 * 
	 */
	private void setUpCrewCountry() {
		int i = 0;
		for (Member m : crew.getTeam()) {
			String country = m.getCountry();
			WebComboBox g = setUpCB(3, country); // 3 = Country
			TooltipManager.setTooltip(g, "Choose the country of origin of this person", TooltipWay.down);
			g.setMaximumRowCount(8);
			crewPanels.get(i++).add(g);
			g.getModel().setSelectedItem(country);
			countriesComboBoxList.add(g);
		}
	}

	/**
	 * Loads the crew's country
	 * 
	 */
	private void loadCrewCountry() {
		int i = 0;
		for (Member m : crew.getTeam()) {
			String country = m.getCountry();
			WebComboBox g = countriesComboBoxList.get(i++); //setUpCB(3, n[i]); // 3 = Country

			populateCountryCombo(m.getSponsorCode(),
								(DefaultComboBoxModel<String>) g.getModel());
			g.getModel().setSelectedItem(country);
		}
	}
			
	/**
	 * Set up the sponsor combox box
	 * 
	 */
	private void setUpCrewSponsor() {
		int i = 0;
		for (Member mb : crew.getTeam()) {
			String s = mb.getSponsorCode();
			String c = mb.getCountry();
			DefaultComboBoxModel<String> m = setUpSponsorCBModel(c);

			WebComboBox g = new WebComboBox(StyleId.comboboxHover, m);
			g.setWidePopup(true);
			g.setPreferredWidth(PANEL_WIDTH);
			g.setMaximumWidth(PANEL_WIDTH);
		    			
			TooltipManager.setTooltip(g, "Choose the sponsor of this person", TooltipWay.down);
			g.setMaximumRowCount(8);
			crewPanels.get(i++).add(g);
			sponsorsComboBoxList.add(g);
			g.getModel().setSelectedItem(s);

			// Set up and add an item listener to the country combobox
			SponsorListener l = new SponsorListener();
			actionListeners.add(l);
		    g.addItemListener(l);		    
		}
	}

	/**
	 * Loads the crew's sponsor
	 * 
	 */
	private void loadCrewSponsor() {
		int i = 0;
		for (Member m : crew.getTeam()) {
			String s = m.getSponsorCode();
			WebComboBox g = sponsorsComboBoxList.get(i++); 
			
			g.getModel().setSelectedItem(s);
		}
	}
	
	/**
	 * The SponsorListener class serves to listen to the change made in the country combo box. 
	 * It triggers a corresponding change in the country combo box.
	 * 
	 * @author mkhelios
	 *
	 */
	class SponsorListener implements ItemListener {

		// This method is called only if a new item has been selected.
		@SuppressWarnings("unchecked")
		public void itemStateChanged(ItemEvent evt) {

			int index = actionListeners.indexOf(this);
			
			if (evt.getStateChange() == ItemEvent.SELECTED && sponsorsComboBoxList.size() > 0) {
				// Item was just selected
		        WebComboBox m = sponsorsComboBoxList.get(index);
		        String sponsor = (String) m.getSelectedItem();
		        
				// Get combo box model
		        WebComboBox combo = countriesComboBoxList.get(index);
		        DefaultComboBoxModel<String> model =
		        		(DefaultComboBoxModel<String>) combo.getModel();
		        
		        populateCountryCombo(sponsor, model);
		        
			} else if (evt.getStateChange() == ItemEvent.DESELECTED && sponsorsComboBoxList.size() > 0) {
				// Item is no longer selected
				
		        WebComboBox combo = countriesComboBoxList.get(index);
		        DefaultComboBoxModel<String> model =
		        		(DefaultComboBoxModel<String>) combo.getModel();
		        
		        // removing old data
		        model.removeAllElements();
		        
				model.addElement(SELECT_SPONSOR);

			}
		}

	}
		 

	/**
	 * Load the country model from a ReportingAuthority
	 * @param sponsor
	 * @param model
	 */
	private void populateCountryCombo(String sponsorCode, DefaultComboBoxModel<String> model) {
		// removing old data
		model.removeAllElements();

		// Load the countries
		ReportingAuthority ra = ReportingAuthorityFactory.getAuthority(sponsorCode);
		for (String country : ra.getCountries()) {
			model.addElement(country);
		}
	}
	
	/**
	 * Get the current committed changes
	 * @return
	 */
	public Crew getCrewConfig() {
		return crew;
	}
	
	private boolean designateCrew(String crewName) {		
		Crew newCrew = crewConfig.loadCrew(crewName);
		if (newCrew == null) {
			return false;
		}
		crew = newCrew;
		crewNum = crew.getNumberOfConfiguredPeople();

		loadCrewNames();
		loadCrewGender();
		loadCrewAges();
		loadCrewJob();
		loadCrewSponsor();
		loadCrewCountry();
		loadCrewPersonality();
		
		// Show alpha crew in title 
		f.setTitle(TITLE + " - " + crew.getName() + " Crew On-board");
		
		logger.config("crew.xml loaded.");
		return true;
	}
	
	/**
	 * Prepare this window for deletion.
	 */
	public void destroy() {
		crewConfig = null;
		simulationConfigEditor = null;
		f = null;
		mainPane = null;
		nameTFs.clear();
		nameTFs = null;
		ageTFs.clear();
		ageTFs = null;
		sponsorsComboBoxList = null;
		countriesComboBoxList = null;
		jobsComboBoxList = null;
	}


}

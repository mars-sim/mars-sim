/*
 * Mars Simulation Project
 * StartUpChooser.java
 * @date 2025-09-23
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.Taskbar;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileFilter;

import com.formdev.flatlaf.util.SystemInfo;
import com.mars_sim.core.Simulation;
import com.mars_sim.core.SimulationRuntime;
import com.mars_sim.core.authority.Authority;
import com.mars_sim.core.authority.AuthorityFactory;
import com.mars_sim.core.configuration.Scenario;
import com.mars_sim.core.configuration.ScenarioConfig;
import com.mars_sim.core.structure.SettlementTemplate;
import com.mars_sim.core.structure.SettlementTemplateConfig;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.MainWindow;
import com.mars_sim.ui.swing.tool.TexturedPanel;

/**
 * This class is a simple dialog to let user choose how to start the simulation.
 */
@SuppressWarnings("serial")
public class StartUpChooser extends JDialog {
    /**
     * Files filter for simulation files.
     */
    private static class SimFileFilter extends FileFilter {
        @Override
        public boolean accept(java.io.File f) {
            if (f.isDirectory()) {
                return true;
            }
            String name = f.getName().toLowerCase();
            return name.endsWith(Simulation.SAVE_FILE_EXTENSION);
        }

        @Override
        public String getDescription() {
            return "Saved simulation file (" + Simulation.SAVE_FILE_EXTENSION + ")";
        }
    }

    static final int NEW_SIM = 0;
    static final int LOAD_SIM = 1;
    static final int EDIT_SCENARIO = 2;
    static final int SCENARIO = 3;
    static final int TEMPLATE = 4;
    static final int EXIT = 5;
    static final int NEW_SOCIETY = 6;
    
    private String scenarioLabel = Msg.getString("StartUpChooser.scenario");
    private String templateLabel = Msg.getString("StartUpChooser.template");

    private ScenarioConfig scenarioConfig = null;
    private AuthorityFactory authorityConfig = null;
    private SettlementTemplateConfig templateConfig = null;

    private int selected = -1;
    private String selectedFile;
    private Scenario selectedScenario;
    private SettlementTemplate selectedTemplate;
    private Authority selectedAuthority;

    /**
     * Shows the start chooser to the user. Pass in the various configuration pools for their selection.
     * 
     * @param scenarioConfig
     * @param templateConfig
     * @param authorityConfig
     */
    StartUpChooser(ScenarioConfig scenarioConfig, SettlementTemplateConfig templateConfig,
                    AuthorityFactory authorityConfig) {
        super(new JFrame());

        this.scenarioConfig = scenarioConfig;
        this.templateConfig = templateConfig;
        this.authorityConfig = authorityConfig;
        
		setSize(300, 365);
		
		if (SystemInfo.isMacOS) {
			final Toolkit defaultToolkit = Toolkit.getDefaultToolkit();
			Image image = defaultToolkit.getImage(getClass().getResource(MainWindow.LANDER_91_PATH));
			final Taskbar taskbar = Taskbar.getTaskbar();
			taskbar.setIconImage(image);
			// Move the menu bar out of the main window to the top of the screen
			System.setProperty( "apple.laf.useScreenMenuBar", "true" );
		}
		else {
			setIconImage(MainWindow.getIconImage());
		}

        setResizable(false);
		setTitle(Msg.getString("StartUpChooser.title")); // -NLS-1$


        Image image = ImageLoader.getImage("starter");
        
		TexturedPanel contentPanel = new TexturedPanel(image);
		
		contentPanel.setLayout(new BorderLayout());
		
        add(contentPanel);

        JLabel instructions = new JLabel(Msg.getString("StartUpChooser.instructions"));
        instructions.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(),
                            BorderFactory.createEmptyBorder(2, 2, 2, 2)));
        instructions.setHorizontalAlignment(SwingConstants.CENTER);
        instructions.setFont(new Font(Font.DIALOG, Font.ITALIC, 12));
        contentPanel.add(instructions, BorderLayout.NORTH);

		// Sets the dialog content panel.
        TexturedPanel buttonPanel = new TexturedPanel(image);
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttonPanel.setAlignmentY(Component.CENTER_ALIGNMENT);
        
        contentPanel.add(buttonPanel, BorderLayout.CENTER);

        addStartButton(buttonPanel, "newSim", e -> choiceMade(NEW_SIM));
        addStartButton(buttonPanel, "load", e -> selectSimFile());
        addStartButton(buttonPanel, "loadDefault", e -> choiceMade(LOAD_SIM));
        addStartButton(buttonPanel, "loadScenario", e -> selectScenario());
        addStartButton(buttonPanel, "loadTemplate", e -> selectTemplate());
        addStartButton(buttonPanel, "editScenario", e -> choiceMade(EDIT_SCENARIO));
        
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 120)));
        
    	// Set the location of the dialog at the center of the screen.
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((screenSize.width - getWidth()) / 2, (screenSize.height - getHeight()) / 2);
		setVisible(true);
        addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent event) {
				choiceMade(EXIT);
			}
		});
    }

    private void addStartButton(JPanel panel, String labelKey, ActionListener action) {
        JButton button = new JButton(Msg.getString("StartUpChooser." + labelKey));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setAlignmentY(Component.CENTER_ALIGNMENT);
//        button.setPreferredSize(new Dimension(-1, 30));
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
//        button.setBorder(BorderFactory.createLineBorder(Color.ORANGE, 2));
        button.setFont(new Font(Font.DIALOG, Font.PLAIN, 14));
        button.setForeground(new Color(75, 54, 33));//92, 64, 51));
        button.setToolTipText(Msg.getString("StartUpChooser." + labelKey + ".tooltip"));
        button.addActionListener(action);

        panel.add(Box.createVerticalGlue());       
//        panel.add(Box.createVerticalStrut(3));
        panel.add(button);
        
        button.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
            	button.setFont(new Font(Font.DIALOG, Font.BOLD, 18));
                button.setBorderPainted(true);
                button.setBorder(BorderFactory.createLineBorder(Color.ORANGE, 1));
            }

            @Override
            public void focusLost(FocusEvent e) {
            	button.setFont(new Font(Font.DIALOG, Font.PLAIN, 14));
//            	button.setBorder(button.getBorder());
            	// Remove the border
            	button.setBorderPainted(false);
            }
        });
    }

    private void selectTemplate() {
        var content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        content.add(new JLabel());
        		
        content.add(SelectionDialog.createComboPane(templateLabel, templateConfig.getKnownItems(),
                                                s -> selectedTemplate = s));
        content.add(SelectionDialog.createComboPane(Msg.getString("StartUpChooser.authority"), authorityConfig.getKnownItems(),
                                                s -> selectedAuthority = s));
        if (SelectionDialog.showDialog(this, content, templateLabel)) {
            choiceMade(TEMPLATE);
        }
        else {
        	choiceMade(EXIT);
        }
    }

    private void selectScenario() {
        var content = SelectionDialog.createComboPane(scenarioLabel, scenarioConfig.getKnownItems(), s -> selectedScenario = s);

        if (SelectionDialog.showDialog(this, content, scenarioLabel)) {
            choiceMade(SCENARIO);
        }
        else {
        	choiceMade(EXIT);
        }
    }

    /**
	 * Performs the process of loading a simulation.
	 */
	private void selectSimFile() {
		JFileChooser chooser = new JFileChooser(SimulationRuntime.getSaveDir());
        chooser.setFileFilter(new SimFileFilter());
		chooser.setDialogTitle(Msg.getString("MainWindow.dialogLoadSavedSim")); // -NLS-1$
		if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			selectedFile = chooser.getSelectedFile().getAbsolutePath();
            choiceMade(LOAD_SIM);
		}
        else {
        	choiceMade(EXIT);
        }
	}

    private synchronized void choiceMade(int choice) {
        selected = choice;
        dispose();
		notifyAll();
	}

    /**
     * Gets the choice made by the user. This method blocks until a choice is made.
     * @return The choice made by the user.
     */
    public synchronized int getChoice() {
        while (selected == -1) {
            try {
                wait();
            } catch (InterruptedException e)  {
                Thread.currentThread().interrupt();
            }
        }
        return selected;
    }

    /**
     * Gets the selected file if the user chose to load a simulation.
     * @return Path to the selected file to load; may be null if default is to be used.
     */
    public String getSelectedFile() {
        return selectedFile;
    }

    /**
     * Return the selected Scenario if the user chose to load a scenario.
     * @return Scenario selected.
     */
    public Scenario getScenario() {
        return selectedScenario;
    }

    /**
     * Return the selected Template if the user chose to load a settlement template.
     * @return Template selected.
     */
    public SettlementTemplate getTemplate() {
        return selectedTemplate;
    }

    /**
     * Return the selected Authority if the user chose to load a settlement template.
     * This may be null if the default authority is to be used.
     * @return Authority selected; may be null.
     */
    public Authority getAuthority() {
        return selectedAuthority;
    }
}

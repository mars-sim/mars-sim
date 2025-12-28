/*
 * Mars Simulation Project
 * TabPanelPersonality.java
 * @date 2025-08-12
 * @author Manny Kung
 */
package com.mars_sim.ui.swing.unit_window.person;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;

import javax.swing.JPanel;

import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.MBTIPersonality;
import com.mars_sim.core.person.ai.PersonalityTraitManager;
import com.mars_sim.core.person.ai.PersonalityTraitType;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.entitywindow.EntityTabPanel;
import com.mars_sim.ui.swing.utils.AttributePanel;
import com.mars_sim.ui.swing.utils.SwingHelper;

/**
 * The TabPanelPersonality is a tab panel for personality information about a person.
 */
@SuppressWarnings("serial")
class TabPanelPersonality extends EntityTabPanel<Person> {

	private static final String PER_ICON = "personality"; //$NON-NLS-1$

	/**
	 * Constructor.
	 * 
	 * @param person the person to display.
	 * @param context the UI context.
	 */
	public TabPanelPersonality(Person person, UIContext context) {
		
		// Use the TabPanel constructor
		super(
			Msg.getString("TabPanelPersonality.title"), //$NON-NLS-1$
			ImageLoader.getIconByName(PER_ICON),		
			Msg.getString("TabPanelPersonality.title"), //$NON-NLS-1$
			context, person
		);
	}
	
	@Override
	protected void buildUI(JPanel content) {

		// Prepare spring layout info panel.
		JPanel infoPanel = new JPanel(new BorderLayout());
		content.add(infoPanel, BorderLayout.NORTH);

		JPanel scorePanel = new JPanel(new GridLayout(2, 1));
		infoPanel.add(scorePanel, BorderLayout.NORTH);
		
		// Create the text area for displaying the Big Five scores
		scorePanel.add(createBigFive(getEntity()));
		// Create the text area for displaying the MBTI scores
		scorePanel.add(createMBTI(getEntity()));
	}
	
	private static record TraitScore(String traitName, int score) {}

	/**
	 * Creates the MBTI text area.
	 * 
	 * @param person Person for MBTI
	 * @return
	 */
	private JPanel createMBTI(Person person) {
		MBTIPersonality p = person.getMind().getMBTI();
		int ie = p.getIntrovertExtrovertScore();
		int ns = p.getScores().get(1);
		int ft = p.getScores().get(2);
		int jp = p.getScores().get(3);
		
		TraitScore[] traits = new TraitScore[4];

		// Prepare attribute panel.
		AttributePanel attributePanel = new AttributePanel();
		
		JPanel listPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		listPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		listPanel.setAlignmentY(Component.CENTER_ALIGNMENT);
		listPanel.add(attributePanel, BorderLayout.CENTER);
		listPanel.setBorder(SwingHelper.createLabelBorder(Msg.getString("TabPanelPersonality.mbti.title")));
		
		traits[0] = new TraitScore((ie < 0) ? "Introvert (I)" : "Extrovert (E)", ie);
		traits[1] = new TraitScore((ns < 0) ? "Intuitive (N)" : "Sensing (S)", ns);
		traits[2] = new TraitScore((ft < 0) ? "Feeling (F)" : "Thinking (T)", ft);
		traits[3] = new TraitScore((jp < 0) ? "Judging (J)" : "Perceiving (P)", jp);
		
		String tip =  "<html>Introvert (I) / Extrovert  (E) : -50 to 0 / 0 to 50" 
				  + "<br>Intuitive (N) / Sensing    (S) : -50 to 0 / 0 to 50"
				  + "<br>  Feeling (F) / Thinking   (T) : -50 to 0 / 0 to 50"
				  + "<br>  Judging (J) / Perceiving (P) : -50 to 0 / 0 to 50</html>";
		
		for (var t : traits) {
			attributePanel.addTextField(t.traitName(), Math.round(t.score() * 10.0)/10.0 + " %", tip);
		}
		
		String type = p.getTypeString();
		String descriptor = p.getDescriptor();
		
		attributePanel.addTextField(type, descriptor, Msg.getString("TabPanelPersonality.mbti.descriptor.tip"));
		
		return listPanel;
	}

	/**
	 * Creates the text area for the Big Five.
	 * 
	 * @return
	 */
	private JPanel createBigFive(Person person) {
		PersonalityTraitManager p = person.getMind().getTraitManager();
		
		TraitScore[] traits = new TraitScore[PersonalityTraitType.values().length];
    	for (PersonalityTraitType t : PersonalityTraitType.values()) {
    		traits[t.ordinal()] = new TraitScore(t.getName(), p.getPersonalityTrait(t));
    	}

		// Prepare attribute panel.
		AttributePanel attributePanel = new AttributePanel();
    	
		JPanel listPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		listPanel.setAlignmentY(Component.CENTER_ALIGNMENT);
		listPanel.setToolTipText(Msg.getString("TabPanelPersonality.bigFive.tip"));//$NON-NLS-1$
		listPanel.add(attributePanel, BorderLayout.CENTER);
		
		listPanel.setBorder(SwingHelper.createLabelBorder(Msg.getString("TabPanelPersonality.bigFive.title")));
		
		String tip =  "<html>          Openness : 0 to 100" 
			  	  + "<br> Conscientiousness : 0 to 100"
			  	  + "<br>      Extraversion : 0 to 100"
			  	  + "<br>       Neuroticism : 0 to 100</html>";

		for (var t : traits) {
			attributePanel.addTextField(t.traitName(), Math.round(t.score() * 10.0)/10.0 + " %", tip);
		}
		
		return listPanel;
	}
}

/*
 * Mars Simulation Project
 * NotesTabPanel.java
 * @date 2022-07-09
 * @author Manny Kung
 */
package com.mars_sim.ui.swing.unit_window;

import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.mars_sim.core.Unit;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.MarsPanelBorder;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.entitywindow.EntityTabPanel;


/**
 * The NotesTabPanel is a tab panel for recording commander's notes regarding this unit
 */
@SuppressWarnings("serial")
public class NotesTabPanel extends EntityTabPanel<Unit> {

	private static final String NOTE_ICON = "note";
	
	private static final String ENTER_HERE = "Enter Here";
		
	/** The text area for holding the notes. */
	private JTextArea textArea;
	private JButton applyButton;
	private JButton revertButton;
		
	/**
	 * Constructor.
	 * 
	 * @param unit    the unit to display.
	 * @param desktop the main desktop.
	 */
	public NotesTabPanel(Unit unit, UIContext context) {
		super(Msg.getString("NotesTabPanel.title"), ImageLoader.getIconByName(NOTE_ICON), null,
				 context, unit); //-NLS-1$
	}

	@Override
	protected void buildUI(JPanel content) {

		// Create notes panel
		JPanel notesPanel = new JPanel(new BorderLayout(5, 5));
		notesPanel.setBorder(new MarsPanelBorder());
		notesPanel.setBorder(new EmptyBorder(1, 1, 1, 1));
		content.add(notesPanel, BorderLayout.CENTER);
		
		textArea = new JTextArea();
		notesPanel.add(textArea);

		textArea.getDocument().addDocumentListener(new DocumentListener() { 
			@Override
    		public void insertUpdate(DocumentEvent e) {
        		updateButtons(true);
    		}
			@Override
			public void removeUpdate(DocumentEvent e) {
				updateButtons(true);
			}
			public void changedUpdate(DocumentEvent e) {
				//Plain text components do not fire these events
			}
		});

		var buttonPanel = new JPanel();
		content.add(buttonPanel, BorderLayout.SOUTH);

		applyButton = new JButton("Save");
		applyButton.addActionListener(e -> applyNote());
		buttonPanel.add(applyButton);

		revertButton = new JButton("Revert");
		revertButton.addActionListener(e -> loadNote());
		buttonPanel.add(revertButton);

		loadNote();
	}

	/**
	 * Update the state of the buttons
	 */
	private void updateButtons(boolean enabled) {
		applyButton.setEnabled(enabled);
		revertButton.setEnabled(enabled);
	}

	/**
	 * Loads the info on this panel.
	 */
	private void loadNote() {
		var notesCache = getEntity().getNotes();
		if (notesCache == null || notesCache.equals(""))
			textArea.setText(ENTER_HERE);
		else {
			textArea.setText(notesCache);
		}
		updateButtons(false); // No active change
	}

	/**
	 * Updates the info on this panel.
	 */
	private void applyNote() {
		String notes = textArea.getText();
		Unit unit = getEntity();
		unit.setNotes(notes);
		updateButtons(false); // No active change
	}
}

/*
 * Mars Simulation Project
 * NotesTabPanel.java
 * @date 2022-07-09
 * @author Manny Kung
 */
package org.mars_sim.msp.ui.swing.unit_window;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;

import com.alee.laf.panel.WebPanel;
import com.alee.laf.text.WebTextArea;
import com.alee.managers.style.StyleId;


/**
 * The NotesTabPanel is a tab panel for recording commander's notes regarding this unit
 */
@SuppressWarnings("serial")
public class NotesTabPanel extends TabPanel{

	private static final String NOTE_ICON = Msg.getString("icon.note"); //$NON-NLS-1$
	
	private static final String ENTER_HERE = "Enter Here";

	/** The cache for notes. */
	private String notesCache = "";
		
	/** The text area for holding the notes. */
	private WebTextArea textArea;
		
	/**
	 * Constructor.
	 * 
	 * @param unit    the unit to display.
	 * @param desktop the main desktop.
	 */
	public NotesTabPanel(Unit unit, MainDesktopPane desktop) {
		super(null, ImageLoader.getNewIcon(NOTE_ICON),
				Msg.getString("NotesTabPanel.title"), unit, desktop);
	}

	@Override
	protected void buildUI(JPanel content) {

		// Create notes panel
		WebPanel notesPanel = new WebPanel(new BorderLayout(5, 5));
		notesPanel.setBorder(new MarsPanelBorder());
		notesPanel.setBorder(new EmptyBorder(1, 1, 1, 1));
		content.add(notesPanel);
		
		notesCache = getUnit().getNotes();
		
		textArea = new WebTextArea(StyleId.textareaDecorated);
		notesPanel.add(textArea);
		
		if (notesCache == null || notesCache.equals(""))
			textArea.setInputPrompt(ENTER_HERE);
		else {
			textArea.append(notesCache);
		}
	}
	
	/**
	 * Updates the info on this panel.
	 */
	@Override
	public void update() {
		String notes = textArea.getText();
		Unit unit = getUnit();
		if (notes == null || notes.equals("")) {
			textArea.setInputPrompt(ENTER_HERE);
			notesCache = "";
			unit.setNotes(notes);
		}
		else if (!notesCache.equals(notes)) {
			notesCache = notes;
			unit.setNotes(notes);
		}
	}
}

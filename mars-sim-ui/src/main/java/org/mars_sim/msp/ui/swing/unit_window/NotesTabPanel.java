/**
 * Mars Simulation Project
 * NotesTabPanel.java
 * @version 3.1.0 2019-11-12
 * @author Manny Kung
 */

package org.mars_sim.msp.ui.swing.unit_window;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.logging.Logger;

import javax.swing.Icon;
import javax.swing.border.EmptyBorder;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;

import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;
import com.alee.laf.text.WebTextArea;
import com.alee.managers.style.StyleId;


/**
 * The NotesTabPanel is a tab panel for recording commander's notes regarding this unit
 */
public class NotesTabPanel extends TabPanel{

//	public NotesTabPanel(String tabTitle, Icon tabIcon, String tabToolTip, Unit unit, MainDesktopPane desktop) {
//		super(tabTitle, tabIcon, tabToolTip, unit, desktop);
//		// TODO Auto-generated constructor stub
//	}

	/** default serial id. */
	private static final long serialVersionUID = 12L;

	/** default logger. */
	private static Logger logger = Logger.getLogger(NotesTabPanel.class.getName());

	/** Is UI constructed. */
	private boolean uiDone = false;
	/** The unit of interest. */
	private Unit unit;
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
		// Use the TabPanel constructor
		super(Msg.getString("NotesTabPanel.title"), null, Msg.getString("NotesTabPanel.tooltip"), unit, desktop);

		this.unit = unit;
	}
	
	public boolean isUIDone() {
		return uiDone;
	}
	
	public void initializeUI() {
		uiDone = true;
		
		// Initialize location header.
		WebPanel titlePane = new WebPanel(new FlowLayout(FlowLayout.CENTER));
		topContentPanel.add(titlePane);

		WebLabel titleLabel = new WebLabel(Msg.getString("NotesTabPanel.title"), WebLabel.CENTER); //$NON-NLS-1$
		titleLabel.setFont(new Font("Serif", Font.BOLD, 16));
		// titleLabel.setForeground(new Color(102, 51, 0)); // dark brown
		titlePane.add(titleLabel);
		
		// Create notes panel
		WebPanel notesPanel = new WebPanel(new BorderLayout(5, 5));
		notesPanel.setBorder(new MarsPanelBorder());
		notesPanel.setBorder(new EmptyBorder(1, 1, 1, 1));
		centerContentPanel.add(notesPanel);
		
		notesCache = unit.getNotes();
		
		textArea = new WebTextArea(StyleId.textareaDecorated);
		notesPanel.add(textArea);
		
		if (notesCache == null || notesCache.equals(""))
			textArea.setInputPrompt("Enter Here");
		else {
			textArea.append(notesCache);
		}

	}
	
	/**
	 * Updates the info on this panel.
	 */
	public void update() {
		if (!uiDone)
			initializeUI();
		
		String notes = textArea.getText();
		
		if (notes == null || notes.equals("")) {
			textArea.setInputPrompt("Enter Here");
			notesCache = "";
			unit.setNotes(notes);
		}
		else if (!notesCache.equals(notes)) {
			notesCache = notes;
			unit.setNotes(notes);
//			System.out.println("Notes: " + notes);
		}
	}
}

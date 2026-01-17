/*
 * Mars Simulation Project
 * EntityDisplayInfo.java
 * @date 2023-04-28
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.displayinfo;

import javax.swing.Icon;

import com.mars_sim.core.Entity;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ImageLoader;

/**
 * Provides display information about an Entity.
 */
public class EntityDisplayInfo {

    private Icon buttonIcon;
	private String buttonName;
    private String defaultSound;
	private String singularLabel;
	
	EntityDisplayInfo(String entityKey) {
		this(entityKey, entityKey, null);
	}

	EntityDisplayInfo(String entityKey, String defaultSound) {
		this(entityKey, entityKey, defaultSound);
	}	

    EntityDisplayInfo(String entityIcon, String entityKey, String defaultSound) {
		this.singularLabel = Msg.getString(entityKey + ".singular");
        this.defaultSound = defaultSound;
		this.buttonName = entityIcon.toLowerCase();
	}

	/**
	 * Get the name for a single entity of this type.
	 */
	public String getSingularLabel() {
		return singularLabel;
	}

	/**
	 * Gets icon for Entity.
	 * @param unit the entity to display
	 * @return icon
	 */
	public Icon getButtonIcon(Entity unit) {
		if (buttonIcon == null) {			
        	buttonIcon = ImageLoader.getIconByName(buttonName);
		}
		return buttonIcon;
	}

	/**
	 * Gets a sound appropriate for this entity.
	 * 
	 * @param unit the entity to display.
	 * @return sound filepath for unit or empty string if none.
	 */
    public String getSound(Entity unit) {
        return defaultSound;
    }
}

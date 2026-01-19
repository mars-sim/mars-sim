/*
 * Mars Simulation Project
 * PersonDisplayInfo.java
 * @date 2025-08-11
 * @author Scott Davis
 */

package com.mars_sim.ui.swing.displayinfo;

import com.mars_sim.core.Entity;
import com.mars_sim.core.person.GenderType;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.tool.RandomUtil;
import com.mars_sim.ui.swing.sound.SoundConstants;

/**
 * Provides display information about a person.
 */
class PersonDisplayInfoBean extends EntityDisplayInfo {

	/**
	 * Constructor.
	 */
	PersonDisplayInfoBean() {
		super("Person");
	}

	/**
	 * Gets a sound appropriate for this unit.
	 * 
	 * @param unit the unit to display.
	 * @returns sound filepath for unit or empty string if none.
	 */
	@Override
	public String getSound(Entity unit) {
		Person person = (Person) unit;
		String result = "";
		boolean male = GenderType.MALE == person.getGender();
		int randomSoundNum = RandomUtil.getRandomInt(1, 2);
		if (person.getPhysicalCondition().isDead()) result = SoundConstants.SND_PERSON_DEAD;
		else {
			if (male) {
				if (randomSoundNum == 1) result = SoundConstants.SND_PERSON_MALE1;
				else if (randomSoundNum == 2) result = SoundConstants.SND_PERSON_MALE2;
			}
			else {
				if (randomSoundNum == 1) result = SoundConstants.SND_PERSON_FEMALE1;
				else if (randomSoundNum == 2) result = SoundConstants.SND_PERSON_FEMALE2;
			}
		}
		return result;
	}
}

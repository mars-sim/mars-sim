/**
 * Mars Simulation Project
 * PersonDisplayInfo.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */

package com.mars_sim.ui.swing.unit_display_info;

import javax.swing.Icon;

import com.mars_sim.core.Unit;
import com.mars_sim.core.person.GenderType;
import com.mars_sim.core.person.Person;
import com.mars_sim.tools.util.RandomUtil;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.sound.SoundConstants;

/**
 * Provides display information about a person.
 */
class PersonDisplayInfoBean extends AbstractUnitDisplayInfo {

	/**
	 * Constructor.
	 */
	PersonDisplayInfoBean() {
	}

	/**
	 * Gets icon for unit button.
	 * @return icon
	 */
	public Icon getButtonIcon(Unit unit) {
		Icon buttonIcon = null;

		if (unit instanceof Person) {
			Person person = (Person) unit;
			if (GenderType.MALE == person.getGender()) {
				buttonIcon = ImageLoader.getIconByName("unit/person_male");
			} else {
				buttonIcon = ImageLoader.getIconByName("unit/person_female");
			}
		 }

		return buttonIcon;
	}

	/**
	 * Gets a sound appropriate for this unit.
	 * @param unit the unit to display.
	 * @returns sound filepath for unit or empty string if none.
	 */
	public String getSound(Unit unit) {
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

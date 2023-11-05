package com.mars_sim.core.person.ai;

import com.mars_sim.core.AbstractMarsSimUnitTest;
import com.mars_sim.core.person.Person;


public class SkillManagerTest  extends AbstractMarsSimUnitTest   {
    public void testAddNewSkillNExperience() {
        Person p = buildPerson("Skill", buildSettlement());

        SkillManager sm = p.getSkillManager();

        assertSkillExperience(sm, SkillType.BOTANY, 10);
        assertSkillExperience(sm, SkillType.MANAGEMENT, 15);
    }

    private void assertSkillExperience(SkillManager sm, SkillType type, int level) {
        sm.addNewSkill(type, level);
        Skill b = sm.getSkill(type);

        double expLevel = Skill.BASE * Math.pow(2D, level);

        assertEquals(type.name() + " Skill level", level, b.getLevel());
        assertEquals(type.name() + " time", 0D, b.getTime());
        assertEquals(type.name() + " experience needed", expLevel - b.getExperience(), b.getNeededExp());
    }

    public void testAddExperience() {
        Person p = buildPerson("Skill", buildSettlement());

        SkillManager sm = p.getSkillManager();

        int startLevel = 2;
        sm.addNewSkill(SkillType.CHEMISTRY, startLevel);
        Skill c = sm.getSkill(SkillType.CHEMISTRY);
        double expNeeded = c.getNeededExp();
        sm.addExperience(SkillType.CHEMISTRY, expNeeded, 10D);

        assertEquals("Next level", startLevel + 1, c.getLevel());
        assertEquals("No experience", 0D, c.getExperience());
        assertEquals("Experience needed", Skill.BASE * Math.pow(2D, startLevel+1), c.getNeededExp());
    }
}

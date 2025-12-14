package com.mars_sim.core.person.ai;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.mars_sim.core.test.MarsSimUnitTest;
import com.mars_sim.core.person.Person;


public class SkillManagerTest  extends MarsSimUnitTest   {
    @Test
    public void testAddNewSkillNExperience() {
        Person p = buildPerson("Skill", buildSettlement("mock"));

        SkillManager sm = p.getSkillManager();

        assertSkillExperience(sm, SkillType.BOTANY, 10);
        assertSkillExperience(sm, SkillType.MANAGEMENT, 15);
    }

    private void assertSkillExperience(SkillManager sm, SkillType type, int level) {
        sm.addNewSkill(type, level);
        Skill b = sm.getSkill(type);

        double expLevel = Skill.BASE * Math.pow(2D, level);

        assertEquals(level, b.getLevel(), type.name() + " Skill level");
        assertEquals(0D, b.getTime(), type.name() + " time");
        assertEquals(expLevel - b.getExperience(), b.getNeededExp(), type.name() + " experience needed");
    }

    @Test
    public void testAddExperience() {
        Person p = buildPerson("Skill", buildSettlement("mock"));

        SkillManager sm = p.getSkillManager();

        int startLevel = 2;
        sm.addNewSkill(SkillType.CHEMISTRY, startLevel);
        Skill c = sm.getSkill(SkillType.CHEMISTRY);
        double expNeeded = c.getNeededExp();
        sm.addExperience(SkillType.CHEMISTRY, expNeeded, 10D);

        assertEquals(startLevel + 1, c.getLevel(), "Next level");
        assertEquals(0D, c.getExperience(), "No experience");
        assertEquals(Skill.BASE * Math.pow(2D, startLevel+1), c.getNeededExp(), "Experience needed");
    }
}

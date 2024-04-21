package com.mars_sim.core.person.ai.task.util;


import java.util.Collections;
import java.util.Set;

import com.mars_sim.core.AbstractMarsSimUnitTest;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.NaturalAttributeType;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.task.util.ExperienceImpact.SkillWeight;
import com.mars_sim.core.structure.Settlement;

public class ExperienceImpactTest extends AbstractMarsSimUnitTest {

    public void testApplyBasicSkill() {
        Settlement s = buildSettlement();
        Person p = buildPerson("Worker #1", s);

        double origStress = p.getPhysicalCondition().getStress();

        // Prepare the skills, make sure 2 to be tested are level at the start
        var sm = p.getSkillManager();
        sm.addNewSkill(SkillType.BIOLOGY, 1);
        sm.addNewSkill(SkillType.CHEMISTRY, 1);

        var changed1 = sm.getSkill(SkillType.BIOLOGY);
        var changed2 = sm.getSkill(SkillType.CHEMISTRY);

        var origChanged1Exp = changed1.getCumulativeExperience();
        var origChanged2Exp = changed2.getCumulativeExperience();

        var unchanged = sm.getSkills().get(2);
        var origUnChangedExp = unchanged.getCumulativeExperience();


        double skillsRatio = 1D;
        Set<SkillType> skills = Set.of(changed1.getType(), changed2.getType());
        double stressRatio = 0D;  // No stress doing this

        ExperienceImpact impact = new ExperienceImpact(skillsRatio,
                                            NaturalAttributeType.EXPERIENCE_APTITUDE,
                                            false, stressRatio, skills);

        // Simple apply with no assistance
        impact.apply(p, 100D, 0D, 1D);

        // Check skills have changed
        var newChanged1Exp = sm.getSkill(changed1.getType()).getCumulativeExperience();
        var newChanged2Exp = sm.getSkill(changed2.getType()).getCumulativeExperience();
        var newUnChangedExp = sm.getSkill(unchanged.getType()).getCumulativeExperience();

        assertEquals("Unchanged skill",  origUnChangedExp, newUnChangedExp);
        assertGreaterThan("Changed #1 skill",  origChanged1Exp, newChanged1Exp);
        assertGreaterThan("Changed #2 skill",  origChanged2Exp, newChanged2Exp);

        double newStress = p.getPhysicalCondition().getStress();
        assertEquals("Unchanged stress",  origStress, newStress);

    }

    public void testApplyUnbalancedSkill() {
        Settlement s = buildSettlement();
        Person p = buildPerson("Worker #1", s);

        double origStress = p.getPhysicalCondition().getStress();

        // Prepare the skills
        var sm = p.getSkillManager();
        sm.addNewSkill(SkillType.BIOLOGY, 1);
        sm.addNewSkill(SkillType.CHEMISTRY, 1);
        var changed1 = sm.getSkill(SkillType.BIOLOGY);
        var changed2 = sm.getSkill(SkillType.CHEMISTRY);

        var origChanged1Exp = changed1.getCumulativeExperience();
        var origChanged2Exp = changed2.getCumulativeExperience();

        double skillsRatio = 1D;
        // Create a 2:1 ration of weights
        Set<SkillWeight> skills = Set.of(new SkillWeight(changed1.getType(), 2),
                                        new SkillWeight(changed2.getType(), 1));
        double stressRatio = 1D; // It is stressful
        ExperienceImpact impact = new ExperienceImpact(skillsRatio, skills,
                                                        NaturalAttributeType.EXPERIENCE_APTITUDE,
                                                        false, stressRatio);

        // Simple apply with no assistance but not effective to generate stress
        impact.apply(p, 100D, 0D, 0.5);

        // Check skills have changed
        var newChanged1Exp = sm.getSkill(changed1.getType()).getCumulativeExperience();
        var newChanged2Exp = sm.getSkill(changed2.getType()).getCumulativeExperience();

        assertGreaterThan("Changed #1 skill",  origChanged1Exp, newChanged1Exp);
        assertGreaterThan("Changed #2 skill",  origChanged2Exp, newChanged2Exp);

        double newStress = p.getPhysicalCondition().getStress();
        assertGreaterThan("Increased stress",  origStress, newStress);
    }

    public void testEffectiveSkill() {
        Settlement s = buildSettlement();
        Person p = buildPerson("Worker #1", s);
        p.getPhysicalCondition().setPerformanceFactor(1D);  // Ensure Person does not disrupt skill

        // Get skills and push them up yo at leace level 1
        var sm = p.getSkillManager();
        var selected1 = sm.getSkills().get(0);
        sm.addExperience(selected1.getType(), selected1.getNeededExp() + 1,10);
        var skill1 = sm.getEffectiveSkillLevel(selected1.getType());

        var selected2 = sm.getSkills().get(1);
        sm.addExperience(selected2.getType(), selected2.getNeededExp() + 1,10);
        var skill2 = sm.getEffectiveSkillLevel(selected2.getType());

        // Balanced
        ExperienceImpact balanced = new ExperienceImpact(0, NaturalAttributeType.EXPERIENCE_APTITUDE,
                                          false, 0, Set.of(selected1.getType(), selected2.getType()));
        var skill = balanced.getEffectiveSkillLevel(p);
        assertEquals("Balanced skill", (skill1 + skill2)/2, skill);

        // Balanced
        Set<SkillWeight> skills = Set.of(new SkillWeight(selected1.getType(), 2),
                                        new SkillWeight(selected2.getType(), 1));
        ExperienceImpact unbalanced = new ExperienceImpact(0, skills,
                                                        NaturalAttributeType.EXPERIENCE_APTITUDE,
                                                        false, 0);
        skill = unbalanced.getEffectiveSkillLevel(p);
        assertEquals("Unalanced skill", ((skill1*2) + skill2)/3, skill);
    }

    public void testPersonNoEffort() {
        Settlement s = buildSettlement();
        Person p = buildPerson("Worker #1", s);
        p.getPhysicalCondition().setPerformanceFactor(1D);  // Ensure Person does not disrupt skill

        var origFatigue = p.getPhysicalCondition().getFatigue();

        // No effort impact
        ExperienceImpact noEffort = new ExperienceImpact(0, NaturalAttributeType.EXPERIENCE_APTITUDE,
                                          false, 0, Collections.emptySet());
        assertFalse("No effort experience", noEffort.isEffortAffected());
        noEffort.apply(p, 10, 1, 1);

        var newFatigue = p.getPhysicalCondition().getFatigue();
        assertEquals("Fatigue unchanged", origFatigue, newFatigue);
    }

    
    public void testPersonEffort() {
        Settlement s = buildSettlement();
        Person p = buildPerson("Worker #1", s);
        p.getPhysicalCondition().setPerformanceFactor(1D);  // Ensure Person does not disrupt skill

        var origEnergy = p.getPhysicalCondition().getEnergy();

        // No effort impact
        ExperienceImpact noEffort = new ExperienceImpact(0, NaturalAttributeType.EXPERIENCE_APTITUDE,
                                          true, 0, Collections.emptySet());
        assertTrue("No effort experience", noEffort.isEffortAffected());
        noEffort.apply(p, 10, 1, 1);

        var newEnergy = p.getPhysicalCondition().getEnergy();
        assertGreaterThan("Energy reduced inceased", newEnergy, origEnergy);
    }
}

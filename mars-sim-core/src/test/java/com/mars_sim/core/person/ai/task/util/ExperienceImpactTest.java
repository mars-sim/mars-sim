package com.mars_sim.core.person.ai.task.util;


import java.util.Set;

import com.mars_sim.core.AbstractMarsSimUnitTest;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.NaturalAttributeType;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.task.util.ExperienceImpact.PhysicalEffort;
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
        double stressRatio = 0D;  // No stress doing this

        ExperienceImpact impact = new ExperienceImpact(skillsRatio,
                                            NaturalAttributeType.EXPERIENCE_APTITUDE,
                                            false, stressRatio,
                                            changed1.getType(), changed2.getType());

        // Simple apply with no assistance
        impact.apply(p, 100D, 1D, 1D);

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

    public void testApplySingleSkill() {
        Settlement s = buildSettlement();
        Person p = buildPerson("Worker #1", s);

        double origStress = p.getPhysicalCondition().getStress();

        // Prepare the skills, make sure 2 to be tested are level at the start
        var sm = p.getSkillManager();
        sm.addNewSkill(SkillType.BIOLOGY, 1);

        var changed1 = sm.getSkill(SkillType.BIOLOGY);

        var origChanged1Exp = changed1.getCumulativeExperience();

        var unchanged = sm.getSkills().get(2);
        var origUnChangedExp = unchanged.getCumulativeExperience();


        double skillsRatio = 1D;
        double stressRatio = 0D;  // No stress doing this

        ExperienceImpact impact = new ExperienceImpact(skillsRatio,
                                            NaturalAttributeType.EXPERIENCE_APTITUDE,
                                            false, stressRatio, SkillType.BIOLOGY);

        // Simple apply with no assistance
        impact.apply(p, 100D, 1D, 1D);

        // Check skills have changed
        var newChanged1Exp = sm.getSkill(changed1.getType()).getCumulativeExperience();
        var newUnChangedExp = sm.getSkill(unchanged.getType()).getCumulativeExperience();

        assertEquals("Unchanged skill",  origUnChangedExp, newUnChangedExp);
        assertGreaterThan("Changed #1 skill",  origChanged1Exp, newChanged1Exp);

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
        ExperienceImpact impact = new ExperienceImpact(skillsRatio, NaturalAttributeType.EXPERIENCE_APTITUDE,
                                                        false, stressRatio, skills);

        // Simple apply with no assistance but not effective to generate stress
        impact.apply(p, 100D, 1D, 0.5);

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
                                          false, 0,
                                          selected1.getType(), selected2.getType());
        var skill = balanced.getEffectiveSkillLevel(p);
        assertEquals("Balanced skill", (skill1 + skill2)/2, skill);

        // Balanced
        Set<SkillWeight> skills = Set.of(new SkillWeight(selected1.getType(), 2),
                                        new SkillWeight(selected2.getType(), 1));
        ExperienceImpact unbalanced = new ExperienceImpact(0, NaturalAttributeType.EXPERIENCE_APTITUDE,
                                                        false, 0, skills);
        skill = unbalanced.getEffectiveSkillLevel(p);
        assertEquals("Unalanced skill", ((skill1*2) + skill2)/3, skill);
    }

    public void testPersonNoEffort() {
        Settlement s = buildSettlement();
        Person p = buildPerson("Worker #1", s);
        p.getPhysicalCondition().setPerformanceFactor(1D);  // Ensure Person does not disrupt skill

        var origFatigue = p.getPhysicalCondition().getFatigue();

        // No effort impact
        ExperienceImpact noEffort = new ExperienceImpact(0, null,
                                          PhysicalEffort.NONE, 0);
        assertEquals("No effort experience", PhysicalEffort.NONE, noEffort.getEffortRequired());
        noEffort.apply(p, 10, 1, 1);

        var newFatigue = p.getPhysicalCondition().getFatigue();
        assertEquals("Fatigue unchanged", origFatigue, newFatigue);
    }
    
    public void testPersonLowEffort() {
        Settlement s = buildSettlement();
        Person p = buildPerson("Worker #1", s);
        p.getPhysicalCondition().setPerformanceFactor(1D);  // Ensure Person does not disrupt skill

        var origEnergy = p.getPhysicalCondition().getEnergy();

        // No effort impact
        ExperienceImpact noEffort = new ExperienceImpact(0, NaturalAttributeType.EXPERIENCE_APTITUDE,
                                          PhysicalEffort.LOW, 0);
        assertEquals("Low effort experience", PhysicalEffort.LOW, noEffort.getEffortRequired());
        noEffort.apply(p, 10, 1, 1);

        var newEnergy = p.getPhysicalCondition().getEnergy();
        assertGreaterThan("Energy reduced inceased", newEnergy, origEnergy);
    }

    public void testPersonHighEffort() {
        Settlement s = buildSettlement();
        Person p = buildPerson("Worker #1", s);
        var cond = p.getPhysicalCondition();
        cond.setPerformanceFactor(1D);  // Ensure Person does not disrupt skill

        var origEnergy = cond.getEnergy();
        var origSoreness = cond.getMuscleSoreness();
        var origHealth = cond.getMuscleHealth();

        // No effort impact
        ExperienceImpact noEffort = new ExperienceImpact(0, NaturalAttributeType.EXPERIENCE_APTITUDE,
                                          PhysicalEffort.HIGH, 0);
        assertEquals("High effort experience", PhysicalEffort.HIGH, noEffort.getEffortRequired());

        noEffort.apply(p, 10, 1, 1);

        assertLessThan("Energy reduced", origEnergy, cond.getEnergy());
        assertGreaterThan("Muscle soreness inceased", origSoreness, cond.getMuscleSoreness());
        assertLessThan("Muscle health decreased", origHealth, cond.getMuscleHealth());
    }

    public void testConstructors() {
        ExperienceImpact effort = new ExperienceImpact(0, NaturalAttributeType.EXPERIENCE_APTITUDE,
                true, 0);
        assertEquals("Low effort experience", PhysicalEffort.LOW, effort.getEffortRequired());

        ExperienceImpact noEffort = new ExperienceImpact(0, NaturalAttributeType.EXPERIENCE_APTITUDE,
            false, 0);
        assertEquals("No effort experience", PhysicalEffort.NONE, noEffort.getEffortRequired());
    }

}

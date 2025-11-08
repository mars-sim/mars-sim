package com.mars_sim.core.person.ai.task.util;
import static com.mars_sim.core.test.SimulationAssertions.assertGreaterThan;
import static com.mars_sim.core.test.SimulationAssertions.assertLessThan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;


import java.util.Set;

import com.mars_sim.core.test.MarsSimUnitTest;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.NaturalAttributeType;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.task.util.ExperienceImpact.PhysicalEffort;
import com.mars_sim.core.person.ai.task.util.ExperienceImpact.SkillWeight;
import com.mars_sim.core.structure.Settlement;

public class ExperienceImpactTest extends MarsSimUnitTest {

    @Test
    public void testApplyBasicSkill() {
        Settlement s = buildSettlement("mock");
        Person p = buildPerson("Worker #1", s);

        double origStress = p.getPhysicalCondition().getStress();

        // Prepare the skills, make sure 2 to be tested are level at the start
        var sm = p.getSkillManager();
        sm.addNewSkill(SkillType.ASTROBIOLOGY, 1);
        sm.addNewSkill(SkillType.CHEMISTRY, 1);

        var changed1 = sm.getSkill(SkillType.ASTROBIOLOGY);
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

        assertEquals(origUnChangedExp, newUnChangedExp, "Unchanged skill");
        assertGreaterThan("Changed #1 skill",  origChanged1Exp, newChanged1Exp);
        assertGreaterThan("Changed #2 skill",  origChanged2Exp, newChanged2Exp);

        double newStress = p.getPhysicalCondition().getStress();
        assertEquals(origStress, newStress, "Unchanged stress");

    }

    @Test
    public void testApplySingleSkill() {
        Settlement s = buildSettlement("mock");
        Person p = buildPerson("Worker #1", s);

        double origStress = p.getPhysicalCondition().getStress();

        // Prepare the skills, make sure 2 to be tested are level at the start
        var sm = p.getSkillManager();
        sm.addNewSkill(SkillType.ASTROBIOLOGY, 1);

        var changed1 = sm.getSkill(SkillType.ASTROBIOLOGY);

        var origChanged1Exp = changed1.getCumulativeExperience();

        var unchanged = sm.getSkills().get(2);
        var origUnChangedExp = unchanged.getCumulativeExperience();


        double skillsRatio = 1D;
        double stressRatio = 0D;  // No stress doing this

        ExperienceImpact impact = new ExperienceImpact(skillsRatio,
                                            NaturalAttributeType.EXPERIENCE_APTITUDE,
                                            false, stressRatio, SkillType.ASTROBIOLOGY);

        // Simple apply with no assistance
        impact.apply(p, 100D, 1D, 1D);

        // Check skills have changed
        var newChanged1Exp = sm.getSkill(changed1.getType()).getCumulativeExperience();
        var newUnChangedExp = sm.getSkill(unchanged.getType()).getCumulativeExperience();

        assertEquals(origUnChangedExp, newUnChangedExp, "Unchanged skill");
        assertGreaterThan("Changed #1 skill",  origChanged1Exp, newChanged1Exp);

        double newStress = p.getPhysicalCondition().getStress();
        assertEquals(origStress, newStress, "Unchanged stress");

    }

    @Test
    public void testApplyUnbalancedSkill() {
        Settlement s = buildSettlement("mock");
        Person p = buildPerson("Worker #1", s);

        double origStress = p.getPhysicalCondition().getStress();

        // Prepare the skills
        var sm = p.getSkillManager();
        sm.addNewSkill(SkillType.ASTROBIOLOGY, 1);
        sm.addNewSkill(SkillType.CHEMISTRY, 1);
        var changed1 = sm.getSkill(SkillType.ASTROBIOLOGY);
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

    @Test
    public void testEffectiveSkill() {
        Settlement s = buildSettlement("mock");
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
        assertEquals((skill1 + skill2)/2, skill, "Balanced skill");

        // Balanced
        Set<SkillWeight> skills = Set.of(new SkillWeight(selected1.getType(), 2),
                                        new SkillWeight(selected2.getType(), 1));
        ExperienceImpact unbalanced = new ExperienceImpact(0, NaturalAttributeType.EXPERIENCE_APTITUDE,
                                                        false, 0, skills);
        skill = unbalanced.getEffectiveSkillLevel(p);
        assertEquals(((skill1*2) + skill2)/3, skill, "Unalanced skill");
    }

    @Test
    public void testPersonNoEffort() {
        Settlement s = buildSettlement("mock");
        Person p = buildPerson("Worker #1", s);
        p.getPhysicalCondition().setPerformanceFactor(1D);  // Ensure Person does not disrupt skill

        var origFatigue = p.getPhysicalCondition().getFatigue();

        // No effort impact
        ExperienceImpact noEffort = new ExperienceImpact(0, null,
                                          PhysicalEffort.NONE, 0);
        assertEquals(PhysicalEffort.NONE, noEffort.getEffortRequired(), "No effort experience");
        noEffort.apply(p, 10, 1, 1);

        var newFatigue = p.getPhysicalCondition().getFatigue();
        assertEquals(origFatigue, newFatigue, "Fatigue unchanged");
    }
    
    @Test
    public void testPersonLowEffort() {
        Settlement s = buildSettlement("mock");
        Person p = buildPerson("Worker #1", s);
        p.getPhysicalCondition().setPerformanceFactor(1D);  // Ensure Person does not disrupt skill

        var origEnergy = p.getPhysicalCondition().getEnergy();

        // No effort impact
        ExperienceImpact noEffort = new ExperienceImpact(0, NaturalAttributeType.EXPERIENCE_APTITUDE,
                                          PhysicalEffort.LOW, 0);
        assertEquals(PhysicalEffort.LOW, noEffort.getEffortRequired(), "Low effort experience");
        noEffort.apply(p, 10, 1, 1);

        var newEnergy = p.getPhysicalCondition().getEnergy();
        assertGreaterThan("Energy reduced inceased", newEnergy, origEnergy);
    }

    @Test
    public void testPersonHighEffort() {
        Settlement s = buildSettlement("mock");
        Person p = buildPerson("Worker #1", s);
        var cond = p.getPhysicalCondition();
        cond.setPerformanceFactor(1D);  // Ensure Person does not disrupt skill

        var origEnergy = cond.getEnergy();
        var origSoreness = cond.getMuscleSoreness();
        var origHealth = cond.getMuscleHealth();

//        System.out.println("origSoreness: " + origSoreness);
        // No effort impact
        ExperienceImpact noEffort = new ExperienceImpact(0, NaturalAttributeType.EXPERIENCE_APTITUDE,
                                          PhysicalEffort.HIGH, 0);
        assertEquals(PhysicalEffort.HIGH, noEffort.getEffortRequired(), "High effort experience");

        noEffort.apply(p, 10, 1, 1);

//        System.out.println("new Soreness: " + cond.getMuscleSoreness());
        
        assertLessThan("Energy reduced", origEnergy, cond.getEnergy());
        assertGreaterThan("Muscle soreness inceased", origSoreness, cond.getMuscleSoreness());
        assertLessThan("Muscle health decreased", origHealth, cond.getMuscleHealth());
    }

    @Test
    public void testConstructors() {
        ExperienceImpact effort = new ExperienceImpact(0, NaturalAttributeType.EXPERIENCE_APTITUDE,
                true, 0);
        assertEquals(PhysicalEffort.LOW, effort.getEffortRequired(), "Low effort experience");

        ExperienceImpact noEffort = new ExperienceImpact(0, NaturalAttributeType.EXPERIENCE_APTITUDE,
            false, 0);
        assertEquals(PhysicalEffort.NONE, noEffort.getEffortRequired(), "No effort experience");
    }

    @Test
    public void testEffort() {
        assertEffort(PhysicalEffort.HIGH, PhysicalEffort.LOW);
        assertEffort(PhysicalEffort.HIGH, PhysicalEffort.NONE);
        assertEffort(PhysicalEffort.LOW, PhysicalEffort.NONE);

        assertFalse(ExperienceImpact.isEffortHigher(PhysicalEffort.HIGH,
                             PhysicalEffort.HIGH), "Physical effort HIGH == HIGH");
        assertFalse(ExperienceImpact.isEffortHigher(PhysicalEffort.LOW,
                             PhysicalEffort.LOW), "Physical effort LOW == LOW");
        assertFalse(ExperienceImpact.isEffortHigher(PhysicalEffort.NONE,
                             PhysicalEffort.NONE), "Physical effort NONE == NONE");

    }

    private void assertEffort(PhysicalEffort higher, PhysicalEffort lower) {
        assertTrue(ExperienceImpact.isEffortHigher(higher, lower), "Physical effort " + higher.name() + " > " + lower.name());
        assertFalse(ExperienceImpact.isEffortHigher(lower, higher), "Physical effort " + lower.name() + " > " + higher.name());
    }
}

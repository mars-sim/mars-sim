/*
 * Mars Simulation Project
 * OffDutyWellbeingMetaTask.java
 * @date 2025-08-27
 * Adds a gentle "rest / hydrate / stroll" cadence during OFF_DUTY / ON_LEAVE / ON_CALL.
 */
package com.mars_sim.core.person.ai.task.util;

import java.util.ArrayList;
import java.util.List;

import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.shift.ShiftSlot.WorkStatus;
import com.mars_sim.core.person.ai.task.EatDrink;
import com.mars_sim.core.person.ai.task.Sleep;
import com.mars_sim.core.person.ai.task.Walk;

/**
 * A simple FactoryMetaTask that injects well-being activities off shift.
 * - When OFF_DUTY / ON_LEAVE: offer Sleep (high), EatDrink (medium), Walk (low)
 * - When ON_CALL: offer EatDrink (low) and Walk (very low), avoiding long Sleep blocks
 *
 * This complements existing metas and reduces reliance on fallback defaults,
 * yielding a more believable off-duty rhythm without touching core logic.
 */
public final class OffDutyWellbeingMetaTask implements FactoryMetaTask {

    private static final SimLogger LOG =
            SimLogger.getLogger(OffDutyWellbeingMetaTask.class.getName());

    private static final String NAME = "OffDutyWellbeing";

    // Tunable base weights (0..1). These are deliberately modest: they "suggest" rather than force.
    private static final double SLEEP_WEIGHT_OFFDUTY = 0.90;
    private static final double EAT_WEIGHT_OFFDUTY   = 0.60;
    private static final double WALK_WEIGHT_OFFDUTY  = 0.30;

    private static final double EAT_WEIGHT_ONCALL    = 0.35;
    private static final double WALK_WEIGHT_ONCALL   = 0.15;

    @Override
    public String getName() {
        return NAME;
    }

    /**
     * Produces a small set of TaskJobs appropriate for off-duty contexts.
     * Returns empty when the person is ON_DUTY.
     */
    @Override
    public List<TaskJob> getTaskJobs(Person person) {

        final List<TaskJob> out = new ArrayList<>(3);
        final WorkStatus status = person.getShiftSlot().getStatus();

        // On duty? This meta does not apply.
        if (status == WorkStatus.ON_DUTY) {
            return out;
        }

        // If outside, prefer moving rather than starting indoor tasks.
        final boolean outside = person.isOutside();

        // OFF_DUTY / ON_LEAVE
        if (status == WorkStatus.OFF_DUTY || status == WorkStatus.ON_LEAVE) {

            if (!outside) {
                // High: Sleep block (lets the main loop pick a real Sleep task organically)
                out.add(new AbstractTaskJob("Sleep", new RatingScore(SLEEP_WEIGHT_OFFDUTY)) {
                    private static final long serialVersionUID = 1L;
                    @Override public Task createTask(Person p) { return new Sleep(p); }
                });

                // Medium: Eat/drink & hydrate
                out.add(new AbstractTaskJob("Eat/Drink", new RatingScore(EAT_WEIGHT_OFFDUTY)) {
                    private static final long serialVersionUID = 1L;
                    @Override public Task createTask(Person p) { return new EatDrink(p); }
                });
            }

            // Low: Stretch legs / short stroll (works both inside corridors & outside to head back)
            out.add(new AbstractTaskJob("Walk", new RatingScore(WALK_WEIGHT_OFFDUTY)) {
                private static final long serialVersionUID = 1L;
                @Override public Task createTask(Person p) { return new Walk(p); }
            });

            LOG.fine(person, 5_000L, "Offering OffDuty wellbeing TaskJobs.");
            return out;
        }

        // ON_CALL: keep it light, avoid long sleep blocks
        if (status == WorkStatus.ON_CALL) {
            if (!outside) {
                out.add(new AbstractTaskJob("Eat/Drink", new RatingScore(EAT_WEIGHT_ONCALL)) {
                    private static final long serialVersionUID = 1L;
                    @Override public Task createTask(Person p) { return new EatDrink(p); }
                });
            }
            out.add(new AbstractTaskJob("Walk", new RatingScore(WALK_WEIGHT_ONCALL)) {
                private static final long serialVersionUID = 1L;
                @Override public Task createTask(Person p) { return new Walk(p); }
            });

            LOG.fine(person, 5_000L, "Offering On-Call light wellbeing TaskJobs.");
        }

        return out;
    }
}

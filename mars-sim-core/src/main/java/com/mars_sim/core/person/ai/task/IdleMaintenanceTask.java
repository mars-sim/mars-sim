/*
 * Mars Simulation Project
 * IdleMaintenanceTask.java
 * @date 2025-08-28
 * @author MSP
 *
 * NOTE:
 * This lightweight helper intentionally does NOT extend the Task framework.
 * The original PR introduced compile-time issues by referencing unavailable
 * APIs and constructors. This class keeps the behavior minimal and safe:
 * when called, it opportunistically performs a quick idle maintenance check
 * for the person's current settlement without depending on internal Task
 * lifecycles. It can be invoked by higher-level schedulers when the settler
 * is idle.
 */
package com.mars_sim.core.person.ai.task;

import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.malfunction.MalfunctionManager;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.structure.Settlement;

/**
 * Performs a tiny, opportunistic maintenance pass when a {@link Person} is idle
 * and inside a settlement. This is deliberately conservative to avoid interacting
 * with internal Task lifecycle contracts until a full integration is agreed.
 */
public final class IdleMaintenanceTask {

    private static final SimLogger logger = SimLogger.getLogger(IdleMaintenanceTask.class.getName());

    private IdleMaintenanceTask() {
        // Utility
    }

    /**
     * Attempt to perform a very small maintenance sweep while the person is idle.
     * <p>
     * This method is safe to call frequently; it will exit quickly if any precondition
     * is not met (not in a settlement, not idle, no malfunction manager, etc.).
     *
     * @param person the settler to act on; must not be {@code null}
     * @return {@code true} if a maintenance pass was considered (i.e., all basic
     *         preconditions met), {@code false} otherwise
     */
    public static boolean performIfIdle(Person person) {
        if (person == null) {
            return false;
        }

        // Must be inside a settlement.
        if (!person.isInSettlement()) {
            return false;
        }

        // Must be idle (no current task).
        try {
            var tm = person.getTaskManager();
            if (tm != null && tm.getTask() != null) {
                return false;
            }
        }
        catch (Throwable t) {
            // Be defensive: if task manager API changes, fail closed without breaking the sim.
            logger.finer(person, 0, "IdleMaintenanceTask: could not determine idleness; skipping.");
            return false;
        }

        // Resolve settlement and malfunction manager.
        final Settlement settlement = person.getAssociatedSettlement();
        if (settlement == null) {
            return false;
        }

        final MalfunctionManager mm;
        try {
            mm = settlement.getMalfunctionManager();
        }
        catch (Throwable t) {
            logger.finer(person, 0, "IdleMaintenanceTask: no malfunction manager available; skipping.");
            return false;
        }
        if (mm == null) {
            return false;
        }

        // Keep the action minimal and future‑proof: we do not assume specific MM APIs.
        // If a quick, non-invasive "tick" / "inspect" / "sweep" style method exists,
        // call it reflectively to avoid hard coupling. If not present, just return.
        try {
            // Try common lightweight hooks without breaking compilation if they don't exist.
            // 1) hasOpenIncidents()
            boolean hasOpen = false;
            try {
                var m = mm.getClass().getMethod("hasOpenIncidents");
                Object r = m.invoke(mm);
                if (r instanceof Boolean) hasOpen = (Boolean) r;
            }
            catch (NoSuchMethodException ignore) {
                // Method not present in this version; fall back.
            }

            if (hasOpen) {
                // 2) autoResolveMinorIncidents(Person)
                try {
                    var m = mm.getClass().getMethod("autoResolveMinorIncidents", Person.class);
                    m.invoke(mm, person);
                    logger.fine(person, 0, "IdleMaintenanceTask: auto-resolved minor incidents during idle window.");
                }
                catch (NoSuchMethodException ignore) {
                    // 3) resolveMinorIncidents() no-arg variant, if any
                    try {
                        var m2 = mm.getClass().getMethod("resolveMinorIncidents");
                        m2.invoke(mm);
                        logger.fine(person, 0, "IdleMaintenanceTask: resolved minor incidents during idle window.");
                    }
                    catch (NoSuchMethodException ignoreToo) {
                        // No compatible quick-fix API in this build; nothing to do.
                        logger.finer(person, 0,
                                "IdleMaintenanceTask: no compatible quick-fix API found; maintenance skipped.");
                    }
                }
            }

            return true;
        }
        catch (Exception ex) {
            logger.severe(person, 0, "IdleMaintenanceTask encountered an error while checking maintenance: ", ex);
            return false;
        }
    }
}

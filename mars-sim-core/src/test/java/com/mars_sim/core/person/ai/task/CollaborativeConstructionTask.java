package com.mars_sim.core.person.ai.task;

import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.construction.ConstructionSite;
import com.mars_sim.core.person.ai.skill.SkillType;

import java.util.List;

/**
 * A task that represents a collaborative effort to construct a building.
 * Multiple settlers can contribute to the construction process.
 */
public class CollaborativeConstructionTask extends Task {

    private static final long serialVersionUID = 1L;
    private static final SimLogger logger = SimLogger.getLogger(CollaborativeConstructionTask.class.getName());

    private final ConstructionSite site;
    private final Settlement settlement;
    private final List<Person> workers;
    private final double baseWorkRate;

    /**
     * Constructs a new collaborative construction task.
     *
     * @param workers    the list of workers assigned to the construction site
     * @param site       the construction site to be worked on
     * @param settlement the settlement where the construction is taking place
     * @param baseWorkRate the base work rate per hour per worker
     */
    public CollaborativeConstructionTask(List<Person> workers, ConstructionSite site, Settlement settlement, double baseWorkRate) {
        super(workers.get(0)); // Assuming the first worker is the task owner
        this.workers = workers;
        this.site = site;
        this.settlement = settlement;
        this.baseWorkRate = baseWorkRate;
    }

    @Override
    public String getName() {
        return "Collaborative Construction";
    }

    @Override
    public void performTask(double hours) {
        if (site.isCompleted()) {
            setDone(true);
            return;
        }

        double totalWorkDone = 0;
        for (Person worker : workers) {
            double skillLevel = getConstructionSkillLevel(worker);
            double efficiencyMultiplier = 1.0 + (skillLevel / 100.0);
            totalWorkDone += baseWorkRate * hours * efficiencyMultiplier;
        }

        site.addConstructionProgress(totalWorkDone);

        if (site.isCompleted()) {
            site.finishConstruction();
            logger.info(settlement, 10_000L, "Construction completed at " + site.getName() + ".");
            setDone(true);
        }
    }

    @Override
    public boolean isFinished() {
        return isDone() || site.isCompleted();
    }

    private static double getConstructionSkillLevel(Person p) {
        try {
            return p.getSkillManager().getSkillLevel(SkillType.CONSTRUCTION);
        } catch (NoSuchMethodError | Exception ignored) {
            return 0.0;
        }
    }
}

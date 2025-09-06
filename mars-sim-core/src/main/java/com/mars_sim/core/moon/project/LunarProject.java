/* 
 * Mars Simulation Project
 * LunarProject.java
 * @date 2024-02-15 (revised)
 * @author Manny Kung
 */
package com.mars_sim.core.moon.project;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.moon.Colonist;
import com.mars_sim.core.science.ScienceType;
import com.mars_sim.core.tool.RandomUtil;

/**
 * Represents a lunar research/engineering project in the simulation.
 * <p>Thread-safety: not thread-safe; callers should synchronize externally if needed.</p>
 */
public class LunarProject implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Kept public for backward compatibility. */
    public static final SimLogger logger = SimLogger.getLogger(LunarProject.class.getName());

    /** Maximum number of non-lead participants permitted for this project. */
    private final int maxNumParticipants;

    /** Accumulated "value" produced by this project. */
    private double value;

    /** Project name. */
    private final String name;

    /** Science domain of this project. */
    private final ScienceType science;

    /** The lead colonist. */
    private final Colonist lead;

    /** Major topics covered by this project. */
    private final List<String> topics;

    /** Participating colonists (excluding the lead). */
    private final Set<Colonist> participants;

    /**
     * Package-private constructor kept exactly (signature/visibility) for compatibility.
     */
    LunarProject(Colonist lead, String name, ScienceType science) {
        this(lead, name, science, RandomUtil.getRandomInt(3, 10));
    }

    /**
     * Additional constructor to allow deterministic capacity in tests/sim configs.
     * Visibility kept package-private to match the original constructor's intent.
     */
    LunarProject(Colonist lead, String name, ScienceType science, int maxNumParticipants) {
        this.lead = Objects.requireNonNull(lead, "lead");
        this.name = Objects.requireNonNull(name, "name");
        this.science = Objects.requireNonNull(science, "science");
        if (maxNumParticipants < 1) {
            throw new IllegalArgumentException("maxNumParticipants must be >= 1");
        }
        this.maxNumParticipants = maxNumParticipants;

        this.participants = new HashSet<>();
        this.topics = new ArrayList<>();
    }

    /**
     * Adds a participant if capacity allows; no-ops if null, the same as lead, or full.
     * (Void return preserved for compatibility.)
     */
    public void addParticipant(Colonist participant) {
        if (participant == null || participant.equals(lead)) {
            return;
        }
        if (!canAddParticipants()) {
            return;
        }
        participants.add(participant);
    }

    /** The lead colonist. */
    public Colonist getLead() {
        return lead;
    }

    /**
     * Returns the live set of participants (kept mutable for backward compatibility).
     * Prefer using {@link #addParticipant(Colonist)} to modify membership.
     */
    public Set<Colonist> getParticipants() {
        return participants;
    }

    /** True if another participant can be added (fixed logic). */
    public boolean canAddParticipants() {
        return getNumParticipants() < maxNumParticipants;
    }

    /** Current number of non-lead participants. */
    public int getNumParticipants() {
        return participants.size();
    }

    /** Adds a randomly selected topic for this project's science domain. */
    public void addTopic() {
        // ScienceConfig#getATopic returns a String. See ScienceConfig.java.
        String topic = SimulationConfig.instance()
                                       .getScienceConfig()
                                       .getATopic(science);
        if (topic != null && !topic.isEmpty()) {
            topics.add(topic);
        }
    }

    /** Adds (positive or negative) value to the project. */
    public void addValue(double value) {
        this.value += value;
    }

    /** Total accumulated value. */
    public double getValue() {
        return this.value;
    }

    /** Project name. */
    public String getName() {
        return name;
    }

    /** Project science domain. */
    public ScienceType getScience() {
        return science;
    }

    /** Maximum allowed participant count (excluding lead). */
    public int getMaxNumParticipants() {
        return maxNumParticipants;
    }

    /** Topics accumulated so far (mutable list for now to avoid breaking callers if they rely on mutation). */
    public List<String> getTopics() {
        return topics;
    }

    @Override
    public String toString() {
        return "LunarProject{name='" + name + "', science=" + science
                + ", lead=" + (lead != null ? lead.getName() : "null")
                + ", participants=" + participants.size()
                + ", value=" + value + "}";
    }
}

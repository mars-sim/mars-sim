/*
 * Mars Simulation Project
 * AverageTimer.java
 * @date 2024-10-11
 * @author Barry Evans
 */
package com.mars_sim.core.tool;

import com.mars_sim.core.logging.SimLogger;

/**
 * This class acts as a timer that maintans a running avrage. It also produces
 * an average over the most recent runs.
 * Each run is scoped by calls to teh startTimer and stopTimer methods.
 */
public class AverageTimer {
    private static SimLogger logger = SimLogger.getLogger(AverageTimer.class.getName());

    private long totalTime;
    private int totalRuns;
    private long recentTime;
    private int recentQuota;

    private String name;
    private long startTime;

    public AverageTimer(String name, int recentQuota) {
        this.recentQuota = recentQuota;
        this.name = name;
    }

    /**
     * Start the timer.
     */
    public void startTimer() {
        startTime = System.currentTimeMillis();
    }

    /**
     * Stop any active timer. Add the elapsed time to the recent and total times.
     * If the number of recent class matches the quota; then the output stats are generated.
     */
    public void stopTimer() {
        if ((recentQuota <= 0) || (startTime < 0)) {
            return;
        }

        long elapsed = System.currentTimeMillis() - startTime;

        totalTime += elapsed;
        recentTime += elapsed;
        totalRuns++;
        startTime = -1;

        // Reached quota so output stats
        if ((totalRuns % recentQuota) == 0) {
            logger.info(name + " ============================================");
            logger.info("Recent ave " + (recentTime/recentQuota) + "ms over last " + recentQuota
                        + " Total ave " + (totalTime/totalRuns) + "ms over " + totalRuns);
            recentTime = 0L;
        }
    }
}

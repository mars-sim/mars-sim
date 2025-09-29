/*
 * Mars Simulation Project
 * RunnerData.java
 * @date 2024-08-10
 * @author Manny Kung
 */

package com.mars_sim.ui.swing.terminal;

import java.util.Collections;
import java.util.Map;

class RunnerData {
    private final String initData;
    private Map<String, String> sessionData = Collections.emptyMap();

    public RunnerData(String initData) {
        this.initData = initData;
    }

    public String getInitData() {
        return initData;
    }

    public Map<String, String> getSessionData() {
        return sessionData;
    }
    
    public void setSessionData(Map<String, String> sessionData) {
        this.sessionData = sessionData;
    }
}

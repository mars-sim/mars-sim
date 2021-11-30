/*
 * Mars Simulation Project
 * AppUtil.java
 * @date 2021-11-29
 * @author Manny Kung
 */

package org.mars.sim.console;

import org.beryx.textio.TextTerminal;

public class AppUtil {

	private AppUtil() {}

    public static void printGsonMessage(TextTerminal<?> terminal, String initData) {
        if (initData != null && !initData.isEmpty()) {
              terminal.println(initData);
        }
    }
}

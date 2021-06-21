/**
 * Mars Simulation Project
 * AppUtil.java
 * @version 3.1.0 2018-09-24
 * @author Manny Kung
 */

package org.mars.sim.console;

import org.beryx.textio.TextTerminal;

public class AppUtil {
    public static void printGsonMessage(TextTerminal<?> terminal, String initData) {
        if(initData != null && !initData.isEmpty()) {
            String message = initData;
            if(message != null && !message.isEmpty()) {
                terminal.println(message);
            }
        }
    }
}

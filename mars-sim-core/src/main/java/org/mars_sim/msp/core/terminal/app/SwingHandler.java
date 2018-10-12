/*
 * Copyright 2018 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mars_sim.msp.core.terminal.app;

import org.apache.commons.lang3.StringUtils;
import org.beryx.textio.ReadHandlerData;
import org.beryx.textio.ReadInterruptionStrategy;
import org.beryx.textio.TextIO;
import org.beryx.textio.swing.SwingTextTerminal;

import java.time.Month;
import java.util.stream.Stream;

public class SwingHandler {
    private static final String KEY_STROKE_UP = "pressed UP";
    private static final String KEY_STROKE_DOWN = "pressed DOWN";

    private final SwingTextTerminal terminal;

    private String originalInput = "";
    private int choiceIndex = -1;
    private String[] choices = {};


    public SwingHandler(SwingTextTerminal terminal) {
        this.terminal = terminal;
        terminal.registerHandler(KEY_STROKE_UP, t -> {
            if(choiceIndex < 0) {
                originalInput = terminal.getPartialInput();
            }
            if(choiceIndex < choices.length - 1) {
                choiceIndex++;
                t.replaceInput(choices[choiceIndex], false);
            }
            return new ReadHandlerData(ReadInterruptionStrategy.Action.CONTINUE);
        });

        terminal.registerHandler(KEY_STROKE_DOWN, t -> {
            if(choiceIndex >= 0) {
                choiceIndex--;
                String text = (choiceIndex < 0) ? originalInput : choices[choiceIndex];
                t.replaceInput(text, false);
            }
            return new ReadHandlerData(ReadInterruptionStrategy.Action.CONTINUE);
        });
    }

    public void setChoices(String... choices) {
        this.originalInput = "";
        this.choiceIndex = -1;
        this.choices = choices;
    }

    public static void main(String[] args) {
        SwingTextTerminal terminal = new SwingTextTerminal();
        terminal.init();
        TextIO textIO = new TextIO(terminal);
        SwingHandler handler = new SwingHandler(terminal);

        terminal.println("-------------------------------------------------------------------------");
        terminal.println("|   You can use the up and down arrow keys to scroll through choices.   |");
        terminal.println("-------------------------------------------------------------------------\n");

        handler.setChoices("albert", "alice", "ava", "betty", "cathy");
        String player = textIO.newStringInputReader().read("Player name");

        // No choices here
        handler.setChoices();
        int points = textIO.newIntInputReader().read("Total points");

        String[] monthNames = Stream.of(Month.values())
                .map(Month::name)
                .map(String::toLowerCase)
                .map(StringUtils::capitalize)
                .toArray(String[]::new);
        handler.setChoices(monthNames);
        String month = textIO.newStringInputReader()
                .withInlinePossibleValues(monthNames)
                .withIgnoreCase()
                .withPromptAdjustments(false)
                .read("Month: ");

        terminal.printf("\nHello, %s! You earned %d points in %s.\n\n", player, points, month);

        textIO.newStringInputReader().withMinLength(0).read("\nPress enter to terminate...");
        textIO.dispose();
    }
}

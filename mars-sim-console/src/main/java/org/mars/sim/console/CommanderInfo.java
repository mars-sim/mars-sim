/*
 * Mars Simulation Project
 * CommanderInfo.java
 * @date 2021-11-29
 * @author Manny Kung
 */

package org.mars.sim.console;

import static org.beryx.textio.ReadInterruptionStrategy.Action.ABORT;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.beryx.textio.ReadAbortedException;
import org.beryx.textio.ReadHandlerData;
import org.beryx.textio.TextIO;
import org.beryx.textio.TextIoFactory;
import org.beryx.textio.TextTerminal;

/**
 * Illustrates how to use read handlers to allow going back to a previous field.
 */
public class CommanderInfo implements BiConsumer<TextIO, RunnerData> {
    private static class Contact {
        String firstName;
        String lastName;
        String gender;
        String age;
        String career;
        String sponsor;
        String country;

        @Override
        public String toString() {
            return "\n\tfirstName: " + firstName +
                    "\n\tlastName: " + lastName +
                    "\n\tgender: " + gender +
                    "\n\tage: " + age +
                    "\n\tcareer: " + career +
                    "\n\tsponsor: " + sponsor +
                    "\n\tcountry: " + country;
        }
    }

    private final Contact contact = new Contact();

    private final List<Runnable> operations = new ArrayList<>();

    public static void main(String[] args) {
        TextIO textIO = TextIoFactory.getTextIO();
        new CommanderInfo().accept(textIO, null);
    }

    @Override
    public void accept(TextIO textIO, RunnerData runnerData) {
        TextTerminal<?> terminal = textIO.getTextTerminal();
        String initData = (runnerData == null) ? null : runnerData.getInitData();
        AppUtil.printGsonMessage(terminal, initData);

        addTask(textIO, "First name", () -> contact.firstName, s -> contact.firstName = s);
        addTask(textIO, "Last name", () -> contact.lastName, s -> contact.lastName = s);
        addTask(textIO, "Gender", () -> contact.gender, s -> contact.gender = s);
        addTask(textIO, "Age", () -> contact.age, s -> contact.age = s);
        addTask(textIO, "Career", () -> contact.career, s -> contact.career = s);
        addTask(textIO, "Sponsor", () -> contact.sponsor, s -> contact.sponsor = s);
        addTask(textIO, "Country", () -> contact.country, s -> contact.country = s);

        String backKeyStroke = "ctrl U";
        boolean registered = terminal.registerHandler(backKeyStroke, t -> new ReadHandlerData(ABORT));
        if(registered) {
            terminal.println("During the data entry, you can press '" + backKeyStroke + "' to go back to the previous field.\n");
        }
        int step = 0;
        while(step < operations.size()) {
            terminal.setBookmark("bookmark_" + step);
            try {
                operations.get(step).run();
            } catch (ReadAbortedException e) {
                if(step > 0) step--;
                terminal.resetToBookmark("bookmark_" + step);
                continue;
            }
            step++;
        }

        terminal.println("\n----------- Commander's Profile -----------" + contact);

        textIO.newStringInputReader().withMinLength(0).read("\nPress enter to terminate...");
        textIO.dispose();
    }

    private void addTask(TextIO textIO, String prompt, Supplier<String> defaultValueSupplier, Consumer<String> valueSetter) {
        operations.add(() -> valueSetter.accept(textIO.newStringInputReader()
                .withDefaultValue(defaultValueSupplier.get())
                .read(prompt)));
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ": reading the Commander's Profile.\n" +
                "(Illustrates how to use read handlers to allow going back to a previous field.)";
    }
}

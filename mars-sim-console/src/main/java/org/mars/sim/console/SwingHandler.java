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
package org.mars.sim.console;

import static org.beryx.textio.ReadInterruptionStrategy.Action.ABORT;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.beryx.textio.InputReader;
import org.beryx.textio.ReadAbortedException;
import org.beryx.textio.ReadHandlerData;
import org.beryx.textio.StringInputReader;
import org.beryx.textio.TextIO;
import org.beryx.textio.swing.SwingTextTerminal;
import org.mars_sim.msp.core.LogConsolidated;

public class SwingHandler {
	
	private static Logger logger = Logger.getLogger(SwingHandler.class.getName());
	private static String sourceName = logger.getName().substring(logger.getName().lastIndexOf(".") + 1,
			logger.getName().length());
	
    private final SwingTextTerminal terminal;
    private final Object dataObject;

    private final String backKeyStroke;
    
    private final Supplier<StringInputReader> stringInputReaderSupplier;

    private final List<Task<?,?,?>> tasks = new ArrayList<>();

    public SwingHandler(TextIO textIO, String appName, Object dataObject) {
        this.terminal = (SwingTextTerminal)textIO.getTextTerminal();
        this.dataObject = dataObject;

        this.stringInputReaderSupplier = () -> textIO.newStringInputReader();

        this.backKeyStroke = terminal.getProperties().getString("custom.back.key", "ctrl U");

        terminal.registerHandler(backKeyStroke, t -> new ReadHandlerData(ABORT));
    }

    public class Task<T,B extends Task<T,B, R>, R extends InputReader<T, ?>> implements Runnable {
        protected final String prompt;
        protected final String key;
        protected final Supplier<R> inputReaderSupplier;
        protected final Supplier<T> defaultValueSupplier;
        protected final Consumer<T> valueSetter;
        protected final List<T> choices = new ArrayList<>();
        protected boolean showPrevious;
        protected boolean constrainedInput;
        protected Consumer<R> inputReaderConfigurator;

        public Task(String key, String prompt, boolean showPrevious, Supplier<R> inputReaderSupplier, Supplier<T> defaultValueSupplier, Consumer<T> valueSetter) {
            this.prompt = prompt;
            this.key = key;
            this.showPrevious = showPrevious;
            this.inputReaderSupplier = inputReaderSupplier;
            this.defaultValueSupplier = defaultValueSupplier;
            this.valueSetter = valueSetter;
        }

        @Override
        public void run() {
            
            R inputReader = inputReaderSupplier.get();
            if (showPrevious)
            	inputReader.withDefaultValue(defaultValueSupplier.get());
            if(inputReaderConfigurator != null) {
                inputReaderConfigurator.accept(inputReader);
            }
            if(constrainedInput) {
                inputReader.withValueChecker((val,name) -> choices.contains(val) ? null
                        : Arrays.asList("'" + val + "' is not in the choice list."));

            }
            T value = inputReader.read(prompt);
            valueSetter.accept(value);
        }

        @SuppressWarnings("unchecked")
        public B withInputReaderConfigurator(Consumer<R> configurator) {
            this.inputReaderConfigurator = configurator;
            return (B)this;
        }

        @SuppressWarnings("unchecked")
        public B addChoices(List<T> choices) {
            this.choices.addAll(choices);
            return (B)this;
        }

        public void constrainInputToChoices() {
            this.constrainedInput = true;
        }
    }

    private final <T> Supplier<T> getDefaultValueSupplier(String fieldName) {
        return () -> getFieldValue(fieldName);
    }

    private final <T> Consumer<T> getValueSetter(String fieldName) {
        return value -> setFieldValue(fieldName, value);
    }

    public class StringTask extends Task<String, StringTask, StringInputReader> {
        public StringTask(String fieldName, String prompt, boolean showPrevious) {
            super(fieldName, prompt,
            		showPrevious,
                    stringInputReaderSupplier,
                    getDefaultValueSupplier(fieldName),
                    getValueSetter(fieldName));
        }

        public StringTask addChoices(String... choices) {
            this.choices.addAll(Arrays.asList(choices));
            return this;
        }
    }

    public StringTask addStringTask(String fieldName, String prompt, boolean showPrevious) {
        StringTask task = new StringTask(fieldName, prompt, showPrevious);
        tasks.add(task);
        return task;
    }

    public void execute() {
        int step = 0;
        while(step < tasks.size()) {
            terminal.setBookmark("bookmark_" + step);
            try {
                tasks.get(step).run();
            } catch (ReadAbortedException e) {
                if(step > 0) step--;
                terminal.resetToBookmark("bookmark_" + step);
                continue;
            }
            step++;
        }
    }

    public void executeOneTask() {
    	// Remove the last task
    	if (tasks.size() > 1)
    		tasks.remove(0);
        terminal.setBookmark("bookmark_" + 0);
        try {
            if (tasks.get(0) != null)
            	tasks.get(0).run();
        } catch (ReadAbortedException e) {
            terminal.resetToBookmark("bookmark_" + 0);
        } catch (RuntimeException e) {
			e.printStackTrace();
			LogConsolidated.flog(Level.SEVERE, 0, sourceName, "RuntimeException detected.");
        }
        
    }
    
    private Field getField(String fieldName) {
        try {
            return dataObject.getClass().getField(fieldName);
        } catch (NoSuchFieldException e) {
            throw new IllegalArgumentException("Unknown field: " + fieldName);
        }
    }

    @SuppressWarnings("unchecked")
    private <V> V getFieldValue(String fieldName) {
        try {
            return (V) getField(fieldName).get(dataObject);
        } catch (Exception e) {
            throw new IllegalArgumentException("Cannot retrieve value of " + fieldName, e);
        }
    }

    private <V> void setFieldValue(String fieldName, V value) {
        try {
            getField(fieldName).set(dataObject, value);
        } catch (Exception e) {
            throw new IllegalArgumentException("Cannot set value of " + fieldName, e);
        }
    }
}

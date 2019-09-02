/**
 * Mars Simulation Project
 * History.java
 * @version 3.1.0 2019-08-17
 * @author Manny Kung
 */

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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.mars_sim.msp.core.Simulation;

public class History {
    private static final Logger logger = Logger.getLogger(History.class.getName());

    private final String appName;
    private final Map<String, List<String>> history = new HashMap<>();

    public History(String appName) {
        this.appName = appName;
        Properties props = new Properties();
        
        if (!new File(getPropFilePath()).getParentFile().exists()) {
        	new File(getPropFilePath()).getParentFile().mkdirs();
		}
        
        try {
            props.load(new FileInputStream(getPropFilePath()));
        } catch (IOException e) {
            logger.fine("History file not found. Initializing empty history.");
        }
        props.entrySet().forEach(entry -> {
            String[] values = entry.getValue().toString().split("\\s*,\\s*");
            history.put(entry.getKey().toString(), new ArrayList<>(Arrays.asList(values)));
        });
    }

    public List<String> getValues(String name) {
        return history.getOrDefault(name, new ArrayList<>());
    }

    public void addValue(String name, String value){
        history.compute(name, (v,list) -> {
            List<String> l = (list == null) ? new ArrayList<>() : list;
            l.removeAll(Collections.singleton(value));
            l.add(0, value);
            return l;
        });
    }

    public void save() {
        Properties props = new Properties();
        history.entrySet().forEach(entry -> {
            props.setProperty(entry.getKey(), entry.getValue().stream().collect(Collectors.joining(", ")));
        });
        try {
            props.store(new FileOutputStream(getPropFilePath()), "List of previous inputs");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String getPropFilePath() {
        return Simulation.HOME_DIR + 
    			File.separator + Simulation.CONSOLE_DIR +
    			File.separator + appName + ".input";
    }
}

/*
 * Mars Simulation Project
 * RobotGenerator.java
 * @date 2025-02-08
 * @author Barry Evans
 */
package com.mars_sim.tools.helpgenerator;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.mars_sim.core.robot.RobotSpec;

/**
 * Generates help output for any Malfunction Meta
 */
public class RobotGenerator extends TypeGenerator<RobotSpec> {

    public static final String TYPE_NAME = "robot";

    // Handy POJO to collapse complex object
    private static final record NamedValue(String name, int value) {}

    protected RobotGenerator(HelpContext parent) {
        super(parent, TYPE_NAME, "Robot",
        "Specification of various Robots.",
        "robots");
    }

   
    /**
     * Add specific properties for 
     * @param r Robot Spec for generation
     * @param output Destination of the content
     */
    @Override
    protected void addEntityProperties(RobotSpec r, Map<String,Object> scope) {

        var attrs = r.getAttributeMap().entrySet().stream()
                    .map(p -> new NamedValue(p.getKey().getName(),
                        p.getValue()))
                    .sorted(Comparator.comparing(NamedValue::name))
                    .toList();
        scope.put("attributes", attrs);

        var skills = r.getSkillMap().entrySet().stream()
                    .map(p -> new NamedValue(p.getKey().getName(),
                            p.getValue()))
                    .sorted(Comparator.comparing(NamedValue::name))
                    .toList();
        scope.put("skills", skills);
    }

    /**
     * Get a list of all robots.
     */
    @Override
    protected List<RobotSpec> getEntities() {
        return getConfig().getRobotConfiguration().getRobotSpecs()
                    .stream()
                    .sorted((o1, o2)->o1.getName().compareTo(o2.getName()))
                    .toList();
    }

    @Override
    protected String getEntityName(RobotSpec r) {
        return r.getName();
    }
}

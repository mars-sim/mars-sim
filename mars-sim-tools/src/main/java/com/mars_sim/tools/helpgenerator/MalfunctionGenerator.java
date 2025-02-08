/*
 * Mars Simulation Project
 * MalfunctionGenerator.java
 * @date 2025-02-02
 * @author Barry Evans
 */
package com.mars_sim.tools.helpgenerator;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.mars_sim.core.malfunction.MalfunctionMeta;
import com.mars_sim.core.resource.ItemResourceUtil;

/**
 * Generates help output for any Malfunction Meta
 */
public class MalfunctionGenerator extends TypeGenerator<MalfunctionMeta> {

    public static final String TYPE_NAME = "malfunction";

    // Handy POJO to collapse complex object
    private static final record NamedValues(String name, int number, double value) {}

    protected MalfunctionGenerator(HelpContext parent) {
        super(parent, TYPE_NAME, "Malfunction",
        "Various defined Malfunctions that can occur.",
        "malfunctions");

        // Groups according to Severity
        setGrouper("Severity", r-> Integer.toString(r.getSeverity()));
    }

   
    /**
     * Add specific properties for Malfunction/ Create simplified lists of effort, complaints, parts
     * @param m MalfuncionMeta for generation
     * @param output Destination of the content
     */
    @Override
    protected void addEntityProperties(MalfunctionMeta m, Map<String,Object> scope) {

        // Build a better versino of repair parts
        var parts = m.getParts().stream()
                    .map(p -> new NamedValues(ItemResourceUtil.findItemResourceName(p.getPartID()),
                                        p.getNumber(), p.getRepairProbability()))
                    .sorted(Comparator.comparing(NamedValues::name))
                    .toList();
        scope.put("parts", parts);


        var efforts = m.getRepairEffort().entrySet().stream()
                    .map(p -> new NamedValues(p.getKey().getName(),
                        p.getValue().getDesiredWorkers(), p.getValue().getWorkTime()))
                    .sorted(Comparator.comparing(NamedValues::name))
                    .toList();
        scope.put("efforts", efforts);

        var complaints = m.getMedicalComplaints().entrySet().stream()
                .map(p -> new NamedValues(p.getKey().getName(),
                    1, p.getValue()))
                .sorted(Comparator.comparing(NamedValues::name))
                .toList();
        scope.put("complaints", complaints);

        
        var lifesupport = m.getLifeSupportEffects().entrySet().stream()
                .map(p -> new NamedValues(p.getKey(),
                    1, p.getValue()))
                .sorted(Comparator.comparing(NamedValues::name))
                .toList();
        scope.put("lifesupport", lifesupport);
    }

    /**
     * Get a list of all resources.
     */
    @Override
    protected List<MalfunctionMeta> getEntities() {
        return getParent().getConfig().getMalfunctionConfiguration().getMalfunctionList()
                    .stream()
                    .sorted((o1, o2)->o1.getName().compareTo(o2.getName()))
                    .toList();
    }

    @Override
    protected String getEntityName(MalfunctionMeta v) {
        return v.getName();
    }
}

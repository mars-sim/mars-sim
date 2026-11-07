/*
 * Mars Simulation Project
 * MassCheck.java
 * @author CoPilot
 * @date 2026-09-10
 */
package com.mars_sim.tools.manufacture;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.goods.GoodType;
import com.mars_sim.core.process.ProcessInfo;
import com.mars_sim.core.resource.ItemResourceUtil;
import com.mars_sim.core.resource.ResourceUtil;

/**
 * ResourceCheck
 */
public class ResourceCheck {
    private final SimulationConfig config;


    private static final String DETAILS_FILENAME = "resource-check-details.txt";

    private static final int AMOUNT_FROM_FOOD = 0;
    private static final int AMOUNT_FROM_MANU = 1;
    private static final int AMOUNT_FROM_CROP = 2;
    private static final int AMOUNT_FROM_RESOURCE = 3;
    private static final int AMOUNT_NONE = 4;

    private record NamedResource(int id, String name) {}

    public ResourceCheck(SimulationConfig config) {
        this.config = config;
    }

    private static int getAmountSource(GoodType goodType) {
        return switch(goodType) {
            case ANIMAL, DERIVED, MEDICAL, SOY_BASED -> AMOUNT_FROM_FOOD;
            case CHEMICAL, CONSTRUCTION -> AMOUNT_FROM_RESOURCE;
            case COMPOUND, ELEMENT, OIL, UTILITY -> AMOUNT_FROM_MANU;
            case CROP -> AMOUNT_FROM_CROP;
            default -> AMOUNT_NONE;
        };
    }


    // Build a map from the output resource to all Processes that produce it
    private static Map<Integer,Set<String>> buildOutput2Process(String named, List<? extends ProcessInfo> processList) {
        Map<Integer,Set<String>> manuOutputs = new HashMap<>();
        for(var process : processList) {
            for(var output : process.getOutputList()) {
                manuOutputs.computeIfAbsent(output.getId(), k -> new HashSet<>()).add(named + ":" + process.getName());
            }
        }
        return manuOutputs;
    }

    public void report(PrintStream out, Path outputPath) {

        // Build a map from the output resource to all Processes that produce it
        var manuOutputs = buildOutput2Process("Manufacture",
                            config.getManufactureConfiguration().getManufactureProcessList());
        var foodOutputs = buildOutput2Process("Food",config.getFoodProductionConfiguration().getProcessList());
        
        Map<Integer, Set<String>> chemicalOutputs = new HashMap<>();
        for(var entry : config.getResourceProcessConfiguration().getOutputResourceMap().entrySet()) {
            chemicalOutputs.put(entry.getKey().getID(), entry.getValue().stream()
                    .map(r -> "Resource:" + r.getName())
                    .collect(Collectors.toSet()));
        }
           
        var totalOutputs = mergeOutputs(chemicalOutputs, manuOutputs);
        mergeOutputs(totalOutputs, foodOutputs);
        mergeOutputs(totalOutputs, chemicalOutputs);

        // Create the details report file
        Path detailsFile = outputPath.resolve(DETAILS_FILENAME);
        try (BufferedWriter reportFile = Files.newBufferedWriter(detailsFile,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {

            // Parts can only be resolved by Manufacture processes
            var partIDs = ItemResourceUtil.getItemResources().stream()
                            .map(r -> new NamedResource(r.getID(), r.getName()))
                            .toList();
            checkForCreation("Part", out, reportFile, partIDs, totalOutputs);

            // Split Ammonut resource up according to their type
            var amountResourceIDs = ResourceUtil.getAmountResources().stream()
                        .collect(Collectors.groupingBy(ar -> getAmountSource(ar.getGoodType()),
                            Collectors.mapping(ar -> new NamedResource(ar.getID(), ar.getName()), Collectors.toList())
                        ));

            // Check food against food processes
            checkForCreation("Food", out, reportFile,
                            amountResourceIDs.getOrDefault(AMOUNT_FROM_FOOD, List.of()), totalOutputs);

            // Check manufactured resources
            checkForCreation("Manufactured", out, reportFile,
                            amountResourceIDs.getOrDefault(AMOUNT_FROM_MANU, List.of()), totalOutputs);

            // Chemical resources are checked
            checkForCreation("Chemical", out, reportFile,
                            amountResourceIDs.getOrDefault(AMOUNT_FROM_RESOURCE, List.of()), totalOutputs);

        }
    	catch (IOException ex) {
			throw new IllegalStateException("Cannot write details report file in directory: " + outputPath, ex);
		}
    }

    private static Map<Integer, Set<String>> mergeOutputs(Map<Integer, Set<String>> sourceMap,
            Map<Integer, Set<String>> additionalMap) {

        // Merge in the manufacturing process
        for(var e : additionalMap.entrySet()) {
            sourceMap.merge(e.getKey(), e.getValue(),
                    (oldSet, newSet) -> {
                        oldSet.addAll(newSet);
                        return oldSet;
                    });
        }

        return sourceMap;
    }

    /**
     * Check that a set of Resource IDs have processes that create them.
     * This checks the resource is present in the output map.
     * 
     * @param title the title to use in the report for this set of resources
     * @param out the PrintStream to output the report of immediate issues.
     * @param reportFile File for detailed report.
     * @param resourceIDs the set of resource IDs to check
     * @param outputs2Process a map from resource ID to the set of processes that produce it
     */
    private void checkForCreation(String title, PrintStream out, BufferedWriter reportFile, List<NamedResource> resourceIDs,
                Map<Integer, Set<String>> outputs2Process) 
            throws IOException {

        reportFile.write("Checking creation for " + title);
        reportFile.newLine();
        reportFile.write("==========================================");
        reportFile.newLine();

        // Sort the resources by name for consistent reporting
        var sorted = resourceIDs.stream().sorted(Comparator.comparing(NamedResource::name)).toList();

        for(var resourceId : sorted) {
            var name = resourceId.name();

            // Only uncreated resoruce go to out
            if (!outputs2Process.containsKey(resourceId.id)) {
                out.println(title + " " + name + " has no process to create.");

                reportFile.write(name + " has no process to create.");
                reportFile.newLine();
            }
            else {
                reportFile.write(name + " created by: " + 
                            outputs2Process.get(resourceId.id()).stream()
                                .map(p -> "'" + p + "'")
                                .collect(Collectors.joining(", ")));
                reportFile.newLine();
            }
        }
        reportFile.newLine();
    } 
}

/*
 * Mars Simulation Project
 * BundleCheck.java
 * @date 2025-09-28
 * @author Barry Evans			
 */
package com.mars_sim.tools.locale;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.help.HelpFormatter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import com.mars_sim.core.UnitType;
import com.mars_sim.core.building.BuildingCategory;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.building.function.SystemType;
import com.mars_sim.core.building.function.cooking.DishCategory;
import com.mars_sim.core.building.function.farming.PhaseType;
import com.mars_sim.core.building.utility.heating.HeatMode;
import com.mars_sim.core.building.utility.power.PowerMode;
import com.mars_sim.core.computing.ComputingLoadType;
import com.mars_sim.core.environment.DustStormType;
import com.mars_sim.core.environment.LandmarkType;
import com.mars_sim.core.equipment.EquipmentType;
import com.mars_sim.core.events.HistoricalEventCategory;
import com.mars_sim.core.events.HistoricalEventType;
import com.mars_sim.core.food.FoodType;
import com.mars_sim.core.goods.GoodCategory;
import com.mars_sim.core.interplanetary.transport.TransitState;
import com.mars_sim.core.location.LocationStateType;
import com.mars_sim.core.person.ai.NaturalAttributeType;
import com.mars_sim.core.person.ai.PersonalityTraitType;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.fav.FavoriteType;
import com.mars_sim.core.person.ai.job.util.AssignmentType;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.mission.PlanType;
import com.mars_sim.core.person.ai.role.RoleType;
import com.mars_sim.core.person.ai.training.CertificationType;
import com.mars_sim.core.person.ai.training.TrainingType;
import com.mars_sim.core.person.health.ComplaintType;
import com.mars_sim.core.person.health.HealthProblemState;
import com.mars_sim.core.person.health.HealthRiskType;
import com.mars_sim.core.science.ScienceType;
import com.mars_sim.core.science.StudyStatus;
import com.mars_sim.core.structure.ObjectiveType;
import com.mars_sim.core.vehicle.StatusType;
import com.mars_sim.core.vehicle.VehicleType;

/**
 * Tool to check locale bundles for missing or redundant keys and parameter mismatches.
 * This tool assumes that enum types use a standard key format of EnumType.enumvalue in lower case.
 * This tool will generate a report file for each locale in the target/locale-check folder.
 */
public class BundleCheck {

    private static final String SOURCE_ARG = "source";

    private static final String TARGET_ARG = "output";

    public static void main(String[] args) throws IOException {
        var sourceFolder = "./mars-sim-core/src/main/resources/";
        var targetFolder = "./target/locale-check/";

		Options options = new Options();
		options.addOption(Option.builder(SOURCE_ARG).argName("Folder to scan").hasArg()
				.desc("Location to scan for bundle files.").get());
		options.addOption(Option.builder(TARGET_ARG).argName("Folder to output reports").hasArg()
				.desc("Location for final reports.").get());

		DefaultParser commandline = new DefaultParser();
		try {
			CommandLine line = commandline.parse(options, args);
            if (line.hasOption(SOURCE_ARG)) {
				sourceFolder = line.getOptionValue(SOURCE_ARG);
			}
			if (line.hasOption(TARGET_ARG)) {
				targetFolder = line.getOptionValue(TARGET_ARG);
			}
        }
		catch (ParseException e) {
            // New non-deprecated HelpFormatter (Commons CLI 1.10+)
            HelpFormatter fmt = HelpFormatter.builder().get();
            String header = "\n" + e.getMessage() + "\n";
            try {
                fmt.printHelp("bundleCheck [options]", header, options, null, true);
            } catch (IOException ioe) {
                // Fallback if printing help fails
                logger.severe("usage: bundleCheck [options]");
            }
            System.exit(1);
		}

        var checker = new BundleCheck(targetFolder);
        checker.checkBundles(sourceFolder);
    }

    private static Logger logger = Logger.getLogger(BundleCheck.class.getName());

    private File outputFolder;

    private BundleCheck(String outputDir) {
        outputFolder = new File(outputDir);
    
        logger.info("Output folder: " + outputFolder.getAbsolutePath());
        outputFolder.mkdirs();
    }

    /**
     * Find any defined locale properties file in the source folder and compare to the base
     * messages.properties file.
     * @throws IOException
     */
    private void checkBundles(String sourceFolder) throws IOException {
        var source = new File(sourceFolder);

        // Load the base messages file
        var baseMessages = loadMessages(new File(source, "messages.properties"));

        // Define keys that are automatically handled by the system
        Set<String> defaultEnumsKeys = loadDefaultEnumsKeys();

        // Compare each found locale file
        for(var f : FileUtils.listFiles(source, new String[]{"properties"}, false)) {
            if (f.getName().startsWith("messages_")) {
                var name = f.getName().substring("messages_".length(), f.getName().length() - ".properties".length());
                if (!name.isEmpty()) {
                    compareBundle(f, name, baseMessages, defaultEnumsKeys);
                }
            }
        }
        
    }

    /**
     * Create an entry for every constant in the enum class using the format
     * @param <T> the enum type
     * @param results   the set to add the keys to
     * @param enumClass the enum class to process
     */
    private <T extends Enum<T>>  void loadEnum(Set<String> results, Class<T> enumClass) {
        String mainKey = enumClass.getSimpleName() + ".";
        for(var v : enumClass.getEnumConstants()) {
            results.add(mainKey + v.name().toLowerCase());
        }
    }

    /**
     * Load all the enum keys that use the default naming strategy.
     * The enums used here should match those in Msg.getStringOptional calls.
     * @return set of enum keys
     */
    private Set<String> loadDefaultEnumsKeys() {
        Set<String> results = new TreeSet<>();

        // These are all the enums that use a default name strategy
        loadEnum(results, FunctionType.class);
        loadEnum(results, EquipmentType.class);
        loadEnum(results, RoleType.class);
        loadEnum(results, HeatMode.class);
        loadEnum(results, LocationStateType.class);
        loadEnum(results, HealthRiskType.class);
        loadEnum(results, HistoricalEventCategory.class);
        loadEnum(results, PersonalityTraitType.class);
        loadEnum(results, PhaseType.class);
        loadEnum(results, StudyStatus.class);
        loadEnum(results, ComputingLoadType.class);
        loadEnum(results, ObjectiveType.class);
        loadEnum(results, VehicleType.class);
        loadEnum(results, UnitType.class);
        loadEnum(results, AssignmentType.class);
        loadEnum(results, DustStormType.class);
        loadEnum(results, FoodType.class);
        loadEnum(results, ScienceType.class);
        loadEnum(results, LandmarkType.class);
        loadEnum(results, FavoriteType.class);
        loadEnum(results, BuildingCategory.class);
        loadEnum(results, GoodCategory.class);
        loadEnum(results, TrainingType.class);
        loadEnum(results, DishCategory.class);
        loadEnum(results, StatusType.class);
        loadEnum(results, NaturalAttributeType.class);
        loadEnum(results, SkillType.class);
        loadEnum(results, PlanType.class);
        loadEnum(results, CertificationType.class);
        loadEnum(results, SystemType.class);
        loadEnum(results, HealthProblemState.class);
        loadEnum(results, JobType.class);
        loadEnum(results, ComplaintType.class);
        loadEnum(results, PowerMode.class);
        loadEnum(results, TransitState.class);
        loadEnum(results, HistoricalEventType.class);

        return results;
    }

    /**
     * Compare a locale bundle to the base messages and generate a report file.
     * @param f             the locale file
     * @param name          the locale name
     * @param baseMessages  the base messages map
     * @param enumKeys      the set of enum keys that are automatically handled
     * @throws IOException
     */
    private void compareBundle(File f, String name, Map<String,Integer> baseMessages,
                Set<String> enumKeys) throws IOException {
        var localeMessage = loadMessages(f);
        int differences = 0;
        int enumMissing = 0;

        File outputFile = new File(outputFolder, "report_" + name + ".txt");
        try(var out = new PrintWriter(new FileOutputStream(outputFile))) {
            out.println("Report for locale " + name);

            // Report on the enum keys
            out.println();
            out.println("Enum keys (these will automatically default to internal enum value):");
            for(var k : enumKeys) {
                if (!localeMessage.containsKey(k)) {
                    out.println(k + " : MISSING");
                    enumMissing++;
                }
            }

            // Report on the match with the base keys
            Set<String> baseKeys = new TreeSet<>(baseMessages.keySet());
            out.println();
            out.println("Base keys:");
            for(var k : baseKeys) {
                if (localeMessage.containsKey(k)) {
                    // Check parameter count
                    var localeCount = localeMessage.get(k);
                    int baseCount = baseMessages.get(k).intValue();
                    if (baseCount != localeCount.intValue()) {
                        out.println(k + " : parameters base=" + baseCount + ", locale=" + localeCount);
                        differences++;
                    }
                } else {
                    out.println(k + " : MISSING");
                    differences++;
                }
            }

            // Find keys in the locale that are missing in the base but allow any enum keys
            Set<String> extraKeys = new TreeSet<>(localeMessage.keySet());
            extraKeys.removeAll(baseMessages.keySet());
            extraKeys.removeAll(enumKeys);
            out.println();
            out.println("Redundant keys:");
            for(var k : extraKeys) {
                out.println(k);
            }
            logger.info("Report for locale " + name + ":" + differences + " differences "
                        + enumMissing + " missing enum keys, "
                        + extraKeys.size() + " redundant keys.");
        }
    }

    /**
     * Load messages from a properties file and count the number of parameters in each message.
     * @param file  the properties file
     * @return map of message keys to parameter count
     * @throws IOException
     */
    private Map<String, Integer> loadMessages(File file) throws IOException {
        Map<String, Integer> results = new HashMap<>();

        try(var input = FileUtils.openInputStream(file)) {  
            Properties rawMessages = new Properties();
            rawMessages.load(input);
        
            // Tag messages that contain properties
            rawMessages.forEach( (k,v)-> {
                var key = (String) k;
                int count = StringUtils.countMatches((String) v, "{");
                results.put(key, count);
            });
        }
        
        return results;
    }
}
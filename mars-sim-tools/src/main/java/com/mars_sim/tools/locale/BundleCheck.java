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

import com.mars_sim.core.Named;
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
import com.mars_sim.core.equipment.BatteryStatus;
import com.mars_sim.core.equipment.EquipmentType;
import com.mars_sim.core.events.HistoricalEventCategory;
import com.mars_sim.core.events.HistoricalEventType;
import com.mars_sim.core.food.FoodType;
import com.mars_sim.core.goods.GoodCategory;
import com.mars_sim.core.interplanetary.transport.TransitState;
import com.mars_sim.core.location.LocationStateType;
import com.mars_sim.core.person.FatigueLevel;
import com.mars_sim.core.person.HungerLevel;
import com.mars_sim.core.person.PerformanceLevel;
import com.mars_sim.core.person.StressLevel;
import com.mars_sim.core.person.ThirstLevel;
import com.mars_sim.core.person.ai.NaturalAttributeType;
import com.mars_sim.core.person.ai.PersonalityTraitType;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.fav.FavoriteType;
import com.mars_sim.core.person.ai.job.util.AssignmentType;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.mission.MissionType;
import com.mars_sim.core.person.ai.mission.PlanType;
import com.mars_sim.core.person.ai.role.RoleType;
import com.mars_sim.core.person.ai.training.CertificationType;
import com.mars_sim.core.person.ai.training.TrainingType;
import com.mars_sim.core.person.health.BodyRegionType;
import com.mars_sim.core.person.health.ComplaintType;
import com.mars_sim.core.person.health.HealthProblemState;
import com.mars_sim.core.person.health.HealthRiskType;
import com.mars_sim.core.robot.BotMode;
import com.mars_sim.core.robot.RobotPerfLevel;
import com.mars_sim.core.science.ScienceType;
import com.mars_sim.core.science.StudyStatus;
import com.mars_sim.core.structure.ObjectiveType;
import com.mars_sim.core.structure.OverrideType;
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

    record LangValue(String value, int paramCount) {}

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
        try(var report = new PrintWriter(new FileOutputStream(new File(outputFolder, "report.txt")))) {
            // Compare each found locale file
            for(var f : FileUtils.listFiles(source, new String[]{"properties"}, false)) {
                if (f.getName().startsWith("messages_")) {
                    var name = f.getName().substring("messages_".length(), f.getName().length() - ".properties".length());
                    if (!name.isEmpty()) {
                        compareBundle(f, name, baseMessages, report);
                    }
                }
            }
        }
    }

    /**
     * Compare the enums used in the code to the keys in the locale bundle and report any missing keys.
     * @param localeMessages the messages from the locale bundle
     * @param output the output writer to write the report to
     * @return the number of missing enum keys
     */
    private int compareEnums(Map<String, LangValue> localeMessages, PrintWriter output) {
        output.println("# Enum keys (these will automatically default to internal enum value):");

        int enumMissing = 0;

        // These are all the enums that use a default name strategy
        // This is all Enums that use the Msg.getStringOptional("EnumType", name()) strategy
        // This could be done dynamically by finding all Enums that implement Named.
        enumMissing += compareEnum(AssignmentType.class, localeMessages, output);
        enumMissing += compareEnum(BatteryStatus.class, localeMessages, output);
        enumMissing += compareEnum(BodyRegionType.class, localeMessages, output);
        enumMissing += compareEnum(BotMode.class, localeMessages, output);
        enumMissing += compareEnum(BuildingCategory.class, localeMessages, output);
        enumMissing += compareEnum(CertificationType.class, localeMessages, output);
        enumMissing += compareEnum(ComplaintType.class, localeMessages, output);
        enumMissing += compareEnum(ComputingLoadType.class, localeMessages, output);
        enumMissing += compareEnum(DishCategory.class, localeMessages, output);
        enumMissing += compareEnum(DustStormType.class, localeMessages, output);
        enumMissing += compareEnum(EquipmentType.class, localeMessages, output);
        enumMissing += compareEnum(FatigueLevel.class, localeMessages, output);
        enumMissing += compareEnum(FavoriteType.class, localeMessages, output);
        enumMissing += compareEnum(FoodType.class, localeMessages, output);
        enumMissing += compareEnum(FunctionType.class, localeMessages, output);
        enumMissing += compareEnum(GoodCategory.class, localeMessages, output);
        enumMissing += compareEnum(HealthProblemState.class, localeMessages, output);
        enumMissing += compareEnum(HealthRiskType.class, localeMessages, output);
        enumMissing += compareEnum(HeatMode.class, localeMessages, output);
        enumMissing += compareEnum(HistoricalEventCategory.class, localeMessages, output);
        enumMissing += compareEnum(HistoricalEventType.class, localeMessages, output);
        enumMissing += compareEnum(HungerLevel.class, localeMessages, output);
        enumMissing += compareEnum(JobType.class, localeMessages, output);
        enumMissing += compareEnum(LandmarkType.class, localeMessages, output);
        enumMissing += compareEnum(LocationStateType.class, localeMessages, output);
        enumMissing += compareEnum(MissionType.class, localeMessages, output);
        enumMissing += compareEnum(NaturalAttributeType.class, localeMessages, output);
        enumMissing += compareEnum(ObjectiveType.class, localeMessages, output);
        enumMissing += compareEnum(OverrideType.class, localeMessages, output);
        enumMissing += compareEnum(PerformanceLevel.class, localeMessages, output);
        enumMissing += compareEnum(PersonalityTraitType.class, localeMessages, output);
        enumMissing += compareEnum(PhaseType.class, localeMessages, output);
        enumMissing += compareEnum(PlanType.class, localeMessages, output);
        enumMissing += compareEnum(PowerMode.class, localeMessages, output);
        enumMissing += compareEnum(RobotPerfLevel.class, localeMessages, output);
        enumMissing += compareEnum(RoleType.class, localeMessages, output);
        enumMissing += compareEnum(ScienceType.class, localeMessages, output);
        enumMissing += compareEnum(SkillType.class, localeMessages, output);
        enumMissing += compareEnum(StatusType.class, localeMessages, output);
        enumMissing += compareEnum(StressLevel.class, localeMessages, output);
        enumMissing += compareEnum(StudyStatus.class, localeMessages, output);
        enumMissing += compareEnum(SystemType.class, localeMessages, output);
        enumMissing += compareEnum(TrainingType.class, localeMessages, output);
        enumMissing += compareEnum(TransitState.class, localeMessages, output);
        enumMissing += compareEnum(ThirstLevel.class, localeMessages, output);
        enumMissing += compareEnum(UnitType.class, localeMessages, output);
        enumMissing += compareEnum(VehicleType.class, localeMessages, output);

        return enumMissing;
    }

    /**
     * Compare the enum values of a given enum class to the keys in the locale bundle and report any missing keys.
     * @param enumClass the enum class to compare
     * @param localeMessages the messages from the locale bundle
     * @param output the output writer to write the report to
     * @return the number of missing enum keys
     */
    private int compareEnum(Class<? extends Enum<?>> enumClass, Map<String, LangValue> localeMessages, PrintWriter output) {
        int enumMissing = 0;

        // This follows the pattern used in Msg.getOptional where the key is the enum type name in lower case
        // followed by the enum value in lower case
        String mainKey = enumClass.getSimpleName().toLowerCase() + ".";
        for(var v : enumClass.getEnumConstants()) {
            String key = mainKey + v.name().toLowerCase();
            if (localeMessages.containsKey(key)) {
                output.println(key + "=" + localeMessages.get(key).value);    
            }
            else if (v instanceof Named n) {
                // Supports localised name
                output.println("#" + key + "=" + n.getName());
                enumMissing++;
            }
            else {
                // Ideally should not happen for these Enums
                output.println("#" + key + "=" + v.name());
                enumMissing++;
            }
        }
        return enumMissing;
    }
    
    /**
     * 
     * Compare a locale bundle to the base messages and generate a report file.
     * @param f             the locale file
     * @param name          the locale name
     * @param report    the report writer
     * @param baseMessages  the base messages map
     * @throws IOException
     */
    private void compareBundle(File f, String name, Map<String, LangValue> baseMessages,
                                PrintWriter report) throws IOException {
        var localeMessage = loadMessages(f);
        int differences = 0;
        var outputFile = new File(outputFolder, "messages_" + name + ".properties");
        logger.info("Locale " + name + " output file: " + outputFile.getAbsolutePath());

        try(var output = new PrintWriter(new FileOutputStream(outputFile))) {

            // Header in output files
            output.println("# Proposed new properties file for locale " + name);

            // Do enums first
            var enumMissing = compareEnums(localeMessage, output);

            // Report on the match with the base keys
            Set<String> baseKeys = new TreeSet<>(baseMessages.keySet());
            output.println();
            output.println("# Base keys:");
            for(var k : baseKeys) {
                var baseValue = baseMessages.get(k);
                if (localeMessage.containsKey(k)) {
                    // Check parameter count
                    var localeValue = localeMessage.get(k);
                    if (baseValue.paramCount != localeValue.paramCount) {
                        differences++;

                        output.println("#" + k + "=" + localeMessage.get(k).value + " expected params " + baseValue.paramCount);
                    }
                    else {
                        output.println(k + "=" + localeMessage.get(k).value);
                    }
                } else {
                    differences++;
                    output.println("#" + k + "=" + baseValue.value);
                }
            }

            // Create a summary of the results
            var summary = "Locale " + name + ":" + differences + " missing keys "
                        + enumMissing + " missing enum keys.";
            logger.info(summary);
            report.println(summary);
        }
    }

    /**
     * Load messages from a properties file and count the number of parameters in each message.
     * @param file  the properties file
     * @return map of message keys to parameter count
     * @throws IOException
     */
    private Map<String, LangValue> loadMessages(File file) throws IOException {
        Map<String, LangValue> results = new HashMap<>();

        try(var input = FileUtils.openInputStream(file)) {  
            Properties rawMessages = new Properties();
            rawMessages.load(input);
        
            // Tag messages that contain properties
            rawMessages.forEach( (k,v)-> {
                var key = (String) k;
                var value = (String) v;
                int count = StringUtils.countMatches(value, "{");
                results.put(key, new LangValue(value, count));
            });
        }
        
        return results;
    }

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
            } catch (IOException _) {
                // Fallback if printing help fails
                logger.severe("usage: bundleCheck [options]");
            }
            System.exit(1);
		}

        var checker = new BundleCheck(targetFolder);
        checker.checkBundles(sourceFolder);
    }
}
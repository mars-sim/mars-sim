/**
 * Mars Simulation Project
 * HelpGenerator.java
 * @version 3.1.0 2017-02-03
 * @author stpa
 */

package org.mars_sim.msp.ui.helpGenerator;

// TODO make the generated help files internationalizable.

import java.io.File;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.mars_sim.msp.core.LifeSupportType;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.foodProduction.FoodProductionProcessInfo;
import org.mars_sim.msp.core.foodProduction.FoodProductionProcessItem;
import org.mars_sim.msp.core.foodProduction.FoodProductionUtil;
import org.mars_sim.msp.core.manufacture.ManufactureProcessInfo;
import org.mars_sim.msp.core.manufacture.ManufactureProcessItem;
import org.mars_sim.msp.core.manufacture.ManufactureUtil;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.ItemResourceUtil;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.resource.ItemType;
import org.mars_sim.msp.core.vehicle.VehicleConfig;
import org.mars_sim.msp.core.vehicle.VehicleConfig.VehicleDescription;
import org.mars_sim.msp.ui.swing.tool.Conversion;
import org.mars_sim.msp.ui.swing.tool.resupply.SupplyTableModel;

/**
 * Generates html files for the in-game help. 
 * Note : start it by running "MarsProjectFX -html.launch" in Eclipse 
 */
public class HelpGenerator {

	/** initialized logger for this class. */
	private static Logger logger = Logger.getLogger(HelpGenerator.class.getName());

	/*
	 * absolute path for creating help files with command
	 * line argument -generateHelp only when needed.
	 *
	private static final String ABSOLUTE_DIR = "X:/path/to/your/workspace/code/mars-sim/mars-sim-ui/src/main/resources/docs/help";

	// In Windows OS, the Eclipse's htmls are kept in the \src\ folder
	// or at \mars-sim\mars-sim-ui\src\main\resources\docs\help
	// e.g. D:\Data\git\mars-sim\mars-sim-ui\src\main\resources\docs\help
	//
	// After running "MarsProjectFX -html.launch" in Eclipse, the newly generated htmls will be at the \target\ folder 
	// or at at \mars-sim-ui\target\classes\docs\help
	// e.g. D:\Data\git\mars-sim\mars-sim-ui\target\classes\docs\help

*/

	private static final String DIR = "\\docs\\help\\";
	private static final String SUFFIX = ".html";

	private static final String VEHICLES = "vehicles";
	private static final String VEHICLE = "vehicle_";

	private static final String RESOURCES = "resources";
	private static final String RESOURCE = "resource_";

	private static final String PARTS = "parts";
	private static final String PART = "part_";

	private static final String PROCESSES = "processes";
	private static final String PROCESS = "process_";

	private static final String FOOD_PRODUCTIONS = "food_productions";
	private static final String FOOD_PRODUCTION = "food_production_";

	private static final String EQUIPMENTS = "equipment";
	private static final String EQUIPMENT = "equipment_";

	/** used to count how many files are generated. */
	private static int filesGenerated = 0;
	/** used to count how many files could not be generated. */
	private static int filesNotGenerated = 0;

	/**
	 * insert html header with given page title into given string buffer.
	 * @param s {@link StringBuffer}
	 * @param title {@link String}
	 */
	private static final void helpFileHeader(final StringBuffer s, final String title) {
		StringBuffer header = new StringBuffer()
		.append("<!DOCTYPE html>\n")
		.append("<!-- Generated for mars-sim by st.pa. -->\n")
		.append("<html>\n")
		.append("\t<head>\n")
		//.append("\t\t<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n")
		.append("\t\t<meta charset=UTF-8\">\n")
		.append("\t\t<title>")
		.append("Mars Simulation Project - Generated help file - ")
		.append(title)
		.append("</title>\n")
		.append("\t\t<link rel=\"stylesheet\" href=\"../msp.css\">\n")
		.append("\t</head>\n")
		.append("\t<body>\n\n")
		.append("\t<TT>\n\n");

		s.insert(0,header.toString());
	}

	private static final void helpFileFooter(final StringBuffer s) {
		s.append("\n\t</TT>\n\n");
		s.append("\n\t</body>\n");
		s.append("</html>\n");
	}

	// 2016-04-20 Fixed generating <a href> link correctly
	private static final void helpFileTableRow(
		final StringBuffer s,
		final String[] columnContents
	) {
		s.append("\t<tr>\n");
		for (String columnContent : columnContents) {
			s.append("\t\t<td>");
			s.append(columnContent.replace(" ","&nbsp;").replace("a&nbsp;href", "a href"));
			s.append("</td>\n");
		}
		s.append("\t</tr>\n");
	}

	private static final void helpFileNoSuchProcess(final StringBuffer content) {
		content.append("<p><ul><li>No Such Manufacturing Processes Known</li></ul></p>");
	}

	private static final void helpFileNoSuchFoodProductionProcess(final StringBuffer content) {
		content.append("<p><ul><li>No Such Food Production Processes Known</li></ul></p>");
	}

	private static final void generateFile(final StringBuffer path, final StringBuffer content) {
		try {
			String absPath = new File(
				HelpGenerator
				.class
				.getClassLoader()
				.getResource(DIR)
				.toURI()
			).getAbsolutePath();
			//System.out.println("absPath is " + absPath);
			File file = new File(absPath + '/' + path.toString());
//			File file = new File(ABSOLUTE_DIR + '/' + path.toString());
			PrintWriter pw = new PrintWriter(file);
			pw.write(content.toString());
			pw.close();
//			logger.log(Level.INFO,"generated file " + file.getName());
			filesGenerated++;
		} catch (Exception e) {
			logger.log(Level.WARNING,"failed to generate file " + path.toString());
			filesNotGenerated++;
		}
	}

	private static String escape(String s) {
		return s.replace(" ","_").replace("/","--");
	}

	private static final StringBuffer getPathVehicle(final String vehicle) {
		return new StringBuffer()
		.append(VEHICLE)
		.append(escape(vehicle))
		.append(SUFFIX);
	}

	private static final StringBuffer getPathVehicles() {
		return new StringBuffer()
		.append(VEHICLES)
		.append(SUFFIX);
	}

	private static final StringBuffer getPathResource(final String resource) {
		return new StringBuffer()
		.append(RESOURCE)
		.append(escape(resource))
		.append(SUFFIX);
	}

	private static final StringBuffer getPathResources() {
		return new StringBuffer()
		.append(RESOURCES)
		.append(SUFFIX);
	}

	private static final StringBuffer getPathPart(final String part) {
		return new StringBuffer()
		.append(PART)
		.append(escape(part))
		.append(SUFFIX);
	}

	private static final StringBuffer getPathParts() {
		return new StringBuffer()
		.append(PARTS)
		.append(SUFFIX);
	}

	private static final StringBuffer getPathProcess(final String process) {
		return new StringBuffer()
		.append(PROCESS)
		.append(escape(process))
		.append(SUFFIX);
	}

	private static final StringBuffer getPathProcesses() {
		return new StringBuffer()
		.append(PROCESSES)
		.append(SUFFIX);
	}

	private static final StringBuffer getPathFoodProductionProcess(final String process) {
		return new StringBuffer()
		.append(FOOD_PRODUCTION)
		.append(escape(process))
		.append(SUFFIX);
	}

	private static final StringBuffer getPathFoodProductionProcesses() {
		return new StringBuffer()
		.append(FOOD_PRODUCTIONS)
		.append(SUFFIX);
	}

	private static final StringBuffer getPathEquipment(final String equipment) {
		return new StringBuffer()
		.append(EQUIPMENT)
		.append(escape(equipment))
		.append(SUFFIX);
	}

	private static final StringBuffer getPathEquipments() {
		return new StringBuffer()
		.append(EQUIPMENTS)
		.append(SUFFIX);
	}

	/**
	 * produces a link <code> a href = getPathResource(name) </code>
	 * with the name as caption.
	 * @param resourceName {@link String}
	 * @return {@link String}
	 */
	private static final String getLinkResource(final String resourceName) {
		return link(getPathResource(resourceName), resourceName);
	}

	/**
	 * produces a link to the resources page with the given caption.
	 * @param caption {@link String}
	 * @return {@link String}
	 */
	private static final String getLinkResources(final String caption) {
		return link(getPathResources(),caption);
	}

	/**
	 * produces a link <code> a href = getPathVehicle(name) </code>
	 * with the name as caption.
	 * @param vehicleName {@link String}
	 * @return {@link String}
	 */
	private static final String getLinkVehicle(final String vehicleName) {
		return link(getPathVehicle(vehicleName),vehicleName);
	}

	/**
	 * produces a link to the vehicles page with the given caption.
	 * @param caption {@link String}
	 * @return {@link String}
	 */
	private static final String getLinkVehicles(final String caption) {
		return link(getPathVehicles(),caption);
	}

	/**
	 * produces a link <code> a href = getPathProcess(name) </code>
	 * with the name as caption.
	 * @param processName {@link String}
	 * @return {@link String}
	 */
	private static final String getLinkProcess(final String processName) {
		return link(getPathProcess(processName),processName);
	}

	/**
	 * produces a link to the processes page with the given caption.
	 * @param caption {@link String}
	 * @return {@link String}
	 */
	private static final String getLinkProcesses(final String caption) {
		return link(getPathProcesses(),caption);
	}

	/**
	 * produces a link <code> a href = getPathFoodProductionProcess(name) </code>
	 * with the name as caption.
	 * @param processName {@link String}
	 * @return {@link String}
	 */
	private static final String getLinkFoodProductionProcess(final String processName) {
		return link(getPathFoodProductionProcess(processName),processName);
	}

	/**
	 * produces a link to the Food Production processes page with the given caption.
	 * @param caption {@link String}
	 * @return {@link String}
	 */
	private static final String getLinkFoodProductionProcesses(final String caption) {
		return link(getPathFoodProductionProcesses(),caption);
	}

	private static final String getLinkEquipment(final String equipmentName) {
		return link(getPathEquipment(equipmentName),equipmentName);
	}

	private static final String getLinkEquipments(final String caption) {
		return link(getPathEquipments(),caption);
	}

	/**
	 * produces a link <code> a href = getPathPart(name) </code>
	 * with the name as caption.
	 * @param partName {@link String}
	 * @return {@link String}
	 */
	private static final String getLinkPart(final String partName) {
		return link(getPathPart(partName),partName);
	}

	/**
	 * produces a link to the parts page with the given caption.
	 * @param caption {@link String}
	 * @return {@link String}
	 */
	private static final String getLinkParts(final String caption) {
		return link(getPathParts(),caption);
	}

	private static final String link(final StringBuffer target, final String caption) {
		return new StringBuffer()
		.append("<a href=\"")
		.append(target)
		.append("\">")
		.append(Conversion.capitalize(caption))
		.append("</a>")
		.toString();
/*
		return
		"<a href=\""
		+ target
		+ "\">"
		+ Conversion.capitalize(caption)
		+ "</a>";
*/
	}

	/**
	 * generate help files with vehicle descriptions.
	 */
	private static final void generateVehicleDescriptions() {
		List<String> vehicles = SupplyTableModel.getSortedVehicleTypes();

		// first generate "vehicles.html" with a list of defined vehicles
		StringBuffer content = new StringBuffer()
		.append("<h2>Vehicles</h2>\n")
		.append("<p>Available Types of Vehicles Featured for Mars Surface Operations:</p>")
		.append("<ul>\n");
		for (String vehicle : vehicles) {
			content.append("<li>")
			.append(getLinkVehicle(vehicle))
			.append("</li>\n");
		}
		content.append("</ul>");
		helpFileHeader(content,"vehicles");
		helpFileFooter(content);
		generateFile(getPathVehicles(),content);

		// second loop over vehicle types to generate a help file for each one
		String[] cargoArray = new String[] {
			"hydrogen",
			"methane",
			LifeSupportType.OXYGEN,
			LifeSupportType.WATER,
			LifeSupportType.FOOD,
			"dessert",
			"rock samples",
			"ice"
		};
		VehicleConfig config = SimulationConfig.instance().getVehicleConfiguration();
		for (String vehicle : vehicles) {
			VehicleDescription v = config.getVehicleDescription(vehicle);
			String description = v.getDescription();
			if (description == null)
				description = "No Description is Available";
			content = new StringBuffer()
			.append("<h2>Vehicle \"")
			.append(vehicle)
			.append("\"</h2>\n")
			.append("</p></p>\n")
			.append("<p>")
			.append(getLinkVehicles("Back to Vehicles Overview"))
			.append("</p><br/>\n")
			.append("<p>")
			.append(description)
			.append("</p><br/>")
			.append("<table>\n");

			if (v.hasPartAttachments()) {
				StringBuffer parts = new StringBuffer().append("[");
				Collection<Part> partsCollection = config.getAttachableParts(vehicle);
				if (partsCollection != null) {
					Iterator<Part> iterator = partsCollection.iterator();
					while (iterator.hasNext()) {
						Part part = iterator.next();
						parts.append(getLinkPart(part.getName()));
						if (iterator.hasNext()) {
							parts.append(", ");
						}
					}
					helpFileTableRow(content,new String[] {"Attachable Parts",parts.append("]").toString()});
					helpFileTableRow(content,new String[] {"Attachment Slots",Integer.toString(v.getAttachmentSlots())});
				}
			}
			helpFileTableRow(content,new String[] {"Base Speed",Double.toString(v.getBaseSpeed())});
			helpFileTableRow(content,new String[] {"Total Cargo Capacity",Double.toString(v.getTotalCapacity())});
			for (String cargo : cargoArray) {
				Double capacity = v.getCargoCapacity(cargo);
				if (capacity > 0.0) {
					StringBuffer caption = new StringBuffer()
					.append("Cargo Capacity for ")
					.append(getLinkResource(cargo));
					helpFileTableRow(
						content,
						new String[] {
							caption.toString(),
							Double.toString(capacity)
						}
					);
				}
			}
			helpFileTableRow(content,new String[] {"Crew Size",Integer.toString(v.getCrewSize())});
			helpFileTableRow(content,new String[] {"Empty Mass",Double.toString(v.getEmptyMass())});
			helpFileTableRow(content,new String[] {"Fuel Efficiency",Double.toString(v.getDriveTrainEff())});
			if (v.hasLab()) {
				helpFileTableRow(content,new String[] {"Lab Tech Level",Integer.toString(v.getLabTechLevel())});
				helpFileTableRow(content,new String[] {"Lab Specialties",v.getLabTechSpecialties().toString()});
			}
			if (v.hasSickbay()) {
				helpFileTableRow(content,new String[] {"Sickbay Tech Level",Integer.toString(v.getSickbayTechLevel())});
				helpFileTableRow(content,new String[] {"Sickbay Beds",Integer.toString(v.getSickbayBeds())});
			}
			helpFileTableRow(content,new String[] {"Width",Double.toString(v.getWidth())});
			helpFileTableRow(content,new String[] {"Length",Double.toString(v.getLength())});

			content.append("</table>\n");

			helpFileHeader(content, "Vehicle \"" + vehicle + "\"");
			helpFileFooter(content);
			generateFile(getPathVehicle(vehicle),content);
		}
	}

	/**
	 * generate help files with resources descriptions.
	 */
	//2016-04-17 Added checking for edible
	private static final void generateResourceDescriptions() {
		//System.out.println("Calling generateResourceDescriptions()");
		//Map<String,AmountResource> resources = ResourceUtil.getAmountResourcesMap();
		//Map<Integer, AmountResource> resources = AmountResource.getAmountResourcesIDMap();
		
		ResourceUtil.getInstance().createMaps();
		List<AmountResource> resources = ResourceUtil.getSortedAmountResources();
		
		// first: generate "resources.html" with a list of defined resources
		StringBuffer content = new StringBuffer()
		.append("<h2>Amount Resources</h2>\n")
		.append("<p>Available Types of Resources :</p>")
		.append("<table>\n");

		//System.out.println("Done with making content");

		//for (Map.Entry<String,AmountResource> entry : resources.entrySet()) {
		//for (Entry<Integer, AmountResource> entry : resources.entrySet()) {
			//AmountResource resource = entry.getValue();
			//String name = entry.getKey();
		for (AmountResource resource : resources) {
			String name = resource.getName();	
			//int id = entry.getKey();
			String life = resource.isLifeSupport() ? "   (Life Support)" : "";
			String edible = resource.isEdible() ? "   (Edible)" : "";

			helpFileTableRow(
				content,
				new String[] {
					getLinkResource(name),
					"   ",
					Conversion.capitalize(resource.getPhase().getName()),
					life,
					edible
				}
			);
		}

		//System.out.println("Done with making all rows");

		content.append("</table>\n");

		helpFileHeader(content,"resources");
		helpFileFooter(content);
		generateFile(getPathResources(),content);

		//System.out.println("generateResourceDescriptions(): done with part 1");

		// STEP 2 :
	
		// loop over resource types to generate a help file for each one
		//for (Map.Entry<String,AmountResource> entry : resources.entrySet()) {
		//	AmountResource resource = entry.getValue();
		//	String name = entry.getKey();
		for (AmountResource resource : resources) {
			String name = resource.getName();	
			String description = resource.getDescription();
			if (description == null)
				description = "No Description is Available";
			content = new StringBuffer()
			.append("<h2>Resource : \"")
			.append(Conversion.capitalize(name))
			.append("\" (")
			.append(Conversion.capitalize(resource.getPhase().getName()))
			.append(")\t</h2>\n")
			.append("<br/>")
			.append("<p>")
			.append(getLinkResources("Back to Resources Overview"))
			.append("</p>")
			.append("<br/>")
			.append("1. Description :<br/>\n")
			.append("<p><ul><li>")
			.append(description)
			.append("</li></ul></p><br/>");
			if (resource.isLifeSupport()) {
				content.append("<p>this resource is needed for life support.</p>\n");
			}

			content.append("\n2. Manufacturing Processes : \n");

			// list of manufacturing processes with the current resource as output
			List<ManufactureProcessInfo> output = ManufactureUtil
			.getManufactureProcessesWithGivenOutput(name);
			content.append("<p><u>How to make ")
			.append(name)
			.append(":</u></p>\n");
			if (output.size() > 0) {
				content.append("<ul>\n");
				for (ManufactureProcessInfo info : output) {
					content.append("\t<li>")
					.append(getLinkProcess(info.getName()))
					.append("</li>\n");
				}
				content.append("</ul>\n");
			} else helpFileNoSuchProcess(content);

			// list of manufacturing processes with the current resource as input
			List<ManufactureProcessInfo> input = ManufactureUtil
			.getManufactureProcessesWithGivenInput(name);
			content.append("<p><u>What to do with ")
			.append(name)
			.append(":</u></p>\n");
			if (input.size() > 0) {
				content.append("<ul>\n");
				for (ManufactureProcessInfo info : input) {
					content.append("\t<li>")
					.append(getLinkProcess(info.getName()))
					.append("</li>\n");
				}
				content.append("</ul>\n");
			} else helpFileNoSuchProcess(content);

			content.append("\n3. Food Production Processes : \n");

			// list of food production processes with the current resource as output
			List<FoodProductionProcessInfo> output_fp = FoodProductionUtil
			.getFoodProductionProcessesWithGivenOutput(name);
			content.append("<p><u>How to make ")
			.append(name)
			.append(":</u></p>\n");
			if (output_fp.size() > 0) {
				content.append("<ul>\n");
				for (FoodProductionProcessInfo info : output_fp) {
					content.append("\t<li>")
					.append(getLinkFoodProductionProcess(info.getName()))
					.append("</li>\n");
				}
				content.append("</ul>\n");
			} else helpFileNoSuchFoodProductionProcess(content);

			// list of food production processes with the current resource as input
			List<FoodProductionProcessInfo> input_fp = FoodProductionUtil
			.getFoodProductionProcessesWithGivenInput(name);
			content.append("<p><u>What to do with ")
			.append(name)
			.append(":</u></p>\n");
			if (input_fp.size() > 0) {
				content.append("<ul>\n");
				for (FoodProductionProcessInfo info : input_fp) {
					content.append("\t<li>")
					.append(getLinkFoodProductionProcess(info.getName()))
					.append("</li>\n");
				}
				content.append("</ul>\n");
			} else helpFileNoSuchFoodProductionProcess(content);

			// finalize and generate help file
			helpFileHeader(content,"Resource \"" + resource + "\"");
			helpFileFooter(content);
			generateFile(getPathResource(name),content);

		}
	}

	/**
	 * generate help files with parts descriptions.
	 */
	private static final void generatePartsDescriptions() {
		//Map<String, ItemResource> parts = ItemResource.getItemResourcesMap();
		//Map<String, Part> parts = ItemResource.getItemResourcesMap();
		List<Part> parts = ItemResourceUtil.getSortedParts();
		
		// first: generate "parts.html" with a list of defined equipment parts
		StringBuffer content = new StringBuffer()
		.append("<h2>Parts</h2>\n")
		.append("<p>Available Types of Parts and Equipments :</p>")
		.append("<ul>\n");
		
		//for (String part : parts.keySet()) {
		for (Part part : parts) {		
			String name = part.getName();
			content.append("\t<li>")
			.append(getLinkPart(name))
			.append("</li>\n");
		}
		content.append("</ul>\n");
		helpFileHeader(content,"parts");
		helpFileFooter(content);
		generateFile(getPathParts(),content);

		// second: loop over part types to generate a help file for each one
		//for (Entry<String, ItemResource> entry : parts.entrySet()) {
		//	Part part = (Part) entry.getValue();
		//for (Entry<String, Part> entry : parts.entrySet()) {
		//	Part part = entry.getValue();
		//	String name = entry.getKey();
		for (Part part : parts) {
			String name = part.getName();
			String description = part.getDescription();
			if (description == null)
				description = "No Description is Available";
			content = new StringBuffer()
			.append("<h2>Part : \"")
			.append(Conversion.capitalize(name))
			.append("\"</h2>\n")
			.append("</p><br/>")
			.append("<p>")
			.append(getLinkParts("Back to Parts Overview"))
			.append("</p><br/>")
			.append("1. Mass Per Unit : ")
			.append(Double.toString(part.getMassPerItem()))
			.append("kg<br/><br/>\n")
			.append("2. Description :<p><ul><li>\n")
			.append(description)
			.append("</li></ul></p><br/>");

			content.append("\n3. Manufacturing Processes : \n");

			// list of manufacturing processes with the current part as output
			List<ManufactureProcessInfo> output = ManufactureUtil
			.getManufactureProcessesWithGivenOutput(name);
			content.append("<p><u>a. How to make ")
			.append(name)
			.append(":</u></p>\n");
			if (output.size() > 0) {
				content.append("<ul>\n");
				for (ManufactureProcessInfo info : output) {
					content.append("\t<li>")
					.append(getLinkProcess(info.getName()))
					.append("</li>\n");
				}
				content.append("</ul>\n");
			} else helpFileNoSuchProcess(content);
			content.append("</p>\n");
			// list of manufacturing processes with the current part as input
			List<ManufactureProcessInfo> input = ManufactureUtil
			.getManufactureProcessesWithGivenInput(name);
			content.append("<p><u>b. What to do with ")
			.append(name)
			.append(":</u></p>\n");
			if (input.size() > 0) {
				content.append("<ul>\n");
				for (ManufactureProcessInfo info : input) {
					content.append("\t<li>")
					.append(getLinkProcess(info.getName()))
					.append("</li>\n");
				}
				content.append("</ul>\n");
			} else helpFileNoSuchProcess(content);

			content.append("\n4. Food Production Processes : \n");

			// list of food production processes with the current resource as output
			List<FoodProductionProcessInfo> output_fp = FoodProductionUtil
			.getFoodProductionProcessesWithGivenOutput(name);
			content.append("<p><u>How to make ")
			.append(name)
			.append(":</u></p>\n");
			if (output_fp.size() > 0) {
				content.append("<ul>\n");
				for (FoodProductionProcessInfo info : output_fp) {
					content.append("\t<li>")
					.append(getLinkFoodProductionProcess(info.getName()))
					.append("</li>\n");
				}
				content.append("</ul>\n");
			} else helpFileNoSuchFoodProductionProcess(content);

			// list of food production processes with the current resource as input
			List<FoodProductionProcessInfo> input_fp = FoodProductionUtil
			.getFoodProductionProcessesWithGivenInput(name);
			content.append("<p><u>What to do with ")
			.append(name)
			.append(":</u></p>\n");
			if (input_fp.size() > 0) {
				content.append("<ul>\n");
				for (FoodProductionProcessInfo info : input_fp) {
					content.append("\t<li>")
					.append(getLinkFoodProductionProcess(info.getName()))
					.append("</li>\n");
				}
				content.append("</ul>\n");
			} else helpFileNoSuchFoodProductionProcess(content);

			// finalize and generate help file
			helpFileHeader(content,"Part \"" + part + "\"");
			helpFileFooter(content);
			generateFile(getPathPart(name),content);

		}
	}

	/**
	 * generate help files with manufacturing process descriptions.
	 */
	private static final void generateProcessDescriptions() {
		TreeMap<String,ManufactureProcessInfo> processes = ManufactureUtil.getAllManufactureProcessesMap();

		// first: generate "processes.html" with a list of defined processes
		String[] header = new String[] {
			"<b>Tech  </b>",
			"<b>Skill  </b>",
			"<b>Work  </b>",
			"<b>Time  </b>",
			"<b>Power  </b>",
			"<b>Name</b>"
		};
		StringBuffer content = new StringBuffer()
		.append("<h2>Processes</h2>\n")
		.append("<p>Available Types of Manufacturing Processes:</p>\n")
		.append("<table>\n");
		helpFileTableRow(content,header);
		for (Entry<String,ManufactureProcessInfo> process : processes.entrySet()) {
			String name = process.getKey();
			ManufactureProcessInfo info = process.getValue();
			helpFileTableRow(
				content,
				new String[] {
					Integer.toString(info.getTechLevelRequired()),
					Integer.toString(info.getSkillLevelRequired()),
					Double.toString(info.getWorkTimeRequired()),
					Double.toString(info.getProcessTimeRequired()),
					Double.toString(info.getPowerRequired()),
					getLinkProcess(name)
				}
			);
		}
		helpFileTableRow(content,header);
		content.append("</table>\n");
		helpFileHeader(content,"processes");
		helpFileFooter(content);
		generateFile(getPathProcesses(),content);

		// second: loop over processes to generate a help file for each one
		for (Entry<String,ManufactureProcessInfo> process : processes.entrySet()) {
			String name = process.getKey();
			ManufactureProcessInfo info = process.getValue();
			String description = info.getDescription();
			if (description == null)
				description = "No Description is Available";
			content = new StringBuffer()
			.append("<h2>Process : \"")
			.append(Conversion.capitalize(name))
			.append("\"</h2>\n")
			.append("<br/>")
			.append(getLinkProcesses("Back to Processes Overview"))
			.append("</br></br>\n")
			.append("1. Description :\n")
			.append("<p><ul><li>")
			.append(description)
			.append("</li></ul></p><br/>")
			.append("2. Characteristics :\n")
			.append("<table>\n");
			helpFileTableRow(content,new String [] {"Required Building Tech Level   ",Integer.toString(info.getTechLevelRequired())});
			helpFileTableRow(content,new String [] {"Required Skill Level",Integer.toString(info.getSkillLevelRequired())});
			helpFileTableRow(content,new String [] {"Work Time in millisols",Double.toString(info.getWorkTimeRequired())});
			helpFileTableRow(content,new String [] {"Time in millisols",Double.toString(info.getProcessTimeRequired())});
			helpFileTableRow(content,new String [] {"Power Requirement",Double.toString(info.getPowerRequired())});
			content.append("</table>\n")
			.append("<br/>\n")
			.append("3. Process Inputs :\n")
			.append("<table><ul>\n");
			for (ManufactureProcessItem input : info.getInputList()) {
				String inputName = input.getName();
				ItemType inputType = input.getType();
				String link = getLink_ResourceType(inputType,inputName);
				String type = getLink_ResourceLink(inputType);
				helpFileTableRow(
					content,
					new String[] {
						"<li>", type, "   ",
						Double.toString(input.getAmount()), "   ",
						link, "</li>"
					}
				);
			}
			content.append("</table>\n")
			.append("<br/>\n")
			.append("4. Process Outputs :\n")
			.append("<table><ul>\n");
			for (ManufactureProcessItem output : info.getOutputList()) {
				String outputName = output.getName();
				ItemType outputType = output.getType();
				String link = getLink_ResourceType(outputType,outputName);
				String type = getLink_ResourceLink(outputType);
				helpFileTableRow(
					content,
					new String[] {
						"<li>", type, "   ",
						Double.toString(output.getAmount()), "   ",
						link, "</li>"
					}
				);
			}
			content.append("</ul></table>\n");

			// finalize and generate help file
			helpFileHeader(content,"Process \"" + name + "\"");
			helpFileFooter(content);
			generateFile(getPathProcess(name),content);
		}
	}


	/**
	 * generate help files with food production process descriptions.
	 */
	private static final void generateFoodProductionDescriptions() {
		TreeMap<String,FoodProductionProcessInfo> processes = FoodProductionUtil.getAllFoodProductionProcessesMap();

		// first: generate "foodProduction.html" with a list of defined processes
		String[] header = new String[] {
			"<b>Tech  </b>",
			"<b>Skill  </b>",
			"<b>Work  </b>",
			"<b>Time  </b>",
			"<b>Power  </b>",
			"<b>Name</b>"
		};
		StringBuffer content = new StringBuffer()
		.append("<h2>Food Production</h2>\n")
		.append("<p>Available Types of Food Production Processes:</p>\n")
		.append("<table><ul>\n");
		helpFileTableRow(content,header);
		for (Entry<String,FoodProductionProcessInfo> process : processes.entrySet()) {
			String name = process.getKey();
			FoodProductionProcessInfo info = process.getValue();
			helpFileTableRow(
				content,
				new String[] {
					Integer.toString(info.getTechLevelRequired()),
					Integer.toString(info.getSkillLevelRequired()),
					Double.toString(info.getWorkTimeRequired()),
					Double.toString(info.getProcessTimeRequired()),
					Double.toString(info.getPowerRequired()),
					getLinkFoodProductionProcess(name)
				}
			);
		}
		helpFileTableRow(content,header);
		content.append("</ul></table>\n");
		helpFileHeader(content,"Food Production");
		helpFileFooter(content);
		generateFile(getPathFoodProductionProcesses(),content);

		// second: loop over processes to generate a help file for each one
		for (Entry<String,FoodProductionProcessInfo> process : processes.entrySet()) {
			String name = process.getKey();
			FoodProductionProcessInfo info = process.getValue();
			String description = info.getDescription();
			if (description == null)
				description = "No Description is Available";
			content = new StringBuffer()
			.append("<h2>Food Production \"")
			.append(Conversion.capitalize(name))
			.append("\"</h2>\n")
			.append("<br/>")
			.append(getLinkFoodProductionProcesses("Back to Food Production Overview"))
			.append("</br></br>\n")
			.append("1. Description :\n")
			.append("<p><ul><li>")
			.append(description)
			.append("</li></ul></p><br/>")
			.append("2. Characteristics :\n")
			.append("<table>\n");
			helpFileTableRow(content,new String [] {"Required Building Tech Level   ",Integer.toString(info.getTechLevelRequired())});
			helpFileTableRow(content,new String [] {"Required Skill Level",Integer.toString(info.getSkillLevelRequired())});
			helpFileTableRow(content,new String [] {"Work Time in millisols",Double.toString(info.getWorkTimeRequired())});
			helpFileTableRow(content,new String [] {"Time in millisols",Double.toString(info.getProcessTimeRequired())});
			helpFileTableRow(content,new String [] {"Power Requirement",Double.toString(info.getPowerRequired())});
			content.append("</table>\n")
			.append("<br/>\n")
			.append("3. Process Inputs :\n")
			.append("<table><ul>\n");
			for (FoodProductionProcessItem input : info.getInputList()) {
				String inputName = input.getName();
				ItemType inputType = input.getType();
				String link = getLink_ResourceType(inputType,inputName);
				String type = getLink_ResourceLink(inputType);
				helpFileTableRow(
					content,
					new String[] {
						"<li>", type, "   ",
						Double.toString(input.getAmount()), "   ",
						link, "</li>"
					}
				);
			}
			content.append("</ul></table>\n")
			.append("<br/>\n")

			.append("4. Process Outputs :\n")
			.append("<table><ul>\n");
			for (FoodProductionProcessItem output : info.getOutputList()) {
				String outputName = output.getName();
				ItemType outputType = output.getType();
				String link = getLink_ResourceType(outputType,outputName);
				String type = getLink_ResourceLink(outputType);
				helpFileTableRow(
					content,
					new String[] {
						"<li>", type, "   ",
						Double.toString(output.getAmount()), "   ",
						link, "</li>"
					}
				);
			}
			content.append("</ul></table>\n");

			// finalize and generate help file
			helpFileHeader(content,"Food Production \"" + name + "\"");
			helpFileFooter(content);
			generateFile(getPathFoodProductionProcess(name),content);
		}
	}

	private static String getLink_ResourceType(ItemType type, String name) {
		String link;
		switch (type) {
			case AMOUNT_RESOURCE : {
				link = getLinkResource(name);
				break;
			}
			case EQUIPMENT : {
				link = getLinkEquipment(name);
				break;
			}
			case PART : {
				link = getLinkPart(name);
				break;
			}
			case VEHICLE : {
				link = getLinkVehicle(name);
				break;
			}
			default : link = "";
		}
		return link;
	}

	private static String getLink_ResourceLink(ItemType type) {
		String link;
		switch (type) {
			case AMOUNT_RESOURCE : {
				link = getLinkResources("resource");
				break;
			}
			case EQUIPMENT : {
				link = getLinkEquipments("equipment");
				break;
			}
			case PART : {
				link = getLinkParts("part");
				break;
			}
			case VEHICLE : {
				link = getLinkVehicles("vehicle");
				break;
			}
			default : link = "";
		}
		return link;
	}

	/**
	 * generate html help files for use in the in-game help and tutorial browser.
	 */
	public static final void generateHtmlHelpFiles() {
		logger.log(Level.INFO,"Starting to generate help files");
		//System.out.println("Calling generateHtmlHelpFiles()");
		HelpGenerator.generateVehicleDescriptions();
		logger.log(Level.INFO,"generateVehicleDescriptions() is done");
		HelpGenerator.generateResourceDescriptions();
		logger.log(Level.INFO,"generateResourceDescriptions() is done");
		HelpGenerator.generatePartsDescriptions();
		logger.log(Level.INFO,"generatePartsDescriptions() is done");
		HelpGenerator.generateProcessDescriptions();
		logger.log(Level.INFO,"generateProcessDescriptions() is done");

		//2016-04-18 Added generateFoodProductionDescriptions();
		HelpGenerator.generateFoodProductionDescriptions();
		logger.log(Level.INFO,"generateFoodProductionDescriptions() is done");

		//TODO: will create HelpGenerator.generateMealsDescriptions();

		logger.log(
			Level.INFO,
			new StringBuffer()
				.append("Files Generated: ")
				.append(Integer.toString(filesGenerated))
				.append("  Failed: ")
				.append(Integer.toString(filesNotGenerated))
			.toString()
		);

		System.exit(0);
	}
}

/**
 * this can be run at development time to generate .html-files
 * for the in-game help tool. or can be started with every
 * run of the simulation.
 * @author stpa
 * 2015-05-12
 * TODO make the generated help files internationalizable.
 */

package org.mars_sim.msp.helpGenerator;

import java.io.File;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.LifeSupportType;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.manufacture.ManufactureProcessInfo;
import org.mars_sim.msp.core.manufacture.ManufactureProcessItem;
import org.mars_sim.msp.core.manufacture.ManufactureUtil;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.ItemResource;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.resource.Type;
import org.mars_sim.msp.core.vehicle.VehicleConfig;
import org.mars_sim.msp.core.vehicle.VehicleConfig.VehicleDescription;
import org.mars_sim.msp.ui.swing.tool.resupply.SupplyTableModel;


public class HelpGenerator {

	/** initialized logger for this class. */
	private static Logger logger = Logger.getLogger(HelpGenerator.class.getName());

	/*
	 * absolute path for creating help files with command
	 * line argument -generateHelp only when needed.
	 *
	private static final String ABSOLUTE_DIR = "X:/path/to/your/workspace/code/mars-sim/mars-sim-ui/src/main/resources/docs/help";
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
		.append("<!DOCTYPE HTML>\n")
		.append("<!-- generated for mars-sim by st.pa. -->\n")
		.append("<html>\n")
		.append("\t<head>\n")
		.append("\t\t<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n")
		.append("\t\t<title>")
		.append("Mars Simulation Project - generated help file - ")
		.append(title)
		.append("</title>\n")
		.append("\t\t<link rel=\"stylesheet\" href=\"../msp.css\">\n")
		.append("\t</head>\n")
		.append("\t<body>\n\n");
		s.insert(0,header.toString());
	}

	private static final void helpFileFooter(final StringBuffer s) {
		s.append("\n\t</body>\n");
		s.append("</html>\n");
	}

	private static final void helpFileTableRow(
		final StringBuffer s,
		final String[] columnContents
	) {
		s.append("\t<tr>\n");
		for (String columnContent : columnContents) {
			s.append("\t\t<td>");
			s.append(columnContent.replace(" ","&nbsp;"));
			s.append("</td>\n");
		}
		s.append("\t</tr>\n");
	}

	private static final void helpFileNoSuchProcess(final StringBuffer content) {
		content.append("<p>&nbsp;&nbsp;no such manufacturing processes known.</p>");
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
		return link(getPathResource(resourceName),resourceName);
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
		.append(caption)
		.append("</a>")
		.toString();
	}

	/**
	 * generate help files with vehicle descriptions.
	 */
	private static final void generateVehicleDescriptions() {
		List<String> vehicles = SupplyTableModel.getSortedVehicleTypes();

		// first generate "vehicles.html" with a list of defined vehicles
		StringBuffer content = new StringBuffer()
		.append("<h2>Vehicles</h2>\n")
		.append("<p>MSP features several types of vehicles for use on the Mars surface. Here are the default ones defined:</p>")
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
			content = new StringBuffer()
			.append("<h2>Vehicle \"")
			.append(vehicle)
			.append("\"</h2>\n")
			.append("</p></p>\n")
			.append("<p>")
			.append(getLinkVehicles("Back to vehicles overview"))
			.append("</p><br/>\n")
			.append("<p>")
			.append(v.getDescription())
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
					helpFileTableRow(content,new String[] {"attachable parts",parts.append("]").toString()});
					helpFileTableRow(content,new String[] {"attachment slots",Integer.toString(v.getAttachmentSlots())});
				}
			}
			helpFileTableRow(content,new String[] {"base speed",Double.toString(v.getBaseSpeed())});
			helpFileTableRow(content,new String[] {"total cargo capacity",Double.toString(v.getTotalCapacity())});
			for (String cargo : cargoArray) {
				Double capacity = v.getCargoCapacity(cargo);
				if (capacity > 0.0) {
					StringBuffer caption = new StringBuffer()
					.append("cargo capacity for ")
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
			helpFileTableRow(content,new String[] {"crew size",Integer.toString(v.getCrewSize())});
			helpFileTableRow(content,new String[] {"empty mass",Double.toString(v.getEmptyMass())});
			helpFileTableRow(content,new String[] {"fuel efficiency",Double.toString(v.getFuelEff())});
			if (v.hasLab()) {
				helpFileTableRow(content,new String[] {"lab tech level",Integer.toString(v.getLabTechLevel())});
				helpFileTableRow(content,new String[] {"lab specialties",v.getLabTechSpecialties().toString()});
			}
			if (v.hasSickbay()) {
				helpFileTableRow(content,new String[] {"sickbay tech level",Integer.toString(v.getSickbayTechLevel())});
				helpFileTableRow(content,new String[] {"sickbay beds",Integer.toString(v.getSickbayBeds())});
			}
			helpFileTableRow(content,new String[] {"width",Double.toString(v.getWidth())});
			helpFileTableRow(content,new String[] {"length",Double.toString(v.getLength())});

			content.append("</table>\n");

			helpFileHeader(content,"vehicle \"" + vehicle + "\"");
			helpFileFooter(content);
			generateFile(getPathVehicle(vehicle),content);
		}
	}

	/**
	 * generate help files with resources descriptions.
	 */
	private static final void generateResourceDescriptions() {
		Map<String,AmountResource> resources = AmountResource.getAmountResourcesMap();

		// first: generate "resources.html" with a list of defined resources
		StringBuffer content = new StringBuffer()
		.append("<h2>Resources</h2>\n")
		.append("<p>These are all the default resources:</p>\n")
		.append("<table>\n");

		for (Entry<String,AmountResource> entry : resources.entrySet()) {
			AmountResource resource = entry.getValue();
			String name = entry.getKey();
			String life = resource.isLifeSupport() ? " (life support)" : "";
			helpFileTableRow(
				content,
				new String[] {
					getLinkResource(name),
					resource.getPhase().getName(),
					life
				}
			);
		}

		content.append("</table>\n");

		helpFileHeader(content,"resources");
		helpFileFooter(content);
		generateFile(getPathResources(),content);

		// second: loop over resource types to generate a help file for each one
		for (Entry<String,AmountResource> entry : resources.entrySet()) {
			AmountResource resource = entry.getValue();
			String name = entry.getKey();
			content = new StringBuffer()
			.append("<h2>Resource \"")
			.append(name)
			.append("\" ")
			.append(resource.getPhase().getName())
			.append("\t</h2>\n")
			.append("<br/>\n")
			.append("<p>")
			.append(getLinkResources("Back to resources overview"))
			.append("</p><br/>\n")
			.append("<p>")
			.append(resource.getDescription())
			.append("</p><br/>\n");
			if (resource.isLifeSupport()) {
				content.append("<p>this resource is needed for life support.</p>\n");
			}
			// list of manufacturing processes with the current resource as output
			List<ManufactureProcessInfo> output = ManufactureUtil
			.getManufactureProcessesWithGivenOutput(name);
			content.append("<p><u>how to make ")
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
			content.append("<p><u>what to do with ")
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
			// finalize and generate help file
			helpFileHeader(content,"resource \"" + resource + "\"");
			helpFileFooter(content);
			generateFile(getPathResource(name),content);
		}
	}

	/**
	 * generate help files with parts descriptions.
	 */
	private static final void generatePartsDescriptions() {
		TreeMap<String,Part> parts = ItemResource.getItemResourcesMap();

		// first: generate "parts.html" with a list of defined equipment parts
		StringBuffer content = new StringBuffer()
		.append("<h2>Parts</h2>\n")
		.append("<p>These are all the default equipment parts:</p>\n")
		.append("<ul>\n");
		for (String part : parts.keySet()) {
			content.append("\t<li>")
			.append(getLinkPart(part))
			.append("</li>\n");
		}
		content.append("</ul>\n");
		helpFileHeader(content,"parts");
		helpFileFooter(content);
		generateFile(getPathParts(),content);

		// second: loop over part types to generate a help file for each one
		for (Entry<String,Part> entry : parts.entrySet()) {
			Part part = entry.getValue();
			String name = entry.getKey();
			content = new StringBuffer()
			.append("<h2>part \"")
			.append(name)
			.append("\"</h2>\n")
			.append("</p><br/>\n")
			.append("<p>")
			.append(getLinkParts("Back to parts overview"))
			.append("</p><br/>\n")
			.append("<p>mass per unit: ")
			.append(Double.toString(part.getMassPerItem()))
			.append("kg</p><br/>\n")
			.append("<p>")
			.append(part.getDescription())
			.append("</p><br/>\n");
			// list of manufacturing processes with the current part as output
			List<ManufactureProcessInfo> output = ManufactureUtil
			.getManufactureProcessesWithGivenOutput(name);
			content.append("<p><u>how to make ")
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
			content.append("<p><u>what to do with ")
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
			// finalize and generate help file
			helpFileHeader(content,"part \"" + part + "\"");
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
			"<b>tech</b>",
			"<b>skill</b>",
			"<b>work</b>",
			"<b>time</b>",
			"<b>power</b>",
			"<b>name</b>"
		};
		StringBuffer content = new StringBuffer()
		.append("<h2>Processes</h2>\n")
		.append("<p>These are all the default manufacturing processes:</p>\n")
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
			content = new StringBuffer()
			.append("<h2>Process \"")
			.append(name)
			.append("\"</h2>\n")
			.append("<br/>")
			.append(getLinkProcesses("Back to processes overview"))
			.append("</br>\n")
			.append("<p>")
			.append(info.getDescription())
			.append("</p><br/>\n")
			.append("<table>\n");
			helpFileTableRow(content,new String [] {"required building tech level",Integer.toString(info.getTechLevelRequired())});
			helpFileTableRow(content,new String [] {"required skill level",Integer.toString(info.getSkillLevelRequired())});
			helpFileTableRow(content,new String [] {"work time in millisols",Double.toString(info.getWorkTimeRequired())});
			helpFileTableRow(content,new String [] {"time in millisols",Double.toString(info.getProcessTimeRequired())});
			helpFileTableRow(content,new String [] {"power requirement",Double.toString(info.getPowerRequired())});
			content.append("</table>\n")
			.append("<br/>\n")
			.append("<p><u>process inputs:</u></p>\n")
			.append("<table>\n");
			for (ManufactureProcessItem input : info.getInputList()) {
				String inputName = input.getName();
				Type inputType = input.getType();
				String link = getLink_ResourceType(inputType,inputName);
				String type = getLink_ResourceLink(inputType);
				helpFileTableRow(
					content,
					new String[] {
						type,
						Double.toString(input.getAmount()),
						link
					}
				);
			}
			content.append("</table>\n")
			.append("<br/>\n")
			.append("<p><u>process outputs:</u></p>\n")
			.append("<table>\n");
			for (ManufactureProcessItem output : info.getOutputList()) {
				String outputName = output.getName();
				Type outputType = output.getType();
				String link = getLink_ResourceType(outputType,outputName);
				String type = getLink_ResourceLink(outputType);
				helpFileTableRow(
					content,
					new String[] {
						type,
						Double.toString(output.getAmount()),
						link
					}
				);
			}
			content.append("</table>\n");

			// finalize and generate help file
			helpFileHeader(content,"process \"" + name + "\"");
			helpFileFooter(content);
			generateFile(getPathProcess(name),content);
		}
	}

	private static String getLink_ResourceType(Type type, String name) {
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

	private static String getLink_ResourceLink(Type type) {
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
		
		HelpGenerator.generateVehicleDescriptions();
		logger.log(Level.INFO,"generateVehicleDescriptions() is done");
		HelpGenerator.generateResourceDescriptions();
		logger.log(Level.INFO,"generateResourceDescriptions() is done");
		HelpGenerator.generatePartsDescriptions();
		logger.log(Level.INFO,"generatePartsDescriptions() is done");
		HelpGenerator.generateProcessDescriptions();
		//TODO: will create HelpGenerator.generateFoodProductionDescriptions();
		//TODO: will create HelpGenerator.generateMealsDescriptions();
		logger.log(Level.INFO,"generateProcessDescriptions() is done");

		logger.log(
			Level.INFO,
			new StringBuffer()
				.append("generated ")
				.append(Integer.toString(filesGenerated))
				.append(" help files. failures: ")
				.append(Integer.toString(filesNotGenerated))
			.toString()
		);
	}
}

/**
 * Mars Simulation Project
 * SettlementConfig.java
 * @version 3.1.0 2017-01-21
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure;

import org.jdom.Document;
import org.jdom.Element;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.interplanetary.transport.resupply.ResupplyMissionTemplate;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.resource.PartPackageConfig;
import org.mars_sim.msp.core.structure.BuildingTemplate.BuildingConnectionTemplate;

import java.io.Serializable;
import java.util.*;
import java.util.logging.Logger;


/**
 * Provides configuration information about settlements.
 * Uses a DOM document to get the information.
 */
public class SettlementConfig
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 2L;

	private static Logger logger = Logger.getLogger(SettlementConfig.class.getName());

	// Element names
	private static final String ROVER_LIFE_SUPPORT_RANGE_ERROR_MARGIN = "rover-life-support-range-error-margin";
	private static final String ROVER_FUEL_RANGE_ERROR_MARGIN = "rover-fuel-range-error-margin";
	private static final String MISSION_CONTROL = "mission-control";
	private static final String LIFE_SUPPORT_REQUIREMENTS = "life-support-requirements";
	private static final String TOTAL_PRESSURE = "total-pressure";// low="99.9" high="102.7" />
	private static final String PARTIAL_PRESSURE_OF_O2 = "partial-pressure-of-oxygen"; //low="19.5" high="23.1" />
	private static final String PARTIAL_PRESSURE_OF_N2 = "partial-pressure-of-nitrogen";// low="79" high="79"/>
	private static final String PARTIAL_PRESSURE_OF_CO2 = "partial-pressure-of-carbon-dioxide"; //low=".4" high=".4" />
	private static final String TEMPERATURE = "temperature";// low="18.3" high="23.9"/>
	private static final String RELATIVE_HUMIDITY = "relative-humidity"; //low="30" high="70"/>
	private static final String VENTILATION = "ventilation";//
	private static final String LOW = "low";
	private static final String HIGH = "high";
	private static final String SETTLEMENT_TEMPLATE_LIST = 	"settlement-template-list";
	private static final String TEMPLATE = "template";
	private static final String NAME = "name";
	private static final String DEFAULT_POPULATION = "default-population";
	private static final String DEFAULT_NUM_ROBOTS = "number-of-robots";
	private static final String BUILDING = "building";
	private static final String ID = "id";
	private static final String TYPE = "type";
	private static final String WIDTH = "width";
	private static final String LENGTH = "length";
	private static final String X_LOCATION = "x-location";
	private static final String Y_LOCATION = "y-location";
	private static final String FACING = "facing";
	private static final String CONNECTION_LIST = "connection-list";
	private static final String CONNECTION = "connection";
	private static final String NUMBER = "number";
	private static final String ROBOTS_NUMBER = "number";
	private static final String VEHICLE = "vehicle";
	private static final String EQUIPMENT = "equipment";
	private static final String INITIAL_SETTLEMENT_LIST = "initial-settlement-list";
	private static final String SETTLEMENT = "settlement";
	private static final String LOCATION = "location";
	private static final String LONGITUDE = "longitude";
	private static final String LATITUDE = "latitude";
	private static final String POPULATION = "population";
	private static final String NUM_OF_ROBOTS = "number-of-robots";
	private static final String SETTLEMENT_NAME_LIST = "settlement-name-list";
	private static final String SETTLEMENT_NAME = "settlement-name";
	private static final String VALUE = "value";
	//2016-08-23 Added sponsor
	private static final String SPONSOR = "sponsor";
	private static final String RESUPPLY = "resupply";
	private static final String RESUPPLY_MISSION = "resupply-mission";
	private static final String ARRIVAL_TIME = "arrival-time";
	private static final String RESOURCE = "resource";
	private static final String AMOUNT = "amount";
	private static final String PART = "part";
	private static final String PART_PACKAGE = "part-package";
	private static final String NEW_ARRIVING_SETTLEMENT_LIST = "new-arriving-settlement-list";
	private static final String ARRIVING_SETTLEMENT = "arriving-settlement";

	// 2015-10-03 Added MAX_MSD and MSD_NUMBER
	//private static final String SPONSOR = "max-MSD";
	//private static final String NAME = "number";

	// Random value indicator.
	public static final String RANDOM = "random";

	private double[] rover_values = new double[] {0,0};

	private double[][] life_support_values = new double[2][7];

	// Data members
	private Collection<SettlementTemplate> settlementTemplates;
	private List<InitialSettlement> initialSettlements;
	private List<NewArrivingSettlement> newArrivingSettlements;
	//private List<ExistingSettlement> existingSettlements;
	//private List<String> settlementNames;
	private Map<String, List<String>> settlementNamesMap = new HashMap<>();
	private Map<Integer, String> scenarioMap = new HashMap<>();
	private Map<Integer, String> settlementMap = new HashMap<>();

	private Document settlementDoc;

	//private MultiplayerClient multiplayerClient;
	/**
	 * Constructor.
	 * @param settlementDoc DOM document with settlement configuration.
	 * @param partPackageConfig the part package configuration.
	 * @throws Exception if error reading XML document.
	 */
	public SettlementConfig(Document settlementDoc, PartPackageConfig partPackageConfig) {
		this.settlementDoc = settlementDoc;
		settlementTemplates = new ArrayList<SettlementTemplate>();
		initialSettlements = new ArrayList<InitialSettlement>();
		newArrivingSettlements = new ArrayList<NewArrivingSettlement>();
		//settlementNames = new ArrayList<String>();
		//existingSettlements = new ArrayList<ExistingSettlement>();

		//loadMissionControl(settlementDoc);
		loadSettlementNames(settlementDoc);
		loadSettlementTemplates(settlementDoc, partPackageConfig);
		loadInitialSettlements(settlementDoc);
		loadNewArrivingSettlements(settlementDoc);

	}

	//public void setMultiplayerClient(MultiplayerClient multiplayerClient) {
	//	return multiplayerClient;
	//}

	public Collection<SettlementTemplate> GetSettlementTemplates() {
		return settlementTemplates;
	}

	/**
	 * Maps a number to an alphabet
	 * @param a number
	 * @return a String
	 */
	// 2014-10-29 Added getCharForNumber()
	private String getCharForNumber(int i) {
		// NOTE: i must be > 1, if i = 0, return null
	    return i > 0 && i < 27 ? String.valueOf((char)(i + 'A' - 1)) : null;
	}


	/**
	 * Load the rover range margin error from the mission control parameters of a settlement from the XML document.
	 * @return range margin.
	 * @throws Exception if error reading XML document.
	 */
	// 2016-10-14 loadMissionControl()
    @SuppressWarnings("unchecked")
	public double[] loadMissionControl() {

		if (rover_values[0] != 0) {
			//System.out.println("using saved rover_values");
			return rover_values;
		}

		else {
			Element root = settlementDoc.getRootElement();
			Element missionControlElement = root.getChild(MISSION_CONTROL);
			Element lifeSupportRange = (Element) missionControlElement.getChild(ROVER_LIFE_SUPPORT_RANGE_ERROR_MARGIN);
			Element fuelRange = (Element) missionControlElement.getChild(ROVER_FUEL_RANGE_ERROR_MARGIN);

			rover_values[0] = Double.parseDouble(lifeSupportRange.getAttributeValue(VALUE));
			if (rover_values[0] < 1.0 || rover_values[0] > 2.0 )
		        throw new IllegalStateException("Error in SettlementConfig.xml: rover life support range error margin is beyond acceptable range");


			rover_values[1]= Double.parseDouble(fuelRange.getAttributeValue(VALUE));
			if (rover_values[1] < 1.0 || rover_values[1] > 2.0 )
		        throw new IllegalStateException("Error in SettlementConfig.xml: rover fuel range error margin is beyond acceptable range");

			return rover_values;
		}
    }

	/**
	 * Load the life support requirements from the XML document.
	 * @return an array of double.
	 * @throws Exception if error reading XML document.
	 */
	// 2016-12-11 loadLifeSupportRequirements()
    @SuppressWarnings("unchecked")
	public double[][] loadLifeSupportRequirements() {

		if (life_support_values[0][0] != 0) {
			// testing only the value at [0][0]
			return life_support_values;
		}

		else {

			Element root = settlementDoc.getRootElement();
			Element req = (Element) root.getChild(LIFE_SUPPORT_REQUIREMENTS);

			String [] types = new String[] {
					TOTAL_PRESSURE,
					PARTIAL_PRESSURE_OF_O2,
					PARTIAL_PRESSURE_OF_N2,
					PARTIAL_PRESSURE_OF_CO2,
					TEMPERATURE,
					RELATIVE_HUMIDITY,
					VENTILATION};

			for (int j = 0; j < 2; j++) {
				for (int i = 0; i < 7; i++) {
					 double t[] = getValues(req, types[i]);
					 life_support_values[j][i] = t[j];
				}
			}
			
			return life_support_values;
		}
    }

    public double[] getValues(Element element, String name) {
		Element el = (Element) element.getChild(name);

		double a = Double.parseDouble(el.getAttributeValue(LOW));
		//if (result[0] < 1.0 || result[0] > 15.0 )
		//	result[0] = 101.0;
		//System.out.println(a);

		double b = Double.parseDouble(el.getAttributeValue(HIGH));
		//if (result[0] < 1.0 || result[0] > 15.0 )
		//	result[0] = 99.0;
		//System.out.println(b);

		return new double[] {a,b};
/*
		TOTAL_PRESSURE; // low="99.9" high="102.7" />
		PARTIAL_PRESSURE_OF_O2 ; //low="19.5" high="23.1" />
		PARTIAL_PRESSURE_OF_N2 ;// low="79" high="79"/>
		PARTIAL_PRESSURE_OF_CO2 ; //low=".4" high=".4" />
		TEMPERATURE ;// low="18.3" high="23.9"/>
		RELATIVE_HUMIDITY ; //low="30" high="70"/>
		VENTILATION ;//
*/
    }

	/**
	 * Load the settlement templates from the XML document.
	 * @param settlementDoc DOM document with settlement configuration.
	 * @param partPackageConfig the part package configuration.
	 * @throws Exception if error reading XML document.
	 */
    @SuppressWarnings("unchecked")
	private void loadSettlementTemplates(Document settlementDoc,
	        PartPackageConfig partPackageConfig) {

		Element root = settlementDoc.getRootElement();
		Element templateList = root.getChild(SETTLEMENT_TEMPLATE_LIST);
		// 2014-10-29 Added settlement id to Settlement.xml and loaded settlement id here
		//Set<Integer> existingSIDs = new HashSet<Integer>();
		List<Element> templateNodes = templateList.getChildren(TEMPLATE);
		for (Element templateElement : templateNodes) {
		    int scenarioID = Integer.parseInt(templateElement.getAttributeValue(ID));
			String settlementTemplateName = templateElement.getAttributeValue(NAME);

		    if (scenarioMap.containsKey(scenarioID) ) {
		        throw new IllegalStateException("Error in SettlementConfig.xml: scenarioID in settlement template " + settlementTemplateName + " is not unique.");
		    }
		    else
		    	scenarioMap.put(scenarioID, settlementTemplateName);

		    int defaultPopulation = Integer.parseInt(templateElement.getAttributeValue(DEFAULT_POPULATION));
		    int defaultNumOfRobots = Integer.parseInt(templateElement.getAttributeValue(DEFAULT_NUM_ROBOTS));

		    // 2014-10-29 Added scenarioID
			SettlementTemplate template = new SettlementTemplate(settlementTemplateName, scenarioID, defaultPopulation, defaultNumOfRobots);
			settlementTemplates.add(template);

			Set<Integer> existingIDs = new HashSet<Integer>();
			// 2015-12-13 Added buildingTypeIDMap
			Map<String, Integer> buildingTypeIDMap = new HashMap<>();

			List<Element> buildingNodes = templateElement.getChildren(BUILDING);
			for (Element buildingElement : buildingNodes) {

				double width = -1D;
				if (buildingElement.getAttribute(WIDTH) != null) {
				    width = Double.parseDouble(buildingElement.getAttributeValue(WIDTH));
				}

				// Determine optional length attribute value.  "-1" if it doesn't exist.
				double length = -1D;
				if (buildingElement.getAttribute(LENGTH) != null) {
				    length = Double.parseDouble(buildingElement.getAttributeValue(LENGTH));
				}

				double xLoc = Double.parseDouble(buildingElement.getAttributeValue(X_LOCATION));
				double yLoc = Double.parseDouble(buildingElement.getAttributeValue(Y_LOCATION));
				double facing = Double.parseDouble(buildingElement.getAttributeValue(FACING));

			    // 2014-10-28  Changed id to bid
			    int bid = Integer.parseInt(buildingElement.getAttributeValue(ID));
			    if (existingIDs.contains(bid)) {
			        throw new IllegalStateException("Error in SettlementConfig.xml : building ID in settlement template " + settlementTemplateName + " is not unique.");
			    }
			    else
			    	existingIDs.add(bid);

				String buildingType = buildingElement.getAttributeValue(TYPE);

				if (buildingTypeIDMap.containsKey(buildingType)) {
					int last = buildingTypeIDMap.get(buildingType);
					buildingTypeIDMap.put(buildingType, last + 1);
				}
				else
					buildingTypeIDMap.put(buildingType, 1);

				// 2014-10-28  Created a building nickname for every building
				// by appending the settlement id and building id to that building's type.
				String scenario = getCharForNumber(scenarioID + 1);
				// NOTE: i = sid + 1 since i must be > 1, if i = 0, s = null
				//String buildingID = bid + "";
	            // 2015-12-13 Added buildingTypeID
				int buildingTypeID = buildingTypeIDMap.get(buildingType);
				//String buildingNickName = buildingType + " " + scenario + buildingID;
				String buildingNickName = buildingType + " " + buildingTypeID;

				 // 2014-10-28  Added buildingNickName, Changed id to bid
				BuildingTemplate buildingTemplate = new BuildingTemplate(settlementTemplateName, bid, scenario, buildingType, buildingNickName, width, length,
				        xLoc, yLoc, facing);

				template.addBuildingTemplate(buildingTemplate);

				// Create building connection templates.
				Element connectionListElement = buildingElement.getChild(CONNECTION_LIST);
				if (connectionListElement != null) {
				    List<Element> connectionNodes = connectionListElement.getChildren(CONNECTION);
				    for (Element connectionElement : connectionNodes) {
				        int connectionID = Integer.parseInt(connectionElement.getAttributeValue(ID));

				        // Check that connection ID is not the same as the building ID.
				        if (connectionID == bid) {
				            throw new IllegalStateException("Connection ID cannot be the same as building ID for building: " +
				                    buildingType + " in settlement template: " + settlementTemplateName);
				        }

				        double connectionXLoc = Double.parseDouble(connectionElement.getAttributeValue(X_LOCATION));
				        double connectionYLoc = Double.parseDouble(connectionElement.getAttributeValue(Y_LOCATION));

				        buildingTemplate.addBuildingConnection(connectionID, connectionXLoc, connectionYLoc);
				    }
				}
			}

			// Check that building connections point to valid building ID's.
			List<BuildingTemplate> buildingTemplates = template.getBuildingTemplates();
			for (BuildingTemplate buildingTemplate : buildingTemplates) {
			    List<BuildingConnectionTemplate> connectionTemplates = buildingTemplate.getBuildingConnectionTemplates();
			    for (BuildingConnectionTemplate connectionTemplate : connectionTemplates) {
			        if (!existingIDs.contains(connectionTemplate.getID())) {
			        	//2014-10-28  Modified getName() to getNickName()
			            throw new IllegalStateException("Connection ID: " + connectionTemplate.getID() +
			                    " invalid for building: " + buildingTemplate.getNickName() + " in settlement template: " + settlementTemplateName);
			        }
			    }
			}

			// Load vehicles
			List<Element> vehicleNodes = templateElement.getChildren(VEHICLE);
			for (Element vehicleElement : vehicleNodes) {
				String vehicleType = vehicleElement.getAttributeValue(TYPE);
				int vehicleNumber = Integer.parseInt(vehicleElement.getAttributeValue(NUMBER));
				template.addVehicles(vehicleType, vehicleNumber);
			}

			// Load equipment
			List<Element> equipmentNodes = templateElement.getChildren(EQUIPMENT);
			for (Element equipmentElement : equipmentNodes) {
				String equipmentType = equipmentElement.getAttributeValue(TYPE);
				int equipmentNumber = Integer.parseInt(equipmentElement.getAttributeValue(NUMBER));
				template.addEquipment(equipmentType, equipmentNumber);
			}

			// Load resources
			List<Element> resourceNodes = templateElement.getChildren(RESOURCE);
			for (Element resourceElement : resourceNodes) {
				String resourceType = resourceElement.getAttributeValue(TYPE);
				AmountResource resource = AmountResource.findAmountResource(resourceType);
				if (resource == null)
					logger.severe(resourceType + " shows up in settlements.xml but doesn't exist in resources.xml.");
				else {			
					double resourceAmount = Double.parseDouble(resourceElement.getAttributeValue(AMOUNT));
					template.addAmountResource(resource, resourceAmount);
				}

			}

			// Load parts
			List<Element> partNodes = templateElement.getChildren(PART);
			for (Element partElement : partNodes) {
				String partType = partElement.getAttributeValue(TYPE);
				Part part = (Part) Part.findItemResource(partType);
				if (part == null)
					logger.severe(partType + " shows up in settlements.xml but doesn't exist in parts.xml.");
				else {         
					int partNumber = Integer.parseInt(partElement.getAttributeValue(NUMBER));
					template.addPart(part, partNumber);	
				}	
			}
			

			// Load part packages
			List<Element> partPackageNodes = templateElement.getChildren(PART_PACKAGE);
			for (Element partPackageElement : partPackageNodes) {
				String packageName = partPackageElement.getAttributeValue(NAME);
				int packageNumber = Integer.parseInt(partPackageElement.getAttributeValue(NUMBER));
				if (packageNumber > 0) {
					for (int z = 0; z < packageNumber; z++) {
						Map<Part, Integer> partPackage = partPackageConfig.getPartsInPackage(packageName);
						Iterator<Part> i = partPackage.keySet().iterator();
						while (i.hasNext()) {
							Part part = i.next();
							int partNumber = partPackage.get(part);
							template.addPart(part, partNumber);
						}
					}
				}
			}

			//System.out.println("SettlementConfig's partPackageNodes : " + partPackageNodes.size());

			// Load resupplies
			Element resupplyList = templateElement.getChild(RESUPPLY);
			if (resupplyList != null) {
				List<Element> resupplyNodes = resupplyList.getChildren(RESUPPLY_MISSION);
				for (Element resupplyMissionElement: resupplyNodes) {
				    String resupplyName = resupplyMissionElement.getAttributeValue(NAME);
                    double arrivalTime =
                        Double.parseDouble(resupplyMissionElement.getAttributeValue(ARRIVAL_TIME));
				    ResupplyMissionTemplate resupplyMissionTemplate =
				        new ResupplyMissionTemplate(resupplyName, arrivalTime);
				    template.addResupplyMissionTemplate(resupplyMissionTemplate);
				}
			}
		}
	}

	/**
	 * Load initial settlements.
	 * @param settlementDoc DOM document with settlement configuration.
	 * @throws Exception if XML error.
	 */
    @SuppressWarnings("unchecked")
	private void loadInitialSettlements(Document settlementDoc) {
		Element root = settlementDoc.getRootElement();
		Element initialSettlementList = root.getChild(INITIAL_SETTLEMENT_LIST);
		List<Element> settlementNodes = initialSettlementList.getChildren(SETTLEMENT);
		for (Element settlementElement : settlementNodes) {
			InitialSettlement initialSettlement = new InitialSettlement();

			String settlementName = settlementElement.getAttributeValue(NAME);
			if (settlementName.equals(RANDOM)) initialSettlement.randomName = true;
			else initialSettlement.name = settlementName;

			initialSettlement.template = settlementElement.getAttributeValue(TEMPLATE);

			List<Element> locationNodes = settlementElement.getChildren(LOCATION);
			if (locationNodes.size() > 0) {
				Element locationElement = locationNodes.get(0);

				String longitudeString = locationElement.getAttributeValue(LONGITUDE);
				if (longitudeString.equals(RANDOM)) initialSettlement.randomLongitude = true;
				else {
					// take care to internationalize the coordinates
					longitudeString = longitudeString.replace("E",Msg.getString("direction.eastShort")); //$NON-NLS-1$ //$NON-NLS-2$
					longitudeString = longitudeString.replace("W",Msg.getString("direction.westShort")); //$NON-NLS-1$ //$NON-NLS-2$
					initialSettlement.longitude = longitudeString;
				}

				String latitudeString = locationElement.getAttributeValue(LATITUDE);
				if (latitudeString.equals(RANDOM)) initialSettlement.randomLatitude = true;
				else {
					// take care to internationalize the coordinates
					latitudeString = latitudeString.replace("N",Msg.getString("direction.northShort")); //$NON-NLS-1$ //$NON-NLS-2$
					latitudeString = latitudeString.replace("S",Msg.getString("direction.southShort")); //$NON-NLS-1$ //$NON-NLS-2$
					initialSettlement.latitude = latitudeString;
				}
			}
			else {
				initialSettlement.randomLongitude = true;
				initialSettlement.randomLatitude = true;
			}

			Element populationElement = settlementElement.getChild(POPULATION);
			String numberStr = populationElement.getAttributeValue(NUMBER);
			int number = Integer.parseInt(numberStr);
			if (number < 0) {
				throw new IllegalStateException("populationNumber cannot be less than zero: " + number);
			}
			initialSettlement.populationNumber = number;

			Element numOfRobotsElement = settlementElement.getChild(NUM_OF_ROBOTS);
			String numOfRobotsStr = numOfRobotsElement.getAttributeValue(ROBOTS_NUMBER);
			int numOfRobots = Integer.parseInt(numOfRobotsStr);
			//System.out.println("loadInitialSettlements() : numOfRobots is "+numOfRobots);
			if (number < 0) {
				throw new IllegalStateException("The number of robots cannot be less than zero: " + number);
			}
			initialSettlement.numOfRobots = numOfRobots;

/*
			// 2015-10-04 Added MSD element
			Element maxMSDElement = settlementElement.getChild(SPONSOR);
			String maxMSDStr = maxMSDElement.getAttributeValue(NAME);
			int maxMSD = Integer.parseInt(maxMSDStr);
			//System.out.println("loadInitialSettlements() : maxMSD is "+maxMSD);
			if (number < 0) {
				throw new IllegalStateException("The maximum number of Mars Society delegate cannot be less than zero: " + number);
			}
			initialSettlement.maxMSD = maxMSD;
*/

			Element sponsorElement = settlementElement.getChild(SPONSOR);
			String sponsor = sponsorElement.getAttributeValue(NAME);
			initialSettlement.sponsor = sponsor;



			initialSettlements.add(initialSettlement);
		}
	}

	/**
	 * Load new arriving settlements.
	 * @param settlementDoc DOM document with settlement configuration.
	 */
    @SuppressWarnings("unchecked")
	private void loadNewArrivingSettlements(Document settlementDoc) {
		Element root = settlementDoc.getRootElement();
		Element arrivingSettlementList = root.getChild(NEW_ARRIVING_SETTLEMENT_LIST);
		List<Element> settlementNodes = arrivingSettlementList.getChildren(ARRIVING_SETTLEMENT);
		for (Element settlementElement : settlementNodes) {
			NewArrivingSettlement arrivingSettlement = new NewArrivingSettlement();

			String settlementName = settlementElement.getAttributeValue(NAME);
			if (settlementName.equals(RANDOM)) arrivingSettlement.randomName = true;
			else arrivingSettlement.name = settlementName;

			arrivingSettlement.template = settlementElement.getAttributeValue(TEMPLATE);

			arrivingSettlement.arrivalTime = Double.parseDouble(settlementElement.getAttributeValue(ARRIVAL_TIME));

			List<Element> locationNodes = settlementElement.getChildren(LOCATION);
			if (locationNodes.size() > 0) {
				Element locationElement = locationNodes.get(0);

				String longitudeString = locationElement.getAttributeValue(LONGITUDE);
				if (longitudeString.equals(RANDOM)) arrivingSettlement.randomLongitude = true;
				else arrivingSettlement.longitude = longitudeString;

				String latitudeString = locationElement.getAttributeValue(LATITUDE);
				if (latitudeString.equals(RANDOM)) arrivingSettlement.randomLatitude = true;
				else arrivingSettlement.latitude = latitudeString;
			}
			else {
				arrivingSettlement.randomLongitude = true;
				arrivingSettlement.randomLatitude = true;
			}

			Element populationElement = settlementElement.getChild(POPULATION);
			String numberStr = populationElement.getAttributeValue(NUMBER);
			int number = Integer.parseInt(numberStr);
			if (number < 0) {
				throw new IllegalStateException("populationNumber cannot be less than zero: " + number);
			}
			arrivingSettlement.populationNumber = number;

			Element numOfRobotsElement = settlementElement.getChild(NUM_OF_ROBOTS);
			String numOfRobotsStr = numOfRobotsElement.getAttributeValue(ROBOTS_NUMBER);
			int numOfRobots = Integer.parseInt(numOfRobotsStr);
			if (numOfRobots < 0) {
				throw new IllegalStateException("numOfRobots cannot be less than zero: " + number);
			}
			arrivingSettlement.numOfRobots = number;
/*
			Element maxMSDElement = settlementElement.getChild(MAX_MSD);
			String maxMSDStr = populationElement.getAttributeValue(MSD_NUMBER);
			int maxMSD = Integer.parseInt(numOfRobotsStr);
			if (maxMSD < 0) {
				throw new IllegalStateException("maxMSD cannot be less than zero: " + number);
			}
			arrivingSettlement.maxMSD = number;
*/

			Element sponsorElement = settlementElement.getChild(SPONSOR);
			String sponsor = sponsorElement.getAttributeValue(NAME);
			arrivingSettlement.sponsor = sponsor;


			newArrivingSettlements.add(arrivingSettlement);
		}
	}

	/**
	 * Load settlement names.
	 * @param settlementDoc DOM document with settlement configuration.
	 * @throws Exception if XML error.
	 */
    @SuppressWarnings("unchecked")
	private void loadSettlementNames(Document settlementDoc) {
		Element root = settlementDoc.getRootElement();
		Element settlementNameList = root.getChild(SETTLEMENT_NAME_LIST);
		List<Element> settlementNameNodes = settlementNameList.getChildren(SETTLEMENT_NAME);
		for (Element settlementNameElement : settlementNameNodes) {
			String name = settlementNameElement.getAttributeValue(VALUE);
			String sponsor = settlementNameElement.getAttributeValue(SPONSOR);

			// (Skipped) match sponsor to the corresponding element in sponsor list
			// load names list
			List<String> oldlist = settlementNamesMap.get(sponsor);
			// add the settlement name
			if (oldlist == null) { //oldlist.isEmpty() ||
				List<String> newlist = new ArrayList<>();
				newlist.add(name);
				settlementNamesMap.put(sponsor, newlist);
			}
			else {
				oldlist.add(name);
				settlementNamesMap.put(sponsor, oldlist);
			}

			int newID = settlementMap.size() + 1;
			settlementMap.put(newID, name);
		}

	}

	/**
	 * Obtains the key of a value in a particular map
	 * @param map
	 * @param name
	 * @return
	 */
    // 2015-12-13 Added getMapKey()
    public int getMapKey(Map<Integer, String> map,  String name) {
    	int result = -1;
    	if (map.containsValue(name)) {
            for (Map.Entry<Integer, String> e : map.entrySet()) {
                Integer key = e.getKey();
                Object value2 = e.getValue();
                if ((value2.toString()).equalsIgnoreCase(name)) {
                	result = key;
                }
            }
        }

    	return result;
    }

    /**
     * Changes a settlement's name in settlementMap
     * @param oldName
     * @param newName
     */
    // 2015-12-13 Added changeSettlementName()
    public void changeSettlementName(String oldName, String newName) {
    	if (settlementMap.containsValue(oldName)) {
            for (Map.Entry<Integer,String> e : settlementMap.entrySet()) {
                Integer key = e.getKey();
                Object value = e.getValue();
                if ((value.toString()).equalsIgnoreCase(oldName)) {
                	settlementMap.remove(key, oldName);
            		settlementMap.put(key, newName);
                }
            }
        }
    }

	/**
	 * Gets the settlement template that matches a template name.
	 * @param templateName the template name.
	 * @return settlement template
	 */
	public SettlementTemplate getSettlementTemplate(String templateName) {
		SettlementTemplate result = null;

		Iterator<SettlementTemplate> i = settlementTemplates.iterator();
		while (i.hasNext()) {
			SettlementTemplate template = i.next();
			if (template.getTemplateName().equals(templateName)) result = template;
		}

		if (result == null) {
		    throw new IllegalArgumentException("templateName: "
				+ templateName + " not found.");
		}

		return result;
	}

	/**
	 * Gets a list of settlement templates.
	 * @return list of settlement templates.
	 */
	public List<SettlementTemplate> getSettlementTemplates() {
	    return new ArrayList<SettlementTemplate>(settlementTemplates);
	}

	/**
	 * Gets the number of new arriving settlements.
	 * @return number of settlements.
	 */
	public int getNumberOfNewArrivingSettlements() {
		return newArrivingSettlements.size();
	}

	/**
	 * Gets the name of a new arriving settlement
	 * or 'random' if the name is to chosen randomly from the settlement name list.
	 * @param index the index of the new arriving settlement.
	 * @return settlement name
	 */
	public String getNewArrivingSettlementName(int index) {
		if ((index >= 0) && (index < newArrivingSettlements.size())) {
			NewArrivingSettlement settlement = newArrivingSettlements.get(index);
			if (settlement.randomName) return RANDOM;
			else return settlement.name;
		}
		else throw new IllegalArgumentException("index: " + index + "is out of bounds");
	}

	/**
	 * Gets the template used by a new arriving settlement.
	 * @param index the index of the new arriving settlement.
	 * @return settlement template name.
	 */
	public String getNewArrivingSettlementTemplate(int index) {
		if ((index >= 0) && (index < newArrivingSettlements.size()))
			return newArrivingSettlements.get(index).template;
		else throw new IllegalArgumentException("index: " + index + "is out of bounds");
	}

	/**
	 * Gets the scenarioID used by an New Arriving settlement.
	 * @param index the index of the New Arriving settlement.
	 * @return settlement scenarioID.
	 */
	// 2015-01-17 Added getNewArrivingSettlementScenarioID()
	public int getNewArrivingSettlementScenarioID(int index) {
		if ((index >= 0) && (index < newArrivingSettlements.size()))
			//return scenarioMap.get(newArrivingSettlements.get(index).template);
			return getMapKey(scenarioMap, newArrivingSettlements.get(index).template);
		else throw new IllegalArgumentException("index: " + index + "is out of bounds");
	}

    /**
     * Gets the arrival time for a new arriving settlement from
     * the start of the simulation.
     * @param templateName the template name.
     * @return arrival time (Sols).
     */
	public double getNewArrivingSettlementArrivalTime(int index) {
		if ((index >= 0) && (index < newArrivingSettlements.size()))
			return newArrivingSettlements.get(index).arrivalTime;
		else throw new IllegalArgumentException("index: " + index + "is out of bounds");
	}

	/**
	 * Gets the longitude of a new arriving settlement,
	 * or 'random' if the longitude is to be randomly determined.
	 * @param index the index of the new arriving settlement.
	 * @return longitude of the settlement as a string. Example: '0.0 W'
	 */
	public String getNewArrivingSettlementLongitude(int index) {
		if ((index >= 0) && (index < newArrivingSettlements.size())) {
			NewArrivingSettlement settlement = newArrivingSettlements.get(index);
			if (settlement.randomLongitude) return RANDOM;
			else return settlement.longitude;
		}
		else throw new IllegalArgumentException("index: " + index + "is out of bounds");
	}

	/**
	 * Gets the latitude of a new arriving settlement,
	 * or 'random' if the longitude is to be randomly determined.
	 * @param index the index of the new arriving settlement.
	 * @return latitude of the settlement as a string. Example: '0.0 N'
	 */
	public String getNewArrivingSettlementLatitude(int index) {
		if ((index >= 0) && (index < newArrivingSettlements.size())) {
			NewArrivingSettlement settlement = newArrivingSettlements.get(index);
			if (settlement.randomLatitude) return RANDOM;
			else return settlement.latitude;
		}
		else throw new IllegalArgumentException("index: " + index + "is out of bounds");
	}

	/**
	 * Gets the population number for a new arriving settlement.
	 * @param index the index of the new arriving settlement.
	 * @return population number of the settlement.
	 */
	public int getNewArrivingSettlementPopulationNumber(int index) {
		if ((index >= 0) && (index < newArrivingSettlements.size())) {
			NewArrivingSettlement settlement = newArrivingSettlements.get(index);
			return settlement.populationNumber;
		}
		else throw new IllegalArgumentException("index: " + index + "is out of bounds");
	}

	/**
	 * Gets the number of robots for a new arriving settlement.
	 * @param index the index of the new arriving settlement.
	 * @return number of robots of the settlement.
	 */
	public int getNewArrivingSettlementNumOfRobots(int index) {
		if ((index >= 0) && (index < newArrivingSettlements.size())) {
			NewArrivingSettlement settlement = newArrivingSettlements.get(index);
			return settlement.numOfRobots;
		}
		else throw new IllegalArgumentException("index: " + index + "is out of bounds");
	}

	/**
	 * Gets the number of initial settlements.
	 * @return number of settlements.
	 */
	public int getNumberOfInitialSettlements() {
		return initialSettlements.size();
	}

	/**
	 * Gets the name of an initial settlement
	 * or 'random' if the name is to chosen randomly from the settlement name list.
	 * @param index the index of the initial settlement.
	 * @return settlement name
	 */
	public String getInitialSettlementName(int index) {
		if ((index >= 0) && (index < initialSettlements.size())) {
			InitialSettlement settlement = initialSettlements.get(index);
			if (settlement.randomName) return RANDOM;
			else return settlement.name;
		}
		else throw new IllegalArgumentException("index: " + index + "is out of bounds");
	}

	/**
	* Gets the scenarioID used by an initial settlement.
	* @param index the index of the initial settlement.
	* @return settlement scenarioID.
	*/
	// 2015-01-17 Added getInitialSettlementScenarioID()
	public int getInitialSettlementScenarioID(int index) {
		if ((index >= 0) && (index < initialSettlements.size()))
			//return scenarioMap.get((initialSettlements.get(index).template));
			return getMapKey(scenarioMap, initialSettlements.get(index).template);
		else throw new IllegalArgumentException("index: " + index + "is out of bounds");
	}

	/**
	 * Gets the template used by an initial settlement.
	 * @param index the index of the initial settlement.
	 * @return settlement template name.
	 */
	public String getInitialSettlementTemplate(int index) {
		if ((index >= 0) && (index < initialSettlements.size()))
			return initialSettlements.get(index).template;
		else throw new IllegalArgumentException("index: " + index + "is out of bounds");
	}

	/**
	 * Gets the internationalized longitude of an initial settlement,
	 * or 'random' if the longitude is to be randomly determined.
	 * @param index the index of the initial settlement.
	 * @return longitude of the settlement as a string. Example: '0.0 W'
	 */
	public String getInitialSettlementLongitude(int index) {
		if ((index >= 0) && (index < initialSettlements.size())) {
			InitialSettlement settlement = initialSettlements.get(index);
			if (settlement.randomLongitude) return RANDOM;
			else return settlement.longitude;
		}
		else throw new IllegalArgumentException("index: " + index + "is out of bounds");
	}

	/**
	 * Gets the internationalized latitude of an initial settlement,
	 * or 'random' if the longitude is to be randomly determined.
	 * @param index the index of the initial settlement.
	 * @return latitude of the settlement as a string. Example: '0.0 N'
	 */
	public String getInitialSettlementLatitude(int index) {
		if ((index >= 0) && (index < initialSettlements.size())) {
			InitialSettlement settlement = initialSettlements.get(index);
			if (settlement.randomLatitude) return RANDOM;
			else return settlement.latitude;
		}
		else throw new IllegalArgumentException("index: " + index + "is out of bounds");
	}

	/**
	 * Gets the population number for an initial settlement.
	 * @param index the index of the initial settlement.
	 * @return population number of the settlement.
	 */
	public int getInitialSettlementPopulationNumber(int index) {
		if ((index >= 0) && (index < initialSettlements.size())) {
			InitialSettlement settlement = initialSettlements.get(index);
			return settlement.populationNumber;
		}
		else throw new IllegalArgumentException("index: " + index + "is out of bounds");
	}

	/**
	 * Gets the robot population number for an initial settlement.
	 * @param index the index of the initial settlement.
	 * @return robot population number of the settlement.
	 */
	public int getInitialSettlementNumOfRobots(int index) {
		if ((index >= 0) && (index < initialSettlements.size())) {
			InitialSettlement settlement = initialSettlements.get(index);
			return settlement.numOfRobots;
		}
		else throw new IllegalArgumentException("index: " + index + "is out of bounds");
	}

	/**
	 * Gets the sponsoring agency for the initial settlement.
	 * @param index the index of the initial settlement.
	 * @return the name of the sponsoring agency
	 */
	//2016-07-18 Added getInitialSettlementSponsor()
	public String getInitialSettlementSponsor(int index) {
	if ((index >= 0) && (index < initialSettlements.size())) {
		InitialSettlement settlement = initialSettlements.get(index);
		//if (settlement.randomName) return RANDOM;
		//else
		//System.out.println("settlement : sponsor is " + settlement.sponsor);
		return settlement.sponsor;
	}
	else throw new IllegalArgumentException("index: " + index + "is out of bounds");
}


	/**
	 * Gets the maximum number of Mars Society delegates for an initial settlement.
	 * @param index the index of the initial settlement.
	 * @return number of delegates.

	// 2015-10-03 Added loading getInitialSettlementMaxMSD()
	public int getInitialSettlementMaxMSD(int index) {
		if ((index >= 0) && (index < initialSettlements.size())) {
			InitialSettlement settlement = initialSettlements.get(index);
			//System.out.println("settlement.maxMSD is "+ settlement.maxMSD + "   index is " + index + "  initialSettlements.size() is " + initialSettlements.size());
			return settlement.maxMSD;
		}
		else throw new IllegalArgumentException("index: " + index + "is out of bounds");
	}
	 */

	/**
	 * Gets a list of possible settlement names.
	 * @return list of settlement names as strings
	 */
	public List<String> getSettlementNameList() {
		//return new ArrayList<String>(settlementNames);
		return new ArrayList<String>(settlementNamesMap.get("Mars Society (MS)"));
	}

	
	/**
	 * Gets a list of possible settlement names.
	 * @return list of settlement names as strings
	 */
	public List<String> getSettlementNameList(String sponsor) {
		return new ArrayList<String>(settlementNamesMap.get(sponsor));
	}

	/**
	 * Clears the list of initial settlements.
	 */
	public void clearInitialSettlements() {
	    initialSettlements.clear();
	}

	/**
	 * Adds an initial settlement to the configuration.
	 * @param name the settlement name.
	 * @param template the settlement template.
	 * @param latitude the settlement latitude (ex. "10.3 S").
	 * @param longitude the settlement longitude (ex. "47.0 W").
	 */
	public void addInitialSettlement(String name, String template, int populationNum, int numOfRobots,
			String sponsor, String latitude, String longitude) {
	    InitialSettlement settlement = new InitialSettlement();
	    settlement.name = name;
	    settlement.template = template;
	    settlement.populationNumber = populationNum;
	    settlement.numOfRobots = numOfRobots;
	    settlement.sponsor = sponsor;
	    //System.out.println("SettmaxMSDg : numOfRobots is " + numOfRobots);
	    //settlement.scenarioID = scenarioMap.get(template);
	    settlement.scenarioID = getMapKey(scenarioMap, template);

		// take care to internationalize the coordinates
		latitude = latitude.replace("N",Msg.getString("direction.northShort")); //$NON-NLS-1$ //$NON-NLS-2$
		latitude = latitude.replace("S",Msg.getString("direction.southShort")); //$NON-NLS-1$ //$NON-NLS-2$
		longitude = longitude.replace("E",Msg.getString("direction.eastShort")); //$NON-NLS-1$ //$NON-NLS-2$
		longitude = longitude.replace("W",Msg.getString("direction.westShort")); //$NON-NLS-1$ //$NON-NLS-2$

	    settlement.latitude = latitude;
	    settlement.longitude = longitude;
	    //settlement.maxMSD = maxMSD;
	    initialSettlements.add(settlement);

	}

	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
	    Iterator<SettlementTemplate> i = settlementTemplates.iterator();
	    while (i.hasNext()) {
	        i.next().destroy();
	    }
	    settlementTemplates.clear();
	    settlementTemplates = null;
	    initialSettlements.clear();
	    initialSettlements = null;
	    settlementNamesMap.clear();
	    settlementNamesMap = null;
	    //settlementNames.clear();
	    //settlementNames = null;
	}

	/**
	 * Private inner class for holding a initial settlement info.
	 */
	private static class InitialSettlement
	implements Serializable {
		/** default serial id. */
		private static final long serialVersionUID = 1L;
		private boolean randomName = false;
		private boolean randomLongitude = false;
		private boolean randomLatitude = false;

		private String name;
		private String template;
		private String longitude;
		private String latitude;
		private int populationNumber;
		private int numOfRobots;
		private String sponsor = Msg.getString("ReportingAuthorityType.MarsSociety"); //$NON-NLS-1$ //"Mars Society (MS)";

		//private int maxMSD;
		private int scenarioID;
	}

	/**
	 * Private inner class for holding a new arriving settlement info.
	 */
	private static class NewArrivingSettlement
	implements Serializable {
		/** default serial id. */
		private static final long serialVersionUID = 1L;
		private boolean randomName = false;
		private boolean randomLongitude = false;
		private boolean randomLatitude = false;

		private String name;
		private String template;
		private double arrivalTime;
		private String longitude;
		private String latitude;
		private int populationNumber;
		private int numOfRobots;
		private String sponsor = Msg.getString("ReportingAuthorityType.MarsSociety"); //$NON-NLS-1$ //"Mars Society (MS)";

		//private int maxMSD;
		private int scenarioID;
	}
}
/**
 * Mars Simulation Project
 * BuildingConnectorManager.java
 * @version 3.1.0 2017-04-15
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.building.connection;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.LogConsolidated;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.structure.BuildingTemplate;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.SettlementConfig;
import org.mars_sim.msp.core.structure.BuildingTemplate.BuildingConnectionTemplate;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.FunctionType;

/**
 * Manages all the building connectors at a settlement.
 */
public class BuildingConnectorManager implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(BuildingConnectorManager.class.getName());

	private static String sourceName = logger.getName().substring(logger.getName().lastIndexOf(".") + 1,
			logger.getName().length());

	/** Comparison to indicate a small but non-zero amount. */
	private static final double SMALL_AMOUNT_COMPARISON = .0000001D;

	/** Enum representing the four different sides of a building. */
	private enum BuildingSide {

		FRONT("front"), BACK("back"), LEFT("left"), RIGHT("right");

		private String name;

		/** hidden constructor. */
		private BuildingSide(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return this.name;
		}
	}

	// Data members.
	private Settlement settlement;
	
	private Set<BuildingConnector> buildingConnections;
	
	private static SettlementConfig settlementConfig = SimulationConfig.instance().getSettlementConfiguration();
	
	/**
	 * Constructor
	 * 
	 * @param settlement the settlement.
	 */
	public BuildingConnectorManager(Settlement settlement) {
		this(settlement, settlementConfig
				.getSettlementTemplate(settlement.getTemplate()).getBuildingTemplates());
	}

	/**
	 * Constructor
	 * 
	 * @param settlement        the settlement.
	 * @param buildingTemplates a list of building templates.
	 */
	public BuildingConnectorManager(Settlement settlement, List<BuildingTemplate> buildingTemplates) {

		if (settlement == null) {
			throw new IllegalArgumentException("Settlement cannot be null");
		}

		this.settlement = settlement;
		buildingConnections = Collections.synchronizedSet(new HashSet<BuildingConnector>());

		BuildingManager buildingManager = settlement.getBuildingManager();

		// Create partial building connector list from building connection templates.
		List<PartialBuildingConnector> partialBuildingConnectorList = new ArrayList<PartialBuildingConnector>();
		Iterator<BuildingTemplate> i = buildingTemplates.iterator();
		while (i.hasNext()) {
			BuildingTemplate buildingTemplate = i.next();
			int buildingID = buildingTemplate.getID();
			Building building = buildingManager.getBuildingByTemplateID(buildingID);
			// Building building = buildingManager.getBuilding(0);
			if (building == null) {
				throw new IllegalStateException(
						"On buildingTemplate " + buildingTemplate
						+ "    buildingID " + buildingID 
						+ " does not exist for settlement " + settlement.getName());
			}

			Iterator<BuildingConnectionTemplate> j = buildingTemplate.getBuildingConnectionTemplates().iterator();
			while (j.hasNext()) {
				BuildingConnectionTemplate connectionTemplate = j.next();
				int connectionID = connectionTemplate.getID();
				Building connectionBuilding = buildingManager.getBuildingByTemplateID(connectionID);
				if (connectionBuilding == null) {
					throw new IllegalStateException(
							"On buildingTemplate " + buildingTemplate
							+ "    buildingID " + buildingID 
							+ "    connectionID " + connectionID 
							+ " does not exist for settlement " + settlement.getName());
				}

				double connectionXLoc = connectionTemplate.getXLocation();
				double connectionYLoc = connectionTemplate.getYLocation();
				Point2D.Double connectionSettlementLoc = LocalAreaUtil.getLocalRelativeLocation(connectionXLoc,
						connectionYLoc, building);

				double connectionFacing = 0D;
				if (connectionXLoc == (building.getWidth() / 2D)) {
					connectionFacing = building.getFacing() - 90D;
				} else if (connectionXLoc == (building.getWidth() / -2D)) {
					connectionFacing = building.getFacing() + 90D;
				} else if (connectionYLoc == (building.getLength() / 2D)) {
					connectionFacing = building.getFacing();
				} else if (connectionYLoc == (building.getLength() / -2D)) {
					connectionFacing = building.getFacing() + 180D;
				}

				if (connectionFacing < 0D) {
					connectionFacing += 360D;
				}

				if (connectionFacing > 360D) {
					connectionFacing -= 360D;
				}

				PartialBuildingConnector partialConnector = new PartialBuildingConnector(building,
						connectionSettlementLoc.getX(), connectionSettlementLoc.getY(), connectionFacing,
						connectionBuilding);
				partialBuildingConnectorList.add(partialConnector);
			}
		}

		// Match up partial connectors to create building connectors.
		while (partialBuildingConnectorList.size() > 0) {
			PartialBuildingConnector partialConnector = partialBuildingConnectorList.get(0);
			Point2D.Double partialConnectorLoc = new Point2D.Double(partialConnector.xLocation,
					partialConnector.yLocation);
			List<PartialBuildingConnector> validPartialConnectors = new ArrayList<PartialBuildingConnector>();
			for (int x = 1; x < partialBuildingConnectorList.size(); x++) {
				PartialBuildingConnector potentialConnector = partialBuildingConnectorList.get(x);
				if (potentialConnector.building.equals(partialConnector.connectToBuilding)
						&& (potentialConnector.connectToBuilding.equals(partialConnector.building))) {
					validPartialConnectors.add(potentialConnector);
				}
			}

			if (validPartialConnectors.size() > 0) {
				PartialBuildingConnector bestFitConnector = null;
				double closestDistance = Double.MAX_VALUE;
				Iterator<PartialBuildingConnector> j = validPartialConnectors.iterator();
				while (j.hasNext()) {
					PartialBuildingConnector validConnector = j.next();
					Point2D.Double validConnectorLoc = new Point2D.Double(validConnector.xLocation,
							validConnector.yLocation);
					double distance = LocalAreaUtil.getDistance(partialConnectorLoc, validConnectorLoc);
					if (distance < closestDistance) {
						bestFitConnector = validConnector;
						closestDistance = distance;
					}
				}

				if (bestFitConnector != null) {

					BuildingConnector buildingConnector = new BuildingConnector(partialConnector.building,
							partialConnector.xLocation, partialConnector.yLocation, partialConnector.facing,
							bestFitConnector.building, bestFitConnector.xLocation, bestFitConnector.yLocation,
							bestFitConnector.facing);
					addBuildingConnection(buildingConnector);
					partialBuildingConnectorList.remove(partialConnector);
					partialBuildingConnectorList.remove(bestFitConnector);
				} else {
					throw new IllegalStateException("Unable to find building connection for "
							+ partialConnector.building.getBuildingType() 
							+ " (buildingID: " + partialConnector.building.getBuildingID()
							+ ") in " + settlement.getName());
				}
			} else {
				throw new IllegalStateException("Unable to find building connection for "
						+ partialConnector.building.getBuildingType() 
						+ " (buildingID: " + partialConnector.building.getBuildingID()
						+ ") in " + settlement.getName());
			}
		}
	}

	/**
	 * Get the settlement.
	 * 
	 * @return settlement.
	 */
	public Settlement getSettlement() {
		return settlement;
	}

	/**
	 * Adds a new building connector.
	 * 
	 * @param buildingConnector new building connector.
	 */
	public void addBuildingConnection(BuildingConnector buildingConnector) {

		if (!buildingConnections.contains(buildingConnector)) {
			buildingConnections.add(buildingConnector);
		} else {
			throw new IllegalArgumentException("BuildingConnector already exists.");
		}
	}

	/**
	 * Removes an old building connector.
	 * 
	 * @param buildingConnector old building connector.
	 */
	public void removeBuildingConnection(BuildingConnector buildingConnector) {

		if (buildingConnections.contains(buildingConnector)) {
			buildingConnections.remove(buildingConnector);
		} else {
			throw new IllegalArgumentException("BuildingConnector does not exists.");
		}
	}

	/**
	 * Remove all building connectors to a given building.
	 * 
	 * @param building the building.
	 */
	public void removeAllConnectionsToBuilding(Building building) {

		Iterator<BuildingConnector> i = getConnectionsToBuilding(building).iterator();
		while (i.hasNext()) {
			BuildingConnector connector = i.next();
			removeBuildingConnection(connector);
		}
	}

	/**
	 * Gets all of the building connections between two buildings.
	 * 
	 * @param building1 the first building.
	 * @param building2 the second building.
	 * @return a set of building connectors.
	 */
	public Set<BuildingConnector> getBuildingConnections(Building building1, Building building2) {

		Set<BuildingConnector> result = new HashSet<BuildingConnector>();

		Iterator<BuildingConnector> i = buildingConnections.iterator();
		while (i.hasNext()) {
			BuildingConnector connector = i.next();
			if ((building1.equals(connector.getBuilding1()) && building2.equals(connector.getBuilding2()))
					|| (building1.equals(connector.getBuilding2()) && building2.equals(connector.getBuilding1()))) {
				result.add(connector);
			}
		}

		return result;
	}

	/**
	 * Checks if the settlement contains a given building connector.
	 * 
	 * @param connector the building connector.
	 * @return true if settlement contains building connector.
	 */
	public boolean containsBuildingConnector(BuildingConnector connector) {

		return buildingConnections.contains(connector);
	}

	/**
	 * Gets all of the building connections at a settlement.
	 * 
	 * @return set of building connectors.
	 */
	public Set<BuildingConnector> getAllBuildingConnections() {

		return buildingConnections;
	}

	/**
	 * Gets all of the connections to a given building.
	 * 
	 * @param building the building.
	 * @return a set of building connectors.
	 */
	public Set<BuildingConnector> getConnectionsToBuilding(Building building) {

		Set<BuildingConnector> result = new HashSet<BuildingConnector>();

		Iterator<BuildingConnector> i = buildingConnections.iterator();
		while (i.hasNext()) {
			BuildingConnector connector = i.next();
			if (building.equals(connector.getBuilding1()) || building.equals(connector.getBuilding2())) {
				result.add(connector);
			}
		}

		// System.out.println("# of connectors for " + building.getNickName() + " : " +
		// result.size());
		return result;
	}

	/**
	 * Checks if there is a valid interior walking path between two buildings.
	 * 
	 * @param building1 the first building.
	 * @param building2 the second building.
	 * @return true if valid interior walking path.
	 */
	public boolean hasValidPath(Building building1, Building building2) {

		boolean result = false;

		if ((building1 == null) || (building2 == null)) {
			throw new IllegalArgumentException("Building arguments cannot be null");
		}

		InsideBuildingPath validPath = determineShortestPath(building1, building1.getXLocation(),
				building1.getYLocation(), building2, building2.getXLocation(), building2.getYLocation());

		if (validPath != null) {
			result = true;
		} else if (logger.isLoggable(Level.FINEST)) {
			LogConsolidated.log(logger, Level.FINEST, 10000, sourceName,
					"Unable to find valid interior walking path between " + building1 + " and " + building2, null);
		}

		return result;
	}

	/**
	 * Determines the shortest building path between two locations in buildings.
	 * 
	 * @param building1     the first building.
	 * @param building1XLoc the starting X location in the first building.
	 * @param building1YLoc the starting Y location in the first building.
	 * @param building2     the second building.
	 * @param building2XLoc the ending X location in the second building.
	 * @param building2YLoc the ending Y location in the second building.
	 * @return shortest path or null if no path found.
	 */
	public InsideBuildingPath determineShortestPath(Building building1, double building1XLoc, double building1YLoc,
			Building building2, double building2XLoc, double building2YLoc) {

//		if ((building1 == null) || (building2 == null)) {
//			throw new IllegalArgumentException("Building arguments cannot be null");
//		}

		BuildingLocation startingLocation = new BuildingLocation(building1, building1XLoc, building1YLoc);
		BuildingLocation endingLocation = new BuildingLocation(building2, building2XLoc, building2YLoc);

		InsideBuildingPath startingPath = new InsideBuildingPath();
		startingPath.addPathLocation(startingLocation);

		InsideBuildingPath finalPath = null;
		if (!building1.equals(building2)) {
			// Check shortest path to target building from this building.
//			logger.config(building1.getNickName() + " " + building2.getNickName());
			finalPath = determineShortestPath(startingPath, building1, building2, endingLocation);
		} else {
			finalPath = startingPath;
			finalPath.addPathLocation(endingLocation);
		}

		// Iterate path index.
		if (finalPath != null) {
			finalPath.iteratePathLocation();
		}

		return finalPath;
	}

	/**
	 * Recursive method to determine the shortest path between two buildings.
	 * 
	 * @param existingPath    the current path.
	 * @param currentBuilding the current building.
	 * @param targetBuilding  the target building.
	 * @param endingLocation  the end building location.
	 * @return shortest path or null if none found.
	 */
	private InsideBuildingPath determineShortestPath(InsideBuildingPath existingPath, Building currentBuilding,
			Building targetBuilding, BuildingLocation endingLocation) {

		InsideBuildingPath result = null;

		// Try each building connection from current building.
		Iterator<BuildingConnector> i = getConnectionsToBuilding(currentBuilding).iterator();
		while (i.hasNext()) {
			BuildingConnector connector = i.next();

			Building connectionBuilding = null;
			Hatch nearHatch = null;
			Hatch farHatch = null;
			if (connector.getBuilding1().equals(currentBuilding)) {
				connectionBuilding = connector.getBuilding2();
				nearHatch = connector.getHatch1();
				farHatch = connector.getHatch2();
			} else {
				connectionBuilding = connector.getBuilding1();
				nearHatch = connector.getHatch2();
				farHatch = connector.getHatch1();
			}

			// Make sure building or connection is not already in existing path.
			boolean inExistingPath = false;
			if (existingPath.containsPathLocation(connectionBuilding)) {
				inExistingPath = true;
			}
			if (existingPath.containsPathLocation(connector)) {
				inExistingPath = true;
			}
			if (existingPath.containsPathLocation(nearHatch)) {
				inExistingPath = true;
			}
			if (existingPath.containsPathLocation(farHatch)) {
				inExistingPath = true;
			}
			if (inExistingPath) {
				continue;
			}

			// Copy existing path to create new path.
			InsideBuildingPath newPath = (InsideBuildingPath) existingPath.clone();

			// Add building connector to new path.
			if (connector.isSplitConnection()) {
				newPath.addPathLocation(nearHatch);
				newPath.addPathLocation(connector);
				newPath.addPathLocation(farHatch);
			} else {
				newPath.addPathLocation(connector);
			}

			InsideBuildingPath bestPath = null;
			if (connectionBuilding.equals(targetBuilding)) {
				// Add ending location within connection building.
				newPath.addPathLocation(endingLocation);
				bestPath = newPath;
			} else {
				// Add connection building to new path.
				newPath.addPathLocation(connectionBuilding);

				// Recursively call this method with new path and connection building.
				// TODO: how to avoid StackOverflow ?
//				logger.config(connectionBuilding.getNickName() + " -> " + targetBuilding.getNickName() 
//					+ " (" + endingLocation.getXLocation() + ", " + endingLocation.getYLocation() + ")");
				bestPath = determineShortestPath(newPath, connectionBuilding, targetBuilding, endingLocation);
			}

			if (bestPath != null) {
				if ((result == null) || (bestPath.getPathLength() < result.getPathLength())) {
					result = bestPath;
				}
			}
		}

		return result;
	}

	/**
	 * Create building connections from a new building to the surrounding buildings.
	 * 
	 * @param newBuilding the new building.
	 */
	public void createBuildingConnections(Building newBuilding) {

		boolean isBuildingConnector = newBuilding.hasFunction(FunctionType.BUILDING_CONNECTION);
		boolean hasLifeSupport = newBuilding.hasFunction(FunctionType.LIFE_SUPPORT);

		// boolean needEndCap = false;
		// if (newBuilding.getBuildingType().equalsIgnoreCase("hallway")
		// || newBuilding.getBuildingType().equalsIgnoreCase("tunnel")) {
		// needEndCap = true;
		// }

		// Only create building connections for inhabitable buildings.
		if (hasLifeSupport) {
			// If building connector, determine end connections first.
			if (isBuildingConnector) {
				// Try to create connections at North and South ends.
				createBuildingConnectorEndConnections(newBuilding);

			}
			// else
			// Determine connections at points along each of the building's four sides.
			createBuildingConnectionsAlongSides(newBuilding);
		}

	}

	/**
	 * Create connections from the North and South ends of a building connector
	 * building.
	 * 
	 * @param newBuilding the new building connector building.
	 */
	private void createBuildingConnectorEndConnections(Building newBuilding) {

		double connectionRange = (newBuilding.getWidth() / 2D) + .2D;

		// Determine a connection for the front end of the building.
		double frontX = 0D;
		double frontY = newBuilding.getLength() / 2D;
		Point2D frontPos = new Point2D.Double(frontX, frontY);
		double angleFront = 0D;
		double offsetFront = 1D;
		boolean goodFrontConnection = false;
		for (double x = 0D; (x <= 180D) && !goodFrontConnection; x += 10D) {
			offsetFront = offsetFront * -1D;
			angleFront = angleFront + (offsetFront * x);
			goodFrontConnection = createBuildingConnection(newBuilding, BuildingSide.FRONT, frontPos, connectionRange,
					angleFront, false);
		}

		// Determine a connection for the back end of the building.
		double backX = 0D;
		double backY = -1 * (newBuilding.getLength() / 2D);
		Point2D backPos = new Point2D.Double(backX, backY);
		double angleBack = 0D;
		double offsetBack = 1D;
		boolean goodBackConnection = false;
		for (double x = 0D; (x <= 180D) && !goodBackConnection; x += 10D) {
			offsetBack = offsetBack * -1D;
			angleBack = angleBack + (offsetBack * x);
			goodBackConnection = createBuildingConnection(newBuilding, BuildingSide.BACK, backPos, connectionRange,
					angleBack, false);
		}
	}

	/**
	 * Create connections along the four sides of the new building.
	 * 
	 * @param newBuilding the new building.
	 */
	private void createBuildingConnectionsAlongSides(Building newBuilding) {

		// Create connections along the front side of the building.
		createBuildingConnectionsAlongWidthSide(newBuilding, BuildingSide.FRONT);

		// Create connections along the back side of the building.
		createBuildingConnectionsAlongWidthSide(newBuilding, BuildingSide.BACK);

		// Create connections along the left side of the building.
		createBuildingConnectionsAlongLengthSide(newBuilding, BuildingSide.LEFT);

		// Create connections along the right side of the building.
		createBuildingConnectionsAlongLengthSide(newBuilding, BuildingSide.RIGHT);
	}

	/**
	 * Create building connections along a width side (front or back) of a building.
	 * 
	 * @param newBuilding the new building.
	 * @param yLoc        the building-local Y location of the width side.
	 */
	private void createBuildingConnectionsAlongWidthSide(Building newBuilding, BuildingSide side) {

		double yLoc = 0D;
		if (side == BuildingSide.FRONT) {
			yLoc = newBuilding.getLength() / 2D;
		} else if (side == BuildingSide.BACK) {
			yLoc = newBuilding.getLength() / -2D;
		}

		double sideX = 0D;
		double offset = 1D;
		for (double x = 0D; x < newBuilding.getWidth(); x += 1D) {
			offset = offset * -1D;
			sideX = sideX + (offset * x);
			createBuildingConnection(newBuilding, side, new Point2D.Double(sideX, yLoc), 1D, 0D, true);
		}
	}

	/**
	 * Create building connections along a length side (left or right) of a
	 * building.
	 * 
	 * @param newBuilding the new building.
	 * @param xLoc        the building-local X location of the length side.
	 */
	private void createBuildingConnectionsAlongLengthSide(Building newBuilding, BuildingSide side) {

		double xLoc = 0D;
		if (side == BuildingSide.LEFT) {
			xLoc = newBuilding.getWidth() / 2D;
		} else if (side == BuildingSide.RIGHT) {
			xLoc = newBuilding.getWidth() / -2D;
		}

		double sideY = 0D;
		double offset = 1D;
		for (double x = 0D; x < newBuilding.getLength(); x += 1D) {
			offset = offset * -1D;
			sideY = sideY + (offset * x);
			createBuildingConnection(newBuilding, side, new Point2D.Double(xLoc, sideY), 1D, 0D, true);
		}
	}

	/**
	 * Try to create another building connection at a given location along the edge
	 * of a building.
	 * 
	 * @param newBuilding     the new building.
	 * @param position        the building-local (not settlement-local) position
	 *                        along the edge of the new building.
	 * @param connectionRange the range (meters) allowed to connect to another
	 *                        building.
	 * @param offsetAngle     the rotation offset angle (degrees) clockwise from
	 *                        side-perpendicular (-90.0 to 90.0).
	 * @param checkRange      true if new building connection should not be created
	 *                        within a range from any existing building connection.
	 * @return true if connection was created.
	 */
	private boolean createBuildingConnection(Building newBuilding, BuildingSide side, Point2D position,
			double connectionRange, double offsetAngle, boolean checkRange) {

		double initialSecondX = 0D;
		double initialSecondY = 0D;
		if (side == BuildingSide.FRONT) {
			initialSecondY += connectionRange;
		} else if (side == BuildingSide.BACK) {
			initialSecondY -= connectionRange;
		} else if (side == BuildingSide.LEFT) {
			initialSecondX += connectionRange;
		} else if (side == BuildingSide.RIGHT) {
			initialSecondX -= connectionRange;
		}

		// Rotate second position by offset angle.
		double offsetAngleRad = Math.toRadians(offsetAngle);
		double secondX = (initialSecondX * Math.cos(offsetAngleRad)) - (initialSecondY * Math.sin(offsetAngleRad))
				+ position.getX();
		double secondY = (initialSecondX * Math.sin(offsetAngleRad)) + (initialSecondY * Math.cos(offsetAngleRad))
				+ position.getY();

		Point2D correctedLinePosition1 = LocalAreaUtil.getLocalRelativeLocation(position.getX(), position.getY(),
				newBuilding);
		Point2D correctedLinePosition2 = LocalAreaUtil.getLocalRelativeLocation(secondX, secondY, newBuilding);
		Line2D line = new Line2D.Double(correctedLinePosition1, correctedLinePosition2);

		Point2D firstBuildingConnectionPt = correctedLinePosition1;

		// Check if an existing building connection is too close to this connection.
		if (checkRange && connectionTooCloseToExistingConnection(newBuilding, firstBuildingConnectionPt)) {
			return false;
		}

		boolean goodConnection = false;
		BuildingManager manager = settlement.getBuildingManager();
		Iterator<Building> i = manager.getBuildings(FunctionType.LIFE_SUPPORT).iterator();
		while (i.hasNext() && !goodConnection) {
			Building building = i.next();
			if (!building.equals(newBuilding)) {
				if (getBuildingConnections(newBuilding, building).size() == 0) {

					Set<Point2D> collisionPoints = LocalAreaUtil.getLinePathCollisionPoints(line, building);
					if (collisionPoints.size() > 0) {

						// Determine closest collision point.
						Point2D closestCollisionPoint = null;
						double closestDistance = Double.MAX_VALUE;
						Iterator<Point2D> j = collisionPoints.iterator();
						while (j.hasNext()) {
							Point2D collisionPoint = j.next();
							double distance = collisionPoint.distance(firstBuildingConnectionPt);
							if (distance < closestDistance) {
								closestCollisionPoint = collisionPoint;
								closestDistance = distance;
							}
						}

						Point2D secondBuildingConnectionPt = closestCollisionPoint;

						// Check if an existing building connection is too close to this connection.
						if (checkRange
								&& connectionTooCloseToExistingConnection(building, secondBuildingConnectionPt)) {
							return false;
						}

						double hatch1Facing = determineHatchFacing(newBuilding, firstBuildingConnectionPt);
						double hatch2Facing = determineHatchFacing(building, secondBuildingConnectionPt);

						BuildingConnector connector = new BuildingConnector(newBuilding,
								firstBuildingConnectionPt.getX(), firstBuildingConnectionPt.getY(), hatch1Facing,
								building, secondBuildingConnectionPt.getX(), secondBuildingConnectionPt.getY(),
								hatch2Facing);
						addBuildingConnection(connector);

						goodConnection = true;
					}
				}
			}
		}

		return goodConnection;
	}

	/**
	 * Check if the new connection point location is too close to an existing
	 * connection point on a building.
	 * 
	 * @param building        the building.
	 * @param connectionPoint the new connection point location.
	 * @return true if too close to existing connection point.
	 */
	private boolean connectionTooCloseToExistingConnection(Building building, Point2D connectionPoint) {

		boolean result = false;

		Iterator<BuildingConnector> i = getConnectionsToBuilding(building).iterator();
		while (i.hasNext() && !result) {
			BuildingConnector connector = i.next();
			Hatch hatch = null;
			if (building.equals(connector.getBuilding1())) {
				hatch = connector.getHatch1();
			} else {
				hatch = connector.getHatch2();
			}
			Point2D point = new Point2D.Double(hatch.getXLocation(), hatch.getYLocation());
			double distance = connectionPoint.distance(point);
			if (distance < Hatch.WIDTH) {
				result = true;
			}
		}

		return result;
	}

	/**
	 * Determines the new hatch facing direction for a location on a building.
	 * 
	 * @param building     the building.
	 * @param connectionPt the new hatch connection point on the building.
	 * @return facing direction (degrees clockwise from North).
	 */
	private double determineHatchFacing(Building building, Point2D connectionPt) {

		double result = 0D;

		BuildingSide side = determineBuildingSide(building, connectionPt);

		if (side == BuildingSide.FRONT) {
			result = building.getFacing();
		} else if (side == BuildingSide.BACK) {
			result = building.getFacing() + 180D;
		} else if (side == BuildingSide.LEFT) {
			result = building.getFacing() + 270D;
		} else if (side == BuildingSide.RIGHT) {
			result = building.getFacing() + 90D;
		}

		while (result >= 360D) {
			result -= 360D;
		}

		while (result < 0D) {
			result += 360D;
		}

		return result;
	}

	/**
	 * Determines the building side of a point along the building's boundary.
	 * 
	 * @param building the building.
	 * @param point    the point along the building's boundary.
	 * @return building side or null if point is not along the building's boundary.
	 */
	private BuildingSide determineBuildingSide(Building building, Point2D point) {

		BuildingSide result = null;

		// Exception in thread "JavaFX Application Thread"
		// java.lang.NullPointerException
		Point2D buildingRelativePt = LocalAreaUtil.getObjectRelativeLocation(point.getX(), point.getY(), building);

		if (Math.abs(buildingRelativePt.getY() - (building.getLength() / 2D)) < SMALL_AMOUNT_COMPARISON) {
			result = BuildingSide.FRONT;
		} else if (Math.abs(buildingRelativePt.getY() - (building.getLength() / -2D)) < SMALL_AMOUNT_COMPARISON) {
			result = BuildingSide.BACK;
		} else if (Math.abs(buildingRelativePt.getX() - (building.getWidth() / 2D)) < SMALL_AMOUNT_COMPARISON) {
			result = BuildingSide.LEFT;
		} else if (Math.abs(buildingRelativePt.getX() - (building.getWidth() / -2D)) < SMALL_AMOUNT_COMPARISON) {
			result = BuildingSide.RIGHT;
		} else {
			logger.severe("Building side could not be determined for point");
		}

		return result;
	}

	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
		settlement = null;
		Iterator<BuildingConnector> i = buildingConnections.iterator();
		while (i.hasNext()) {
			i.next().destroy();
		}
		// buildingConnections.clear();
		buildingConnections = null;
	}

	/**
	 * Inner class for representing a partial building connector.
	 */
	private class PartialBuildingConnector implements Serializable {

		/** default serial id. */
		private static final long serialVersionUID = 1L;
		// Data members.
		private Building building;
		private double xLocation;
		private double yLocation;
		private double facing;
		private Building connectToBuilding;

		/**
		 * Constructor.
		 * 
		 * @param building          the building.
		 * @param xLocation         the X Location relative to the settlement.
		 * @param yLocation         the Y location relative to the settlement.
		 * @param facing            the facing (degrees).
		 * @param connectToBuilding the building to connect to.
		 */
		PartialBuildingConnector(Building building, double xLocation, double yLocation, double facing,
				Building connectToBuilding) {
			this.building = building;
			this.xLocation = xLocation;
			this.yLocation = yLocation;
			this.facing = facing;
			this.connectToBuilding = connectToBuilding;
		}
	}
}
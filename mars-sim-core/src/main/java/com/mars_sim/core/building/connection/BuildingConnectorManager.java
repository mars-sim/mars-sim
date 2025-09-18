/*
 * Mars Simulation Project
 * BuildingConnectorManager.java
 * @date 2025-07-18
 * @author Scott Davis
 */
package com.mars_sim.core.building.connection;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mars_sim.core.LocalAreaUtil;
import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.BuildingManager;
import com.mars_sim.core.building.BuildingTemplate;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.structure.Settlement;

/**
 * This class manages all building connectors at a settlement.
 */
public class BuildingConnectorManager implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private static final SimLogger logger = SimLogger.getLogger(BuildingConnectorManager.class.getName());
	
	/** Comparison to indicate a small but non-zero amount. */
	private static final double SMALL_AMOUNT_COMPARISON = .0000001D;

	private static final String NORTH = "north";
	private static final String EAST = "east";
	private static final String SOUTH = "south";
	private static final String WEST = "west";
	
	/**
	 * This be a bidirectional equality. Ends are stored according to the identifier value.
	 * A,B == B,A as well as A,B == A,B
	 */
	private static class PathKey {
		private Building lower;
		private Building upper;

		PathKey(Building start, Building end) {
			if (start.getIdentifier() < end.getIdentifier()) {
				this.lower = start;
				this.upper = end;
			}
			else {
				this.upper = start;
				this.lower = end;
			}
		}

		@Override
		public int hashCode() {
			return lower.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			PathKey other = (PathKey) obj;
			if (!lower.equals(other.lower))
				return false;
			return upper.equals(other.upper);
		}
	}
	
	private transient Cache<PathKey,List<InsidePathLocation>> learntPaths = buildCache();

	/**
	 * Inner class for representing a partial building connector.
	 */
	private static record PartialBuildingConnector(Building building, LocalPosition pos, double facing,
				Building connectToBuilding) {}

	/** Enum representing the four different sides of a building. */
	private enum BuildingSide {
		FRONT, BACK, LEFT, RIGHT
	}

	// Data members.
	private Settlement settlement;
	
	private Set<BuildingConnector> buildingConnections;
		
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
		
		initialize(settlement, buildingTemplates);
	}
	
	/**
	 * Initializes a list of building templates for a settlement. 
	 * 
	 * @param settlement
	 * @param buildingTemplates
	 */
	public void initialize(Settlement settlement, List<BuildingTemplate> buildingTemplates) {
		
		buildingConnections = new HashSet<>();

		BuildingManager buildingManager = settlement.getBuildingManager();

		// Create partial building connector list from building connection templates.
		List<PartialBuildingConnector> partialBuildingConnectorList = new ArrayList<>();
		for(var bt : buildingTemplates) {
			partialBuildingConnectorList.addAll(processBuildTemplate(bt, buildingManager));
		}

		processAllPartialBuildingConnectors(partialBuildingConnectorList);
	}
	
	/**
	 * Processes a building template.
	 * 
	 * @param settlement
	 * @param buildingTemplate
	 */
	public void processBuildingTemplate(Settlement settlement, BuildingTemplate buildingTemplate) {
		
		BuildingManager buildingManager = settlement.getBuildingManager();

		// Create partial building connector list from building connection templates.
		List<PartialBuildingConnector> partialBuildingConnectorList = new ArrayList<>();

		partialBuildingConnectorList.addAll(processBuildTemplate(buildingTemplate, buildingManager));

		processOnePartialBuildingConnector(partialBuildingConnectorList);
	}
	
	/**
	 * Processes just one partial building connector.
	 * 
	 * @param partialBuildingConnectorList
	 */
	private void processOnePartialBuildingConnector(List<PartialBuildingConnector> partialBuildingConnectorList) {
		PartialBuildingConnector partialConnector = partialBuildingConnectorList.remove(0);
		PartialBuildingConnector foundMatch = findConnectorMatch(partialConnector, partialBuildingConnectorList);

		if (foundMatch != null) {
				BuildingConnector buildingConnector = new BuildingConnector(partialConnector.building,
						partialConnector.pos, partialConnector.facing,
						foundMatch.building, foundMatch.pos, 
						foundMatch.facing);
				addBuildingConnection(buildingConnector);
				partialBuildingConnectorList.remove(foundMatch);
		} 
		else {
			throw new IllegalStateException(
					settlement.getName() + " - Missing/Invalid PartialBuildingConnector(s). "
					+ partialConnector.building.getName()
					+ " [templateID: " + partialConnector.building.getTemplateID() 
					+ "  building name: " + partialConnector.building.getName()
					+ "  pos: " + partialConnector.pos
					+ "  facing: " + partialConnector.facing
					+ "  connectToBuilding: " + partialConnector.connectToBuilding
					+ "]  partialConnectorLoc: " + partialConnector.pos
					+ "  List size: " + partialBuildingConnectorList.size()
					 + ".");
		}	
	}
	
	/**
	 * Processes all partial building connectors.
	 * 
	 * @param partialBuildingConnectorList
	 */
	private void processAllPartialBuildingConnectors(List<PartialBuildingConnector> partialBuildingConnectorList) {
		
		// Match up partial connectors to create building connectors.
		while (!partialBuildingConnectorList.isEmpty()) {
			processOnePartialBuildingConnector(partialBuildingConnectorList);
		}
	}

	/**
	 * Finds the best match for a partial connector in the pool of unmatched connectors.
	 * 
	 * @param seed
	 * @param unmatched
	 * @return
	 */
	private PartialBuildingConnector findConnectorMatch(PartialBuildingConnector seed,
						List<PartialBuildingConnector> unmatched) {

		LocalPosition partialConnectorLoc = seed.pos;

		// Filter current unmatched connectors to find those that match the seed
		List<PartialBuildingConnector> validPartialConnectors = unmatched.stream()
				.filter(pc -> pc.building.equals(seed.connectToBuilding)
						&& pc.connectToBuilding.equals(seed.building))
				.toList();

		// Out of valid connector choose the nearest
		PartialBuildingConnector bestFitConnector = null;
		if (!validPartialConnectors.isEmpty()) {
			double closestDistance = Double.MAX_VALUE;
			for(PartialBuildingConnector validConnector : validPartialConnectors) {
				double distance = partialConnectorLoc.getDistanceTo(validConnector.pos);
				if (distance < closestDistance) {
					bestFitConnector = validConnector;
					closestDistance = distance;
				}
			}
		}
		return bestFitConnector;
	}

	/**
	 * This processes a building template and creates a set of partial building connectors
	 * in the correct relative position and reference the correct other Building.
	 * 
	 * @param buildingTemplate
	 * @param buildingManager
	 * @return
	 */
	private Set<PartialBuildingConnector> processBuildTemplate(BuildingTemplate buildingTemplate,
			BuildingManager buildingManager) {
		Set<PartialBuildingConnector> partialBuildingConnectorList = new HashSet<>();

		String buildingID = buildingTemplate.getID();
		Building building = buildingManager.getBuildingByTemplateID(buildingID);
		if (building == null) {
			throw new IllegalStateException(
					"On buildingTemplate " + buildingTemplate.getBuildingName()
					+ "    buildingID " + buildingID 
					+ " does not exist in settlement " + settlement.getName());
		}
		double halfL = building.getLength() / 2;
		double halfW = building.getWidth() / 2;
				
		double bFacing = alignFacing(building.getFacing());	

		for(var connectionTemplate : buildingTemplate.getBuildingConnectionTemplates()) {
			Building connectionBuilding = buildingManager.getBuildingByTemplateID(connectionTemplate.getID());
			if (connectionBuilding == null) {
				throw new IllegalStateException(
						"On buildingTemplate " + buildingTemplate
						+ "    buildingID " + buildingID 
						+ "    connectionID " + connectionTemplate.getID() 
						+ " does not exist in settlement " + settlement.getName());
			}

			double connectionFacing = 0;		
			LocalPosition connectionPosn = null;
					
			String hatchFace = connectionTemplate.getHatchFace();
			if (hatchFace != null) {
				if (bFacing == 0) {
					if (hatchFace.equalsIgnoreCase(NORTH)) {
						connectionFacing = 0;
						connectionPosn = new LocalPosition(0, halfL);
					}
					else if (hatchFace.equalsIgnoreCase(EAST)) {
						connectionFacing = 90;
						connectionPosn = new LocalPosition(-halfW, 0);
					}
					else if (hatchFace.equalsIgnoreCase(SOUTH)) {
						connectionFacing = 0;
						connectionPosn = new LocalPosition(0, -halfL);
					}
					else if (hatchFace.equalsIgnoreCase(WEST)) {
						connectionFacing = 90;
						connectionPosn = new LocalPosition(halfW, 0);
					}
				}
				else if (bFacing == 90) {
					if (hatchFace.equalsIgnoreCase(NORTH)) {
						// verified as good
						connectionFacing = 0;
						connectionPosn = new LocalPosition(halfW, 0);
					}
					else if (hatchFace.equalsIgnoreCase(EAST)) {
						// verified as good
						connectionFacing = 90;
						connectionPosn = new LocalPosition(0, halfL);
					}
					else if (hatchFace.equalsIgnoreCase(SOUTH)) {
						// verified as good
						connectionFacing = 0;
						connectionPosn = new LocalPosition(-halfW, 0);
					}
					else if (hatchFace.equalsIgnoreCase(WEST)) {
						// verified as good
						connectionFacing = 90;
						connectionPosn = new LocalPosition(0, -halfL);
					}
				}
				else if (bFacing == 180) {
					if (hatchFace.equalsIgnoreCase(NORTH)) {
						// verified as 
						connectionFacing = 0;
						connectionPosn = new LocalPosition(0, -halfL);
					}
					else if (hatchFace.equalsIgnoreCase(EAST)) {
						// verified as good
						connectionFacing = 90;
						connectionPosn = new LocalPosition(halfW, 0);
					}
					else if (hatchFace.equalsIgnoreCase(SOUTH)) {
						connectionFacing = 0;
						connectionPosn = new LocalPosition(0, halfL);
					}
					else if (hatchFace.equalsIgnoreCase(WEST)) {
						// verified as good
						connectionFacing = 90;
						connectionPosn = new LocalPosition(-halfW, 0);
					}
				}
				else if (bFacing == 270) {
					if (hatchFace.equalsIgnoreCase(NORTH)) {
						// verified as good
						connectionFacing = 0;
						connectionPosn = new LocalPosition(-halfW, 0);
					}
					else if (hatchFace.equalsIgnoreCase(EAST)) {
						// verified as good 
						connectionFacing = 90;
						connectionPosn = new LocalPosition(0, -halfL);
					}
					else if (hatchFace.equalsIgnoreCase(SOUTH)) {
						// verified as good
						connectionFacing = 0;
						connectionPosn = new LocalPosition(halfW, 0);
					}
					else if (hatchFace.equalsIgnoreCase(WEST)) {
						// verified as good
						connectionFacing = 90; // both 90 or 270 are fine
						connectionPosn = new LocalPosition(0, halfL);
					}
				}
			}
			else {		
				connectionPosn = connectionTemplate.getPosition();
				if (connectionPosn.getX() == halfW) {
					connectionFacing = bFacing - 90D;
				} else if (connectionPosn.getX()  == -halfW) {
					connectionFacing = bFacing + 90D;
				} else if (connectionPosn.getY()  == halfL) {
					connectionFacing = bFacing;
				} else if (connectionPosn.getY()  == -halfL) {
					connectionFacing = bFacing + 180D;
				}

				connectionFacing = alignFacing(connectionFacing);
			}
			
			LocalPosition connectionSettlementLoc = LocalAreaUtil.convert2SettlementPos(connectionPosn, building);
			PartialBuildingConnector partialConnector = new PartialBuildingConnector(building,
					connectionSettlementLoc, connectionFacing, connectionBuilding);
			partialBuildingConnectorList.add(partialConnector);
		}

		return partialBuildingConnectorList;
	}

	private static double alignFacing(double facing) {
		if (facing < 0D) {
			facing += 360D;
		}

		if (facing > 360D) {
			facing -= 360D;
		}
		return facing;
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
	 * Removes all building connectors to a given building.
	 * 
	 * @param building the building.
	 */
	public void removeAllConnectionsToBuilding(Building building) {

		Iterator<BuildingConnector> i = getConnectionsToBuilding(building).iterator();
		while (i.hasNext()) {
			BuildingConnector connector = i.next();
			buildingConnections.remove(connector);
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

		Set<BuildingConnector> result = new HashSet<>();

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
		return buildingConnections.stream()
				.filter(c -> building.equals(c.getBuilding1()) || building.equals(c.getBuilding2()))
				.collect(Collectors.toSet());
	}

	/**
	 * Checks if there is a valid interior walking path between two buildings.
	 * 
	 * @param startBuilding the first building.
	 * @param endBuilding the second building.
	 * @return true if valid interior walking path.
	 */
	public boolean hasValidPath(Building startBuilding, Building endBuilding) {
		// Check agsint the already learnt paths
		PathKey key = new PathKey(startBuilding, endBuilding);
		if (learntPaths.getIfPresent(key) != null) {
			return true;
		}

		BuildingLocation start = new BuildingLocation(startBuilding, startBuilding.getPosition());
		BuildingLocation end = new BuildingLocation(endBuilding, endBuilding.getPosition());
		var finder = new PathFinder(this, start, end);

		var result = finder.isValidRoute();
		if (!result && logger.isLoggable(Level.FINEST)) {
			logger.fine(startBuilding, "Unable to find valid interior walking path to " + endBuilding);
		}

		return result;
	}

	/**
	 * Determines the shortest building path between two locations in buildings.
	 * 
	 * @param startBuilding     the first building.
	 * @param startPositionc the starting position in the first building.
	 * @param endBuilding     the second building.
	 * @param endPosition the ending position in the second building.
	 * @return shortest path or null if no path found.
	 */
	public InsideBuildingPath determineShortestPath(Building startBuilding, LocalPosition startPosition,
			Building endBuilding, LocalPosition endPosition) {
		
		BuildingLocation start = new BuildingLocation(startBuilding, startPosition);
		BuildingLocation end = new BuildingLocation(endBuilding, endPosition);

		// Check against the already learnt paths
		PathKey key = new PathKey(startBuilding, endBuilding);
		var foundPath = learntPaths.getIfPresent(key);
		if (foundPath != null) {
			// Reuse the found path but alter the start and end posns
			return new InsideBuildingPath(foundPath, start, end);
		}

		var finder = new PathFinder(this, start, end);
		if (finder.isValidRoute()) {
			// Actual path is immutable so it can be shared safely
			var sharedPath = Collections.unmodifiableList(finder.toPath());
			learntPaths.put(key, sharedPath);

			return new InsideBuildingPath(sharedPath);
		}
		return null;
	}

	/**
	 * Creates building connections from a new building to the surrounding buildings.
	 * 
	 * @param newBuilding the new building.
	 */
	public void createBuildingConnections(Building newBuilding) {

		// Only create building connections for habitable buildings.
		if (!newBuilding.isInhabitable()) {
			// If building connector, determine end connections first.
			if (newBuilding.hasFunction(FunctionType.CONNECTION)) {
				// Try to create connections at North and South ends.
				createBuildingConnectorEndConnections(newBuilding);
			}

			// Determine connections at points along each of the building's four sides.
			createBuildingConnectionsAlongSides(newBuilding);
		}
	}

	/**
	 * Creates connections from the North and South ends of a building connector
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
	 * Creates connections along the four sides of the new building.
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
	 * Creates building connections along a width side (front or back) of a building.
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
	 * Creates building connections along a length side (left or right) of a
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
	 * Tries to create another building connection at a given location along the edge
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
		switch(side) {
			case FRONT -> initialSecondY += connectionRange;
			case BACK -> initialSecondY -= connectionRange;
			case LEFT -> initialSecondX += connectionRange;
			case RIGHT -> initialSecondX -= connectionRange;
		}

		// Rotate second position by offset angle.
		double offsetAngleRad = Math.toRadians(offsetAngle);
		double secondX = (initialSecondX * Math.cos(offsetAngleRad)) - (initialSecondY * Math.sin(offsetAngleRad))
				+ position.getX();
		double secondY = (initialSecondX * Math.sin(offsetAngleRad)) + (initialSecondY * Math.cos(offsetAngleRad))
				+ position.getY();

		Point2D correctedLinePosition1 = LocalAreaUtil.convert2SettlementPos(position.getX(), position.getY(),
				newBuilding);
		Point2D correctedLinePosition2 = LocalAreaUtil.convert2SettlementPos(secondX, secondY, newBuilding);
		Line2D line = new Line2D.Double(correctedLinePosition1, correctedLinePosition2);

		Point2D firstBuildingConnectionPt = correctedLinePosition1;

		// Check if an existing building connection is too close to this connection.
		if (checkRange && connectionTooCloseToExistingConnection(newBuilding, firstBuildingConnectionPt)) {
			return false;
		}

		boolean goodConnection = false;
		BuildingManager manager = settlement.getBuildingManager();
		
		Iterator<Building> i = manager.getBuildingSet(FunctionType.LIFE_SUPPORT).iterator();
		while (i.hasNext() && !goodConnection) {
			Building building = i.next();
			if (!building.equals(newBuilding)
				&& getBuildingConnections(newBuilding, building).isEmpty()) {

				Set<Point2D> collisionPoints = LocalAreaUtil.getLinePathCollisionPoints(line, building);
				if (!collisionPoints.isEmpty()) {

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

					if (closestCollisionPoint == null)
						return false;
					
					Point2D secondBuildingConnectionPt = closestCollisionPoint;

					// Check if an existing building connection is too close to this connection.
					if (checkRange
							&& connectionTooCloseToExistingConnection(building, secondBuildingConnectionPt)) {
						return false;
					}

					double hatch1Facing = determineHatchFacing(newBuilding, firstBuildingConnectionPt);
					double hatch2Facing = determineHatchFacing(building, secondBuildingConnectionPt);

					BuildingConnector connector = new BuildingConnector(newBuilding,
							new LocalPosition(firstBuildingConnectionPt.getX(), firstBuildingConnectionPt.getY()),
							hatch1Facing,
							building,
							new LocalPosition(secondBuildingConnectionPt.getX(), secondBuildingConnectionPt.getY()),
							hatch2Facing);
					addBuildingConnection(connector);

					goodConnection = true;
				}
			}
		}

		return goodConnection;
	}

	/**
	 * Checks if the new connection point location is too close to an existing
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

		BuildingSide side = determineBuildingSide(building, connectionPt);

		double result = switch(side) {
			case FRONT -> building.getFacing();
			case BACK -> building.getFacing() + 180D;
			case LEFT -> building.getFacing() + 270D;
			case RIGHT -> building.getFacing() + 90D;
		};
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

		LocalPosition buildingRelativePt = LocalAreaUtil.convert2LocalPos(new LocalPosition(point.getX(), point.getY()),
													building);

		if (Math.abs(buildingRelativePt.getY() - (building.getLength() / 2D)) < SMALL_AMOUNT_COMPARISON) {
			result = BuildingSide.FRONT;
		} else if (Math.abs(buildingRelativePt.getY() - (building.getLength() / -2D)) < SMALL_AMOUNT_COMPARISON) {
			result = BuildingSide.BACK;
		} else if (Math.abs(buildingRelativePt.getX() - (building.getWidth() / 2D)) < SMALL_AMOUNT_COMPARISON) {
			result = BuildingSide.LEFT;
		} else if (Math.abs(buildingRelativePt.getX() - (building.getWidth() / -2D)) < SMALL_AMOUNT_COMPARISON) {
			result = BuildingSide.RIGHT;
		} else {
			logger.severe(building, "Building side could not be determined for point");
		}

		return result;
	}

	/**
	 * Prepares object for garbage collection.
	 */
	public void destroy() {
		buildingConnections.forEach(c -> c.destroy());
		buildingConnections = null;
	}

	/**
	 * Initialises the objects.
	 *  
	 * @param in
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private void readObject(java.io.ObjectInputStream in)
    	throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		learntPaths = buildCache();
	}

	/**
	 * Gets a status of the learnt path cache.
	 * 
	 * @return
	 */
	public String getPathCacheStatus() {
		var stats = learntPaths.stats();
		return "Size=" + learntPaths.size() + ", Hit rate=" + Math.round(stats.hitRate() * 1000D)/10D
			+ "%, requests=" + stats.requestCount() + ", evicted=" + stats.evictionCount();
	}

	/**
	 * Uses a cache that will purge the older paths.
	 * 
	 * @return
	 */
	private static Cache<PathKey, List<InsidePathLocation>> buildCache() {
		// Set a maximum number of routes to be cached
		// Use a concurrency of 1 because it's a private Cache for this settlement
		return CacheBuilder.newBuilder()
						.recordStats()
						.concurrencyLevel(1)
						.maximumSize(100).build();
	}
}

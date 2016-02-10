package edu.cwru.sepia.agent;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.environment.model.history.History;
import edu.cwru.sepia.environment.model.state.ResourceNode;
import edu.cwru.sepia.environment.model.state.State;
import edu.cwru.sepia.environment.model.state.Unit;
import edu.cwru.sepia.util.Direction;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

public class AstarAgent extends Agent {

    static class MapLocation implements Comparable<MapLocation>
    {
        public int x, y;
        MapLocation cameFrom;
        float heuristic;
        float cost;

        public MapLocation(int x, int y, MapLocation cameFrom, float cost)
        {
            this.x = x;
            this.y = y;
        }
        
        @Override
        public boolean equals(Object o) {
        	if (o instanceof MapLocation) {
        		MapLocation temp = (MapLocation)o;
        		return temp.x == this.x && temp.y == this.y;
        	}
        	else {
        		return false;
        	}
        }
        
        @Override
    	public int compareTo(MapLocation loc) {
    		
    		// Determine the estimated cost for each node
    		double cost = heuristic + this.cost;
    		double testCost = loc.heuristic + loc.cost;
    		
    		if (cost > testCost) {
    			return 1;
    		}
    		else if (cost == testCost) {
    			return 0;
    		}
    		else {
    			return -1;
    		}
    	}
    }

    Stack<MapLocation> path;
    int footmanID, townhallID, enemyFootmanID;
    MapLocation nextLoc;

    private long totalPlanTime = 0; // nsecs
    private long totalExecutionTime = 0; //nsecs

    public AstarAgent(int playernum)
    {
        super(playernum);

        System.out.println("Constructed AstarAgent");
    }

    @Override
    public Map<Integer, Action> initialStep(State.StateView newstate, History.HistoryView statehistory) {
        // get the footman location
        List<Integer> unitIDs = newstate.getUnitIds(playernum);

        if(unitIDs.size() == 0)
        {
            System.err.println("No units found!");
            return null;
        }

        footmanID = unitIDs.get(0);

        // double check that this is a footman
        if(!newstate.getUnit(footmanID).getTemplateView().getName().equals("Footman"))
        {
            System.err.println("Footman unit not found");
            return null;
        }

        // find the enemy playernum
        Integer[] playerNums = newstate.getPlayerNumbers();
        int enemyPlayerNum = -1;
        for(Integer playerNum : playerNums)
        {
            if(playerNum != playernum) {
                enemyPlayerNum = playerNum;
                break;
            }
        }

        if(enemyPlayerNum == -1)
        {
            System.err.println("Failed to get enemy playernumber");
            return null;
        }

        // find the townhall ID
        List<Integer> enemyUnitIDs = newstate.getUnitIds(enemyPlayerNum);

        if(enemyUnitIDs.size() == 0)
        {
            System.err.println("Failed to find enemy units");
            return null;
        }

        townhallID = -1;
        enemyFootmanID = -1;
        for(Integer unitID : enemyUnitIDs)
        {
            Unit.UnitView tempUnit = newstate.getUnit(unitID);
            String unitType = tempUnit.getTemplateView().getName().toLowerCase();
            if(unitType.equals("townhall"))
            {
                townhallID = unitID;
            }
            else if(unitType.equals("footman"))
            {
                enemyFootmanID = unitID;
            }
            else
            {
                System.err.println("Unknown unit type");
            }
        }

        if(townhallID == -1) {
            System.err.println("Error: Couldn't find townhall");
            return null;
        }

        long startTime = System.nanoTime();
        path = findPath(newstate);
        totalPlanTime += System.nanoTime() - startTime;

        return middleStep(newstate, statehistory);
    }

    @Override
    public Map<Integer, Action> middleStep(State.StateView newstate, History.HistoryView statehistory) {
        long startTime = System.nanoTime();
        long planTime = 0;

        Map<Integer, Action> actions = new HashMap<Integer, Action>();

        if(shouldReplanPath(newstate, statehistory, path)) {
            long planStartTime = System.nanoTime();
            path = findPath(newstate);
            planTime = System.nanoTime() - planStartTime;
            totalPlanTime += planTime;
        }

        Unit.UnitView footmanUnit = newstate.getUnit(footmanID);

        int footmanX = footmanUnit.getXPosition();
        int footmanY = footmanUnit.getYPosition();

        if(!path.empty() && (nextLoc == null || (footmanX == nextLoc.x && footmanY == nextLoc.y))) {

            // stat moving to the next step in the path
            nextLoc = path.pop();

            System.out.println("Moving to (" + nextLoc.x + ", " + nextLoc.y + ")");
        }

        if(nextLoc != null && (footmanX != nextLoc.x || footmanY != nextLoc.y))
        {
            int xDiff = nextLoc.x - footmanX;
            int yDiff = nextLoc.y - footmanY;

            // figure out the direction the footman needs to move in
            Direction nextDirection = getNextDirection(xDiff, yDiff);

            actions.put(footmanID, Action.createPrimitiveMove(footmanID, nextDirection));
        } else {
            Unit.UnitView townhallUnit = newstate.getUnit(townhallID);

            // if townhall was destroyed on the last turn
            if(townhallUnit == null) {
                terminalStep(newstate, statehistory);
                return actions;
            }

            if(Math.abs(footmanX - townhallUnit.getXPosition()) > 1 ||
                    Math.abs(footmanY - townhallUnit.getYPosition()) > 1)
            {
                System.err.println("Invalid plan. Cannot attack townhall");
                totalExecutionTime += System.nanoTime() - startTime - planTime;
                return actions;
            }
            else {
                System.out.println("Attacking TownHall");
                // if no more movements in the planned path then attack
                actions.put(footmanID, Action.createPrimitiveAttack(footmanID, townhallID));
            }
        }

        totalExecutionTime += System.nanoTime() - startTime - planTime;
        return actions;
    }

    @Override
    public void terminalStep(State.StateView newstate, History.HistoryView statehistory) {
        System.out.println("Total turns: " + newstate.getTurnNumber());
        System.out.println("Total planning time: " + totalPlanTime/1e9);
        System.out.println("Total execution time: " + totalExecutionTime/1e9);
        System.out.println("Total time: " + (totalExecutionTime + totalPlanTime)/1e9);
    }

    @Override
    public void savePlayerData(OutputStream os) {

    }

    @Override
    public void loadPlayerData(InputStream is) {

    }

    /**
     * You will implement this method.
     *
     * This method should return true when the path needs to be replanned
     * and false otherwise. This will be necessary on the dynamic map where the
     * footman will move to block your unit.
     *
     * @param state
     * @param history
     * @param currentPath
     * @return
     */
    private boolean shouldReplanPath(State.StateView state, History.HistoryView history, Stack<MapLocation> currentPath)
    {
    	MapLocation enemyFootmanLocation = null;
    	if (enemyFootmanID == -1){
    		return false;
    	}
    	else{
    		Unit.UnitView enemy = state.getUnit(enemyFootmanID);
    		enemyFootmanLocation = new MapLocation(enemy.getXPosition(), enemy.getYPosition(), null, 0);
    	}
    	// See if the enemy footman is within the current path
    	for (MapLocation location : currentPath){
    		if (enemyFootmanLocation.equals(location)){
    			return true;
    		}
    	}
        return false;
    }

    /**
     * This method is implemented for you. You should look at it to see examples of
     * how to find units and resources in Sepia.
     *
     * @param state
     * @return
     */
    private Stack<MapLocation> findPath(State.StateView state)
    {
        Unit.UnitView townhallUnit = state.getUnit(townhallID);
        Unit.UnitView footmanUnit = state.getUnit(footmanID);

        MapLocation startLoc = new MapLocation(footmanUnit.getXPosition(), footmanUnit.getYPosition(), null, 0);

        MapLocation goalLoc = new MapLocation(townhallUnit.getXPosition(), townhallUnit.getYPosition(), null, 0);

        MapLocation footmanLoc = null;
        if(enemyFootmanID != -1) {
            Unit.UnitView enemyFootmanUnit = state.getUnit(enemyFootmanID);
            footmanLoc = new MapLocation(enemyFootmanUnit.getXPosition(), enemyFootmanUnit.getYPosition(), null, 0);
        }

        // get resource locations
        List<Integer> resourceIDs = state.getAllResourceIds();
        Set<MapLocation> resourceLocations = new HashSet<MapLocation>();
        for(Integer resourceID : resourceIDs)
        {
            ResourceNode.ResourceView resource = state.getResourceNode(resourceID);

            resourceLocations.add(new MapLocation(resource.getXPosition(), resource.getYPosition(), null, 0));
        }

        return AstarSearch(startLoc, goalLoc, state.getXExtent(), state.getYExtent(), footmanLoc, resourceLocations);
    }
    /**
     * This is the method you will implement for the assignment. Your implementation
     * will use the A* algorithm to compute the optimum path from the start position to
     * a position adjacent to the goal position.
     *
     * You will return a Stack of positions with the top of the stack being the first space to move to
     * and the bottom of the stack being the last space to move to. If there is no path to the townhall
     * then return null from the method and the agent will print a message and do nothing.
     * The code to execute the plan is provided for you in the middleStep method.
     *
     * As an example consider the following simple map
     *
     * F - - - -
     * x x x - x
     * H - - - -
     *
     * F is the footman
     * H is the townhall
     * x's are occupied spaces
     *
     * xExtent would be 5 for this map with valid X coordinates in the range of [0, 4]
     * x=0 is the left most column and x=4 is the right most column
     *
     * yExtent would be 3 for this map with valid Y coordinates in the range of [0, 2]
     * y=0 is the top most row and y=2 is the bottom most row
     *
     * resourceLocations would be {(0,1), (1,1), (2,1), (4,1)}
     *
     * The path would be
     *
     * (1,0)
     * (2,0)
     * (3,1)
     * (2,2)
     * (1,2)
     *
     * Notice how the initial footman position and the townhall position are not included in the path stack
     *
     * @param start Starting position of the footman
     * @param goal MapLocation of the townhall
     * @param xExtent Width of the map
     * @param yExtent Height of the map
     * @param resourceLocations Set of positions occupied by resources
     * @return Stack of positions with top of stack being first move in plan
     */
    private Stack<MapLocation> AstarSearch(MapLocation start, MapLocation goal, int xExtent, int yExtent, MapLocation enemyFootmanLoc, Set<MapLocation> resourceLocations)
    {
    	// Declare open and closed lists
    	ArrayList<MapLocation> openList = new ArrayList<MapLocation>();
    	ArrayList<MapLocation> closedList = new ArrayList<MapLocation>();
    	
    	closedList.add(enemyFootmanLoc);
    	// Add the starting location to the open list and empty the closed list
    	openList.add(start);
    	Collections.sort(openList);
    	
    	// While there are still nodes in the open list and the target hasn't been found
    	while (openList.size() > 0) {
    		
    		// Goal test
    		MapLocation curr = openList.get(0);
    		if (curr.x == goal.x && curr.y == goal.y) {
    			break;
    		}
    		
    		// Move this node from open list to closed list
    		openList.remove(curr);
    		closedList.add(curr);
    		
    		// Look at every neighbor of the step
    		ArrayList<MapLocation> neighbors = produceNeighborList(curr, xExtent, yExtent, resourceLocations);
    		for (MapLocation neighbor: neighbors) {
    			
    			/*
    			 * Calculate the path cost of reaching the neighbor
    			 * Assuming the movement cost is just 1
    			 */
    			float checkCost = curr.cost + 1;
    			
    			// If the cost is less than the cost known for this position, we have found a better path. Remove it from the open or closed lists
    			if (checkCost < neighbor.cost) {
    				if (openList.contains(neighbor)) {
    					openList.remove(neighbor);
    				}
    				if (closedList.contains(neighbor)) {
    					closedList.remove(neighbor);
    				}
    			}
    			
    			// If the location isn't in either the open or closed list, record the costs for location and add it to the open list. Record the path to this node.
    			if (!openList.contains(neighbor) && !closedList.contains(neighbor)) {
    				neighbor.cost = checkCost;
    				neighbor.heuristic = computeHeuristicCost(curr.x, curr.y, goal.x, goal.y);
    				openList.add(neighbor);
    				neighbor.cameFrom = curr;
    				Collections.sort(openList);
    			}
    		}
    	}
    	    	
    	// Check that the goal has a parent node
    	if (openList.get(0).cameFrom == null) {
    		return null;
    	}
    	
    	/*
    	 * Initializes a stack and returns the path on it
    	 * Ignores the start and end nodes as specified in the problem description
    	 */
    	Stack<MapLocation> path = new Stack<MapLocation>();
    	
    	// While the current node does not equal start
    	MapLocation goalLoc = openList.get(0).cameFrom;
    	while (!goalLoc.equals(start)) {
    		path.push(goalLoc);
    		goalLoc = goalLoc.cameFrom;
    	}
    	
        return path;
    }

    /**
     * Computes the heuristic for this map location using the Chebyshev distance
     * max(|x2 - x1|, |y2 - y1|)
     * 
     * @param currentX 
     * @param currentY
     * @param goalX
     * @param goalY
     * @return
     */
	public float computeHeuristicCost(int currentX, int currentY, int goalX, int goalY) {
		int xDistance = goalX - currentX;
		int yDistance = goalY - currentY;
		
		// Absolute value
		if (xDistance < 0) {
			xDistance = xDistance * -1;
		}
		
		// Absolute value
		if (yDistance < 0) {
			yDistance = yDistance * -1;
		}
		
		// Determines the max of the two
		if (xDistance > yDistance) {
			return xDistance;
		}
		else {
			return yDistance;
		}
	}
    
    /**
     * Check if attempted move is valid
     * 
     * @param current the location of the agent
     * @param next the map location that is being tested
     * @param xExtent Width of map
     * @param yExtent Height of map
     * @return true if the next map location is valid
     */
    public boolean isValidMapLocation(MapLocation current, MapLocation next, int xExtent, int yExtent, Set<MapLocation> resourceLocations) {
    	// Tests grid bounds to determine if the next location is within the grid
    	boolean isOutsideGrid = (next.x < 0) || (next.y < 0) || (next.x >= xExtent) || (next.y >= yExtent);
    	// TODO: Insert condition that covers case where agent is blocked
    	for (MapLocation loc: resourceLocations) {
    		if (loc.equals(next)) {
    			return false;
    		}
    	}
    	return !isOutsideGrid;
    }
    
    /**
     * A method which produces a list of neighbor map locations from the current map location
     * 
     * @param current the current map location
     * @param xExtent the width of the map
     * @param yExtent the height of the map
     * @return a list of neighbor map locations
     */
    public ArrayList<MapLocation> produceNeighborList(MapLocation current, int xExtent, int yExtent, Set<MapLocation> resourceLocations) {
    	ArrayList<MapLocation> neighbors = new ArrayList<MapLocation>();
    	
    	// Iterates through all neighbor nodes
    	for (int x = -1; x < 2; x++) {
    		for (int y = -1; y < 2; y++) {
    			
    			// We don't want to add the current node to the neighbors list
    			if (x == current.x && y == current.y) {
    				continue;
    			}
    			
    			// Compute the location of the current neighbor
    			int neighborX = current.x + x;
    			int neighborY = current.y + y;
    			MapLocation neighbor = new MapLocation(neighborX, neighborY, null, 0);
    			
    			// If the neighbor is valid, add it to the list of neighbors
    			if (isValidMapLocation(current, neighbor, xExtent, yExtent, resourceLocations)) {
    				neighbors.add(neighbor);
    			}
    		}
    	}
    	
    	return neighbors;
    }
    
    /**
     * Primitive actions take a direction (e.g. NORTH, NORTHEAST, etc)
     * This converts the difference between the current position and the
     * desired position to a direction.
     *
     * @param xDiff Integer equal to 1, 0 or -1
     * @param yDiff Integer equal to 1, 0 or -1
     * @return A Direction instance (e.g. SOUTHWEST) or null in the case of error
     */
    private Direction getNextDirection(int xDiff, int yDiff) {

        // figure out the direction the footman needs to move in
        if(xDiff == 1 && yDiff == 1)
        {
            return Direction.SOUTHEAST;
        }
        else if(xDiff == 1 && yDiff == 0)
        {
            return Direction.EAST;
        }
        else if(xDiff == 1 && yDiff == -1)
        {
            return Direction.NORTHEAST;
        }
        else if(xDiff == 0 && yDiff == 1)
        {
            return Direction.SOUTH;
        }
        else if(xDiff == 0 && yDiff == -1)
        {
            return Direction.NORTH;
        }
        else if(xDiff == -1 && yDiff == 1)
        {
            return Direction.SOUTHWEST;
        }
        else if(xDiff == -1 && yDiff == 0)
        {
            return Direction.WEST;
        }
        else if(xDiff == -1 && yDiff == -1)
        {
            return Direction.NORTHWEST;
        }

        System.err.println("Invalid path. Could not determine direction");
        return null;
    }
}

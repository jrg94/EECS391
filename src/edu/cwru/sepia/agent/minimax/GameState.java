package edu.cwru.sepia.agent.minimax;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.action.ActionType;
import edu.cwru.sepia.action.DirectedAction;
import edu.cwru.sepia.action.TargetedAction;
import edu.cwru.sepia.environment.model.state.State;
import edu.cwru.sepia.environment.model.state.Unit;
import edu.cwru.sepia.environment.model.state.Unit.UnitView;
import edu.cwru.sepia.util.Direction;

import java.util.*;

/**
 * This class stores all of the information the agent
 * needs to know about the state of the game. For example this
 * might include things like footmen HP and positions.
 *
 * Add any information or methods you would like to this class,
 * but do not delete or change the signatures of the provided methods.
 */
public class GameState {
	
	private State.StateView state;
	private List<Unit.UnitView> footmen;
	private List<Unit.UnitView> archers;
	private List<Integer> obstacleIDs;
	private int xMax;
	private int yMax;

    /**
     * You will implement this constructor. It will
     * extract all of the needed state information from the built in
     * SEPIA state view.
     *
     * You may find the following state methods useful:
     *
     * state.getXExtent() and state.getYExtent(): get the map dimensions
     * state.getAllResourceIDs(): returns all of the obstacles in the map
     * state.getResourceNode(Integer resourceID): Return a ResourceView for the given ID
     *
     * For a given ResourceView you can query the position using
     * resource.getXPosition() and resource.getYPosition()
     *
     * For a given unit you will need to find the attack damage, range and max HP
     * unitView.getTemplateView().getRange(): This gives you the attack range
     * unitView.getTemplateView().getBasicAttack(): The amount of damage this unit deals
     * unitView.getTemplateView().getBaseHealth(): The maximum amount of health of this unit
     *
     * @param state Current state of the episode
     */
    public GameState(State.StateView state) {
	
    	// Initializes the lists of units
		footmen = new LinkedList<Unit.UnitView>();
		archers = new LinkedList<Unit.UnitView>();
		
		// Generates the lists of archers and footmen based on unit type
		for (Unit.UnitView unit: state.getAllUnits()) {
			String unitType = unit.getTemplateView().getName().toLowerCase();
			
			if (unitType.equals("footman")) {
				footmen.add(unit);
			}
			else if (unitType.equals("archer")) {
				archers.add(unit);
			}
		}
		
		// Track the size of the grid
		xMax = state.getXExtent();
		yMax = state.getYExtent();
		
		// Track the state of the obstacles
		obstacleIDs = state.getAllResourceIds();
		
		// If all else fails, have a direct reference to the state
		this.state = state;
    }

    /**
     * You will implement this function.
     *
     * You should use weighted linear combination of features.
     * The features may be primitives from the state (such as hp of a unit)
     * or they may be higher level summaries of information from the state such
     * as distance to a specific location. Come up with whatever features you think
     * are useful and weight them appropriately.
     *
     * It is recommended that you start simple until you have your algorithm working. Then watch
     * your agent play and try to add features that correct mistakes it makes. However, remember that
     * your features should be as fast as possible to compute. If the features are slow then you will be
     * able to do less plys in a turn.
     *
     * Add a good comment about what is in your utility and why you chose those features.
     *
     * @return The weighted linear combination of the features
     */
    public double getUtility() {
    	
    	// Initialize the current shortest path to zero
    	Double totalShortestPath = 0.0;
    	
    	// For each footman in the game
    	for (Unit.UnitView footman: footmen) {
    		
    		// Initialize the current shortest path to infinity
    		Double footmanShortestPath = Double.POSITIVE_INFINITY;
    		
    		// For each archer in the game
    		for (Unit.UnitView archer: archers) {
    		
    			// Compute the distance between the footman and the archer - c = sqrt(a^2 + b^2)
    			double a = footman.getXPosition() - archer.getXPosition();
    			double b = footman.getYPosition() - archer.getYPosition();
    			double aSquared = Math.pow(a, 2);
    			double bSquared = Math.pow(b, 2);
    			double c = Math.sqrt(aSquared + bSquared);
    			
    			// Store the shortest path
    			if (c < footmanShortestPath) {
    				footmanShortestPath = c;
    			}
    		}
    		
    		totalShortestPath += footmanShortestPath;
    	}
    	
    	/**
    	 * Our utility function calculates the average shortest path (straight line)
    	 * to an archer. The smallest average path for any set of footmen yields the highest 
    	 * utility
    	 */
    	
    	// The total shortest path is averaged, inverted, and normalized (based on largest possible straight line path) 
    	// such that shortest paths yield higher utilities
    	// TODO: Figure out how to normalize this
    	Double utility = 1 / (totalShortestPath / footmen.size());
    	
        return utility;
    }

    /**
     * You will implement this function.
     *
     * This will return a list of GameStateChild objects. You will generate all of the possible
     * actions in a step and then determine the resulting game state from that action. These are your GameStateChildren.
     *
     * You may find it useful to iterate over all the different directions in SEPIA.
     *
     * for(Direction direction : Directions.values())
     *
     * To get the resulting position from a move in that direction you can do the following
     * x += direction.xComponent()
     * y += direction.yComponent()
     *
     * @return All possible actions and their associated resulting game state
     */
    public List<GameStateChild> getChildren() {
    	
    	List<GameStateChild> allActionsAndState = new LinkedList<GameStateChild>();
    	
    	// TODO: Take the generated hashmaps for each unit and combine them to form
    	// all the possible combinations of next states. Decide if this is a good
    	// way to do this or hardcode this for two footmen.
    	
        return allActionsAndState;
    }
    
    /**
     * Produces a set of unit actions 
     * @param unit
     * @return
     */
    private List<Map<Integer, Action>> getUnitActions(Unit.UnitView unit) {
    	
    	// The list of action maps
    	List<Map<Integer, Action>> unitActions = new LinkedList<Map<Integer, Action>>();
    	
    	// Check each direction for this unit
    	for (Direction direction: Direction.values()) {
    		
    		// If this unit is not blocked
    		if (!isValidMove(unit, direction)) {
    			
    		}
    	}
    	return unitActions;
    }
    
    /**
     * Tests to see if movement is legal within the bounds of the board
     * @param x the current x of a unit in this state
     * @param y the current y of a unit in this state
     * @param dir the intended movement direction
     * @return true if the intended movement is legal with the bounds of the board
     */
    private boolean isInBounds(int x, int y, Direction dir) {
    	boolean inX = x + dir.xComponent() < xMax && x >= 0;
    	boolean inY = y + dir.yComponent() < yMax && y >= 0;
    	
    	return inX && inY;
    }
    
    /**
     * Tests to see if movement is legal based on whether the space is occupied or not
     * @param unit The unit to be moved
     * @param dir The direction of movement
     * @return true if the move is valid
     */
    private boolean isValidMove(Unit.UnitView unit, Direction dir) {
    	
    	int newX = unit.getXPosition() + dir.xComponent();
    	int newY = unit.getYPosition() + dir.yComponent();
    	
    	// If this move is in the bounds of the map, we can check if the move is occupied
    	if (isInBounds(newX, newY, dir)) {
    	
	    	boolean isResource = state.isResourceAt(newX, newY);
	    	boolean isUnit = state.isUnitAt(newX, newY);
	    	
	    	// If there is a unit in the new space
	    	if (isUnit && !isResource) {
	    	
		    	// Get the unit and its type
		    	Integer otherUnitID = state.unitAt(newX, newY);
		    	Unit.UnitView otherUnit = state.getUnit(otherUnitID);
		    	String otherUnitType = otherUnit.getTemplateView().getName().toLowerCase();
		    	
		    	String unitType = unit.getTemplateView().getName().toLowerCase();
		    	
		    	// TODO: Decide if it is necessary to set this up for archers and footmen
		    	// or if it is safe to assume that this function will be used to attack 
		    	// as a footman only
	    	}
	    	
	    	// If space does not have a resource or unit on it, 
	    	else if (!isResource && !isUnit) {
	    		return true;
	    	}
    	}
    	
    	return false;
    }
    
}

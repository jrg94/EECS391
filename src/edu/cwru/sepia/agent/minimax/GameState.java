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
	
	private boolean maxPlayer;

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
    	
    	maxPlayer = false;
	
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
    
    // TODO: write a method that generates all combinations of actions
    // This has to be recursive because we can't dynamically write nested loops
    
    /**
     * Produces a set of unit actions 
     * 
     * @param unit the unit for which the actions will be generated
     * @return 
     */
    private List<Action> getUnitActions(Unit.UnitView unit) {
    	
    	// The list of action maps
    	List<Action> unitActions = new LinkedList<Action>();
    	
    	// Check each direction for this unit
    	for (Direction direction: Direction.values()) {
    		
    		// If this move is valid, add 
    		if (!isValidMove(unit, direction)) {
    			unitActions.add(Action.createPrimitiveMove(unit.getID(), direction));
    		}
    	}
    	
    	// Determines if we should attack footmen or archers
    	boolean isFootman = unit.getTemplateView().getName().toLowerCase().equals("footman");
    	
    	// Check nearby enemies as well
    	for (Unit.UnitView enemy: getNearbyEnemies(unit, isFootman ? archers : footmen)) {
    		unitActions.add(Action.createPrimitiveAttack(unit.getID(), enemy.getID()));
    	}
    	
    	return unitActions;
    }
    
    /**
     * Tests to see if movement is legal based on whether the space is occupied or not
     * 
     * @param unit The unit to be moved
     * @param dir The direction of movement
     * @return true if the move is valid
     */
    private boolean isValidMove(Unit.UnitView unit, Direction dir) {
    	
    	int newX = unit.getXPosition() + dir.xComponent();
    	int newY = unit.getYPosition() + dir.yComponent();
    	
    	// Not sure if the latter two tests will throw an error because out of bounds
    	boolean isInBounds = state.inBounds(newX, newY);
    	boolean hasResource = state.isResourceAt(newX, newY);
    	boolean hasUnit = state.isUnitAt(newX, newY);
    	
    	// Return true if and only if the new space is in bounds and empty
    	return isInBounds && !hasResource && !hasUnit;
    }
    
    /**
     * Returns a list of enemies that are within attack range
     * 
     * @param unit the unit who is looking for something to attack
     * @param enemies the list of potential threats within range
     * @return the finalized list of enemies in attack range
     */
    private List<Unit.UnitView> getNearbyEnemies(Unit.UnitView unit, List<Unit.UnitView> enemies) {
    	
    	// Holds the list of enemies within attack range
    	List<Unit.UnitView> nearbyEnemies = new LinkedList<Unit.UnitView>();
    	
    	// Stores the attack range of our unit
    	int attackRange = unit.getTemplateView().getRange();
    	
    	// Run through the list of enemies
    	for (Unit.UnitView enemy: enemies) {
    		int changeInX = Math.abs(unit.getXPosition() - enemy.getXPosition());
    		int changeInY = Math.abs(unit.getYPosition() - enemy.getYPosition());
    		
    		// If attack range is within the sum of the change in x and change in y
    		if (attackRange >= changeInX + changeInY) {
    			nearbyEnemies.add(enemy);
    		}
    	}
    	
    	return nearbyEnemies;
    }
}

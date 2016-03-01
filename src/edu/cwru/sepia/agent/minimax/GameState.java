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
		Integer[] playerNumbers = state.getPlayerNumbers();
		
		// Generates a list of footmen and a list of archers based on an array of player numbers
		footmen = state.getUnits(playerNumbers[0]);
		archers = state.getUnits(playerNumbers[1]);
		
		// Track the size of the grid
		xMax = state.getXExtent();
		yMax = state.getYExtent();
		
		// Track the state of the obstacles
		obstacleIDs = state.getAllResourceIds();
    	
    	System.out.println(String.format("Game contains %d footmen and %d archers", footmen.size(), archers.size()));
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
        return footmen.get(0).getHP() * .5 + footmen.get(1).getHP() * .5;
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
    	
    	// Convenient variables for unit
    	int x = unit.getXPosition();
    	int y = unit.getYPosition();
    	
    	// Check each direction for this unit
    	for (Direction direction: Direction.values()) {
    		
    		// If this unit is in bounds and the movement is not blocked
    		if (isInBounds(x, y, direction)) {
    			
    		}
    	}
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
    
    private boolean isBlocked(int x, int y, Direction dir) {
    	
    	
    	return true;
    }
}

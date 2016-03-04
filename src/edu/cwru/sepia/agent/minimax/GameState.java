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
	
	private static final double FOOTMAN_HP_WEIGHT = .3;
	private static final double ARCHER_HP_WEIGHT = .7;
	private static final double DISTANCE_WEIGHT = 1;
	private static final double ACTIONS_WEIGHT = 1;
	private static final double MAX_ACTIONS = 25;
	
	private State.StateView state;
	private List<UnitSimulation> footmen;
	private List<UnitSimulation> archers;
	private List<Integer> obstacleIDs;
	private int xMax;
	private int yMax;
	private int turnNumber;
	private boolean isFootmenTurn;
	
	private Double utility;
	private int sameSpaceTurnCount;
	private int numActions;
	
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
		footmen = new LinkedList<UnitSimulation>();
		archers = new LinkedList<UnitSimulation>();
		
		// Generates the lists of archers and footmen based on unit type
		for (Unit.UnitView unit: state.getAllUnits()) {
			String unitType = unit.getTemplateView().getName().toLowerCase();
			
			if (unitType.equals("footman")) {
				footmen.add(new UnitSimulation(unit));
			}
			else if (unitType.equals("archer")) {
				archers.add(new UnitSimulation(unit));
			}
		}
		
		// Track the size of the grid
		xMax = state.getXExtent();
		yMax = state.getYExtent();
		
		// Get turn
		turnNumber = state.getTurnNumber();
		
		// Track the state of the obstacles
		obstacleIDs = state.getAllResourceIds();
		
		// If all else fails, have a direct reference to the state
		this.state = state;
		
		// This will start out as true as a footman will first call this
		isFootmenTurn = true;
		utility = null;
		sameSpaceTurnCount = 0;
    }
    
    /**
     * Constructor override to clone game state easily
     * 
     * Deep clones the units
     * @param originalState
     */
    public GameState (GameState originalState){
    	footmen = new LinkedList<UnitSimulation>();
    	for (int i = 0; i<originalState.footmen.size(); i++){
    		footmen.add(new UnitSimulation(originalState.footmen.get(i)));
    	}
    	archers = new LinkedList<UnitSimulation>();
    	for (int i = 0; i<originalState.archers.size(); i++){
    		archers.add(new UnitSimulation(originalState.archers.get(i)));
    	}
    	obstacleIDs = originalState.obstacleIDs; //this value isn't getting changed, no need to clone
    	isFootmenTurn = !isFootmenTurn;
    	state = originalState.state;
    	utility = null;
    	sameSpaceTurnCount = originalState.sameSpaceTurnCount;
    }
    
    public int getTurnNumber() {
    	return turnNumber;
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
    	
    	if (this.utility != null){
    		return this.utility;
    	}
    	
    	/**
    	 * Our utility function calculates the average shortest path (straight line)
    	 * to an archer. The smallest average path for any set of footmen yields the highest 
    	 * utility
    	 */
    	
    	// The total shortest path is averaged, inverted, and normalized (based on largest possible straight line path) 
    	// such that shortest paths yield higher utilities
    	// TODO: Figure out how to normalize this
    	Double utility = distanceUtility() + numberOfActionsUtility();
    	this.utility = utility;
        return utility;
    }

    //TODO might want to use simpler distance formula to save time
	private double distance(UnitSimulation footman, UnitSimulation archer) {
		double a = footman.getXPosition() - archer.getXPosition();
		double b = footman.getYPosition() - archer.getYPosition();
		double aSquared = Math.pow(a, 2);
		double bSquared = Math.pow(b, 2);
		return Math.sqrt(aSquared + bSquared);
	}
	
	/**
	 * higher this value, the better
	 * @param footman
	 * @return
	 */
	private double footmanHPUtility(UnitSimulation footman){
		return FOOTMAN_HP_WEIGHT*(double)footman.getCurrentHP()/footman.getMaxHP();
	}
	
	/**
	 * lower this value, the better
	 * @param archer
	 * @return
	 */
	private double archerHPUtility(UnitSimulation archer){
		return ARCHER_HP_WEIGHT*(double)archer.getCurrentHP()/archer.getMaxHP();
	}
	
	private double numberOfActionsUtility() {
		int actions = getUnitActions(footmen.get(0)).size() * getUnitActions(footmen.get(1)).size();
		double temp = ACTIONS_WEIGHT * actions/MAX_ACTIONS;
		System.out.println("Action Utility: " + temp);
		return temp;
	}

	private double distanceUtility() {
		// Initialize the current shortest path to zero
    	Double totalShortestPath = 0.0;
    	
    	// For each footman in the game
    	for (UnitSimulation footman: footmen) {
    		
    		// Initialize the current shortest path to infinity
    		Double footmanShortestPath = Double.POSITIVE_INFINITY;
    		
    		// For each archer in the game
    		for (UnitSimulation archer: archers) {
    		
    			// Compute the distance between the footman and the archer - c = sqrt(a^2 + b^2)
    			double c = distance(footman, archer);
    			//TODO incorporate footman hp and archer hp
    			
    			
    			// Store the shortest path
    			if (c < footmanShortestPath) {
    				footmanShortestPath = c;
    			}
    		}
    		
    		totalShortestPath += footmanShortestPath;
    	}
    	return DISTANCE_WEIGHT * (1 / (totalShortestPath/footmen.size()));
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
    	List<Action> unitAction1;
    	List<Action> unitAction2;
    	
    	List<UnitSimulation> units = getUnitsByTurn();
    	
    	// All possible actions depending on the units' turn
    	unitAction1 = getUnitActions(units.get(0));
    	unitAction2 = getUnitActionsIfAlive(units);
    	
    	if (unitAction2.isEmpty()){
    		for (Action action : unitAction1){
				Map<Integer, Action> unitActionsMap = new HashMap<Integer, Action>();
				unitActionsMap.put(units.get(0).getID(), action);
    			addNextStateToChildren(allActionsAndState, unitActionsMap);
    		}
    	}
    	else{
    		for (Action action1 : unitAction1){
    			for (Action action2 : unitAction2){
    				Map<Integer, Action> unitActionsMap = new HashMap<Integer, Action>();
    				unitActionsMap.put(units.get(0).getID(), action1);
    				unitActionsMap.put(units.get(1).getID(), action2);
    				addNextStateToChildren(allActionsAndState, unitActionsMap);
    			}
    		}
    	}
    	numActions = allActionsAndState.size();
        return allActionsAndState;
    }

    private List<UnitSimulation> getUnitsByTurn(){
    	if (isFootmenTurn){
    		return footmen;
    	}
    	else{
    		return archers;
    	}
    }
    
	private void addNextStateToChildren(List<GameStateChild> allActionsAndState, Map<Integer, Action> unitActionsMap) {
		GameState nextState = new GameState(this);
		nextState.calculateNextState(unitActionsMap);
		GameStateChild child = new GameStateChild(unitActionsMap, nextState);
		
		allActionsAndState.add(child);
	}
	
	private void calculateNextState(Map<Integer, Action> unitActionsMap){
		for (Integer unitID : unitActionsMap.keySet()){
			Action action = unitActionsMap.get(unitID);
			UnitSimulation unit;
			switch(action.getType()){
			case PRIMITIVEATTACK:
				unit = getUnitSimulationByID(unitID);
				TargetedAction attack = (TargetedAction)action;
				UnitSimulation targetUnit = getUnitSimulationByID(attack.getTargetId());
				int damageDealt = unit.expectedDamageCalculation(targetUnit.getArmor());
				targetUnit.decrementHP(damageDealt);
				break;
			case PRIMITIVEMOVE:
				unit = getUnitSimulationByID(unitID);
				int x = ((DirectedAction)action).getDirection().xComponent();
				int y = ((DirectedAction)action).getDirection().yComponent();
				unit.moveXBy(x);
				unit.moveYBy(y);
				break;
			default:
				System.out.println(String.format("Illegal unit action detected: %s", action.getType()));
				break;
			
			}
		}
	}
	
	/**
	 * determines if the next state results in doing nothing
	 * @param nextState
	 * @return
	 */
	private boolean isSameSpace (GameStateChild nextState){
		//if next state's actions include attack, return false
		for (Action action : nextState.action.values()){
			if (action.getType() == ActionType.PRIMITIVEATTACK){
				return true;
			}
		}
		
		// check if footmen are on the same space
		for (UnitSimulation unit: nextState.state.footmen){
			
		}
		return false;
		
	}
	
	/**
	 * finds unit simulation by id
	 * has to manually go through and match because list index does not necessarily equal unitID
	 * @param unitID
	 * @return
	 */
	private UnitSimulation getUnitSimulationByID(int unitID){
		if (unitID<2){//isFootman
			for (UnitSimulation unit : footmen){
				if (unit.getID()==unitID){
					return unit;
				}
			}
		}
		else{
			for (UnitSimulation unit : archers){
				if (unit.getID()==unitID){
					return unit;
				}
			}
		}
		return null;
	}
    
    // TODO: write a method that generates all combinations of actions
    // This has to be recursive because we can't dynamically write nested loops
    
    /**
     * Produces a set of unit actions 
     * 
     * @param unit the unit for which the actions will be generated
     * @return 
     */
    private List<Action> getUnitActions(UnitSimulation unit) {
    	
    	// The list of action maps
    	List<Action> unitActions = new LinkedList<Action>();
    	
    	// Check each direction for this unit
    	Direction[] legalDirections = new Direction[]{Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
    	for (Direction direction: legalDirections) {// Direction.values include diagonal movements 
    		
    		// If this move is valid, add 
    		if (isValidMove(unit, direction)) {
    			unitActions.add(Action.createPrimitiveMove(unit.getID(), direction));
    		}
    	}
    	
    	//TODO does this allow for move and attack at the same time?
    	
    	// Determines if we should attack footmen or archers
    	boolean isFootman = unit.getName().equals("footman");
    	
    	// Check nearby enemies as well
    	for (UnitSimulation enemy: getNearbyEnemies(unit, isFootman ? archers : footmen)) {
    		unitActions.add(Action.createPrimitiveAttack(unit.getID(), enemy.getID()));
    	}
    	
    	return unitActions;
    }
    

    /**
     * helper method to see if the second unit is alive or not
     * @param units
     * @return
     */
	private List<Action> getUnitActionsIfAlive(List<UnitSimulation> units) {
		if (units.size()==2){
			return getUnitActions(units.get(1));
		}
		return new LinkedList<Action>();
	}
	
    /**
     * Tests to see if movement is legal based on whether the space is occupied or not
     * 
     * @param unit The unit to be moved
     * @param dir The direction of movement
     * @return true if the move is valid
     */
    private boolean isValidMove(UnitSimulation unit, Direction dir) {
    	
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
    private List<UnitSimulation> getNearbyEnemies(UnitSimulation unit, List<UnitSimulation> enemies) {
    	
    	// Holds the list of enemies within attack range
    	List<UnitSimulation> nearbyEnemies = new LinkedList<UnitSimulation>();
    	
    	// Stores the attack range of our unit
    	int attackRange = unit.getRange();
    	
    	// Run through the list of enemies
    	for (UnitSimulation enemy: enemies) {
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

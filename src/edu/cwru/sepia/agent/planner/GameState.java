package edu.cwru.sepia.agent.planner;

import edu.cwru.sepia.environment.model.state.ResourceNode.ResourceView;
import edu.cwru.sepia.agent.planner.actions.StripsAction;
import edu.cwru.sepia.environment.model.state.State;
import edu.cwru.sepia.environment.model.state.Unit.UnitView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is used to represent the state of the game after applying one of the available actions. It will also
 * track the A* specific information such as the parent pointer and the cost and heuristic function. Remember that
 * unlike the path planning A* from the first assignment the cost of an action may be more than 1. Specifically the cost
 * of executing a compound action such as move can be more than 1. You will need to account for this in your heuristic
 * and your cost function.
 *
 * The first instance is constructed from the StateView object (like in PA2). Implement the methods provided and
 * add any other methods and member variables you need.
 *
 * Some useful API calls for the state view are
 *
 * state.getXExtent() and state.getYExtent() to get the map size
 *
 * I recommend storing the actions that generated the instance of the GameState in this class using whatever
 * class/structure you use to represent actions.
 */
public class GameState implements Comparable<GameState> {


	private int playerNum;
	private int requiredGold;
	private int requiredWood;
	private boolean buildPeasants;
	
	private int mapSizeX;
	private int mapSizeY;
	
	private Map<Integer, PeasantSimulation> peasantMap;
	private UnitView townHall;
	private Map<Integer, ResourceSimulation> resourceMap;
	
	
	private GameState parent;
	/** The action used to get to this state*/
	private StripsAction action;
	
    /**
     * Construct a GameState from a stateview object. This is used to construct the initial search node. All other
     * nodes should be constructed from the another constructor you create or by factory functions that you create.
     *
     * @param state The current stateview at the time the plan is being created
     * @param playernum The player number of agent that is planning
     * @param requiredGold The goal amount of gold (e.g. 200 for the small scenario)
     * @param requiredWood The goal amount of wood (e.g. 200 for the small scenario)
     * @param buildPeasants True if the BuildPeasant action should be considered
     */
    public GameState(State.StateView state, int playernum, int requiredGold, int requiredWood, boolean buildPeasants) {
    	this.playerNum = playernum;
    	this.requiredGold = requiredGold;
    	this.requiredWood = requiredWood;
    	this.buildPeasants = buildPeasants;
    	
    	this.mapSizeX = state.getXExtent();
    	this.mapSizeY = state.getYExtent();
    	
    	peasantMap = new HashMap<Integer, PeasantSimulation> ();
    	
    			new PeasantSimulation(findUnit(state.getAllUnits(),"peasant"));
    	townHall = findUnit(state.getAllUnits(), "townhall");

    	for (UnitView unit : state.getAllUnits()){
    		switch(unit.getTemplateView().getName()){
    		case "townhall":
    			townHall = unit;
    			break;
    		case "peasant":
    			peasantMap.put(unit.getID(), new PeasantSimulation(unit));
    			break;
    		}
    	}
    	resourceMap = new HashMap<Integer, ResourceSimulation>();
    	
    	for (ResourceView res : state.getAllResourceNodes()){
    		resourceMap.put(res.getID(), new ResourceSimulation(res));
    	}
    	
    	parent = null;
    	action = null;
    }
    
    /**
     * Constructor for cloning a parent game state
     * @param parent
     */
    public GameState(GameState parent, StripsAction action){
    	this.playerNum = parent.playerNum;
    	this.requiredGold = parent.requiredGold;
    	this.requiredWood = parent.requiredWood;
    	this.buildPeasants = parent.buildPeasants;
    	this.mapSizeX = parent.mapSizeX;
    	this.mapSizeY = parent.mapSizeY;
    	this.peasantMap = new HashMap<Integer, PeasantSimulation>(parent.peasantMap);
    	this.townHall = parent.townHall;
    	this.resourceMap = new HashMap<Integer, ResourceSimulation>(parent.resourceMap);
    	this.parent = parent;
    	this.action = action;
    }

	/**
	 * @return the resourceMap
	 */
	public Map<Integer, ResourceSimulation> getResourceMap() {
		return resourceMap;
	}

	public Map<Integer, PeasantSimulation> getPeasantMap(){
    	return peasantMap;
    }

    /**
     * @param peasantMap the peasantMap to set
     */
    public void setPeasantMap(Map<Integer, PeasantSimulation> peasantMap) {
    	this.peasantMap = peasantMap;
    }
    
    public UnitView getTownHall(){
    	return townHall;
    }
    
	/**
	 * @return the playerNum
	 */
	public int getPlayerNum() {
		return playerNum;
	}

	/**
	 * @return the requiredGold
	 */
	public int getRequiredGold() {
		return requiredGold;
	}

	/**
	 * @return the requiredWood
	 */
	public int getRequiredWood() {
		return requiredWood;
	}

	/**
	 * @return the buildPeasants
	 */
	public boolean isBuildPeasants() {
		return buildPeasants;
	}

	/**
	 * @return the mapSizeX
	 */
	public int getMapSizeX() {
		return mapSizeX;
	}

	/**
	 * @return the mapSizeY
	 */
	public int getMapSizeY() {
		return mapSizeY;
	}

	/**
	 * @return the parent
	 */
	public GameState getParent() {
		return parent;
	}
	
    public void setParent(GameState parent) {
    	this.parent = parent;
    }

    /**
	 * @return the action
	 */
	public StripsAction getAction() {
		return action;
	}

	/**
     * Unlike in the first A* assignment there are many possible goal states. As long as the wood and gold requirements
     * are met the peasants can be at any location and the capacities of the resource locations can be anything. Use
     * this function to check if the goal conditions are met and return true if they are.
     *
     * @return true if the goal conditions are met in this instance of game state.
     */
    public boolean isGoal() {
    	return requiredGold <= 0 && requiredWood <= 0;
    }

    /**
     * The branching factor of this search graph are much higher than the planning. Generate all of the possible
     * successor states and their associated actions in this method.
     *
     * @return A list of the possible successor states and their associated actions
     */
    public List<GameState> generateChildren() {
        // TODO: Implement me!
        return null;
    }

    /**
     * Write your heuristic function here. Remember this must be admissible for the properties of A* to hold. If you
     * can come up with an easy way of computing a consistent heuristic that is even better, but not strictly necessary.
     *
     * Add a description here in your submission explaining your heuristic.
     *
     * @return The value estimated remaining cost to reach a goal state from this state.
     */
    public double heuristic() {
        // TODO: Implement me!
        return 0.0;
    }

    /**
     *
     * Write the function that computes the current cost to get to this node. This is combined with your heuristic to
     * determine which actions/states are better to explore.
     *
     * @return The current cost to reach this goal
     */
    public double getCost() {
        // TODO: Implement me!
        return 0.0;
    }

    /**
     * This is necessary to use your state in the Java priority queue. See the official priority queue and Comparable
     * interface documentation to learn how this function should work.
     *
     * @param o The other game state to compare
     * @return 1 if this state costs more than the other, 0 if equal, -1 otherwise
     */
    @Override
    public int compareTo(GameState o) {
        // TODO: Implement me!
        return 0;
    }

    /**
     * This will be necessary to use the GameState as a key in a Set or Map.
     *
     * @param o The game state to compare
     * @return True if this state equals the other state, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        // TODO: Implement me!
        return false;
    }

    /**
     * This is necessary to use the GameState as a key in a HashSet or HashMap. Remember that if two objects are
     * equal they should hash to the same value.
     *
     * @return An integer hashcode that is equal for equal states.
     */
    @Override
    public int hashCode() {
        // TODO: Implement me!
        return 0;
    }
    
    private UnitView findUnit(List<UnitView> allUnits, String unitName){
    	for(UnitView unit : allUnits){
    		if (unit.getTemplateView().getName().toLowerCase().equals(unitName)){
    			return unit;
    		}
    	}
    	return null;
    }

}

package edu.cwru.sepia.agent.planner;

import edu.cwru.sepia.environment.model.state.ResourceNode.ResourceView;
import edu.cwru.sepia.agent.planner.actions.BuildPeasantAction;
import edu.cwru.sepia.agent.planner.actions.DepositAction;
import edu.cwru.sepia.agent.planner.actions.HarvestAction;
import edu.cwru.sepia.agent.planner.actions.MoveAction;
import edu.cwru.sepia.agent.planner.actions.StripsAction;
import edu.cwru.sepia.environment.model.state.ResourceNode;
import edu.cwru.sepia.environment.model.state.ResourceType;
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
	
	private int currentGold;
	private int currentFood;
	private int supplyCap;
	
	private boolean buildPeasants;
	
	private int mapSizeX;
	private int mapSizeY;
	
	private Map<Integer, PeasantSimulation> peasantMap;
	private StructureSimulation townHall;
	private Map<Position, ResourceSimulation> resourceMap;
	
	
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

    	currentGold = state.getResourceAmount(playernum, ResourceType.GOLD);
    	currentFood = state.getSupplyAmount(playernum);
    	supplyCap = state.getSupplyCap(playernum);
    	
    	for (UnitView unit : state.getAllUnits()){
    		switch(unit.getTemplateView().getName().toLowerCase()){
    		case "townhall":
    			townHall = new StructureSimulation(unit);
    			break;
    		case "peasant":
    			peasantMap.put(unit.getID(), new PeasantSimulation(unit));
    			break;
    		}
    	}
    	resourceMap = new HashMap<Position, ResourceSimulation>();
    	
    	for (ResourceView res : state.getAllResourceNodes()){
    		resourceMap.put(new Position(res.getXPosition(), res.getYPosition()), new ResourceSimulation(res));
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
    	this.resourceMap = new HashMap<Position, ResourceSimulation>(parent.resourceMap);
    	this.parent = parent;
    	this.action = action;
    	
    	this.currentFood = parent.currentFood;
    	this.currentGold = parent.currentGold;
    	this.supplyCap = parent.supplyCap;
    }

	/**
	 * @return the resourceMap
	 */
	public Map<Position, ResourceSimulation> getResourceMap() {
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
    
    public StructureSimulation getTownHall(){
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
	 * @param requiredGold the requiredGold to set
	 */
	public void setRequiredGold(int requiredGold) {
		this.requiredGold = requiredGold;
	}
	
	/**
	 * @return the requiredWood
	 */
	public int getRequiredWood() {
		return requiredWood;
	}

	/**
	 * @param requiredWood the requiredWood to set
	 */
	public void setRequiredWood(int requiredWood) {
		this.requiredWood = requiredWood;
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
	 * @return the currentGold
	 */
	public int getCurrentGold() {
		return currentGold;
	}

	/**
	 * @param currentGold the currentGold to set
	 */
	public void setCurrentGold(int currentGold) {
		this.currentGold = currentGold;
	}

	/**
	 * @return the currentFood
	 */
	public int getCurrentFood() {
		return currentFood;
	}

	/**
	 * @param currentFood the currentFood to set
	 */
	public void setCurrentFood(int currentFood) {
		this.currentFood = currentFood;
	}

	/**
	 * @return the supplyCap
	 */
	public int getSupplyCap() {
		return supplyCap;
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
    	List<GameState> children = new ArrayList<GameState>();
    	StripsAction action;
    	for (PeasantSimulation peasant : peasantMap.values()){
    		if (buildPeasants){
    			action = new BuildPeasantAction();
    			if (action.preconditionsMet(this)){
    				children.add(action.apply(this));
    			}
    			continue;
    		}
    		
    		action = new HarvestAction(findAdjacentResource(peasant));
    		if (action.preconditionsMet(this)){
    			children.add(action.apply(this));
    			continue;
    		}
    		action = new DepositAction();
    		if (action.preconditionsMet(this)){
    			children.add(action.apply(this));
    			continue;
    		}
    		//movement actions
    		if (peasant.isCarrying()){
    			//go drop off
    			action = new MoveAction(townHall.getPosition());
    			if (action.preconditionsMet(this)){
    				children.add(action.apply(this));
    			}
    		}
    		else{
    			//harvest
    			//addOnlyClosestResourcesChildren(children, peasant);
    			addAllResourcesChildren(children);
    		}
    	}
        return children;
    }

	private void addOnlyClosestResourcesChildren(List<GameState> children, PeasantSimulation peasant) {
		StripsAction action;
		int minDistance = Integer.MAX_VALUE;
		ResourceSimulation closestRes = null;
		for (ResourceSimulation resource : resourceMap.values()){
			if (resourceRequirementMet(resource.getResourceType())){
				continue;
			}
			int distance = peasant.getPosition().chebyshevDistance(resource.getPosition());
			if (distance<minDistance){
				closestRes = resource;
			}
		}
		//action = new MoveAction (peasant, resource.getPosition());
		action = new MoveAction (closestRes.getPosition());
		if (action.preconditionsMet(this)){
			children.add(action.apply(this));
		}
	}
	
	private void addAllResourcesChildren(List<GameState> children){
		StripsAction action;
		for (ResourceSimulation resource : resourceMap.values()){
			if (resourceRequirementMet(resource.getResourceType()) || isResourceDepleted(resource)){
				continue;
			}
			action = new MoveAction(resource.getPosition());
			if (action.preconditionsMet(this)){
				children.add(action.apply(this));
			}
		}
	}
    
    /**
     * finds adjacent resources
     * if it exists return
     * else return null
     * @param peasant
     * @return
     */
    private ResourceSimulation findAdjacentResource(PeasantSimulation peasant){
    	for (Position pos : peasant.getPosition().getAdjacentPositions()){
    		ResourceSimulation resource = resourceMap.get(pos);
    		if (resource != null){
    			return resource;
    		}
    	}
    	return null;
    }
    
    /**
     * determines if the gamestate has enough of a certain resource and shouldn't plan for any more of these
     * @param type
     * @return
     */
    public boolean resourceRequirementMet(ResourceNode.Type type){
    	switch(type){
    	case GOLD_MINE:
    		if (requiredGold<=0){
    			return true;
    		}
    		break;
		case TREE:
			if (requiredWood<=0){
				return true;
			}
			break;
    	}
    	return false;
    }
    
    private boolean isResourceDepleted(ResourceSimulation resource){
    	return resource.getResourceRemaining()<=0;
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
    	
        // TODO: Implement me! there are more factors than just gold and wood
    	double resourceRemaining = requiredGold+requiredWood;
    	double carryingCount = 0;
    	double distanceFromTownHall = 0;
    	double peasantCount = 0;
    	for(PeasantSimulation peasant : peasantMap.values()){
    		carryingCount += peasant.getCargo();
    		distanceFromTownHall += peasant.getPosition().chebyshevDistance(townHall.getPosition());
    		peasantCount++;
    	}
        return resourceRemaining - carryingCount + distanceFromTownHall/peasantCount - peasantCount*800;//TODO get better heuristic
    }

    /**
     *
     * Write the function that computes the current cost to get to this node. This is combined with your heuristic to
     * determine which actions/states are better to explore.
     *
     * @return The current cost to reach this goal
     */
    public double getCost() {
//    	GameState trav = parent;
//    	if (action == null){
//    		return 0;
//    	}
//    	int costSum = action.cost();
//    	while (trav != null){
//    		if (trav.action != null){
//    			costSum += trav.action.cost();
//    		}
//    		trav = trav.parent;
//    	}
//        return costSum;
    	if (action == null){
    		return 0;
    	}
    	return action.cost();
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
    	if (this.getCost()+this.heuristic()>o.getCost()+o.heuristic()){
    		return 1;
    	}
    	else if (this.getCost()+this.heuristic()<o.getCost()+o.heuristic()){
    		return -1;
    	}
        return 0;
    }

    /* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GameState other = (GameState) obj;
		if (buildPeasants != other.buildPeasants)
			return false;
		if (currentFood != other.currentFood)
			return false;
		if (currentGold != other.currentGold)
			return false;
		if (mapSizeX != other.mapSizeX)
			return false;
		if (mapSizeY != other.mapSizeY)
			return false;
		if (parent == null) {
			if (other.parent != null)
				return false;
		} else if (!parent.equals(other.parent))
			return false;
		if (peasantMap == null) {
			if (other.peasantMap != null)
				return false;
		} else if (!peasantMap.equals(other.peasantMap))
			return false;
		if (playerNum != other.playerNum)
			return false;
		if (requiredGold != other.requiredGold)
			return false;
		if (requiredWood != other.requiredWood)
			return false;
		if (resourceMap == null) {
			if (other.resourceMap != null)
				return false;
		} else if (!resourceMap.equals(other.resourceMap))
			return false;
		if (supplyCap != other.supplyCap)
			return false;
		if (townHall == null) {
			if (other.townHall != null)
				return false;
		} else if (!townHall.equals(other.townHall))
			return false;
		return true;
	}

    /* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "GameState [action=" + action +  ", requiredGold=" + requiredGold + ", requiredWood=" + requiredWood+ ", heuristic="+heuristic() + ", peasantMap=" + peasantMap
				 + ", resourceMap=" + resourceMap + ", parent="
				+ parent +  "]";
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (buildPeasants ? 1231 : 1237);
		result = prime * result + currentFood;
		result = prime * result + currentGold;
		result = prime * result + mapSizeX;
		result = prime * result + mapSizeY;
		result = prime * result + ((parent == null) ? 0 : parent.hashCode());
		result = prime * result + ((peasantMap == null) ? 0 : peasantMap.hashCode());
		result = prime * result + playerNum;
		result = prime * result + requiredGold;
		result = prime * result + requiredWood;
		result = prime * result + ((resourceMap == null) ? 0 : resourceMap.hashCode());
		result = prime * result + supplyCap;
		result = prime * result + ((townHall == null) ? 0 : townHall.hashCode());
		return result;
	}

}

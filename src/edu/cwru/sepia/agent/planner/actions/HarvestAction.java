package edu.cwru.sepia.agent.planner.actions;

import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.PeasantSimulation;
import edu.cwru.sepia.agent.planner.ResourceSimulation;
import edu.cwru.sepia.environment.model.state.ResourceNode;
import edu.cwru.sepia.environment.model.state.ResourceType;

public class HarvestAction implements StripsAction{

	private PeasantSimulation peasant;
	private ResourceSimulation resource;
	
	private static final int PEASANT_GOLD_HARVEST_DURATION = 400;
	private static final int PEASANT_WOOD_HARVEST_DURATION = 1000;
	
	public HarvestAction(PeasantSimulation peasant, ResourceSimulation resource) {
		this.peasant = peasant;
		this.resource = resource;
	}
	
	@Override
	public boolean preconditionsMet(GameState state) {
		if (resource == null){
			return false;
		}
		boolean hasEnough = state.hasEnough(resource.getResourceType());
		return peasant.getPosition().isAdjacent(resource.getPosition()) 
				&& !peasant.isCarrying() 
				&& resource.getResourceRemaining()>0
				&& !hasEnough;
	}

	@Override
	public GameState apply(GameState state) {
		GameState nextGameState = new GameState(state, this);

		ResourceType resType = resourceNodeTypeToResourceType(resource.getResourceType());
		int cargoAmount = (resource.getResourceRemaining()>=100) ? 100 : resource.getResourceRemaining();
		
		PeasantSimulation peasantClone = new PeasantSimulation(peasant.getPosition(), cargoAmount, resType, peasant.getUnitId());
		ResourceSimulation resourceClone = new ResourceSimulation(resource.getPosition(), resource.getResourceRemaining()-cargoAmount, resource.getResourceType());
		nextGameState.getPeasantMap().put(peasantClone.getUnitId(), peasantClone);
		nextGameState.getResourceMap().put(resourceClone.getPosition(), resourceClone);
		return nextGameState;
	}
	
	private ResourceType resourceNodeTypeToResourceType(ResourceNode.Type type){
		switch(type){
		case GOLD_MINE:
			return ResourceType.GOLD;
		case TREE:
			return ResourceType.WOOD;
		}
		return null;
	}

	/**
	 * harvest costs 1 turn
	 */
	@Override
	public int cost() {
		return 1;
	}

	/**
	 * @return the peasant
	 */
	public PeasantSimulation getPeasant() {
		return peasant;
	}

	/**
	 * @return the resource
	 */
	public ResourceSimulation getResource() {
		return resource;
	}

}

package edu.cwru.sepia.agent.planner.actions;

import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.PeasantSimulation;
import edu.cwru.sepia.agent.planner.Position;

public class BuildPeasantAction implements StripsAction{

	private static final int PEASANT_GOLD_COST = 400;
	private static final int PEASANT_BUILD_TIME = 225;
	private static final int PEASANT_FOOD_CONSUMPTION = 1;
	private int cost = -1000;
	
	@Override
	public boolean preconditionsMet(GameState state) {
		int currentGold = state.getCurrentGold();
		int currentFood = state.getCurrentFood();
		int supplyCap = state.getSupplyCap();
		boolean isItSaneToBuildPeasant = state.getGoldLeftInMap()+currentGold >= PEASANT_GOLD_COST + state.getRequiredGold();
		
		if (currentFood + PEASANT_FOOD_CONSUMPTION > state.getOptimalPeasantCount()){
			cost = 1000;
		}
		return currentGold>=PEASANT_GOLD_COST 
				&& currentFood + PEASANT_FOOD_CONSUMPTION <= supplyCap 
				&& isItSaneToBuildPeasant;
	}

	@Override
	public GameState apply(GameState state) {
		GameState nextGameState = new GameState(state, this);
		
		int currentFood = nextGameState.getCurrentFood();
		currentFood+= PEASANT_FOOD_CONSUMPTION;
		//TODO actually get townhall's spawn point
		Position peasantSpawnPoint = new Position(nextGameState.getTownHall().getPosition().getAdjacentPositions().get((int)(8*Math.random())));
		PeasantSimulation babyPeasant = new PeasantSimulation(peasantSpawnPoint, 0, null, currentFood);//TODO no clue about the peasant id either lol
		
		nextGameState.getPeasantMap().put(babyPeasant.getUnitId(), babyPeasant);
		nextGameState.setRequiredGold(nextGameState.getRequiredGold() + PEASANT_GOLD_COST);
		nextGameState.setCurrentGold(nextGameState.getCurrentGold() - PEASANT_GOLD_COST);
		nextGameState.setCurrentFood(currentFood);

		return nextGameState;
	}

	@Override
	public int cost() {
		// TODO Auto-generated method stub
		return cost;
	}

}

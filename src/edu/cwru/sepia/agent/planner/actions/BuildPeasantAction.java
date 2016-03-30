package edu.cwru.sepia.agent.planner.actions;

import edu.cwru.sepia.agent.planner.GameState;

public class BuildPeasantAction implements StripsAction{

	private int currentGold;
	private int currentFood;
	private int supplyCap;
	private static final int PEASANT_GOLD_COST = 400;
	private static final int PEASANT_BUILD_TIME = 225;
	private static final int PEASANT_FOOD_CONSUMPTION = 1;
	
	public BuildPeasantAction(GameState state){
		this.currentGold = state.getCurrentGold();
		this.currentFood = state.getCurrentFood();
		this.supplyCap = state.getSupplyCap();
	}
	
	@Override
	public boolean preconditionsMet(GameState state) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public GameState apply(GameState state) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int cost() {
		// TODO Auto-generated method stub
		return 0;
	}

}

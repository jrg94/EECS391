package edu.cwru.sepia.agent.planner.actions;

import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.PeasantSimulation;
import edu.cwru.sepia.agent.planner.Position;

public class MoveAction implements StripsAction{

	private PeasantSimulation peasant;
	private Position destinationPosition;
	
	private static final int PEASANT_MOVE_DURATION = 16;
	
	public MoveAction(PeasantSimulation peasant, Position destinationPosition){
		this.peasant = peasant;
		this.destinationPosition = destinationPosition;
	}
	
	@Override
	public boolean preconditionsMet(GameState state) {
		
		return !peasant.getPosition().equals(destinationPosition);
		//TODO add another condition that checks if there's already a peasant in the way
	}

	@Override
	public GameState apply(GameState state) {
		GameState nextGameState = new GameState(state, this);
		PeasantSimulation peasantClone = new PeasantSimulation(destinationPosition, peasant.getCargo(), peasant.getCargoType(), peasant.getUnitId());
		nextGameState.getPeasantMap().put(peasantClone.getUnitId(), peasantClone);
		return nextGameState;
	}

	/**
	 * cost of moving is more than 1
	 */
	@Override
	public int cost() {
		return peasant.getPosition().chebyshevDistance(destinationPosition);
	}

	/**
	 * @return the peasant
	 */
	public PeasantSimulation getPeasant() {
		return peasant;
	}

	/**
	 * @return the destinationPosition
	 */
	public Position getDestinationPosition() {
		return destinationPosition;
	}

}

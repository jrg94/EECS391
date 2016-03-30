package edu.cwru.sepia.agent.planner.actions;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.PeasantSimulation;
import edu.cwru.sepia.agent.planner.Position;

public class MoveAction implements StripsAction{

	private Position destinationPosition;
	private Map<Integer, Position> destinationPositionMap;
	private int cost;
	
	private static final int PEASANT_MOVE_DURATION = 16;
	
	public MoveAction(Position destinationPosition){
		this.destinationPosition = destinationPosition;
		destinationPositionMap = new HashMap<Integer, Position>();
		cost = 0;
	}
	
	@Override
	public boolean preconditionsMet(GameState state) {
		boolean condition = true;
		for (PeasantSimulation peasant : state.getPeasantMap().values()){
			condition &= !peasant.getPosition().equals(destinationPosition);
		}
		return condition;
		//TODO add another condition that checks if there's already a peasant in the way
	}

	@Override
	public GameState apply(GameState state) {
		GameState nextGameState = new GameState(state, this);

		/**
		 * where N is the size of adjacents
		 * M is the size of peasants
		 * Sorting approach takes NlogN + M time compared to 
		 * trying to find the closest adjacent position for each peasant is N*M
		 * 
		 * For the case of peasantSize=3, adjacents=8 -> N=8, M=3
		 * 8log8 + 3 = 8*3 + 3 = 27
		 * 8*3 = 24
		 *
		 * Even though sort is slower (very slightly), it stops peasants from getting on top of each other by finding the 2nd best, 3rd best, ...
		 */
		List<Position> closestPositions = sortClosestPositions(state);
		
		
		int i = 0;
		for (PeasantSimulation peasant : state.getPeasantMap().values()){
			Position gatherPosition = new Position(closestPositions.get(i));
			PeasantSimulation peasantClone = new PeasantSimulation(gatherPosition, peasant.getCargo(), peasant.getCargoType(), peasant.getUnitId());
			nextGameState.getPeasantMap().put(peasantClone.getUnitId(), peasantClone);
			i++;
			
			cost += peasant.getPosition().chebyshevDistance(gatherPosition);
		}
		return nextGameState;
	}

	private List<Position> sortClosestPositions(GameState state) {
		List<Position> closestPositions = destinationPosition.getAdjacentPositions();
		closestPositions.sort(new Comparator<Position>(){
			//this sorts the list from smallest distance to greatest
			@Override
			public int compare(Position arg0, Position arg1) {
				return state.getTownHall().getPosition().chebyshevDistance(arg0) - state.getTownHall().getPosition().chebyshevDistance(arg1);
			}
		});
		return closestPositions;
	}

	/**
	 * cost of moving is more than 1
	 * returns the average chebyshev distance between peasant and the destination
	 */
	@Override
	public int cost() {
		return cost/destinationPositionMap.size();
	}

	/**
	 * @return the destinationPosition
	 */
	public Position getDestinationPosition() {
		return destinationPosition;
	}

	/**
	 * @return the destinationPositionMap
	 */
	public Map<Integer, Position> getDestinationPositionMap() {
		return destinationPositionMap;
	}

    private Position findClosestAdjacent(Position pos, Position peasantPosition){
    	int min = Integer.MAX_VALUE;
    	Position minPos = null;
    	for (Position adjacent : pos.getAdjacentPositions()){
    		int distance = peasantPosition.chebyshevDistance(adjacent);
    		if (distance<min){
    			minPos = adjacent;
    			min = distance;
    		}
    	}
    	return minPos;
    }
}

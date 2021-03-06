package edu.cwru.sepia.agent.planner;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.action.ActionResult;
import edu.cwru.sepia.action.LocatedAction;
import edu.cwru.sepia.agent.Agent;
import edu.cwru.sepia.agent.planner.actions.*;
import edu.cwru.sepia.environment.model.history.History;
import edu.cwru.sepia.environment.model.state.ResourceNode;
import edu.cwru.sepia.environment.model.state.ResourceNode.ResourceView;
import edu.cwru.sepia.environment.model.state.ResourceType;
import edu.cwru.sepia.environment.model.state.State;
import edu.cwru.sepia.environment.model.state.Template;
import edu.cwru.sepia.environment.model.state.Unit;
import edu.cwru.sepia.environment.model.state.Unit.UnitView;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import com.sun.org.apache.xerces.internal.dom.DeferredCDATASectionImpl;

/**
 * This is an outline of the PEAgent. Implement the provided methods. You may add your own methods and members.
 */
public class PEAgent extends Agent {

	// The plan being executed
	private Stack<StripsAction> plan = null;

	// maps the real unit Ids to the plan's unit ids
	// when you're planning you won't know the true unit IDs that sepia assigns. So you'll use placeholders (1, 2, 3).
	// this maps those placeholders to the actual unit IDs.
	private Map<Integer, Integer> peasantIdMap;
	private int townhallId;
	private Position townhallPosition;
	private int peasantTemplateId;

	public PEAgent(int playernum, Stack<StripsAction> plan) {
		super(playernum);
		peasantIdMap = new HashMap<Integer, Integer>();
		this.plan = plan;

	}

	@Override
	public Map<Integer, Action> initialStep(State.StateView stateView, History.HistoryView historyView) {
		// gets the townhall ID and the peasant ID
		for(int unitId : stateView.getUnitIds(playernum)) {
			Unit.UnitView unit = stateView.getUnit(unitId);
			String unitType = unit.getTemplateView().getName().toLowerCase();
			if(unitType.equals("townhall")) {
				townhallId = unitId;
				townhallPosition = new Position(unit.getXPosition(), unit.getYPosition());
			} else if(unitType.equals("peasant")) {
				peasantIdMap.put(unitId, unitId);
			}
		}

		// Gets the peasant template ID. This is used when building a new peasant with the townhall
		for(Template.TemplateView templateView : stateView.getTemplates(playernum)) {
			if(templateView.getName().toLowerCase().equals("peasant")) {
				peasantTemplateId = templateView.getID();
				break;
			}
		}

		return middleStep(stateView, historyView);
	}

	/**
	 * This is where you will read the provided plan and execute it. If your plan is correct then when the plan is empty
	 * the scenario should end with a victory. If the scenario keeps running after you run out of actions to execute
	 * then either your plan is incorrect or your execution of the plan has a bug.
	 *
	 * You can create a SEPIA deposit action with the following method
	 * Action.createPrimitiveDeposit(int peasantId, Direction townhallDirection)
	 *
	 * You can create a SEPIA harvest action with the following method
	 * Action.createPrimitiveGather(int peasantId, Direction resourceDirection)
	 *
	 * You can create a SEPIA build action with the following method
	 * Action.createPrimitiveProduction(int townhallId, int peasantTemplateId)
	 *
	 * You can create a SEPIA move action with the following method
	 * Action.createCompoundMove(int peasantId, int x, int y)
	 *
	 * these actions are stored in a mapping between the peasant unit ID executing the action and the action you created.
	 *
	 * For the compound actions you will need to check their progress and wait until they are complete before issuing
	 * another action for that unit. If you issue an action before the compound action is complete then the peasant
	 * will stop what it was doing and begin executing the new action.
	 *
	 * To check an action's progress you can call getCurrentDurativeAction on each UnitView. If the Action is null nothing
	 * is being executed. If the action is not null then you should also call getCurrentDurativeProgress. If the value is less than
	 * 1 then the action is still in progress.
	 *
	 * Also remember to check your plan's preconditions before executing!
	 */
	@Override
	public Map<Integer, Action> middleStep(State.StateView stateView, History.HistoryView historyView) {
		Map<Integer, Action> sepiaActions = new HashMap<Integer, Action>();
		Action action = null;
		if (stateView.getTurnNumber()!=0){
			Map<Integer, ActionResult> actionResults = historyView.getCommandFeedback(playernum, stateView.getTurnNumber()-1);
			for (ActionResult result : actionResults.values()) {
				switch(result.getFeedback()){
				case INCOMPLETE:
					action = result.getAction();
					sepiaActions.put(action.getUnitId(), action);
					break;
				case COMPLETED:
					System.out.println("Completed the action: " + result.getAction());
					if (result.getAction().getUnitId() == townhallId){ //this is a townhall finishing producing unit
						//refresh peasantIdMap
						updatePeasantIdMap(stateView);
					}
					break;
				case INCOMPLETEMAYBESTUCK:
					System.out.println("[PEAgent] may be stuck, unitID= "+action.getUnitId());
					break;
				case FAILED:
					System.out.println("[PEAgent] failed an action, with the action: " + result.getAction());
					action = result.getAction();
					sepiaActions.put(action.getUnitId(), action);
					break;
				default:
					System.out.println("[PEAgent] default case with the feedback: " + result.getFeedback().toString());
					break;
				}
			}
		}
		// only pop the plan when all are idle.
		if (action == null){
			StripsAction stripsAction = plan.pop(); //If I don't do this SEPIA will magically pop my stack twice...
			createSepiaActions(stripsAction, stateView, sepiaActions);
		}
		
		/**
		 * need 2d loop to go through to see if the move command is occupied
		 * We need to do this because Sepia doesn't give a FAILED feedback 
		 * when the last move of the compound move cannot be made
		 * because there is an unit occupying it
		 */
		for (Action sepiaAction : sepiaActions.values()){
			for (int id : peasantIdMap.values()){
				if (id == sepiaAction.getUnitId()){
					continue;
				}
				if (!(sepiaAction instanceof LocatedAction)){
					continue;
				}
				LocatedAction moveAction = (LocatedAction)sepiaAction;
				Position targetPosition = new Position (moveAction.getX(), moveAction.getY());
				
				Unit.UnitView otherUnit = stateView.getUnit(id);
				Position otherUnitPosition = new Position(otherUnit.getXPosition(), otherUnit.getYPosition());
				if (targetPosition.equals(otherUnitPosition)){
					//we are stuck
					
					//does it happen to resource nodes as well? or is it just townhall?
					Position townhallPosition = new Position (stateView.getUnit(townhallId).getXPosition(), stateView.getUnit(townhallId).getYPosition());
					Position destinationPosition = null;
					if (targetPosition.isAdjacent(townhallPosition)){
						destinationPosition = townhallPosition;
					}
					else{
						//it's not town hall
						for (ResourceView resource : stateView.getAllResourceNodes()){
							Position resourcePosition = new Position(resource.getXPosition(), resource.getYPosition());
							if (targetPosition.isAdjacent(resourcePosition)){
								destinationPosition = resourcePosition;
							}
						}
					}
					if (destinationPosition == null){
						System.out.println("replacement plan failed");
					}
					MoveAction replacementStripsAction = new MoveAction(destinationPosition);
					replacementStripsAction.populateDestinationList(destinationPosition, peasantIdMap.size());
					plan.push(replacementStripsAction);
					break;
				}
			}
		}
			
		return sepiaActions;
	}

	/**
	 * Returns a SEPIA version of the specified Strips Action.
	 * @param action StripsAction
	 * @return SEPIA representation of same action
	 */
	private void createSepiaActions(StripsAction action, State.StateView stateView, Map<Integer, Action> sepiaActions) {

		if (action instanceof DepositAction){
			for (int id : peasantIdMap.values()){
				UnitView peasant = stateView.getUnit(id);
				Position peasantPosition = new Position(peasant.getXPosition(), peasant.getYPosition());
				sepiaActions.put(id, Action.createPrimitiveDeposit(id, peasantPosition.getDirection(townhallPosition)));
			}
			return;
		}
		else if (action instanceof HarvestAction){
			HarvestAction harvestAction = ((HarvestAction)action);
			for (int id : peasantIdMap.values()){
				UnitView peasant = stateView.getUnit(id);
				Position peasantPosition = new Position(peasant.getXPosition(), peasant.getYPosition());
				sepiaActions.put(id, Action.createPrimitiveGather(id, peasantPosition.getDirection(harvestAction.getResource().getPosition())));
			}
			return;
		}
		else if (action instanceof MoveAction){
			MoveAction moveAction = ((MoveAction)action);
			int i =0;
			for (int id : peasantIdMap.values()){
				Position destinationPosition = moveAction.getDestinationList().get(i);
				sepiaActions.put(id, Action.createCompoundMove(id, destinationPosition.x, destinationPosition.y));
				i++;
			}
			return;
		}
		else if (action instanceof BuildPeasantAction){
			sepiaActions.put(townhallId, Action.createCompoundProduction(townhallId, peasantTemplateId));
			return;
		}
		System.out.println("[PEAgent] Invalid StripsAction was entered in createSepiaAction");
	}
	
	private void updatePeasantIdMap(State.StateView stateView){
		for(int unitId : stateView.getUnitIds(playernum)) {
			Unit.UnitView unit = stateView.getUnit(unitId);
			String unitType = unit.getTemplateView().getName().toLowerCase();
			if(unitType.equals("peasant")) {
				peasantIdMap.put(unitId, unitId);
			}
		}
	}

	@Override
	public void terminalStep(State.StateView stateView, History.HistoryView historyView) {

	}

	@Override
	public void savePlayerData(OutputStream outputStream) {

	}

	@Override
	public void loadPlayerData(InputStream inputStream) {

	}
}

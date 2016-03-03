package edu.cwru.sepia.agent.minimax;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.agent.Agent;
import edu.cwru.sepia.environment.model.history.History;
import edu.cwru.sepia.environment.model.state.State;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

public class MinimaxAlphaBeta extends Agent {

    private final int numPlys;

    public MinimaxAlphaBeta(int playernum, String[] args)
    {
        super(playernum);

        if(args.length < 1)
        {
            System.err.println("You must specify the number of plys");
            System.exit(1);
        }

        numPlys = Integer.parseInt(args[0]);
    }

    @Override
    public Map<Integer, Action> initialStep(State.StateView newstate, History.HistoryView statehistory) {
        return middleStep(newstate, statehistory);
    }

    @Override
    public Map<Integer, Action> middleStep(State.StateView newstate, History.HistoryView statehistory) {
        GameStateChild bestChild = alphaBetaSearch(new GameStateChild(newstate),
                numPlys,
                Double.NEGATIVE_INFINITY,
                Double.POSITIVE_INFINITY);

        return bestChild.action;
    }

    @Override
    public void terminalStep(State.StateView newstate, History.HistoryView statehistory) {

    }

    @Override
    public void savePlayerData(OutputStream os) {

    }

    @Override
    public void loadPlayerData(InputStream is) {

    }

    /**
     * You will implement this.
     *
     * This is the main entry point to the alpha beta search. Refer to the slides, assignment description
     * and book for more information.
     *
     * Try to keep the logic in this function as abstract as possible (i.e. move as much SEPIA specific
     * code into other functions and methods)
     *
     * @param node The action and state to search from
     * @param depth The remaining number of plys under this node
     * @param alpha The current best value for the maximizing node from this node to the root
     * @param beta The current best value for the minimizing node from this node to the root
     * @return The best child of this node with updated values
     */
    public GameStateChild alphaBetaSearch(GameStateChild node, int depth, double alpha, double beta)
    {
    	return alphaBetaSearch(node, depth, alpha, beta, true);
    }

	private GameStateChild oldAlphaBeta(GameStateChild node, int depth,
			double alpha, double beta) {
		// Order the list of children
    	List<GameStateChild> orderedChildren = orderChildrenWithHeuristics(node.state.getChildren());
    	
    	// If depth is 0 or node is terminal node, return node with updated heuristic
    	System.out.println("Depth: " + depth);
    	if (depth == 0 || orderedChildren.size() == 0) {
    		System.out.println("Depth: 0");
    		return node;
    	}
    	
    	// If maximizing player
    	if (isMaxPlayer(node)) {
    		
    		// v = -infinity
    		double v = -Double.POSITIVE_INFINITY;
    		
			// For each child of node
    		for (GameStateChild child: orderedChildren) {
    			
    			// Run recursive call to generate childNode & get utility
    			GameStateChild childNode = alphaBetaSearch(child, depth - 1, alpha, beta);
    			double utility = childNode.state.getUtility();
    			
				// Set v to be the max of v and alphaBetaSearch(child, depth, alpha, beta)
    			v = Math.max(v, utility);
    			
    			// If v and the utility function match, reassign the node 
    			if (v == utility) {
    				node = childNode;
    			}
    			
				// Set a to be the max a and v
    			alpha = Math.max(alpha, v);
    			
				// If Beta <= Alpha
    			if (beta <= alpha) {
    				break;
    			}
    		}
    		// return node with updated v
    	}
    	
    	// Else, minimize
    	else {
    		
    		// v = infinity
    		double v = Double.POSITIVE_INFINITY;
    		
    		// for each child node
    		for (GameStateChild child: orderedChildren) {
    			
    			// Run recursive call to generate childNode & get utility
    			GameStateChild childNode = alphaBetaSearch(child, depth - 1, alpha, beta);
    			double utility = childNode.state.getUtility();
    			
    			// Set v to be the min of v and alphaBetaSearch(child, depth, alpha, beta)
    			v = Math.min(v, utility);
    			
    			// If v and the utility function match, reassign the node 
    			if (v == utility) {
    				node = childNode;
    			}
    			
    			// Set b to be the min of b and v
    			beta = Math.min(beta, v);
    			
    			// If Beta <= Alpha
    			if (beta <= alpha) {
    				break;
    			}
    		}
    		// return node with updated v
    	}
        return node;
	}
    
    private GameStateChild alphaBetaSearch(GameStateChild node, int depth, double alpha, double beta, boolean isMaximizingPlayer){
    	if (depth == 0){ //or archers dead
    		return node;
    	}
    	GameStateChild best = node;
    	List<GameStateChild> orderedChildren = orderChildrenWithHeuristics(node.state.getChildren());
    	if (isMaximizingPlayer){
    		double v = Double.NEGATIVE_INFINITY;
    		for (GameStateChild child : orderedChildren){
    			GameStateChild descendent = alphaBetaSearch(child, depth-1, alpha, beta, !isMaximizingPlayer);
    			if (descendent.state.getUtility() > v){
    				node=descendent;
    			}
    			v = Math.max (v, descendent.state.getUtility());
    			alpha = Math.max(alpha, v);
    			if (beta <= alpha){
    				break;
    			}
    		}
    		return best;
    	}
    	else{
    		double v = Double.POSITIVE_INFINITY;
    		for (GameStateChild child : orderedChildren){
    			GameStateChild descendent = alphaBetaSearch(child, depth-1, alpha, beta, !isMaximizingPlayer);
    			if (descendent.state.getUtility() < v){
    				node = descendent;
    			}
    			v = Math.min(v, descendent.state.getUtility());
    			node = descendent;
    			beta = Math.min(beta, v);
    			if (beta <= alpha){
    				break;
    			}
    		}
    		return best;
    	}
    }

    /**
     * You will implement this.
     *
     * Given a list of children you will order them according to heuristics you make up.
     * See the assignment description for suggestions on heuristics to use when sorting.
     *
     * Use this function inside of your alphaBetaSearch method.
     *
     * Include a good comment about what your heuristics are and why you chose them.
     *
     * @param children
     * @return The list of children sorted by your heuristic.
     */
    public List<GameStateChild> orderChildrenWithHeuristics(List<GameStateChild> children)
    {
    	// Just use collections.sort
    	Collections.sort(children, new Comparator<GameStateChild>() {
    		public int compare(GameStateChild gsc1, GameStateChild gsc2) {
    			if (gsc1.state.getUtility() < gsc2.state.getUtility()) {
    				return -1;
    			}
    			else if (gsc1.state.getUtility() == gsc2.state.getUtility()) {
    				return 0;
    			}
    			else {
    				return -1;
    			}
    		}
    	});
    	
        return children;
    }
    
    /**
     * Returns a boolean based on the turn
     * @param node
     * @return
     */
    private boolean isMaxPlayer(GameStateChild node) {
    	return node.state.getTurnNumber() % 2 == 0;
    }
}

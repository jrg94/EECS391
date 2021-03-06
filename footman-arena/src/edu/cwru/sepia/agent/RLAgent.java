package edu.cwru.sepia.agent;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.action.ActionResult;
import edu.cwru.sepia.environment.model.history.DamageLog;
import edu.cwru.sepia.environment.model.history.DeathLog;
import edu.cwru.sepia.environment.model.history.History;
import edu.cwru.sepia.environment.model.state.State;
import edu.cwru.sepia.environment.model.state.Unit;
import edu.cwru.sepia.environment.model.state.Unit.UnitView;

import java.io.*;
import java.util.*;
import java.util.stream.IntStream;

public class RLAgent extends Agent {

    /**
     * Set in the constructor. Defines how many learning episodes your agent should run for.
     * When starting an episode. If the count is greater than this value print a message
     * and call sys.exit(0)
     */
    public final int numEpisodes;

    /**
     * List of your footmen and your enemies footmen
     */
    private List<Integer> myFootmen;
    private List<Integer> enemyFootmen;

    /**
     * Convenience variable specifying enemy agent number. Use this whenever referring
     * to the enemy agent. We will make sure it is set to the proper number when testing your code.
     */
    public static final int ENEMY_PLAYERNUM = 1;

    /**
     * Set this to whatever size your feature vector is.
     */
    public static final int NUM_FEATURES = 5;

    /** 
     * Use this random number generator for your epsilon exploration. When you submit we will
     * change this seed so make sure that your agent works for more than the default seed.
     */
    public final Random random = new Random(12345);

    /**
     * Your Q-function weights.
     */
    public Double[] weights;

    /**
     * These variables are set for you according to the assignment definition. You can change them,
     * but it is not recommended. If you do change them please let us know and explain your reasoning for
     * changing them.
     */
    public final double gamma = 0.9;
    public final double learningRate = .0001;
    public final double epsilon = .02;
    
    /**
     * Reward for a kill
     */
    public final double KILL_REWARD = 100;

    private Map<Integer, double[]> oldFeatureMap;
    
    private List<Double> averageRewardList;
    private Map<Integer, Double> footmanCumulativeRewardMap;
    
    private int overallEpisodeIteration;
    private int episodeIteration;
    private boolean learningMode;
    private int learningEpisodeIteration;
    private double learningRewardsSum;
    
    private int overallWinCount;
    private double bestReward;
    
    private final int DURATION_LEARNING_EPISODES = 5;
    private final int DURATION_FREE_PLAY_EPISODES = 10;
    private final boolean VERBOSE = false;
    
    
    
    public RLAgent(int playernum, String[] args) {
        super(playernum);

        if (args.length >= 1) {
            numEpisodes = Integer.parseInt(args[0]);
            System.out.println("Running " + numEpisodes + " episodes.");
        } else {
            numEpisodes = 10;
            System.out.println("Warning! Number of episodes not specified. Defaulting to 10 episodes.");
        }

        boolean loadWeights = false;
        if (args.length >= 2) {
            loadWeights = Boolean.parseBoolean(args[1]);
        } else {
            System.out.println("Warning! Load weights argument not specified. Defaulting to not loading.");
        }

        if (loadWeights) {
            weights = loadWeights();
        } else {
            // initialize weights to random values between -1 and 1
            weights = new Double[NUM_FEATURES];
            for (int i = 0; i < weights.length; i++) {
                weights[i] = random.nextDouble() * 2 - 1;
            }
        }
        
        oldFeatureMap = new HashMap<Integer, double[]>();
        averageRewardList = new LinkedList<Double>();
        overallEpisodeIteration = 0;
        episodeIteration = 0;
        
        footmanCumulativeRewardMap = new HashMap<Integer, Double>();
        learningMode = false;
        learningRewardsSum = 0d;
        overallWinCount = 0;
        bestReward = Double.MIN_VALUE;
    }

    /**
     * We've implemented some setup code for your convenience. Change what you need to.
     */
    @Override
    public Map<Integer, Action> initialStep(State.StateView stateView, History.HistoryView historyView) {

        // You will need to add code to check if you are in a testing or learning episode

        // Find all of your units
        myFootmen = new LinkedList<>();
        for (Integer unitId : stateView.getUnitIds(playernum)) {
            Unit.UnitView unit = stateView.getUnit(unitId);

            String unitName = unit.getTemplateView().getName().toLowerCase();
            if (unitName.equals("footman")) {
                myFootmen.add(unitId);
            } else {
                System.err.println("Unknown unit type: " + unitName);
            }
        }

        // Find all of the enemy units
        enemyFootmen = new LinkedList<>();
        for (Integer unitId : stateView.getUnitIds(ENEMY_PLAYERNUM)) {
            Unit.UnitView unit = stateView.getUnit(unitId);

            String unitName = unit.getTemplateView().getName().toLowerCase();
            if (unitName.equals("footman")) {
                enemyFootmen.add(unitId);
            } else {
                System.err.println("Unknown unit type: " + unitName);
            }
        }

        return middleStep(stateView, historyView);
    }

    /**
     * You will need to calculate the reward at each step and update your totals. You will also need to
     * check if an event has occurred. If it has then you will need to update your weights and select a new action.
     *
     * If you are using the footmen vectors you will also need to remove killed units. To do so use the historyView
     * to get a DeathLog. Each DeathLog tells you which player's unit died and the unit ID of the dead unit. To get
     * the deaths from the last turn do something similar to the following snippet. Please be aware that on the first
     * turn you should not call this as you will get nothing back.
     *
     * for(DeathLog deathLog : historyView.getDeathLogs(stateView.getTurnNumber() -1)) {
     *     System.out.println("Player: " + deathLog.getController() + " unit: " + deathLog.getDeadUnitID());
     * }
     *
     * You should also check for completed actions using the history view. Obviously you never want a footman just
     * sitting around doing nothing (the enemy certainly isn't going to stop attacking). So at the minimum you will
     * have an even whenever one your footmen's targets is killed or an action fails. Actions may fail if the target
     * is surrounded or the unit cannot find a path to the unit. To get the action results from the previous turn
     * you can do something similar to the following. Please be aware that on the first turn you should not call this
     *
     * Map<Integer, ActionResult> actionResults = historyView.getCommandFeedback(playernum, stateView.getTurnNumber() - 1);
     * for(ActionResult result : actionResults.values()) {
     *     System.out.println(result.toString());
     * }
     *
     * @return New actions to execute or nothing if an event has not occurred.
     */
    @Override
    public Map<Integer, Action> middleStep(State.StateView stateView, History.HistoryView historyView) {
    	
    	Map<Integer, Action> sepiaActions = new HashMap<Integer, Action>();
    	if (stateView.getTurnNumber() == 0){
    		//do first turn
    		for (Integer unitId : myFootmen){
    			sepiaActions.put(unitId, Action.createCompoundAttack(unitId, selectAction(stateView, historyView, unitId)));
    		}
    		return sepiaActions;
    	}
    	
    	// Get the deathlog (TODO: Make sure turn # starts at 1)
    	if (stateView.getTurnNumber() != 0) {
    		
    		// Runs through the death logs and removes corpses from the battlefield
    		for(DeathLog deathLog: historyView.getDeathLogs(stateView.getTurnNumber() - 1)) {
    			if (VERBOSE){
    				System.out.println(String.format("Unit [%d] belonging to player [%d] died", deathLog.getDeadUnitID(), deathLog.getController()));
    			}
    			// If the controller of this unit is the enemy, remove the player from the enemy list
    			if (deathLog.getController() == ENEMY_PLAYERNUM) {
    				removeElementFromList(enemyFootmen, deathLog.getDeadUnitID());
    			}
    			// Otherwise, remove the unit from the player list (TODO: make sure this properly removes the unit)
    			else {
    				removeElementFromList(myFootmen, deathLog.getDeadUnitID());
    			}
    		}
    	}
    	
    	List<Integer> idleUnits = new LinkedList<Integer>();
    	if (isSignificantEvent(stateView, historyView, idleUnits)){
    		for (Integer unitId : myFootmen){
    			double reward = calculateReward(stateView, historyView, unitId);
    			
    			if (!footmanCumulativeRewardMap.containsKey(unitId)){
    				footmanCumulativeRewardMap.put(unitId, 0d);
    			}
    			
    			footmanCumulativeRewardMap.put(unitId, footmanCumulativeRewardMap.get(unitId)+ discountReward(stateView, historyView, reward));
    			double footmanReward = footmanCumulativeRewardMap.get(unitId);
    			
    			if (learningMode){
    				// This way it adds all the footman rewards without having to recalculate it
    				learningRewardsSum += footmanReward;
    			}
    			
    			else if (oldFeatureMap.containsKey(unitId)){
    				weights = updateWeights(weights, oldFeatureMap.get(unitId), footmanReward, stateView, historyView, unitId);
    			}
    			
    			int defenderId = selectAction(stateView, historyView, unitId);
    			sepiaActions.put(unitId,  Action.createCompoundAttack(unitId, defenderId));
    		}	
    	}
    	
    	else{
    		for (Integer unitId : idleUnits){
    			sepiaActions.put(unitId, Action.createCompoundAttack(unitId, selectAction(stateView, historyView, unitId)));
    		}
    	}
    	
        return sepiaActions;
    }

    /**
     * Here you will calculate the cumulative average rewards for your testing episodes. If you have just
     * finished a set of test episodes you will call out testEpisode.
     *
     * It is also a good idea to save your weights with the saveWeights function.
     */
    @Override
    public void terminalStep(State.StateView stateView, History.HistoryView historyView) {

        // MAKE SURE YOU CALL printTestData after you finish a test episode.
    	    	
    	overallEpisodeIteration++;
    	int winnerId = -1;
    	
    	// If we have more footmen than the enemy
    	if (myFootmen.size() > enemyFootmen.size()){
    		winnerId = 0;
    		overallWinCount++;
    	}
    	// Otherwise, we lost
    	else{
    		winnerId = 1;
    	}
		
		System.out.println(String.format("[Episode: %d] player %d won with remaining: enemyFootmen[%d] vs. myFootmen[%d]"
				, overallEpisodeIteration
				, winnerId
				, enemyFootmen.size(), myFootmen.size()));
		
		// TODO: reset footmanCumulativeRewardMap?
		footmanCumulativeRewardMap = new HashMap<Integer, Double>();
		oldFeatureMap = new HashMap<Integer, double[]>();
		
		// If we're in learning mode
		if (learningMode){
			learningEpisodeIteration++;
			
			// Add rewards to averageRewardList
			if (learningEpisodeIteration == DURATION_LEARNING_EPISODES){
				double learningRewardAverage = learningRewardsSum/DURATION_LEARNING_EPISODES;
				averageRewardList.add(learningRewardAverage);
				learningMode = false;
				if (learningRewardAverage > bestReward){
					bestReward = learningRewardAverage;
					// Save your weights
					saveWeights(weights);
				}
			}
		}
		
		else{
			episodeIteration++;
			if (episodeIteration % DURATION_FREE_PLAY_EPISODES == 0){
				learningMode = true;
				learningEpisodeIteration = 0;
				learningRewardsSum = 0;
			}
		}
		
		if (overallEpisodeIteration >= numEpisodes){
			System.out.println("Win count: " + overallWinCount);
			printTestData(averageRewardList);
			System.exit(0);
		}


    }

    /**
     * Calculate the updated weights for this agent. 
     * @param oldWeights Weights prior to update
     * @param oldFeatures Features from (s,a)
     * @param totalReward Cumulative discounted reward for this footman.
     * @param stateView Current state of the game.
     * @param historyView History of the game up until this point
     * @param footmanId The footman we are updating the weights for
     * @return The updated weight vector.
     */
    public Double[] updateWeights(Double[] oldWeights, double[] oldFeatures, double totalReward, State.StateView stateView, History.HistoryView historyView, int footmanId) {
    	
    	// Creates a new weights array based on the size of the input array
    	Double[] newWeights = new Double[oldWeights.length];
    	double oldQValue = IntStream.range(0, oldWeights.length).mapToDouble(i->oldWeights[i]*oldFeatures[i]).sum();
    	
    	int defenderId = selectAction(stateView, historyView, footmanId);
    	double newQValue = calcQValue(stateView, historyView, footmanId, defenderId);
    	
    	// Loops through the old weights and calculates the new weights
    	for (int i = 0; i < oldWeights.length; i++) {
    		newWeights[i] = oldWeights[i] + learningRate * (totalReward + (gamma * newQValue) - oldQValue) * oldFeatures[i];
    	}
    	
        return newWeights;
    }

    /**
     * Given a footman and the current state and history of the game select the enemy that this unit should
     * attack. This is where you would do the epsilon-greedy action selection.
     *
     * @param stateView Current state of the game
     * @param historyView The entire history of this episode
     * @param attackerId The footman that will be attacking
     * @return The enemy footman ID this unit should attack
     */
    public int selectAction(State.StateView stateView, History.HistoryView historyView, int attackerId) {
    	
    	// Holds the last turn number
    	int lastTurnNumber = stateView.getTurnNumber() - 1;
    	
    	// If there is something to attack
    	if (enemyFootmen.size() != 0) {
    		
    		// If it is the first turn or we rolled higher that 1 - e
    		if (lastTurnNumber == 0 || random.nextDouble() > 1 - epsilon) {
    			
    			// Calculate the enemy index and get the enemy id
    			double enemyIndex  = random.nextDouble() * enemyFootmen.size();
    			int enemyID = enemyFootmen.get((int)enemyIndex);
    			
    			return enemyID;
    		}
    		
    		// Do the recommended action
    		else {
    		
    			// Store the current enemyID and calculates its qValue
    			int enemyID = enemyFootmen.get(0);
    			double qValue = calcQValue(stateView, historyView, attackerId, enemyID);
    			
    			// Run through the list of enemy footmen
    			for (int i = 0; i < enemyFootmen.size(); i++) {
    				
    				// Store the current enemy id and calculate its q value
    				int tmp = enemyFootmen.get(i);
    				double currQValue = calcQValue(stateView, historyView, attackerId, tmp);
    				
    				// If the current qValue is the largest yet
    				if (currQValue > qValue) {
    					
    					// Update some values
    					qValue = currQValue;
    					enemyID = enemyFootmen.get(i);
    				}
    			}
    			
    			return enemyID;
    			
    		}
    	}
    	
    	// There is nothing to attack
        return -1;
    }

    /**
     * Given the current state and the footman in question calculate the reward received on the last turn.
     * This is where you will check for things like Did this footman take or give damage? Did this footman die
     * or kill its enemy. Did this footman start an action on the last turn? See the assignment description
     * for the full list of rewards.
     *
     * Remember that you will need to discount this reward based on the timestep it is received on. See
     * the assignment description for more details.
     *
     * As part of the reward you will need to calculate if any of the units have taken damage. You can use
     * the history view to get a list of damages dealt in the previous turn. Use something like the following.
     *
     * for(DamageLog damageLogs : historyView.getDamageLogs(lastTurnNumber)) {
     *     System.out.println("Defending player: " + damageLog.getDefenderController() + " defending unit: " + \
     *     damageLog.getDefenderID() + " attacking player: " + damageLog.getAttackerController() + \
     *     "attacking unit: " + damageLog.getAttackerID());
     * }
     *
     * You will do something similar for the deaths. See the middle step documentation for a snippet
     * showing how to use the deathLogs.
     *
     * To see if a command was issued you can check the commands issued log.
     *
     * Map<Integer, Action> commandsIssued = historyView.getCommandsIssued(playernum, lastTurnNumber);
     * for (Map.Entry<Integer, Action> commandEntry : commandsIssued.entrySet()) {
     *     System.out.println("Unit " + commandEntry.getKey() + " was command to " + commandEntry.getValue().toString);
     * }
     *
     * @param stateView The current state of the game.
     * @param historyView History of the episode up until this turn.
     * @param footmanId The footman ID you are looking for the reward from.
     * @return The current reward
     */
    public double calculateReward(State.StateView stateView, History.HistoryView historyView, int footmanId) {
    	
    	// Each action costs the agent -0.1
    	// Friendly footman hits an enemy for d damage, the reward is +d
    	// Friendly footman is hit for d damage, the reward is -d
    	// If an enemy footman dies, the reward is +100
    	// If a friendly footman dies, the reward is -100
    	
    	// Reward accumulator
    	double reward = 0;
    	
    	// Holds the last turn number
    	int lastTurnNumber = stateView.getTurnNumber() - 1;
    	
    	// For each damage view, accumulate the reward
    	for(DamageLog damageLog : historyView.getDamageLogs(lastTurnNumber)) {
    		
    		if (VERBOSE){
	    	     System.out.println("Defending player: " + damageLog.getDefenderController() + " defending unit: " +
	    	     damageLog.getDefenderID() + " attacking player: " + damageLog.getAttackerController() +
	    	     "attacking unit: " + damageLog.getAttackerID());
    		}
    	     
    	     // If the enemy did damage
    	     if (damageLog.getDefenderController() == ENEMY_PLAYERNUM) {
    	    	 // Decrement the damage from the reward
    	    	 reward = reward - damageLog.getDamage();
    	     }
    	     // Otherwise, the player did the damage
    	     else {
    	    	 // Increment the damage to the reward
    	    	 reward = reward + damageLog.getDamage();
    	     }
    	}
    	
    	// Runs through the death logs
		for(DeathLog deathLog: historyView.getDeathLogs(lastTurnNumber)) {
			
			//System.out.println("Player: " + deathLog.getController() + " unit: " + deathLog.getDeadUnitID());
			
			// If the controller is an enemy
			if (deathLog.getController() == ENEMY_PLAYERNUM) {
				// Add the kill reward
				reward = reward + KILL_REWARD;
			}
			// Otherwise, this is friendly
			else {
				reward = reward - KILL_REWARD;
			}
		}
    	
		// Runs through all issued commands
    	Map<Integer, Action> commandsIssued = historyView.getCommandsIssued(playernum, lastTurnNumber);
        for (Map.Entry<Integer, Action> commandEntry : commandsIssued.entrySet()) {
        	// TODO: figure out what the hell is going on here
        	if (VERBOSE){
        		System.out.println("Unit " + commandEntry.getKey() + " was command to " + commandEntry.getValue().toString());
        	}
        	
        	reward = reward - 0.1;
        }
    	
        return reward;
    }

    /**
     * Calculate the Q-Value for a given state action pair. The state in this scenario is the current
     * state view and the history of this episode. The action is the attacker and the enemy pair for the
     * SEPIA attack action.
     *
     * This returns the Q-value according to your feature approximation. This is where you will calculate
     * your features and multiply them by your current weights to get the approximate Q-value.
     *
     * @param stateView Current SEPIA state
     * @param historyView Episode history up to this point in the game
     * @param attackerId Your footman. The one doing the attacking.
     * @param defenderId An enemy footman that your footman would be attacking
     * @return The approximate Q-value
     */
    public double calcQValue(State.StateView stateView,
                             History.HistoryView historyView,
                             int attackerId,
                             int defenderId) {
    	
    	// w*f(s,a)+w0
    	// w is a vector of learned weights
    	// f(s,a) is a vector of state-action features derived from the primitive state
    	
    	double[] featureVector = calculateFeatureVector(stateView, historyView, attackerId, defenderId);
    	double qValue = 0;
    	
    	// Sum the features by the product of their weights
    	for (int i = 0; i < weights.length; i++) {
    		qValue = qValue + (weights[i] * featureVector[i]);
    	}
    	
    	// Add w0 to the calculated qValue
    	qValue = qValue + weights[0];
    	
        return qValue;
    }

    /**
     * Given a state and action calculate your features here. Please include a comment explaining what features
     * you chose and why you chose them.
     *
     * All of your feature functions should evaluate to a double. Collect all of these into an array. You will
     * take a dot product of this array with the weights array to get a Q-value for a given state action.
     *
     * It is a good idea to make the first value in your array a constant. This just helps remove any offset
     * from 0 in the Q-function. The other features are up to you. Many are suggested in the assignment
     * description.
     *
     * @param stateView Current state of the SEPIA game
     * @param historyView History of the game up until this turn
     * @param attackerId Your footman. The one doing the attacking.
     * @param defenderId An enemy footman. The one you are considering attacking.
     * @return The array of feature function outputs.
     */
    public double[] calculateFeatureVector(State.StateView stateView,
                                           History.HistoryView historyView,
                                           int attackerId,
                                           int defenderId) {
    	/**
    	 * Features to use
    	 * 0. just a constant 1 so that w0 can be added in the loop
    	 * 1. inverse of Chebyshev distance
    	 * 2. Allied footmen attacking same defender //Number of times allied footmen successfully attacked
    	 * 3. Is self being attacked? //Number of times allied footmen got harmed
    	 * 4. HP comparison between self and target
    	 */
    	double[] features = new double[NUM_FEATURES];
    	//0. just a constant 1 so that w0 can be added in the loop
    	features[0] = 1.0;
    	
    	//1. inverse of Chebyshev distance
    	UnitView attacker = stateView.getUnit(attackerId);
    	UnitView defender = stateView.getUnit(defenderId);
    	
    	if (attacker == null || defender == null){
    		//Is this a bandage fix?
    		features[1] = 1;
    		features[4] = 1;
    	}
    	
    	else {
    		Position attackerPosition = new Position(attacker.getXPosition(), attacker.getYPosition());
    		Position defenderPosition = new Position(defender.getXPosition(), defender.getYPosition());
    		features[1] = 1.0/(attackerPosition.chebyshevDistance(defenderPosition));
    		
        	//4. HP comparison between self and target
        	//@TODO or should we do attackerHP/(attackerHP+defenderHP)?
        	double attackerHP = attacker.getHP();
        	double defenderHP = defender.getHP();
        	features[4] = attackerHP/defenderHP;
    	}
    	
    	//2. Allied footmen attacking same target
    	final int gangLimit = 3; //any more than this means the footman has to overextend into enemy lines
    	long gangCount = historyView.getDamageLogs(stateView.getTurnNumber()-1).stream().filter(dlog -> dlog.getDefenderID()==defenderId).count();
    	double gangFeature = 0d;
    	
    	if (gangCount <= 3) {
    		//gangFeature = gangCount/3;
    		gangFeature = 1/((double)(gangLimit - gangCount + 1));
    	}
    	
    	else {
    		gangFeature = 1/((double)(gangCount - gangLimit + 1));
    	}
    	
    	features[2] = gangFeature;
    	//3. Is self being attacked? 
    	//3. change to being attacked by defender?
    	features[3] = historyView.getDamageLogs(stateView.getTurnNumber()-1).stream().
    			filter(dlog -> dlog.getAttackerID() == defenderId && dlog.getDefenderID() == attackerId).findAny().isPresent() ? .5 : 1;
    	//features[3] = -historyView.getDamageLogs(stateView.getTurnNumber()-1).stream().filter(dlog -> dlog.getDefenderID()==attackerId).count();
    	
    	oldFeatureMap.put(attackerId, features);
        return features;
    }

    /**
     * DO NOT CHANGE THIS!
     *
     * Prints the learning rate data described in the assignment. Do not modify this method.
     *
     * @param averageRewards List of cumulative average rewards from test episodes.
     */
    public void printTestData (List<Double> averageRewards) {
        System.out.println("");
        System.out.println("Games Played      Average Cumulative Reward");
        System.out.println("-------------     -------------------------");
        for (int i = 0; i < averageRewards.size(); i++) {
            String gamesPlayed = Integer.toString(10*i);
            String averageReward = String.format("%.2f", averageRewards.get(i));

            int numSpaces = "-------------     ".length() - gamesPlayed.length();
            StringBuffer spaceBuffer = new StringBuffer(numSpaces);
            for (int j = 0; j < numSpaces; j++) {
                spaceBuffer.append(" ");
            }
            System.out.println(gamesPlayed + spaceBuffer.toString() + averageReward);
        }
        System.out.println("");
    }

    /**
     * DO NOT CHANGE THIS!
     *
     * This function will take your set of weights and save them to a file. Overwriting whatever file is
     * currently there. You will use this when training your agents. You will include th output of this function
     * from your trained agent with your submission.
     *
     * Look in the agent_weights folder for the output.
     *
     * @param weights Array of weights
     */
    public void saveWeights(Double[] weights) {
        File path = new File("agent_weights/weights.txt");
        // create the directories if they do not already exist
        path.getAbsoluteFile().getParentFile().mkdirs();

        try {
            // open a new file writer. Set append to false
            BufferedWriter writer = new BufferedWriter(new FileWriter(path, false));

            for (double weight : weights) {
                writer.write(String.format("%f\n", weight));
            }
            writer.flush();
            writer.close();
        } catch(IOException ex) {
            System.err.println("Failed to write weights to file. Reason: " + ex.getMessage());
        }
    }

    /**
     * DO NOT CHANGE THIS!
     *
     * This function will load the weights stored at agent_weights/weights.txt. The contents of this file
     * can be created using the saveWeights function. You will use this function if the load weights argument
     * of the agent is set to 1.
     *
     * @return The array of weights
     */
    public Double[] loadWeights() {
        File path = new File("agent_weights/weights.txt");
        if (!path.exists()) {
            System.err.println("Failed to load weights. File does not exist");
            return null;
        }

        try {
            BufferedReader reader = new BufferedReader(new FileReader(path));
            String line;
            List<Double> weights = new LinkedList<>();
            while((line = reader.readLine()) != null) {
                weights.add(Double.parseDouble(line));
            }
            reader.close();

            return weights.toArray(new Double[weights.size()]);
        } catch(IOException ex) {
            System.err.println("Failed to load weights from file. Reason: " + ex.getMessage());
        }
        return null;
    }

    @Override
    public void savePlayerData(OutputStream outputStream) {

    }

    @Override
    public void loadPlayerData(InputStream inputStream) {

    }
    
    private boolean isSignificantEvent(State.StateView stateView, History.HistoryView historyView, List<Integer> idleUnits){
    	int lastTurn = stateView.getTurnNumber() - 1;
    	if (lastTurn <= 0){
    		return true;
    	}
    	//Death is a significant event
    	if (historyView.getDeathLogs(lastTurn).size() > 0){
    		return true;
    	}
    	//friendly unit is hit
    	if (historyView.getDamageLogs(lastTurn).stream().filter(d->d.getDefenderController()!=ENEMY_PLAYERNUM).findAny().isPresent()){
    		return true;
    	}
    	
		Map<Integer, ActionResult> actionResults = historyView.getCommandFeedback(playernum, stateView.getTurnNumber() - 1);
	    for(ActionResult result : actionResults.values()) {
	    	int unitId = result.getAction().getUnitId();
	    	switch(result.getFeedback()){
	    	case INCOMPLETE:
	    		//Prob means the unit is still walking to the enemy
	    		//let the footman keep doing what it's doing
	    		break;
	    	case FAILED:
	    		//System.out.println(String.format("Unit [%d] failed to attack", unitId));
	    		return true;
	    	case COMPLETED:
	    		idleUnits.add(unitId);
	    		break;
			case INCOMPLETEMAYBESTUCK:
				System.out.println(String.format("Unit [%d] may be stuck", unitId));
				break;
			case INVALIDUNIT:
				return true;
			default:
				System.out.println(String.format("a case that shouldn't happen (%s) happened with unit [%d]", result.getFeedback().toString(), unitId));
				break;
	    	}
	    }
	    
	    return false;
    	
    }
    
    private void removeElementFromList(List<Integer> list, Integer element) {
    	int index = list.indexOf(element);
    	list.remove(index);
	}
    
    private double discountReward(State.StateView stateView, History.HistoryView historyView, double reward){
	    return Math.pow(gamma, stateView.getTurnNumber()-1)*reward;
    }
}

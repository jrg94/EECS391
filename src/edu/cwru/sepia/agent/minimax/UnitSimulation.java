package edu.cwru.sepia.agent.minimax;

import edu.cwru.sepia.environment.model.state.Unit;

public class UnitSimulation {
	private int xPosition;
	private int yPosition;
	private int id;
	private int currentHP;
	private int maxHP;
	private String name;
	private int range;
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}


	/**
	 * @return the range
	 */
	public int getRange() {
		return range;
	}


	public UnitSimulation(Unit.UnitView unit){
		xPosition=unit.getXPosition();
		yPosition=unit.getYPosition();
		id=unit.getID();
		currentHP=unit.getHP();
		maxHP=unit.getTemplateView().getBaseHealth();
		name=unit.getTemplateView().getName().toLowerCase();
		range=unit.getTemplateView().getRange();
	}
	

	/**
	 * @return the xPosition
	 */
	public int getXPosition() {
		return xPosition;
	}


	/**
	 * @param x the x to move horizontally by
	 */
	public void moveXBy(int x){
		this.xPosition += x;
	}


	/**
	 * @return the yPosition
	 */
	public int getYPosition() {
		return yPosition;
	}


	/**
	 * @param y the y to move vertically by
	 */
	public void moveYBy(int y) {
		this.yPosition += y;
	}


	/**
	 * @return the unitID
	 */
	public int getID() {
		return id;
	}

	/**
	 * @return the currentHP
	 */
	public int getCurrentHP() {
		return currentHP;
	}

	/**
	 * @return the maxHP
	 */
	public int getMaxHP() {
		return maxHP;
	}

}

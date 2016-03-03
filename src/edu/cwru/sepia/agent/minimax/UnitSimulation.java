package edu.cwru.sepia.agent.minimax;

import edu.cwru.sepia.environment.model.state.Unit;

public class UnitSimulation {
	private int x;
	/**
	 * @return the x
	 */
	public int getX() {
		return x;
	}

	/**
	 * @param x the x to set
	 */
	public void setX(int x) {
		this.x = x;
	}

	/**
	 * @return the y
	 */
	public int getY() {
		return y;
	}

	/**
	 * @param y the y to set
	 */
	public void setY(int y) {
		this.y = y;
	}

	/**
	 * @return the unitID
	 */
	public int getUnitID() {
		return unitID;
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

	private int y;
	private int unitID;
	private int currentHP;
	private int maxHP;
	
	public UnitSimulation(Unit.UnitView unit){
		x=unit.getXPosition();
		y=unit.getYPosition();
		unitID=unit.getID();
		currentHP=unit.getHP();
		maxHP=unit.getTemplateView().getBaseHealth();
	}
}

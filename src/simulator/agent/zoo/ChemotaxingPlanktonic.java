/**
 * 
 */
package simulator.agent.zoo;

import simulator.SoluteGrid;
import simulator.geometry.ContinuousVector;

/**
 * @author alexandraweston
 *
 */

public class ChemotaxingPlanktonic extends Planktonic {
	public enum Direction {UP, DOWN, LEFT, RIGHT};
	/**
	 * 
	 */
	public ChemotaxingPlanktonic() {
		// 
		super();
	}
	
	/**
	 * overrides Planktonic.determineNewLoc
	 * implements chemotaxis
	 */
	protected void determineNewLoc() {
		chemotax();
		
	}
	
	/**
	 * examines solute grid to determine where to
	 * move based on attraction/repulsion to/from 
	 * a solute
	 */
	private void chemotax() {
		// TODO implement more complicated chemotaxis
		
		//get the specific soluteGrid
		//TODO make a more reusable way to get the correct solute grid
		//this ONLY works for planktonic_biofilm_timres.xml
		SoluteGrid chemotaxSolute = this._agentGrid.mySim.soluteList[0];
		//get the [solute] at adjacent grid points
		ContinuousVector testLoc = this._location;
		double curr= chemotaxSolute.getValueAt(testLoc);
		testLoc.add(8,0,0);
		double above = chemotaxSolute.getValueAt(testLoc);
		testLoc.add(-16,0,0);
		double below= chemotaxSolute.getValueAt(testLoc);
		testLoc.add(8,-8,0);
		double left= chemotaxSolute.getValueAt(testLoc);
		testLoc.add(0,16,0);
		double right= chemotaxSolute.getValueAt(testLoc);
		
		//_movement.add(0,2,0);
		//move in the direction that has the smallest concentration. Else, move right
		
		Direction currDir = Direction.RIGHT;
		if((curr>left)){
			curr = left;
			currDir = Direction.LEFT;
		}
		if((curr>right)){
			curr = right;
			currDir = Direction.RIGHT;
		}
		if((curr>above)){
			curr = above;
			currDir = Direction.UP;
		}
		if((curr>below)){
			curr = below;
			currDir = Direction.DOWN;
		}
		switch(currDir){
			case UP:
				_movement.add(2,0,0);
				//System.err.println("moving UP");
				break;
			case DOWN:
				_movement.add(-2,0,0);
				//System.err.println("moving DOWN");
				break;
			case LEFT:
				_movement.add(0,-2,0);
				//System.err.println("moving LEFT");
				break;
			case RIGHT:
				_movement.add(0,2,0);
				//System.err.println("moving RIGHT");
				break;
		}
		
		
	}

}

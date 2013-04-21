/**
 * 
 */
package simulator.agent.zoo;

/**
 * @author alexandraweston
 *
 */
public class ChemotaxingPlanktonic extends Planktonic {

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
		_movement.add(0,2,0);
		
	}

}

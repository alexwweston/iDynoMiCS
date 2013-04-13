/**
 * 
 */
package simulator.agent.zoo;

import simulator.geometry.ContinuousVector;
import utils.LogFile;

/**
 * @author alexandraweston
 *
 */
public class Planktonic extends Bacterium {

	/**
	 * @author alexandraweston: Planktonic is similar to Bacterium, but overrides
	 * methods that function differently.
	 */
	public Planktonic() {
		// TODO Planktonic-specific application calls
		super();
		
	}
	/**
	 * Called at each time step (under the control of the method Step of the
	 * class Agent to avoid multiple calls
	 */
	protected void internalStep() {
		//Compute mass growth over all compartments
		//grow();
		//updateSize();
		//get and add to location
		//System.out.println(this.getLocation());\
		//System.out.println("Agent internalStep");
		_movement.add(0,2,0);
		
		move();
		
		
		//System.out.println(this.getLocation());


		// Die if you are out of bounds
		/*
		if (! _agentGrid.domain.isInside(_location)  ) {
			die(true);
			System.out.println("killing in internalStep due to out of bounds location");
		}
		*/
	}
	
	/**
	 * @override original method in LocatedAgent
	 * LocatedAgent.move() prevents agents from leaving a boundary--but we
	 * need to simply detect and kill those agents that leave the domain
	 * Apply the movement stored taking care to respect boundary conditions
	 */
	public double move() {
		//System.out.println("Agent is moving");
		if (!_movement.isValid()) {
			LogFile.writeLog("Incorrect movement coordinates");
			_movement.reset();
		}

		if (!_agentGrid.is3D&&_movement.z!=0) {
			_movement.z = 0;
			_movement.reset();
			LogFile.writeLog("Try to move in z direction !");
		}

		// No movement planned, finish here
		if (_movement.isZero())
			return 0;
		
		_location.add(_movement);
		
		// Test the boundaries
		//checkBoundaries();
		// Die if you are out of bounds
		/*if (! _agentGrid.domain.isInside(_location)  ){ 
			die(true);
			System.out.println("killing in move() due to out of bounds location");
			
			return 0;
		}*/
	

			_agentGrid.registerMove(this);
	
			double delta = _movement.norm();
			_movement.reset();
			return delta/_totalRadius;


	}

	
	
	

}

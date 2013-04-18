/**
 * 
 */
package simulator.agent.zoo;

import java.util.Iterator;

import simulator.agent.LocatedAgent;
import simulator.geometry.ContinuousVector;
import utils.LogFile;

/**
 * @author alexandraweston
 *
 */
public class Planktonic extends Bacterium {
	public double swimSpeed;//speed (um/s) at which the planktonic executes
							//swim behavior //TODO: integrate parameter
	public double attachmentRadius=3;//microns

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

		_movement.add(0,2,0);
		
		move();
		
		//check for possibility that planktonic will attach
		//to biofilm
		if(willAttach(1, attachmentRadius)){
			attach();
			
		}
		
	}
	
	/**
	 * Handles removal of this planktonic from the system
	 * and addition of new biofilm cell to the System
	 */
	private void attach() {
		System.out.println("would attach at location: " + _location);
		//1) Register new biofilm cell with this planktonic's 
		//location //TODO: should other parameters carry over as well?
		

		//2) Remove and kill this planktonic
		
	}
	/**
	 * Tests if planktonic is close enough to a biofilm cell to attach.
	 * 
	 * @param gridDist : how many grid cells around the agent will you check
	 * 					 intuition says just one
	 * @param attachDist : the distance a planktonic must be from a biofilm agent
	 * 					   to achieve attachment. This is implimented similarly
	 * 					   to LocatedAgent.addPushMovement();
	 */
	private boolean willAttach(double gridDist, double attachDist) {
		boolean attach= false;
		LocatedAgent anAgent;
		double dist;
		
		getPotentialShovers(gridDist);
		Iterator<LocatedAgent> iter = _myNeighbors.iterator();
		while (iter.hasNext()&&attach==false) {
			anAgent=iter.next();
			//check for a neighboring biofilm cell
			if(!anAgent.getClass().equals(Planktonic.class)){
				dist = computeDifferenceVector(_location, anAgent._location);
				if(dist<attachDist) attach= true;
				
			}
	
		}
		_myNeighbors.clear();
		
		
		return attach;
		
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
		if (! _agentGrid.domain.isInside(_location)  ){ 
			//die(true);
			_agentGrid.mySim.planktonicManager.scheduleRemove(this);
			System.out.println("removing planktonic from planktonicManager." +
								" Planktonic is out of bounds");
			
		}
	

			_agentGrid.registerMove(this);
	
			double delta = _movement.norm();
			_movement.reset();
			return delta/_totalRadius;


	}
	
	/**
	 * Register the agent on the agent grid and on the guilds
	 * @author alexandraweston: additionally, register the planktonic
	 * in the PlanktonicManager
	 */
	public void registerBirth() {
		// Register on species and reaction grids
		super.registerBirth();
		this._agentGrid.mySim.planktonicManager.registerPlanktonic(this);
		
	}

	
	
	

}

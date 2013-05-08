/**
 * 
 */
package simulator.agent.zoo;

import java.util.Iterator;

import simulator.Simulator;
import simulator.agent.LocatedAgent;
import simulator.agent.SpecialisedAgent;
import simulator.geometry.ContinuousVector;
import simulator.geometry.Domain;
import utils.ExtraMath;
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
		super();		
		
		
	}
	/**
	 * Called at each time step (under the control of the method Step of the
	 * class Agent to avoid multiple calls
	 */
	protected void internalStep() {
		
		determineNewLoc();
		
		
		move();
		
		//check for possibility that planktonic will attach
		//to biofilm
		if(willAttach(1, attachmentRadius)){
			attach();
			
		}
		
	}
	
	/**
	 * sets the location to move to during an internalStep()
	 * 
	 */
	protected void determineNewLoc() {
		
		//_movement.add((ExtraMath.getNormRand()-.5)*5,(ExtraMath.getNormRand()-.5)*5,0);
		_movement.add(0,2,0);
		
	}
	/**
	 * Handles removal of this planktonic from the system
	 * and addition of new biofilm cell to the System
	 */
	private void attach() {
		System.out.println("attach at location: " + _location);
		//1) Register new biofilm cell with this planktonic's 
		//location //TODO: should other parameters carry over as well?
		
		_agentGrid.mySim.planktonicManager.newBiofilmCell(_location);
		//2) Remove and kill this planktonic
		_agentGrid.mySim.planktonicManager.scheduleRemove(this);
		super.die(true);
		
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
			if(!(anAgent instanceof Planktonic)){
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
		//set the location at which the planktonic
		//enters the system
		entryLoc();
		
		
		// Register on species and reaction grids
		super.registerBirth();
		//register in Planktonic Manager
		_agentGrid.mySim.planktonicManager.registerPlanktonic(this);
		
	}
	public void entryLoc(){
		//remember here that the traditional meanings of x and y
		//are switched in iDynoMiCS
		double rand, x_coord, y_coord, height, width;
		try{
		Domain compDomain = _agentGrid.mySim.world.getDomain("MyBiofilm");
		width = compDomain.length_Y;
		height = compDomain.length_X;
		System.out.println("after Planktonic()->width/height");
		//the random number will be normally distributed, as it's more likely
		//planktonics enter from the top of the simulation.
		//2width + height is the length of the computational domain's border; that is,
		//the length of the border at which planktonics can arrive
		//TODO: implement normal. for now, just using uniform
		rand=  ((2*width + height)*ExtraMath.getUniRand());
		//test if the entry location will be at the 
		//top or on one of the sides

		if(rand<=height){
			y_coord = 1;
			x_coord = rand;
			
			
		}else if(rand<=(height+width)){
			y_coord = rand - height;
			x_coord = height-1;
		}else{
			y_coord = width-1;
			x_coord = rand - width - height;
		}

		ContinuousVector startLoc = new ContinuousVector(x_coord, y_coord, 0);
		if( !_agentGrid.domain.isInside(startLoc) ){
			System.err.println("ru-roh");
		}
		this.setLocation(startLoc);
		}catch(Exception e){
			System.out.println("error in Planktonic.entryLoc()");
		}
		
	}
	
	

}

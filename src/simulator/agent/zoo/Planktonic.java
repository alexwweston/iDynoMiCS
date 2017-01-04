/**
 * 
 */
package simulator.agent.zoo;

import java.util.Iterator;

import org.jdom.Element;

import simulator.Simulator;
import simulator.agent.LocatedAgent;
import simulator.agent.SpecialisedAgent;
import simulator.agent.Species;
import simulator.geometry.ContinuousVector;
import simulator.geometry.Domain;
import utils.ExtraMath;
import utils.LogFile;
import utils.XMLParser;

/**
 * @author alexandraweston
 *
 */
public class Planktonic extends Bacterium {
	
	/*
	 * Parameters read in from protocol file
	 */
	public int arrivalRate;
	boolean hasAttachment; //whether or not the species will attach if it nears the biofilm
	public double distEachRun;
	int attachmentRadius;
	String attachmentSpecies; //indicates what species this becomes if
							  //it joins the biofilm
	
	/**
	 * Initialises the progenitor
	 */

	public void initFromProtocolFile(Simulator aSim, XMLParser aSpeciesRoot) {
		// Initialisation of Bacterium
		super.initFromProtocolFile(aSim, aSpeciesRoot);
		
		//Planktonic-specific protocol parameters
		hasAttachment  	=  	aSpeciesRoot.getParamBool("willAttach");
		distEachRun 	= 	aSpeciesRoot.getParamDbl("distEachRun");
		arrivalRate 	=	aSpeciesRoot.getParamInt("arrivalRate");
		//attachment parameters
		if(hasAttachment){
			attachmentRadius = aSpeciesRoot.getParamInt("attachmentDistance");
			attachmentSpecies = aSpeciesRoot.getParam("becomesBiofilmSpecies").trim();
		}

		init();
	}


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
		
		if(hasAttachment && willAttach(1, attachmentRadius)){
			attach();
		}		
		
	}
	
	/**
	 * sets the location to move to during an internalStep()
	 * 
	 */
	protected void determineNewLoc() {
		
			//choose a random direction by getting a random angle
		    double rads =  (ExtraMath.getUniRand() * Math.PI * 2);
		    double random_z = ExtraMath.getUniRand();
		    double random_x = ExtraMath.getUniRand();
		    if (random_x > .5) {
		    	random_z = random_z * -1;
		    }
		    ContinuousVector direction = new ContinuousVector (Math.cos(rads), Math.sin(rads), random_z);
		    _movement = this.getScaledMove(_location, direction, ExtraMath.getUniRand()*distEachRun);
		
		//_movement.add( (ExtraMath.getNormRand())*2, (ExtraMath.getNormRand())*2, 0);
		//_movement.add(-10,5,0);
		
	}
	/**
	 * Handles removal of this planktonic from the system
	 * and addition of new biofilm cell to the System
	 */
	private void attach() {
		System.out.println("attach at location: " + _location);
		//1) Register new biofilm cell with this planktonic's 
		//location //TODO: should other parameters carry over as well?
		
		LogFile.chronoMessageOut("JOINER" + "\t" + this.getName() + "\t" + _location);
		_agentGrid.mySim.planktonicManager.newBiofilmCell(_location, attachmentSpecies);
		
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
	/**
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
			System.out.println("removing planktonic " + this + " from planktonicManager." +
								" Planktonic is out of bounds");
		}

			_agentGrid.registerMove(this);


			double delta = _movement.norm();
			_movement.reset();

			return delta/_totalRadius;


	}
	*/
	
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
		// ARSN: entry location for our simulations is the z
		
		double rand, x_coord, y_coord, z_coord, height, width, depth;
		
		try {
		Domain compDomain = _agentGrid.mySim.world.getDomain("MyBiofilm");
		width = compDomain.length_Y;
		height = compDomain.length_X;
		if (compDomain.is3D()) {
			depth = compDomain.length_Z;
		} else {
			depth = 0;
		}
		x_coord = height - 20;
		y_coord = Math.random() * width;
		z_coord = Math.random() * depth;

		ContinuousVector startLoc = new ContinuousVector(x_coord, y_coord, z_coord);
		this.setLocation(startLoc);
		
		} catch (Exception e) {
			System.out.println("error in Planktonic.entryLoc()");			
		}

/*		try{
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
			System.err.println("ru-roh: " + startLoc.toString());
		}
		this.setLocation(startLoc);
		}catch(Exception e){
			System.out.println("error in Planktonic.entryLoc()");
		}
		*/
		
	}
	
	/**
	 * 
	 * @param loc
	 * @param direction
	 * @param dist
	 * @return location that is dist away from loc in the direction dir
	 * 
	 * Adds direction to loc, gets the Euclidean distance between these two points,
	 * then multiplies newLoc by a scaler to get the correct distance in direction
	 * dir 
	 */
	ContinuousVector getScaledMove(ContinuousVector loc, ContinuousVector dir, double dist){
		//create directed move
		double currDist, scaler;
		ContinuousVector newLoc = new ContinuousVector();
		newLoc.set(loc);
		newLoc.add(dir);
		//		(loc.x + dir.x), (loc.y + dir.y), (loc.z+dir.z) );
		
		//get euclidean distance between the two locations
		currDist = newLoc.distance(loc);
		
		//compute scaler
		scaler = dist/currDist;
		dir.times(scaler);
			//newLoc.times(scaler);
			
			//subtract distance from the location, to create
			//compatibility with current move() method
			//newLoc.subtract(loc);
		return dir;
	}
	


	

}

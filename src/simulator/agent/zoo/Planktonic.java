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
		//1) Register new biofilm cell with this planktonic's 
		//location //TODO: should other parameters carry over as well?
		
		LogFile.chronoMessageOut("Joined biofilm:" + "\t" + this.getName() + "\t" + _location);
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

/**
 * 
 */
package simulator.Planktonic;

import idyno.Idynomics;
import idyno.SimTimer;

import java.util.*;

import simulator.agent.*;
import simulator.agent.zoo.*;

import simulator.detachment.*;
import simulator.diffusionSolver.Solver_pressure;

import simulator.geometry.*;
import simulator.geometry.boundaryConditions.AllBC;
import simulator.AgentContainer;
import simulator.Simulator;
import simulator.SpatialGrid;

import utils.ResultFile;
import utils.XMLParser;
import utils.LogFile;
import utils.ExtraMath;


/**
 * @author alexandraweston
 * This class handles Planktonic steps and additions of planktonics to the system
 */
public class PlanktonicManager {
	public enum eventType {ARRIVAL, STEP};
	
/* -------------------- Properties ---------------------------- */	

	public Domain domain;
	public Simulator mySim;
	public final double AGENTTIMESTEP;//(hr)
	public final double PLANKTONICTIMESTEP;//(hr)
	public final double PLANKTONICARRIVALRATE;//arrivals/hr
	public double arrivalPerAgentTS;//arrivals/agentTimeStep
	public double plankTSPerAgentTS;//number of planktonic time steps per agent time step
	protected Species biofilmSpec;
	String planktonicName = "ChemotaxPlank";
	
	// Container for all agents (even the non located ones)

	public LinkedList<Planktonic> planktonicList ;
	public ListIterator<Planktonic> planktonicIter;
	
	// To avoid modifying the list whilst stepping through planktonics in
	// stepPlanktonics(), add planktonics to this list and remove from 
	// planktonicList only after stepping through the entire planktonicList
	public LinkedList<Planktonic> removePlanktonics;
	public ListIterator<Planktonic> removePlanktonicsIter;

	// Temporary containers used to store agents who will be removed
	public LinkedList<Planktonic> _planktToKill = new LinkedList<Planktonic>();

	public PriorityQueue<Event> eventQueue;
	
	/*
	 * instantiate a PlanktonicManager
	 * called by Simulator.createSpecies()
	 * the root is set at planktonicManagement
	 */
	public PlanktonicManager(Simulator aSimulator, XMLParser root, double agentTimeStep) throws Exception {
		try{
			//TODO read in vals from the protocol file. For now, just hard-code them
			PLANKTONICTIMESTEP = .01;
			PLANKTONICARRIVALRATE = 50;
			
			mySim = aSimulator;
			AGENTTIMESTEP= agentTimeStep;
	
			//set up list holding planktonics
			planktonicList = new  LinkedList<Planktonic> ();
			planktonicIter = planktonicList.listIterator();
			
			//set up list for holding planktonics to be removed
			removePlanktonics = new LinkedList<Planktonic>();
			//set up eventQueue
			eventQueue=new PriorityQueue<Event>();
			
			//determine how many planktonics will arrive per AGENTTIMESTEP
			arrivalPerAgentTS = PLANKTONICARRIVALRATE*AGENTTIMESTEP;
			
			//TODO: associate the planktonics with a specific type of biofilm cell
			//in the protocol file. Parse this here
			int index = mySim.getSpeciesIndex("JoinedPlanktonic"); 
			biofilmSpec = mySim.speciesList.get(index);
			
		}catch(Exception e){
			System.out.println(" error in PlnktonicManager constructor " + e);
			throw e;
		}
			
		
	}
	
	/** @author alexandraweston
	 * add a planktonic to the manager's planktonicList. 
	 */
	public void registerPlanktonic(Planktonic anAgent){
	
	try{
			planktonicList.add(anAgent);
	}catch(Exception e){
		System.out.println(" error in registerPlanktonic " + e);
	}
		
		
	}
	
	/**
	 * @author alexandraweston:remove all agents scheduled for removal from the Planktonic list
	 */
	public void removePlanktonics(){
		try{
			if(!removePlanktonics.isEmpty()){
				Planktonic aPlanktonic;
				for (removePlanktonicsIter = removePlanktonics.listIterator(); removePlanktonicsIter.hasNext();) {
					aPlanktonic = removePlanktonicsIter.next();
					planktonicList.remove(aPlanktonic);
					
				}
				//now clear the list of planktonics to remove
				removePlanktonics.clear();
			}
		}catch(Exception e){
			System.out.println(" error in removePlanktonics " + e);
		}
	}
	
	/*
	 * adds a Planktonic to the removePlanktonics list
	 * called by Planktonic.move() when an agent moves out of bounds
	 */
	public void scheduleRemove(Planktonic aPlanktonic){
		try{
			removePlanktonics.add(aPlanktonic);
		}
		catch(Exception e){
			System.out.println(" error in scheduleRemove " + e);
		}
		
	}
	
	/**
	 * @author alexandraweston: called every AGENTTIMESTEP by Agent Container
	 * updates the locations of all Planktonics. Uses eventlist to schedule planktonic 
	 * arrival in between agent time steps based on PLANKTONICARRIVALRATE
	 */
	
	public void runPlanktonicTimeSteps(){
		try{
			//add planktonic arrival events
			//time will be uniformly distributed between 0 and AGENTTIMESTEP
			for(int i=0; i< arrivalPerAgentTS; i++){
				eventQueue.add(new Event(eventType.ARRIVAL, Math.random()*AGENTTIMESTEP));
			}
			
			Event currEvent;
			double time = PLANKTONICTIMESTEP;
			
			//add planktonic stepping events
			while(time<AGENTTIMESTEP){
				eventQueue.add(new Event(eventType.STEP, time));
				time += PLANKTONICTIMESTEP;
			}
			
			
			/*add a final step event, which occurs at the next AGENTTIMESTEP
			* Note: similar to how the last iterate of an Agent time step is handled in 
			* AgentContainer.step(), this final time slice could be shorter than PLANKTONICTIMESTEP
			*/
			eventQueue.add(new Event(eventType.STEP, AGENTTIMESTEP));
			
			
			while(!eventQueue.isEmpty()){
				currEvent= eventQueue.remove();
				//System.out.println("time: " + currEvent.time);
				if(currEvent.etype == eventType.ARRIVAL){
					addPlanktonic();
				}
				else{//etype is eventType.STEP
					stepPlanktonics();
				}
			}
		}catch(Exception e){
			System.out.println(" error in runPlanktonicTimeSteps " + e);
		}
			

	}
	
	public void stepPlanktonics(){
		try{
		System.out.println("stepping all planktonics");
		Planktonic aPlanktonic;
		
		for (planktonicIter = planktonicList.listIterator(); planktonicIter.hasNext();) {
			aPlanktonic = planktonicIter.next();
			aPlanktonic.step();
		}
		//only remove planktonics after stepping through the whole list
		removePlanktonics();
		}catch(Exception e){
			System.out.println(" error in stepPlanktonics " + e);
		}
		
	}
	
	/**
	 * add a planktonic to the entire system
	 */
	public void addPlanktonic(){
		//TODO generalize this for use with any protocol file
		
		try{
		System.out.println("adding planktonic");
		
		XMLParser parser = mySim._protocolFile.getChild("planktonicInit").getChild("initArea");
		int index = mySim.getSpeciesIndex(planktonicName);
		//System.out.println("good");
		Species plankSpec = mySim.speciesList.get(index);
		//System.out.println("good 2");
		plankSpec.createPop(parser);
		//System.out.println("good 3");
		
		}catch(Exception e){
			System.out.println(" error in addPlanktonic " + e);
		}
			
	}
	
	/**
	 * called by Planktonic when a particular plantonic is within range to
	 * attach to the biofilm.
	 * @param _location is the current location of the plankonic cell that will attach
	 */

	public void newBiofilmCell(ContinuousVector _location) {
		// for now, just get the MyBiofilm species and createPop() one of those.
		biofilmSpec.createPop(_location);
		
		
		
	}
	
	
	/**
	 * private inner class for use in the eventQueue
	 */
	private class Event implements Comparable<Event>{
		public eventType etype;
		public double time;
		
		public Event(eventType type, double t){
			etype = type;
			time =t;
			
		}

		@Override
		public int compareTo(Event o) {
			//don't allow equals (i.e. comparator never returns 0
			//that way we won't have to worry about tie-breaking in the eventQueue
			if(this.time<=o.time)
				return -1;
			if(this.time>o.time)
				return 1;
			else
				return 0;
		}
	}

	
	

}


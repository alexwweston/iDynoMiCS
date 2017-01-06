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
	public double plankTSPerAgentTS;//number of planktonic time steps per agent time step

	// Container for all agents (even the non located ones)
	public LinkedList<Planktonic> planktonicList ;
	public ListIterator<Planktonic> planktonicIter;
	
	//Container to keep track of a planktonic species' particular properties
	//needed by manager
	public LinkedList<Planktonic> plankProgenList;
	public ListIterator<Planktonic> plankProgenIter;
	
	// To avoid modifying the list whilst stepping through planktonics in
	// stepPlanktonics(), add planktonics to this list and remove from 
	// planktonicList only after stepping through the entire planktonicList
	public LinkedList<Planktonic> removePlanktonics;
	public ListIterator<Planktonic> removePlanktonicsIter;

	// Temporary containers used to store agents who will be removed
	// not used???
	public LinkedList<Planktonic> _planktToKill = new LinkedList<Planktonic>();

	public PriorityQueue<Event> eventQueue;
	
	/**
	 * instantiate a PlanktonicManager
	 * called by Simulator.createSpecies()
	 * the root is set at planktonicManagement
	 */
	public PlanktonicManager(Simulator aSimulator, XMLParser root, double agentTimeStep) throws Exception {
		try{
			//TODO read in vals from the protocol file. For now, just hard-code them
			PLANKTONICTIMESTEP = root.getParamDbl("planktonicTimeStep");
			
			mySim = aSimulator;
			AGENTTIMESTEP= agentTimeStep;
	
			//set up list holding planktonics
			planktonicList = new  LinkedList<Planktonic> ();
			planktonicIter = planktonicList.listIterator();
			
			//set up list for holding planktonics to be removed
			removePlanktonics = new LinkedList<Planktonic>();
			//set up eventQueue
			eventQueue=new PriorityQueue<Event>();
			
			//for implementation of different arrival rates, start a list of the
			//different planktonic species
			plankProgenList = new LinkedList<Planktonic>();
			planktonicIter = plankProgenList.listIterator();
			
		}catch(Exception e){
			System.out.println(" error in PlanktonicManager constructor " + e);
			throw e;
		}
		
	}
	
	/** @author alexandraweston
	 * add a planktonic to the manager's planktonicList.
	 * called by Planktonic.registerBirth() when a new planktonic is
	 * created
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
			/*for(int i=0; i< arrivalPerAgentTS; i++){
				eventQueue.add(new Event(eventType.ARRIVAL, Math.random()*AGENTTIMESTEP));
			}*/
			Planktonic aPlanktonic;
			for (plankProgenIter = plankProgenList.listIterator(); plankProgenIter.hasNext();) {
				aPlanktonic = plankProgenIter.next();
				for(int i=0; i< aPlanktonic.arrivalRate*AGENTTIMESTEP; i++){
					eventQueue.add(new Event(eventType.ARRIVAL, Math.random()*AGENTTIMESTEP, aPlanktonic.getSpecies().speciesName));
				}
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
					addPlanktonic(currEvent.spec);
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
		LogFile.chronoMessageOut("Stepping all planktonics");
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
	 * add a planktonic to the entire system.
	 * actual starting location will be set by the planktonic itself
	 */
	public void addPlanktonic(String plankName){
		
		try{

		int index = mySim.getSpeciesIndex(plankName);
		
		Species plankSpec = mySim.speciesList.get(index);
		
		plankSpec.createPop(new ContinuousVector(0,0,0));
		
		
		}catch(Exception e){
			System.out.println(" error in addPlanktonic " + e);
		}
			
	}
	
	/**
	 * called by Planktonic when a particular planktonic is within range to
	 * attach to the biofilm.
	 * @param _location is the current location of the plankonic cell that will attach
	 */

	public void newBiofilmCell(ContinuousVector _location, String species) {

		int index = mySim.getSpeciesIndex(species); 
		Species biofilmSpec = mySim.speciesList.get(index);
		biofilmSpec.createPop(_location);
		
		
	}
	
	
	/**
	 * private inner class for use in the eventQueue
	 */
	private class Event implements Comparable<Event>{
		public eventType etype;
		public double time;
		public String spec; //used for arrival events
		
		public Event(eventType type, double t){
			etype = type;
			time =t;
			spec = null;
			
		}
		
		public Event(eventType type, double t, String s){
			etype = type;
			time =t;
			spec = s;
			
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


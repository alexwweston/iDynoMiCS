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
 * This class is similar to AgentContainer, but extending AgentContainer for Planktonic purposes
 * would violate the principle of Substitutability, so instead a similar but different class is written for 
 * PlanktonicContainer.
 */
public class PlanktonicManager {
/* -------------------- Properties ---------------------------- */	

	public Domain domain;
	public Simulator mySim;
	public double AGENTTIMESTEP;
	
	// Container for all agents (even the non located ones)

	public LinkedList<Planktonic> planktonicList;
	public ListIterator<Planktonic> planktonicIter;

	// Temporary containers used to store agents who will be removed
	public LinkedList<Planktonic> _planktToKill = new LinkedList<Planktonic>();



	public PlanktonicManager(Simulator aSimulator, XMLParser root, double agentTimeStep) throws Exception {
		if(Simulator.isChemostat){
			throw new Exception("no Planktonics allowed in chemostat simulation. Please remove Planktonics form your chemostat protocol.");
			
		}else{
		if (root.getParam("planktonicTimeStep") == null){
			AGENTTIMESTEP = SimTimer.getCurrentTimeStep();
		}
		else{
		double value = agentTimeStep;

			if (AGENTTIMESTEP > SimTimer.getCurrentTimeStep()) {
				LogFile.writeLog("ERROR: Planktonic agentTimeStep in agentGrid markup MUST be "+
						"less than or equal to the global timestep\n"+
						"\tagentTimeStep was given as: "+AGENTTIMESTEP+"\n"+
						"\tglobal time step is currently: "+SimTimer.getCurrentTimeStep());
				throw new Exception("agentTimeStep too large");
			}
			LogFile.writeLog("Plantonic time step is... " + value);
		}


		// Reference to the domain where this container is defined
		domain = (Domain) aSimulator.world.getDomain(root.getParam("computationDomain"));
		mySim = aSimulator;
		agentList = new LinkedList<Planktonic>();
		agentIter = agentList.listIterator();
		// Optimised the resolution of the grid used to sort located agents
		System.out.println("checkgridsize");
		checkGridSize(aSimulator, root);


		// Initialise spatial grid used to display species distribution
		//createOutputGrid(aSimulator);


	   //LogFile.writeLog(" " + _nTotal + " grid elements, resolution: " + _res + " micrometers");
		
		}

		
		
	
	}
	
	

}

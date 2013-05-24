/**
 * 
 */
package simulator.agent.zoo;

import simulator.Simulator;
import simulator.SoluteGrid;
import simulator.geometry.ContinuousVector;
import utils.ExtraMath;
import utils.XMLParser;

/**
 * @author alexandraweston
 *
 */

public class ChemotaxingPlanktonic extends Planktonic {
	
	String chemoeffector;
	boolean repellent;
	double chem_threshold;//concentration of chemoeffector at which the agent moves chemotactically
	SoluteGrid chemotaxSolute;
	/**
	 * 
	 */
	public ChemotaxingPlanktonic() {
		// 
		super();
		
	}

	public void initFromProtocolFile(Simulator aSim, XMLParser aSpeciesRoot) {
		// Initialisation of Bacterium
		super.initFromProtocolFile(aSim, aSpeciesRoot);
		
		//Chemotaxis-specific protocol parameters
		
		chemoeffector =aSpeciesRoot.getParam("chemoeffector").trim();
		repellent = aSpeciesRoot.getParamBool("repellent");
		chem_threshold =aSpeciesRoot.getParamDbl("chem_threshold");

		init();
	}
	
	/**
	 * overrides Planktonic.determineNewLoc
	 * implements chemotaxis
	 */
	protected void determineNewLoc() {
		//get the specific soluteGrid
		if(chemotaxSolute == null){
			chemotaxSolute = this._agentGrid.mySim.soluteList[this._agentGrid.mySim.getSoluteIndex(chemoeffector)];
		}
		if(chemotaxSolute.getValueAt(_location)>chem_threshold){
			
				chemotax();
		}
		else{
			super.determineNewLoc();
		}
		
	}
	
	/**
	 * examines solute grid to determine where to
	 * move based on attraction/repulsion to/from 
	 * a solute
	 */
	private void chemotax() {

		

		
		//1) get the gradient--this will be the direction the particle 
		//should move in or away from
		
		ContinuousVector direction = chemotaxSolute.getGradient2DNoZ(_location);
		
		if(repellent){
			direction.turnAround();
		}
		
		//2) scale the gradient set the scaled gradient as _movement
		_movement = this.getScaledMove(_location, direction, distEachRun);
		
		
		
		
		
		
	}

}

/**
 * 
 */
package simulator.agent.zoo;

import simulator.Simulator;
import simulator.SoluteGrid;
import simulator.agent.Species;
import simulator.geometry.ContinuousVector;
import utils.ExtraMath;
import utils.XMLParser;

/**
 * @author alexandraweston
 * A Bactleaver can sense the local [chemoeffector], then leave the biofilm (becoming
 * a planktonic) with a certain probability if [chemoffector] is greater than a threshold
 *
 */
public class BactLeaver extends Bacterium {
	public boolean onBorder = false;
	public String becomesPlanktonicSpec;
	public double leavingProb;
	public double threshold;
	public String chemoeffector;
	
	
	public void initFromProtocolFile(Simulator aSim, XMLParser aSpeciesRoot) {
		// Initialisation of Bacterium
		super.initFromProtocolFile(aSim, aSpeciesRoot);
		
		//BactLeaver-specific initializations
		becomesPlanktonicSpec = aSpeciesRoot.getParam("becomesPlanktonicSpecies").trim();
		leavingProb = aSpeciesRoot.getParamDbl("leavingProbability");
		chemoeffector = aSpeciesRoot.getParam("chemoeffector").trim();
		threshold = aSpeciesRoot.getParamDbl("chemoeffectorThreshold");

		init();
	}

	
	/**
	 * A Bactleaver can sense the local [chemoeffector]
	 */
	public BactLeaver() {
		super();

	}
	
	protected void internalStep(){
		if(willLeave()){
			leaveBiofilm();
		}
		else{
			//reset, as this cell may not be on the border
			//the next time step, due to cell divisions
			onBorder=false;
			super.internalStep();
		}
	}
	/**
	 * Creates a new planktonic and kills
	 * the current biofilm cell
	 */
	private void leaveBiofilm() {
		//create new planktonic
		int index = _agentGrid.mySim.getSpeciesIndex(becomesPlanktonicSpec); 
		Species plankSpec = _agentGrid.mySim.speciesList.get(index);
		plankSpec.createPop(this._location);
		//The planktonic will add itself to the simulation
		
		this.die(false);
		
	}
	
	/**
	 * determines first if the cell is on the
	 * biofilm/liquid interface (the onBorder property is set by AgentContainer
	 * Every agent step before biofilm agents internal steps are called).
	 * Then, the concentration of repellant/quorum-sensing molecule at this index
	 * is checked. If it is above the threshold, the bacterium will become a planktonic
	 * with the given probability.
	 */
	private boolean willLeave() {
		boolean leave = false;
		double chemConc;
		double uRandom;
		if(onBorder){
			SoluteGrid chemotaxSolute = this._agentGrid.mySim.soluteList[this._agentGrid.mySim.getSoluteIndex(chemoeffector)];
			//get the [chemoeffecter]
			chemConc= chemotaxSolute.getValueAt(this._location);
			//System.err.println("[ai2] =" + chemConc);
			if(chemConc > threshold){
				//get a uniform random number--use for computing probability of
				//leaving
				uRandom = ExtraMath.getUniRand();
				if(uRandom < leavingProb){
					leave = true;
				System.err.println("leaving");
				}
				
			}
			
		}
		
		return leave;
	}

}

package simulator.agent.zoo;

import java.awt.Color;
import java.io.FileWriter;
import java.io.IOException;

import idyno.SimTimer;
import simulator.Simulator;
import simulator.agent.LocatedAgent;
import simulator.geometry.Bulk;
import simulator.geometry.boundaryConditions.AllBC;

//import utils.LogFile;
import utils.XMLParser;

public class BactAdaptableGrowth extends BactAdaptable {

	public BactAdaptableGrowth() {
		super();
		_speciesParam = new BactAdaptableGrowthParam();
	}
	
	public void respondToConditions() {

		double localValue=0;

		if(Simulator.isChemostat){
			if (getSpeciesParam().switchType.equals("solute")) {
			
				for (AllBC aBC : _agentGrid.domain.getAllBoundaries()){
					if (aBC.hasBulk()){
						Bulk aBulk = aBC.getBulk();
							if(aBulk.getName().equals("chemostat")){
								localValue = aBulk.getValue(getSpeciesParam()._soluteList[getSpeciesParam().switchControlIndex].soluteIndex);
							}
					}	
				}
				
				
			} else {
				// biomass
				localValue = getParticleMass(getSpeciesParam().switchControlIndex);
			}
			
		}else{
			// get the value needed to check the switch
			if (getSpeciesParam().switchType.equals("solute")) {
				localValue = getSpeciesParam().
						_soluteList[getSpeciesParam().switchControlIndex].
							getValueAround((LocatedAgent) this);
			} else if (getSpeciesParam().switchType.equals("biomass")) {
				// biomass
				localValue = getParticleMass(getSpeciesParam().switchControlIndex);
			} else if (getSpeciesParam().switchType.equals("growth")) {
				// changes as bacteria grows
				localValue = getNetGrowth();
			}
		
		}
		
		if ((switchIsOn() && !turnSwitchOff) || (!switchIsOn() && turnSwitchOn)) {
			// state is one of:
			// 		-ON and staying ON
			// 		-OFF and waiting to switch to ON
			// so test whether we need to turn it OFF or cancel the switch
			// (here the test is OPPOSITE of label)

			if (getSpeciesParam().switchCondition.equals("lessThan")) {
				if (localValue >= getSpeciesParam().switchValue) {
					if (switchIsOn()) {
						turnSwitchOff = true;
						timeOfRequestToSwitchOff = SimTimer.getCurrentTime();
					} else {
						turnSwitchOn = false;
					}
				}
			} else {
				if (localValue <= getSpeciesParam().switchValue) {
					if (switchIsOn()) {
						turnSwitchOff = true;
						timeOfRequestToSwitchOff = SimTimer.getCurrentTime();
					} else{
						turnSwitchOn = false;
					}
				}
			}
		} else {
			// state is one of:
			// 		-ON and waiting to switch to OFF
			// 		-OFF and staying OFF
			// so test whether we need to turn it ON or cancel the switch
			if (getSpeciesParam().switchCondition.equals("lessThan")) {
				if (localValue < getSpeciesParam().switchValue) {
					if (switchIsOn()) {
						turnSwitchOff = false;
					} else {
						turnSwitchOn = true;						
						timeOfRequestToSwitchOn = SimTimer.getCurrentTime();
					}
				}
			} else {
				if (localValue > getSpeciesParam().switchValue) {
					if (switchIsOn()) {
						turnSwitchOff = false;
					} else {
						turnSwitchOn = true;
						timeOfRequestToSwitchOn = SimTimer.getCurrentTime();
					}
				}
			}
		}
	}

	public BactAdaptableParam getSpeciesParam() {
		return (BactAdaptableGrowthParam) _speciesParam;
	}
	
	
}

package simulator.agent.zoo;

import java.util.ArrayList;

import org.jdom.Element;
import java.awt.Color;

import simulator.Simulator;
import simulator.SoluteGrid;
import utils.XMLParser;

public class BactAdaptableGrowthParam extends BactAdaptableParam {

	
	public BactAdaptableGrowthParam() {
		super();
	}

	public void init(Simulator aSim, XMLParser aSpeciesRoot){
		String colorName;

		super.init(aSim,aSpeciesRoot);

		// Now take care of reaction switch business
		int reacIndex;
		_soluteList = aSim.soluteList;

		onStateReactions = new ArrayList<Integer>();
		offStateReactions = new ArrayList<Integer>();

		XMLParser switchParser = new XMLParser(aSpeciesRoot.getChildElement("reactionSwitch"));
		XMLParser childParser;

		//////////////////////////////////////////////////////////////////////////
		// create list of reactions during the On state
		childParser = new XMLParser(switchParser.getChildElement("whenOn"));
		for (Element aReactionMarkUp : childParser.buildSetMarkUp("reaction")) {
			// Add the reaction to the list of reactions for the on-state
			// but add only ACTIVE reactions!!
			reacIndex = aSim.getReactionIndex(aReactionMarkUp.getAttributeValue("name"));
			if (aReactionMarkUp.getAttributeValue("status").equals("active")) {
				onStateReactions.add(reacIndex);

			}
		}

		colorName = childParser.getParam("color");
		if (colorName == null)
			colorName = "white";
		onColor = utils.UnitConverter.getColor(colorName);

		lagSwitchOn  = childParser.getParamTime("switchLag");


		//////////////////////////////////////////////////////////////////////////
		// create list of reactions during the Off state
		childParser = new XMLParser(switchParser.getChildElement("whenOff"));
		for (Element aReactionMarkUp : childParser.buildSetMarkUp("reaction")) {
			// Add the reaction to the list of reactions for the off-state
			// but add only ACTIVE reactions!!
			reacIndex = aSim.getReactionIndex(aReactionMarkUp.getAttributeValue("name"));
			if (aReactionMarkUp.getAttributeValue("status").equals("active")) {
				offStateReactions.add(reacIndex);
			}
		}

		colorName = childParser.getParam("color");
		if (colorName == null)
			colorName = "black";
		offColor = utils.UnitConverter.getColor(colorName);

		lagSwitchOff = childParser.getParamTime("switchLag");

		// now set up when the on and off states are set/met
		childParser = new XMLParser(switchParser.getChildElement("onCondition"));

		// get type (solute or biomass) and controlling component
		switchType = childParser.getAttribute("type");
		switchControl = childParser.getAttribute("name");

		// get whether it's lessThan or greaterThan
		switchCondition = childParser.getParam("switch");

		// get the concentration or mass for the switch
		if (switchType.equals("solute")) {
			switchValue = childParser.getParamConc("concentration");
			switchControlIndex = aSim.soluteDic.indexOf(switchControl);
			if (switchControlIndex == -1)
				System.out.println("WARNING: solute "+switchControl+
				" used in the <reactionSwitch> markup does not exist.");
		}
		if (switchType.equals("biomass")) {
			switchValue = childParser.getParamMass("mass");
			switchControlIndex = aSim.particleDic.indexOf(switchControl);
			if (switchControlIndex == -1)
				System.out.println("WARNING: biomass type "+switchControl+
				" used in the <reactionSwitch> markup does not exist.");

		}
		// switchValue remains "concentration" so we don't have to rewrite XMLparser, growth rate doesn't have a switchControlIndex
		if (switchType.equals("growth")) {
			switchValue = childParser.getParamConc("concentration");
	//		switchControlIndex = aSim.particleDic.indexOf(switchControl);
	//		if (switchControlIndex == -1)
	//			System.out.println("WARNING: growth type "+switchControl+
	//			" used in the <reactionSwitch> markup does not exist.");

		}
		if (switchType.equals("boundary")) {
			switchValue = childParser.getParamConc("concentration");
	//		switchControlIndex = aSim.particleDic.indexOf(switchControl);
	//		if (switchControlIndex == -1)
	//			System.out.println("WARNING: growth type "+switchControl+
	//			" used in the <reactionSwitch> markup does not exist.");

		}		
		
	}

}

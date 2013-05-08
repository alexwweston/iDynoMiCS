package simulator.agent.zoo;

import static org.junit.Assert.*;

import org.junit.Test;

public class ChemotaxingPlanktonicTest {

	public ChemotaxingPlanktonicTest() {
	}

	@Test
	public void testChemotaxingPlanktonic() {
		ChemotaxingPlanktonic chemotaxAgent = new ChemotaxingPlanktonic();
		if(chemotaxAgent.getClass().equals(Planktonic.class)){
			System.out.println("Chemotaxing agent is planktonic");
		}
		else{
			System.out.println("Chemotaxing agent is not planktonic");
		}
		
		Planktonic plankAgent = new Planktonic();
		if(plankAgent.getClass().equals(Planktonic.class)){
			System.out.println("Plank agent is planktonic");
		}
		else{
			System.out.println("Plank agent is not planktonic");
		}
		
		if (chemotaxAgent instanceof Planktonic){
			System.out.println("chemotax is planktonic");
		}
		
		if (plankAgent instanceof ChemotaxingPlanktonic){
			System.out.println("plank is chemotaxing");
		}
	}

}

/**
 * 
 */
package simulator.Planktonic;

import static org.junit.Assert.*;

import org.junit.Test;
import simulator.agent.zoo.*;

/**
 * @author alexandraweston
 *
 */
public class PlanktonicTest {

	public PlanktonicTest() {
	}

	/**
	 * Test method for {@link simulator.Planktonic.Planktonic#Planktonic()}.
	 */
	@Test
	public void testPlanktonic() {
		//fail("Not yet implemented");
		
		Bacterium myBact = new Bacterium();
		Planktonic myPlank = new Planktonic();
		if (myPlank instanceof Planktonic){
			System.out.println("myPlank is a Planktonic!");
		}
		if (myPlank.getClass().equals(Bacterium.class)){
			System.out.println("myPlank is a Bacterium!");
		}
		if (!myBact.getClass().equals(Planktonic.class)){
			System.out.println("myBact is a is a Bacterium!");
		}
		
		
	}

}

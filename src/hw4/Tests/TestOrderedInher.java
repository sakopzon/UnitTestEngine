package hw4.Tests;

import hw4.Solution.OOPAfter;
import hw4.Solution.OOPBefore;
import hw4.Solution.OOPTest;
import hw4.Solution.OOPTestClass;

import static hw4.Tests.TestFunctions.*;
/**
 * Created by elran on 08/01/17.
 */
@OOPTestClass(OOPTestClass.OOPTestClassType.ORDERED)
public class TestOrderedInher extends TestOrdered {

	@Override
	@OOPTest(order = 16)
	protected void test16() //we override
	{
		c=2;
	}

	@OOPTest(order = 18)
	private void test18()
	{
		shouldPass(2,c);//make sure this ran insted of father's function
	}

	@OOPBefore({"test17"})
	private void SbeforeTest17_1(){
		shouldPass(1,d);
		d++;
	}
	@OOPTest(order = 17)
	public void test17()
	{
		shouldPass(2,d);
		d++;
	}
	@OOPAfter({"test17"})
	public void SafterTest17_1()
	{
		shouldPass(3,d);
		d++;
	}
}

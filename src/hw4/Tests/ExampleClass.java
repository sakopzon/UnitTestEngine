package hw4.Tests;

import hw4.Provided.OOPAssertionFailure;
import hw4.Solution.OOPBefore;
import hw4.Solution.OOPTest;
import hw4.Solution.OOPTestClass;
import hw4.Solution.OOPUnitCore;

@OOPTestClass(OOPTestClass.OOPTestClassType.ORDERED)
public class ExampleClass {

    private int field = 0;

    @OOPBefore({"test1"})
    public void beforeFirstTest() {
        this.field = 123;
    }


    @OOPTest(order = 1)
    public void test1() throws OOPAssertionFailure {
        //this must run before the other test. must not throw an exception to succeed
        OOPUnitCore.assertEquals(123, this.field);
    }

    @OOPTest(order = 2)
    public void test2() throws OOPAssertionFailure {
        OOPUnitCore.assertEquals(321, this.field);
    }

}

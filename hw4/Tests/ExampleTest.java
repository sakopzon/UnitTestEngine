package hw4.Tests;

import hw4.Solution.OOPTestSummary;
import hw4.Solution.OOPUnitCore;
import org.junit.Test;

import static hw4.Solution.OOPUnitCore.assertEquals;


public class ExampleTest {

    @Test
    public void testForExample() {

        OOPTestSummary result = OOPUnitCore.runClass(ExampleClass.class);
        assertEquals(1, result.getNumSuccesses());
        assertEquals(1, result.getNumFailures());
        assertEquals(0, result.getNumErrors());
    }

}

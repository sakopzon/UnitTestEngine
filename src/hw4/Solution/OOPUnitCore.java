package hw4.Solution;

import hw4.Provided.OOPAssertionFailure;

public class OOPUnitCore {
	
	public static void assertEquals(Object expected, Object actual) {
		if(!expected.equals(actual))
			throw new OOPAssertionFailure(expected, actual);
	}
	
	public static OOPTestSummary runClass(Class<?> testClass) {
		return null;
	}

}

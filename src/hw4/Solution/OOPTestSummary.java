package hw4.Solution;

import java.util.Map;

import hw4.Provided.OOPResult;
import hw4.Provided.OOPResult.OOPTestResult;

public class OOPTestSummary {

	Map<String, OOPResult> testMap;
	
	OOPTestSummary (Map<String, OOPResult> testMap) {
		this.testMap = testMap;
	}
	
	public int getNumSuccesses() {
		return occurenceCounter(OOPTestResult.SUCCESS);
	}
	
	public int getNumFailures() {
		return occurenceCounter(OOPTestResult.FAILURE);
	}
	
	public int getNumErrors() {
		return occurenceCounter(OOPTestResult.ERROR);
	}
	
	private int occurenceCounter(OOPTestResult r) {
		return (int) testMap.values().stream().filter(i->i.getResultType().equals(r)).count();
	}
}

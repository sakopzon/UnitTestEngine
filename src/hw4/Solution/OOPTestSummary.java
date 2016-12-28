package hw4.Solution;

import java.util.Map;

import hw4.Provided.OOPResult;

public class OOPTestSummary {

	Map<String, OOPResult> testMap;
	
	OOPTestSummary (Map<String, OOPResult> testMap) {
		this.testMap = testMap;
	}
	
	public int getNumSuccesses() {
		return 0;
	}
	
	public int getNumFailures() {
		return 0;
	}
	
	public int getNumErrors() {
		return 0;
	}
}

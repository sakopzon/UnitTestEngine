package hw4.Solution;

import hw4.Provided.OOPResult;

public class OOPResultImpl implements OOPResult{

	OOPTestResult result;
	String message;
	
	@Override
	public OOPTestResult getResultType() {
		return result;
	}

	@Override
	public String getMessage() {
		return message;
	}

	@Override
	public boolean equals(Object ¢){
		return ¢ == null ? this == null : (¢ instanceof OOPResultImpl && ((OOPResultImpl)¢).equals(this));
	}
	
	private boolean equals(OOPResultImpl ¢){
		return getMessage().equals(¢.getMessage()) && getResultType().equals(¢.getResultType());
	}
}

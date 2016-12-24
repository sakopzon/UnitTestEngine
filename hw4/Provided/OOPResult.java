package hw4.Provided;

public interface OOPResult {

    OOPTestResult getResultType();

    String getMessage();

    boolean equals(Object obj);

    enum OOPTestResult {
        SUCCESS, FAILURE, ERROR
    }

}

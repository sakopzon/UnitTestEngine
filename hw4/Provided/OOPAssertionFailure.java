package hw4.Provided;

public class OOPAssertionFailure extends AssertionError {

    private static final long serialVersionUID = 1L;
    private Object expected;
    private Object actual;

    public OOPAssertionFailure(Object expected, Object actual) {
        this.expected = expected;
        this.actual = actual;
    }

    @Override
    public String getMessage() {
        return "expected: <" + expected + "> but was: <" + actual + ">";
    }

}

package Exceptions;

/**
 * Created by tt on 19.08.14.
 */
public class BadQueryDepthValueException extends Exception {
    @Override
    public String toString() {
        return "QueryDepth should be > 0";
    }
}

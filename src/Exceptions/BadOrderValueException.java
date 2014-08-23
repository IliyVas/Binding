package Exceptions;

/**
 * Created by tt on 23.08.14.
 */
public class BadOrderValueException extends Exception {
    @Override
    public String toString() {
        return "QueryDepth should be > 0";
    }
}

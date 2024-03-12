package kkadak.fujitsutask.exceptions;

/**
 * Custom Exception to indicate errors during creation of a fee rule
 */
public class IncompatibleFeeRuleException extends Exception {

    public IncompatibleFeeRuleException(String message) {

        super(message);
    }

}

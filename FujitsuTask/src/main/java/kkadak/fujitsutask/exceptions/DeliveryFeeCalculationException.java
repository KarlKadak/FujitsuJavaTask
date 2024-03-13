package kkadak.fujitsutask.exceptions;

/**
 * Custom Exception to indicate errors during delivery fee calculation
 */
public class DeliveryFeeCalculationException extends Exception {

    /**
     * Primary constructor
     *
     * @param message error message
     */
    public DeliveryFeeCalculationException(String message) {
        super(message);
    }
}

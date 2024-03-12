package kkadak.fujitsutask.enums;

/**
 * Used to specify the value type of the weather data metric used in an extra fee rule
 */
public enum ExtraFeeRuleValueType {

    /**
     * Beginning from the specified value
     */
    FROM,

    /**
     * Until the specified value
     */
    UNTIL,

    /**
     * A String value; phenomenon description or cloud coverage
     */
    PHENOMENON,

    /**
     * Used in case of a String translation error
     */
    UNKNOWN

}

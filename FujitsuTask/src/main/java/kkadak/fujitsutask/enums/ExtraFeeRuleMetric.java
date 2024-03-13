package kkadak.fujitsutask.enums;

/**
 * Used to specify the weather data metric used in an extra fee rule
 */
public enum ExtraFeeRuleMetric {

    /**
     * ATEF (air temperature extra fee)
     */
    AIRTEMP,

    /**
     * WSEF (wind speed extra fee)
     */
    WINDSPEED,

    /**
     * WPEF (weather phenomenon extra fee)
     */
    PHENOMENON,

    /**
     * Used in case of a String translation error
     */
    UNKNOWN
}

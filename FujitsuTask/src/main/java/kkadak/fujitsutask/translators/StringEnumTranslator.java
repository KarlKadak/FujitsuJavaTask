package kkadak.fujitsutask.translators;

import kkadak.fujitsutask.enums.City;
import kkadak.fujitsutask.enums.ExtraFeeRuleMetric;
import kkadak.fujitsutask.enums.ExtraFeeRuleValueType;
import kkadak.fujitsutask.enums.VehicleType;

/**
 * Used to translate between Strings and
 * {@link kkadak.fujitsutask.enums.City} or {@link kkadak.fujitsutask.enums.VehicleType} enums
 *
 * @see kkadak.fujitsutask.enums.City
 * @see kkadak.fujitsutask.enums.VehicleType
 */
public class StringEnumTranslator {

    /**
     * Used to translate user input String to {@link kkadak.fujitsutask.enums.City}
     *
     * @param cityStr user input city name String
     *                (any variation of "Tallinn", "TLN", "Tartu", "TRT", "Pärnu", "Parnu", "PRN")
     * @return respective {@link kkadak.fujitsutask.enums.City} in case of success
     * <p>
     * {@link kkadak.fujitsutask.enums.City}.UNKNOWN in case of error
     */
    public static City getCityFromStr(String cityStr) {

        // Input validation
        if (cityStr == null) return City.UNKNOWN;

        return switch (cityStr.toLowerCase()) {

            case "tallinn", "tln" -> City.TALLINN;
            case "tartu", "trt" -> City.TARTU;
            case "pärnu", "parnu", "prn" -> City.PARNU;
            default -> City.UNKNOWN;
        };
    }

    /**
     * Used to translate {@link kkadak.fujitsutask.enums.City} to human-readable String of the city's name
     *
     * @param city {@link kkadak.fujitsutask.enums.City} enum to be translated
     * @return human-readable String of the city's name
     * @throws IllegalArgumentException in case of undefined translation
     */
    public static String getStrFromCity(City city) {

        return switch (city) {

            case TALLINN -> "Tallinn";
            case TARTU -> "Tartu";
            case PARNU -> "Pärnu";
            default -> throw new IllegalArgumentException();
        };
    }

    /**
     * Used to translate user input String to {@link kkadak.fujitsutask.enums.VehicleType}
     *
     * @param vehicleTypeStr user input city name String (any variation of "car", "scooter", "bike")
     * @return respective {@link kkadak.fujitsutask.enums.VehicleType} in case of success
     * <p>
     * {@link kkadak.fujitsutask.enums.VehicleType}.UNKNOWN in case of error
     */
    public static VehicleType getVehicleTypeFromStr(String vehicleTypeStr) {

        // Input validation
        if (vehicleTypeStr == null) return VehicleType.UNKNOWN;

        return switch (vehicleTypeStr.toLowerCase()) {

            case "car" -> VehicleType.CAR;
            case "scooter" -> VehicleType.SCOOTER;
            case "bike" -> VehicleType.BIKE;
            default -> VehicleType.UNKNOWN;
        };

    }

    /**
     * Used to translate {@link kkadak.fujitsutask.enums.VehicleType} to human-readable String of the vehicle type
     *
     * @param vehicleType {@link kkadak.fujitsutask.enums.VehicleType} enum to be translated
     * @return human-readable String of the vehicle type
     * @throws IllegalArgumentException in case of undefined translation
     */
    public static String getStrFromVehicleType(VehicleType vehicleType) {

        return switch (vehicleType) {

            case CAR -> "car";
            case SCOOTER -> "scooter";
            case BIKE -> "bike";
            default -> throw new IllegalArgumentException();
        };

    }

    /**
     * Used to translate user input rule value type String to {@link kkadak.fujitsutask.enums.ExtraFeeRuleValueType}
     *
     * @param valueTypeStr user input rule value type String (any variation of "from", "until", "phenomenon")
     * @return respective {@link kkadak.fujitsutask.enums.ExtraFeeRuleValueType} in case of success
     * <p>
     * {@link kkadak.fujitsutask.enums.ExtraFeeRuleValueType}.UNKNOWN in case of error
     */
    public static ExtraFeeRuleValueType getValueTypeFromStr(String valueTypeStr) {

        // Input validation
        if (valueTypeStr == null) return ExtraFeeRuleValueType.UNKNOWN;

        return switch (valueTypeStr.toLowerCase()) {

            case "from" -> ExtraFeeRuleValueType.FROM;
            case "until" -> ExtraFeeRuleValueType.UNTIL;
            case "phenomenon" -> ExtraFeeRuleValueType.PHENOMENON;
            default -> ExtraFeeRuleValueType.UNKNOWN;
        };

    }

    /**
     * Used to translate user input rule metric String to {@link kkadak.fujitsutask.enums.ExtraFeeRuleMetric}
     *
     * @param metricStr user input rule metric String (any variation of "airtemp", "windspeed", "phenomenon")
     * @return respective {@link kkadak.fujitsutask.enums.ExtraFeeRuleMetric} in case of success
     * <p>
     * {@link kkadak.fujitsutask.enums.ExtraFeeRuleMetric}.UNKNOWN in case of error
     */
    public static ExtraFeeRuleMetric getMetricFromStr(String metricStr) {

        // Input validation
        if (metricStr == null) return ExtraFeeRuleMetric.UNKNOWN;

        return switch (metricStr.toLowerCase()) {

            case "airtemp" -> ExtraFeeRuleMetric.AIRTEMP;
            case "windspeed" -> ExtraFeeRuleMetric.WINDSPEED;
            case "phenomenon" -> ExtraFeeRuleMetric.PHENOMENON;
            default -> ExtraFeeRuleMetric.UNKNOWN;
        };

    }

}

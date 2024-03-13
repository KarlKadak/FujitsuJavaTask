package kkadak.fujitsutask.service;

import kkadak.fujitsutask.enums.City;
import kkadak.fujitsutask.enums.VehicleType;
import kkadak.fujitsutask.exceptions.DeliveryFeeCalculationException;

/**
 * Provides methods for calculating delivery fees
 * <p>
 * Defines method {@link #getDeliveryFee(City, VehicleType)}
 * for getting the total delivery fee with the most recent weather data
 * and method {@link #getDeliveryFee(City, VehicleType, long)}
 * for getting the total delivery fee for a specified time in the past (measured in seconds past UTC epoch)
 */
public interface DeliveryService {

    /**
     * Calculates the current delivery fee based on the given city and vehicle type
     * <p>
     * In case of error throws a {@link kkadak.fujitsutask.exceptions.DeliveryFeeCalculationException}
     *
     * @param city        the {@link kkadak.fujitsutask.enums.City} for delivery
     * @param vehicleType the {@link kkadak.fujitsutask.enums.VehicleType} used for the delivery
     * @return the total delivery fee
     * @throws DeliveryFeeCalculationException in case of error
     */
    double getDeliveryFee(City city, VehicleType vehicleType) throws DeliveryFeeCalculationException;

    /**
     * Calculates the current delivery fee based on the given city and vehicle type for the given time
     * <p>
     * In case of error throws a {@link kkadak.fujitsutask.exceptions.DeliveryFeeCalculationException}
     *
     * @param city        the {@link kkadak.fujitsutask.enums.City} for delivery
     * @param vehicleType the {@link kkadak.fujitsutask.enums.VehicleType} used for the delivery
     * @param timestamp   the timestamp for the calculation data (measured in seconds past UTC epoch)
     * @return the total delivery fee
     * @throws DeliveryFeeCalculationException in case of error
     */
    double getDeliveryFee(City city, VehicleType vehicleType, long timestamp) throws DeliveryFeeCalculationException;
}

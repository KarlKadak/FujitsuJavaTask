package kkadak.fujitsutask.model;

import jakarta.persistence.Entity;
import kkadak.fujitsutask.enums.City;
import kkadak.fujitsutask.enums.VehicleType;

/**
 * A single base fee rule for the fee calculation
 */
@Entity
public class BaseFeeRule extends FeeRule {

    /**
     * Specifies the city which the rule applies in
     */
    private City city;

    public City getCity() {
        return city;
    }

    public void setCity(City city) { this.city = city; }

    /**
     * Required for JPA
     */
    protected BaseFeeRule() {
    }

    /**
     * Primary constructor
     *
     * @param city        {@link kkadak.fujitsutask.enums.City} which the rule applies in
     * @param vehicleType {@link kkadak.fujitsutask.enums.VehicleType} which the rule applies for
     * @param feeAmount   fee amount, null if the use of specified vehicle in specified city is prohibited
     */
    public BaseFeeRule(City city, VehicleType vehicleType, Double feeAmount) {

        this.city = city;
        this.setVehicleType(vehicleType);
        this.setFeeAmount(feeAmount);
    }

}

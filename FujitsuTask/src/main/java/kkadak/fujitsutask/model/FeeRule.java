package kkadak.fujitsutask.model;

import jakarta.persistence.*;
import kkadak.fujitsutask.enums.VehicleType;

/**
 * A single rule for the fee calculation
 */
@Entity
// Specifies a table per concrete class in the database, separates base fee rules table and extra fee rules table
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@SequenceGenerator(name = "fee_rule_seq", sequenceName = "fee_rule_seq", allocationSize = 1)
public abstract class FeeRule {

    /**
     * Primary key for the rule in the corresponding fee rule table
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "fee_rule_seq")
    private Long id;

    /**
     * Specifies the vehicle type the rule applies for
     */
    private VehicleType vehicleType;

    /**
     * Fee amount
     * <p>
     * In case of null value, use of vehicle type is prohibited in given conditions
     */
    private Double feeAmount;

    /**
     * Amount of seconds past UTC epoch when the rule was set
     */
    private Long validFromTimestamp;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(VehicleType vehicleType) {
        this.vehicleType = vehicleType;
    }

    public Double getFeeAmount() {
        return feeAmount;
    }

    public void setFeeAmount(Double feeAmount) {
        this.feeAmount = feeAmount;
    }

    public Long getValidFromTimestamp() {
        return validFromTimestamp;
    }

    public void setValidFromTimestamp(Long timestamp) {
        this.validFromTimestamp = timestamp;
    }
}

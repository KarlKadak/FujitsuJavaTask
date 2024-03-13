package kkadak.fujitsutask.model;

import jakarta.persistence.Entity;
import kkadak.fujitsutask.enums.ExtraFeeRuleMetric;
import kkadak.fujitsutask.enums.ExtraFeeRuleValueType;
import kkadak.fujitsutask.enums.VehicleType;

import java.time.Instant;

/**
 * A single extra fee rule for the fee calculation
 */
@Entity
public class ExtraFeeRule extends FeeRule {

    /**
     * Specifies which metric the rule applies for
     *
     * @see kkadak.fujitsutask.enums.ExtraFeeRuleMetric
     */
    private ExtraFeeRuleMetric metric;

    /**
     * Specifies the value type of metric the rule applies for
     *
     * @see kkadak.fujitsutask.enums.ExtraFeeRuleValueType
     */
    private ExtraFeeRuleValueType valueType;

    /**
     * The value of the metric the rule applies for
     */
    private String valueStr;

    /**
     * Amount of seconds past UTC epoch when the rule was disabled
     */
    private Long validUntilTimestamp;

    public ExtraFeeRuleMetric getMetric() {
        return metric;
    }

    public void setMetric(ExtraFeeRuleMetric metric) {
        this.metric = metric;
    }

    public ExtraFeeRuleValueType getValueType() {
        return valueType;
    }

    public void setValueType(ExtraFeeRuleValueType valueType) {
        this.valueType = valueType;
    }

    public String getValueStr() {
        return valueStr;
    }

    public void setValueStr(String valueStr) {
        this.valueStr = valueStr;
    }

    public Long getValidUntilTimestamp() {
        return validUntilTimestamp;
    }

    public void setValidUntilTimestamp(Long validUntilTimestamp) {
        this.validUntilTimestamp = validUntilTimestamp;
    }

    /**
     * Required for JPA
     */
    protected ExtraFeeRule() {
    }

    /**
     * Constructor for creating an extra fee rule with a numeric value
     *
     * @param metric      {@link kkadak.fujitsutask.enums.ExtraFeeRuleMetric} the rule applies for
     * @param valueType   {@link kkadak.fujitsutask.enums.ExtraFeeRuleValueType} type of the metric the rule applies for
     * @param value       value of the metric the rule applies for
     * @param vehicleType {@link kkadak.fujitsutask.enums.VehicleType} which the rule applies for
     * @param feeAmount   fee amount, null if the use of specified vehicle is prohibited in the conditions
     */
    public ExtraFeeRule(ExtraFeeRuleMetric metric, ExtraFeeRuleValueType valueType,
                        double value, VehicleType vehicleType, Double feeAmount) {
        this.metric = metric;
        this.valueType = valueType;
        this.valueStr = String.format("%f", value);
        this.setVehicleType(vehicleType);
        this.setFeeAmount(feeAmount);
        this.setValidFromTimestamp(Instant.now().getEpochSecond());
    }

    /**
     * Constructor for creating an extra fee rule with a weather phenomenon value
     *
     * @param phenomenon  phenomenon description or cloud coverage
     * @param vehicleType {@link kkadak.fujitsutask.enums.VehicleType} which the rule applies for
     * @param feeAmount   fee amount, null if the use of specified vehicle is prohibited in the conditions
     */
    public ExtraFeeRule(String phenomenon, VehicleType vehicleType, Double feeAmount) {
        this.metric = ExtraFeeRuleMetric.PHENOMENON;
        this.valueType = ExtraFeeRuleValueType.PHENOMENON;
        this.valueStr = phenomenon;
        this.setVehicleType(vehicleType);
        this.setFeeAmount(feeAmount);
        this.setValidFromTimestamp(Instant.now().getEpochSecond());
    }
}

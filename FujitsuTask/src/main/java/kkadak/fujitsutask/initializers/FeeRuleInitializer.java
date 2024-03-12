package kkadak.fujitsutask.initializers;

import kkadak.fujitsutask.enums.City;
import kkadak.fujitsutask.enums.ExtraFeeRuleMetric;
import kkadak.fujitsutask.enums.ExtraFeeRuleValueType;
import kkadak.fujitsutask.enums.VehicleType;
import kkadak.fujitsutask.exceptions.IncompatibleFeeRuleException;
import kkadak.fujitsutask.model.BaseFeeRule;
import kkadak.fujitsutask.model.ExtraFeeRule;
import kkadak.fujitsutask.repository.BaseFeeRuleRepository;
import kkadak.fujitsutask.repository.ExtraFeeRuleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Used to create new fee rules or initialize them to the default values
 */
@Component
public class FeeRuleInitializer {

    private final BaseFeeRuleRepository baseFeeRuleRepository;
    private final ExtraFeeRuleRepository extraFeeRuleRepository;

    @Autowired
    public FeeRuleInitializer(BaseFeeRuleRepository baseFeeRuleRepository,
                              ExtraFeeRuleRepository extraFeeRuleRepository) {

        this.baseFeeRuleRepository = baseFeeRuleRepository;
        this.extraFeeRuleRepository = extraFeeRuleRepository;
    }

    /**
     * Attempts to set a new base fee rule in the BaseFeeRule table
     * <p>
     * In case of existence of conflicting rule throws
     * {@link kkadak.fujitsutask.exceptions.IncompatibleFeeRuleException}
     *
     * @param city        {@link kkadak.fujitsutask.enums.City} in which the rule applies
     * @param vehicleType {@link kkadak.fujitsutask.enums.VehicleType} for which the rule applies
     * @param feeAmount   the amount of the fee, null if the use of specified vehicle in specified city is prohibited
     * @throws IncompatibleFeeRuleException in case of existence of conflicting rule or invalid parameters
     */
    public void InitializeNewRule(City city, VehicleType vehicleType, Double feeAmount)
            throws IncompatibleFeeRuleException {

        // Check parameter validity
        if (city == null
                || city == City.UNKNOWN
                || vehicleType == null
                || vehicleType == VehicleType.UNKNOWN
                || (feeAmount != null && feeAmount <= 0)) {

            throw new IncompatibleFeeRuleException("Invalid rule parameter(s)");
        }

        // Check existence of rule
        if (baseFeeRuleRepository.getBaseFeeRuleByCityAndVehicleType(city, vehicleType) != null)
            throw new IncompatibleFeeRuleException("Rule for specified vehicle type in specified city already exists");

        // Save new rule
        baseFeeRuleRepository.save(new BaseFeeRule(city, vehicleType, feeAmount));
    }

    /**
     * Attempts to set a new extra fee rule with numeric value in the ExtraFeeRule table
     * <p>
     * In case of existence of conflicting rule throws
     * {@link kkadak.fujitsutask.exceptions.IncompatibleFeeRuleException}
     *
     * @param ruleMetric  metric to be used, see {@link kkadak.fujitsutask.enums.ExtraFeeRuleMetric}
     * @param valueType   value type to be used, see {@link kkadak.fujitsutask.enums.ExtraFeeRuleValueType}
     * @param value       specifies the value itself
     * @param vehicleType {@link kkadak.fujitsutask.enums.VehicleType} for which the rule applies
     * @param feeAmount   the amount of the fee, null if the use of specified vehicle is prohibited in the conditions
     * @throws IncompatibleFeeRuleException in case of existence of conflicting rule or invalid parameters
     */
    public void InitializeNewRule(ExtraFeeRuleMetric ruleMetric,
                                  ExtraFeeRuleValueType valueType,
                                  double value, VehicleType vehicleType,
                                  Double feeAmount)
            throws IncompatibleFeeRuleException {

        // Check parameter validity
        if (vehicleType == null
                || vehicleType == VehicleType.UNKNOWN
                || ruleMetric == ExtraFeeRuleMetric.PHENOMENON
                || valueType == ExtraFeeRuleValueType.PHENOMENON
                || (feeAmount != null && feeAmount <= 0))
            throw new IncompatibleFeeRuleException("Invalid rule parameter(s)");

        // Check conflicting rules
        for (ExtraFeeRule existingRule : extraFeeRuleRepository
                .getExtraFeeRulesByVehicleType(vehicleType).stream()
                .filter(rule -> rule.getMetric() == ruleMetric).toList()) {

            ExtraFeeRuleValueType existingType = existingRule.getValueType();
            if ((existingType == valueType && value == Double.parseDouble(existingRule.getValueStr()))
                    || (existingType == ExtraFeeRuleValueType.UNTIL
                        && valueType == ExtraFeeRuleValueType.FROM
                        && Double.parseDouble(existingRule.getValueStr()) >= value)
                    || (existingType == ExtraFeeRuleValueType.FROM
                        && valueType == ExtraFeeRuleValueType.UNTIL
                        && Double.parseDouble(existingRule.getValueStr()) <= value))
                throw new IncompatibleFeeRuleException(String
                        .format("Conflicting rule (ID: %d)", existingRule.getId()));
        }

        // Save new rule
        extraFeeRuleRepository.save(new ExtraFeeRule(ruleMetric, valueType, value, vehicleType, feeAmount));
    }

    /**
     * Attempts to set a new extra fee rule with String value in the ExtraFeeRule table
     * <p>
     * In case of existence of conflicting rule throws
     * {@link kkadak.fujitsutask.exceptions.IncompatibleFeeRuleException}
     *
     * @param phenomenon  phenomenon for which the rule applies
     * @param vehicleType {@link kkadak.fujitsutask.enums.VehicleType} for which the rule applies
     * @param amount      the amount of the fee, null if the use of specified vehicle is prohibited in the conditions
     * @throws IncompatibleFeeRuleException in case of existence of conflicting rule or invalid parameters
     */
    public void InitializeNewRule(String phenomenon, VehicleType vehicleType, Double amount)
            throws IncompatibleFeeRuleException {

        // Check parameter validity
        if (vehicleType == null
                || vehicleType == VehicleType.UNKNOWN
                || phenomenon == null
                || phenomenon.isEmpty()
                || (amount != null && amount <= 0))
            throw new IncompatibleFeeRuleException("Invalid rule parameter(s)");

        // Check conflicting rules
        for (ExtraFeeRule rule : extraFeeRuleRepository.getExtraFeeRulesByVehicleType(vehicleType)) {

            if (rule.getValueType() == ExtraFeeRuleValueType.PHENOMENON
                    && Objects.equals(rule.getValueStr(), phenomenon))
                throw new IncompatibleFeeRuleException(String.format("Conflicting rule (ID: %d)", rule.getId()));
        }

        // Save new rule
        extraFeeRuleRepository.save(new ExtraFeeRule(phenomenon, vehicleType, amount));
    }

    /**
     * Initializes the fee rules to their default values in the fee rule tables
     */
    public void InitializeDefaultRules() {

        final List<BaseFeeRule> baseFeeRules = new ArrayList<>() {
            {
                // Base fees for Tallinn
                add(new BaseFeeRule(City.TALLINN, VehicleType.CAR, 4.0));
                add(new BaseFeeRule(City.TALLINN, VehicleType.SCOOTER, 3.5));
                add(new BaseFeeRule(City.TALLINN, VehicleType.BIKE, 3.0));

                // Base fees for Tartu
                add(new BaseFeeRule(City.TARTU, VehicleType.CAR, 3.5));
                add(new BaseFeeRule(City.TARTU, VehicleType.SCOOTER, 3.0));
                add(new BaseFeeRule(City.TARTU, VehicleType.BIKE, 2.5));

                // Base fees for Pärnu
                add(new BaseFeeRule(City.PARNU, VehicleType.CAR, 3.0));
                add(new BaseFeeRule(City.PARNU, VehicleType.SCOOTER, 2.5));
                add(new BaseFeeRule(City.PARNU, VehicleType.BIKE, 2.0));
            }
        };

        final List<ExtraFeeRule> extraFeeRules = new ArrayList<>() {
            {
                // Extra fees for air temperature
                add(new ExtraFeeRule(ExtraFeeRuleMetric.AIRTEMP, ExtraFeeRuleValueType.UNTIL,
                        -10.0, VehicleType.SCOOTER, 1.0));
                add(new ExtraFeeRule(ExtraFeeRuleMetric.AIRTEMP, ExtraFeeRuleValueType.UNTIL,
                        -10.0, VehicleType.BIKE, 1.0));
                add(new ExtraFeeRule(ExtraFeeRuleMetric.AIRTEMP, ExtraFeeRuleValueType.UNTIL,
                        0.0, VehicleType.SCOOTER, 0.5));
                add(new ExtraFeeRule(ExtraFeeRuleMetric.AIRTEMP, ExtraFeeRuleValueType.UNTIL,
                        0.0, VehicleType.BIKE, 0.5));

                // Extra fees for wind speed
                add(new ExtraFeeRule(ExtraFeeRuleMetric.WINDSPEED, ExtraFeeRuleValueType.FROM,
                        10.0, VehicleType.BIKE, 0.5));
                add(new ExtraFeeRule(ExtraFeeRuleMetric.WINDSPEED, ExtraFeeRuleValueType.FROM,
                        20.0, VehicleType.BIKE, null));

                // Extra fees for weather phenomenons

                // Snow
                add(new ExtraFeeRule("Light snow shower",
                        VehicleType.BIKE, 1.0));
                add(new ExtraFeeRule("Moderate snow shower",
                        VehicleType.BIKE, 1.0));
                add(new ExtraFeeRule("Heavy snow shower",
                        VehicleType.BIKE, 1.0));
                add(new ExtraFeeRule("Light sleet",
                        VehicleType.BIKE, 1.0));
                add(new ExtraFeeRule("Moderate sleet",
                        VehicleType.BIKE, 1.0));
                add(new ExtraFeeRule("Light snowfall",
                        VehicleType.BIKE, 1.0));
                add(new ExtraFeeRule("Moderate snowfall",
                        VehicleType.BIKE, 1.0));
                add(new ExtraFeeRule("Heavy snowfall",
                        VehicleType.BIKE, 1.0));
                add(new ExtraFeeRule("Blowing snow",
                        VehicleType.BIKE, 1.0));
                add(new ExtraFeeRule("Drifting snow",
                        VehicleType.BIKE, 1.0));

                // Rain
                add(new ExtraFeeRule("Light shower",
                        VehicleType.BIKE, 0.5));
                add(new ExtraFeeRule("Moderate shower",
                        VehicleType.BIKE, 0.5));
                add(new ExtraFeeRule("Heavy shower",
                        VehicleType.BIKE, 0.5));
                add(new ExtraFeeRule("Light rain",
                        VehicleType.BIKE, 0.5));
                add(new ExtraFeeRule("Moderate rain",
                        VehicleType.BIKE, 0.5));
                add(new ExtraFeeRule("Heavy rain",
                        VehicleType.BIKE, 0.5));

                // Glaze, hail, thunder
                add(new ExtraFeeRule("Glaze",
                        VehicleType.BIKE, null));
                add(new ExtraFeeRule("Hail",
                        VehicleType.BIKE, null));
                add(new ExtraFeeRule("Thunder",
                        VehicleType.BIKE, null));
                add(new ExtraFeeRule("Thunderstorm",
                        VehicleType.BIKE, null));
            }
        };

        baseFeeRuleRepository.deleteAll();
        extraFeeRuleRepository.deleteAll();

        baseFeeRuleRepository.saveAll(baseFeeRules);
        extraFeeRuleRepository.saveAll(extraFeeRules);
    }

}

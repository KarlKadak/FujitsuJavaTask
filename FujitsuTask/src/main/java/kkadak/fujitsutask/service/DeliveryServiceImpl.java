package kkadak.fujitsutask.service;

import kkadak.fujitsutask.enums.City;
import kkadak.fujitsutask.enums.ExtraFeeRuleMetric;
import kkadak.fujitsutask.enums.ExtraFeeRuleValueType;
import kkadak.fujitsutask.enums.VehicleType;
import kkadak.fujitsutask.exceptions.DeliveryFeeCalculationException;
import kkadak.fujitsutask.model.BaseFeeRule;
import kkadak.fujitsutask.model.ExtraFeeRule;
import kkadak.fujitsutask.model.WeatherData;
import kkadak.fujitsutask.repository.BaseFeeRuleRepository;
import kkadak.fujitsutask.repository.ExtraFeeRuleRepository;
import kkadak.fujitsutask.repository.WeatherDataRepository;
import kkadak.fujitsutask.translators.WeatherStationTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of the {@link kkadak.fujitsutask.service.DeliveryService} interface
 */
@Service
public class DeliveryServiceImpl implements DeliveryService {
    private final WeatherDataRepository weatherDataRepository;
    private final BaseFeeRuleRepository baseFeeRuleRepository;
    private final ExtraFeeRuleRepository extraFeeRuleRepository;

    @Autowired
    public DeliveryServiceImpl(WeatherDataRepository weatherDataRepository,
                               BaseFeeRuleRepository baseFeeRuleRepository,
                               ExtraFeeRuleRepository extraFeeRuleRepository) {
        this.weatherDataRepository = weatherDataRepository;
        this.baseFeeRuleRepository = baseFeeRuleRepository;
        this.extraFeeRuleRepository = extraFeeRuleRepository;
    }

    @Override
    public double getDeliveryFee(City city, VehicleType vehicleType) throws DeliveryFeeCalculationException {
        return getBaseFee(city, vehicleType, null) + getExtraFee(city, vehicleType, null);
    }

    @Override
    public double getDeliveryFee(City city, VehicleType vehicleType, long timestamp)
            throws DeliveryFeeCalculationException {
        return getBaseFee(city, vehicleType, timestamp) + getExtraFee(city, vehicleType, timestamp);
    }

    /**
     * Returns the base fee for currently set rules (if timestamp is null) or rules active at timestamp (if set)
     * <p>
     * In case of error throws a {@link kkadak.fujitsutask.exceptions.DeliveryFeeCalculationException}
     *
     * @param city        the {@link kkadak.fujitsutask.enums.City} for delivery
     * @param vehicleType the {@link kkadak.fujitsutask.enums.VehicleType} used for the delivery
     * @param timestamp   the timestamp for the calculation data (measured in seconds past UTC epoch) or null
     * @return the total delivery fee from base fee rules
     * @throws DeliveryFeeCalculationException in case of error
     */
    private double getBaseFee(City city, VehicleType vehicleType, Long timestamp)
            throws DeliveryFeeCalculationException {
        Optional<BaseFeeRule> rule;
        if (timestamp == null)
            rule = baseFeeRuleRepository.findTopByCityAndVehicleTypeOrderByValidFromTimestampDesc(city, vehicleType);
        else
            rule = baseFeeRuleRepository
                    .findTopByCityAndVehicleTypeAndValidFromTimestampLessThanEqualOrderByValidFromTimestampDesc(city,
                            vehicleType, timestamp);

        // Handle vehicle not allowed
        if (rule.isEmpty() || rule.get().getFeeAmount() == null)
            throw new DeliveryFeeCalculationException("Use of selected vehicle is not allowed in specified city");

        return rule.get().getFeeAmount();
    }

    /**
     * Returns the extra fee for currently set rules (if timestamp is null) or rules active at timestamp (if set)
     * <p>
     * In case of error throws a {@link kkadak.fujitsutask.exceptions.DeliveryFeeCalculationException}
     *
     * @param city        the {@link kkadak.fujitsutask.enums.City} for delivery
     * @param vehicleType the {@link kkadak.fujitsutask.enums.VehicleType} used for the delivery
     * @param timestamp   the timestamp for the calculation data (measured in seconds past UTC epoch) or null
     * @return the total delivery fee from extra fee rules
     * @throws DeliveryFeeCalculationException in case of error
     */
    private double getExtraFee(City city, VehicleType vehicleType, Long timestamp)
            throws DeliveryFeeCalculationException {
        List<ExtraFeeRule> rules;
        WeatherData weatherData;

        // Get either most recent weather data and rules for now or for a specified timestamp
        if (timestamp == null) {
            weatherData = weatherDataRepository
                    .getTopByStationWmoOrderByTimestampDesc(WeatherStationTranslator.getWmoOfCity(city));
            rules = extraFeeRuleRepository.getRules(vehicleType);
        } else {
            weatherData = weatherDataRepository
                    .getTopByStationWmoAndTimestampLessThanEqualOrderByTimestampDesc(WeatherStationTranslator
                            .getWmoOfCity(city), timestamp);
            rules = extraFeeRuleRepository.getRules(vehicleType, timestamp);
        }

        // Handle weather data missing
        // Only happens when the date selected is before the start of weather data gathering (initial application start)
        // or when weather data source has been unavailable since initial application start
        if (weatherData == null) throw new DeliveryFeeCalculationException("No valid weather data recorded");
        double extraFeeTotal = 0;

        // Calculate extra fees from numeric metrics
        for (ExtraFeeRuleMetric metric : ExtraFeeRuleMetric.values()) {

            // Skip phenomenon metric assessment
            if (metric == ExtraFeeRuleMetric.PHENOMENON) continue;
            boolean applicableFromExists = false, applicableUntilExists = false;
            Double fromRuleFeeTotal = null, untilRuleFeeTotal = null;

            // Process 'FROM' rules in descending order of value
            for (ExtraFeeRule rule : rules.stream()
                    .filter(rule -> rule.getMetric() == metric && rule.getValueType() == ExtraFeeRuleValueType.FROM)
                    .sorted((o1, o2) -> Double.compare(Double.parseDouble(o2.getValueStr()),
                            Double.parseDouble(o1.getValueStr()))).toList()) {

                // If value in gathered data is higher than or equal to the rule's value
                if ((metric == ExtraFeeRuleMetric.AIRTEMP
                            && weatherData.getAirTemp() >= Double.parseDouble(rule.getValueStr()))
                        || (metric == ExtraFeeRuleMetric.WINDSPEED
                            && weatherData.getWindSpeed() >= Double.parseDouble(rule.getValueStr()))) {
                    applicableFromExists = true;
                    fromRuleFeeTotal = rule.getFeeAmount();
                    break;
                }
            }

            // Process 'UNTIL' rules in ascending order of value
            for (ExtraFeeRule rule : rules.stream()
                    .filter(rule -> rule.getMetric() == metric && rule.getValueType() == ExtraFeeRuleValueType.UNTIL)
                    .sorted(Comparator.comparingDouble(o -> Double.parseDouble(o.getValueStr()))).toList()) {

                // If value in gathered data is lower than or equal to the rule's value
                if ((metric == ExtraFeeRuleMetric.AIRTEMP
                            && weatherData.getAirTemp() <= Double.parseDouble(rule.getValueStr()))
                        || (metric == ExtraFeeRuleMetric.WINDSPEED
                            && weatherData.getWindSpeed() <= Double.parseDouble(rule.getValueStr()))) {
                    applicableUntilExists = true;
                    untilRuleFeeTotal = rule.getFeeAmount();
                    break;
                }
            }

            if (applicableFromExists && fromRuleFeeTotal == null || applicableUntilExists && untilRuleFeeTotal == null)
                throw new DeliveryFeeCalculationException("Usage of selected vehicle type is currently forbidden");
            if (fromRuleFeeTotal != null) extraFeeTotal += fromRuleFeeTotal;
            if (untilRuleFeeTotal != null) extraFeeTotal += untilRuleFeeTotal;
        }

        // Calculate extra fees from phenomenon metric
        Optional<ExtraFeeRule> applicableRule = rules.stream()
                .filter(rule -> rule.getValueStr().equalsIgnoreCase(weatherData.getPhenomenon())).findFirst();
        Double phenomenonFeeTotal = 0.0;
        if (applicableRule.isPresent()) {
            phenomenonFeeTotal = applicableRule.get().getFeeAmount();
            if (phenomenonFeeTotal == null)
                throw new DeliveryFeeCalculationException("Usage of selected vehicle type is currently forbidden");
        }

        extraFeeTotal += phenomenonFeeTotal;
        return extraFeeTotal;
    }
}

package kkadak.fujitsutask.controller;

import kkadak.fujitsutask.enums.City;
import kkadak.fujitsutask.enums.ExtraFeeRuleMetric;
import kkadak.fujitsutask.enums.ExtraFeeRuleValueType;
import kkadak.fujitsutask.enums.VehicleType;
import kkadak.fujitsutask.initializers.FeeRuleInitializer;
import kkadak.fujitsutask.model.BaseFeeRule;
import kkadak.fujitsutask.model.ExtraFeeRule;
import kkadak.fujitsutask.repository.BaseFeeRuleRepository;
import kkadak.fujitsutask.repository.ExtraFeeRuleRepository;
import kkadak.fujitsutask.service.DeliveryServiceImpl;
import kkadak.fujitsutask.translators.StringEnumTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

/**
 * REST controller for the delivery fee calculation and rules management
 */
@RestController
public class DeliveryController {

    private final DeliveryServiceImpl deliveryService;
    private final FeeRuleInitializer feeRuleInitializer;
    private final BaseFeeRuleRepository baseFeeRuleRepository;
    private final ExtraFeeRuleRepository extraFeeRuleRepository;

    @Autowired
    public DeliveryController(DeliveryServiceImpl deliveryService,
                              FeeRuleInitializer feeRuleInitializer,
                              BaseFeeRuleRepository baseFeeRuleRepository,
                              ExtraFeeRuleRepository extraFeeRuleRepository) {

        this.deliveryService = deliveryService;
        this.feeRuleInitializer = feeRuleInitializer;
        this.baseFeeRuleRepository = baseFeeRuleRepository;
        this.extraFeeRuleRepository = extraFeeRuleRepository;
    }

    /**
     * REST endpoint handler for delivery fee calculation and rule management
     *
     * @param modeStr      optional parameter to specify action mode for rule management,
     *                     in case missing, default fee calculation is assumed, valid options are:
     *                     "reset", "print", "remove", "add"
     * @param idStr        specifies the ID of the rule to be removed in case modeStr is set to "remove"
     * @param typeStr      specifies the type of rule to be added in case modeStr is set to "add",
     *                     valid options are: "base", "from", "until", "phenomenon",
     *                     translated to {@link kkadak.fujitsutask.enums.ExtraFeeRuleValueType} in case of extra fee
     *                     using {@link kkadak.fujitsutask.translators.StringEnumTranslator}
     * @param metricStr    specifies the metric for which the rule to be added applies,
     *                     translated to {@link kkadak.fujitsutask.enums.ExtraFeeRuleMetric}
     *                     using {@link kkadak.fujitsutask.translators.StringEnumTranslator}
     * @param valueStr     the value from/until/to which the rule to be added applies
     * @param amountStr    the fee amount for the rule to be added
     * @param cityStr      the city for the price calculation or in which the base rule to be added applies,
     *                     translated to {@link kkadak.fujitsutask.enums.City}
     *                     using {@link kkadak.fujitsutask.translators.StringEnumTranslator}
     * @param vehicleStr   the vehicle for the price calculation or for which rule to be added applies,
     *                     translated to {@link kkadak.fujitsutask.enums.VehicleType}
     *                     using {@link kkadak.fujitsutask.translators.StringEnumTranslator}
     * @param timestampStr the timestamp to be used in case a query is made for the past, measured in seconds past epoch
     * @return the response output, delivery fee calculation result in case mode is unset
     * or other output in case of rule management
     */
    @GetMapping("/getFee")
    public String getFee(@RequestParam(value = "mode", required = false) String modeStr,
                         @RequestParam(value = "id", required = false) String idStr,
                         @RequestParam(value = "type", required = false) String typeStr,
                         @RequestParam(value = "metric", required = false) String metricStr,
                         @RequestParam(value = "value", required = false) String valueStr,
                         @RequestParam(value = "amount", required = false) String amountStr,
                         @RequestParam(value = "city", required = false) String cityStr,
                         @RequestParam(value = "vehicle", required = false) String vehicleStr,
                         @RequestParam(value = "time", required = false) String timestampStr) {

        City selectedCity = City.UNKNOWN;
        VehicleType selectedVehicle = VehicleType.UNKNOWN;

        // Translate cityStr
        if (cityStr != null) {

            selectedCity = StringEnumTranslator.getCityFromStr(cityStr);

            if (selectedCity == City.UNKNOWN) return "Unknown value for 'city' parameter";
        }

        // Translate vehicleStr
        if (vehicleStr != null) {

            selectedVehicle = StringEnumTranslator.getVehicleTypeFromStr(vehicleStr);

            if (selectedVehicle == VehicleType.UNKNOWN) return "Unknown value for 'vehicle' parameter";
        }


        // Handle modeStr
        if (modeStr != null) {

            // Calculation rules reset to default values
            if (modeStr.equals("reset")) {

                feeRuleInitializer.InitializeDefaultRules();
                return "The fee rules were reset";
            }

            // Print current calculation rules
            if (modeStr.equals("print")) {

                String result = "";

                result = result.concat("Currently set base fee rules:<br>");

                for (BaseFeeRule rule : baseFeeRuleRepository.findAll()) {

                    result = result.concat(String.format("ID: %d - applies for %s in %s - ",
                            rule.getId(),
                            StringEnumTranslator.getStrFromVehicleType(rule.getVehicleType()),
                            StringEnumTranslator.getStrFromCity(rule.getCity())));
                    Double amount = rule.getFeeAmount();

                    if (amount == null) result = result.concat("forbidden<br>");
                    else result = result.concat(String.format("%.2f<br>", amount));
                }

                result = result.concat("<br>Currently set extra fee rules:<br>");

                for (ExtraFeeRule rule : extraFeeRuleRepository.findAll()) {

                    if (rule.getMetric() != ExtraFeeRuleMetric.PHENOMENON) {

                        result = result.concat(String.format("ID: %d - applies for %s - %s %s %.2f - ",
                                rule.getId(),
                                StringEnumTranslator.getStrFromVehicleType(rule.getVehicleType()),
                                rule.getMetric().name(), rule.getValueType().name(),
                                Double.parseDouble(rule.getValueStr())));
                        Double amount = rule.getFeeAmount();

                        if (amount == null) result = result.concat("forbidden<br>");
                        else result = result.concat(String.format("%.2f<br>", amount));

                    } else {

                        result = result.concat(String.format("ID: %d - applies for %s during %s - ",
                                rule.getId(),
                                StringEnumTranslator.getStrFromVehicleType(rule.getVehicleType()),
                                rule.getValueStr()));
                        Double amount = rule.getFeeAmount();

                        if (amount == null) result = result.concat("forbidden<br>");
                        else result = result.concat(String.format("%.2f<br>", amount));
                    }
                }

                return result;
            }

            // Remove rule with ID of ruleId
            if (modeStr.equals("remove")) {

                Long ruleId;

                try {

                    ruleId = Long.parseLong(idStr);
                } catch (Exception e) {

                    return "Unknown value for 'id' parameter";
                }

                Optional<BaseFeeRule> baseFeeRule = baseFeeRuleRepository.findById(ruleId);
                Optional<ExtraFeeRule> extraFeeRule = extraFeeRuleRepository.findById(ruleId);

                if (baseFeeRule.isPresent()) {

                    baseFeeRuleRepository.deleteById(ruleId);
                    return "Rule deleted";
                }

                if (extraFeeRule.isPresent()) {

                    extraFeeRuleRepository.deleteById(ruleId);
                    return "Rule deleted";
                }

                return "Rule not found";
            }

            // Add a rule
            if (modeStr.equals("add")) {

                // Input validation
                if (selectedVehicle == VehicleType.UNKNOWN) return "Specify 'vehicle' parameter";
                if (typeStr == null) return "Specify 'type' parameter";
                if (amountStr == null) return "Specify 'amount' parameter";

                switch (typeStr.toLowerCase()) {

                    // Adding a base fee rule
                    case "base":
                        if (selectedCity == City.UNKNOWN) return "Specify 'city' parameter";
                        try {

                            if (amountStr.equalsIgnoreCase("forbid"))
                                feeRuleInitializer.InitializeNewRule(selectedCity, selectedVehicle, null);
                            else
                                feeRuleInitializer
                                        .InitializeNewRule(selectedCity, selectedVehicle,
                                                Double.parseDouble(amountStr));
                            return "Rule added";

                        } catch (Exception e) {

                            return e.getMessage();
                        }

                        // Adding a numerical value extra fee rule
                    case "from":
                    case "until":
                        ExtraFeeRuleMetric metric = StringEnumTranslator.getMetricFromStr(metricStr);
                        ExtraFeeRuleValueType valueType = StringEnumTranslator.getValueTypeFromStr(typeStr);

                        if (metric == ExtraFeeRuleMetric.UNKNOWN) return "Unknown value for 'metric' parameter";
                        if (valueType == ExtraFeeRuleValueType.UNKNOWN) return "Unknown value for 'type' parameter";

                        try {

                            double value;

                            try {

                                value = Double.parseDouble(valueStr);
                            } catch (Exception e) {

                                return "Unknown value for 'value' parameter";
                            }
                            if (amountStr.equalsIgnoreCase("forbid"))
                                feeRuleInitializer
                                        .InitializeNewRule(metric, valueType, value, selectedVehicle, null);
                            else
                                feeRuleInitializer
                                        .InitializeNewRule(metric, valueType, value,
                                                selectedVehicle, Double.parseDouble(amountStr));
                            return "Rule added";

                        } catch (Exception e) {

                            return e.getMessage();
                        }

                        // Adding a String value extra fee rule
                    case "phenomenon":
                        if (valueStr == null) return "Specify 'value' parameter";
                        try {

                            if (amountStr.equalsIgnoreCase("forbid"))
                                feeRuleInitializer.InitializeNewRule(valueStr, selectedVehicle, null);
                            else
                                feeRuleInitializer
                                        .InitializeNewRule(valueStr, selectedVehicle, Double.parseDouble(amountStr));
                            return "Rule added";

                        } catch (Exception e) {

                            return e.getMessage();
                        }

                    default:
                        return "Unknown value for 'type' parameter";

                }
            }

            return "Unknown value for 'mode' parameter";
        }

        // Input validation
        if (selectedCity == City.UNKNOWN) return "Specify 'city' parameter";
        if (selectedVehicle == VehicleType.UNKNOWN) return "Specify 'vehicle' parameter";

        // Handle delivery fee calculation
        try {

            if (timestampStr != null)
                return String.format("%.2f",
                        deliveryService.getDeliveryFee(selectedCity, selectedVehicle, Long.parseLong(timestampStr)));
            else
                return String.format("%.2f",
                        deliveryService.getDeliveryFee(selectedCity, selectedVehicle));

        } catch (Exception e) {

            return e.getMessage();
        }
    }
}

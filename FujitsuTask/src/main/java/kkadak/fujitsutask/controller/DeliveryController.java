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

import java.time.Instant;
import java.util.List;
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
     *                     "reset", "print", "history", "disable", "add"
     * @param idStr        specifies the ID of the rule to be removed in case modeStr is set to "disable"
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
                result = result.concat("<h3>BASE FEES</h3><table style=\"border-spacing:10px\"><tr><th></th>");

                // Add table headers
                for (VehicleType vehicleType : VehicleType.values()) {
                    if (vehicleType == VehicleType.UNKNOWN) continue;
                    result = result.concat(String.format("<th>%s</th>",
                            StringEnumTranslator.getStrFromVehicleType(vehicleType)));
                }

                result = result.concat("</tr>");

                // Iterate over every valid city and vehicle type
                for (City city : City.values()) {
                    if (city == City.UNKNOWN) continue;

                    result = result.concat("<tr><td><b>" + StringEnumTranslator.getStrFromCity(city) + "</b></td>");
                    for (VehicleType vehicleType : VehicleType.values()) {
                        if (vehicleType == VehicleType.UNKNOWN) continue;

                        Optional<BaseFeeRule> rule = baseFeeRuleRepository
                                .findTopByCityAndVehicleTypeOrderByValidFromTimestampDesc(city, vehicleType);

                        // Add table data field
                        if (rule.isPresent() && rule.get().getFeeAmount() != null)
                            result = result.concat(String.format("<td>%.2f</td>", rule.get().getFeeAmount()));

                        else result = result.concat("<td>-</td>");
                    }

                    result = result.concat("</tr>");
                }

                result = result.concat("</table><br>");
                result = result.concat("<h3>EXTRA FEES</h3><table style=\"border-spacing:10px\">"
                        + "<tr><th>ID</th><th>vehicle type</th><th>condition</th><th>fee</th></tr>");

                // Iterate over every vehicle type, getting the currently valid rules for that type
                for (VehicleType vehicleType : VehicleType.values()) {
                    if (vehicleType == VehicleType.UNKNOWN) continue;

                    for (ExtraFeeRule rule : extraFeeRuleRepository.getRules(vehicleType)) {
                        result = result.concat(String.format("<tr><td>%d</td><td>%s</td>",
                                rule.getId(), StringEnumTranslator.getStrFromVehicleType(rule.getVehicleType())));

                        if (rule.getMetric() != ExtraFeeRuleMetric.PHENOMENON)
                            result = result.concat(String.format("<td>%s %s %.2f</td>",
                                    rule.getMetric().name().toLowerCase(), rule.getValueType().name().toLowerCase(),
                                    Double.parseDouble(rule.getValueStr())));
                        else result = result.concat(String.format("<td>%s</td>", rule.getValueStr()));

                        Double amount = rule.getFeeAmount();
                        if (amount == null) result = result.concat("<td><b>forbidden</b></td>");
                        else result = result.concat(String.format("<td>%.2f</td>", amount));

                        result = result.concat("</tr>");
                    }
                }

                result = result.concat("</table>");
                return result;
            }

            // Print history of fee rules
            if (modeStr.equals("history")) {
                String result = "";
                result = result.concat("<h3>BASE FEES</h3><table style=\"border-spacing:10px\">"
                        + "<tr><th>ID</th><th>city</th><th>vehicle type</th><th>valid from</th><th>fee</th></tr>");
                List<BaseFeeRule> baseFeeRuleHistory = baseFeeRuleRepository.findByOrderByValidFromTimestampDesc();

                // Iterate over every base fee rule
                for (BaseFeeRule rule : baseFeeRuleHistory) {
                    result = result.concat(String.format("<tr><td>%d</td><td>%s</td><td>%s</td><td>%d</td>",
                            rule.getId(), StringEnumTranslator.getStrFromCity(rule.getCity()),
                            StringEnumTranslator.getStrFromVehicleType(rule.getVehicleType()),
                            rule.getValidFromTimestamp()));

                    Double amount = rule.getFeeAmount();
                    if (amount == null) result = result.concat("<td><b>forbidden</b></td>");
                    else result = result.concat(String.format("<td>%.2f</td>", amount));
                    result = result.concat("</tr>");
                }

                result = result.concat("</table><br>");
                result = result.concat("<h3>EXTRA FEES</h3><table style=\"border-spacing:10px\">"
                        + "<tr><th>ID</th><th>vehicle type</th><th>condition</th>"
                        + "<th>valid from</th><th>valid until</th><th>fee</th></tr>");
                List<ExtraFeeRule> extraFeeRuleHistory = extraFeeRuleRepository.findByOrderByValidFromTimestampDesc();

                // Iterate over every base fee rule
                for (ExtraFeeRule rule : extraFeeRuleHistory) {
                    result = result.concat(String.format("<tr><td>%d</td><td>%s</td>",
                            rule.getId(), StringEnumTranslator.getStrFromVehicleType(rule.getVehicleType())));

                    if (rule.getMetric() != ExtraFeeRuleMetric.PHENOMENON)
                        result = result.concat(String.format("<td>%s %s %.2f</td>",
                                rule.getMetric().name().toLowerCase(), rule.getValueType().name().toLowerCase(),
                                Double.parseDouble(rule.getValueStr())));
                    else result = result.concat(String.format("<td>%s</td>", rule.getValueStr()));

                    result = result.concat(String.format("<td>%d</td>", rule.getValidFromTimestamp()));

                    if (rule.getValidUntilTimestamp() != null)
                        result = result.concat(String.format("<td>%d</td>", rule.getValidUntilTimestamp()));
                    else result = result.concat("<td><b>now</b></td>");

                    Double amount = rule.getFeeAmount();
                    if (amount == null) result = result.concat("<td><b>forbidden</b></td>");
                    else result = result.concat(String.format("<td>%.2f</td>", amount));

                    result = result.concat("</tr>");
                }

                result = result.concat("</table><br>");
                result = result.concat("</table>");
                return result;
            }

            // Disable rule with ID of ruleId
            if (modeStr.equals("disable")) {
                long ruleId;

                try {
                    ruleId = Long.parseLong(idStr);
                } catch (Exception e) {
                    return "Unknown value for 'id' parameter";
                }

                Optional<ExtraFeeRule> rule = extraFeeRuleRepository.findById(ruleId);
                if (rule.isPresent()) {
                    if (rule.get().getValidUntilTimestamp() != null) return "Rule already disabled";

                    rule.get().setValidUntilTimestamp(Instant.now().getEpochSecond());
                    extraFeeRuleRepository.save(rule.get());
                    return "Rule disabled";
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
                                feeRuleInitializer.InitializeNewRule(selectedCity,
                                        selectedVehicle, Double.parseDouble(amountStr));

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
                                feeRuleInitializer.InitializeNewRule(metric, valueType, value,
                                        selectedVehicle, null);
                            else
                                feeRuleInitializer.InitializeNewRule(metric, valueType, value,
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
                            else {
                                double amount;
                                try {
                                    amount = Double.parseDouble(amountStr);
                                } catch (Exception e) {
                                    return "Unknown value for 'amount' parameter";
                                }

                                feeRuleInitializer.InitializeNewRule(valueStr, selectedVehicle, amount);
                            }

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
            if (timestampStr != null) {
                long timestamp;

                try {
                    timestamp = Long.parseLong(timestampStr);
                } catch (Exception e) {
                    return "Unknown value for 'time' parameter";
                }

                return String.format("%.2f", deliveryService.getDeliveryFee(selectedCity, selectedVehicle, timestamp));
            }
            else return String.format("%.2f", deliveryService.getDeliveryFee(selectedCity, selectedVehicle));
        } catch (Exception e) {
            return e.getMessage();
        }
    }
}

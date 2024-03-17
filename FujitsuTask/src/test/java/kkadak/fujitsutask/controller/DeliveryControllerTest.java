package kkadak.fujitsutask.controller;

import kkadak.fujitsutask.enums.City;
import kkadak.fujitsutask.enums.VehicleType;
import kkadak.fujitsutask.exceptions.DeliveryFeeCalculationException;
import kkadak.fujitsutask.initializers.FeeRuleInitializer;
import kkadak.fujitsutask.model.ExtraFeeRule;
import kkadak.fujitsutask.repository.BaseFeeRuleRepository;
import kkadak.fujitsutask.repository.ExtraFeeRuleRepository;
import kkadak.fujitsutask.service.DeliveryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class DeliveryControllerTest {
    @Mock
    private DeliveryServiceImpl deliveryService;
    @Mock
    private FeeRuleInitializer feeRuleInitializer;
    @Mock
    private BaseFeeRuleRepository baseFeeRuleRepository;
    @Mock
    private ExtraFeeRuleRepository extraFeeRuleRepository;
    @InjectMocks
    private DeliveryController deliveryController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetFee_ReturnsCorrectFee() throws DeliveryFeeCalculationException {
        when(deliveryService.getDeliveryFee(City.TALLINN, VehicleType.CAR)).thenReturn(3.5);
        when(deliveryService.getDeliveryFee(City.TARTU, VehicleType.SCOOTER, 10)).thenReturn(5.0);
        assertEquals(deliveryController.getFee(null, null, null, null,
                null, null, "Tallinn", "car", null), "3.50");
        assertEquals(deliveryController.getFee(null, null, null, null,
                null, null, "TRT", "SCOOTER", "10"), "5.00");
    }

    @Test
    void testGetFee_ReturnsCorrectMessage() {
        assertEquals(deliveryController.getFee(null, null, null, null, null,
                null, "a", "car", null), "Unknown value for 'city' parameter");
        assertEquals(deliveryController.getFee(null, null, null, null, null,
                null, "TLN", "a", null), "Unknown value for 'vehicle' parameter");
        assertEquals(deliveryController.getFee(null, null, null, null, null,
                null, "PRN", "car", "a"), "Unknown value for 'time' parameter");
        assertEquals(deliveryController.getFee("reset", null, null, null, null,
                null, null, null, null), "The fee rules were reset");
        assertDoesNotThrow(() -> deliveryController.getFee("print", null, null, null,
                null, null, null, null, null));
        assertDoesNotThrow(() -> deliveryController.getFee("history", null, null, null,
                null, null, null, null, null));
        when(extraFeeRuleRepository.findById(1L)).thenReturn(
                Optional.of(new ExtraFeeRule("a", VehicleType.BIKE, 1.0) {{
                    setId(1L);
                }}));
        assertEquals(deliveryController.getFee("disable", "a", null, null,
                null, null, null, null, null), "Unknown value for 'id' parameter");
        assertEquals(deliveryController.getFee("disable", "1", null, null,
                null, null, null, null, null), "Rule disabled");
        assertEquals(deliveryController.getFee("disable", "0", null, null,
                null, null, null, null, null), "Rule not found");
        assertEquals(deliveryController.getFee("add", null, "base", null,
                null, "forbid", "Tallinn", "a", null), "Unknown value for 'vehicle' parameter");
        assertEquals(deliveryController.getFee("add", null, "base", null,
                null, "forbid", "a", "car", null), "Unknown value for 'city' parameter");
        assertEquals(deliveryController.getFee("add", null, null, null,
                null, "forbid", "Tallinn", "car", null), "Specify 'type' parameter");
        assertEquals(deliveryController.getFee("add", null, "base", null,
                null, null, "Tallinn", "car", null), "Specify 'amount' parameter");
        assertEquals(deliveryController.getFee("add", null, "base", null,
                null, "forbid", "Tallinn", "car", null), "Rule added");
        assertEquals(deliveryController.getFee("add", null, "base", null,
                null, "3", "Tallinn", "car", null), "Rule added");
        assertEquals(deliveryController.getFee("add", null, "from", null,
                "10", "2", null, "car", null), "Unknown value for 'metric' parameter");
        assertEquals(deliveryController.getFee("add", null, "from", "windspeed",
                null, "2", null, "car", null), "Unknown value for 'value' parameter");
        assertEquals(deliveryController.getFee("add", null, "from", "windspeed",
                "10", "2", null, "car", null), "Rule added");
        assertEquals(deliveryController.getFee("add", null, "from", "windspeed",
                "10", "forbid", null, "car", null), "Rule added");
        assertEquals(deliveryController.getFee("add", null, "until", null,
                "10", "2", null, "car", null), "Unknown value for 'metric' parameter");
        assertEquals(deliveryController.getFee("add", null, "until", "airtemp",
                null, "2", null, "car", null), "Unknown value for 'value' parameter");
        assertEquals(deliveryController.getFee("add", null, "until", "airtemp",
                "10", "2", null, "car", null), "Rule added");
        assertEquals(deliveryController.getFee("add", null, "until", "airtemp",
                "10", "forbid", null, "car", null), "Rule added");
        assertEquals(deliveryController.getFee("add", null, "phenomenon", null,
                null, "3", null, "car", null), "Specify 'value' parameter");
        assertEquals(deliveryController.getFee("add", null, "phenomenon", null,
                "test", null, null, "car", null), "Specify 'amount' parameter");
        assertEquals(deliveryController.getFee("add", null, "phenomenon", null,
                "test", "a", null, "car", null), "Unknown value for 'amount' parameter");
        assertEquals(deliveryController.getFee("add", null, "phenomenon", null,
                "test", "forbid", null, "car", null), "Rule added");
        assertEquals(deliveryController.getFee("add", null, "phenomenon", null,
                "test", "3", null, "car", null), "Rule added");
        assertEquals(deliveryController.getFee("add", null, "a", null,
                "test", "a", null, "car", null), "Unknown value for 'type' parameter");
        assertEquals(deliveryController.getFee("a", null, "phenomenon", null,
                "test", "a", null, "car", null), "Unknown value for 'mode' parameter");
    }
}

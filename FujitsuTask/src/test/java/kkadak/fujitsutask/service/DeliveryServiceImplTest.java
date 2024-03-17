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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.mockito.Mockito.when;

class DeliveryServiceImplTest {
    @Mock
    private WeatherDataRepository weatherDataRepository;
    @Mock
    private BaseFeeRuleRepository baseFeeRuleRepository;
    @Mock
    private ExtraFeeRuleRepository extraFeeRuleRepository;
    @InjectMocks
    private DeliveryServiceImpl deliveryService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetDeliveryFee_ReturnsCorrectCurrentFee() throws DeliveryFeeCalculationException {
        assertThrowsExactly(DeliveryFeeCalculationException.class,
                () -> deliveryService.getDeliveryFee(City.UNKNOWN, VehicleType.CAR));
        assertThrowsExactly(DeliveryFeeCalculationException.class,
                () -> deliveryService.getDeliveryFee(City.TARTU, VehicleType.UNKNOWN));
        when(baseFeeRuleRepository
                .findTopByCityAndVehicleTypeOrderByValidFromTimestampDesc(City.TALLINN, VehicleType.CAR))
                .thenReturn(Optional.of(new BaseFeeRule(City.TALLINN, VehicleType.CAR, 5D)));
        when(extraFeeRuleRepository.getRules(VehicleType.CAR)).thenReturn(new ArrayList<>() {{
            add(new ExtraFeeRule(ExtraFeeRuleMetric.WINDSPEED, ExtraFeeRuleValueType.FROM, 5D, VehicleType.CAR, 3D));
            add(new ExtraFeeRule(ExtraFeeRuleMetric.AIRTEMP, ExtraFeeRuleValueType.UNTIL, -5D, VehicleType.CAR, 2D));
            add(new ExtraFeeRule("testPhenomenon", VehicleType.CAR, 5D));
        }});
        when(weatherDataRepository
                .getTopByStationWmoOrderByTimestampDesc(WeatherStationTranslator.getWmoOfCity(City.TALLINN)))
                .thenReturn(new WeatherData(WeatherStationTranslator.getWmoOfCity(City.TALLINN), "Tallinn-Harku", -10D,
                        10D, "testPhenomenon", 10L));
        assertEquals(deliveryService.getDeliveryFee(City.TALLINN, VehicleType.CAR), 15D);
    }

    @Test
    void testGetDeliveryFee_ReturnsCorrectPastFee() throws DeliveryFeeCalculationException {
        long timestamp = 50L;

        assertThrowsExactly(DeliveryFeeCalculationException.class,
                () -> deliveryService.getDeliveryFee(City.UNKNOWN, VehicleType.CAR, timestamp));
        assertThrowsExactly(DeliveryFeeCalculationException.class,
                () -> deliveryService.getDeliveryFee(City.TARTU, VehicleType.UNKNOWN, timestamp));
        when(baseFeeRuleRepository
                .findTopByCityAndVehicleTypeAndValidFromTimestampLessThanEqualOrderByValidFromTimestampDesc(
                        City.TALLINN, VehicleType.CAR, timestamp))
                .thenReturn(Optional.of(new BaseFeeRule(City.TALLINN, VehicleType.CAR, 5D) {{
                    setValidFromTimestamp(timestamp - 10);
                }}));
        when(extraFeeRuleRepository.getRules(VehicleType.CAR, timestamp)).thenReturn(new ArrayList<>() {{
            add(new ExtraFeeRule(ExtraFeeRuleMetric.WINDSPEED, ExtraFeeRuleValueType.FROM, 5D, VehicleType.CAR, 3D));
            add(new ExtraFeeRule(ExtraFeeRuleMetric.AIRTEMP, ExtraFeeRuleValueType.UNTIL, -5D, VehicleType.CAR, 2D));
            add(new ExtraFeeRule("testPhenomenon", VehicleType.CAR, 5D));
            forEach(rule -> rule.setValidFromTimestamp(timestamp - 10));
            forEach(rule -> rule.setValidUntilTimestamp(timestamp + 10));
        }});
        when(weatherDataRepository
                .getTopByStationWmoAndTimestampLessThanEqualOrderByTimestampDesc(
                        WeatherStationTranslator.getWmoOfCity(City.TALLINN), timestamp))
                .thenReturn(new WeatherData(WeatherStationTranslator.getWmoOfCity(City.TALLINN), "Tallinn-Harku", -10D,
                        10D, "testPhenomenon", timestamp - 10));
        assertEquals(deliveryService.getDeliveryFee(City.TALLINN, VehicleType.CAR, timestamp), 15D);
    }
}

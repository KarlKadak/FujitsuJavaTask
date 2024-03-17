package kkadak.fujitsutask.translators;

import kkadak.fujitsutask.enums.City;
import kkadak.fujitsutask.enums.ExtraFeeRuleMetric;
import kkadak.fujitsutask.enums.ExtraFeeRuleValueType;
import kkadak.fujitsutask.enums.VehicleType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

class StringEnumTranslatorTest {

    @Test
    void testGetCityFromStr() {
        assertEquals(StringEnumTranslator.getCityFromStr("Tallinn"), City.TALLINN);
        assertEquals(StringEnumTranslator.getCityFromStr("TLN"), City.TALLINN);
        assertEquals(StringEnumTranslator.getCityFromStr("tartu"), City.TARTU);
        assertEquals(StringEnumTranslator.getCityFromStr("trt"), City.TARTU);
        assertEquals(StringEnumTranslator.getCityFromStr("Parnu"), City.PARNU);
        assertEquals(StringEnumTranslator.getCityFromStr("Prn"), City.PARNU);
        assertEquals(StringEnumTranslator.getCityFromStr("PÄRNU"), City.PARNU);
        assertEquals(StringEnumTranslator.getCityFromStr("a"), City.UNKNOWN);
        assertEquals(StringEnumTranslator.getCityFromStr(null), City.UNKNOWN);
    }

    @Test
    void testGetStrFromCity() {
        assertEquals(StringEnumTranslator.getStrFromCity(City.TALLINN), "Tallinn");
        assertEquals(StringEnumTranslator.getStrFromCity(City.TARTU), "Tartu");
        assertEquals(StringEnumTranslator.getStrFromCity(City.PARNU), "Pärnu");
        assertThrowsExactly(IllegalArgumentException.class, () -> StringEnumTranslator.getStrFromCity(City.UNKNOWN));
        assertThrowsExactly(NullPointerException.class, () -> StringEnumTranslator.getStrFromCity(null));
    }

    @Test
    void testGetVehicleTypeFromStr() {
        assertEquals(StringEnumTranslator.getVehicleTypeFromStr("CAR"), VehicleType.CAR);
        assertEquals(StringEnumTranslator.getVehicleTypeFromStr("Scooter"), VehicleType.SCOOTER);
        assertEquals(StringEnumTranslator.getVehicleTypeFromStr("bike"), VehicleType.BIKE);
        assertEquals(StringEnumTranslator.getVehicleTypeFromStr("a"), VehicleType.UNKNOWN);
        assertEquals(StringEnumTranslator.getVehicleTypeFromStr(null), VehicleType.UNKNOWN);
    }

    @Test
    void testGetStrFromVehicleType() {
        assertEquals(StringEnumTranslator.getStrFromVehicleType(VehicleType.CAR), "car");
        assertEquals(StringEnumTranslator.getStrFromVehicleType(VehicleType.SCOOTER), "scooter");
        assertEquals(StringEnumTranslator.getStrFromVehicleType(VehicleType.BIKE), "bike");
        assertThrowsExactly(IllegalArgumentException.class,
                () -> StringEnumTranslator.getStrFromVehicleType(VehicleType.UNKNOWN));
        assertThrowsExactly(NullPointerException.class, () -> StringEnumTranslator.getStrFromVehicleType(null));
    }

    @Test
    void testGetValueTypeFromStr() {
        assertEquals(StringEnumTranslator.getValueTypeFromStr("from"), ExtraFeeRuleValueType.FROM);
        assertEquals(StringEnumTranslator.getValueTypeFromStr("until"), ExtraFeeRuleValueType.UNTIL);
        assertEquals(StringEnumTranslator.getValueTypeFromStr("phenomenon"), ExtraFeeRuleValueType.PHENOMENON);
        assertEquals(StringEnumTranslator.getValueTypeFromStr("a"), ExtraFeeRuleValueType.UNKNOWN);
        assertEquals(StringEnumTranslator.getValueTypeFromStr(null), ExtraFeeRuleValueType.UNKNOWN);
    }

    @Test
    void testGetMetricFromStr() {
        assertEquals(StringEnumTranslator.getMetricFromStr("airtemp"), ExtraFeeRuleMetric.AIRTEMP);
        assertEquals(StringEnumTranslator.getMetricFromStr("windspeed"), ExtraFeeRuleMetric.WINDSPEED);
        assertEquals(StringEnumTranslator.getMetricFromStr("phenomenon"), ExtraFeeRuleMetric.PHENOMENON);
        assertEquals(StringEnumTranslator.getMetricFromStr("a"), ExtraFeeRuleMetric.UNKNOWN);
        assertEquals(StringEnumTranslator.getMetricFromStr(null), ExtraFeeRuleMetric.UNKNOWN);
    }
}

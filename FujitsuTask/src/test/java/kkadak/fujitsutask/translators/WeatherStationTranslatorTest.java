package kkadak.fujitsutask.translators;

import kkadak.fujitsutask.enums.City;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class WeatherStationTranslatorTest {

    @Test
    void testGetStationWmosToFetch() {
        assertEquals(WeatherStationTranslator.getStationWmosToFetch().stream().sorted().toList(),
                Stream.of(26038, 26242, 41803).sorted().toList());
    }

    @Test
    void testGetWmoOfCity() {
        assertEquals(WeatherStationTranslator.getWmoOfCity(City.TALLINN), 26038);
        assertEquals(WeatherStationTranslator.getWmoOfCity(City.TARTU), 26242);
        assertEquals(WeatherStationTranslator.getWmoOfCity(City.PARNU), 41803);
        assertThrowsExactly(NullPointerException.class, () -> WeatherStationTranslator.getWmoOfCity(City.UNKNOWN));
        assertThrowsExactly(NullPointerException.class, () -> WeatherStationTranslator.getWmoOfCity(null));
    }
}

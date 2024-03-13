package kkadak.fujitsutask.translators;

import kkadak.fujitsutask.enums.City;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * Used to translate between weather station WMO codes and their respective {@link kkadak.fujitsutask.enums.City} enums
 * <p>
 * Also declares which stations will be fetched by {@link kkadak.fujitsutask.cron.WeatherDataImporter}
 *
 * @see kkadak.fujitsutask.cron.WeatherDataImporter
 */
public class WeatherStationTranslator {

    /**
     * Declares the WMO codes of stations along with the cities for which they are used
     */
    private static final HashMap<City, Integer> citiesToStationWmos = new HashMap<>() {{
        put(City.TALLINN, 26038); // Tallinn-Harku station
        put(City.TARTU, 26242);   // Tartu-Tõravere station
        put(City.PARNU, 41803);   // Pärnu station
    }};

    /**
     * Returns the WMO codes of the stations to be fetched
     *
     * @return List of WMO codes of the stations to be fetched
     */
    public static List<Integer> getStationWmosToFetch() {
        return citiesToStationWmos.values().stream().toList();
    }

    /**
     * Returns the WMO code of the weather station used for the specified city
     *
     * @param city {@link kkadak.fujitsutask.enums.City} to query
     * @return WMO code of the weather station used for the specified city
     */
    public static int getWmoOfCity(City city) {
        return citiesToStationWmos.get(city);
    }

    /**
     * Returns the {@link kkadak.fujitsutask.enums.City}
     * for which the weather station with the specified WMO code is used
     * <p>
     * Implies there are no keys with equal values in {@link #citiesToStationWmos}
     *
     * @param wmo WMO code to query
     * @return {@link kkadak.fujitsutask.enums.City} for which the weather station with the specified WMO code is used
     */
    public static City getCityOfWmo(int wmo) {
        return citiesToStationWmos.entrySet().stream()
                .filter(entry -> Objects.equals(entry.getValue(), wmo)).toList().get(0).getKey();
    }
}

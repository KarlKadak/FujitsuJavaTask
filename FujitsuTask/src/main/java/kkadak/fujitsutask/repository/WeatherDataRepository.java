package kkadak.fujitsutask.repository;

import kkadak.fujitsutask.model.WeatherData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for interacting with the WeatherData table
 */
@Repository
public interface WeatherDataRepository extends JpaRepository<WeatherData, Long> {

    /**
     * Returns the most recent WeatherData entry for the specified station from the table
     *
     * @param stationWmo WMO code of the station
     * @return most recent WeatherData entry for the specified station from the table
     */
    WeatherData findTopByStationWmoOrderByTimestampDesc(int stationWmo);

    /**
     * Returns the most recent WeatherData entry for the specified station before the specified timestamp from the table
     *
     * @param stationWmo WMO code of the station
     * @param timestamp  latest accepted timestamp in seconds past UTC epoch
     * @return most recent WeatherData entry for the specified station before the specified timestamp from the table
     */
    WeatherData findTopByStationWmoAndTimestampLessThanEqualOrderByTimestampDesc(int stationWmo, long timestamp);

}

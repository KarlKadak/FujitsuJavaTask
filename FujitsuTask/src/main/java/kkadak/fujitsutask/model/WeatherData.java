package kkadak.fujitsutask.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/**
 * A single point of captured data for a single weather station
 */
@Entity
public class WeatherData {

    /**
     * Primary key for the WeatherData table
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * WMO code of the station
     */
    private Integer stationWmo;

    /**
     * Human-readable name of the station where the data was measured
     */
    private String stationName;

    /**
     * Air temperature in Celsius
     */
    private Double airTemp;

    /**
     * Wind speed in metres per second
     */
    private Double windSpeed;

    /**
     * Phenomenon description or cloud coverage
     */
    private String phenomenon;

    /**
     * Amount of seconds past UTC epoch when the data was measured
     */
    private Long timestamp;

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public Integer getStationWmo() { return stationWmo; }

    public void setStationWmo(Integer stationWmo) { this.stationWmo = stationWmo; }

    public String getStationName() { return stationName; }

    public void setStationName(String stationName) { this.stationName = stationName; }

    public Double getAirTemp() { return airTemp; }

    public void setAirTemp(Double airTemp) { this.airTemp = airTemp; }

    public Double getWindSpeed() { return windSpeed; }

    public void setWindSpeed(Double windSpeed) { this.windSpeed = windSpeed; }

    public String getPhenomenon() { return phenomenon; }

    public void setPhenomenon(String phenomenon) { this.phenomenon = phenomenon; }

    public Long getTimestamp() { return timestamp; }

    public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }

    /**
     * Required for JPA
     */
    protected WeatherData() {}

    /**
     * Primary constructor
     *
     * @param stationWmo  WMO code of the station
     * @param stationName human-readable name of the station where the data was measured
     * @param airTemp     air temperature in Celsius
     * @param windSpeed   wind speed in metres per second
     * @param phenomenon  phenomenon description or cloud coverage
     * @param timestamp   amount of seconds past UTC epoch when the data was measured
     */
    public WeatherData(int stationWmo, String stationName, double airTemp,
                       double windSpeed, String phenomenon, long timestamp) {

        this.stationWmo = stationWmo;
        this.stationName = stationName;
        this.airTemp = airTemp;
        this.windSpeed = windSpeed;
        this.phenomenon = phenomenon;
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {

        return String.format("[id: %d] @%d - station %d (%s) - %.1fC %.1fm/s %s",
                id, timestamp, stationWmo, stationName, airTemp, windSpeed, phenomenon);
    }

}

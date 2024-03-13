package kkadak.fujitsutask;

import kkadak.fujitsutask.cron.WeatherDataImporter;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FujitsuTaskApplication {
    private final WeatherDataImporter weatherDataImporter;

    public FujitsuTaskApplication(WeatherDataImporter weatherDataImporter) {
        this.weatherDataImporter = weatherDataImporter;
    }

    /**
     * Control flow entry point
     *
     * @param args CLI arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(FujitsuTaskApplication.class, args);
    }

    /**
     * Runs the weather data fetch once at application startup to ensure existence of relevant weather information
     */
    @Bean
    public ApplicationRunner initialWeatherDataFetch() {
        return args -> weatherDataImporter.fetchAndSave();
    }
}

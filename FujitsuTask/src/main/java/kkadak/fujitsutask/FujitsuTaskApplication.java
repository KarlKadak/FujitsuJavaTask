package kkadak.fujitsutask;

import kkadak.fujitsutask.cron.WeatherDataImporter;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@SpringBootApplication
@EnableScheduling
public class FujitsuTaskApplication {

    /**
     * Control flow entry point
     *
     * @param args CLI arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(FujitsuTaskApplication.class, args);
    }

    /**
     * Bean for TaskScheduler used for scheduling weather data import
     *
     * @return a ThreadPoolTaskScheduler
     * @see kkadak.fujitsutask.cron.WeatherDataImporter
     */
    @Bean
    public ThreadPoolTaskScheduler taskScheduler() {
        return new ThreadPoolTaskScheduler();
    }

    /**
     * Runs the weather data fetch once at application startup to ensure existence of relevant weather information
     * and schedules the method to run according to the cron expression specified in application.properties
     */
    @Bean
    public ApplicationRunner initialWeatherDataFetch(WeatherDataImporter weatherDataImporter) {
        return args -> {
            weatherDataImporter.fetchAndSave();
            weatherDataImporter.scheduleFetch();
        };
    }
}

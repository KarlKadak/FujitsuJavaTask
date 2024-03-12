package kkadak.fujitsutask;

import kkadak.fujitsutask.cron.WeatherDataImporter;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class FujitsuTaskApplication {

    private final WeatherDataImporter weatherDataImporter;

    public FujitsuTaskApplication(WeatherDataImporter weatherDataImporter) {

        this.weatherDataImporter = weatherDataImporter;
    }

    public static void main(String[] args) {
        SpringApplication.run(FujitsuTaskApplication.class, args);
    }

    @Bean
    public ApplicationRunner initialWeatherDataFetch() {

        return args -> weatherDataImporter.fetchAndSave();
    }

}

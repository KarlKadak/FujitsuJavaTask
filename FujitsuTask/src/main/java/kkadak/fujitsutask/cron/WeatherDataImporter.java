package kkadak.fujitsutask.cron;

import kkadak.fujitsutask.model.WeatherData;
import kkadak.fujitsutask.repository.WeatherDataRepository;
import kkadak.fujitsutask.translators.WeatherStationTranslator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

/**
 * Class used for importing weather data to the WeatherData table
 */
@Component
@PropertySource("classpath:application.properties")
public class WeatherDataImporter {
    private final WeatherDataRepository repository;
    private final TaskScheduler taskScheduler;

    /**
     * Cron expression gathered from application.properties based on which {@link #taskScheduler} runs
     * {@link #fetchAndSave()}
     */
    @Value("${cron.expression}")
    private String cronExpression;

    public WeatherDataImporter(WeatherDataRepository repository, TaskScheduler taskScheduler) {
        this.repository = repository;
        this.taskScheduler = taskScheduler;
    }

    /**
     * Schedules the {@link #fetchAndSave()} method to run using the cron expression specified in application.properties
     */
    public void scheduleFetch() {
        taskScheduler.schedule(this::fetchAndSave, new CronTrigger(cronExpression));
    }

    /**
     * Fetches the data from
     * <a href="https://www.ilmateenistus.ee/teenused/ilmainfo/eesti-vaatlusandmed-xml/">Estonian Environment Agency</a>
     * and saves it to the database for the stations declared in
     * {@link kkadak.fujitsutask.translators.WeatherStationTranslator}
     * <p>
     * Configured to automatically run using cron expression in application.properties file
     *
     * @see kkadak.fujitsutask.translators.WeatherStationTranslator
     */
    public void fetchAndSave() {
        final List<Integer> stationWmosToFetch = WeatherStationTranslator.getStationWmosToFetch();
        List<WeatherData> fetchedData = new ArrayList<>();
        Document doc;

        // Fetches the XML from the URL and builds a Document object from it
        // In case of Exception, prints it to the console and returns
        try {
            URL url = new URL("https://www.ilmateenistus.ee/ilma_andmed/xml/observations.php");
            URLConnection connection = url.openConnection();
            InputStream inputStream = connection.getInputStream();

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            doc = dBuilder.parse(inputStream);
            doc.getDocumentElement().normalize();
        } catch (Exception e) {
            System.out.printf("Exception thrown while fetching data: %s%n", e.getMessage());
            return;
        }

        // Attempt to retrieve the 'timestamp' attribute from the XML, in case of failure abort the function and return
        long timestamp;
        try {
            timestamp = Long.parseLong(doc.getDocumentElement().getAttribute("timestamp"));
        } catch (Exception e) {
            return;
        }

        // Build a NodeList from the 'station' elements
        NodeList nList = doc.getElementsByTagName("station");

        // Iterate over the Nodes in the NodeList
        // In case the WMO code of current Node is present in stationWmosToFetch, attempt to:
        // - parse the elements of the Node
        // - build a WeatherData object with the metrics and timestamp from XML
        // - add the new object to the database
        // In case an Exception is thrown continue to the next Node
        for (int i = 0, length = nList.getLength(); i < length; i++) {
            try {
                Node nNode = nList.item(i);
                if (nNode.getNodeType() != Node.ELEMENT_NODE) continue;
                Element eElement = (Element) nNode;
                String parsedWmoStr = eElement.getElementsByTagName("wmocode").item(0).getTextContent();
                if (parsedWmoStr.isEmpty()) continue;
                Integer parsedWmo = Integer.parseInt(parsedWmoStr);
                if (!stationWmosToFetch.contains(parsedWmo)) continue;
                fetchedData.add(new WeatherData(Integer.parseInt(eElement.getElementsByTagName("wmocode").item(0)
                            .getTextContent()),
                        eElement.getElementsByTagName("name").item(0)
                                .getTextContent(),
                        Double.parseDouble(eElement.getElementsByTagName("airtemperature").item(0)
                                .getTextContent()),
                        Double.parseDouble(eElement.getElementsByTagName("windspeed").item(0)
                                .getTextContent()),
                        eElement.getElementsByTagName("phenomenon").item(0)
                                .getTextContent(),
                        timestamp));
            } catch (Exception ignored) {
            }
        }
        repository.saveAll(fetchedData);
    }
}

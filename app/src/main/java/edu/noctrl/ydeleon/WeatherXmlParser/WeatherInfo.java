package edu.noctrl.ydeleon.WeatherXmlParser;


import java.util.ArrayList;
import java.util.List;

/**
 * Created by craig_000 on 4/26/2015.
 */
public class WeatherInfo {

    public WeatherLocation location = new WeatherLocation();
    public CurrentObservations current = new CurrentObservations();
    public List<DayForecast> forecast = new ArrayList<>(8);
    public List<String> alerts = new ArrayList<>();
}

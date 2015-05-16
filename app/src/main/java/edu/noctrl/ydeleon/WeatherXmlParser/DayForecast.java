package edu.noctrl.ydeleon.WeatherXmlParser;
import java.util.Date;

/**
 * Created by bacraig on 4/26/2015.
 */
public class DayForecast {
	public static enum DayPeriod{AM,PM}
    public Date day;
    public String icon;
    public double precipitation;
    public ForecastPeriod amForecast;
    public ForecastPeriod pmForecast;
}

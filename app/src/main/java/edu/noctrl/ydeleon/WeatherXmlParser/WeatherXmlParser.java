package edu.noctrl.ydeleon.WeatherXmlParser;


import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Helper object to parse the Weather.gov XML format
 * Created by bacraig on 4/11/2015.
 */
public class WeatherXmlParser {
    // We don't use namespaces
    private static final String ns = null;
    //2015-04-26T12:00:00-05:00
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    XmlPullParser parser;
    InputStream input;
    WeatherInfo weatherInfo;
    CurrentObservations currentObservations;
    HashMap<String, List<Date>> dateLookup = new HashMap<>();

    /**
     * Helper object to parse the Weather.gov XML format
     * @param in input stream containing the Weather.gov XML information
     * @throws XmlPullParserException
     * @throws IOException
     */
    public WeatherXmlParser(InputStream in) throws XmlPullParserException, IOException {
        this.weatherInfo = new WeatherInfo();
        this.currentObservations = weatherInfo.current;
        this.input = in;
        parser = Xml.newPullParser();
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
        parser.setInput(in, null);
    }

    /**
     * Skips the current tag and all children tags
     * @throws XmlPullParserException
     * @throws IOException
     */
    private void skip() throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }
    // For the tags, extracts their text values.

    /**
     * Reads the inner text of the current tag
     * @return tag inner text
     * @throws IOException
     * @throws XmlPullParserException
     */
    private String readText() throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    /**
     * Grabs the inner text of the tag
     * @param tag tag to retrieve inner text from
     * @return inner text of the tag
     * @throws IOException
     * @throws XmlPullParserException
     */
    protected String innerText(String tag) throws IOException, XmlPullParserException{
        Log.i("tag", "reading ..."+tag);
        String inner;
        try {
            parser.require(XmlPullParser.START_TAG, ns, tag);
            inner = readText();
            Log.i("tag", inner);
            parser.require(XmlPullParser.END_TAG, ns, tag);
        }
        catch(XmlPullParserException e){
            Log.i("tag",e.getLocalizedMessage());
            inner = "-100";
        }
        return inner;
    }

    /**
     * Shortcut method to get an attribute from a tag
     * @param attr attribute to retrieve
     * @return attribute value
     */
    protected String getAttribute(String attr){
        return parser.getAttributeValue(ns, attr);
    }


    /**
     * Gets a <code>double</code> value from the innerText of the specified tag
     * @param tag tag to retrieve the <code>double</code> value from
     * @return <code>double</code> value of the tag's inner text
     * @throws XmlPullParserException
     * @throws IOException
     */
    protected double getValue(String tag) throws XmlPullParserException, IOException{
        parser.require(XmlPullParser.START_TAG, ns, tag);
        double val = Double.NaN;
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) continue;

            String name = parser.getName();
            if (name != null && name.equalsIgnoreCase("value")) {
                try {
                    val = Double.parseDouble(innerText("value"));
                } catch (NumberFormatException nfe){
                    nfe.printStackTrace();
                }
            }
        }
        parser.next();
        return val;
    }


    /**
     * Parses the location tag, extracting the location's latitude, longitude, altitude and name
     * @throws XmlPullParserException
     * @throws IOException
     */
    protected void parseLocation() throws XmlPullParserException, IOException{
        parser.require(XmlPullParser.START_TAG, ns, "location");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) continue;

            String name = parser.getName();
            if (name.equalsIgnoreCase("point")) {
                weatherInfo.location.latitude = Double.parseDouble(parser.getAttributeValue(null, "latitude"));
                weatherInfo.location.longitude = Double.parseDouble(parser.getAttributeValue(null, "longitude"));
                skip();
            } else if (name.equalsIgnoreCase("height")) {
                weatherInfo.location.altitude = Double.parseDouble(innerText(name));
            } else if (name.equalsIgnoreCase("area-description")) {
                weatherInfo.location.name = innerText("area-description");
            } else {
                skip();
            }
        }
    }

    /**
     * Parses the value tag to extract the visibility value from the visibility sub-tag
     * @throws XmlPullParserException
     * @throws IOException
     */
    protected void parseVisibilityValueValue() throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "value");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) continue;

            String name = parser.getName();
            if (name.equalsIgnoreCase("visibility")){//dig inside value
                currentObservations.visibility = Double.parseDouble(innerText(name));
            } else {
                skip();
            }
        }
    }

    /**
     * Parses the weather-conditions tag looking for the value sub-tag to parse visibility
     * @throws XmlPullParserException
     * @throws IOException
     */
    protected void parseVisibilityValue() throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "weather-conditions");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) continue;

            String name = parser.getName();
            if (name.equalsIgnoreCase("value")){
                parseVisibilityValueValue();
            } else {
                skip();
            }
        }
    }

    /**
     * Parses the weather tag, looking for the weather-conditions sub-tags.  Extracts
     * current conditions summary from weather-conditions with type="weather-summary".
     * Looks for sub-tag weather-conditions with no type to parse visibility.
     * @throws XmlPullParserException
     * @throws IOException
     */
    protected void parseVisibility() throws XmlPullParserException, IOException{
        parser.require(XmlPullParser.START_TAG, ns, "weather");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) continue;

            String name = parser.getName();
            if (name.equalsIgnoreCase("weather-conditions")){
                String attr = parser.getAttributeValue(ns,"weather-summary");
                if(attr == null || attr.trim().length() == 0) {
                    parseVisibilityValue();
                } else {
                    currentObservations.summary = attr;
                    skip();
                }
            } else {
                skip();
            }
        }
    }

    /**
     * Parses the conditions-icon extracting the image url for the
     * current conditions image
     * @throws XmlPullParserException
     * @throws IOException
     */
    protected void parseConditions() throws XmlPullParserException, IOException{
        parser.require(XmlPullParser.START_TAG, ns, "conditions-icon");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) continue;

            String name = parser.getName();
            if(name.equalsIgnoreCase("icon-link")){
                currentObservations.imageUrl = innerText(name);
            } else {
                skip();
            }
        }
    }


    /**
     * Parses the parameters tag, extracting temperature, dewpoint, humidity, wind direction,
     * wind speed, gusts, and pressure.  Also looks for sub-tags weather and conditions-icon to
     * parse out the visibility, summary and icon data.
     * @throws XmlPullParserException
     * @throws IOException
     */
    protected void parseParameters() throws XmlPullParserException, IOException{
        parser.require(XmlPullParser.START_TAG, ns, "parameters");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) continue;

            String name = parser.getName();
            if(name == null || name.equals("NA")){continue;}
            if (name.equalsIgnoreCase("temperature")) {
                if("apparent".equalsIgnoreCase(getAttribute("type"))){
                    currentObservations.temperature = getValue(name);
                } else if("dew point".equalsIgnoreCase(getAttribute("type"))){
                    currentObservations.dewPoint = getValue(name);
                }
            } else if (name.equalsIgnoreCase("humidity")) {
                currentObservations.humidity = getValue(name);
            } else if (name.equalsIgnoreCase("weather")) {
                parseVisibility();
            } else if (name.equalsIgnoreCase("conditions-icon")){
                parseConditions();
            } else if (name.equalsIgnoreCase("direction")){
                currentObservations.windDirection = getValue(name);
            } else if (name.equalsIgnoreCase("wind-speed")){
                if("gust".equalsIgnoreCase(getAttribute("type"))){
                    currentObservations.gusts = getValue(name);
                } else if("sustained".equalsIgnoreCase(getAttribute("type"))){
                    currentObservations.windSpeed = getValue(name);
                }
            } else if(name.equalsIgnoreCase("pressure")){
                currentObservations.pressure = getValue(name);
            }
            else {
                skip();
            }
        }
    }

    /**
     * Parses the time-layout tag, extracting the timestamp of the weather data
     * @throws XmlPullParserException
     * @throws IOException
     */
    protected void parseTimestamp() throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "time-layout");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) continue;

            String name = parser.getName();
            if(name.equalsIgnoreCase("start-valid-time")){
                currentObservations.timestamp = innerText(name);
            } else {
                skip();
            }
        }
    }

    /**
     * Parses the current observations data tag.  Looks for location, time-layout,
     * and parameters sub-tags
     * @throws XmlPullParserException
     * @throws IOException
     */
    protected void parseCurrentObservations() throws XmlPullParserException, IOException{
        parser.require(XmlPullParser.START_TAG, ns, "data");
        if(!"current observations".equalsIgnoreCase(getAttribute("type")))
            throw new IllegalStateException( "expected <data type=\"current observations\">");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) continue;

            String name = parser.getName();
            if (name.equalsIgnoreCase("location")) {
                parseLocation();
            } else if(name.equalsIgnoreCase("time-layout")){
                parseTimestamp();
            } else if (name.equalsIgnoreCase("parameters")) {
                parseParameters();
            } else {
                skip();
            }
        }

    }


	/**
     * Parse the wordedForecast tag for detailed weather description
     * @throws XmlPullParserException
     * @throws IOException
     */
    protected void parseWeatherVerbose() throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "wordedForecast");
        String layoutKey = getAttribute("time-layout");
        List<Date> dates = dateLookup.get(layoutKey);
        int i = 0;
        DayForecast forecast;
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) continue;
            String name = parser.getName();
            if(name.equalsIgnoreCase("text")){
                Date slotDate = dates.get(i++);
                forecast = findForecast(slotDate, false);
                if(forecast != null){
                    if(forecast.amForecast != null &&
                            forecast.amForecast.time.equals(slotDate)){
                        forecast.amForecast.details = innerText("text");
                    } else if(forecast.pmForecast != null &&
                            forecast.pmForecast.time.equals(slotDate)){
                        forecast.pmForecast.details = innerText("text");
                    }
                } else {
                    skip();
                }
            } else {
                skip();
            }
        }
    }
	
	/**
     * Parse the conditions-icon tag for short weather summary icon
     * @throws XmlPullParserException
     * @throws IOException
     */
    protected void parseForecastIcon() throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "conditions-icon");
        String layoutKey = getAttribute("time-layout");
        List<Date> dates = dateLookup.get(layoutKey);
        int i = 0;
        DayForecast forecast;
        ForecastPeriod fp;
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) continue;
            String name = parser.getName();
            if(name.equalsIgnoreCase("icon-link")){Date slotDate = dates.get(i++);
                forecast = findForecast(slotDate, false);
                if(forecast != null) {
                    if ((forecast.amForecast != null &&
                            forecast.amForecast.time.equals(slotDate)) ||
                            forecast.amForecast == null) {
                        forecast.icon = innerText("icon-link");
                    } else {
                        skip();
                    }
                }
            } else {
                skip();
            }
        }
    }
	/**
     * Parse the weather tag for short weather summary descriptions
     * @throws XmlPullParserException
     * @throws IOException
     */
    protected void parseWeatherSummary() throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "weather");
        String layoutKey = getAttribute("time-layout");
        List<Date> dates = dateLookup.get(layoutKey);
        int i = 0;
        DayForecast forecast;
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) continue;
            String name = parser.getName();
            if(name.equalsIgnoreCase("weather-conditions")){
                Date slotDate = dates.get(i++);
                forecast = findForecast(slotDate, false);
                if(forecast != null){
                    if(forecast.amForecast != null &&
                            forecast.amForecast.time.equals(slotDate)){
                        forecast.amForecast.description = getAttribute("weather-summary");
                    } else if(forecast.pmForecast != null &&
                            forecast.pmForecast.time.equals(slotDate)){
                        forecast.pmForecast.description = getAttribute("weather-summary");
                    }
                }
                skip();
            } else {
                skip();
            }
        }
    }
	/**
     * Parse the probability-of-precipitation tag for max precip chance of a given day
     * @throws XmlPullParserException
     * @throws IOException
     */
    protected void parsePrecipitation() throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "probability-of-precipitation");
        String layoutKey = getAttribute("time-layout");
        List<Date> dates = dateLookup.get(layoutKey);
        int i = 0;
        DayForecast forecast;
        ForecastPeriod fp;
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) continue;
            String name = parser.getName();
            if(name.equalsIgnoreCase("value")){
                forecast = findForecast(dates.get(i++), false);
                if(forecast == null){
                    skip();
                } else {
                    if(getAttribute("xsi:nil") != null && getAttribute("xsi:nil").equals("true")){
                        forecast.precipitation = Math.max(forecast.precipitation, 0);
                        skip();
                    } else {
                        forecast.precipitation = Math.max(forecast.precipitation,
                                Double.parseDouble(innerText("value")));
                    }
                }
            } else {
                skip();
            }
        }
    }
	/**
     * Parse the temperature tags for min/max temperatures
     * @throws XmlPullParserException
     * @throws IOException
     */
    protected void parseForecastTemperature() throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "temperature");
        String layoutKey = getAttribute("time-layout");
        boolean min = getAttribute("type").equalsIgnoreCase("minimum");
        List<Date> dates = dateLookup.get(layoutKey);
        int i = 0;
        DayForecast forecast;
        ForecastPeriod fp;
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) continue;
            String name = parser.getName();
            if(name.equalsIgnoreCase("value")){
                forecast = findForecast(dates.get(i++), false);
                if(forecast == null){
                    skip();
                } else {
                    fp = min ? forecast.pmForecast : forecast.amForecast;
                    if(fp != null) {
                        String temp = (innerText("value").equals("")) ? "0" : innerText("value");
                        fp.temperature = Double.parseDouble(temp);
                    } else {
                        skip();
                    }
                }
            } else {
                skip();
            }
        }
    }
	/**
     * Parse the hazard tag for active alerts
     * @throws XmlPullParserException
     * @throws IOException
     */
    protected void parseHazards() throws XmlPullParserException, IOException{
        parser.require(XmlPullParser.START_TAG, ns, "hazards");
        int next = parser.next();
        while (next != XmlPullParser.END_TAG
                || (next == XmlPullParser.END_TAG
                    && !"hazards".equalsIgnoreCase(parser.getName()))) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                next = parser.next();
                continue;
            }
            String name = parser.getName();
            if(name.equalsIgnoreCase("hazardTextURL")){
                weatherInfo.alerts.add(innerText("hazardTextURL"));
            }
            next = parser.next();
        }
    }
	/**
     * Parses the parameter tags extracting the weather conditions of the forecast
     * @throws XmlPullParserException
     * @throws IOException
     */
    protected void parseForecastParameters() throws XmlPullParserException, IOException{
        parser.require(XmlPullParser.START_TAG, ns, "parameters");
        if(weatherInfo.forecast == null || weatherInfo.forecast.size() == 0){
            skip();
        }
		//fix first day if there is no "PM" forecast
        DayForecast firstDay = weatherInfo.forecast.get(0);
        if(firstDay.pmForecast == null) {
            firstDay.pmForecast = firstDay.amForecast;
            firstDay.amForecast = null;
        }
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) continue;
            String name = parser.getName();
            if(name.equalsIgnoreCase("temperature")){
                parseForecastTemperature();
            } else if(name.equalsIgnoreCase("probability-of-precipitation")){
                parsePrecipitation();}
            else if(name.equalsIgnoreCase("weather")){
                parseWeatherSummary();
            } else if(name.equalsIgnoreCase("conditions-icon")){
                parseForecastIcon();
            } else if(name.equalsIgnoreCase("wordedForecast")){
                parseWeatherVerbose();
            } else if(name.equalsIgnoreCase("hazards")){
                parseHazards();
            } else {
                skip();
            }
        }
    }
	/**
     * Initializes the ForecastPeriod with the supplied values
     * @param forecast Forecast object for the period being initialized
     * @param period AM/PM
     * @param descr Description of the time, ie Monday Night
     * @param startDate Start date of the period
     * @throws XmlPullParserException
     * @throws IOException
     */
    protected void setForecastPeriod(DayForecast forecast, DayForecast.DayPeriod period, String descr, Date startDate){
        ForecastPeriod fp =  new ForecastPeriod();
        fp.timeDesc = descr;
        fp.time = startDate;
        if(period == DayForecast.DayPeriod.AM) { forecast.amForecast = fp; }else{ forecast.pmForecast = fp;}
    }
	
	/**
     * Finds, or creates if missing, the forecast for the specified date
     * @param startDate The date of the forecast we are looking for
     * @param create flag that indicates whether or not we should create the forecast if it doesn't exist
     * @return <code>DayForecast</code> for the specified date
     * @throws XmlPullParserException
     * @throws IOException
     */
    protected DayForecast findForecast(Date startDate, boolean create){
        List<DayForecast> periods = weatherInfo.forecast;
        DayForecast forecast = null;
        Calendar cal = Calendar.getInstance();
        cal.setTime(startDate);
        int startDay = cal.get(Calendar.DAY_OF_MONTH);
        for(int i = 0;i<periods.size();i++){
            cal.setTime(periods.get(i).day);
            if(startDay == cal.get(Calendar.DAY_OF_MONTH)){
                forecast = periods.get(i);
                break;
            }
        }
        if(create && forecast == null){
            forecast = new DayForecast();
            weatherInfo.forecast.add(forecast);
            forecast.day = startDate;
        }
        return forecast;
    }
	
	/**
     * creates initial structure for Day Forecasts and Forecast Periods for a given time value
     * @throws XmlPullParserException
     * @throws IOException
     */
    protected void parseStartTimes(List<Date> dates) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "start-valid-time");
        String descr = getAttribute("period-name");
        String dateStr = innerText("start-valid-time");
        Date startDate = null;
        try{
            startDate = dateFormat.parse(dateStr);
        } catch (Exception ex){
            ex.printStackTrace();
        }
        dates.add(startDate);
        DayForecast forecast = findForecast(startDate, true);
        if(forecast.amForecast == null){
            setForecastPeriod(forecast, DayForecast.DayPeriod.AM,descr,startDate);
        } else if(forecast.pmForecast == null ) {
            if(forecast.amForecast.time.after(startDate)){
                forecast.pmForecast = forecast.amForecast;
                setForecastPeriod(forecast, DayForecast.DayPeriod.AM,descr,startDate);
            } else if(startDate.after(forecast.amForecast.time)){
                setForecastPeriod(forecast, DayForecast.DayPeriod.PM,descr,startDate);
            }
        }
        //2015-04-26T12:00:00-05:00
    }

	/**
     * Parses the time-layout tags, setting up dictionary lookups for key->date[]
	 * Also creates initial structure for Day Forecasts and Forecast Periods
     * @throws XmlPullParserException
     * @throws IOException
     */
    protected void parseTimelayouts() throws XmlPullParserException, IOException{
        parser.require(XmlPullParser.START_TAG, ns, "time-layout");
        List<Date> dates = new ArrayList<>(14);
        String layoutKey = "";
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) continue;
            String name = parser.getName();
            if(name.equalsIgnoreCase("start-valid-time")){
                parseStartTimes(dates);
            } else if(name.equalsIgnoreCase("layout-key")){
                layoutKey = innerText("layout-key");
            } else {
                skip();
            }
        }
        this.dateLookup.put(layoutKey,dates);
    }
	/**
     * Parses the data tag with the forecast information
     * @throws XmlPullParserException
     * @throws IOException
     */
    protected void parseForecast() throws XmlPullParserException, IOException{
        parser.require(XmlPullParser.START_TAG, ns, "data");
        if("current observations".equalsIgnoreCase(getAttribute("type")))
            throw new IllegalStateException( "expected <data type=\"current observations\">");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) continue;

            String name = parser.getName();
            if(name.equalsIgnoreCase("time-layout")){
                parseTimelayouts();
            } else if (name.equalsIgnoreCase("parameters")) {
                parseForecastParameters();
            } else {
                skip();
            }
        }
    }

    /**
     * Root tag parser, looks for the &lt;data tag with type="current observations"
     * @throws XmlPullParserException
     * @throws IOException
     */
    protected void parseDwml() throws XmlPullParserException, IOException{
        parser.require(XmlPullParser.START_TAG, ns, "dwml");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) continue;

            String name = parser.getName();
            // Starts by looking for the entry tag
            if (name.equalsIgnoreCase("data")) {
                if("current observations".equalsIgnoreCase(getAttribute("type")))
                    parseCurrentObservations();
                else
                    parseForecast();
            } else {
                skip();
            }
        }

    }

    /**
     * Parses the weather.gov XML weather schema and produces a CurrentObservations object
     * with the compiled information
     * @return CurrentObservations object populated with the parsed weather data
     * @throws XmlPullParserException
     * @throws IOException
     */
    public WeatherInfo parse() throws XmlPullParserException, IOException{
        try {
            parser.nextTag();
            parseDwml();
            return weatherInfo;
        } finally {
            this.input.close();
        }
    }
}

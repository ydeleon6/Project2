package edu.noctrl.ydeleon.WeatherXmlParser;

/**
 * Object that holds all weather data for our app
 * Created by bacraig on 4/11/2015.
 */
public class CurrentObservations {
    public double temperature;
    public double dewPoint;
    public double humidity;
    public double pressure;
    public double windSpeed;
    public double windDirection;
    public double gusts;
    public double visibility;
    public String imageUrl;
    public String timestamp;
    public String summary;

    /**
     * Converts the decimal value of the wind direction into valid
     * N S E W values.
     * @return NSEW value for wind direction
     */
    public String windDirectionStr(){
        if(Double.isNaN(windDirection)){
            return "";
        }
        if(windDirection >= 337.5 || windDirection < 22.5 ){
            return "N";
        } else if (windDirection < 67.5){
            return "NE";
        } else if (windDirection < 112.5) {
            return "E";
        } else if (windDirection < 157.5) {
            return "SE";
        } else if (windDirection < 202.5) {
            return "S";
        } else if (windDirection < 247.5) {
            return "SW";
        } else if (windDirection < 292.5) {
            return "W";
        } else if (windDirection < 337.5){
            return "NW";
        } else {
            return "UK";
        }
    }
}

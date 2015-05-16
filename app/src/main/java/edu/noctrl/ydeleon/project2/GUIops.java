package edu.noctrl.ydeleon.project2;

import android.app.Activity;
import android.graphics.Bitmap;
import android.util.Log;
import android.util.LruCache;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import java.text.DecimalFormat;

import edu.noctrl.ydeleon.WeatherXmlParser.CurrentObservations;
import edu.noctrl.ydeleon.WeatherXmlParser.Downloader;
import edu.noctrl.ydeleon.WeatherXmlParser.WeatherInfo;
import edu.noctrl.ydeleon.WeatherXmlParser.WeatherInfoIO;

/**
 * Created by deleon118 on 5/10/15.
 */
public class GUIops {

    private static String img;
    private static WeatherInfo currentWeather;
    private static ImageView centerPic;
    private static TextView displayConditions;
    private static EditText enterZip;
    private static TextView temperature;
    private static TextView dew;
    private static TextView humidity;
    private static TextView pressure;
    private static TextView visibility;
    private static TextView windspeed;
    private static TextView gust;
    private static TextView currTime;
    private static RadioButton metricRadio, imperialRadio;
    private static LruCache<String, Bitmap> cache;

    public GUIops(Activity activity){
        Log.i("GUIops","INSIDE THE CONSTRUCTOR");
        centerPic = (ImageView)activity.findViewById(R.id.pic);
        displayConditions = (TextView)activity.findViewById(R.id.displayConditions);
        //enterZip = (EditText)activity.findViewById(R.id.zip);
        temperature = (TextView)activity.findViewById(R.id.displayTemp);
        dew = (TextView)activity.findViewById(R.id.displayDew);
        humidity = (TextView)activity.findViewById(R.id.displayHumidity);
        pressure = (TextView)activity.findViewById(R.id.displayPressure);
        visibility = (TextView)activity.findViewById(R.id.displayVisibility);
        windspeed = (TextView)activity.findViewById(R.id.displayWindspeed);
        gust = (TextView)activity.findViewById(R.id.displayGust);
        currTime= (TextView)activity.findViewById(R.id.currentTimeDisplay);
        metricRadio = (RadioButton)activity.findViewById(R.id.metric);
        imperialRadio = (RadioButton)activity.findViewById(R.id.imperial);
        Log.i("GUIops","creating the cache");
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        // Use 1/8th of the available memory for this memory cache.
        final int cacheSize = maxMemory / 8;
        String zip = "60505"; //get zip from textview or whatever
        cache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return bitmap.getByteCount() / 1024;
            }
        };
        Log.i("GUIops","Finished inside the constructor");
    }
    public static void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();
        String getCurrUnits = temperature.getText().toString();
        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.metric:
                if (checked)
                    if(getCurrUnits.charAt(getCurrUnits.length() - 1) == 'F')
                        toMetric();
                break;
            case R.id.imperial:
                if (checked)
                    if(getCurrUnits.charAt(getCurrUnits.length() - 1) == 'C')
                        toImp();
                break;
        }
    }
    protected static void toMetric(){
        double temp, dewpoint, pres, vis, spd, gst;
        DecimalFormat df = new DecimalFormat("###0.0");

        //gets string component of text fields, removes alpha chars (from StackOverflow), and parses int
        temp = Double.parseDouble(temperature.getText().toString().replaceAll("[//A-Za-z ]*", ""));
        dewpoint = Double.parseDouble(dew.getText().toString().replaceAll("[//A-Za-z ]*", ""));
        pres = Double.parseDouble(pressure.getText().toString().replaceAll("[//A-Za-z ]*", ""));
        vis = Double.parseDouble(visibility.getText().toString().replaceAll("[//A-Za-z ]*", ""));
        spd = Double.parseDouble(windspeed.getText().toString().replaceAll("[//A-Za-z ]*", ""));


        temp = (((temp - 32)*5)/9);
        dewpoint = (((dewpoint - 32)*5)/9);
        pres = (pres * 2.54);
        vis = (vis * 1.60934);
        spd = (spd * 1.60934);

        temperature.setText(df.format(temp) + " C");
        dew.setText(df.format(dewpoint) + " C");
        pressure.setText(df.format(pres) + " mb");
        visibility.setText(df.format(vis) + " km");
        windspeed.setText(df.format(spd) + " km/h");

        if(gust.getText().equals("NA mph")){
            gust.setText("NA km/h");
        }
        else{
            gst = Double.parseDouble(gust.getText().toString().replaceAll("[//A-Za-z ]*", ""));
            gst = (gst * 1.60934) + 0.05;
            gust.setText(df.format(gst) + "km/h");
        }
    }
    protected static void toImp(){
        double temp, dewpoint, pres, vis, spd, gst;
        DecimalFormat df = new DecimalFormat("###0.0");

        //gets string component of text fields, removes apha chars (from StackOverflow), and parses int
        temp = Double.parseDouble(temperature.getText().toString().replaceAll("[//A-Za-z ]*", ""));
        dewpoint = Double.parseDouble(dew.getText().toString().replaceAll("[//A-Za-z ]*", ""));
        pres = Double.parseDouble(pressure.getText().toString().replaceAll("[//A-Za-z ]*", ""));
        vis = Double.parseDouble(visibility.getText().toString().replaceAll("[//A-Za-z ]*", ""));
        spd = Double.parseDouble(windspeed.getText().toString().replaceAll("[//A-Za-z ]*", ""));

        temp = (((temp*9)/5)+32) + 0.05;
        dewpoint = (((dewpoint*9)/5)+32) + 0.05;
        pres = (pres / 2.54) + 0.05;
        vis = (vis / 1.60934) + 0.05;
        spd = (spd / 1.60934) + 0.05;

        temperature.setText(df.format(temp) + " F");
        dew.setText(df.format(dewpoint) + " F");
        pressure.setText(df.format(pres) + " in");
        visibility.setText(df.format(vis) + " mi");
        windspeed.setText(df.format(spd) + " mph");

        if(gust.getText().equals("NA km/h")){
            gust.setText("NA mph");
        }
        else{
            gst = Double.parseDouble(gust.getText().toString().replaceAll("[//A-Za-z ]*", ""));
            gst = (gst / 1.60934) + 0.05;
            gust.setText(df.format(gst) + "mph");
        }
    }
    public static void populateCurrent(WeatherInfo info){
        if(info == null){
            Log.i("DEBUG","NULL OBJECT");
            return;
        }
        currentWeather = info;
        CurrentObservations current = info.current;
        Bitmap image = null;
        //check if image is in the cache.
        img = current.imageUrl;
        if(cache.get(img)==null){
            //download and get image.
            WeatherInfoIO.ImageListener listener = new WeatherInfoIO.ImageListener();
            Downloader<Bitmap> downloader = new Downloader<>(listener);
            downloader.execute(img);
        }
        else{
            image = cache.get(img); //get image
            GUIops.updateCurrentImage(image);//update it
        }
        temperature.setText(current.temperature+" F");
        dew.setText(current.dewPoint+" F");
        humidity.setText(current.humidity + "%");
        displayConditions.setText(current.summary);
        pressure.setText(current.pressure+" in");
        visibility.setText(current.visibility+" mi");
        windspeed.setText(current.windSpeed+" mph");
        gust.setText(current.gusts+" mph");
        currTime.setText(current.timestamp);
    }
    public static void updateCurrentImage(Bitmap image){
        centerPic.setImageBitmap(image);
    }
    public static void updateCache(Bitmap image){
        //img will always be the url that was passed into the listener, so this is okay
        //also, if you're in this method it wasn't in the cache so no need to check if key == null
        cache.put(img, image);
    }
    public static LruCache<String, Bitmap> getCache(){
        return cache;
    }
    public static WeatherInfo getCurrentWeather(){
        return currentWeather;
    }

}

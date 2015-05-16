package edu.noctrl.ydeleon.project2;

import android.app.Activity;
import android.graphics.Bitmap;
import android.util.Log;
import android.util.LruCache;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import edu.noctrl.ydeleon.WeatherXmlParser.DayForecast;

/**
 * Created by deleon118 on 5/11/15.
 */
public class ForecastOps {
    //initialize the FORECAST GUI
    public static int semaphore = 0; //0 = unlock. 1 == lock
    public static int position = 0;
    private static Bitmap pic;
    private static String[]urls = new String[8];
    private static ImageView array[];
    private static LruCache<String, Bitmap> cache;
    private static List<DayForecast> forecast = new ArrayList<>(8);

    public ForecastOps(Activity activity){
        //init pics & cache
        Log.i("FORECASTOPS", "INSIDE THE FORECAST");
        forecast = GUIops.getCurrentWeather().forecast;
        for(int i=0;i<forecast.size();i++){
            DayForecast d = forecast.get(i);
            urls[i] = d.day.toString(); //add url to array.
        }
        cache = GUIops.getCache();
        Log.i("FORECASTOPS", "DONE");
    }
    public static void setIcon(Bitmap image){
        //set the pic
        pic = image;
    }
    public static Bitmap getImage(){
        return pic;
    }
    public static void lock(){
        while(semaphore == 1){}; //spin while locked, then grab the lock
        semaphore++;
    }
    public static void unlock(){
        semaphore--; //unlock
    }
    public static String[] getUrls(){
        return urls;
    }

}

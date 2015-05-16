package edu.noctrl.ydeleon.WeatherXmlParser;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import edu.noctrl.ydeleon.project2.ForecastOps;
import edu.noctrl.ydeleon.project2.GUIops;

/**
 * Static helper class to load CurrentObservations from Weather.gov XML
 * Created by bacraig on 4/11/2015.
 */
public class WeatherInfoIO {
    public static class WeatherListener implements Downloader.DownloadListener<WeatherInfo>{
        @Override
        public WeatherInfo parseResponse(InputStream in) {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                StringBuilder string = new StringBuilder();
                JSONObject zipcode = null;
                String line = null;
                while((line=reader.readLine())!=null){
                    string.append(line);
                }
                zipcode = new JSONObject(string.toString()); //create jsonobj
                String latitude = zipcode.getString("latitude");
                String longitude = zipcode.getString("longitude");
                String weatherURL = "http://forecast.weather.gov/MapClick.php?lat="+latitude+"&lon="+longitude+"&unit=0&lg=english&FcstType=dwml";
                Log.i("DEBUG","About to open inpustream");

                InputStream forecast = new java.net.URL(weatherURL).openStream();
                Log.i("DEBUG", "OPENED STREAM");
                WeatherInfo info = WeatherInfoIO.loadFromXmlStream(forecast); //parse the weather object
                Log.i("DEBUG","WE HAVE RETURNED FROM XMLPARSER");
                return info;
            } catch (Exception ex){
                Log.e("ERROR ERROR ERROR", ex.getMessage());
                ex.printStackTrace();
                return null;
            }
        }

        @Override
        public void handleResult(WeatherInfo result) {
            GUIops.populateCurrent(result); //populate current info.
        }
    }
    public static class ForecastIconListener implements Downloader.DownloadListener<Bitmap>{

        @Override
        public Bitmap parseResponse(InputStream in) {
            Bitmap image = null;
            try{
                image = BitmapFactory.decodeStream(in);
                return image;
            }
            catch(Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        public void handleResult(Bitmap result) {
            if(result != null){
                //give it to the cache.
                GUIops.updateCache(result);
                ForecastOps.setIcon(result); //set it in a static variable
                ForecastOps.unlock();
            }
        }
    }
    public static class ImageListener implements Downloader.DownloadListener<Bitmap>{

        @Override
        public Bitmap parseResponse(InputStream in) {
            Bitmap image = null;
            try{
                image = BitmapFactory.decodeStream(in);
                return image;
            }
            catch(Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        public void handleResult(Bitmap result) {
            GUIops.updateCurrentImage(result);
            GUIops.updateCache(result);
        }
    }
    public static void loadFromUrl(String url, WeatherListener listener){
        try{
            Downloader<WeatherInfo> downloadInfo = new Downloader<>(listener);
            downloadInfo.execute(url);
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }
    /**
     * Loads weather information from an input stream containing weather xml information
     * from www.weather.gov
     * @param ios InputStream of XML data
     * @return CurrentObservations object with the parsed weather values
     */
    public static WeatherInfo loadFromXmlStream(InputStream ios){
        try{
            WeatherXmlParser parser = new WeatherXmlParser(ios);
            return parser.parse();
        } catch (Exception ex){
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Loads weather information from an XML asset file accompanying the application.
     * @param assetMgr Application's asset manager
     * @param filename Filename relative to the /assets/ directory
     * @return CurrentObservations object with the parsed weather values
     */
    public static WeatherInfo loadFromAsset(AssetManager assetMgr, String filename){
        try {
            return loadFromXmlStream(assetMgr.open(filename));
        } catch (IOException ioe) {
            Log.e("WeatherInfoIO",ioe.getMessage());
            ioe.printStackTrace();
        }
        return null;
    }

}

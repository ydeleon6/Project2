package edu.noctrl.ydeleon.project2;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.util.LruCache;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;

import edu.noctrl.ydeleon.WeatherXmlParser.WeatherInfoIO;


public class MainActivity extends ActionBarActivity {

    //SharedPreferences sp = getPreferences(Context.MODE_PRIVATE); //get shared pref
    LruCache<String, Bitmap> cache;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //init vars.
        GUIops gui = new GUIops(MainActivity.this);
        Log.i("DEBUG","initialized variables");
        //ideally, this is done through an event handler.
        /*String zip = "60505";
        //launch async
        String url = "http://craiginsdev.com/zipcodes/findzip.php?zip="+zip;
        WeatherInfoIO.WeatherListener listener = new WeatherInfoIO.WeatherListener();
        WeatherInfoIO.loadFromUrl(url, listener);*/
    }
    public void onRadioButtonClicked(View view) {
        GUIops.onRadioButtonClicked(view);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.forecast_menu, menu);
        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search_zip).getActionView();
        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default
        if (searchView != null) {
            final Menu menu_block = menu;
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

                @Override
                public boolean onQueryTextSubmit(String query) {
                    // collapse the view ?
                    boolean hasNonAlpha = query.matches("^.*[^a-zA-Z0-9 ].*$");
                    if(!hasNonAlpha) {
                        String url = "http://craiginsdev.com/zipcodes/findzip.php?zip=" + query;
                        WeatherInfoIO.WeatherListener listener = new WeatherInfoIO.WeatherListener();
                        WeatherInfoIO.loadFromUrl(url, listener);
                        menu_block.findItem(R.id.search_zip).collapseActionView();
                    }
                    else{
                        //toast saying no luck.
                        //Also, maybe do another
                    }
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    //idk, show recent 5 zips?
                    return false;
                }
            });
            Log.i("sales module", "SearchView OK");
        } else
            Log.i("sales module", "SearchView is null");

        return super.onCreateOptionsMenu(menu);
        //return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.week:
                //launch forecast
                Intent myIntent = new Intent(MainActivity.this, ForecastActivity.class);
                MainActivity.this.startActivity(myIntent);
                return true;
            case R.id.today:
                //nothing.
                return true;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}

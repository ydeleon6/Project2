package edu.noctrl.ydeleon.project2;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;


public class ForecastActivity extends ActionBarActivity implements ForecastFragment.OnFragmentInteractionListener{
    ListView list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forecast);
        //you came here from MainActivity. Call ForecastOps to set up the weekly forecast.
        Log.i("DEBUG","initialize forecast variables");
        ForecastOps ops = new ForecastOps(ForecastActivity.this);
        final String dates[] = ForecastOps.getUrls();
        CustomListAdapter adapter=new CustomListAdapter(this, dates);
        list=(ListView)findViewById(R.id.list);
        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                ///Intent choice = new Intent(ForecastActivity.this, ForecastFragment.class);
                Bundle bundle = new Bundle();
                int pos = position;
                bundle.putInt("position", pos);
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();

                Fragment newFragment = new ForecastFragment();
                newFragment.setArguments(bundle); //add bundle

                transaction.replace(R.id.fragmentee, newFragment);
                transaction.commit();
            }
        });

    }

    public void onClicked(View view) {
        //ForecastOps.onClicked(view);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_forecast, menu);
        return true;
    }

    /**
     * Override the onFragmentInteraction class from the fragment
     * @param i
     */
    public void onFragmentInteraction(int i){

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
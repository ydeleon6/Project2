package edu.noctrl.ydeleon.project2;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * Created by deleon118 on 5/11/15.
 */

public class CustomListAdapter extends ArrayAdapter<String>{

    String dates[];
    Activity context;
    public CustomListAdapter(Activity context, String[] itemname) {
        super(context, R.layout.forecast_list, itemname);
        this.dates = itemname;
        this.context = context;
    }

    public View getView(int position,View view,ViewGroup parent) {
        LayoutInflater inflater=context.getLayoutInflater();
        View rowView=inflater.inflate(R.layout.forecast_list, null,true);

        TextView txtTitle = (TextView) rowView.findViewById(R.id.day);

        txtTitle.setText(dates[position]);
        //imageView.setImageResource(imgid[position]);
        //extratxt.setText("Description "+itemname[position]);
        return rowView;

    };
}
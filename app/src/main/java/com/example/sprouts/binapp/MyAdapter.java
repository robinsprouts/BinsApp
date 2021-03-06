package com.example.sprouts.binapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by sprouts on 11/07/15.
 */
public class MyAdapter extends ArrayAdapter<String> {

    private final Context context;
    private final ArrayList<String> values;

    public MyAdapter(Context context, ArrayList values) {
        super(context, R.layout.list_item, values);
        this.context = context;
        this.values = values;
    }

    @Override

    public int getCount(){
        return 4;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View item = inflater.inflate(R.layout.list_item, parent, false);
        TextView titleView = (TextView) item.findViewById(R.id.title);
        TextView contentView = (TextView) item.findViewById(R.id.content);
        ImageView imageView = (ImageView) item.findViewById(R.id.image);

        String title = values.get(position * 2);
        String binText = values.get(position *2 +1);
        int binTextLength = binText.length();

        titleView.setText(title);
        contentView.setText(binText);

        if (binTextLength == 34) {
            imageView.setImageResource(R.drawable.ic_blackbin);
        } else {
            imageView.setImageResource(R.drawable.ic_greenbin);
        }


        return item;
    }
}

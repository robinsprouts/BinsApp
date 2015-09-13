package com.example.sprouts.binapp.util;

import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.sprouts.binapp.R;


public class MyPreference extends Preference {
    private TextView text;

    public MyPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected View onCreateView(ViewGroup parent) {

        super.onCreateView(parent);

        LayoutInflater li = (LayoutInflater)getContext().getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        return li.inflate(R.layout.my_pref, parent, false);
    }

}

//To be finished...
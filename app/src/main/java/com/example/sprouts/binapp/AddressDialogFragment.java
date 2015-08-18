package com.example.sprouts.binapp;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Spinner;

public class AddressDialogFragment extends DialogFragment {

    private Spinner spin;

    public AddressDialogFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_address_dialog, container);
        spin = (Spinner) view.findViewById(R.id.spinner);

        getDialog().setTitle("Hello");

        return view;
    }


}

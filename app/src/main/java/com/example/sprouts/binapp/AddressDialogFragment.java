package com.example.sprouts.binapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class AddressDialogFragment extends DialogFragment {

    ArrayList<String> arrayList = new ArrayList();

    public AddressDialogFragment() {
        // Required empty public constructor
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Bundle bundle = getArguments();
        arrayList = (bundle.getStringArrayList("list"));
        String numString = arrayList.get(0);
        String[] numStrings = numString.split(" ");

        numString = numStrings[3];

        arrayList.remove(0);

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, arrayList) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {

                View view;
                TextView text;

                LayoutInflater mInflater = LayoutInflater.from(getActivity());

                view = mInflater.inflate(android.R.layout.simple_list_item_1, parent, false);

                text = (TextView) view;

                text.setText(Html.fromHtml(getItem(position)));

                return view;
            }
        };


        String title;

        if (numString.equals("1")) {
            title = "Choose your address";
        } else {
            title = "Choose from " + numString + " addresses";
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title)
                .setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        String address = arrayList.get(which);

                        mListener.onComplete(address);

                    }
                });

        return builder.create();
    }



    public interface OnCompleteListener {
        void onComplete(String string);
    }

    private OnCompleteListener mListener;

    // make sure the Activity implemented it
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            this.mListener = (OnCompleteListener)activity;
        }
        catch (final ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnCompleteListener");
        }
    }

}

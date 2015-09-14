package com.example.sprouts.binapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class AddressDialogFragment extends DialogFragment {

    private ListView list;
    private String addressOutput;

    ArrayList<String> arrayList = new ArrayList();

    public AddressDialogFragment() {
        // Required empty public constructor
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Bundle bundle = getArguments();
        arrayList = bundle.getStringArrayList("list");

        String numString = arrayList.get(0);
        String[] numStrings = numString.split(" ");

        numString = numStrings[3];

        arrayList.remove(0);

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, arrayList);


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

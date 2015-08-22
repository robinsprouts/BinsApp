package com.example.sprouts.binapp;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;

import android.support.v7.internal.widget.AdapterViewCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

public class AddressDialogFragment extends DialogFragment implements AdapterView.OnItemClickListener /* implements AdapterViewCompat.OnItemClickListener*/ {

    private ListView list;
    private String addressOutput;

    ArrayList<String> arrayList = new ArrayList();

    public AddressDialogFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_address_dialog, container);
        list = (ListView) view.findViewById(R.id.dialoglist);

        Bundle bundle = getArguments();
        arrayList = bundle.getStringArrayList("list");

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, arrayList);

        list.setAdapter(arrayAdapter);
        list.setOnItemClickListener(this);

        // list.setOnItemClickListener(this);
    }



    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            addressOutput = arrayList.get(position);
            addressOutput = "168 Inverness Place";
            /*mListener.onComplete(addressOutput);*/
            dismiss();
    }


/*

    public interface OnCompleteListener {
        public void onComplete(String address);
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
*/
}

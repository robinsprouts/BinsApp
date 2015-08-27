package com.example.sprouts.binapp;

import android.app.Fragment;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;

import android.support.design.widget.Snackbar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.jaunt.Element;
import com.jaunt.Elements;
import com.jaunt.JauntException;
import com.jaunt.UserAgent;

import java.io.IOException;
import java.util.ArrayList;

public class AddressFragment extends Fragment implements AddressDialogFragment.OnCompleteListener {

    private EditText address;

    public static AddressFragment newInstance(String param1, String param2) {

        AddressFragment fragment = new AddressFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    public AddressFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_address, container, false);

        Button button = (Button) view.findViewById(R.id.ok1);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lookup();
            }
        });




        return view;

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        address = (EditText) getView().findViewById(R.id.address1);
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    /** main methods **/


    public void lookup() {

        View layoutView = getView();

        String stringUrl = "https://wastemanagementcalendar.cardiff.gov.uk/English.aspx";
        ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {



            new GetAddressTask().execute(stringUrl);

            Snackbar.make(layoutView, "Updating", Snackbar.LENGTH_SHORT).show();
        } else {
            Snackbar.make(layoutView, "Connecting", Snackbar.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onComplete(String a) {
        View layoutView = getView();
        Snackbar.make(layoutView, a, Snackbar.LENGTH_LONG).show();
        address.setText(a);
    }


    /*
        public void onComplete(String address) {
            selectText = address;

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(AddressActivity.this);

            final SharedPreferences.Editor edit = prefs.edit();
            edit.putString("address", selectText);
            edit.commit();
        }


    */
    private class GetAddressTask extends AsyncTask<String, Void, ArrayList> {

        @Override
        protected ArrayList doInBackground(String... urls) {

            ArrayList<String> a = new ArrayList();

            try {
                return jaunty2(urls[0]);
            } catch (IOException e) {
                return a;
            }

        }

        @Override
        protected void onPostExecute(ArrayList arrayList) {

            if (arrayList.size() == 2) {
                arrayList.remove(0);
            }

            dialogList(arrayList);

        }
    }

    private ArrayList jaunty2(String myurl) throws IOException {

        ArrayList<String> addressList = new ArrayList();

        String addString = address.getText().toString();

        try {

            UserAgent userAgent = new UserAgent();

            userAgent.visit(myurl);
            userAgent.doc.apply(addString);
            userAgent.doc.submit("Search");

            Element element = userAgent.doc.findFirst("<select id = droAddress>");

            element.findFirst("<option>");

            Elements options = element.findEach("<option>");


            for (Element option : options) {

                String text = option.getText();
                addressList.add(text);
            }


        } catch (JauntException e) {
            System.err.println(e);
            return addressList;
        }


        return addressList;


    }

    public void dialogList(ArrayList arrayList) {
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("list", arrayList);

        AddressDialogFragment dialog = new AddressDialogFragment();
        dialog.setArguments(bundle);
        dialog.show(getFragmentManager(), "dialog");
    }

}

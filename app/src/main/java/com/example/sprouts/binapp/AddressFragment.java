package com.example.sprouts.binapp;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import android.support.design.widget.Snackbar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.jaunt.Element;
import com.jaunt.Elements;
import com.jaunt.JauntException;
import com.jaunt.UserAgent;

import java.io.IOException;
import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AddressFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AddressFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddressFragment extends Fragment {

    private EditText address;
    private Button ok;
    private TextView textView2;
    private String selectText;

    private OnFragmentInteractionListener mListener;


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
        return inflater.inflate(R.layout.fragment_address, container, false);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        textView2 = (TextView) getView().findViewById(R.id.textView2);

        textView2.setText("MOO");
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    /** main methods **/

    /* For some reason this onclick isn't clicking!*/

    public void lookup(View view) {

        View layoutView = getView().findViewById(R.id.addressLayout);

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




    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

}

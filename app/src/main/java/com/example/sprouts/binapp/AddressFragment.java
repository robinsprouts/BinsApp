package com.example.sprouts.binapp;

import android.app.*;
import android.os.*;
import android.support.design.widget.TextInputLayout;
import android.text.*;
import android.view.*;
import android.widget.*;
import com.jaunt.*;
import java.io.*;
import java.util.*;

public class AddressFragment extends Fragment implements AddressDialogFragment.OnCompleteListener {

    private EditText address;

    private String errorText;

    private TextInputLayout textInputLayout;

    private Button button;

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

        button = (Button) view.findViewById(R.id.ok1);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lookup();
            }
        });


        textInputLayout = (TextInputLayout) view.findViewById(R.id.til);
        textInputLayout.setErrorEnabled(true);

        button.setEnabled(false);

        return view;

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        address = (EditText) getView().findViewById(R.id.address1);
        address.addTextChangedListener(watcher);
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    /** main methods **/


    public void lookup() {

        String stringUrl = "https://wastemanagementcalendar.cardiff.gov.uk/English.aspx";

            new GetAddressTask().execute(stringUrl);
    }

    @Override
    public void onComplete(String a) {
        address.setText(a);

    }

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

            if (errorText != null) {
                textInputLayout.setError(errorText);
                errorText = null;
            } else {
                textInputLayout.setError(null);
                dialogList(arrayList);
            }
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


        } catch (ResponseException e) {
            System.err.println(e);

            HttpResponse response = e.getResponse();
            if (response != null) {
                errorText = "HTTP error";
            }
            else {
                errorText = "connection error";
            }
            return addressList;
        }
        catch (SearchException e) {
            System.err.println(e);
            errorText = "address not found";
            return addressList;

        } catch (JauntException e) {
            e.printStackTrace();
            errorText = e.toString();
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

    private TextWatcher watcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

            String input = s.toString();
            int len = input.length();

            if (len >= 3) {
                button.setEnabled(true);
            } else {
                button.setEnabled(false);
            }

        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

}

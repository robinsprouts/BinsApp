package com.example.sprouts.binapp;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class AddressActivity extends AppCompatActivity implements AddressDialogFragment.OnCompleteListener  {

    private EditText address;
    private Button ok;
    private TextView textView2;
    private String selectText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address);

        address = (EditText) findViewById(R.id.address1);

    }

    @Override
    public void onComplete(String string) {
        address.setText(string);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(AddressActivity.this);

        final SharedPreferences.Editor edit = prefs.edit();
        edit.putString("address", string);
        edit.commit();

        View layoutView = findViewById(R.id.layout);
        Snackbar.make(layoutView, string, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_address, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
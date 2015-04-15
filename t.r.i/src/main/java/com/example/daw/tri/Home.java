package com.example.daw.tri;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.SQLException;


public class Home extends ActionBarActivity {

   @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
       DatabaseHandler database = new DatabaseHandler(this);
       try {
           database.createDataBase();
       } catch (IOException ioe) {
           throw new Error("Unable to create database");
       }
       try {
           database.openDataBase();
       }catch(SQLException sqle){
       }

       TextView txtonline = (TextView) findViewById(R.id.txt_online);

       Network internet = new Network(getApplicationContext());
       if (internet.isOnline()) {
           txtonline.setText("online");
           new downloadTableDay().execute();
       }
       else txtonline.setText("offline");
      // database.close();
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
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

    private class downloadTableDay extends AsyncTask<String, String, JSONObject> {
        private ProgressDialog pDialog;

        String email;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(Home.this);
            pDialog.setTitle("Komunikace se serverem");
            pDialog.setMessage("Stahování dat");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected JSONObject doInBackground(String... args) {
            Communicator talkie = new Communicator();
            JSONObject json = talkie.getTableDay();
            return json;
        }
        @Override
        protected void onPostExecute(JSONObject json) {
            try {
                DatabaseHandler db = new DatabaseHandler(getApplicationContext());
                //db.openDataBase();
                int jsonid = json.getInt("id");
                String jsonstr = json.getString("day");
                db.insertDay(jsonid,jsonstr);


                pDialog.dismiss();

            } catch (JSONException e) {
               e.printStackTrace();
            }

        }}
}

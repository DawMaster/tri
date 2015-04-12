package com.example.daw.tri;

import android.content.Context;
import android.net.ConnectivityManager;

/**
 * Created by Daw on 10.4.2015.
 */
public class Network  {

    Context context;

    // Konstruktor
    public Network(Context context) {
        this.context = context;
    }

    public boolean isOnline()
    {
        try
        {
            ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
            return cm.getActiveNetworkInfo().isConnectedOrConnecting();
        }
        catch (Exception e)
        {
            return false;
        }
    }


}
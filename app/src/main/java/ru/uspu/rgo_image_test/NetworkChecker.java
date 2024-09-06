package ru.uspu.rgo_image_test;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkChecker
{
    private Context myContext;

    public NetworkChecker(Context contextToSetUp)
    {
        this.myContext=contextToSetUp;
    }

    public boolean isNetworkAvailable()
    {
        ConnectivityManager cm = (ConnectivityManager)myContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return (netInfo!=null) && netInfo.isConnected();
    }
}
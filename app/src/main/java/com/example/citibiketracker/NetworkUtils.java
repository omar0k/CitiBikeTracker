package com.example.citibiketracker;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class NetworkUtils {
    private static final String TAG=NetworkUtils.class.getSimpleName();
    private static RequestQueue queue;
    public interface OnResponseListener{
        void onSuccess(JSONObject response);
        void onError(VolleyError error);
    }
    public static void makeRequest(Context context,String url,final OnResponseListener listener){
        queue= Volley.newRequestQueue(context);
        StringRequest stringRequest=new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject responseJson = new JSONObject(response);
                    listener.onSuccess(responseJson);
                } catch (JSONException e) {
                    Log.e(TAG, "error parsing JSON response.");
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG,"Error making request");
                listener.onError(error);
            }
        });
        queue.add(stringRequest);
    }

}

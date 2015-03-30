package com.example.boris.goglobaltask;

/**
 * Created by boris on 3/26/15.
 */

import android.app.Activity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.HashMap;

public class Api {

    private static Api instance;

    public static Api getInstance() {
        if (instance == null) {
            instance = new Api();
        }
        return instance;
    }

    private static final String BASE_URL = "http://37.233.102.48";
    private static final String FEEDBACK = "/feedback";

    private void requestPost(String url, HashMap<String, String> params, Response.Listener responseListener,
                             Response.ErrorListener errorListener, Activity obj){
        RequestQueue queue = Volley.newRequestQueue(obj);
        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, url,
                new JSONObject(params), responseListener, errorListener);
        queue.add(jsObjRequest);
    }

    public void sendFeedback(Response.Listener responseListener, Response.ErrorListener errorListener,
                             Activity obj, HashMap<String, String> params){
        String url = BASE_URL + FEEDBACK;
        requestPost(url, params, responseListener, errorListener, obj);
    }

    public void sendImage(Response.Listener responseListener, Response.ErrorListener errorListener,
                          Activity obj, HashMap<String, String> image, String url){
        requestPost(url, image,responseListener, errorListener, obj);
    }
}

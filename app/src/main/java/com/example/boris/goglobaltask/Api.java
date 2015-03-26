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

    private void requestPost(String url, HashMap params, Response.Listener responseListener, Response.ErrorListener errorListener, Activity obj){
        RequestQueue queue = Volley.newRequestQueue(obj);
        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, url, new JSONObject(params), responseListener, errorListener);
        queue.add(jsObjRequest);
    }

    public void sendFeedback(Response.Listener responseListener, Response.ErrorListener errorListener, Activity obj, HashMap params){
        String url = "http://37.233.102.48/feedback";
        requestPost(url, params, responseListener, errorListener, obj);
    }

    public void sendImage(Response.Listener responseListener, Response.ErrorListener errorListener, Activity obj, HashMap image, String url){
        requestPost(url, image,responseListener, errorListener, obj);
    }
}

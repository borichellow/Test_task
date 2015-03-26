package com.example.boris.goglobaltask;

/**
 * Created by boris on 3/22/15.
 */
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

public class FeedbackFragment extends Fragment {

    private CheckBox attachImage;
    private File lastImage = null;
    private double latitude;
    private double longitude;
    private EditText feedbackText;
    private JSONObject responseFromServer;
    private Bitmap bitmap;

    private View.OnClickListener sendFeedbackToServer = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            send();
        }
    };

    private View.OnClickListener cancel = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            FragmentTransaction fTrans;
            fTrans = getFragmentManager().beginTransaction();
            fTrans.replace(R.id.container, new OtherFragment());
            fTrans.commit();
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_feedback, container, false);

        getLastFile();
        if(lastImage != null){
            attachImage = (CheckBox) view.findViewById(R.id.sendImage);
            attachImage.setEnabled(true);

            bitmap = BitmapFactory.decodeFile(lastImage.getPath());
            ImageView image = (ImageView) view.findViewById(R.id.lastImage);
            image.setVisibility(View.VISIBLE);
            image.setImageBitmap(Bitmap.createScaledBitmap(bitmap, 400, 400, true));
        }

        Button sendFeedback = (Button) view.findViewById(R.id.buttonSendFeedback);
        sendFeedback.setOnClickListener(sendFeedbackToServer);

        feedbackText = (EditText) view.findViewById(R.id.editTextFeedback);

        Button cancelButton = (Button) view.findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(cancel);

        return view;
    }

    private void getLastFile(){
        String path = "/com.example.boris.goglobaltask/";
        File f = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), path);
        File[] files = f.listFiles();
        if (files.length > 0){
            lastImage = files[files.length - 1];
        }
    }

    private void getLocation(){
        LocationManager mLocationManager = (LocationManager) getActivity().getSystemService(getActivity().LOCATION_SERVICE);
        Location location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        latitude = location.getLatitude();
        longitude = location.getLongitude();
    }

    private String bitMapToString(Bitmap bitmap){
        ByteArrayOutputStream baos=new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,100, baos);
        byte [] b=baos.toByteArray();
        String temp=Base64.encodeToString(b, Base64.DEFAULT);
        return temp;
    }

    private boolean waitForResponse(){
        boolean flag = false;
        for(int i = 0 ; i < 30 ; i++){
            if(responseFromServer != null) {
                flag = true;
                break;
            }
            try {
                Thread.sleep(500);
            } catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
        return flag;
    }

    private void send(){

        getLocation();

        Response.Listener<JSONObject> responseListener = new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                responseFromServer = response;
            }
        };
        Response.ErrorListener errorListener = new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO Auto-generated method stub
                Toast.makeText(getActivity(), "Feedback wasn't sent :(",
                        Toast.LENGTH_SHORT).show();
            }
        };

        HashMap<String, String> params = new HashMap<String, String>();
        params.put("feedback", feedbackText.getText().toString());
        params.put("latitude", String.valueOf(latitude));
        params.put("longitude", String.valueOf(longitude));

        Api.getInstance().sendFeedback(responseListener, errorListener, getActivity(), params);

        if(attachImage.isChecked()) {
//            send request with image
            if (waitForResponse()){
                String url = "";
                try{
                    url = responseFromServer.getString("contentUrl");
                }catch (JSONException e) {
                    e.printStackTrace();
                }
                params.put("file", bitMapToString(bitmap));
                Api.getInstance().sendImage(responseListener, errorListener, getActivity(), params, url);

                Toast.makeText(getActivity(), "File was sent",
                        Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(getActivity(), "File wasn't sent :(",
                        Toast.LENGTH_SHORT).show();
            }
        }
        responseListener = null;
    }

}

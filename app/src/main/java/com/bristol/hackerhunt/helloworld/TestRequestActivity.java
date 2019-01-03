package com.bristol.hackerhunt.helloworld;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class TestRequestActivity extends AppCompatActivity {
    private RequestQueue queue;
    private final String testRequestUrl = "https://serendipity-game-controller.herokuapp.com/update";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_request);
        this.queue = Volley.newRequestQueue(this);

        // Set up the button to go back home.
        final Button goBackHomeButton = (Button) findViewById(R.id.go_home_from_test_request);
        goBackHomeButton.setOnClickListener(goHomeOnClickListener());

        // Set up the request testing button
        final Button testRequestButton = (Button) findViewById(R.id.test_request);
        JSONObject beaconDetailsJson = null;
        try {
            beaconDetailsJson = new JSONObject("{\"id\":1,\"beacons\":[]}");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        testRequestButton.setOnClickListener(makeTestRequestOnClickListener(beaconDetailsJson));
    }

    private View.OnClickListener goHomeOnClickListener() {
        return new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(TestRequestActivity.this, TestBeaconsActivity.class);
                startActivity(intent);
            }
        };
    }

    private View.OnClickListener makeTestRequestOnClickListener(final JSONObject beaconDetailsJSON) {
        return new View.OnClickListener() {
            public void onClick(View v) {
                queue.add(testRequest(beaconDetailsJSON));
            }
        };
    }

    private StringRequest testRequest(final JSONObject beaconDetailsJSON) {
        return new StringRequest (Request.Method.POST, testRequestUrl,
                new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                TextView textView = (TextView) findViewById(R.id.test_request_received);
                String text = "Server response: " + response;
                textView.setText(text);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                TextView textView = (TextView) findViewById(R.id.test_request_received);
                String text = "Server error: " + error.getCause();
                textView.setText(text);
            }
        }) {
            @Override
            public byte[] getBody() throws AuthFailureError {
                return beaconDetailsJSON.toString().getBytes();
            }

            @Override
            public String getBodyContentType() {
                return "application/json";
            }
        };
    }
}

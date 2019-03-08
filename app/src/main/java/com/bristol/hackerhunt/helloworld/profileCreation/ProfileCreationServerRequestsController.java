package com.bristol.hackerhunt.helloworld.profileCreation;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bristol.hackerhunt.helloworld.R;
import com.bristol.hackerhunt.helloworld.StringInputRunnable;

import org.json.JSONException;
import org.json.JSONObject;

public class ProfileCreationServerRequestsController implements IProfileCreationServerRequestsController {

    private final String SERVER_ADDRESS;
    private final String REGISTER_PLAYER_URL;

    private final RequestQueue requestQueue;

    private StringInputRunnable onProfileValidRunnable;
    private StringInputRunnable onProfileInvalidRunnable;

    private int statusCode = 0;

    ProfileCreationServerRequestsController(Context context) {
        this.requestQueue = Volley.newRequestQueue(context);

        this.SERVER_ADDRESS = context.getString(R.string.server_address);
        this.REGISTER_PLAYER_URL = context.getString(R.string.register_player_request);
    }

    @Override
    public void cancelAllRequests() {
        requestQueue.stop();
    }

    @Override
    public void registerPlayerRequest(String realName, String codeName) throws JSONException {
        // this is for testing.
        // onProfileValidRunnable.run("100");

        requestQueue.add(volleyRegisterPlayerRequest(realName, codeName));
    }

    @Override
    public void registerOnProfileValidRunnable(StringInputRunnable runnable) {
        this.onProfileValidRunnable = runnable;
    }

    @Override
    public void registerOnProfileInvalidRunnable(StringInputRunnable runnable) {
        this.onProfileInvalidRunnable = runnable;
    }

    private JsonObjectRequest volleyRegisterPlayerRequest(String realName, String codeName) throws JSONException {
        Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String id = response.getString("player_id");
                    onProfileValidRunnable.run(id);
                } catch (JSONException e) {
                    Log.d("Network", String.valueOf(e.getCause()));
                }
            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                if (error.networkResponse != null && error.networkResponse.statusCode == 204) {
                    onProfileInvalidRunnable.run("There is currently no game available to join.");
                }
                else if (error.networkResponse != null && error.networkResponse.statusCode == 400) {
                    onProfileInvalidRunnable.run("Codename already exists.");
                }
                else {
                    Log.d("Network","Message:" + error.toString());
                    onProfileInvalidRunnable.run("Network error.");
                }

            }
        };

        return new JsonObjectRequest(Request.Method.POST, SERVER_ADDRESS + REGISTER_PLAYER_URL,
                playerIdentifiersToJson(realName, codeName), listener, errorListener);
    }

    private JSONObject playerIdentifiersToJson(String realName, String codeName) throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("real_name", realName);
        obj.put("code_name", codeName);
        return obj;
    }
}

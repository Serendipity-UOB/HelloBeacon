package com.bristol.hackerhunt.helloworld.profileCreation;

import android.content.Context;

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
    private Runnable onProfileInvalidRunnable;

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
    public void registerPlayerRequest(String realName, String hackerName) throws JSONException {
       // this is a placeholder.
        onProfileValidRunnable.run("100");

        // TODO: requestQueue.add(volleyRegisterPlayerRequest(realName, hackerName));
    }

    @Override
    public void registerOnProfileValidRunnable(StringInputRunnable runnable) {
        this.onProfileValidRunnable = runnable;
    }

    @Override
    public void registerOnProfileInvalidRunnable(Runnable runnable) {
        this.onProfileInvalidRunnable = runnable;
    }

    private JsonObjectRequest volleyRegisterPlayerRequest(String realName, String hackerName) throws JSONException {
        Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String id = response.getString("player_id");
                    onProfileValidRunnable.run(id);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                onProfileInvalidRunnable.run();
            }
        };

        return new JsonObjectRequest(Request.Method.GET, SERVER_ADDRESS + REGISTER_PLAYER_URL,
                playerIdentifiersToJson(realName, hackerName), listener, errorListener);
    }

    private JSONObject playerIdentifiersToJson(String realName, String hackerName) throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("real_name", realName);
        obj.put("hacker_name", hackerName);
        return obj;
    }
}

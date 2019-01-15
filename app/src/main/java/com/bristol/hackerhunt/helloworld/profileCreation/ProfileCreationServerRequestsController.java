package com.bristol.hackerhunt.helloworld.profileCreation;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bristol.hackerhunt.helloworld.model.PlayerIdentifiers;

import org.json.JSONException;
import org.json.JSONObject;

public class ProfileCreationServerRequestsController implements IProfileCreationServerRequestsController {

    private static final String SERVER_ADDRESS = "";
    private static final String REGISTER_PLAYER_URL = "/registerPlayer";

    private final RequestQueue requestQueue;
    private final ProfileValid profileValid;

    ProfileCreationServerRequestsController(Context context, ProfileValid profileValid) {
        this.profileValid = profileValid;
        this.requestQueue = Volley.newRequestQueue(context);
    }

    @Override
    public void cancelAllRequests() {
        requestQueue.stop();
    }

    public void registerPlayerRequest(String realName, String hackerName, String nfcId) throws JSONException {
       // this is a placeholder.
        this.profileValid.valid = true;

        // TODO: requestQueue.add(volleyRegisterPlayerRequest(realName, hackerName, nfcId));
    }

    private JsonObjectRequest volleyRegisterPlayerRequest(String realName, String hackerName, String nfcId) throws JSONException {
        Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                profileValid.valid = true;
            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                profileValid.valid = false;
            }
        };

        return new JsonObjectRequest(Request.Method.GET, SERVER_ADDRESS + REGISTER_PLAYER_URL,
                playerIdentifiersToJson(realName, hackerName, nfcId), listener, errorListener);
    }

    private JSONObject playerIdentifiersToJson(String realName, String hackerName, String nfcId) throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("real_name", realName);
        obj.put("hacker_name", hackerName);
        obj.put("nfc_id", nfcId);
        return obj;
    }
}

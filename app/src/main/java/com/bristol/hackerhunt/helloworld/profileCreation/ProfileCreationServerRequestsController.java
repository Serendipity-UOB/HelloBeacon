package com.bristol.hackerhunt.helloworld.profileCreation;

import com.bristol.hackerhunt.helloworld.model.PlayerIdentifiers;

import org.json.JSONException;
import org.json.JSONObject;

public class ProfileCreationServerRequestsController {

    private ProfileValid profileValid;

    public ProfileCreationServerRequestsController(ProfileValid profileValid) {
        this.profileValid = profileValid;
    }

    // TODO: POST /registerPlayer { real_name, hacker_name, nfc_id }
    public void registerPlayerRequest(String realName, String hackerName, String nfcId) {
       // this is a placeholder.
        this.profileValid.valid = true;
    }

    private JSONObject playerIdentifiersToJson(PlayerIdentifiers playerIdentifiers) throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("real_name", playerIdentifiers.getRealName());
        obj.put("hacker_name", playerIdentifiers.getHackerName());
        obj.put("nfc_id", playerIdentifiers.getNfcId());
        return obj;
    }
}
